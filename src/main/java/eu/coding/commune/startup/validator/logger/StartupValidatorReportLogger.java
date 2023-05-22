package eu.coding.commune.startup.validator.logger;

import eu.coding.commune.startup.validator.model.StartupValidatorReport;

public interface StartupValidatorReportLogger {
    void logValidationReport(StartupValidatorReport report);

}
