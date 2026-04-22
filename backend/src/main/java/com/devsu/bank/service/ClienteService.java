package com.devsu.bank.service;

import com.devsu.bank.dto.ClienteRequest;
import com.devsu.bank.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {
    ClienteResponse crear(ClienteRequest request);
    List<ClienteResponse> listarTodos();
    ClienteResponse buscarPorId(Long id);
    ClienteResponse actualizar(Long id, ClienteRequest request);
    ClienteResponse actualizarParcial(Long id, ClienteRequest request);
    void eliminar(Long id);
}
