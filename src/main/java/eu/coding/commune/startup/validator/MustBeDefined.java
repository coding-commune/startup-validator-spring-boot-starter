package eu.coding.commune.startup.validator;

import eu.coding.commune.startup.validator.model.StartupAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static eu.coding.commune.startup.validator.model.StartupAction.FAIL;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MustBeDefined {
    StartupAction otherwise() default FAIL;
    String message() default "";
}
