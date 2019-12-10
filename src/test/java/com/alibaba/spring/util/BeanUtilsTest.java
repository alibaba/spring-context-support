package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link BeanUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanUtils
 * @since 2017.01.13
 */
@SuppressWarnings("unchecked")
public class BeanUtilsTest {


    @Configuration
    public static class Config {

        @Bean(name = "testString")
        public String testString(Environment environment) {
            return "test";
        }

    }

    @Test
    public void testResolveBeanType() {

        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

        Class<?> beanType = BeanUtils.resolveBeanType(this.getClass().getName(), classLoader);

        Assert.assertEquals(beanType, this.getClass());

        beanType = BeanUtils.resolveBeanType("", classLoader);

        Assert.assertNull(beanType);

        beanType = BeanUtils.resolveBeanType("     ", classLoader);

        Assert.assertNull(beanType);

        beanType = BeanUtils.resolveBeanType("java.lang.Abc", classLoader);

        Assert.assertNull(beanType);

    }

    @Test
    public void testGetBeanNamesOnAnnotationBean() {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(applicationContext, Config.class);

        applicationContext.refresh();

        String[] beanNames = BeanUtils.getBeanNames(applicationContext, String.class);

        Assert.assertTrue(Arrays.asList(beanNames).contains("testString"));


    }

//    @Test
//    public void testGetBeanNamesOnXmlBean() {
//
//        ClassPathXmlApplicationContext context =
//                new ClassPathXmlApplicationContext(new String[]{"spring-context.xml"}, false);
//
//        context.refresh();
//
//        String[] beanNames = BeanUtils.getBeanNames(context, User.class);
//
//        Assert.assertTrue(Arrays.asList(beanNames).contains("user"));
//
//    }

    @Test
    public void testGetBeanNames() {

        DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.setParentBeanFactory(parentBeanFactory);

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(parentBeanFactory, TestBean.class);

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(beanFactory, TestBean2.class);

        ListableBeanFactory listableBeanFactory = parentBeanFactory;

        String[] beanNames = BeanUtils.getBeanNames(listableBeanFactory, TestBean.class);

        Assert.assertEquals(1, beanNames.length);

        String beanName = beanNames[0];

        Assert.assertEquals("testBean", beanName);

        beanNames = BeanUtils.getBeanNames(listableBeanFactory, TestBean.class, true);

        Assert.assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean", beanName);

        listableBeanFactory = beanFactory;

        beanNames = BeanUtils.getBeanNames(listableBeanFactory, TestBean.class);

        Assert.assertEquals(0, beanNames.length);

        beanNames = BeanUtils.getBeanNames(listableBeanFactory, TestBean.class, true);

        Assert.assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean", beanName);

        beanNames = BeanUtils.getBeanNames(listableBeanFactory, TestBean2.class, true);

        Assert.assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean2", beanName);

        beanNames = BeanUtils.getBeanNames(listableBeanFactory, com.alibaba.spring.util.Bean.class, true);

        Assert.assertEquals(2, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean2", beanName);

        beanName = beanNames[1];

        Assert.assertEquals("testBean", beanName);


        beanNames = BeanUtils.getBeanNames(beanFactory, com.alibaba.spring.util.Bean.class, true);

        Assert.assertEquals(2, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean2", beanName);

        beanName = beanNames[1];

        Assert.assertEquals("testBean", beanName);

        beanNames = BeanUtils.getBeanNames(beanFactory, com.alibaba.spring.util.Bean.class);

        Assert.assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        Assert.assertEquals("testBean2", beanName);


    }


    @Test
    public void testIsBeanPresent() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        Assert.assertFalse(BeanUtils.isBeanPresent(registry, TestBean.class.getName(), true));
        Assert.assertFalse(BeanUtils.isBeanPresent(registry, TestBean.class.getName()));

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, TestBean.class, TestBean2.class);


        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean.class.getName(), true));
        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean.class.getName()));

        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean.class, true));
        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean.class));

        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean2.class.getName(), true));
        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean2.class.getName()));

        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean2.class, true));
        Assert.assertTrue(BeanUtils.isBeanPresent(registry, TestBean2.class));

        Assert.assertFalse(BeanUtils.isBeanPresent(registry, BeanUtils.class.getName(), true));
        Assert.assertFalse(BeanUtils.isBeanPresent(registry, BeanUtils.class.getName()));

        Assert.assertFalse(BeanUtils.isBeanPresent(registry, BeanUtils.class, true));
        Assert.assertFalse(BeanUtils.isBeanPresent(registry, BeanUtils.class));

    }

    @Test
    public void testGetOptionalBean() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        TestBean testBean = BeanUtils.getOptionalBean(registry, TestBean.class, true);

        Assert.assertNull(testBean);

        testBean = BeanUtils.getOptionalBean(registry, TestBean.class);

        Assert.assertNull(testBean);

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, TestBean.class);

        testBean = BeanUtils.getOptionalBean(registry, TestBean.class);

        Assert.assertNotNull(testBean);

    }

    @Test
    public void testGetOptionalBeanExcludingAncestors() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, TestBean.class, TestBean2.class);

        List<com.alibaba.spring.util.Bean> beans = BeanUtils.getSortedBeans(registry, com.alibaba.spring.util.Bean.class);

        Assert.assertEquals(2, beans.size());

        TestBean testBean = BeanUtils.getOptionalBean(registry, TestBean.class);

        Assert.assertEquals(testBean, beans.get(0));

        TestBean2 testBean2 = BeanUtils.getOptionalBean(registry, TestBean2.class);

        Assert.assertEquals(testBean2, beans.get(1));

    }

    @Test
    public void testSort() {

        int times = 9;

        Map<String, OrderedBean> orderedBeansMap = new LinkedHashMap<String, OrderedBean>(times);

        for (int i = times; i > 0; i--) {
            OrderedBean orderedBean = new OrderedBean(i);
            orderedBeansMap.put(orderedBean.toString(), orderedBean);
        }

        Map<String, OrderedBean> expectedBeansMap = new LinkedHashMap<String, OrderedBean>(times);

        for (int i = 1; i <= times; i++) {
            OrderedBean orderedBean = new OrderedBean(i);
            expectedBeansMap.put(orderedBean.toString(), orderedBean);
        }

        Map<String, OrderedBean> sortedBeansMap = BeanUtils.sort(orderedBeansMap);

        Assert.assertArrayEquals(expectedBeansMap.values().toArray(), sortedBeansMap.values().toArray());

    }


    private static class OrderedBean implements Ordered {

        private final int order;

        private OrderedBean(int order) {
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public String toString() {
            return "Bean #" + order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OrderedBean that = (OrderedBean) o;

            return order == that.order;
        }

        @Override
        public int hashCode() {
            return order;
        }
    }

    @Test
    public void testNamingBean() {

        BeanUtils.NamingBean namingBean = new BeanUtils.NamingBean("testBean", new TestBean());

        BeanUtils.NamingBean namingBean2 = new BeanUtils.NamingBean("testBean2", new TestBean2());

        List<BeanUtils.NamingBean> namingBeans = Arrays.asList(namingBean, namingBean2);

        AnnotationAwareOrderComparator.sort(namingBeans);

        Assert.assertEquals(1, namingBean.getOrder());
        Assert.assertEquals(2, namingBean2.getOrder());

        Assert.assertEquals(-1, namingBean.compareTo(namingBean2));


    }

}
