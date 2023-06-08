package eu.coding.commune.startup.validator.impl;

import eu.coding.commune.startup.validator.PackageProvider;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

public class StartupValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurableApplicationContext context;
    private final StartupFieldValidator fieldValidator;
    private final StartupMethodValidator methodValidator;
    private final StartupValidatorReport report;

    public StartupValidator(ConfigurableApplicationContext context) {
        this.context = context;
        this.report = new StartupValidatorReport();
        this.methodValidator = new StartupMethodValidator(this.context, this.report);
        this.fieldValidator = new StartupFieldValidator(this.context, this.report);
    }

    public StartupValidatorReport getReport() {
        return this.report;
    }

    public void validate() {
        this.validate(true);
    }

    public void validateFieldsOnly() {
        this.validate(false);
    }

    public void validateMethods() {
        logger.atDebug().log("Starting method validation...");

        logger.atDebug().log("Method validation ended successfully!");
    }

    private void validate(boolean validateMethods) {
        Set<BeanDefinition> beanDefinitions = getBeanDefinitionsToValidate();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> toValidate = Class.forName(beanDefinition.getBeanClassName());
                this.fieldValidator.validate(toValidate);
                if (validateMethods) {
                    this.methodValidator.validate(toValidate);
                }
            } catch (ClassNotFoundException e) {
                //TODO handling?
                throw new RuntimeException(e);
            }
        }
        logger.atDebug().log("Field validation ended successfully!");
    }

    private Set<BeanDefinition> getBeanDefinitionsToValidate() {
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        ClassPathScanningCandidateComponentProvider provider = createClassPathScanningCandidateComponentProvider();
        for (String aPackage : PackageProvider.getInstance().getPackagesToScan()) {
            beanDefinitions.addAll(provider.findCandidateComponents(aPackage));
        }
        return beanDefinitions;
    }

    private ClassPathScanningCandidateComponentProvider createClassPathScanningCandidateComponentProvider() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setEnvironment(this.context.getEnvironment());
        scanner.setResourceLoader(this.context);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Repository.class));
        return scanner;
    }
}
