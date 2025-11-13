package com.QuanTech.QuanTech.util;

import com.QuanTech.QuanTech.entity.Employee;

public class CreateFullName {
    public static String fullName(Employee e){
        String last = e.getLastName() == null ? "" : " " + e.getLastName();
        return e.getFirstName() + last;
    }
}
