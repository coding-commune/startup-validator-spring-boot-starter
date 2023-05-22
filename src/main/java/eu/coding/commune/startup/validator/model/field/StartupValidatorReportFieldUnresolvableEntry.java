package eu.coding.commune.startup.validator.model.field;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;

//TODO possibility to implement it by user
public class StartupValidatorReportFieldUnresolvableEntry extends StartupValidatorReportFieldEntry {

    private final String type;

    @Builder
    public StartupValidatorReportFieldUnresolvableEntry(SeverityLevel severityLevel, String message, String property,
                                                        String resolvedValue, String type) {
        super(severityLevel, message, property, resolvedValue, true);
        this.type = type;
    }

    @Override
    public String getGenericMessage() {
        return "Value " +
                getMaskedResolvedValue(this.resolvedValue) + " could not be resolved for property "
                + this.property + " that requires type " + type;
    }
}
