package com.daham.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for working with ISO 8601 formatted timestamps.
 * <p>
 * Provides methods for converting between {@link LocalDateTime}
 * and its string representation in ISO 8601 format with milliseconds.
 * This class uses a predefined {@link DateTimeFormatter} for consistency.
 * </p>
 *
 * <p>
 * Format pattern used: {@code yyyy-MM-dd'T'HH:mm:ss.SSS}
 * </p>
 *
 * @author
 * @version 1.1
 */
@SuppressWarnings("all")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimestampUtils {
  private static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /**
   * Parses an ISO 8601 formatted timestamp string into a {@link LocalDateTime} object.
   *
   * @param timestamp The ISO 8601 string to parse.
   * @return A {@link LocalDateTime} representing the parsed timestamp.
   */
  public static LocalDateTime fromString(String timestamp) {
    return LocalDateTime.parse(timestamp, ISO_DATETIME_FORMAT);
  }

  /**
   * Formats a {@link LocalDateTime} object into an ISO 8601 formatted timestamp string.
   *
   * @param timestamp The {@link LocalDateTime} to format.
   * @return A string representation of the timestamp in ISO 8601 format.
   */
  public static String toString(LocalDateTime timestamp) {
    return timestamp.format(ISO_DATETIME_FORMAT);
  }
}
