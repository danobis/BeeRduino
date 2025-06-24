package com.daham.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

  @Override
  public boolean isValid(String str, ConstraintValidatorContext context) {
    // allow null values and empty strings, non‐null and non-empty strings are pattern‐checked
    if (str == null || str.isEmpty()) {
      return true;
    }
    var match = PHONE_NUMBER_PATTERN.matcher(str);
    return match.matches();
  }
}
