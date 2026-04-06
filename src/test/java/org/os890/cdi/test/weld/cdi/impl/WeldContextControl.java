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

package org.os890.cdi.test.weld.cdi.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.os890.cdi.test.weld.cdi.ContextControl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Weld-specific implementation of {@link ContextControl} that manages
 * built-in CDI context lifecycles using Weld's bound context APIs.
 *
 * <p>Each thread gets its own request and session storage via {@link ThreadLocal}.</p>
 */
@Dependent
public class WeldContextControl implements ContextControl {

    private static final ThreadLocal<RequestContextHolder> REQUEST_CONTEXTS = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> SESSION_MAPS = new ThreadLocal<>();

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private BoundSessionContext sessionContext;

    @Inject
    private Instance<BoundRequestContext> requestContextFactory;

    @Inject
    private BoundConversationContext conversationContext;

    @Override
    public void startContexts() {
        startApplicationScope();
        startSessionScope();
        startRequestScope();
        startConversationScope(null);
    }

    @Override
    public void startContext(Class<? extends Annotation> scopeClass) {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class)) {
            startApplicationScope();
        } else if (scopeClass.isAssignableFrom(SessionScoped.class)) {
            startSessionScope();
        } else if (scopeClass.isAssignableFrom(RequestScoped.class)) {
            startRequestScope();
        } else if (scopeClass.isAssignableFrom(ConversationScoped.class)) {
            startConversationScope(null);
        }
    }

    @Override
    public void stopContexts() {
        stopConversationScope();
        stopRequestScope();
        stopSessionScope();
        stopApplicationScope();
    }

    @Override
    public void stopContext(Class<? extends Annotation> scopeClass) {
        if (scopeClass.isAssignableFrom(ApplicationScoped.class)) {
            stopApplicationScope();
        } else if (scopeClass.isAssignableFrom(SessionScoped.class)) {
            stopSessionScope();
        } else if (scopeClass.isAssignableFrom(RequestScoped.class)) {
            stopRequestScope();
        } else if (scopeClass.isAssignableFrom(ConversationScoped.class)) {
            stopConversationScope();
        }
    }

    private void startApplicationScope() {
        // No-op: Weld application context is always active
    }

    private void stopApplicationScope() {
        if (applicationContext.isActive()) {
            applicationContext.invalidate();
        }
    }

    void startRequestScope() {
        RequestContextHolder holder = REQUEST_CONTEXTS.get();
        if (holder == null) {
            holder = new RequestContextHolder(requestContextFactory.get(), new HashMap<>());
            REQUEST_CONTEXTS.set(holder);
            holder.boundRequestContext().associate(holder.requestMap());
            holder.boundRequestContext().activate();
        }
    }

    void stopRequestScope() {
        RequestContextHolder holder = REQUEST_CONTEXTS.get();
        if (holder != null && holder.boundRequestContext().isActive()) {
            holder.boundRequestContext().invalidate();
            holder.boundRequestContext().deactivate();
            holder.boundRequestContext().dissociate(holder.requestMap());
            REQUEST_CONTEXTS.set(null);
            REQUEST_CONTEXTS.remove();
        }
    }

    private void startSessionScope() {
        Map<String, Object> sessionMap = SESSION_MAPS.get();
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
            SESSION_MAPS.set(sessionMap);
        }
        sessionContext.associate(sessionMap);
        sessionContext.activate();
    }

    private void stopSessionScope() {
        if (sessionContext.isActive()) {
            sessionContext.invalidate();
            sessionContext.deactivate();
            sessionContext.dissociate(SESSION_MAPS.get());
            SESSION_MAPS.set(null);
            SESSION_MAPS.remove();
        }
    }

    void startConversationScope(String cid) {
        RequestContextHolder holder = REQUEST_CONTEXTS.get();
        if (holder == null) {
            startRequestScope();
            holder = REQUEST_CONTEXTS.get();
        }
        conversationContext.associate(new MutableBoundRequest(holder.requestMap(), SESSION_MAPS.get()));
        conversationContext.activate(cid);
    }

    void stopConversationScope() {
        RequestContextHolder holder = REQUEST_CONTEXTS.get();
        if (holder == null) {
            startRequestScope();
            holder = REQUEST_CONTEXTS.get();
        }
        if (conversationContext.isActive()) {
            conversationContext.invalidate();
            conversationContext.deactivate();
            conversationContext.dissociate(new MutableBoundRequest(holder.requestMap(), SESSION_MAPS.get()));
        }
    }

    private record RequestContextHolder(BoundRequestContext boundRequestContext, Map<String, Object> requestMap) {
    }
}
