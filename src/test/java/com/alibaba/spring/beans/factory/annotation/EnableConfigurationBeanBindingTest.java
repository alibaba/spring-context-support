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

import com.alibaba.spring.context.config.ConfigurationBeanBinder;
import com.alibaba.spring.context.config.ConfigurationBeanCustomizer;
import com.alibaba.spring.context.config.DefaultConfigurationBeanBinder;
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

import static org.junit.Assert.assertEquals;

public class EnableConfigurationBeanBindingTest {

    private AnnotationConfigApplicationContext context;

    @EnableConfigurationBeanBinding(prefix = "user", type = User.class)
    static class Config {

        @Bean
        public ConfigurationBeanCustomizer customizer() {
            return new ConfigurationBeanCustomizer() {

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public void customize(String beanName, Object configurationBean) {
                    if ("m".equals(beanName) && configurationBean instanceof User) {
                        User user = (User) configurationBean;
                        user.setAge(19);
                    }
                }
            };
        }

        @Bean
        public ConfigurationBeanBinder configurationBeanBinder() {
            return new DefaultConfigurationBeanBinder();
        }
    }

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
        context.register(Config.class);
        context.refresh();
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void testUser() {
        User user = context.getBean("m", User.class);
        assertEquals("mercyblitz", user.getName());
        assertEquals(19, user.getAge());
    }
}
