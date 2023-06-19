package eu.coding.commune.startup.validator.impl.method;

import eu.coding.commune.startup.validator.MustReturn;
import eu.coding.commune.startup.validator.model.method.MethodData;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodEntry;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodMustSucceedEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static eu.coding.commune.startup.validator.impl.ValidatorUtils.resolveProperty;

public class MustReturnValidator extends MethodValidator {

    private final static Logger logger = LoggerFactory.getLogger(MustReturnValidator.class);

    public MustReturnValidator(ApplicationContext context) {
        super(context);
    }

    @Override
    public Class<? extends Annotation> getSupportedAnnotation() {
        return MustReturn.class;
    }

    //TODO implement check of primitives
    @Override
    protected Optional<? extends StartupValidatorReportMethodEntry> validateInternal(MethodData methodData) {
        MustReturn annotation = methodData.method().getAnnotation(MustReturn.class);
        boolean isAccessible =  methodData.method().canAccess( methodData.clazz().cast(methodData.bean()));
        if (!methodData.method().getReturnType().isPrimitive()
                && !"void".equals(methodData.method().getReturnType().getName())) {
            try {
                if (!isAccessible) {
                     methodData.method().trySetAccessible();
                }
                Object expectedValue = resolveProperty(context, String.class, annotation.result(), true);
                Object methodResult =  methodData.method().invoke(methodData.clazz().cast(methodData.bean()));
                if (!methodResult.toString().equals(expectedValue)) {
                    return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                            .message(annotation.message())
                            .throwable(new Exception("Expected method result: \"" + expectedValue
                                    + "\", actual result: \"" + methodResult + "\""))
                            .methodName( methodData.method().getName())
                            .className( methodData.clazz().getCanonicalName())
                            .severityLevel(annotation.otherwise().getSeverityLevel())
                            .build());
                }
            } catch (IllegalAccessException e) {
                //TODO handling
                logger.error("An error occurred during checking", e);
            } catch (InvocationTargetException e) {
                return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                        .message(annotation.message())
                        .throwable(e.getTargetException())
                        .methodName( methodData.method().getName())
                        .className( methodData.clazz().getCanonicalName())
                        .severityLevel(annotation.otherwise().getSeverityLevel())
                        .build());
            } finally {
                 methodData.method().setAccessible(isAccessible);
            }
        } else {
            logger.error("Method return type is either void or primitive");
            return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                    .message(annotation.message())
                    .throwable(new Exception("Unsupported method return type: \""
                            +  methodData.method().getReturnType().getName() + "\". Only objects are supported."))
                    .methodName(methodData.method().getName())
                    .className(methodData.clazz().getCanonicalName())
                    .severityLevel(annotation.otherwise().getSeverityLevel())
                    .build());
        }
        return Optional.empty();
    }
}
