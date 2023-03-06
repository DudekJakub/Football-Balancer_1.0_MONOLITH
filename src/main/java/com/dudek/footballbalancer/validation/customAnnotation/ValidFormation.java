package com.dudek.footballbalancer.validation.customAnnotation;

import com.dudek.footballbalancer.validation.FormationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = FormationValidator.class) // Your validator class
@Target({ FIELD }) // Static import from ElementType, change this to METHOD/FIELD if you want to create a validator for a single field (rather than a cross-field validation)
@Retention(RUNTIME) // Static import from RetentionPolicy
@Documented
public @interface ValidFormation {

    String message() default "{ValidFormation.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
