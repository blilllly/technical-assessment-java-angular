package com.devsu.bank.service;

import com.devsu.bank.dto.MovimientoRequest;
import com.devsu.bank.dto.MovimientoResponse;
import com.devsu.bank.entity.Cuenta;
import com.devsu.bank.entity.Movimiento;
import com.devsu.bank.exception.CupoDiarioExcedidoException;
import com.devsu.bank.exception.RecursoNoEncontradoException;
import com.devsu.bank.exception.SaldoInsuficienteException;
import com.devsu.bank.repository.CuentaRepository;
import com.devsu.bank.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoServiceImpl implements MovimientoService {

    private static final BigDecimal LIMITE_RETIRO_DIARIO = new BigDecimal("1000");

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    public MovimientoResponse crear(MovimientoRequest request) {
        Cuenta cuenta = findCuenta(request.getCuentaId());
        LocalDate fecha = Optional.ofNullable(request.getFecha()).orElse(LocalDate.now());

        BigDecimal saldoActual = calcularSaldoActual(cuenta);
        BigDecimal valor = resolverValor(request.getTipoMovimiento(), request.getValor());

        validarDebito(valor, saldoActual, cuenta.getId(), fecha);

        BigDecimal nuevoSaldo = saldoActual.add(valor);
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(fecha);
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setValor(valor);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuenta(cuenta);

        return mapToResponse(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarTodos() {
        return movimientoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponse buscarPorId(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public MovimientoResponse actualizar(Long id, MovimientoRequest request) {
        Movimiento movimiento = findById(id);
        Cuenta cuenta = findCuenta(request.getCuentaId());

        movimiento.setFecha(Optional.ofNullable(request.getFecha()).orElse(movimiento.getFecha()));
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setValor(resolverValor(request.getTipoMovimiento(), request.getValor()));
        movimiento.setCuenta(cuenta);
        recalcularSaldoMovimiento(movimiento);

        return mapToResponse(movimientoRepository.save(movimiento));
    }

    @Override
    public MovimientoResponse actualizarParcial(Long id, MovimientoRequest request) {
        Movimiento movimiento = findById(id);
        Optional.ofNullable(request.getFecha()).ifPresent(movimiento::setFecha);
        Optional.ofNullable(request.getTipoMovimiento()).ifPresent(movimiento::setTipoMovimiento);
        if (request.getValor() != null) {
            movimiento.setValor(resolverValor(movimiento.getTipoMovimiento(), request.getValor()));
        }
        if (request.getCuentaId() != null) {
            movimiento.setCuenta(findCuenta(request.getCuentaId()));
        }
        recalcularSaldoMovimiento(movimiento);
        return mapToResponse(movimientoRepository.save(movimiento));
    }

    @Override
    public void eliminar(Long id) {
        movimientoRepository.delete(findById(id));
    }

    /**
     * Determina el signo del valor según el tipo de movimiento.
     * Créditos → positivos, Débitos/Retiros → negativos.
     */
    private BigDecimal resolverValor(String tipoMovimiento, BigDecimal valor) {
        boolean esDebito = tipoMovimiento != null &&
                (tipoMovimiento.equalsIgnoreCase("Retiro") ||
                 tipoMovimiento.equalsIgnoreCase("Débito") ||
                 tipoMovimiento.equalsIgnoreCase("Debito"));
        return esDebito ? valor.abs().negate() : valor.abs();
    }

    private BigDecimal calcularSaldoActual(Cuenta cuenta) {
        return movimientoRepository.findByCuentaIdOrderByFechaDesc(cuenta.getId())
                .stream()
                .findFirst()
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
    }

    private void validarDebito(BigDecimal valor, BigDecimal saldoActual, Long cuentaId, LocalDate fecha) {
        if (valor.compareTo(BigDecimal.ZERO) >= 0) return;

        if (saldoActual.compareTo(BigDecimal.ZERO) == 0) {
            throw new SaldoInsuficienteException();
        }
        if (saldoActual.add(valor).compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException();
        }

        BigDecimal retirosDiarios = movimientoRepository.sumRetirosDiarios(cuentaId, fecha).abs();
        BigDecimal retiroNuevo = valor.abs();
        if (retirosDiarios.add(retiroNuevo).compareTo(LIMITE_RETIRO_DIARIO) > 0) {
            throw new CupoDiarioExcedidoException();
        }
    }

    private void recalcularSaldoMovimiento(Movimiento movimiento) {
        BigDecimal saldoActual = calcularSaldoActual(movimiento.getCuenta());
        movimiento.setSaldo(saldoActual.add(movimiento.getValor()));
    }

    private Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento", id));
    }

    private Cuenta findCuenta(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", cuentaId));
    }

    private MovimientoResponse mapToResponse(Movimiento m) {
        return MovimientoResponse.builder()
                .id(m.getId())
                .fecha(m.getFecha())
                .tipoMovimiento(m.getTipoMovimiento())
                .valor(m.getValor())
                .saldo(m.getSaldo())
                .cuentaId(m.getCuenta().getId())
                .numeroCuenta(m.getCuenta().getNumeroCuenta())
                .build();
    }
}
