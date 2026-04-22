package com.devsu.bank.service;

import com.devsu.bank.dto.MovimientoRequest;
import com.devsu.bank.dto.MovimientoResponse;

import java.util.List;

public interface MovimientoService {
    MovimientoResponse crear(MovimientoRequest request);
    List<MovimientoResponse> listarTodos();
    MovimientoResponse buscarPorId(Long id);
    MovimientoResponse actualizar(Long id, MovimientoRequest request);
    MovimientoResponse actualizarParcial(Long id, MovimientoRequest request);
    void eliminar(Long id);
}
