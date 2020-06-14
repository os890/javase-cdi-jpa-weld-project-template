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
package org.os890.cdi.template.persistence;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class ConfigEntry implements Serializable {
    private static final long serialVersionUID = 2764878761692675990L;

    @Id
    protected String id;

    @Version
    protected Long version;

    @Column(unique = true, nullable = false)
    private String entryKey;

    @Column
    private String value;

    protected ConfigEntry() {
    }

    public ConfigEntry(String entryKey, String value) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.entryKey = entryKey;
        this.value = value;
    }

    @Transient
    public boolean isTransient() {
        return version == null;
    }

    /*
     * generated
     */

    public String getId() {
        return id;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConfigEntry{ entryKey='" + entryKey + "\', value='" + value + "\'}";
    }
}
