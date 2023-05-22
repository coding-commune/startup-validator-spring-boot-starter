package eu.coding.commune.startup.validator.model;

import lombok.Getter;

@Getter
public abstract class StartupValidatorReportEntry {
    protected final String message;
    protected final SeverityLevel severityLevel;

    protected StartupValidatorReportEntry(SeverityLevel severityLevel, String message) {
        this.severityLevel = severityLevel;
        this.message = message;
    }

    public abstract String getGenericMessage();

}
