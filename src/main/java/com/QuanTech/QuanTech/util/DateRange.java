package com.QuanTech.QuanTech.util;

import java.time.*;

public final class DateRange {
    private DateRange() {}

    public static DateRangeRecord forLocalDate(LocalDate date, ZoneId zone) {
        ZonedDateTime startZdt = date.atStartOfDay(zone);
        ZonedDateTime endZdt = date.plusDays(1).atStartOfDay(zone).minusNanos(1);
        return new DateRangeRecord(startZdt.toOffsetDateTime(), endZdt.toOffsetDateTime());
    }

    public record DateRangeRecord(OffsetDateTime start, OffsetDateTime end) {}
}
