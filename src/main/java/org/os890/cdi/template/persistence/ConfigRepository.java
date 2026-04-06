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

package org.os890.cdi.template.persistence;

import java.util.List;

/**
 * Data access contract for {@link ConfigEntry} entities.
 */
public interface ConfigRepository {

    /**
     * Returns all configuration entries.
     *
     * @return an unmodifiable list of all persisted entries
     */
    List<ConfigEntry> findAll();

    /**
     * Loads a single configuration entry by its identifier.
     *
     * @param id the entity identifier
     * @return the matching entry, or {@code null} if not found
     */
    ConfigEntry load(String id);

    /**
     * Persists a new entry or merges an existing one.
     *
     * @param newConfigEntry the entry to save
     * @return the managed entity instance
     */
    ConfigEntry save(ConfigEntry newConfigEntry);
}
