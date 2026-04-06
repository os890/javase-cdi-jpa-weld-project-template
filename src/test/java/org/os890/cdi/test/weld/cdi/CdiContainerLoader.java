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

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory that discovers a {@link CdiContainer} implementation via
 * the {@link ServiceLoader} mechanism.
 *
 * <p>Exactly one implementation must be present on the classpath.
 * Multiple or zero implementations cause an {@link IllegalStateException}.</p>
 */
public final class CdiContainerLoader {

    private static CdiContainer cdiContainer;

    private CdiContainerLoader() {
        // prevent instantiation
    }

    /**
     * Returns the single {@link CdiContainer} implementation on the classpath.
     *
     * @return the discovered container implementation
     * @throws IllegalStateException if zero or more than one implementation is found
     */
    public static synchronized CdiContainer getCdiContainer() {
        if (cdiContainer == null) {
            ServiceLoader<CdiContainer> loader = ServiceLoader.load(CdiContainer.class);
            Iterator<CdiContainer> it = loader.iterator();

            if (it.hasNext()) {
                cdiContainer = it.next();
            } else {
                throw new IllegalStateException(
                    "Could not find an implementation of " + CdiContainer.class.getName()
                        + " available in the classpath!");
            }

            if (it.hasNext()) {
                String details = getContainerDetails();
                throw new IllegalStateException(
                    "Too many implementations of " + CdiContainer.class.getName()
                        + " found in the classpath! Details: " + details);
            }
        }
        return cdiContainer;
    }

    private static String getContainerDetails() {
        StringBuilder result = new StringBuilder();
        for (CdiContainer container : ServiceLoader.load(CdiContainer.class)) {
            Class<?> containerClass = container.getClass();
            result.append(containerClass.getProtectionDomain().getCodeSource().getLocation().toExternalForm());
            result.append(containerClass.getName());
            result.append(System.lineSeparator());
        }
        return result.toString();
    }
}
