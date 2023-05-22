package eu.coding.commune.startup.validator.model;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static eu.coding.commune.startup.validator.model.SeverityLevel.NONE;


@Getter
public class StartupValidatorReport {

    private final Queue<StartupValidatorReportEntry> entries = new ConcurrentLinkedQueue<>();
    private SeverityLevel severityLevel = NONE;

    public void addEntry(StartupValidatorReportEntry entry) {
        if (entry.severityLevel.isMoreSevere(this.severityLevel)) {
            this.severityLevel = entry.severityLevel;
        }
        entries.add(entry);
    }



}
