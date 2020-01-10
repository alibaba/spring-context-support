package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link AnnotatedBeanDefinitionRegistryUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanDefinitionRegistryUtils
 * @since 2017.01.13
 */
public class AnnotatedBeanDefinitionRegistryUtilsTest {

    private DefaultListableBeanFactory registry = null;

    @Before
    public void init() {
        registry = new DefaultListableBeanFactory();
        registry.setAllowBeanDefinitionOverriding(false);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);
    }

    @Test
    public void testRegisterBeans() {

        for(int i=0;i<100;i++) {
            AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, this.getClass());
        }

        String[] beanNames = registry.getBeanNamesForType(this.getClass());

        Assert.assertEquals(1, beanNames.length);

        beanNames = registry.getBeanNamesForType(AnnotatedBeanDefinitionRegistryUtils.class);

        Assert.assertTrue(ObjectUtils.isEmpty(beanNames));

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry);

    }

    @Test
    public void testScanBasePackages() {

        int count = AnnotatedBeanDefinitionRegistryUtils.scanBasePackages(registry, getClass().getPackage().getName());

        Assert.assertEquals(5, count);

        String[] beanNames = registry.getBeanNamesForType(TestBean.class);

        Assert.assertEquals(1, beanNames.length);

        beanNames = registry.getBeanNamesForType(TestBean2.class);

        Assert.assertEquals(1, beanNames.length);

        count = AnnotatedBeanDefinitionRegistryUtils.scanBasePackages(registry);

        Assert.assertEquals(0, count);
    }

}
