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

import java.lang.annotation.Annotation;

/**
 * Manual control over built-in CDI context lifecycles.
 *
 * <p>Context operations are thread-scoped: each thread receives its own
 * storage for session and request contexts. Callers must stop every
 * started context (typically in a {@code finally} block).</p>
 */
public interface ContextControl {

    /**
     * Starts all built-in CDI contexts (application, session, request, conversation).
     */
    void startContexts();

    /**
     * Stops all built-in CDI contexts and destroys their beans.
     */
    void stopContexts();

    /**
     * Starts the context identified by the given scope annotation.
     *
     * @param scopeClass the scope annotation, e.g. {@code RequestScoped.class}
     */
    void startContext(Class<? extends Annotation> scopeClass);

    /**
     * Stops the context identified by the given scope annotation.
     *
     * @param scopeClass the scope annotation, e.g. {@code RequestScoped.class}
     */
    void stopContext(Class<? extends Annotation> scopeClass);
}
