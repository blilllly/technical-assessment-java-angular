package com.devsu.bank.service;

import com.devsu.bank.dto.ReporteDTO;
import com.devsu.bank.dto.ReporteResponse;
import com.devsu.bank.entity.Movimiento;
import com.devsu.bank.exception.RecursoNoEncontradoException;
import com.devsu.bank.repository.ClienteRepository;
import com.devsu.bank.repository.MovimientoRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public ReporteResponse generarReporte(LocalDate fechaInicio, LocalDate fechaFin, Long clienteId) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", clienteId));

        List<Movimiento> movimientos = movimientoRepository
                .findByClienteIdAndFechaRange(clienteId, fechaInicio, fechaFin);

        List<ReporteDTO> reporte = movimientos.stream()
                .map(this::toReporteDTO)
                .collect(Collectors.toList());

        String pdfBase64 = generarPdfBase64(reporte, fechaInicio, fechaFin);

        return ReporteResponse.builder()
                .reporte(reporte)
                .pdfBase64(pdfBase64)
                .build();
    }

    private ReporteDTO toReporteDTO(Movimiento m) {
        return ReporteDTO.builder()
                .fecha(m.getFecha().format(FORMATO_FECHA))
                .cliente(m.getCuenta().getCliente().getNombre())
                .numeroCuenta(m.getCuenta().getNumeroCuenta())
                .tipo(m.getCuenta().getTipoCuenta())
                .saldoInicial(m.getCuenta().getSaldoInicial())
                .estado(m.getCuenta().getEstado())
                .movimiento(m.getValor())
                .saldoDisponible(m.getSaldo())
                .build();
    }

    private String generarPdfBase64(List<ReporteDTO> reporte, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Reporte de Movimientos")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "Período: " + fechaInicio.format(FORMATO_FECHA) + " - " + fechaFin.format(FORMATO_FECHA))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            String[] headers = {"Fecha", "Cliente", "Nro Cuenta", "Tipo", "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible"};
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 12, 10, 12, 8, 12, 12}));
            table.setWidth(UnitValue.createPercentValue(100));

            for (String header : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            reporte.forEach(r -> {
                table.addCell(r.getFecha());
                table.addCell(r.getCliente());
                table.addCell(r.getNumeroCuenta());
                table.addCell(r.getTipo());
                table.addCell(r.getSaldoInicial().toPlainString());
                table.addCell(r.getEstado() ? "Activa" : "Inactiva");
                table.addCell(r.getMovimiento().toPlainString());
                table.addCell(r.getSaldoDisponible().toPlainString());
            });

            document.add(table);
            document.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }
}
