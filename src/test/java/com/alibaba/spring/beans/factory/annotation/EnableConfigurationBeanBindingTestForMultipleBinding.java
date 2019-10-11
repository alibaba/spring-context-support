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
import com.alibaba.spring.util.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        EnableConfigurationBeanBindingTestForMultipleBinding.SingleConfig.class,
        EnableConfigurationBeanBindingTestForMultipleBinding.MultipleConfig.class
})
@TestPropertySource(properties = {
        "users.a.name = name-a",
        "users.a.age = 1",
        "users.b.name = name-b",
        "users.b.age = 2",
        "user.id= c",
        "user.name = name-c",
        "user.age = 3"
})
public class EnableConfigurationBeanBindingTestForMultipleBinding {

    @Autowired
    @Qualifier("a")
    private User aUser;

    @Autowired
    @Qualifier("b")
    private User bUser;

    @Autowired
    @Qualifier("c")
    private User cUser;

    @Autowired
    private Collection<User> users;

    @Autowired
    @Qualifier(ConfigurationBeanBindingPostProcessor.BEAN_NAME)
    private ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor;

    @EnableConfigurationBeanBinding(prefix = "user", type = User.class)
    static class SingleConfig {

        @Bean
        public ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor() {
            ConfigurationBeanBindingPostProcessor processor = new ConfigurationBeanBindingPostProcessor();
            processor.setConfigurationBeanBinder(new DefaultConfigurationBeanBinder());
            return processor;
        }
    }

    @EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true, ignoreUnknownFields = false, ignoreInvalidFields = false)
    static class MultipleConfig {
    }

    @Test
    public void testUser() {
        assertEquals(3, users.size());
        assertTrue(users.contains(aUser));
        assertTrue(users.contains(bUser));

        assertEquals("name-a", aUser.getName());
        assertEquals(1, aUser.getAge());

        assertEquals("name-b", bUser.getName());
        assertEquals(2, bUser.getAge());

        assertEquals("name-c", cUser.getName());
        assertEquals(3, cUser.getAge());

        assertNotNull(configurationBeanBindingPostProcessor.getConfigurationBeanBinder());
    }
}
