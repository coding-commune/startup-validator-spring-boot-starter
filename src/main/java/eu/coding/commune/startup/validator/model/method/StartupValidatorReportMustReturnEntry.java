package eu.coding.commune.startup.validator.model.method;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

public class StartupValidatorReportMustReturnEntry  extends StartupValidatorReportMethodEntry {

    private final Object expectedReturnValue;
    private final Object actualReturnValue;

    @Builder
    public StartupValidatorReportMustReturnEntry(SeverityLevel severityLevel,
                                                 String message,
                                                 Throwable throwable,
                                                 String className,
                                                 String methodName,
                                                 Object expectedReturnValue,
                                                 Object actualReturnValue) {
        super(severityLevel, message, throwable, className, methodName);
        this.expectedReturnValue = expectedReturnValue;
        this.actualReturnValue = actualReturnValue;
    }

    @Override
    public String getGenericMessage() {
        return "Mismatch in class: [" + className + "], method: [" + methodName +
                "]. Expected method result: \"" + expectedReturnValue
                + "\", actual result: \"" + actualReturnValue + "\"";
    }
}
