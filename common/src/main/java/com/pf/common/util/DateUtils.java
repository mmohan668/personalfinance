package com.pf.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateUtils {

    public static LocalDate toLocalDate(String date) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("M/d/").appendValueReduced(ChronoField.YEAR, 2, 4, 2000).toFormatter();
            return LocalDate.parse(date, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
