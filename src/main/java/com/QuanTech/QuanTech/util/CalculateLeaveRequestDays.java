package com.QuanTech.QuanTech.util;

import com.QuanTech.QuanTech.entity.LeaveRequest;

import java.time.temporal.ChronoUnit;

public class CalculateLeaveRequestDays {
    public static long getLeaveRequestDays(LeaveRequest lr) {
        return ChronoUnit.DAYS.between(
                lr.getStartDate(),
                lr.getEndDate()
        ) + 1;
    }
}
