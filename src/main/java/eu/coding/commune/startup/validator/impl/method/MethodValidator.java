package eu.coding.commune.startup.validator.impl.method;

import eu.coding.commune.startup.validator.impl.Validator;
import eu.coding.commune.startup.validator.model.method.MethodData;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class MethodValidator implements Validator {

    protected final ApplicationContext context;

    public boolean isNotApplicable(MethodData methodData) {
        return !methodData.method().isAnnotationPresent(getSupportedAnnotation());
    }

    public Optional<? extends StartupValidatorReportMethodEntry> validate(MethodData methodData) {
        if (isNotApplicable(methodData)) {
            return Optional.empty();
        }
        return validateInternal(methodData);
    }
    protected abstract Optional<? extends StartupValidatorReportMethodEntry> validateInternal(MethodData methodData);

}
