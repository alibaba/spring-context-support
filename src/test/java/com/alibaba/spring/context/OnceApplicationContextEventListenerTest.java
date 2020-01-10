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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;


/**
 * {@link OnceApplicationContextEventListener} Test
 *
 * @see OnceApplicationContextEventListener
 * @since 1.0.6
 */
public class OnceApplicationContextEventListenerTest {

    @Test
    public void test() {

        for (int levels = 1; levels < 100; levels++) {
            testOnceApplicationContextEventListener(levels, true);
            testOnceApplicationContextEventListener(levels, false);
        }
    }

    private void testOnceApplicationContextEventListener(int levels, boolean listenersAsBean) {

        ConfigurableApplicationContext context = createContext(levels, listenersAsBean);

        context.start();

        context.stop();

        context.close();

    }

    private ConfigurableApplicationContext createContext(int levels, boolean listenersAsBean) {

        if (levels < 1) {
            return null;
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        if (listenersAsBean) {
            context.register(MyContextEventListener.class);
        } else {
            context.addApplicationListener(new MyContextEventListener(context));
        }

        context.setParent(createContext(levels - 1, listenersAsBean));

        context.refresh();

        return context;
    }

    static class MyContextEventListener extends OnceApplicationContextEventListener {

        public MyContextEventListener() {

        }

        public MyContextEventListener(ApplicationContext applicationContext) {
            super(applicationContext);
        }


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
