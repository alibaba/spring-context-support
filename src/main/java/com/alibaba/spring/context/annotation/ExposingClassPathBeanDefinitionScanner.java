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
package com.alibaba.spring.context.annotation;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.util.Set;

import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;

/**
 * A extension class of {@link ClassPathBeanDefinitionScanner} to expose some methods:
 * <ul>
 *     <li>{@link ClassPathBeanDefinitionScanner#doScan(String...)}</li>
 *     <li>{@link ClassPathBeanDefinitionScanner#checkCandidate(String, BeanDefinition)}</li>
 * </ul>
 * <p>
 * {@link ExposingClassPathBeanDefinitionScanner} is also a {@link BeanDefinitionRegistry}
 * and {@link SingletonBeanRegistry}
 *
 * @see ClassPathBeanDefinitionScanner
 * @see BeanDefinitionRegistry
 * @see SingletonBeanRegistry
 * @since 1.0.6
 */
public class ExposingClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner implements
        BeanDefinitionRegistry, SingletonBeanRegistry {

    public ExposingClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
                                                  Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment);
        setResourceLoader(resourceLoader);
        registerAnnotationConfigProcessors(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }

    @Override
    public boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }

    public SingletonBeanRegistry getSingletonBeanRegistry() {
        return (SingletonBeanRegistry) getRegistry();
    }

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        getRegistry().registerBeanDefinition(beanName, beanDefinition);
    }

    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        getRegistry().removeBeanDefinition(beanName);
    }

    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return getRegistry().getBeanDefinition(beanName);
    }

    public boolean containsBeanDefinition(String beanName) {
        return getRegistry().containsBeanDefinition(beanName);
    }

    public String[] getBeanDefinitionNames() {
        return getRegistry().getBeanDefinitionNames();
    }

    public int getBeanDefinitionCount() {
        return getRegistry().getBeanDefinitionCount();
    }

    public boolean isBeanNameInUse(String beanName) {
        return getRegistry().isBeanNameInUse(beanName);
    }

    public void registerAlias(String name, String alias) {
        getRegistry().registerAlias(name, alias);
    }

    public void removeAlias(String alias) {
        getRegistry().removeAlias(alias);
    }

    public boolean isAlias(String name) {
        return getRegistry().isAlias(name);
    }

    public String[] getAliases(String name) {
        return getRegistry().getAliases(name);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        getSingletonBeanRegistry().registerSingleton(beanName, singletonObject);
    }

    @Override
    public Object getSingleton(String beanName) {
        return getSingletonBeanRegistry().getSingleton(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return getSingletonBeanRegistry().containsSingleton(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        return getSingletonBeanRegistry().getSingletonNames();
    }

    @Override
    public int getSingletonCount() {
        return getSingletonBeanRegistry().getSingletonCount();
    }

    @Override
    public Object getSingletonMutex() {
        return getSingletonBeanRegistry().getSingletonMutex();
    }
}
