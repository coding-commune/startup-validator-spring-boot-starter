package eu.coding.commune.startup.validator.model.method;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

//TODO possibility to implement it by user
public class StartupValidatorReportMethodMustSucceedEntry extends StartupValidatorReportMethodEntry {

    @Builder
    public StartupValidatorReportMethodMustSucceedEntry(SeverityLevel severityLevel, String message, Throwable throwable,
                                                        String className, String methodName) {
        super(severityLevel, message, throwable, className, methodName);
    }

    //TODO better throwable handling
    @Override
    public String getGenericMessage() {
        return "Following error occurred during execution of " +  methodName + "() in class " +  className + ": " + throwable.toString();
    }
}
