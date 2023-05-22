package eu.coding.commune.startup.validator.model;

import lombok.Getter;

@Getter
public enum StartupAction {
    WARN(SeverityLevel.WARN),
    FAIL(SeverityLevel.ERROR);

    private final SeverityLevel severityLevel;

    StartupAction(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

}