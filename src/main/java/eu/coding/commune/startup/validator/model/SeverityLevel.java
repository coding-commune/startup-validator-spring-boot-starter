package eu.coding.commune.startup.validator.model;

import lombok.Getter;
import org.slf4j.event.Level;

@Getter
public enum SeverityLevel {
    NONE(0, Level.TRACE),
    INFO(1, Level.INFO),
    WARN(2, Level.WARN),
    PROBABLE_ERROR(3, Level.WARN),
    ERROR(4, Level.ERROR);

    private final int value;
    private final Level level;

    SeverityLevel(final int severity, final Level level) {
        this.value = severity;
        this.level = level;
    }

    public boolean isMoreSevere(SeverityLevel severityLevel) {
        return this.value > severityLevel.value;
    }

    public boolean isMoreOrEquallySevere(SeverityLevel severityLevel) {
        return this.value >= severityLevel.value;
    }

    public boolean isLessSevere(SeverityLevel severityLevel) {
        return this.value < severityLevel.value;
    }

    public boolean isLessOrEquallySevere(SeverityLevel severityLevel) {
        return this.value <= severityLevel.value;
    }

}
