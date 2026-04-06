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

package org.os890.cdi.template.service;

import org.os890.cdi.template.persistence.ConfigEntry;

import java.util.Collection;

/**
 * Service contract for managing {@link ConfigEntry} configuration data.
 *
 * <p>Implementations handle transaction boundaries and delegate
 * persistence operations to the underlying repository.</p>
 */
public interface ConfigService {

    /**
     * Returns all stored configuration entries.
     *
     * @return a collection of all entries
     */
    Collection<ConfigEntry> findAll();

    /**
     * Loads a single configuration entry by its identifier.
     *
     * @param id the entity identifier
     * @return the matching entry, or {@code null} if not found
     */
    ConfigEntry load(String id);

    /**
     * Saves a configuration entry within a transaction.
     *
     * @param newConfigEntry the entry to persist or merge
     * @return the managed entity instance
     */
    ConfigEntry save(ConfigEntry newConfigEntry);
}
