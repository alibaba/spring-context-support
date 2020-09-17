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

import com.alibaba.spring.util.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {@link AbstractAnnotationBeanPostProcessor} Test
 *
 * @since 1.0.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        AnnotationInjectedBeanPostProcessorTest.TestConfiguration.class,
        AbstractAnnotationBeanPostProcessorTest.ReferencedAnnotationInjectedBeanPostProcessor.class,
        AbstractAnnotationBeanPostProcessorTest.GenericConfiguration.class,
})
@SuppressWarnings({"deprecation", "unchecked"})
public class AbstractAnnotationBeanPostProcessorTest {

    @Autowired
    @Qualifier("parent")
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.Parent parent;

    @Autowired
    @Qualifier("child")
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.Child child;

    @Autowired
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.UserHolder userHolder;

    @Autowired
    private AbstractAnnotationBeanPostProcessor processor;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private GenericConfiguration.GenericChild genericChild;

    @Test
    public void testCustomizedAnnotationBeanPostProcessor() {

        Assert.assertEquals(environment, processor.getEnvironment());
        Assert.assertEquals(beanFactory.getBeanClassLoader(), processor.getClassLoader());
        Assert.assertEquals(beanFactory, processor.getBeanFactory());

        Assert.assertEquals(AnnotationInjectedBeanPostProcessorTest.Referenced.class, processor.getAnnotationType());
        Assert.assertEquals(1, processor.getInjectedObjects().size());
        Assert.assertTrue(processor.getInjectedObjects().contains(parent.parentUser));
        Assert.assertEquals(3, processor.getInjectedFieldObjectsMap().size());
        Assert.assertEquals(1, processor.getInjectedMethodObjectsMap().size());
        Assert.assertEquals(Ordered.LOWEST_PRECEDENCE - 3, processor.getOrder());
    }

    @Test
    public void testReferencedUser() {
        Assert.assertEquals("mercyblitz", parent.user.getName());
        Assert.assertEquals(32, parent.user.getAge());
        Assert.assertEquals(parent.user, parent.parentUser);
        Assert.assertEquals(parent.user, child.childUser);
        Assert.assertEquals(parent.user, userHolder.user);
        Assert.assertEquals(parent.user, genericChild.getS());
    }

    public static class ReferencedAnnotationInjectedBeanPostProcessor extends AbstractAnnotationBeanPostProcessor {

        public ReferencedAnnotationInjectedBeanPostProcessor() {
            super(AnnotationInjectedBeanPostProcessorTest.Referenced.class);
        }

        @Override
        protected Object doGetInjectedBean(AnnotationAttributes attributes, Object bean, String beanName,
                                           Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) throws Exception {
            return getBeanFactory().getBean(injectedType);
        }

        @Override
        protected String buildInjectedObjectCacheKey(AnnotationAttributes attributes, Object bean, String beanName,
                                                     Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) {
            return injectedType.getName();
        }
    }

    public static class GenericConfiguration {


        static abstract class GenericParent<S> {

            @AnnotationInjectedBeanPostProcessorTest.Referenced
            S s;

            public S getS() {
                return s;
            }
        }

        static class GenericChild extends GenericParent<User> {
        }

        @Bean
        public GenericChild genericChild() {
            return new GenericChild();
        }

    }

}
