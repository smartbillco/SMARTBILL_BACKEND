package com.mitocode.exception.user;

public class DocumentAlreadyRegisteredException extends RuntimeException {
    public DocumentAlreadyRegisteredException(String message) {
        super(message);
    }
}