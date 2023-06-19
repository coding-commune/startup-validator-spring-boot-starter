package eu.coding.commune.startup.validator.model.field;

import lombok.Builder;

import java.lang.reflect.Field;

@Builder
public record FieldData(
        Field field,
        String propertyName,
        Object resolvedValue
) {}
