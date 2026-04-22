package com.devsu.bank.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReporteResponse {
    private List<ReporteDTO> reporte;
    private String pdfBase64;
}
