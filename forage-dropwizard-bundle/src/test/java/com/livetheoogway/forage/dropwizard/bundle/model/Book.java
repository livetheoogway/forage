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

package com.livetheoogway.forage.dropwizard.bundle.model;

import com.google.common.collect.ImmutableList;
import com.livetheoogway.forage.models.DataId;
import com.livetheoogway.forage.models.result.field.Field;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.TextField;
import lombok.Value;

import java.util.List;

@Value
public class Book implements DataId {
    String id;
    String title;
    String author;
    float rating;
    String language;
    int numPage;

    public List<Field> fields() {
        return ImmutableList.of(new TextField("title", title),
                                new TextField("author", author),
                                new FloatField("rating", new float[]{rating}),
                                new IntField("numPage", new int[]{numPage}));
    }

    @Override
    public String id() {
        return id;
    }
}
