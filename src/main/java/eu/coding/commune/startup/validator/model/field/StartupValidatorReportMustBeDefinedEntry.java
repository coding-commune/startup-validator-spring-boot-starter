package eu.coding.commune.startup.validator.model.field;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

public class StartupValidatorReportMustBeDefinedEntry extends StartupValidatorReportFieldEntry {

    @Builder
    public StartupValidatorReportMustBeDefinedEntry(SeverityLevel severityLevel, String message, String property,
                                                    String resolvedValue) {
        super(severityLevel, message, property, resolvedValue, true);
    }

    @Override
    public String getGenericMessage() {
        return "Mandatory property not configured: " +
                property;
    }

}
