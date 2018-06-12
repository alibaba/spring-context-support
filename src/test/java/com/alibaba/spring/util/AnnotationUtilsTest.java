package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationUtils
 * @since 2017.01.13
 */
public class AnnotationUtilsTest {

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
                AnnotationUtils.findAnnotations(method, RuntimeAnnotation.class);

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

        annotationsMap =
                AnnotationUtils.findAnnotations(method, RuntimeAnnotation.class);

        Assert.assertTrue(annotationsMap.isEmpty());

        Map<ElementType, List<ClassAnnotation>> classAnnotationsMap = AnnotationUtils.findAnnotations(method,
                ClassAnnotation.class);

        Assert.assertTrue(classAnnotationsMap.isEmpty());
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
