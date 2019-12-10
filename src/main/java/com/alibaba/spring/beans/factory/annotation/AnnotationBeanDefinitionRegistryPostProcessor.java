/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.spring.beans.factory.annotation;

import com.alibaba.spring.context.annotation.ExposingClassPathBeanDefinitionScanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.spring.util.AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator;
import static com.alibaba.spring.util.AnnotationUtils.tryGetMergedAnnotation;
import static com.alibaba.spring.util.WrapperUtils.unwrap;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * An abstract class for the extension to {@link BeanDefinitionRegistryPostProcessor}, which will execute two main registration
 * methods orderly:
 * <ol>
 *     <li>{@link #registerPrimaryBeanDefinitions(ExposingClassPathBeanDefinitionScanner, String[])} : Scan and register
 *     the primary {@link BeanDefinition BeanDefinitions} that were annotated by
 *     {@link #getSupportedAnnotationTypes() the supported annotation types}, and then return the {@link Map} with bean name plus
 *     aliases if present and primary {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions},
 *     it's allowed to be override
 *     </li>
 *     <li>{@link #registerSecondaryBeanDefinitions(ExposingClassPathBeanDefinitionScanner, Map, String[])} :
 *      it's mandatory to be override by the sub-class to register secondary {@link BeanDefinition BeanDefinitions}
 *      if required
 *     </li>
 * </ol>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.6
 */
@SuppressWarnings("unchecked")
public abstract class AnnotationBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor,
        BeanFactoryAware, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Set<Class<? extends Annotation>> supportedAnnotationTypes;

    private final Set<String> packagesToScan;

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurableEnvironment environment;

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> primaryAnnotationType,
                                                         Class<?>... basePackageClasses) {
        this(primaryAnnotationType, resolveBasePackages(basePackageClasses));
    }

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> primaryAnnotationType,
                                                         String... packagesToScan) {
        this(primaryAnnotationType, asList(packagesToScan));
    }

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> primaryAnnotationType,
                                                         Iterable<String> packagesToScan) {
        this.supportedAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();
        addSupportedAnnotationType(primaryAnnotationType);
        this.packagesToScan = new LinkedHashSet<String>();
        Iterator<String> iterator = packagesToScan.iterator();
        while (iterator.hasNext()) {
            this.packagesToScan.add(iterator.next());
        }
    }

    public void addSupportedAnnotationType(Class<? extends Annotation>... annotationTypes) {
        Assert.notEmpty(annotationTypes, "The argument of annotation types can't be empty");
        Assert.noNullElements(annotationTypes, "Any element of annotation types can't be null");
        this.supportedAnnotationTypes.addAll(asList(annotationTypes));
    }

    private static String[] resolveBasePackages(Class<?>... basePackageClasses) {
        int size = basePackageClasses.length;
        String[] basePackages = new String[size];
        for (int i = 0; i < size; i++) {
            basePackages[i] = basePackageClasses[i].getPackage().getName();
        }
        return basePackages;
    }

    protected static Annotation getAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        Annotation annotation = tryGetMergedAnnotation(annotatedElement, annotationType);
        if (annotation == null) {
            annotation = annotatedElement.getAnnotation(annotationType);
        }
        return annotation;
    }

    @Override
    public final void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        String[] basePackages = resolveBasePackages(getPackagesToScan());

        if (!ObjectUtils.isEmpty(basePackages)) {
            registerBeanDefinitions(registry, basePackages);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("packagesToScan is empty , The BeanDefinition's registry will be ignored!");
            }
        }
    }

    private void registerBeanDefinitions(BeanDefinitionRegistry registry, String[] basePackages) {

        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(registry, false,
                getEnvironment(), getResourceLoader());

        BeanNameGenerator beanNameGenerator = resolveAnnotatedBeanNameGenerator(registry);
        // Set the BeanNameGenerator
        scanner.setBeanNameGenerator(beanNameGenerator);
        // Add the AnnotationTypeFilter for annotationTypes
        for (Class<? extends Annotation> supportedAnnotationType : getSupportedAnnotationTypes()) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(supportedAnnotationType));
        }
        // Register the primary BeanDefinitions
        Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions = registerPrimaryBeanDefinitions(scanner, basePackages);
        // Register the secondary BeanDefinitions
        registerSecondaryBeanDefinitions(scanner, primaryBeanDefinitions, basePackages);
    }

    /**
     * Scan and register the primary {@link BeanDefinition BeanDefinitions} that were annotated by
     * {@link #getSupportedAnnotationTypes() the supported annotation types}, and then return the {@link Map} with bean name plus
     * aliases if present and primary {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}.
     * <p>
     * Current method is allowed to be override by the sub-class to change the registration logic
     *
     * @param scanner      {@link ExposingClassPathBeanDefinitionScanner}
     * @param basePackages the base packages to scan
     * @return the {@link Map} with bean name plus aliases if present and primary
     * {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}
     */
    protected Map<String, AnnotatedBeanDefinition> registerPrimaryBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                                                  String[] basePackages) {
        // Scan and register
        Set<BeanDefinitionHolder> primaryBeanDefinitionHolders = scanner.doScan(basePackages);
        // Log the primary BeanDefinitions
        logPrimaryBeanDefinitions(primaryBeanDefinitionHolders, basePackages);

        Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions = new LinkedHashMap<String, AnnotatedBeanDefinition>();

        for (BeanDefinitionHolder beanDefinitionHolder : primaryBeanDefinitionHolders) {
            putPrimaryBeanDefinitions(primaryBeanDefinitions, beanDefinitionHolder);
        }

        // return
        return primaryBeanDefinitions;
    }

    private void putPrimaryBeanDefinitions(Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions,
                                           BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();

        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            putPrimaryBeanDefinition(primaryBeanDefinitions, annotatedBeanDefinition, beanDefinitionHolder.getBeanName());
            putPrimaryBeanDefinition(primaryBeanDefinitions, annotatedBeanDefinition, beanDefinitionHolder.getAliases());
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("What's the problem? Please investigate " + beanDefinitionHolder);
            }
        }
    }

    private void putPrimaryBeanDefinition(Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions,
                                          AnnotatedBeanDefinition annotatedBeanDefinition,
                                          String... keys) {
        if (!ObjectUtils.isEmpty(keys)) {
            for (String key : keys) {
                primaryBeanDefinitions.put(key, annotatedBeanDefinition);
            }
        }
    }

    /**
     * Register the secondary {@link BeanDefinition BeanDefinitions}
     * <p>
     * Current method is allowed to be override by the sub-class to change the registration logic
     *
     * @param scanner                the {@link ExposingClassPathBeanDefinitionScanner} instance
     * @param primaryBeanDefinitions the {@link Map} with bean name plus aliases if present and primary
     *                               {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}, which may be empty
     * @param basePackages           the base packages to scan
     */
    protected abstract void registerSecondaryBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                             Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions,
                                                             String[] basePackages);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }

    private void logPrimaryBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitionHolders, String[] basePackages) {
        if (isEmpty(beanDefinitionHolders)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No Spring Bean annotation @" + getSupportedAnnotationTypeNames() + " was found under base packages"
                        + asList(basePackages));
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(beanDefinitionHolders.size() + " annotations " + getSupportedAnnotationTypeNames() + " components { " +
                        beanDefinitionHolders + " } were scanned under packages" + asList(basePackages));
            }
        }
    }

    /**
     * Resolve the placeholders for the raw scanned packages to scan
     *
     * @param packagesToScan the raw scanned packages to scan
     * @return non-null
     */
    protected String[] resolveBasePackages(Set<String> packagesToScan) {
        Set<String> resolvedPackagesToScan = new LinkedHashSet<String>(packagesToScan.size());
        for (String packageToScan : packagesToScan) {
            if (StringUtils.hasText(packageToScan)) {
                String resolvedPackageToScan = getEnvironment().resolvePlaceholders(packageToScan.trim());
                resolvedPackagesToScan.add(resolvedPackageToScan);
            }
        }
        // Set to Array
        return packagesToScan.toArray(new String[packagesToScan.size()]);
    }

    protected final Class<?> resolveBeanClass(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return resolveBeanClass(beanDefinition);
    }

    protected final Class<?> resolveBeanClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        return resolveClassName(beanClassName, getClassLoader());
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Set<Class<? extends Annotation>> getSupportedAnnotationTypes() {
        return unmodifiableSet(supportedAnnotationTypes);
    }

    protected Set<String> getSupportedAnnotationTypeNames() {
        Set<String> supportedAnnotationTypeNames = new LinkedHashSet<String>();
        for (Class<? extends Annotation> supportedAnnotationType : supportedAnnotationTypes) {
            supportedAnnotationTypeNames.add(supportedAnnotationType.getName());
        }
        return unmodifiableSet(supportedAnnotationTypeNames);
    }

    public Set<String> getPackagesToScan() {
        return packagesToScan;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = unwrap(beanFactory);
    }

    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = unwrap(environment);
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
