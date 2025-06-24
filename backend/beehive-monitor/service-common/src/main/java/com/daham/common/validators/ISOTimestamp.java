package com.daham.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimestampValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ISOTimestamp {
  String value() default "";
  String message() default "Invalid ISO 8601 timestamp format. Expected format: yyyy-MM-dd'T'HH:mm:ss.SSS";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
