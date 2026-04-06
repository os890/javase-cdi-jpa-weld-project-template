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

package org.os890.cdi.template;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * CDI producer for JPA {@link EntityManager} instances.
 *
 * <p>Manages the lifecycle of request-scoped entity managers by producing
 * them from an injected {@link EntityManagerFactory} and closing them
 * when the request scope ends.</p>
 */
@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    /**
     * Produces a request-scoped {@link EntityManager} from the injected factory.
     *
     * @return a new entity manager bound to the current request scope
     */
    @Produces
    @Default
    @RequestScoped
    protected EntityManager exposeEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * Disposer method that closes the entity manager when the request scope ends.
     *
     * @param entityManager the entity manager to close
     */
    protected void onTransactionEnd(@Disposes @Default EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
