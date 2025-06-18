package com.daham.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimestampUtils {
  private static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  public static LocalDateTime fromString(String timestamp) {
    return LocalDateTime.parse(timestamp, ISO_DATETIME_FORMAT);
  }

  public static String toString(LocalDateTime timestamp) {
    return timestamp.format(ISO_DATETIME_FORMAT);
  }
}
