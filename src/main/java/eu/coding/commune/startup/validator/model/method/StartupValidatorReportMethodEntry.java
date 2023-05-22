package eu.coding.commune.startup.validator.model.method;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.StartupValidatorReportEntry;
import lombok.Getter;

@Getter
public abstract class StartupValidatorReportMethodEntry extends StartupValidatorReportEntry {

    protected final Throwable throwable;
    protected final String className;
    protected final String methodName;

    public StartupValidatorReportMethodEntry(SeverityLevel severityLevel, String message, Throwable throwable,
                                             String className, String methodName) {
        super(severityLevel, message);
        this.throwable = throwable;
        this.className = className;
        this.methodName = methodName;
    }


}
