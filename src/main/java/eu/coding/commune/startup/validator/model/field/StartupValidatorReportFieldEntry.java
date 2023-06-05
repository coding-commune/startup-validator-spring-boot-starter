package eu.coding.commune.startup.validator.model.field;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.StartupValidatorReportEntry;
import lombok.Getter;

import static java.util.Objects.isNull;

@Getter
public abstract class StartupValidatorReportFieldEntry extends StartupValidatorReportEntry {
    protected final String property;
    protected final Object resolvedValue;
    protected final boolean isConcealed;

    public StartupValidatorReportFieldEntry(SeverityLevel severityLevel, String message, String property,
                                            Object resolvedValue, boolean isConcealed) {
        super(severityLevel, message);
        this.property = property;
        this.resolvedValue = resolvedValue;
        this.isConcealed = isConcealed;
    }

    protected String getMaskedResolvedValue(String value) {
        StringBuilder maskedSb = new StringBuilder();
        if (isNull(value) || value.isBlank()) {
            return "null";
        }
        if (value.length() > 5) {
            maskedSb.append(value, 0, 2);
        }
        maskedSb.append("***** (concealed)");
        return maskedSb.toString();
    }

}
