package com.daham.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<Email, String> {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  @Override
  public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
    // allow null values and empty strings, non‐null and non-empty strings are pattern‐checked
    if (str == null || str.isEmpty()) {
      return true;
    }
    var match = EMAIL_PATTERN.matcher(str);
    return match.matches();
  }
}
