package com.daham.core.graphql.json;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OneOfLocationValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OneOfLocation {
  String value() default "";
  String message() default "Either <location_uuid> or <location> must be specified";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
