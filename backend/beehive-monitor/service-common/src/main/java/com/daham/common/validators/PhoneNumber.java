package com.daham.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
  String value() default "";
  String message() default "Invalid E.164 phone number format.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
