package com.daham.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Email {
  String value() default "";
  String message() default "Invalid email format. Expected format: local-part@domain (e.g. user@example.com)";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
