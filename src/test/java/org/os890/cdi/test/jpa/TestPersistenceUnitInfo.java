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

package org.os890.cdi.test.jpa;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static java.util.Collections.emptyList;

/**
 * Programmatic {@link PersistenceUnitInfo} for an in-memory HSQLDB test database.
 *
 * <p>Configures Hibernate with {@code create-drop} schema generation
 * so each test run starts with a clean database.</p>
 */
public class TestPersistenceUnitInfo implements PersistenceUnitInfo {

    private final Properties properties = new Properties();

    /**
     * Creates the persistence unit info with HSQLDB in-memory settings.
     */
    public TestPersistenceUnitInfo() {
        properties.put("jakarta.persistence.jdbc.driver", "org.hsqldb.jdbc.JDBCDriver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
    }

    @Override
    public String getPersistenceUnitName() {
        return "testPU";
    }

    @Override
    public String getPersistenceProviderClassName() {
        return HibernatePersistenceProvider.class.getName();
    }

    @SuppressWarnings("removal")
    @Override
    public jakarta.persistence.spi.PersistenceUnitTransactionType getTransactionType() {
        return jakarta.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getMappingFileNames() {
        return List.of("META-INF/entities.xml");
    }

    @Override
    public List<URL> getJarFileUrls() {
        return emptyList();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return emptyList();
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.UNSPECIFIED;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return "3.2";
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        // not needed for test persistence unit
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }

    @Override
    public java.util.List<String> getQualifierAnnotationNames() {
        return java.util.List.of();
    }

    @Override
    public String getScopeAnnotationName() {
        return null;
    }
}
