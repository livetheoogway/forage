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

package com.livetheoogway.forage.search.engine.model.opresult;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.Getter;

public class FailedResult<S> implements OperationResult<S> {
    @Getter
    private final ForageSearchError searchError;

    public FailedResult(final ForageSearchError searchError) {
        this.searchError = searchError;
    }

    @Override
    public <T> T accept(final OperationResultVisitor<S, T> visitor) {
        return visitor.visit(this);
    }
}
