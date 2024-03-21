package ru.practicum;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class DateTimeFormat {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String localDateTimeToString(LocalDateTime dateTime) {
        return Objects.isNull(dateTime) ? null : dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public LocalDateTime stringToLocalDateTime(String dateTime) {
        return Objects.isNull(dateTime) ?
                null : LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }
}
