package com.daham.core.utils.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class TimestampValidator implements ConstraintValidator<ISOTimestamp, String> {
  private static final Pattern ISO_TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");

  @Override
  public boolean isValid(String str, ConstraintValidatorContext context) {
    // allow null values and empty strings, non‐null and non-empty strings are pattern‐checked
    if (str == null || str.isEmpty()) {
      return true;
    }
    var match = ISO_TIMESTAMP_PATTERN.matcher(str);
    return match.matches();
  }
}
