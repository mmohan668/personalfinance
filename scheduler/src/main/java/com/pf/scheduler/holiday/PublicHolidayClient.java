package com.pf.scheduler.holiday;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PublicHolidayClient {

    private static final Pattern ICS_DATE = Pattern.compile("^DTSTART(?:;[^:]*)?:(\\d{8})$");
    private final RestClient nagerClient = RestClient.create("https://date.nager.at");
    private final RestClient officeHolidaysClient = RestClient.create("https://www.officeholidays.com");
    private final ConcurrentHashMap<HolidayKey, Set<LocalDate>> holidayCache = new ConcurrentHashMap<>();

    public Set<LocalDate> fetchPublicHolidays(
            String countryCode,
            String countryName,
            String stateCode,
            String stateName,
            int year
    ) {
        HolidayKey key = new HolidayKey(countryCode, countryName, stateCode, stateName, year);
        return holidayCache.computeIfAbsent(key, this::loadPublicHolidays);
    }

    private Set<LocalDate> loadPublicHolidays(HolidayKey key) {
        List<Holiday> holidays = nagerClient.get()
                .uri("/api/v3/PublicHolidays/{year}/{countryCode}", key.year(), key.countryCode())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (holidays == null) {
            return fetchOfficeHolidaysCalendar(key);
        }

        return holidays.stream()
                .filter(holiday -> holiday.types() != null && holiday.types().contains("Public"))
                .filter(holiday -> holiday.counties() == null
                        || holiday.counties().isEmpty()
                        || holiday.counties().contains(key.stateCode()))
                .map(holiday -> LocalDate.parse(holiday.date()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<LocalDate> fetchOfficeHolidaysCalendar(HolidayKey key) {
        String countrySlug = switch (key.countryCode()) {
            case "IN" -> "india";
            case "US" -> "usa";
            default -> slug(key.countryName());
        };
        String calendar = officeHolidaysClient.get()
                .uri("/ics/{country}/{state}", countrySlug, slug(key.stateName()))
                .retrieve()
                .body(String.class);
        if (calendar == null || !calendar.contains("BEGIN:VCALENDAR")) {
            throw new IllegalStateException(
                    "No public holiday calendar available for " +
                            key.countryCode() + "/" + key.stateCode() + " in " + key.year()
            );
        }
        return parseIcsDates(calendar, key.year());
    }

    static Set<LocalDate> parseIcsDates(String calendar, int year) {
        String unfolded = calendar.replaceAll("\\r?\\n[ \\t]", "");
        return unfolded.lines()
                .map(String::trim)
                .map(ICS_DATE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(date -> LocalDate.of(
                        Integer.parseInt(date.substring(0, 4)),
                        Integer.parseInt(date.substring(4, 6)),
                        Integer.parseInt(date.substring(6, 8))
                ))
                .filter(date -> date.getYear() == year)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String slug(String name) {
        return name.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private record HolidayKey(
            String countryCode,
            String countryName,
            String stateCode,
            String stateName,
            int year
    ) {
    }

    private record Holiday(
            String date,
            List<String> counties,
            List<String> types
    ) {
    }
}
