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

package org.os890.cdi.test.weld.junit;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.os890.cdi.test.weld.cdi.CdiContainer;
import org.os890.cdi.test.weld.cdi.CdiContainerLoader;
import org.os890.cdi.test.weld.cdi.impl.WeldContextControl;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;

/**
 * JUnit Jupiter extension that boots a Weld SE CDI container for the test class,
 * manages request-scoped context per test method, and injects CDI beans into test instances.
 *
 * <p>Usage: annotate the test class with {@code @ExtendWith(WeldExtension.class)}
 * and declare bean classes via {@link CdiBeanClass} annotations (directly or through
 * meta-annotations).</p>
 *
 * <p>The container is started once before all tests and shut down after all tests.
 * A fresh request scope is started before each test method and stopped afterwards.</p>
 */
public class WeldExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NS =
        ExtensionContext.Namespace.create(WeldExtension.class);

    private static final String CONTAINER_KEY = "cdiContainer";

    @Override
    public void beforeAll(ExtensionContext context) {
        CdiContainer container = CdiContainerLoader.getCdiContainer();

        List<CdiBeanClass> beanClassAnnotations = findAnnotations(context.getRequiredTestClass(), CdiBeanClass.class);

        Set<Class<?>> beanClasses = new HashSet<>();
        beanClasses.add(WeldContextControl.class);
        beanClasses.addAll(
            beanClassAnnotations.stream()
                .flatMap(a -> Stream.of(a.value()))
                .collect(toSet())
        );

        container.bootFor(beanClasses.toArray(new Class<?>[0]));
        context.getStore(NS).put(CONTAINER_KEY, container);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        CdiContainer container = context.getStore(NS).get(CONTAINER_KEY, CdiContainer.class);
        if (container != null) {
            container.shutdown();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        CdiContainer container = context.getStore(NS).get(CONTAINER_KEY, CdiContainer.class);
        container.getContextControl().startContext(RequestScoped.class);
        context.getRequiredTestInstances().getAllInstances()
            .forEach(instance -> injectFields(container.getBeanManager(), instance));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        CdiContainer container = context.getStore(NS).get(CONTAINER_KEY, CdiContainer.class);
        container.getContextControl().stopContext(RequestScoped.class);
    }

    /**
     * Looks up a CDI bean by type from the current container.
     *
     * @param <T>        the bean type
     * @param type       the bean class
     * @param optional   if {@code true}, returns {@code null} when no bean is found
     * @param qualifiers optional qualifier annotations
     * @return the contextual reference, or {@code null} if optional and not found
     * @throws IllegalStateException if not optional and no matching bean exists
     */
    public static <T> T getContextualReference(Class<T> type,
                                               boolean optional,
                                               Annotation... qualifiers) {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        BeanManager beanManager = container.getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty()) {
            if (optional) {
                return null;
            }
            throw new IllegalStateException(
                "Could not find beans for Type=" + type
                    + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        return getContextualReference(type, beanManager, beans);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getContextualReference(Class<T> type,
                                                BeanManager beanManager,
                                                Set<Bean<?>> beans) {
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, type, creationalContext);
    }

    private static <T extends Annotation> List<T> findAnnotations(Class<?> testClass,
                                                                   Class<? extends T> annotationClass) {
        List<T> result = new ArrayList<>();

        T[] annotations = testClass.getAnnotationsByType(annotationClass);
        if (annotations.length > 0) {
            result.addAll(Arrays.asList(annotations));
        }

        for (Annotation annotation : toMetaAnnotationList(testClass)) {
            annotations = annotation.annotationType().getAnnotationsByType(annotationClass);
            if (annotations.length > 0) {
                result.addAll(Arrays.asList(annotations));
            }
        }

        return result;
    }

    private static List<Annotation> toMetaAnnotationList(Class<?> testClass) {
        return Arrays.stream(testClass.getAnnotations())
            .flatMap(a -> {
                if (a.annotationType().isAnnotationPresent(Stereotype.class)) {
                    return Stream.of(a.annotationType().getAnnotations());
                } else {
                    return Stream.of(a);
                }
            })
            .toList();
    }

    @SuppressWarnings("unchecked")
    private static <T> void injectFields(BeanManager beanManager, T instance) {
        if (instance != null) {
            CreationalContext<T> creationalContext =
                beanManager.createCreationalContext((Contextual<T>) null);
            AnnotatedType<T> annotatedType =
                (AnnotatedType<T>) beanManager.createAnnotatedType(instance.getClass());
            InjectionTarget<T> injectionTarget =
                beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
            injectionTarget.inject(instance, creationalContext);
        }
    }

    /**
     * Repeatable annotation that declares CDI bean classes to register in the test container.
     *
     * <p>Can be placed directly on a test class or on a meta-annotation.</p>
     */
    @Target(TYPE)
    @Retention(RUNTIME)
    @Documented
    @Repeatable(CdiBeanClass.List.class)
    public @interface CdiBeanClass {

        /**
         * The bean classes to register.
         *
         * @return array of classes
         */
        Class<?>[] value();

        /**
         * Container annotation for repeatable {@link CdiBeanClass}.
         */
        @Documented
        @Target(TYPE)
        @Retention(RUNTIME)
        @interface List {

            /**
             * The repeated bean class declarations.
             *
             * @return array of CdiBeanClass annotations
             */
            CdiBeanClass[] value();
        }
    }
}
