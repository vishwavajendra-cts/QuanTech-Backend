package com.QuanTech.QuanTech.exception.custom;

public class PasswordDoNotMatchException extends RuntimeException {
    public PasswordDoNotMatchException(String message) {
        super(message);
    }
}
