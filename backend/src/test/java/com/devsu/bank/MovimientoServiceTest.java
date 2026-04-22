package com.devsu.bank;

import com.devsu.bank.dto.MovimientoRequest;
import com.devsu.bank.entity.Cliente;
import com.devsu.bank.entity.Cuenta;
import com.devsu.bank.entity.Movimiento;
import com.devsu.bank.exception.CupoDiarioExcedidoException;
import com.devsu.bank.exception.SaldoInsuficienteException;
import com.devsu.bank.repository.CuentaRepository;
import com.devsu.bank.repository.MovimientoRepository;
import com.devsu.bank.service.MovimientoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoServiceImpl movimientoService;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Jose Lema");
        cliente.setIdentificacion("001");
        cliente.setContrasena("1234");
        cliente.setEstado(true);

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("478758");
        cuenta.setTipoCuenta("Ahorro");
        cuenta.setSaldoInicial(new BigDecimal("2000"));
        cuenta.setEstado(true);
        cuenta.setCliente(cliente);
    }

    @Test
    void crearRetiro_conSaldoSuficiente_debeGuardarMovimiento() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(movimientoRepository.sumRetirosDiarios(eq(1L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoRequest request = new MovimientoRequest();
        request.setCuentaId(1L);
        request.setTipoMovimiento("Retiro");
        request.setValor(new BigDecimal("575"));
        request.setFecha(LocalDate.now());

        var response = movimientoService.crear(request);

        assertEquals(new BigDecimal("-575"), response.getValor());
        assertEquals(new BigDecimal("1425"), response.getSaldo());
    }

    @Test
    void crearRetiro_conSaldoCero_debeLanzarSaldoInsuficiente() {
        cuenta.setSaldoInicial(BigDecimal.ZERO);
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());

        MovimientoRequest request = new MovimientoRequest();
        request.setCuentaId(1L);
        request.setTipoMovimiento("Retiro");
        request.setValor(new BigDecimal("100"));
        request.setFecha(LocalDate.now());

        assertThrows(SaldoInsuficienteException.class, () -> movimientoService.crear(request));
    }

    @Test
    void crearRetiro_excedeLimiteDiario_debeLanzarCupoDiarioExcedido() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(movimientoRepository.sumRetirosDiarios(eq(1L), any(LocalDate.class)))
                .thenReturn(new BigDecimal("-900"));

        MovimientoRequest request = new MovimientoRequest();
        request.setCuentaId(1L);
        request.setTipoMovimiento("Retiro");
        request.setValor(new BigDecimal("200"));
        request.setFecha(LocalDate.now());

        assertThrows(CupoDiarioExcedidoException.class, () -> movimientoService.crear(request));
    }

    @Test
    void crearDeposito_debeIncrementarSaldo() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoRequest request = new MovimientoRequest();
        request.setCuentaId(1L);
        request.setTipoMovimiento("Depósito");
        request.setValor(new BigDecimal("600"));
        request.setFecha(LocalDate.now());

        var response = movimientoService.crear(request);

        assertEquals(new BigDecimal("600"), response.getValor());
        assertEquals(new BigDecimal("2600"), response.getSaldo());
    }
}
