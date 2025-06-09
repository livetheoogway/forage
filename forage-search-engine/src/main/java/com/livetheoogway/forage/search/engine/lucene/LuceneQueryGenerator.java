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

import com.livetheoogway.forage.models.query.search.BooleanQuery;
import com.livetheoogway.forage.models.query.search.ClauseVisitor;
import com.livetheoogway.forage.models.query.search.FunctionScoreQuery;
import com.livetheoogway.forage.models.query.search.FuzzyMatchQuery;
import com.livetheoogway.forage.models.query.search.MatchAllQuery;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.search.ParsableQuery;
import com.livetheoogway.forage.models.query.search.PhraseMatchQuery;
import com.livetheoogway.forage.models.query.search.PrefixMatchQuery;
import com.livetheoogway.forage.models.query.search.QueryVisitor;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.FloatRange;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import com.livetheoogway.forage.models.query.search.range.RangeVisitor;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserSupplier;
import com.livetheoogway.forage.search.engine.lucene.FieldBoostRegistry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;

public class LuceneQueryGenerator implements QueryVisitor<Query> {
    private static final ClauseVisitor<BooleanClause.Occur> CLAUSE_VISITOR = new ClauseVisitor<>() {
        @Override
        public BooleanClause.Occur must() {
            return BooleanClause.Occur.MUST;
        }

        @Override
        public BooleanClause.Occur should() {
            return BooleanClause.Occur.SHOULD;
        }

        @Override
        public BooleanClause.Occur mustNot() {
            return BooleanClause.Occur.MUST_NOT;
        }

        @Override
        public BooleanClause.Occur filter() {
            return BooleanClause.Occur.FILTER;
        }
    };
    private final Analyzer analyzer;
    private final QueryParserSupplier queryParserSupplier;
    private final FieldBoostRegistry fieldBoostRegistry;

    public LuceneQueryGenerator(Analyzer analyzer, QueryParserSupplier queryParserSupplier) {
        this(analyzer, queryParserSupplier, null);
    }

    public LuceneQueryGenerator(Analyzer analyzer, QueryParserSupplier queryParserSupplier, FieldBoostRegistry fieldBoostRegistry) {
        this.analyzer = analyzer;
        this.queryParserSupplier = queryParserSupplier;
        this.fieldBoostRegistry = fieldBoostRegistry != null ? fieldBoostRegistry : new FieldBoostRegistry();
    }

    @Override
    public Query visit(final BooleanQuery booleanQuery) {
        final org.apache.lucene.search.BooleanQuery.Builder queryBuilder =
                new org.apache.lucene.search.BooleanQuery.Builder();
        booleanQuery.getQueries()
                .stream()
                .map(this::visitThis)
                .forEach(query -> queryBuilder.add(query, booleanQuery.getClauseType().accept(CLAUSE_VISITOR)));
        Query builtQuery = queryBuilder.build();
        return applyBoost(builtQuery, booleanQuery.getBoost());
    }

    @Override
    public Query visit(final MatchQuery matchQuery) {
        Query termQuery = new TermQuery(new Term(matchQuery.getField(), matchQuery.getValue()));
        
        // Apply field-level boost from registry if available
        float fieldBoost = fieldBoostRegistry.getFieldBoost(matchQuery.getField());
        if (fieldBoost != 1.0f) {
            termQuery = new BoostQuery(termQuery, fieldBoost);
        }
        
        return applyBoost(termQuery, matchQuery.getBoost());
    }

    @Override
    public Query visit(final ParsableQuery parsableQuery) throws ForageSearchError {
        try {
            Query parsedQuery = queryParserSupplier
                    .queryParser(parsableQuery.getField())
                    .parse(parsableQuery.getQueryString());
            return applyBoost(parsedQuery, parsableQuery.getBoost());
        } catch (Exception e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
    }

    @Override
    public Query visit(final RangeQuery rangeQuery) {
        Query rangeQueryLucene = rangeQuery.getRange().accept(new RangeVisitor<>() {
            @Override
            public Query visit(final IntRange intRange) {
                return IntPoint.newRangeQuery(rangeQuery.getField(), intRange.getLow(), intRange.getHigh());
            }

            @Override
            public Query visit(final FloatRange floatRange) {
                return FloatPoint.newRangeQuery(rangeQuery.getField(), floatRange.getLow(), floatRange.getHigh());
            }
        });
        return applyBoost(rangeQueryLucene, rangeQuery.getBoost());
    }

    @Override
    public Query visit(final FuzzyMatchQuery fuzzyMatchQuery) {
        Query fuzzyQuery = new FuzzyQuery(new Term(fuzzyMatchQuery.getField(), fuzzyMatchQuery.getValue()));
        return applyBoost(fuzzyQuery, fuzzyMatchQuery.getBoost());
    }

    @Override
    public Query visit(final PhraseMatchQuery phraseMatchQuery) throws ForageSearchError {

        final MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
        try (final TokenStream tokenStream = analyzer.tokenStream(phraseMatchQuery.getField(), phraseMatchQuery.getPhrase())) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset(); // Resets this stream to the beginning. (Required)
            while (tokenStream.incrementToken()) {
                builder.add(new Term(phraseMatchQuery.getField(), charTermAttribute.toString()));
            }
            tokenStream.end();   // Perform end-of-stream operations, e.g. set the final offset.
        } catch (IOException e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
        Query phraseQuery = builder.build();
        return applyBoost(phraseQuery, phraseMatchQuery.getBoost());
    }

    @Override
    public Query visit(final MatchAllQuery matchAllQuery) {
        Query matchAllDocsQuery = new MatchAllDocsQuery();
        return applyBoost(matchAllDocsQuery, matchAllQuery.getBoost());
    }

    @Override
    public Query visit(final PrefixMatchQuery prefixMatchQuery) {
        Query prefixQuery = new PrefixQuery(new Term(prefixMatchQuery.getField(), prefixMatchQuery.getValue()));
        return applyBoost(prefixQuery, prefixMatchQuery.getBoost());
    }

    @SneakyThrows
    private Query visitThis(final com.livetheoogway.forage.models.query.search.Query query) {
        return query.accept(this);
    }

    @Override
    public Query visit(final FunctionScoreQuery functionScoreQuery) throws Exception {
        // For now, implement function scoring as a basic boost query
        // This is a simplified implementation - a full implementation would require
        // custom Lucene query classes or expression-based scoring
        Query baseQuery = functionScoreQuery.getBaseQuery().accept(this);
        
        // Apply a simple boost based on the score function type
        // This is a basic implementation that can be enhanced
        float calculatedBoost = calculateBoostFromFunction(functionScoreQuery.getScoreFunction());
        Query boostedQuery = new BoostQuery(baseQuery, calculatedBoost);
        
        return applyBoost(boostedQuery, functionScoreQuery.getBoost());
    }

    private float calculateBoostFromFunction(final com.livetheoogway.forage.models.query.search.ScoreFunction scoreFunction) {
        // Basic implementation - in a full implementation, this would integrate with Lucene's scoring
        switch (scoreFunction.getType()) {
            case CONSTANT_SCORE:
                var constantFunction = (com.livetheoogway.forage.models.query.search.ConstantScoreFunction) scoreFunction;
                return constantFunction.getValue();
            case FIELD_VALUE_FACTOR:
                var fieldFunction = (com.livetheoogway.forage.models.query.search.FieldValueFactorFunction) scoreFunction;
                return fieldFunction.getFactor(); // Simplified - would need field value lookup in real implementation
            default:
                return 1.0f;
        }
    }

    private Query applyBoost(final Query query, final Float boost) {
        if (boost != null && boost != 1.0f) {
            return new BoostQuery(query, boost);
        }
        return query;
    }
}
