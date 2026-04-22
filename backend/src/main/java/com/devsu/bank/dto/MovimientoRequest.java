package com.devsu.bank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimientoRequest {

    private LocalDate fecha;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", inclusive = false, message = "El valor debe ser mayor a 0")
    private BigDecimal valor;

    @NotNull(message = "El ID de cuenta es obligatorio")
    private Long cuentaId;
}
