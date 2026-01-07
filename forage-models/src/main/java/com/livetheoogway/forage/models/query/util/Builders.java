/*
 * Copyright 2026. Live the Oogway, Tushar Naik
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
import com.livetheoogway.forage.models.query.SortCriteria;
import com.livetheoogway.forage.models.query.search.BooleanQuery;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.FunctionScoreQuery;
import com.livetheoogway.forage.models.query.search.FuzzyMatchQuery;
import com.livetheoogway.forage.models.query.search.MatchAllQuery;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.search.PhraseMatchQuery;
import com.livetheoogway.forage.models.query.search.PrefixMatchQuery;
import com.livetheoogway.forage.models.query.search.Query;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.score.ScoreFunction;
import com.livetheoogway.forage.models.query.search.range.FloatRange;
import com.livetheoogway.forage.models.query.search.range.IntRange;

import java.util.List;

public class Builders {

    interface Builder {
        ForageQuery buildForageQuery();

        ForageQuery buildForageQuery(int size);

        ForageQuery buildForageQuery(int size, List<SortCriteria> sortBy);

        ForageQuery buildForageQuery(int size, List<SortCriteria> sortBy, Float minimumScore);
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

        @Override
        public ForageQuery buildForageQuery(final int size, final List<SortCriteria> sortBy) {
            return new ForageSearchQuery(build(), size, sortBy, null);
        }

        @Override
        public ForageQuery buildForageQuery(final int size, final List<SortCriteria> sortBy, final Float minimumScore) {
            return new ForageSearchQuery(build(), size, sortBy, minimumScore);
        }
    }

    public static class InnerMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;
        private Float boost;

        public InnerMatchQueryBuilder(String field, String value) {
            this.field = field;
            this.value = value;
            this.boost = null;
        }

        public InnerMatchQueryBuilder boost(float boost) {
            return new InnerMatchQueryBuilder(field, value).setBoost(boost);
        }

        private InnerMatchQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new MatchQuery(field, value, boost);
        }
    }

    public static class InnerFuzzyMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;
        private Float boost;

        public InnerFuzzyMatchQueryBuilder(String field, String value) {
            this.field = field;
            this.value = value;
            this.boost = null;
        }

        public InnerFuzzyMatchQueryBuilder boost(float boost) {
            return new InnerFuzzyMatchQueryBuilder(field, value).setBoost(boost);
        }

        private InnerFuzzyMatchQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new FuzzyMatchQuery(field, value, boost);
        }
    }

    public static class InnerPhraseMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String phrase;
        private Float boost;

        public InnerPhraseMatchQueryBuilder(String field, String phrase) {
            this.field = field;
            this.phrase = phrase;
            this.boost = null;
        }

        public InnerPhraseMatchQueryBuilder boost(float boost) {
            return new InnerPhraseMatchQueryBuilder(field, phrase).setBoost(boost);
        }

        private InnerPhraseMatchQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new PhraseMatchQuery(field, phrase, boost);
        }
    }

    public static final class InnerMatchAllQueryBuilder extends InnerQueryBuilder {
        private Float boost;

        public InnerMatchAllQueryBuilder() {
            this.boost = null;
        }

        public InnerMatchAllQueryBuilder(Float boost) {
            this.boost = boost;
        }

        public InnerMatchAllQueryBuilder boost(float boost) {
            return new InnerMatchAllQueryBuilder(boost);
        }

        @Override
        public Query build() {
            return new MatchAllQuery(boost);
        }
    }

    public static class InnerPrefixMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String phrase;
        private Float boost;

        public InnerPrefixMatchQueryBuilder(String field, String phrase) {
            this.field = field;
            this.phrase = phrase;
            this.boost = null;
        }

        public InnerPrefixMatchQueryBuilder boost(float boost) {
            return new InnerPrefixMatchQueryBuilder(field, phrase).setBoost(boost);
        }

        private InnerPrefixMatchQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new PrefixMatchQuery(field, phrase, boost);
        }
    }

    public static class InnerIntRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private int low;
        private int high;
        private Float boost;

        public InnerIntRangeQueryBuilder(String field, int low, int high) {
            this.field = field;
            this.low = low;
            this.high = high;
            this.boost = null;
        }

        public InnerIntRangeQueryBuilder boost(float boost) {
            return new InnerIntRangeQueryBuilder(field, low, high).setBoost(boost);
        }

        private InnerIntRangeQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new RangeQuery(field, new IntRange(low, high), boost);
        }
    }

    public static class InnerFloatRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private float low;
        private float high;
        private Float boost;

        public InnerFloatRangeQueryBuilder(String field, float low, float high) {
            this.field = field;
            this.low = low;
            this.high = high;
            this.boost = null;
        }

        public InnerFloatRangeQueryBuilder boost(float boost) {
            return new InnerFloatRangeQueryBuilder(field, low, high).setBoost(boost);
        }

        private InnerFloatRangeQueryBuilder setBoost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new RangeQuery(field, new FloatRange(low, high), boost);
        }
    }

    public static final class InnerBooleanQueryBuilder extends InnerQueryBuilder {
        private List<Query> queries = new java.util.ArrayList<>();
        private ClauseType clauseType;
        private Float boost;

        public InnerBooleanQueryBuilder query(Query query) {
            this.queries.add(query);
            return this;
        }

        public InnerBooleanQueryBuilder clauseType(ClauseType clauseType) {
            this.clauseType = clauseType;
            return this;
        }

        public InnerBooleanQueryBuilder boost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            return new BooleanQuery(queries, clauseType, boost);
        }
    }

    public static final class InnerFunctionScoreQueryBuilder extends InnerQueryBuilder {
        private Query baseQuery;
        private ScoreFunction scoreFunction;
        private Float boost;

        public InnerFunctionScoreQueryBuilder baseQuery(Query baseQuery) {
            this.baseQuery = baseQuery;
            return this;
        }

        public InnerFunctionScoreQueryBuilder constantScore(float value) {
            this.scoreFunction = new ConstantScoreFunction(value);
            return this;
        }

        public InnerFunctionScoreQueryBuilder fieldValueFactor(String field) {
            this.scoreFunction = new FieldValueFactorFunction(field);
            return this;
        }

        public InnerFunctionScoreQueryBuilder fieldValueFactor(String field, float factor) {
            this.scoreFunction = new FieldValueFactorFunction(field, factor);
            return this;
        }

        public InnerFunctionScoreQueryBuilder scoreFunction(ScoreFunction scoreFunction) {
            this.scoreFunction = scoreFunction;
            return this;
        }

        public InnerFunctionScoreQueryBuilder boost(float boost) {
            this.boost = boost;
            return this;
        }

        @Override
        public Query build() {
            if (baseQuery == null) {
                throw new IllegalStateException("Base query is required for function score query");
            }
            if (scoreFunction == null) {
                throw new IllegalStateException("Score function is required for function score query");
            }
            return new FunctionScoreQuery(baseQuery, scoreFunction, boost);
        }
    }
}
