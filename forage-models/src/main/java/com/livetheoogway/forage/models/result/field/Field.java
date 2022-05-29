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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "fieldType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "TEXT", value = TextField.class),
        @JsonSubTypes.Type(name = "STRING", value = StringField.class),
        @JsonSubTypes.Type(name = "INT", value = IntField.class),
        @JsonSubTypes.Type(name = "FLOAT", value = FloatField.class)
})
@Getter
public abstract class Field {
    private final FieldType fieldType;

    public abstract <T> T accept(FieldVisitor<T> fieldVisitor);
}
