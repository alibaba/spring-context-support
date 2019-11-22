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
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

import java.util.Collection;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true, ignoreUnknownFields = false,
        ignoreInvalidFields = false)
public class EnableConfigurationBeanBindingTestForMultipleBinding extends AbstractEnableConfigurationBeanBindingTest {

    @Bean
    public ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor() {
        ConfigurationBeanBindingPostProcessor processor = new ConfigurationBeanBindingPostProcessor();
        processor.setConfigurationBeanBinder(new DefaultConfigurationBeanBinder());
        return processor;
    }

    private User aUser;

    private User bUser;

    private User mUser;

    private Collection<User> users;

    private ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor;

    @Before
    public void init() {
        aUser = context.getBean("a", User.class);
        bUser = context.getBean("b", User.class);
        users = BeanUtils.getSortedBeans(context, User.class);
        configurationBeanBindingPostProcessor = context.getBean("configurationBeanBindingPostProcessor", ConfigurationBeanBindingPostProcessor.class);
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
