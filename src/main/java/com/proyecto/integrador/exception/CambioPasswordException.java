package com.proyecto.integrador.exception;

public class CambioPasswordException extends RuntimeException{
    public CambioPasswordException(String mensaje) {
        super(mensaje);
    }
}
