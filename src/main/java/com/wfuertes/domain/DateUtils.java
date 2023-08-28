package com.wfuertes.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    public static LocalDateTime utcLocalDateTime() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
