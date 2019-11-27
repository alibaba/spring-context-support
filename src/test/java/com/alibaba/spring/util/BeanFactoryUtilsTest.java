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
package com.alibaba.spring.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.alibaba.spring.util.BeanFactoryUtils.getBeans;
import static com.alibaba.spring.util.BeanFactoryUtils.getOptionalBean;
import static com.alibaba.spring.util.ObjectUtils.of;
import static org.junit.Assert.assertEquals;

/**
 * {@link BeanFactoryUtils} Test
 *
 * @since 1.0.2
 */
public class BeanFactoryUtilsTest {

    private AnnotationConfigApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new AnnotationConfigApplicationContext();
    }

    @After
    public void afterTest() {
        applicationContext.close();
    }

    @Test
    public void testGetOptionalBean() {

        applicationContext.register(BaseTestBean.class);

        applicationContext.refresh();

        BaseTestBean testBean = getOptionalBean(applicationContext, "baseTestBean", BaseTestBean.class);

        Assert.assertNotNull(testBean);

        assertEquals("Hello,World", testBean.getName());

    }

    @Test
    public void testGetOptionalBeanIfAbsent() {

        applicationContext.refresh();

        BaseTestBean testBean = getOptionalBean(applicationContext, "baseTestBean", BaseTestBean.class);

        Assert.assertNull(testBean);

        testBean = getOptionalBean(applicationContext, "1", BaseTestBean.class);

        Assert.assertNull(testBean);

        testBean = getOptionalBean(applicationContext, null, BaseTestBean.class);

        Assert.assertNull(testBean);
    }

    @Test
    public void testGetBeans() {

        applicationContext.register(BaseTestBean.class, BaseTestBean2.class);

        applicationContext.refresh();

        List<BaseTestBean> testBeans = getBeans(applicationContext, new String[]{"baseTestBean"}, BaseTestBean.class);

        assertEquals(1, testBeans.size());

        assertEquals("Hello,World", testBeans.get(0).getName());

        testBeans = getBeans(applicationContext, (String[]) null, BaseTestBean.class);

        assertEquals(0, testBeans.size());

        testBeans = getBeans(applicationContext, of((String) null), BaseTestBean.class);

        assertEquals(0, testBeans.size());

        testBeans = getBeans(applicationContext, of("abc"), BaseTestBean.class);

        assertEquals(0, testBeans.size());
    }

    @Test
    public void testGetBeansIfAbsent() {

        applicationContext.refresh();

        List<BaseTestBean> testBeans = getBeans(applicationContext, new String[]{"baseTestBean"}, BaseTestBean.class);

        Assert.assertTrue(testBeans.isEmpty());

    }


    @Component("baseTestBean2")
    private static class BaseTestBean2 extends BaseTestBean {

    }

    @Component("baseTestBean")
    private static class BaseTestBean {

        private String name = "Hello,World";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
