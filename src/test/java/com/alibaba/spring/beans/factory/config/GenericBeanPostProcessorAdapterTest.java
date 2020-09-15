package com.alibaba.spring.beans.factory.config;

import com.alibaba.spring.util.Bean;
import com.alibaba.spring.util.TestBean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeansException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link GenericBeanPostProcessorAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericBeanPostProcessorAdapter
 * @since 2017.01.22
 */
@SuppressWarnings("unchecked")
public class GenericBeanPostProcessorAdapterTest {

    @Test
    public void testPostProcessBeforeInitialization() {

        TestBean testBean = new TestBean();

        GenericBeanPostProcessorAdapter<Bean> beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {

            protected Bean doPostProcessBeforeInitialization(Bean bean, String beanName) throws BeansException {
                return null;
            }
        };

        assertNull(beanPostProcessor.postProcessBeforeInitialization(testBean, "testBean"));
        assertNull(beanPostProcessor.doPostProcessBeforeInitialization(testBean, "testBean"));
        beanPostProcessor.processBeforeInitialization(testBean, "testBean");

        beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {

            protected void processBeforeInitialization(Bean bean, String beanName) throws BeansException {
            }
        };

        assertEquals(testBean, beanPostProcessor.postProcessBeforeInitialization(testBean, "testBean"));
        assertEquals(testBean, beanPostProcessor.doPostProcessBeforeInitialization(testBean, "testBean"));
        beanPostProcessor.processBeforeInitialization(testBean, "testBean");

    }

    @Test
    public void testPostProcessAfterInitialization() {

        TestBean testBean = new TestBean();

        GenericBeanPostProcessorAdapter beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {

            protected Bean doPostProcessAfterInitialization(Bean bean, String beanName) throws BeansException {
                return null;
            }
        };

        assertNull(beanPostProcessor.postProcessAfterInitialization(testBean, "testBean"));
        assertNull(beanPostProcessor.doPostProcessAfterInitialization(testBean, "testBean"));
        beanPostProcessor.processAfterInitialization(testBean, "testBean");

        beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {

            protected Bean doPostProcessAfterInitialization(Bean bean, String beanName) throws BeansException {
                return null;
            }
        };

        assertNull(beanPostProcessor.postProcessAfterInitialization(testBean, "testBean"));

        beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {

            protected void processAfterInitialization(Bean bean, String beanName) throws BeansException {
            }
        };

        assertEquals(testBean, beanPostProcessor.postProcessAfterInitialization(testBean, "testBean"));
        assertEquals(testBean, beanPostProcessor.doPostProcessAfterInitialization(testBean, "testBean"));
        beanPostProcessor.processAfterInitialization(testBean, "testBean");

    }

    @Test
    public void testPostProcess() {

        GenericBeanPostProcessorAdapter<Bean> beanPostProcessor = new GenericBeanPostProcessorAdapter<Bean>() {
        };

        String bean = "test";

        Assert.assertEquals("test", beanPostProcessor.postProcessBeforeInitialization(bean, ""));
        Assert.assertEquals("test", beanPostProcessor.postProcessAfterInitialization(bean, ""));
    }

    @Test
    public void testGetBeanType() {

        assertEquals(Bean.class, new GenericBeanPostProcessorAdapter<Bean>() {
        }.getBeanType());

    }

}
