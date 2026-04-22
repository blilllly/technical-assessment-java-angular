package com.devsu.bank.service;

import com.devsu.bank.dto.CuentaRequest;
import com.devsu.bank.dto.CuentaResponse;

import java.util.List;

public interface CuentaService {
    CuentaResponse crear(CuentaRequest request);
    List<CuentaResponse> listarTodos();
    CuentaResponse buscarPorId(Long id);
    CuentaResponse actualizar(Long id, CuentaRequest request);
    CuentaResponse actualizarParcial(Long id, CuentaRequest request);
    void eliminar(Long id);
}
