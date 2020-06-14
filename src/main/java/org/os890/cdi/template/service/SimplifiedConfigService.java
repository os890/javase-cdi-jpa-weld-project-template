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
package org.os890.cdi.template.service;

import org.os890.cdi.template.persistence.ConfigEntry;
import org.os890.cdi.template.persistence.ConfigRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;

@ApplicationScoped
public class SimplifiedConfigService implements ConfigService {
    @Inject
    private ConfigRepository configRepository;

    @Inject
    private EntityManager entityManager;

    @Override
    public ConfigEntry load(String id) {
        return configRepository.load(id);
    }

    @Override
    public Collection<ConfigEntry> findAll() {
        return configRepository.findAll();
    }

    @Override
    public ConfigEntry save(ConfigEntry newConfigEntry) {
        entityManager.getTransaction().begin();

        try {
            ConfigEntry result = configRepository.save(newConfigEntry);
            entityManager.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
}
