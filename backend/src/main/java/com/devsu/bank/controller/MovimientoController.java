package com.devsu.bank.controller;

import com.devsu.bank.dto.MovimientoRequest;
import com.devsu.bank.dto.MovimientoResponse;
import com.devsu.bank.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponse> crear(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> listar() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.ok(movimientoService.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovimientoResponse> actualizarParcial(
            @PathVariable Long id,
            @RequestBody MovimientoRequest request) {
        return ResponseEntity.ok(movimientoService.actualizarParcial(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
