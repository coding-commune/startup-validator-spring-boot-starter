package eu.coding.commune.startup.validator.validator;

import eu.coding.commune.startup.validator.MustBeDefined;
import eu.coding.commune.startup.validator.MustMatch;
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
import java.util.regex.Pattern;

import static eu.coding.commune.startup.validator.validator.ValidatorUtils.*;
import static java.util.Objects.isNull;

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
                String resolvedValue = resolveProperty(applicationContext, annotationValue.get(), true);
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
            String resolvedValue = resolveProperty(applicationContext, propertyName, false);
            this.performValidation(FieldData.builder()
                .field(field)
                .propertyName(propertyName)
                .resolvedValue(resolvedValue)
                .build());
        }
    }

    private void performValidation(FieldData fieldData) {
        checkResolvability(fieldData).ifPresent(this.report::addEntry);
        checkMustBeDefined(fieldData).ifPresent(this.report::addEntry);
        checkMustMatch(fieldData).ifPresent(this.report::addEntry);
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
        if (!isFieldResolvable(fieldData.field(), fieldData.resolvedValue())) {
            return Optional.of(StartupValidatorReportFieldUnresolvableEntry.builder()
                .severityLevel(SeverityLevel.PROBABLE_ERROR)
                .message("")
                .type(fieldData.field().getType().getTypeName())
                .property(fieldData.propertyName())
                .resolvedValue(fieldData.resolvedValue())
                .build());
        }
        return Optional.empty();
    }

    private Optional<StartupValidatorReportFieldEntry> checkMustBeDefined(FieldData fieldData) {
        if (fieldData.field().isAnnotationPresent(MustBeDefined.class)) {
            if (isNull(fieldData.resolvedValue()) || fieldData.resolvedValue().isBlank()) {
                MustBeDefined annotation = fieldData.field().getAnnotation(MustBeDefined.class);
                return Optional.of(StartupValidatorReportMustBeDefinedEntry.builder()
                    .severityLevel(annotation.otherwise().getSeverityLevel())
                    .message(annotation.message())
                    .property(fieldData.propertyName())
                    .resolvedValue(fieldData.resolvedValue())
                    .build());
            }
        }
        return Optional.empty();
    }

    private Optional<StartupValidatorReportFieldEntry> checkMustMatch(FieldData fieldData) {
        if (fieldData.field().isAnnotationPresent(MustMatch.class)) {
            //TODO validate pattern provided by user!
            MustMatch annotation = fieldData.field().getAnnotation(MustMatch.class);
            Pattern pattern = Pattern.compile(annotation.regex());
            if (!pattern.matcher(fieldData.resolvedValue()).matches()) {
                return Optional.of(StartupValidatorReportMustMatchEntry.builder()
                    .severityLevel(annotation.otherwise().getSeverityLevel())
                    .message(annotation.message())
                    .property(fieldData.propertyName())
                    .regex(annotation.regex())
                    .resolvedValue(fieldData.resolvedValue())
                    .isConcealed(annotation.secret())
                    .build());
            }
        }
        return Optional.empty();
    }
}
