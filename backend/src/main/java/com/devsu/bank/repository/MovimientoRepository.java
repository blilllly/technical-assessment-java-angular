package com.devsu.bank.repository;

import com.devsu.bank.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaId(Long cuentaId);

    @Query("""
        SELECT m FROM Movimiento m
        JOIN m.cuenta c
        JOIN c.cliente cl
        WHERE cl.id = :clienteId
        AND m.fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY m.fecha DESC
        """)
    List<Movimiento> findByClienteIdAndFechaRange(
            @Param("clienteId") Long clienteId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("""
        SELECT COALESCE(SUM(m.valor), 0)
        FROM Movimiento m
        WHERE m.cuenta.id = :cuentaId
        AND m.fecha = :fecha
        AND m.valor < 0
        """)
    BigDecimal sumRetirosDiarios(
            @Param("cuentaId") Long cuentaId,
            @Param("fecha") LocalDate fecha);

    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.id = :cuentaId ORDER BY m.fecha DESC, m.id DESC")
    List<Movimiento> findByCuentaIdOrderByFechaDesc(@Param("cuentaId") Long cuentaId);
}
