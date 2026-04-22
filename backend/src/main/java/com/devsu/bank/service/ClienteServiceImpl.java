package com.devsu.bank.service;

import com.devsu.bank.dto.ClienteRequest;
import com.devsu.bank.dto.ClienteResponse;
import com.devsu.bank.entity.Cliente;
import com.devsu.bank.exception.DuplicadoException;
import com.devsu.bank.exception.RecursoNoEncontradoException;
import com.devsu.bank.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new DuplicadoException("Ya existe un cliente con identificación: " + request.getIdentificacion());
        }
        Cliente cliente = mapToEntity(new Cliente(), request);
        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = findById(id);
        if (!cliente.getIdentificacion().equals(request.getIdentificacion())
                && clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new DuplicadoException("Ya existe un cliente con identificación: " + request.getIdentificacion());
        }
        return mapToResponse(clienteRepository.save(mapToEntity(cliente, request)));
    }

    @Override
    public ClienteResponse actualizarParcial(Long id, ClienteRequest request) {
        Cliente cliente = findById(id);
        Optional.ofNullable(request.getNombre()).ifPresent(cliente::setNombre);
        Optional.ofNullable(request.getGenero()).ifPresent(cliente::setGenero);
        Optional.ofNullable(request.getEdad()).ifPresent(cliente::setEdad);
        Optional.ofNullable(request.getDireccion()).ifPresent(cliente::setDireccion);
        Optional.ofNullable(request.getTelefono()).ifPresent(cliente::setTelefono);
        Optional.ofNullable(request.getContrasena()).ifPresent(cliente::setContrasena);
        Optional.ofNullable(request.getEstado()).ifPresent(cliente::setEstado);
        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    public void eliminar(Long id) {
        clienteRepository.delete(findById(id));
    }

    private Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private Cliente mapToEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.getNombre());
        cliente.setGenero(request.getGenero());
        cliente.setEdad(request.getEdad());
        cliente.setIdentificacion(request.getIdentificacion());
        cliente.setDireccion(request.getDireccion());
        cliente.setTelefono(request.getTelefono());
        cliente.setContrasena(request.getContrasena());
        cliente.setEstado(request.getEstado());
        return cliente;
    }

    private ClienteResponse mapToResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .clienteId(cliente.getId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .build();
    }
}
