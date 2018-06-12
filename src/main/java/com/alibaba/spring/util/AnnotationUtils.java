package com.alibaba.spring.util;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link Annotation} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Annotation
 * @since 2017.01.13
 */
public abstract class AnnotationUtils {


    /**
     * Is specified {@link Annotation} present on {@link Method}'s declaring class or parameters or itself.
     *
     * @param method          {@link Method}
     * @param annotationClass {@link Annotation} type
     * @param <A>             {@link Annotation} type
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static <A extends Annotation> boolean isPresent(Method method, Class<A> annotationClass) {

        Map<ElementType, List<A>> annotationsMap = findAnnotations(method, annotationClass);

        return !annotationsMap.isEmpty();

    }

    /**
     * Find specified {@link Annotation} type maps from {@link Method}
     *
     * @param method          {@link Method}
     * @param annotationClass {@link Annotation} type
     * @param <A>             {@link Annotation} type
     * @return {@link Annotation} type maps , the {@link ElementType} as key ,
     * the list of {@link Annotation} as value.
     * If {@link Annotation} was annotated on {@link Method}'s parameters{@link ElementType#PARAMETER} ,
     * the associated {@link Annotation} list may contain multiple elements.
     */
    public static <A extends Annotation> Map<ElementType, List<A>> findAnnotations(Method method,
                                                                                   Class<A> annotationClass) {

        Retention retention = annotationClass.getAnnotation(Retention.class);

        RetentionPolicy retentionPolicy = retention.value();

        if (!RetentionPolicy.RUNTIME.equals(retentionPolicy)) {
            return Collections.emptyMap();
        }

        Map<ElementType, List<A>> annotationsMap = new LinkedHashMap<ElementType, List<A>>();

        Target target = annotationClass.getAnnotation(Target.class);

        ElementType[] elementTypes = target.value();


        for (ElementType elementType : elementTypes) {

            List<A> annotationsList = new LinkedList<A>();

            switch (elementType) {

                case PARAMETER:

                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                    for (Annotation[] annotations : parameterAnnotations) {

                        for (Annotation annotation : annotations) {

                            if (annotationClass.equals(annotation.annotationType())) {

                                annotationsList.add((A) annotation);

                            }

                        }

                    }

                    break;

                case METHOD:

                    A annotation = findAnnotation(method, annotationClass);

                    if (annotation != null) {

                        annotationsList.add(annotation);

                    }

                    break;

                case TYPE:

                    Class<?> beanType = method.getDeclaringClass();

                    A annotation2 = findAnnotation(beanType, annotationClass);

                    if (annotation2 != null) {

                        annotationsList.add(annotation2);

                    }

                    break;

            }

            if (!annotationsList.isEmpty()) {

                annotationsMap.put(elementType, annotationsList);

            }


        }

        return Collections.unmodifiableMap(annotationsMap);

    }

}
