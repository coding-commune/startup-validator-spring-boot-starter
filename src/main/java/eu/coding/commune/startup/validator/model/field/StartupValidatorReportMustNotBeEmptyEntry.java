package eu.coding.commune.startup.validator.model.field;


import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

public class StartupValidatorReportMustNotBeEmptyEntry extends StartupValidatorReportFieldEntry {

    @Builder
    public StartupValidatorReportMustNotBeEmptyEntry(SeverityLevel severityLevel, String message, String property, Object resolvedValue, boolean isConcealed) {
        super(severityLevel, message, property, resolvedValue, isConcealed);
    }

    @Override
    public String getGenericMessage() {
        return "Collection must not be empty";
    }
}
