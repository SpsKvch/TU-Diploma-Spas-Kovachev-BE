package com.test.template.validation.annotations;

import com.test.template.validation.StepsConstraint;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StepsConstraint.class)
public @interface ValidSteps {

    String message() default "Invalid steps";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
