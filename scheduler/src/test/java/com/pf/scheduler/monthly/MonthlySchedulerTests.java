package com.pf.scheduler.monthly;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlySchedulerTests {

    @Test
    void movesToThursdayWhenFridayIsHolidayBeforeWeekend() {
        LocalDate fridayHoliday = LocalDate.of(2027, 1, 29);
        assertEquals(
                LocalDate.of(2027, 1, 28),
                MonthlyScheduler.lastWorkingDayOfMonth(
                        YearMonth.of(2027, 1),
                        Set.of(fridayHoliday)
                )
        );
    }

    @Test
    void picksLastWeekdayBeforeWeekendWhenThereAreNoHolidays() {
        assertEquals(
                LocalDate.of(2027, 1, 29),
                MonthlyScheduler.lastWorkingDayOfMonth(YearMonth.of(2027, 1), Set.of())
        );
    }

    @Test
    void recognizesLastDayWhenItIsAWeekday() {
        assertEquals(
                LocalDate.of(2026, 4, 30),
                MonthlyScheduler.lastWorkingDayOfMonth(YearMonth.of(2026, 4), Set.of())
        );
    }

}
