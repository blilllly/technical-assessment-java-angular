package com.devsu.bank.exception;

public class CupoDiarioExcedidoException extends RuntimeException {
    public CupoDiarioExcedidoException() {
        super("Cupo diario Excedido");
    }
}
