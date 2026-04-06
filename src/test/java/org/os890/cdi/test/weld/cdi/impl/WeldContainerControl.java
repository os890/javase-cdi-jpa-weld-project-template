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

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.os890.cdi.test.weld.cdi.CdiContainer;
import org.os890.cdi.test.weld.cdi.ContextControl;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Weld SE implementation of {@link CdiContainer}.
 *
 * <p>Manages the lifecycle of a Weld SE container including
 * startup, shutdown, and context control resolution.</p>
 */
public class WeldContainerControl implements CdiContainer {

    private static final Logger LOG = Logger.getLogger(WeldContainerControl.class.getName());

    private Weld weld;
    private WeldContainer weldContainer;

    private Bean<ContextControl> ctxCtrlBean;
    private CreationalContext<ContextControl> ctxCtrlCreationalContext;
    private ContextControl ctxCtrl;

    @Override
    public BeanManager getBeanManager() {
        if (weldContainer == null) {
            return null;
        }
        return weldContainer.getBeanManager();
    }

    @Override
    public synchronized void boot() {
        weld = new Weld();
        weldContainer = weld.initialize();
    }

    @Override
    public void bootFor(Class<?>... beanClasses) {
        weld = new Weld();
        weldContainer = weld.beanClasses(beanClasses).disableDiscovery().initialize();
    }

    @Override
    public void boot(Map<?, ?> properties) {
        boot();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void shutdown() {
        if (ctxCtrl != null) {
            try {
                ctxCtrl.stopContext(ConversationScoped.class);
                ctxCtrl.stopContext(RequestScoped.class);
                ctxCtrl.stopContext(SessionScoped.class);
                ctxCtrlBean.destroy(ctxCtrl, ctxCtrlCreationalContext);
            } catch (Exception e) {
                // contexts likely already stopped
            }
        }
        try {
            weld.shutdown();
        } catch (Exception e) {
            // container may already be shut down
        }
        weld = null;
        ctxCtrl = null;
        ctxCtrlBean = null;
        ctxCtrlCreationalContext = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized ContextControl getContextControl() {
        if (ctxCtrl == null) {
            BeanManager beanManager = getBeanManager();

            if (beanManager == null) {
                LOG.warning("Container not started. Call CdiContainer#boot first.");
                return null;
            }
            Set<Bean<?>> beans = beanManager.getBeans(ContextControl.class);
            ctxCtrlBean = (Bean<ContextControl>) beanManager.resolve(beans);
            ctxCtrlCreationalContext = getBeanManager().createCreationalContext(ctxCtrlBean);
            ctxCtrl = (ContextControl)
                getBeanManager().getReference(ctxCtrlBean, ContextControl.class, ctxCtrlCreationalContext);
        }
        return ctxCtrl;
    }

    @Override
    public String toString() {
        return "WeldContainerControl [Weld SE]";
    }
}
