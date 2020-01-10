package com.alibaba.spring.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Annotated {@link BeanDefinition} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanDefinition
 * @since 2017.01.09
 */
public abstract class AnnotatedBeanDefinitionRegistryUtils {

    private static final Log logger = LogFactory.getLog(AnnotatedBeanDefinitionRegistryUtils.class);

    /**
     * Is present bean that was registered by the specified {@link Annotation annotated} {@link Class class}
     *
     * @param registry       {@link BeanDefinitionRegistry}
     * @param annotatedClass the {@link Annotation annotated} {@link Class class}
     * @return if present, return <code>true</code>, or <code>false</code>
     * @since 1.0.3
     */
    public static boolean isPresentBean(BeanDefinitionRegistry registry, Class<?> annotatedClass) {

        boolean present = false;

        String[] beanNames = registry.getBeanDefinitionNames();

        ClassLoader classLoader = annotatedClass.getClassLoader();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
                String className = annotationMetadata.getClassName();
                Class<?> targetClass = resolveClassName(className, classLoader);
                present = nullSafeEquals(targetClass, annotatedClass);
                if (present) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(format("The annotatedClass[class : %s , bean name : %s] was present in registry[%s]",
                                className, beanName, registry));
                    }
                    break;
                }
            }
        }

        return present;
    }

    /**
     * Register Beans if not present in {@link BeanDefinitionRegistry registry}
     *
     * @param registry         {@link BeanDefinitionRegistry}
     * @param annotatedClasses {@link Annotation annotation} class
     */
    public static void registerBeans(BeanDefinitionRegistry registry, Class<?>... annotatedClasses) {

        if (ObjectUtils.isEmpty(annotatedClasses)) {
            return;
        }

        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);

        if (logger.isDebugEnabled()) {
            logger.debug(registry.getClass().getSimpleName() + " will register annotated classes : " + asList(annotatedClasses) + " .");
        }

        for (Class<?> clazz : annotatedClasses) {
            if (!isPresentBean(registry, clazz)) {
                reader.register(clazz);
            }
        }

    }

    /**
     * Scan base packages for register {@link Component @Component}s
     *
     * @param registry     {@link BeanDefinitionRegistry}
     * @param basePackages base packages
     * @return the count of registered components.
     */
    public static int scanBasePackages(BeanDefinitionRegistry registry, String... basePackages) {

        int count = 0;

        if (!ObjectUtils.isEmpty(basePackages)) {

            boolean debugEnabled = logger.isDebugEnabled();

            if (debugEnabled) {
                logger.debug(registry.getClass().getSimpleName() + " will scan base packages " + Arrays.asList(basePackages) + ".");
            }

            List<String> registeredBeanNames = Arrays.asList(registry.getBeanDefinitionNames());

            ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry);
            count = classPathBeanDefinitionScanner.scan(basePackages);

            List<String> scannedBeanNames = new ArrayList<String>(count);
            scannedBeanNames.addAll(Arrays.asList(registry.getBeanDefinitionNames()));
            scannedBeanNames.removeAll(registeredBeanNames);

            if (debugEnabled) {
                logger.debug("The Scanned Components[ count : " + count + "] under base packages " + Arrays.asList(basePackages) + " : ");
            }

            for (String scannedBeanName : scannedBeanNames) {
                BeanDefinition scannedBeanDefinition = registry.getBeanDefinition(scannedBeanName);
                if (debugEnabled) {
                    logger.debug("Component [ name : " + scannedBeanName + " , class : " + scannedBeanDefinition.getBeanClassName() + " ]");
                }
            }
        }

        return count;

    }
}
