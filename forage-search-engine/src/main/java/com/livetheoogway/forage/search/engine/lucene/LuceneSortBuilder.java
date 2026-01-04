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

package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.query.SortCriteria;
import com.livetheoogway.forage.models.query.SortOrder;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.List;

public class LuceneSortBuilder {

    public static Sort buildSort(final List<SortCriteria> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Sort.RELEVANCE; // Default to relevance sorting
        }

        SortField[] sortFields = sortCriteria.stream()
                .map(LuceneSortBuilder::buildSortField)
                .toArray(SortField[]::new);

        return new Sort(sortFields);
    }

    private static SortField buildSortField(final SortCriteria criteria) {
        final boolean reverse = criteria.getOrder() == SortOrder.DESC;

        return switch (criteria.getType()) {
            case SCORE -> {
                // For SCORE type, Lucene's reverse flag has opposite semantics:
                // reverse=false → highest scores first (descending - natural order)
                // reverse=true → lowest scores first (ascending)
                // So for DESC (highest first), we need reverse=false
                // For ASC (lowest first), we need reverse=true
                boolean scoreReverse = criteria.getOrder() == SortOrder.ASC;
                yield new SortField(null, SortField.Type.SCORE, scoreReverse);
            }
            case FIELD ->
                // For field sorting, we need to determine the field type
                // Default to STRING sorting, but this could be enhanced to detect field types
                    new SortField(criteria.getField(), SortField.Type.STRING, reverse);
        };
    }
}