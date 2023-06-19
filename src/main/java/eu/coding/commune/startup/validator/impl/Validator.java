package eu.coding.commune.startup.validator.impl;

import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;

public abstract class Validator {

    protected ApplicationContext applicationContext;

    protected abstract Class<? extends Annotation> getSupportedAnnotation();

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
