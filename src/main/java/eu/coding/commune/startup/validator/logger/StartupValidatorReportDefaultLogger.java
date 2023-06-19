package eu.coding.commune.startup.validator.logger;

import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import eu.coding.commune.startup.validator.model.StartupValidatorReportEntry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.security.CodeSource;
import java.util.*;

@RequiredArgsConstructor
@ConditionalOnMissingBean(StartupValidatorReportLogger.class)
public class StartupValidatorReportDefaultLogger implements StartupValidatorReportLogger {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void logValidationReport(StartupValidatorReport report) {
        logger.atLevel(report.getSeverityLevel().getLevel()).log(prepareReport(report));
    }

    protected String prepareReport(StartupValidatorReport report) {
        StringJoiner reportJoiner = new StringJoiner(System.lineSeparator());
        reportJoiner.add(getBanner());
        Map<SeverityLevel, List<StartupValidatorReportEntry>> sortedEntries = new TreeMap<>(
            Comparator.comparingInt(SeverityLevel::getValue).reversed());
        for (StartupValidatorReportEntry entry : report.getEntries()) {
            sortedEntries.putIfAbsent(entry.getSeverityLevel(), new LinkedList<>());
            sortedEntries.get(entry.getSeverityLevel()).add(entry);
        }
        sortedEntries.forEach((severity, listOfEntries) -> {
            reportJoiner.add(getSeverityHeader(severity, listOfEntries.size()));
            listOfEntries.forEach(reportEntry -> reportJoiner.add(getReportEntryLogLine(severity, reportEntry)));
        });
        reportJoiner.add(getFooter());
        return reportJoiner.toString();
    }

    protected String getReportEntryLogLine(SeverityLevel severityLevel, StartupValidatorReportEntry entry) {
        return getSeverityColumn(severityLevel) +
            getCustomMessage(entry.getMessage()) +
            entry.getGenericMessage();
    }

    protected String getCustomMessage(String message) {
        return message + " | ";
    }

    protected String getSeverityColumn(SeverityLevel severityLevel) {
        return "[" +
            severityLevel +
            "]" +
            " | ";
    }

    protected String getSeverityHeader(SeverityLevel severityLevel, int howMany) {
        StringJoiner sb = new StringJoiner(System.lineSeparator());
        sb.add(". . . . . . . . . . . . . . . . . . . . . . . ");
        sb.add("SEVERITY LEVEL: " + severityLevel + ". Found " + howMany + " problems.");
        sb.add(". . . . . . . . . . . . . . . . . . . . . . . ");
        return sb.toString();
    }

    // provided by: http://www.network-science.de/ascii/ (doom template)
    protected String getBanner() {
        StringJoiner bannerJoiner = new StringJoiner(System.lineSeparator());
        bannerJoiner.add("");
        bannerJoiner.add(" _____ _____ ___  ______ _____ _   _______ ");
        bannerJoiner.add("/  ___|_   _/ _ \\ | ___ \\_   _| | | | ___ \\");
        bannerJoiner.add("\\ `--.  | |/ /_\\ \\| |_/ / | | | | | | |_/ /");
        bannerJoiner.add(" `--. \\ | ||  _  ||    /  | | | | | |  __/ ");
        bannerJoiner.add("/\\__/ / | || | | || |\\ \\  | | | |_| | |    ");
        bannerJoiner.add("\\____/  \\_/\\_| |_/\\_| \\_| \\_/  \\___/\\_|    ");
        bannerJoiner.add("");
        bannerJoiner.add("   ______ ___________ ___________ _____ ");
        bannerJoiner.add("   | ___ \\  ___| ___ \\  _  | ___ \\_   _|");
        bannerJoiner.add("   | |_/ / |__ | |_/ / | | | |_/ / | |  ");
        bannerJoiner.add("   |    /|  __||  __/| | | |    /  | |  ");
        bannerJoiner.add("   | |\\ \\| |___| |   \\ \\_/ / |\\ \\  | |  ");
        bannerJoiner.add("   \\_| \\_\\____/\\_|    \\___/\\_| \\_| \\_/  ");
        bannerJoiner.add("Version: " + getApplicationVersion());
        bannerJoiner.add("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        return bannerJoiner.toString();
    }

    private static String getApplicationVersion() {
        return "1.1.0";
        //TODO - fix getting version from Path
//        return getVersionFromPath();
    }

    private static String getVersionFromPath() {
        //TODO line below could throw SecurityException in some cases - need tests on different platforms (Linux, MacOS)
        CodeSource codeSource = StartupValidatorReportDefaultLogger.class.getProtectionDomain().getCodeSource();
        return trimPath(codeSource.getLocation().toString());
    }

    private static String trimPath(String path) {
        String pathWithDeletedFilename = path.substring(0, path.lastIndexOf('/'));
        return pathWithDeletedFilename.substring(pathWithDeletedFilename.lastIndexOf('/') + 1);
    }

    protected String getFooter() {
        StringJoiner footerJoiner = new StringJoiner(System.lineSeparator());
        footerJoiner.add("");
        footerJoiner.add("End of Spring Startup Report");
        footerJoiner.add("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        return footerJoiner.toString();
    }
}
