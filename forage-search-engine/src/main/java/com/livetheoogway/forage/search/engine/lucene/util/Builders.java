package com.livetheoogway.forage.search.engine.lucene.util;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.search.BooleanQuery;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.FuzzyMatchQuery;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.search.Query;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.FloatRange;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import lombok.AllArgsConstructor;

class Builders {

    interface Builder {
        ForageQuery build();

        ForageQuery build(int size);
    }

    abstract static class InnerQueryBuilder implements Builder {
        abstract Query query();

        @Override
        public ForageQuery build() {
            return new ForageSearchQuery(query(), 10);
        }

        @Override
        public ForageQuery build(final int size) {
            return new ForageSearchQuery(query(), size);
        }
    }

    @AllArgsConstructor
    public static class InnerMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;

        @Override
        Query query() {
            return new MatchQuery(field, value);
        }
    }

    @AllArgsConstructor
    public static class InnerFuzzyMatchQueryBuilder extends InnerQueryBuilder {
        private String field;
        private String value;

        @Override
        Query query() {
            return new FuzzyMatchQuery(field, value);
        }
    }

    @AllArgsConstructor
    public static class InnerIntRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private int low;
        private int high;

        @Override
        Query query() {
            return new RangeQuery(field, new IntRange(low, high));
        }
    }

    @AllArgsConstructor
    public static class InnerFloatRangeQueryBuilder extends InnerQueryBuilder {
        private String field;
        private float low;
        private float high;

        @Override
        Query query() {
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
        Query query() {
            return innerBuilder.build();
        }
    }
}
