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
package org.os890.cdi.test;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.os890.cdi.template.persistence.ConfigEntry;
import org.os890.cdi.template.service.ConfigService;
import org.os890.cdi.test.mock.TestConfigService;
import org.os890.cdi.test.weld.junit.WeldRule;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.junit.Assert.*;
import static org.os890.cdi.test.weld.junit.WeldRule.getContextualReference;

@SimpleTestBeanClasses
public class SimpleTest {
    @Rule
    public WeldRule weldRule = WeldRule.run(this);

    @Inject
    private ConfigService configService;

    @Inject
    private EntityManager entityManager;

    @AfterClass
    public static void onShutdown() {
        WeldRule.reset();
    }

    @Test
    public void configRepository() {
        assertTrue(configService.findAll().isEmpty());

        ConfigEntry savedEntry = configService.save(new ConfigEntry("test", "demo"));
        entityManager.detach(savedEntry);

        assertFalse(configService.findAll().isEmpty());

        TestConfigService testConfigService = getContextualReference(TestConfigService.class, false);
        assertNotNull(testConfigService);
        assertFalse(testConfigService.isLoadCalled());

        assertNotNull(configService.load(savedEntry.getId()));
        assertTrue(testConfigService.isLoadCalled());
    }
}
