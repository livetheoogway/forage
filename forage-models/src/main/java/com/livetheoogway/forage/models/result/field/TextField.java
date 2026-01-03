/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.models.result.field;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Use this for indexing fields for full text search
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class TextField extends Field {
    String name;
    String value;

    public TextField(String name, String value) {
        super(FieldType.TEXT);
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("TEXT[").append(name).append(":").append(value).append("]").toString();
    }
}
