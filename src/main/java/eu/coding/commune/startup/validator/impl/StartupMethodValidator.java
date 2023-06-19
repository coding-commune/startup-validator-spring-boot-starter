package eu.coding.commune.startup.validator.impl;

import eu.coding.commune.startup.validator.impl.method.MethodValidator;
import eu.coding.commune.startup.validator.model.StartupValidatorReport;
import eu.coding.commune.startup.validator.model.method.MethodData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import java.lang.reflect.Method;
import java.util.ServiceLoader;

@RequiredArgsConstructor
public class StartupMethodValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurableApplicationContext context;
    private final StartupValidatorReport report;

    public void validate(Class<?> clazz) {
        Object bean = getBean(clazz);
        Method[] clazzMethods = clazz.getDeclaredMethods();
        for (Method method : clazzMethods) {
            MethodData methodData = MethodData.builder()
                    .method(method)
                    .bean(bean)
                    .clazz(clazz).build();
            ServiceLoader.load(MethodValidator.class).forEach(validator -> {
                validator.setApplicationContext(context);
                validator.validate(methodData).ifPresent(this.report::addEntry);
            });
        }
    }

    //TODO - if not singleton then what?
    private Object getBean(Class<?> clazz) {
        return context.getBean(clazz);
    }

}
