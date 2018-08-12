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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.spring.util.ClassUtils.resolveGenericType;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * Abstract Generic {@link BeanPostProcessor} for injecting annotated type instance
 *
 * @param <A> The type of {@link Annotation}
 * @param <B> The type of injected-bean
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.1
 */
public abstract class AnnotationInjectedBeanPostProcessor<A extends Annotation, B> extends
        InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor, PriorityOrdered,
        BeanFactoryAware, BeanClassLoaderAware, EnvironmentAware, DisposableBean {

    private final Log logger = LogFactory.getLog(getClass());

    private final Class<A> annotationType;

    private final ConcurrentMap<String, AnnotatedInjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<String, AnnotatedInjectionMetadata>(32);

    private final ConcurrentMap<String, B> beanCaches = new ConcurrentHashMap<String, B>();

    private ConfigurableListableBeanFactory beanFactory;

    private Environment environment;

    private ClassLoader classLoader;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public AnnotationInjectedBeanPostProcessor() {
        this.annotationType = resolveGenericType(getClass());
    }

    private static <T> Collection<T> combine(Collection<? extends T>... elements) {
        List<T> allElements = new ArrayList<T>();
        for (Collection<? extends T> e : elements) {
            allElements.addAll(e);
        }
        return allElements;
    }

    /**
     * Annotation type
     *
     * @return non-null
     */
    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory,
                "AnnotationInjectedBeanPostProcessor requires a ConfigurableListableBeanFactory");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {

        InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @A dependencies failed", ex);
        }
        return pvs;
    }


    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated {@link A} fields
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedFieldElement> findFieldAnnotationMetadata(final Class<?> beanClass) {

        final List<AnnotatedFieldElement> elements = new LinkedList<AnnotatedFieldElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                A annotation = getAnnotation(field, getAnnotationType());

                if (annotation != null) {

                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@A annotation is not supported on static fields: " + field);
                        }
                        return;
                    }

                    elements.add(new AnnotatedFieldElement(field, annotation));
                }

            }
        });

        return elements;

    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated {@link A @A} methods
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedMethodElement> findAnnotatedMethodMetadata(final Class<?> beanClass) {

        final List<AnnotatedMethodElement> elements = new LinkedList<AnnotatedMethodElement>();

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

                Method bridgedMethod = findBridgedMethod(method);

                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }

                A annotation = findAnnotation(bridgedMethod, getAnnotationType());

                if (annotation != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@" + getAnnotationType().getSimpleName() + " annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@" + getAnnotationType().getSimpleName() + " annotation should only be used on methods with parameters: " +
                                    method);
                        }
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                    elements.add(new AnnotatedMethodElement(method, pd, annotation));
                }
            }
        });

        return elements;

    }


    private AnnotatedInjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
        Collection<AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
        Collection<AnnotatedMethodElement> methodElements = findAnnotatedMethodMetadata(beanClass);
        return new AnnotatedInjectionMetadata(beanClass, fieldElements, methodElements);

    }

    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        AnnotatedInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildAnnotatedMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for annotation metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findInjectionMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void destroy() throws Exception {

        for (B bean : beanCaches.values()) {
            if (logger.isInfoEnabled()) {
                logger.info(bean + " was destroying!");
            }

            if (bean instanceof DisposableBean) {
                ((DisposableBean) bean).destroy();
            }
        }

        injectionMetadataCache.clear();
        beanCaches.clear();

        if (logger.isInfoEnabled()) {
            logger.info(getClass() + " was destroying!");
        }

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Gets all beans of {@link B}
     *
     * @return non-null {@link Collection}
     */
    public Collection<B> getBeans() {
        return this.beanCaches.values();
    }

    /**
     * Get injected-bean from specified {@link A annotation} and Bean Class
     *
     * @param annotation {@link A annotation}
     * @param beanClass  Bean Class
     * @return The injected bean
     * @throws Exception
     */
    B getInjectedBean(A annotation, Class<B> beanClass) throws Exception {

        String cacheKey = generateInjectedBeanCacheKey(annotation, beanClass, beanFactory, environment, classLoader);

        B bean = beanCaches.get(cacheKey);

        if (bean == null) {
            bean = resolveInjectedBean(annotation, beanClass, beanFactory, environment, classLoader);
            beanCaches.putIfAbsent(cacheKey, bean);
        }

        return bean;

    }

    /**
     * Resolve injected-bean from specified {@link A annotation} and Bean Class
     *
     * @param annotation  {@link A annotation}
     * @param beanClass   Bean Class
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @param environment {@link Environment}
     * @param classLoader {@link ClassLoader}
     * @return The injected bean
     * @throws Exception If resolving an injected bean is failed.
     */
    protected abstract B resolveInjectedBean(A annotation, Class<B> beanClass,
                                             ConfigurableListableBeanFactory beanFactory,
                                             Environment environment, ClassLoader classLoader) throws Exception;

    /**
     * Generate a cache key of injected-{@link B bean}
     *
     * @param annotation  {@link A}
     * @param beanClass   {@link Class}
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @param environment {@link Environment}
     * @param classLoader {@link ClassLoader}
     * @return Bean cache key
     */
    protected abstract String generateInjectedBeanCacheKey(A annotation, Class<B> beanClass,
                                                           ConfigurableListableBeanFactory beanFactory,
                                                           Environment environment, ClassLoader classLoader);

    /**
     * Get {@link B} {@link Map} in injected field.
     *
     * @return non-null {@link Map}
     */
    protected Map<InjectionMetadata.InjectedElement, B> getInjectedFieldBeanMap() {

        Map<InjectionMetadata.InjectedElement, B> injectedElementBeanMap =
                new LinkedHashMap<InjectionMetadata.InjectedElement, B>();

        for (AnnotatedInjectionMetadata metadata : injectionMetadataCache.values()) {

            Collection<AnnotatedFieldElement> fieldElements = metadata.getFieldElements();

            for (AnnotatedFieldElement fieldElement : fieldElements) {

                injectedElementBeanMap.put(fieldElement, fieldElement.bean);

            }

        }

        return injectedElementBeanMap;

    }

    /**
     * Get {@link B} {@link Map} in injected method.
     *
     * @return non-null {@link Map}
     */
    protected Map<InjectionMetadata.InjectedElement, B> getInjectedMethodBeanMap() {

        Map<InjectionMetadata.InjectedElement, B> injectedElementBeanMap =
                new LinkedHashMap<InjectionMetadata.InjectedElement, B>();

        for (AnnotatedInjectionMetadata metadata : injectionMetadataCache.values()) {

            Collection<AnnotatedMethodElement> methodElements = metadata.getMethodElements();

            for (AnnotatedMethodElement methodElement : methodElements) {

                injectedElementBeanMap.put(methodElement, methodElement.bean);

            }

        }

        return injectedElementBeanMap;

    }

    private <T> T getFieldValue(Object object, String fieldName, Class<T> fieldType) {

        Field field = ReflectionUtils.findField(object.getClass(), fieldName, fieldType);

        ReflectionUtils.makeAccessible(field);

        return (T) ReflectionUtils.getField(field, object);

    }

    /**
     * {@link A} {@link InjectionMetadata} implementation
     */
    private class AnnotatedInjectionMetadata extends InjectionMetadata {

        private final Collection<AnnotatedFieldElement> fieldElements;

        private final Collection<AnnotatedMethodElement> methodElements;


        public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<AnnotatedFieldElement> fieldElements,
                                          Collection<AnnotatedMethodElement> methodElements) {
            super(targetClass, combine(fieldElements, methodElements));
            this.fieldElements = fieldElements;
            this.methodElements = methodElements;
        }

        public Collection<AnnotatedFieldElement> getFieldElements() {
            return fieldElements;
        }

        public Collection<AnnotatedMethodElement> getMethodElements() {
            return methodElements;
        }
    }

    /**
     * {@link A} {@link Method} {@link InjectionMetadata.InjectedElement}
     */
    private class AnnotatedMethodElement extends InjectionMetadata.InjectedElement {

        private final Method method;

        private final A annotation;

        private volatile B bean;

        protected AnnotatedMethodElement(Method method, PropertyDescriptor pd, A annotation) {
            super(method, pd);
            this.method = method;
            this.annotation = annotation;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<B> beanClass = (Class<B>) pd.getPropertyType();

            ReflectionUtils.makeAccessible(method);

            method.invoke(bean, getInjectedBean(annotation, beanClass));

        }

    }

    /**
     * {@link A} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    private class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        private final A annotation;

        private volatile B bean;

        protected AnnotatedFieldElement(Field field, A annotation) {
            super(field, null);
            this.field = field;
            this.annotation = annotation;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<B> beanClass = (Class<B>) field.getType();

            ReflectionUtils.makeAccessible(field);

            field.set(bean, getInjectedBean(annotation, beanClass));

        }

    }
}
