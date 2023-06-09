package eu.coding.commune.startup.validator.validator;

import eu.coding.commune.startup.validator.MustReturn;
import eu.coding.commune.startup.validator.MustSucceed;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import eu.coding.commune.startup.validator.model.StartupValidatorReportEntry;
import eu.coding.commune.startup.validator.model.method.StartupValidatorReportMethodMustSucceedEntry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static eu.coding.commune.startup.validator.validator.ValidatorUtils.resolveProperty;

@RequiredArgsConstructor
public class StartupMethodValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurableApplicationContext context;
    private final StartupValidatorReport report;

    public void validate(Class<?> clazz) {
        Object bean = getBean(clazz);
        Method[] clazzMethods = clazz.getDeclaredMethods();
        for (Method method : clazzMethods) {
            if (method.isAnnotationPresent(MustSucceed.class)) {
                checkMustSucceed(bean, clazz, method).ifPresent(this.report::addEntry);
            }
            if (method.isAnnotationPresent(MustReturn.class)) {
                checkMustReturn(bean, clazz, method).ifPresent(this.report::addEntry);
            }
        }
    }

    //TODO - if not singleton then what?
    private Object getBean(Class<?> clazz) {
        return context.getBean(clazz);
    }

    //TODO - This that not seem bulletproof. What does method.setAccessible(false) do for others?
    private Optional<StartupValidatorReportEntry> checkMustSucceed(Object bean, Class<?> clazz, Method method) {
        MustSucceed annotation = method.getAnnotation(MustSucceed.class);
        boolean isAccessible = method.canAccess(clazz.cast(bean));
        try {
            if (!isAccessible) {
                method.trySetAccessible();
            }
            method.invoke(clazz.cast(bean));
        } catch (IllegalAccessException e) {
            //TODO handling
            logger.error("An error occurred during checking", e);
        } catch (InvocationTargetException e) {
            return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                .message(annotation.message())
                .throwable(e.getTargetException())
                .methodName(method.getName())
                .className(clazz.getCanonicalName())
                .severityLevel(annotation.otherwise().getSeverityLevel())
                .build());
        } finally {
            method.setAccessible(isAccessible);
        }
        return Optional.empty();
    }

    //TODO implement check of primitives
    private Optional<StartupValidatorReportEntry> checkMustReturn(Object bean, Class<?> clazz, Method method) {
        MustReturn annotation = method.getAnnotation(MustReturn.class);
        boolean isAccessible = method.canAccess(clazz.cast(bean));
        if (!method.getReturnType().isPrimitive() && !"void".equals(method.getReturnType().getName())) {
            try {
                if (!isAccessible) {
                    method.trySetAccessible();
                }
                String expectedValue = resolveProperty(context, annotation.result(), true);
                Object methodResult = method.invoke(clazz.cast(bean));
                if (!methodResult.toString().equals(expectedValue)) {
                    return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                        .message(annotation.message())
                        .throwable(new Exception("Expected method result: \"" + expectedValue
                            + "\", actual result: \"" + methodResult.toString() + "\""))
                        .methodName(method.getName())
                        .className(clazz.getCanonicalName())
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
                    .methodName(method.getName())
                    .className(clazz.getCanonicalName())
                    .severityLevel(annotation.otherwise().getSeverityLevel())
                    .build());
            } finally {
                method.setAccessible(isAccessible);
            }
        } else {
            logger.error("Method return type is either void or primitive");
            return Optional.of(StartupValidatorReportMethodMustSucceedEntry.builder()
                .message(annotation.message())
                .throwable(new Exception("Unsupported method return type: \""
                    + method.getReturnType().getName() + "\". Supported only objects."))
                .methodName(method.getName())
                .className(clazz.getCanonicalName())
                .severityLevel(annotation.otherwise().getSeverityLevel())
                .build());
        }
        return Optional.empty();
    }
}
