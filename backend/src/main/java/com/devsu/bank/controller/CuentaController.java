package com.devsu.bank.controller;

import com.devsu.bank.dto.CuentaRequest;
import com.devsu.bank.dto.CuentaResponse;
import com.devsu.bank.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar() {
        return ResponseEntity.ok(cuentaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CuentaRequest request) {
        return ResponseEntity.ok(cuentaService.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizarParcial(
            @PathVariable Long id,
            @RequestBody CuentaRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
