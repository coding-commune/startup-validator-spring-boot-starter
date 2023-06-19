package eu.coding.commune.startup.validator.impl.method;

import eu.coding.commune.startup.validator.MustSucceed;
import eu.coding.commune.startup.validator.model.method.MethodData;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodEntry;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMustSucceedEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class MustSucceedValidator extends MethodValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Class<? extends Annotation> getSupportedAnnotation() {
        return MustSucceed.class;
    }

    //TODO - This that not seem bulletproof. What does method.setAccessible(false) do for others?
    @Override
    protected Optional<? extends StartupValidatorReportMethodEntry> validateInternal(MethodData methodData) {
        MustSucceed annotation = methodData.method().getAnnotation(MustSucceed.class);
        boolean isAccessible = methodData.method().canAccess(methodData.clazz().cast(methodData.bean()));
        try {
            if (!isAccessible) {
                methodData.method().trySetAccessible();
            }
            methodData.method().invoke(methodData.clazz().cast(methodData.bean()));
        } catch (IllegalAccessException e) {
            //TODO handling
            logger.error("An error occurred during checking", e);
        } catch (InvocationTargetException e) {
            return Optional.of(StartupValidatorReportMustSucceedEntry.builder()
                    .message(annotation.message())
                    .throwable(e.getTargetException())
                    .methodName(methodData.method().getName())
                    .className(methodData.clazz().getCanonicalName())
                    .severityLevel(annotation.otherwise().getSeverityLevel())
                    .build());
        } finally {
            methodData.method().setAccessible(isAccessible);
        }
        return Optional.empty();
    }
}
