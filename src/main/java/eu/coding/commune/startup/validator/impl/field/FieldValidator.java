package eu.coding.commune.startup.validator.impl.field;

import eu.coding.commune.startup.validator.impl.Validator;
import eu.coding.commune.startup.validator.model.field.FieldData;
import eu.coding.commune.startup.validator.model.field.StartupValidatorReportFieldEntry;

import java.util.Optional;

public abstract class FieldValidator implements Validator {

    public boolean isNotApplicable(FieldData fieldData) {
        return !fieldData.field().isAnnotationPresent(getSupportedAnnotation());
    }

    public Optional<? extends StartupValidatorReportFieldEntry> validate(FieldData fieldData) {
        if (isNotApplicable(fieldData)) {
            return Optional.empty();
        }
        return validateInternal(fieldData);
    }
    protected abstract Optional<? extends StartupValidatorReportFieldEntry> validateInternal(FieldData fieldData);

}
