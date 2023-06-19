package eu.coding.commune.startup.validator.impl.field;


import eu.coding.commune.startup.validator.MustNotBeEmpty;
import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.field.FieldData;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportFieldEntry;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportMustNotBeEmptyEntry;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

public class MustNotBeEmptyValidator extends FieldValidator {
    @Override
    public Class<? extends Annotation> getSupportedAnnotation() {
        return MustNotBeEmpty.class;
    }

    @Override
    protected Optional<StartupValidatorReportFieldEntry> validateInternal(FieldData fieldData) {
        Class<?> type = fieldData.field().getType();
        MustNotBeEmpty annotation = fieldData.field().getAnnotation(MustNotBeEmpty.class);

        try {
            if (type.isArray()) {
                Object[] objects = (Object[]) fieldData.resolvedValue();
                if (objects.length < 1) {
                    return Optional.of(StartupValidatorReportMustNotBeEmptyEntry.builder()
                            .severityLevel(annotation.otherwise().getSeverityLevel())
                            .message(annotation.message())
                            .property(fieldData.propertyName())
                            .resolvedValue(fieldData.resolvedValue())
                            .build());
                }
            } else if (Collection.class.isAssignableFrom(type)) {
                Collection<?> collection;
                collection = (Collection<?>) fieldData.resolvedValue();
                if (collection.isEmpty()) {
                    return Optional.of(StartupValidatorReportMustNotBeEmptyEntry.builder()
                            .severityLevel(annotation.otherwise().getSeverityLevel())
                            .message(annotation.message())
                            .property(fieldData.propertyName())
                            .resolvedValue(fieldData.resolvedValue())
                            .build());
                }
            }
        } catch (ClassCastException e) {
            return Optional.of(StartupValidatorReportMustNotBeEmptyEntry.builder()
                    .severityLevel(SeverityLevel.PROBABLE_ERROR)
                    .message("Could not cast " + fieldData.propertyName() + " to collection")
                    .property(fieldData.propertyName())
                    .resolvedValue(fieldData.resolvedValue())
                    .build());
        }
        return Optional.empty();
    }
}
