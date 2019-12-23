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
package com.alibaba.spring.context;

import org.junit.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.support.GenericApplicationContext;

import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;


/**
 * {@link OnceApplicationContextEventListener} Test
 * @see OnceApplicationContextEventListener
 * @since 1.0.6
 */
public class OnceApplicationContextEventListenerTest {

    @Test
    public void test() {

        for (int levels = 1; levels < 100; levels++) {
            testOnceApplicationContextEventListener(levels, true, new MyContextEventListener());
            testOnceApplicationContextEventListener(levels, false, new MyContextEventListener());
        }
    }

    private void testOnceApplicationContextEventListener(int levels, boolean listenersAsBean, ApplicationListener... listeners) {

        ConfigurableApplicationContext context = createContext(levels, listenersAsBean, listeners);

        context.start();

        context.stop();

        context.close();

    }

    private ConfigurableApplicationContext createContext(int levels, boolean listenersAsBean, ApplicationListener... listeners) {

        if (levels < 1) {
            return null;
        }

        ConfigurableApplicationContext context = new GenericApplicationContext();

        int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ApplicationListener listener = listeners[i];

            if (listener instanceof ApplicationContextAware) {
                ((ApplicationContextAware) listener).setApplicationContext(context);
            }

            if (listenersAsBean) {
                AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
                if (beanFactory instanceof SingletonBeanRegistry) {
                    SingletonBeanRegistry registry = (SingletonBeanRegistry) beanFactory;
                    registry.registerSingleton("listener-" + (i + 1), listener);
                }
            } else {
                context.addApplicationListener(listener);
            }
        }

        context.setParent(createContext(levels - 1, listenersAsBean));

        context.refresh();

        return context;
    }

    static class MyContextEventListener extends OnceApplicationContextEventListener {


        private Map<EventObject, AtomicInteger> eventsHandledCount = new LinkedHashMap<EventObject, AtomicInteger>();

        @Override
        protected void onApplicationContextEvent(ApplicationContextEvent event) {

            assertEquals(getApplicationContext(), event.getApplicationContext());

            AtomicInteger count = eventsHandledCount.get(event);

            if (count == null) {
                count = new AtomicInteger();
                eventsHandledCount.put(event, count);
            }

            assertEquals(0, count.getAndIncrement());
        }
    }

}
