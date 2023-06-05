package eu.coding.commune.startup.validator.model.field;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StartupValidatorReportMustMatchEntry extends StartupValidatorReportFieldEntry {

    private final String regex;

    @Builder
    public StartupValidatorReportMustMatchEntry(SeverityLevel severityLevel, String message, String property,
                                                Object resolvedValue, String regex, boolean isConcealed) {
        super(severityLevel, message, property, resolvedValue, isConcealed);
        this.regex = regex;
    }

    @Override
    public String getGenericMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Regex ");
        sb.append(regex);
        sb.append(" was not matched for ");
        if (isConcealed) {
            sb.append(getMaskedResolvedValue(resolvedValue.toString()));
        } else {
            sb.append(resolvedValue.toString());
        }
        sb.append(" provided for property ");
        sb.append(property);
        return sb.toString();
    }
}
