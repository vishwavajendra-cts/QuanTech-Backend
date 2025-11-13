package com.QuanTech.QuanTech.exception.custom;

public class LeaveRequestNotFoundException extends RuntimeException {
    public LeaveRequestNotFoundException(String message) {
        super(message);
    }
}
