package com.devsu.bank.service;

import com.devsu.bank.dto.CuentaRequest;
import com.devsu.bank.dto.CuentaResponse;
import com.devsu.bank.entity.Cliente;
import com.devsu.bank.entity.Cuenta;
import com.devsu.bank.exception.DuplicadoException;
import com.devsu.bank.exception.RecursoNoEncontradoException;
import com.devsu.bank.repository.ClienteRepository;
import com.devsu.bank.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new DuplicadoException("Ya existe una cuenta con número: " + request.getNumeroCuenta());
        }
        Cliente cliente = findCliente(request.getClienteId());
        Cuenta cuenta = new Cuenta();
        mapToEntity(cuenta, request, cliente);
        return mapToResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> listarTodos() {
        return cuentaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse buscarPorId(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        Cuenta cuenta = findById(id);
        if (!cuenta.getNumeroCuenta().equals(request.getNumeroCuenta())
                && cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new DuplicadoException("Ya existe una cuenta con número: " + request.getNumeroCuenta());
        }
        Cliente cliente = findCliente(request.getClienteId());
        mapToEntity(cuenta, request, cliente);
        return mapToResponse(cuentaRepository.save(cuenta));
    }

    @Override
    public CuentaResponse actualizarParcial(Long id, CuentaRequest request) {
        Cuenta cuenta = findById(id);
        Optional.ofNullable(request.getNumeroCuenta()).ifPresent(cuenta::setNumeroCuenta);
        Optional.ofNullable(request.getTipoCuenta()).ifPresent(cuenta::setTipoCuenta);
        Optional.ofNullable(request.getSaldoInicial()).ifPresent(cuenta::setSaldoInicial);
        Optional.ofNullable(request.getEstado()).ifPresent(cuenta::setEstado);
        if (request.getClienteId() != null) {
            cuenta.setCliente(findCliente(request.getClienteId()));
        }
        return mapToResponse(cuentaRepository.save(cuenta));
    }

    @Override
    public void eliminar(Long id) {
        cuentaRepository.delete(findById(id));
    }

    private Cuenta findById(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
    }

    private Cliente findCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", clienteId));
    }

    private void mapToEntity(Cuenta cuenta, CuentaRequest request, Cliente cliente) {
        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setSaldoInicial(request.getSaldoInicial());
        cuenta.setEstado(request.getEstado());
        cuenta.setCliente(cliente);
    }

    private CuentaResponse mapToResponse(Cuenta cuenta) {
        return CuentaResponse.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getCliente().getId())
                .clienteNombre(cuenta.getCliente().getNombre())
                .build();
    }
}
