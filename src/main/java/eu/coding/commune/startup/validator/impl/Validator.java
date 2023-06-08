package eu.coding.commune.startup.validator.impl;

import java.lang.annotation.Annotation;

public interface Validator {
    Class<? extends Annotation> getSupportedAnnotation();
}
