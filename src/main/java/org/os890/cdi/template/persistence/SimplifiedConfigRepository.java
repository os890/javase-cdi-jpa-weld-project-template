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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Application-scoped JPA implementation of {@link ConfigRepository}.
 *
 * <p>Delegates all persistence operations to the injected {@link EntityManager}.
 * New entities are persisted; detached entities are merged.</p>
 */
@ApplicationScoped
public class SimplifiedConfigRepository implements ConfigRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    public List<ConfigEntry> findAll() {
        return entityManager.createQuery("select e from ConfigEntry e", ConfigEntry.class).getResultList();
    }

    @Override
    public ConfigEntry load(String id) {
        return entityManager.find(ConfigEntry.class, id);
    }

    @Override
    public ConfigEntry save(ConfigEntry newConfigEntry) {
        if (newConfigEntry.isTransient()) {
            entityManager.persist(newConfigEntry);
            return newConfigEntry;
        }
        return entityManager.merge(newConfigEntry);
    }
}
