package eu.coding.commune.startup.validator.impl.method;

import eu.coding.commune.startup.validator.MustReturn;
import eu.coding.commune.startup.validator.model.SeverityLevel;
import eu.coding.commune.startup.validator.model.method.MethodData;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodEntry;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMustReturnEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static eu.coding.commune.startup.validator.impl.ValidatorUtils.resolveProperty;
import static java.util.Objects.nonNull;

public class MustReturnValidator extends MethodValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

                Object expectedValue;
                if (nonNull(applicationContext)) {
                    expectedValue = resolveProperty(applicationContext, String.class, annotation.result(), true);
                } else {
                    expectedValue = annotation.result();
                }

                Object methodResult =  methodData.method().invoke(methodData.clazz().cast(methodData.bean()));
                if (!methodResult.toString().equals(expectedValue)) {
                    return Optional.of(StartupValidatorReportMustReturnEntry.builder()
                            .message(annotation.message())
                            .expectedReturnValue(expectedValue)
                            .actualReturnValue(methodResult)
                            .methodName(methodData.method().getName())
                            .className(methodData.clazz().getCanonicalName())
                            .severityLevel(annotation.otherwise().getSeverityLevel())
                            .build());
                }
            } catch (IllegalAccessException e) {
                //TODO handling
                logger.error("An error occurred during checking", e);
            } catch (InvocationTargetException e) {
                return Optional.of(StartupValidatorReportMustReturnEntry.builder()
                        .message(annotation.message())
                        .throwable(e.getTargetException())
                        .methodName( methodData.method().getName())
                        .className( methodData.clazz().getCanonicalName())
                        .severityLevel(SeverityLevel.PROBABLE_ERROR)
                        .build());
            } finally {
                 methodData.method().setAccessible(isAccessible);
            }
        } else {
            logger.error("Method return type is either void or primitive");
            return Optional.of(StartupValidatorReportMustReturnEntry.builder()
                    .message(annotation.message())
                    .throwable(new RuntimeException("Unsupported method return type: \""
                            +  methodData.method().getReturnType().getName() + "\". Only objects are supported."))
                    .methodName(methodData.method().getName())
                    .className(methodData.clazz().getCanonicalName())
                    .severityLevel(SeverityLevel.PROBABLE_ERROR)
                    .build());
        }
        return Optional.empty();
    }
}
