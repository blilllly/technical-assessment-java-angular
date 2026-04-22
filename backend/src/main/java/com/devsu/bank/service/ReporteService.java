package com.devsu.bank.service;

import com.devsu.bank.dto.ReporteResponse;

import java.time.LocalDate;

public interface ReporteService {
    ReporteResponse generarReporte(LocalDate fechaInicio, LocalDate fechaFin, Long clienteId);
}
