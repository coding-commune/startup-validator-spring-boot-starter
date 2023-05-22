package eu.coding.commune.startup.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class EnableStartupValidationRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        try {
            Class<?> clazz = Class.forName(metadata.getClassName());
            EnableStartupValidation annotation = clazz.getAnnotation(EnableStartupValidation.class);
            String[] declaredPackages = annotation.value();
            PackageProvider provider = PackageProvider.getInstance();
            if (isNull(declaredPackages) || declaredPackages.length == 0) {
                provider.getPackagesToScan().add(clazz.getPackageName());
            } else {
                provider.getPackagesToScan().addAll(Arrays.stream(declaredPackages).toList());
            }
        } catch (ClassNotFoundException e) {
            //TODO handling
            throw new RuntimeException(e);
        }
    }
}
