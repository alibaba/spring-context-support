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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import static com.alibaba.spring.util.BeanUtils.getBeanIfAvailable;
import static com.alibaba.spring.util.BeanUtils.isBeanPresent;
import static java.lang.String.format;
import static org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;

/**
 * The class to resolve a singleton instance of {@link ConversionService} that may be retrieved from Spring
 * {@link ConfigurableApplicationContext#CONVERSION_SERVICE_BEAN_NAME built-in bean} or create a new one.
 *
 * @see ConversionService
 * @see ConfigurableApplicationContext#CONVERSION_SERVICE_BEAN_NAME
 * @see SingletonBeanRegistry#registerSingleton(String, Object)
 * @since 1.0.6
 */
public class ConversionServiceResolver {

    /**
     * The bean name of a singleton instance of {@link ConversionService} has been resolved
     */
    public static final String RESOLVED_CONVERSION_SERVICE_BEAN_NAME = "resolved-" + CONVERSION_SERVICE_BEAN_NAME;

    private final Log logger = LogFactory.getLog(getClass());

    private final ConfigurableBeanFactory beanFactory;

    public ConversionServiceResolver(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public ConversionService resolve() {
        return resolve(true);
    }

    public ConversionService resolve(boolean requireToRegister) {

        ConversionService conversionService = getResolvedBeanIfAvailable();

        if (conversionService == null) { // If not resolved, try to get from ConfigurableBeanFactory
            conversionService = getFromBeanFactory();
        }

        if (conversionService == null) { // If not found, try to get the bean from BeanFactory
            debug("The conversionService instance can't be found in Spring ConfigurableBeanFactory.getConversionService()");
            conversionService = getIfAvailable();
        }
        if (conversionService == null) { // If not found, will create an instance of ConversionService as default
            conversionService = createDefaultConversionService();
        }

        if (!isBeanPresent(beanFactory, RESOLVED_CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)
                && requireToRegister) { // To register a singleton into SingletonBeanRegistry(ConfigurableBeanFactory)
            beanFactory.registerSingleton(RESOLVED_CONVERSION_SERVICE_BEAN_NAME, conversionService);
        }

        return conversionService;
    }

    private ConversionService getResolvedBeanIfAvailable() {
        return getBeanIfAvailable(beanFactory, RESOLVED_CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
    }

    private ConversionService getFromBeanFactory() {
        return beanFactory.getConversionService();
    }

    private ConversionService getIfAvailable() {
        return getBeanIfAvailable(beanFactory, CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
    }

    /**
     * Create the instance of {@link DefaultFormattingConversionService} as the default,
     * this method is allow to be override by the sub-class.
     *
     * @return non-null
     */
    protected ConversionService createDefaultConversionService() {
        return new DefaultFormattingConversionService();
    }

    private void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format(message, args));
        }
    }
}
