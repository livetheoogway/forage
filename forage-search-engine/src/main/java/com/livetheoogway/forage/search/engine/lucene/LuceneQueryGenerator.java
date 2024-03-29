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
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;

@AllArgsConstructor
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
    private Analyzer analyzer;
    private QueryParserSupplier queryParserSupplier;

    @Override
    public Query visit(final BooleanQuery booleanQuery) {
        final org.apache.lucene.search.BooleanQuery.Builder queryBuilder =
                new org.apache.lucene.search.BooleanQuery.Builder();
        booleanQuery.getQueries()
                .stream()
                .map(this::visitThis)
                .forEach(query -> queryBuilder.add(query, booleanQuery.getClauseType().accept(CLAUSE_VISITOR)));
        return queryBuilder.build();
    }

    @Override
    public Query visit(final MatchQuery matchQuery) {
        return new TermQuery(new Term(matchQuery.getField(), matchQuery.getValue()));
    }

    @Override
    public Query visit(final ParsableQuery parsableQuery) throws ForageSearchError {
        try {
            return queryParserSupplier
                    .queryParser(parsableQuery.getField())
                    .parse(parsableQuery.getQueryString());
        } catch (Exception e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
    }

    @Override
    public Query visit(final RangeQuery rangeQuery) {
        return rangeQuery.getRange().accept(new RangeVisitor<>() {
            @Override
            public Query visit(final IntRange intRange) {
                return IntPoint.newRangeQuery(rangeQuery.getField(), intRange.getLow(), intRange.getHigh());
            }

            @Override
            public Query visit(final FloatRange floatRange) {
                return FloatPoint.newRangeQuery(rangeQuery.getField(), floatRange.getLow(), floatRange.getHigh());
            }
        });
    }

    @Override
    public Query visit(final FuzzyMatchQuery fuzzyMatchQuery) {
        return new FuzzyQuery(new Term(fuzzyMatchQuery.getField(), fuzzyMatchQuery.getValue()));
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
        return builder.build();
    }

    @Override
    public Query visit(final MatchAllQuery matchAllQuery) {
        return new MatchAllDocsQuery();
    }

    @Override
    public Query visit(final PrefixMatchQuery prefixMatchQuery) {
        return new PrefixQuery(new Term(prefixMatchQuery.getField(), prefixMatchQuery.getValue()));
    }

    @SneakyThrows
    private Query visitThis(final com.livetheoogway.forage.models.query.search.Query query) {
        return query.accept(this);
    }
}
