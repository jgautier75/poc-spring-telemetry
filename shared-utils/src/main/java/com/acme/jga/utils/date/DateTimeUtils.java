package com.acme.jga.utils.date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static LocalDateTime nowIso() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime();
    }

}
