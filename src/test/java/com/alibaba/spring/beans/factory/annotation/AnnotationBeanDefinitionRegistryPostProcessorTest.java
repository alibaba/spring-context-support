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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static com.alibaba.spring.beans.factory.annotation.AnnotationBeanDefinitionRegistryPostProcessor.getAnnotation;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * {@link AnnotationBeanDefinitionRegistryPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.6
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        AnnotationBeanDefinitionRegistryPostProcessorTest.ServiceAnnotationBeanDefinitionRegistryPostProcessor.class,
        AnnotationBeanDefinitionRegistryPostProcessorTest.class
})
@Configuration
public class AnnotationBeanDefinitionRegistryPostProcessorTest {

    @Service
    static class MyService {
    }

    @Autowired
    private MyService myService;

    @Qualifier("stringBean")
    @Autowired
    private String stringBean;

    @Test
    public void test() {
        assertNotNull(myService);
        assertEquals("Hello,World", stringBean);
    }

    @Test
    public void testGetAnnotation() {
        assertNotNull(getAnnotation(MyService.class, Service.class));
    }

    @Test
    public void testResolveBeanClass() {

    }

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface Service {
    }

    static class ServiceAnnotationBeanDefinitionRegistryPostProcessor extends
            AnnotationBeanDefinitionRegistryPostProcessor {

        public ServiceAnnotationBeanDefinitionRegistryPostProcessor() {
            super(Service.class, Service.class);
        }

        @Override
        protected void registerSecondaryBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                        Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions,
                                                        String[] basePackages) {
            scanner.registerSingleton("stringBean", "Hello,World");

        }
    }
}
