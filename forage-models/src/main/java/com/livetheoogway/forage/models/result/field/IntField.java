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

import java.util.Arrays;

@Value
@EqualsAndHashCode(callSuper = true)
public class IntField extends Field {
    String name;
    int[] points;

    public IntField(final String name, final int[] points) {
        super(FieldType.INT);
        this.name = name;
        this.points = points;
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("INT[").append(name)
                .append(":").append(Arrays.toString(points)).append("]").toString();
    }
}
