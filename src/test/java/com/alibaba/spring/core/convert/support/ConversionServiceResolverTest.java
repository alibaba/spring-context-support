/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.spring.core.convert.support;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;
import static org.springframework.util.ClassUtils.isAssignable;

/**
 * {@link ConversionServiceResolver} Test
 *
 * @since 1.0.6
 */
public class ConversionServiceResolverTest {

    @Test
    public void testGetResolvedBeanIfAvailable() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        ConversionServiceResolver resolver = new ConversionServiceResolver(beanFactory);

        ConversionService conversionService = resolver.resolve(false);

        assertNotEquals(conversionService, resolver.resolve(false));

        conversionService = resolver.resolve(true);

        assertEquals(conversionService, resolver.resolve());
    }

    @Test
    public void testGetFromBeanFactory() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        ConversionService conversionService = new DefaultConversionService();

        beanFactory.setConversionService(conversionService);

        ConversionServiceResolver resolver = new ConversionServiceResolver(beanFactory);

        assertEquals(conversionService, resolver.resolve(false));
        assertEquals(conversionService, resolver.resolve(true));
        assertEquals(conversionService, resolver.resolve());
    }

    @Test
    public void testGetIfAvailableForBeanDefinition() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerBeanDefinition(CONVERSION_SERVICE_BEAN_NAME,
                genericBeanDefinition(DefaultConversionService.class).getBeanDefinition());

        ConversionServiceResolver resolver = new ConversionServiceResolver(beanFactory);

        assertEquals(resolver.resolve(false), resolver.resolve(true));
    }

    @Test
    public void testGetIfAvailableForSingletonBean() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        ConversionService conversionService = new DefaultConversionService();

        beanFactory.registerSingleton(CONVERSION_SERVICE_BEAN_NAME, conversionService);

        ConversionServiceResolver resolver = new ConversionServiceResolver(beanFactory);

        assertEquals(conversionService, resolver.resolve(false));
        assertEquals(conversionService, resolver.resolve(true));
        assertEquals(conversionService, resolver.resolve());
    }

    @Test
    public void testCreateDefaultConversionService() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        ConversionServiceResolver resolver = new ConversionServiceResolver(beanFactory);
        ConversionService conversionService = resolver.resolve(false);
        assertTrue(isAssignable(DefaultFormattingConversionService.class, conversionService.getClass()));

        conversionService = resolver.resolve(true);
        assertTrue(isAssignable(DefaultFormattingConversionService.class, conversionService.getClass()));

        conversionService = resolver.resolve();
        assertTrue(isAssignable(DefaultFormattingConversionService.class, conversionService.getClass()));
    }

}
