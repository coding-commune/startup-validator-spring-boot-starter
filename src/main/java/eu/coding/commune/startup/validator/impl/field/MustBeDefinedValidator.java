package eu.coding.commune.startup.validator.impl.field;

import eu.coding.commune.startup.validator.MustBeDefined;
import eu.coding.commune.startup.validator.model.field.FieldData;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportFieldEntry;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportMustBeDefinedEntry;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static java.util.Objects.nonNull;

public class MustBeDefinedValidator extends FieldValidator {

    @Override
    public Class<? extends Annotation> getSupportedAnnotation() {
        return MustBeDefined.class;
    }

    @Override
    protected Optional<? extends StartupValidatorReportFieldEntry> validateInternal(FieldData fieldData) {
        if (nonNull(fieldData.resolvedValue()) && !fieldData.resolvedValue().toString().isBlank()) {
            return Optional.empty();
        }
        MustBeDefined annotation = fieldData.field().getAnnotation(MustBeDefined.class);
        return Optional.of(StartupValidatorReportMustBeDefinedEntry.builder()
                .severityLevel(annotation.otherwise().getSeverityLevel())
                .message(annotation.message())
                .property(fieldData.propertyName())
                .resolvedValue(fieldData.resolvedValue().toString())
                .build());
    }
}
