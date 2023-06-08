package eu.coding.commune.startup.validator;

import eu.coding.commune.startup.validator.logger.StartupValidatorReportDefaultLogger;
import eu.coding.commune.startup.validator.logger.StartupValidatorReportLogger;
import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import eu.coding.commune.startup.validator.impl.StartupValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;

public class StartupValidatorInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequiredArgsConstructor
    private final class StartupValidatorListener implements SmartApplicationListener {
        private final ConfigurableApplicationContext context;
        private final StartupValidator startupValidator;
        private boolean isGeneratingReport = false;
        private final String appIdentity;

        public StartupValidatorListener(ConfigurableApplicationContext context) {
            this.context = context;
            this.appIdentity = this.context.getDisplayName();
            this.startupValidator = new StartupValidator(context);
        }

        // List of Spring Boot events to support: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.spring-application.application-events-and-listeners
        @Override
        public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
            return ApplicationStartedEvent.class.isAssignableFrom(eventType) ||
                    ApplicationReadyEvent.class.isAssignableFrom(eventType) ||
                    ApplicationFailedEvent.class.isAssignableFrom(eventType);
        }

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ApplicationStartedEvent contextStartedEvent) {
                if (contextStartedEvent.getApplicationContext().equals(this.context)) {
                    performApplicationStartedValidation();
                }
            }
            if (event instanceof ApplicationReadyEvent applicationReadyEvent) {
                if (applicationReadyEvent.getApplicationContext().equals(this.context)) {
                    this.context.removeApplicationListener(this);
                }
            }
            else if (event instanceof ApplicationFailedEvent applicationFailedEvent
                    && applicationFailedEvent.getApplicationContext().equals(this.context)) {
                performApplicationFailedValidation();
            }
        }

        private void performApplicationStartedValidation() {
            logger.atDebug().log("Starting validation for application {}",  appIdentity);
            this.isGeneratingReport = true;
            this.startupValidator.validate();
            StartupValidatorReport report = this.startupValidator.getReport();
            if (report.getSeverityLevel().isMoreOrEquallySevere(SeverityLevel.ERROR)) {
                logger.error("Application {} failed to pass validation. Preparing report...", appIdentity);
                this.context.getBean(StartupValidatorReportLogger.class).logValidationReport(report);
                this.context.stop();
            } else if (report.getSeverityLevel().isMoreOrEquallySevere(SeverityLevel.WARN)) {
                logger.atWarn().log("Application {} encountered some warnings. Preparing report...", appIdentity);
                this.context.getBean(StartupValidatorReportLogger.class).logValidationReport(report);
            }

        }

        private void performApplicationFailedValidation() {
            if (!this.isGeneratingReport) {
                logger.error("Application {} failed to start whatsoever. Preparing report...", appIdentity);
                //it would be impossible to validate methods if application failed to start
                this.startupValidator.validateFieldsOnly();
                StartupValidatorReport report = this.startupValidator.getReport();
                new StartupValidatorReportDefaultLogger().logValidationReport(report);
            }
        }

        @Override
        public boolean supportsSourceType(Class<?> sourceType) {
            return true;
        }

        @Override
        public String getListenerId() {
            return SmartApplicationListener.super.getListenerId();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if ("true".equalsIgnoreCase(applicationContext.getEnvironment().getProperty("spring.startup.validator.disabled"))) {
            return;
        }
        applicationContext.addApplicationListener(new StartupValidatorListener(applicationContext));
    }
}
