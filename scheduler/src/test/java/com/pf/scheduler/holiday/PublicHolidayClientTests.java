package com.pf.scheduler.holiday;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicHolidayClientTests {
    @Test
    void parsesOnlyDatesForRequestedYearFromIcsCalendar() {
        String calendar = """
                BEGIN:VCALENDAR
                BEGIN:VEVENT
                DTSTART;VALUE=DATE:20270129
                SUMMARY:Public Holiday
                END:VEVENT
                BEGIN:VEVENT
                DTSTART;VALUE=DATE:20280128
                SUMMARY:Next Year Holiday
                END:VEVENT
                END:VCALENDAR
                """;
        assertEquals(
                Set.of(LocalDate.of(2027, 1, 29)),
                PublicHolidayClient.parseIcsDates(calendar, 2027)
        );
    }
}
