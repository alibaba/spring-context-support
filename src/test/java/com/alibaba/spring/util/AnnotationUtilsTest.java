package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.alibaba.spring.util.AnnotationUtils.findAnnotations;
import static com.alibaba.spring.util.AnnotationUtils.getAnnotationAttributes;
import static com.alibaba.spring.util.AnnotationUtils.getAttribute;
import static com.alibaba.spring.util.AnnotationUtils.getAttributes;
import static com.alibaba.spring.util.ObjectUtils.of;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationUtils
 * @since 2017.01.13
 */
public class AnnotationUtilsTest {

    @Bean(name = "dummy-bean")
    public String dummyBean() {
        return "Dummy Bean";
    }

    @Bean
    public String dummyBean2() {
        return "Dummy Bean 2";
    }

    @Bean(name = "${beanName}")
    public String dummyBean3() {
        return "Dummy Bean 3";
    }

    @Test
    public void testIsPresent() {

        Method method = findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class, String.class);

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class);

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = findMethod(RuntimeAnnotationHandler.class, "handle");

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = findMethod(RuntimeAnnotationHandler.class, "handle");

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);


        method = findMethod(ClassAnnotationHandler.class, "echo",
                String.class);

        AnnotationUtils.isPresent(method, ClassAnnotation.class);

    }

    @Test
    public void testFindAnnotations() {

        Method method = findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class, String.class);

        Map<ElementType, List<RuntimeAnnotation>> annotationsMap =
                findAnnotations(method, RuntimeAnnotation.class);

        assertEquals(3, annotationsMap.size());

        List<RuntimeAnnotation> annotationsList = annotationsMap.get(ElementType.TYPE);

        assertEquals(1, annotationsList.size());

        RuntimeAnnotation runtimeAnnotation = annotationsList.get(0);

        assertEquals("type", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(ElementType.METHOD);

        assertEquals(1, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        assertEquals("method", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(ElementType.PARAMETER);

        assertEquals(2, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        assertEquals("parameter1", runtimeAnnotation.value());

        runtimeAnnotation = annotationsList.get(1);

        assertEquals("parameter2", runtimeAnnotation.value());


        annotationsList = annotationsMap.get(ElementType.PACKAGE);

        Assert.assertNull(annotationsList);


        method = findMethod(ClassAnnotationHandler.class, "handle",
                String.class);

        annotationsMap = findAnnotations(method, RuntimeAnnotation.class);

        Assert.assertTrue(annotationsMap.isEmpty());

        Map<ElementType, List<ClassAnnotation>> classAnnotationsMap = findAnnotations(method,
                ClassAnnotation.class);

        Assert.assertTrue(classAnnotationsMap.isEmpty());
    }

    @Test
    public void testGetAttributes() {

        Bean annotation = getAnnotation("dummyBean", Bean.class);

        Map<String, Object> attributes = getAttributes(annotation, true);
        Assert.assertTrue(Arrays.equals(new String[]{"dummy-bean"}, (String[]) attributes.get("name")));

        attributes = getAttributes(annotation, true);
        Assert.assertTrue(Arrays.equals(new String[]{"dummy-bean"}, (String[]) attributes.get("name")));

        attributes = getAttributes(annotation, false);
        assertEquals(Autowire.NO, attributes.get("autowire"));
        assertEquals("", attributes.get("initMethod"));
        assertEquals(AbstractBeanDefinition.INFER_METHOD, attributes.get("destroyMethod"));

        MockEnvironment environment = new MockEnvironment();

        attributes = getAttributes(annotation, environment, false);
        assertEquals(Autowire.NO, attributes.get("autowire"));
        assertEquals("", attributes.get("initMethod"));
        assertEquals(AbstractBeanDefinition.INFER_METHOD, attributes.get("destroyMethod"));

        annotation = getAnnotation("dummyBean2", Bean.class);

        attributes = getAttributes(annotation, true);
        Assert.assertTrue(attributes.isEmpty());

        attributes = getAttributes(annotation, environment, true);
        Assert.assertTrue(attributes.isEmpty());

        environment.setProperty("beanName", "Your Bean Name");

        annotation = getAnnotation("dummyBean3", Bean.class);
        attributes = getAttributes(annotation, environment, true);
        Assert.assertTrue(Arrays.deepEquals(of(environment.getProperty("beanName")), (String[]) attributes.get("name")));

    }

    @Test
    public void testGetAttribute() {
        Bean annotation = getAnnotation("dummyBean", Bean.class);
        assertArrayEquals(of("dummy-bean"), (String[]) getAttribute(annotation, "name"));

        annotation = getAnnotation("dummyBean2", Bean.class);
        assertArrayEquals(of(), (String[]) getAttribute(annotation, "name"));

        annotation = getAnnotation("dummyBean3", Bean.class);
        assertArrayEquals(of("${beanName}"), (String[]) getAttribute(annotation, "name"));
    }

    @Test
    public void testGetAnnotationAttributes() {

        MockEnvironment environment = new MockEnvironment();

        Bean annotation = getAnnotation("dummyBean", Bean.class);

        // case 1 : PropertyResolver(null) , ignoreDefaultValue(true) , ignoreAttributeName(empty)
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation, true);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));

        // case 2 : PropertyResolver , ignoreDefaultValue(true) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, environment, true);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));

        // case 3 : PropertyResolver , ignoreDefaultValue(true) , ignoreAttributeName(name)
        annotationAttributes = getAnnotationAttributes(annotation, environment, true, "name");

        // case 4 : PropertyResolver(null) , ignoreDefaultValue(false) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, false);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));
        assertEquals(Autowire.NO, annotationAttributes.get("autowire"));
        assertEquals("", annotationAttributes.getString("initMethod"));
        assertEquals(AbstractBeanDefinition.INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // case 5 : PropertyResolver , ignoreDefaultValue(false) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, environment, false);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));
        assertEquals(Autowire.NO, annotationAttributes.get("autowire"));
        assertEquals("", annotationAttributes.getString("initMethod"));
        assertEquals(AbstractBeanDefinition.INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // case 6 : PropertyResolver , ignoreDefaultValue(false) , ignoreAttributeName(name,autowire,initMethod)
        annotationAttributes = getAnnotationAttributes(annotation, environment, false, "name", "autowire", "initMethod");
        assertEquals(AbstractBeanDefinition.INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // getAnnotationAttributes(AnnotatedElement, java.lang.Class, PropertyResolver, boolean, String...)
        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Configuration.class, environment, true);
        assertNull(annotationAttributes);

        // getAnnotationAttributes(AnnotatedElement, java.lang.Class, PropertyResolver, boolean, boolean, String...)
        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true, true);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true, false);
        assertArrayEquals(of("dummy-bean"), annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Configuration.class, environment, true, true);
        assertNull(annotationAttributes);
    }

    private <A extends Annotation> A getAnnotation(String methodName, Class<A> annotationClass) {
        Method method = getMethod(methodName);
        return method.getAnnotation(annotationClass);
    }

    private Method getMethod(String methodName) {
        return findMethod(getClass(), methodName);
    }

    @RuntimeAnnotation("type")
    private static class RuntimeAnnotationHandler {

        @RuntimeAnnotation("method")
        public String handle() {

            return "";
        }

        @RuntimeAnnotation("method")
        public String handle(@RuntimeAnnotation("parameter") String message) {
            return message;
        }


        @RuntimeAnnotation("method")
        public String handle(@RuntimeAnnotation("parameter1") String message,
                             @RuntimeAnnotation("parameter2") String message2) {
            return message + message2;
        }

        public void echo() {
        }


    }

    @ClassAnnotation
    private static class ClassAnnotationHandler {

        @ClassAnnotation
        public String handle(@ClassAnnotation String message) {
            return message;
        }

    }


    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface RuntimeAnnotation {

        String value();

    }

    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    private static @interface ClassAnnotation {

    }
}
