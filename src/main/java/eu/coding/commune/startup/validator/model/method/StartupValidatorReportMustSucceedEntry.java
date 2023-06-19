package eu.coding.commune.startup.validator.model.method;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

public class StartupValidatorReportMustSucceedEntry extends StartupValidatorReportMethodEntry {

    @Builder
    public StartupValidatorReportMustSucceedEntry(SeverityLevel severityLevel, String message, Throwable throwable,
                                                  String className, String methodName) {
        super(severityLevel, message, throwable, className, methodName);
    }

    @Override
    public String getGenericMessage() {
        return "Following error occurred during execution of " +  methodName + "() in class " +  className + ": " +
                throwable.toString();
    }
}
