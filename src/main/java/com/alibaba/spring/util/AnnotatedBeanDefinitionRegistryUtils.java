package com.alibaba.spring.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Register Beans
     *
     * @param registry         {@link BeanDefinitionRegistry}
     * @param annotatedClasses {@link Annotation annotation} class
     */
    public static void registerBeans(BeanDefinitionRegistry registry, Class<?>... annotatedClasses) {

        if (ObjectUtils.isEmpty(annotatedClasses)) {
            return;
        }

        boolean debugEnabled = logger.isDebugEnabled();

        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);

        if (debugEnabled) {
            logger.debug(registry.getClass().getSimpleName() + " will register annotated classes : " + Arrays.asList(annotatedClasses) + " .");
        }

        reader.register(annotatedClasses);

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
