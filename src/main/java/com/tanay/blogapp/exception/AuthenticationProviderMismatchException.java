package com.tanay.blogapp.exception;

public class AuthenticationProviderMismatchException extends RuntimeException {
    public AuthenticationProviderMismatchException(String message) {
        super(message);
    }
}
