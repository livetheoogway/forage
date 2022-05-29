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

package com.livetheoogway.forage.models.query;

import com.livetheoogway.forage.models.query.search.Query;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.Max;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ForageSearchQuery extends ForageQuery {
    Query query;

    @Max(1024)
    int size;

    public ForageSearchQuery(final Query query, final int size) {
        super(ForageQueryType.FORAGE_SEARCH);
        this.query = query;
        this.size = size;
    }

    @Override
    public <T> T accept(final ForageQueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
