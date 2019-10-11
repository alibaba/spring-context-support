package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

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
import static com.alibaba.spring.util.AnnotationUtils.getAttributes;
import static com.alibaba.spring.util.ObjectUtils.of;

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

        Method method = ReflectionUtils.findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class, String.class);

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = ReflectionUtils.findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class);

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = ReflectionUtils.findMethod(RuntimeAnnotationHandler.class, "handle");

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);

        method = ReflectionUtils.findMethod(RuntimeAnnotationHandler.class, "handle");

        AnnotationUtils.isPresent(method, RuntimeAnnotation.class);


        method = ReflectionUtils.findMethod(ClassAnnotationHandler.class, "echo",
                String.class);

        AnnotationUtils.isPresent(method, ClassAnnotation.class);

    }

    @Test
    public void testFindAnnotations() {

        Method method = ReflectionUtils.findMethod(RuntimeAnnotationHandler.class, "handle",
                String.class, String.class);

        Map<ElementType, List<RuntimeAnnotation>> annotationsMap =
                findAnnotations(method, RuntimeAnnotation.class);

        Assert.assertEquals(3, annotationsMap.size());

        List<RuntimeAnnotation> annotationsList = annotationsMap.get(ElementType.TYPE);

        Assert.assertEquals(1, annotationsList.size());

        RuntimeAnnotation runtimeAnnotation = annotationsList.get(0);

        Assert.assertEquals("type", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(ElementType.METHOD);

        Assert.assertEquals(1, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        Assert.assertEquals("method", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(ElementType.PARAMETER);

        Assert.assertEquals(2, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        Assert.assertEquals("parameter1", runtimeAnnotation.value());

        runtimeAnnotation = annotationsList.get(1);

        Assert.assertEquals("parameter2", runtimeAnnotation.value());


        annotationsList = annotationsMap.get(ElementType.PACKAGE);

        Assert.assertNull(annotationsList);


        method = ReflectionUtils.findMethod(ClassAnnotationHandler.class, "handle",
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

        Map<String, Object> attributes = getAttributes(annotation, null, true);
        Assert.assertTrue(Arrays.equals(new String[]{"dummy-bean"}, (String[]) attributes.get("name")));

        attributes = getAttributes(annotation, true);
        Assert.assertTrue(Arrays.equals(new String[]{"dummy-bean"}, (String[]) attributes.get("name")));

        attributes = getAttributes(annotation, null, false);
        Assert.assertEquals(Autowire.NO, attributes.get("autowire"));
        Assert.assertEquals("", attributes.get("initMethod"));
        Assert.assertEquals(AbstractBeanDefinition.INFER_METHOD, attributes.get("destroyMethod"));

        MockEnvironment environment = new MockEnvironment();

        attributes = getAttributes(annotation, environment, false);
        Assert.assertEquals(Autowire.NO, attributes.get("autowire"));
        Assert.assertEquals("", attributes.get("initMethod"));
        Assert.assertEquals(AbstractBeanDefinition.INFER_METHOD, attributes.get("destroyMethod"));

        annotation = getAnnotation("dummyBean2", Bean.class);

        attributes = getAttributes(annotation, null, true);
        Assert.assertTrue(attributes.isEmpty());

        attributes = getAttributes(annotation, environment, true);
        Assert.assertTrue(attributes.isEmpty());

        environment.setProperty("beanName", "Your Bean Name");

        annotation = getAnnotation("dummyBean3", Bean.class);
        attributes = getAttributes(annotation, environment, true);
        Assert.assertTrue(Arrays.deepEquals(of(environment.getProperty("beanName")), (String[]) attributes.get("name")));

    }

    private <A extends Annotation> A getAnnotation(String methodName, Class<A> annotationClass) {
        Method method = ReflectionUtils.findMethod(getClass(), methodName);
        return method.getAnnotation(annotationClass);
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
