package eu.coding.commune.startup.validator.impl;

import eu.coding.commune.startup.validator.impl.field.FieldValidator;
import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import eu.coding.commune.startup.validator.model.field.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.ServiceLoader;

import static eu.coding.commune.startup.validator.impl.ValidatorUtils.*;

@RequiredArgsConstructor
public class StartupFieldValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurableApplicationContext applicationContext;
    private final StartupValidatorReport report;

    public void validate(Class<?> clazz) {
        logger.atDebug().log("Starting validating fields of class: {}", clazz);
        Optional<String> prefix = getConfigurationPropertiesPrefix(clazz);
        if (prefix.isPresent()) {
            validateClassFields(clazz.getDeclaredFields(), prefix.get());
        } else {
            validateClassFields(clazz.getDeclaredFields());
        }
        logger.atDebug().log("Validated fields of class: {} successfully", clazz);
    }

    // For properties configured using @Value
    private void validateClassFields(Field[] clazzFields) {
        for (Field field : clazzFields) {
            Optional<String> annotationValue = getValueAnnotationValue(field);
            if (annotationValue.isPresent()) {
                Object resolvedValue = resolveProperty(applicationContext, field.getType(),
                        annotationValue.get(), true);

                String propertyName = getPropertyName(annotationValue.get());
                this.performValidation(FieldData.builder()
                    .field(field)
                    .propertyName(propertyName)
                    .resolvedValue(resolvedValue)
                    .build());
            }
        }
    }

    // For properties configured using @ConfigurationProperties
    private void validateClassFields(Field[] clazzFields, String propertiesPrefix) {
        for (Field field : clazzFields) {
            String propertyName = propertiesPrefix.concat(".").concat(field.getName());
            Object resolvedValue = resolveProperty(applicationContext, field.getType(),
                    propertyName, false);

            this.performValidation(FieldData.builder()
                .field(field)
                .propertyName(propertyName)
                .resolvedValue(resolvedValue)
                .build());
        }
    }

    private void performValidation(FieldData fieldData) {
        checkResolvability(fieldData).ifPresent(this.report::addEntry);
        ServiceLoader.load(FieldValidator.class).forEach(validator -> {
            validator.setApplicationContext(applicationContext);
            validator.validate(fieldData).ifPresent(this.report::addEntry);
        });
    }

    private Optional<String> getConfigurationPropertiesPrefix(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ConfigurationProperties.class)) {
            return clazz.getAnnotation(ConfigurationProperties.class).value().isBlank() ?
                Optional.of(clazz.getAnnotation(ConfigurationProperties.class).prefix()) :
                Optional.of(clazz.getAnnotation(ConfigurationProperties.class).value());
        }
        return Optional.empty();
    }

    private Optional<StartupValidatorReportFieldEntry> checkResolvability(FieldData fieldData) {
        if (isFieldResolvable(fieldData.field(), fieldData.resolvedValue().toString())) {
            return Optional.empty();
        }
        return Optional.of(StartupValidatorReportFieldUnresolvableEntry.builder()
                .severityLevel(SeverityLevel.PROBABLE_ERROR)
                .message("")
                .type(fieldData.field().getType().getTypeName())
                .property(fieldData.propertyName())
                .resolvedValue(fieldData.resolvedValue().toString())
                .build());
    }

}
