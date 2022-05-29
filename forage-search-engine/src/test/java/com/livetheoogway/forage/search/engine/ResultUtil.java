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

package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.search.engine.model.Book;
import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;
import java.util.function.Function;

@UtilityClass
public class ResultUtil {
    public <T, R> R getRepresentation(final ForageQueryResult<T> result,
                                      final Function<MatchingResult<T>, R> mapper,
                                      final BinaryOperator<R> reducer) {
        return result.getMatchingResults()
                .stream()
                .map(mapper)
                .reduce(reducer)
                .orElse(null);
    }

    public String getBookRepresentation(final ForageQueryResult<Book> result) {
        return getRepresentation(result,
                                 matchingResult -> {
                                     final Book data = matchingResult.getData();
                                     return matchingResult.getId() + ":" + data.getTitle();
                                 },
                                 (a, b) -> a + "\n" + b);
    }
}
