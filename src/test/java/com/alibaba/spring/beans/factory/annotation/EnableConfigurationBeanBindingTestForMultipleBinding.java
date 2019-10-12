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

/**
 * {@link EnableConfigurationBeanBinding} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.3
 */

import com.alibaba.spring.context.config.DefaultConfigurationBeanBinder;
import com.alibaba.spring.util.BeanUtils;
import com.alibaba.spring.util.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EnableConfigurationBeanBindingTestForMultipleBinding {

    private User aUser;

    private User bUser;

    private User mUser;

    private Collection<User> users;

    private ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor;

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.setEnvironment(new AbstractEnvironment() {
            @Override
            protected void customizePropertySources(MutablePropertySources propertySources) {
                ResourceLoader resourceLoader = new DefaultResourceLoader();
                ResourcePropertySource propertySource = null;
                try {
                    propertySource = new ResourcePropertySource("temp",
                            resourceLoader.getResource("classpath:/enable-configuration-bean-binding.properties"));
                } catch (IOException e) {
                }
                propertySources.addFirst(propertySource);
            }
        });
        context.register(MultipleConfig.class);
        context.refresh();

        aUser = context.getBean("a", User.class);
        bUser = context.getBean("b", User.class);
        users = BeanUtils.getSortedBeans(context, User.class);
        configurationBeanBindingPostProcessor = context.getBean("configurationBeanBindingPostProcessor", ConfigurationBeanBindingPostProcessor.class);
    }

    @After
    public void tearDown() {
        context.close();
    }

    @EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true, ignoreUnknownFields = false,
            ignoreInvalidFields = false)
    static class MultipleConfig {

        @Bean
        public ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor() {
            ConfigurationBeanBindingPostProcessor processor = new ConfigurationBeanBindingPostProcessor();
            processor.setConfigurationBeanBinder(new DefaultConfigurationBeanBinder());
            return processor;
        }
    }

    @Test
    public void testUser() {

        assertEquals(2, users.size());
        assertTrue(users.contains(aUser));
        assertTrue(users.contains(bUser));

        assertEquals("name-a", aUser.getName());
        assertEquals(1, aUser.getAge());

        assertEquals("name-b", bUser.getName());
        assertEquals(2, bUser.getAge());

        assertNotNull(configurationBeanBindingPostProcessor.getConfigurationBeanBinder());
    }
}
