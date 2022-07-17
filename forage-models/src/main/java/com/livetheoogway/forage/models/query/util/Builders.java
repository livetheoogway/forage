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

package com.livetheoogway.forage.models.query.util;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.search.BooleanQuery;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.FuzzyMatchQuery;
import com.livetheoogway.forage.models.query.search.MatchAllQuery;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.search.PhraseMatchQuery;
import com.livetheoogway.forage.models.query.search.Query;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.FloatRange;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import lombok.AllArgsConstructor;

public class Builders {

    interface Builder {
        ForageQuery buildForageQuery();

        ForageQuery buildForageQuery(int size);
    }

    public abstract static class InnerQueryBuilder implements Builder {
        public abstract Query build();

        @Override
        public ForageQuery buildForageQuery() {
            return new ForageSearchQuery(build(), 10);
        }

        @Override
        public ForageQuery buildForageQuery(final int size) {
            return new ForageSearchQuery(build(), size);
        }
    }

    @AllArgsConstructor
    public static class InnerMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;

        @Override
        public Query build() {
            return new MatchQuery(field, value);
        }
    }

    @AllArgsConstructor
    public static class InnerFuzzyMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;

        @Override
        public Query build() {
            return new FuzzyMatchQuery(field, value);
        }
    }

    @AllArgsConstructor
    public static class InnerPhraseMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String phrase;

        @Override
        public Query build() {
            return new PhraseMatchQuery(field, phrase);
        }
    }

    public static final class InnerMatchAllQueryBuilder extends InnerQueryBuilder {
        @Override
        public Query build() {
            return new MatchAllQuery();
        }
    }

    @AllArgsConstructor
    public static class InnerIntRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private int low;
        private int high;

        @Override
        public Query build() {
            return new RangeQuery(field, new IntRange(low, high));
        }
    }

    @AllArgsConstructor
    public static class InnerFloatRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private float low;
        private float high;

        @Override
        public Query build() {
            return new RangeQuery(field, new FloatRange(low, high));
        }
    }

    public static final class InnerBooleanQueryBuilder extends InnerQueryBuilder {
        private final BooleanQuery.BooleanQueryBuilder innerBuilder = BooleanQuery.builder();

        public InnerBooleanQueryBuilder query(Query query) {
            innerBuilder.query(query);
            return this;
        }

        public InnerBooleanQueryBuilder clauseType(ClauseType clauseType) {
            innerBuilder.clauseType(clauseType);
            return this;
        }

        @Override
        public Query build() {
            return innerBuilder.build();
        }
    }
}
