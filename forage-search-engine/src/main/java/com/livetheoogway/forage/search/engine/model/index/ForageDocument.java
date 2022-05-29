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

package com.livetheoogway.forage.search.engine.model.index;

import com.livetheoogway.forage.models.result.field.Field;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForageDocument extends IndexableDocument {

    @NotNull
    @NotEmpty
    List<Field> fields;

    @Builder
    public ForageDocument(final String id, @Singular final List<Field> fields) {
        super(DocumentType.FORAGE, id);
        this.fields = fields;
    }

    @Override
    public <T> T accept(final DocumentVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
