package com.test.template.validation.annotations;

import com.test.template.validation.TextContentConstraint;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.test.template.validation.ValidationConstants.DEFAULT_CONTENT_CONSTRAINT;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TextContentConstraint.class)
public @interface ValidContent {

    String message() default "Template is not valid";
    int length() default DEFAULT_CONTENT_CONSTRAINT;
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
