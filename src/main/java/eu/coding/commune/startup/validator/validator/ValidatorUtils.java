package eu.coding.commune.startup.validator.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ValidatorUtils {

    static Optional<String> getValueAnnotationValue(Field field) {
        if (field.isAnnotationPresent(Value.class)) {
            return Optional.of(field.getAnnotation(Value.class).value());
        } else {
            return Optional.empty();
        }
    }

    static Optional<String> getValueAnnotationValue(Method method) {
        if (method.isAnnotationPresent(Value.class)) {
            return Optional.of(method.getAnnotation(Value.class).value());
        } else {
            return Optional.empty();
        }
    }

    static String resolveProperty(ConfigurableApplicationContext applicationContext, String property, boolean isSetUsingValueAnnotation) {
        String resolved = null;
        if (isSetUsingValueAnnotation) {
            try {
                resolved = applicationContext.getEnvironment().resolveRequiredPlaceholders(property);
            } catch (IllegalArgumentException ignored) {}
        } else {
            resolved = applicationContext.getEnvironment().getProperty(property);
        }
        return nonNull(resolved) ? resolved : "";
    }

    static String getPropertyName(String placeholder) {
        if (placeholder.contains(":"))
            return placeholder.substring(placeholder.indexOf(":") + 1, placeholder.length() - 1);
        if (placeholder.charAt(0) == '$')
            return placeholder.substring(2, placeholder.length() - 1);
        return placeholder;
    }

    static boolean isFieldResolvable(Field field, String value) {
        Class<?> clazz = field.getType();
        try {
            if (clazz.equals(Boolean.TYPE)) {
                resolveBoolean(value);
            } else if (clazz.equals(Integer.TYPE)) {
                Integer.parseInt(value);
            } else if (clazz.equals(Long.TYPE)) {
                Long.parseLong(value);
            } else if (clazz.equals(Short.TYPE)) {
                Short.parseShort(value);
            } else if (clazz.equals(Character.TYPE)) {
                resolveChar(value);
            } else if (clazz.equals(Double.TYPE)) {
                Double.parseDouble(value);
            } else if (clazz.equals(Byte.TYPE)) {
                Byte.valueOf(value);
            } else if (clazz.equals(Float.TYPE)) {
                Float.parseFloat(value);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    static void resolveBoolean(String value) throws IllegalArgumentException {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return;
        }
        throw new IllegalArgumentException("Could not resolve " + value + " to boolean.");
    }

    static void resolveChar(String value) throws IllegalArgumentException {
        if (isNull(value) || value.length() != 1) {
            throw new IllegalArgumentException("Could not resolve " + value + " to char.");
        }
    }

}
