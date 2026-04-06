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

package org.os890.cdi.test.weld.cdi;

import jakarta.enterprise.inject.spi.BeanManager;

import java.util.Map;

/**
 * Portable CDI container control SPI for Java SE environments.
 *
 * <p>Provides methods to boot, configure, and shut down a CDI container
 * as well as access the {@link BeanManager} and {@link ContextControl}.</p>
 *
 * <p><b>Note:</b> booting the container does <i>not</i> automatically
 * start all CDI contexts. Use {@link #getContextControl()} to manage
 * context lifecycles explicitly.</p>
 */
public interface CdiContainer {

    /**
     * Boots the container by scanning the classpath for beans and extensions.
     */
    void boot();

    /**
     * Boots the container with only the specified bean classes, disabling discovery.
     *
     * @param beanClasses the classes to register as CDI beans
     */
    void bootFor(Class<?>... beanClasses);

    /**
     * Boots the container with additional configuration properties.
     *
     * <p>Property keys and semantics are container-implementation dependent.</p>
     *
     * @param properties container-specific configuration
     */
    void boot(Map<?, ?> properties);

    /**
     * Shuts down the container and stops all active contexts.
     */
    void shutdown();

    /**
     * Returns the {@link BeanManager} for the running container.
     *
     * @return the bean manager, or {@code null} if the container has not been started
     */
    BeanManager getBeanManager();

    /**
     * Returns a {@link ContextControl} for managing built-in CDI contexts.
     *
     * @return the context control, or {@code null} if the container has not been started
     */
    ContextControl getContextControl();
}
