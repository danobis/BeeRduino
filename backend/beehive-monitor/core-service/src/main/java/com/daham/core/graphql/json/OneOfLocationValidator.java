package com.daham.core.graphql.json;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OneOfLocationValidator implements ConstraintValidator<OneOfLocation, BeehiveInputJson> {
  @Override
  public boolean isValid(BeehiveInputJson input, ConstraintValidatorContext context) {
    // allow null values
    if (input == null) {
      return true;
    }
    final boolean haveLocationId = input.getLocationId() != null;
    final boolean haveLocation = input.getLocation() != null;
    return haveLocationId ^ haveLocation;
  }
}
