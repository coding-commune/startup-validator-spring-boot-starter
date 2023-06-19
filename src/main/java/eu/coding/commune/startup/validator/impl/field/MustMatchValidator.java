package eu.coding.commune.startup.validator.impl.field;

import eu.coding.commune.startup.validator.MustMatch;
import eu.coding.commune.startup.validator.model.field.FieldData;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportFieldEntry;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportMustMatchEntry;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.regex.Pattern;

public class MustMatchValidator extends FieldValidator {

    @Override
    public Class<? extends Annotation> getSupportedAnnotation() {
        return MustMatch.class;
    }

    @Override
    protected Optional<? extends StartupValidatorReportFieldEntry> validateInternal(FieldData fieldData) {
        if (isNotApplicable(fieldData)) {
            return Optional.empty();
        }
        MustMatch annotation = fieldData.field().getAnnotation(MustMatch.class);
        Pattern pattern = Pattern.compile(annotation.regex()); //TODO
        if (pattern.matcher(fieldData.resolvedValue().toString()).matches()) {
            return Optional.empty();
        }
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
