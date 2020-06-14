/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.os890.cdi.test.weld.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.os890.cdi.test.weld.cdi.CdiContainer;
import org.os890.cdi.test.weld.cdi.CdiContainerLoader;
import org.os890.cdi.test.weld.cdi.impl.WeldContextControl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.*;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class WeldRule implements TestRule {
    private static final ThreadLocal<CdiContainer> CONTAINER_THREAD_LOCAL = new ThreadLocal<>();

    private final Object testInstance;

    private WeldRule(Object testInstance) {
        this.testInstance = testInstance;
    }

    public static WeldRule run(Object testInstance) {
        WeldRule rule = new WeldRule(testInstance);

        CdiContainer container = CONTAINER_THREAD_LOCAL.get();
        if (container == null) {
            container = CdiContainerLoader.getCdiContainer();


            CONTAINER_THREAD_LOCAL.set(container);

            List<CdiBeanClass> beanClassesAnnotations = findAnnotations(testInstance, CdiBeanClass.class);

            Set<Class> beanClasses = new HashSet<>();
            beanClasses.add(WeldContextControl.class);
            beanClasses.addAll(beanClassesAnnotations.stream().flatMap(e -> Stream.of(e.value())).collect(toSet()));
            container.bootFor(beanClasses.toArray(new Class[0]));
        }

        return rule;
    }

    public static void reset() {
        CONTAINER_THREAD_LOCAL.get().shutdown();
        CONTAINER_THREAD_LOCAL.remove();
        CONTAINER_THREAD_LOCAL.set(null);
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    statement.evaluate();
                } finally {
                    after();
                }
            }
        };
    }


    public static <T> T getContextualReference(Class<T> type,
                                               boolean optional,
                                               Annotation... qualifiers) {
        BeanManager beanManager = CONTAINER_THREAD_LOCAL.get().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty()) {
            if (optional) {
                return null;
            }

            throw new IllegalStateException("Could not find beans for Type=" + type
                + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        return getContextualReference(type, beanManager, beans);
    }

    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans) {
        Bean<?> bean = beanManager.resolve(beans);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        T result = (T) beanManager.getReference(bean, type, creationalContext);
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Annotation> List<T> findAnnotations(Object testInstance, Class<? extends T> annotationClass) {
        List<T> result = new ArrayList<>();

        T[] annotations = testInstance.getClass().getAnnotationsByType(annotationClass);

        if (annotations.length > 0) {
            result.addAll(asList(annotations));
        }

        for (Annotation annotation : toMetaAnnotationList(testInstance)) {
            annotations = annotation.annotationType().getAnnotationsByType(annotationClass);

            if (annotations.length > 0) {
                result.addAll(asList(annotations));
            }
        }

        return result;
    }

    private static List<Annotation> toMetaAnnotationList(Object testInstance) {
        return Arrays.stream(testInstance.getClass().getAnnotations()).flatMap(a -> {
            if (a.annotationType().isAnnotationPresent(Stereotype.class)) {
                return Stream.of(a.annotationType().getAnnotations());
            } else {
                return Stream.of(a);
            }
        }).collect(toList());
    }

    private void before() {
        CONTAINER_THREAD_LOCAL.get().getContextControl().startContext(RequestScoped.class);
        injectFields(CONTAINER_THREAD_LOCAL.get().getBeanManager(), testInstance);
    }

    private void after() {
        CONTAINER_THREAD_LOCAL.get().getContextControl().stopContext(RequestScoped.class);
    }

    private static <T> T injectFields(BeanManager beanManager, T instance) {
        if (instance == null) {
            return null;
        } else {
            CreationalContext<T> creationalContext = beanManager.createCreationalContext((Contextual) null);
            AnnotatedType<T> annotatedType = (AnnotatedType<T>) beanManager.createAnnotatedType(instance.getClass());
            InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);
            injectionTarget.inject(instance, creationalContext);
            return instance;
        }
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @Documented
    @Repeatable(WeldRule.CdiBeanClass.List.class)
    public @interface CdiBeanClass {
        Class[] value();

        @Documented
        @Target(TYPE)
        @Retention(RUNTIME)
        @interface List {
            CdiBeanClass[] value();
        }
    }
}
