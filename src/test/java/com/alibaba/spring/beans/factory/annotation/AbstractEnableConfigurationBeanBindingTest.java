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

import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * Abstract Test cases for {@link EnableConfigurationBeanBinding}
 *
 * @since 1.0.4
 */
public abstract class AbstractEnableConfigurationBeanBindingTest {

    protected AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(getClass());
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
        context.refresh();
    }

    @After
    public void tearDown() {
        context.close();
    }
}
