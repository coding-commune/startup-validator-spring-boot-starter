package eu.coding.commune.startup.validator.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ValidatorUtils {

    private final static ExpressionParser PARSER = new SpelExpressionParser();
    private final static Pattern PATTERN = Pattern.compile(".*?#\\{(.*?)}.*?");

    static Optional<String> getValueAnnotationValue(Field field) {
        if (field.isAnnotationPresent(Value.class)) {
            return Optional.of(field.getAnnotation(Value.class).value());
        } else {
            return Optional.empty();
        }
    }

    static Object resolveProperty(ConfigurableApplicationContext applicationContext,
                                  Class<?> type,
                                  String property,
                                  boolean isSetUsingValueAnnotation) {
        Object resolved = null;
        if (isSetUsingValueAnnotation) {
            try {
                String resolvedPlaceholder = applicationContext.getEnvironment()
                        .resolveRequiredPlaceholders(property);
                if (containsExpression(resolvedPlaceholder)) {
                    Expression exp = PARSER.parseExpression(resolvedPlaceholder
                            .substring(2, resolvedPlaceholder.length() - 1));
                    resolved = exp.getValue(type);
                } else {
                    resolved = resolvedPlaceholder;
                }
            } catch (IllegalArgumentException ignored) {}
        } else {
            resolved = applicationContext.getEnvironment().getProperty(property, type);
        }
        return nonNull(resolved) ? resolved : "";
    }

    private static boolean containsExpression(String value) {
        return PATTERN.matcher(value).matches();
    }

    static String getPropertyName(String placeholder) {
        if (placeholder.charAt(0) == '$') {
            if (placeholder.contains(":")) {
                return placeholder.substring(placeholder.indexOf(":") + 1, placeholder.length() - 1);
            }
            return placeholder.substring(2, placeholder.length() - 1);
        }
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
