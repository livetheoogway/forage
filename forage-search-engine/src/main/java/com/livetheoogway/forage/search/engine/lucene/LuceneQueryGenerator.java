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
import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.DecayFunction;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScriptScoreFunction;
import com.livetheoogway.forage.models.query.search.score.WeightedScoreFunction;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserSupplier;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.text.ParseException;

public class LuceneQueryGenerator implements QueryVisitor<Query> {

    private static final String FIELD_VALUE = "fieldValue";
    private static final String FACTOR = "factor";
    private static final Expression PRECOMPILED_EXPRESSION_FIELD_VALUE_FACTOR;
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

    static {
        try {
            PRECOMPILED_EXPRESSION_FIELD_VALUE_FACTOR = JavascriptCompiler.compile("fieldValue * factor");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private final Analyzer analyzer;
    private final QueryParserSupplier queryParserSupplier;

    public LuceneQueryGenerator(Analyzer analyzer, QueryParserSupplier queryParserSupplier) {
        this.analyzer = analyzer;
        this.queryParserSupplier = queryParserSupplier;
    }

    @Override
    public Query visit(final BooleanQuery booleanQuery) {
        final var queryBuilder = new org.apache.lucene.search.BooleanQuery.Builder();
        booleanQuery.getQueries()
                .stream()
                .map(this::visitThis)
                .forEach(query -> queryBuilder.add(query, booleanQuery.getClauseType().accept(CLAUSE_VISITOR)));
        final var builtQuery = queryBuilder.build();
        return applyBoost(builtQuery, booleanQuery.getBoost());
    }

    @Override
    public Query visit(final MatchQuery matchQuery) {
        final var termQuery = new TermQuery(new Term(matchQuery.getField(), matchQuery.getValue()));
        return applyBoost(termQuery, matchQuery.getBoost());
    }

    @Override
    public Query visit(final ParsableQuery parsableQuery) throws ForageSearchError {
        try {
            final var parsedQuery = queryParserSupplier
                    .queryParser(parsableQuery.getField())
                    .parse(parsableQuery.getQueryString());
            return applyBoost(parsedQuery, parsableQuery.getBoost());
        } catch (Exception e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
    }

    @Override
    public Query visit(final RangeQuery rangeQuery) {
        final Query rangeQueryLucene = rangeQuery.getRange().accept(new RangeVisitor<>() {
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
        final var fuzzyQuery = new FuzzyQuery(new Term(fuzzyMatchQuery.getField(), fuzzyMatchQuery.getValue()));
        return applyBoost(fuzzyQuery, fuzzyMatchQuery.getBoost());
    }

    @Override
    public Query visit(final PhraseMatchQuery phraseMatchQuery) throws ForageSearchError {

        final var builder = new MultiPhraseQuery.Builder();
        try (final TokenStream tokenStream = analyzer.tokenStream(phraseMatchQuery.getField(),
                                                                  phraseMatchQuery.getPhrase())) {
            final var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                builder.add(new Term(phraseMatchQuery.getField(), charTermAttribute.toString()));
            }
            tokenStream.end();
        } catch (IOException e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
        final var phraseQuery = builder.build();
        return applyBoost(phraseQuery, phraseMatchQuery.getBoost());
    }

    @Override
    public Query visit(final MatchAllQuery matchAllQuery) {
        final var matchAllDocsQuery = new MatchAllDocsQuery();
        return applyBoost(matchAllDocsQuery, matchAllQuery.getBoost());
    }

    @Override
    public Query visit(final PrefixMatchQuery prefixMatchQuery) {
        final var prefixQuery = new PrefixQuery(new Term(prefixMatchQuery.getField(), prefixMatchQuery.getValue()));
        return applyBoost(prefixQuery, prefixMatchQuery.getBoost());
    }

    @SneakyThrows
    private Query visitThis(final com.livetheoogway.forage.models.query.search.Query query) {
        return query.accept(this);
    }

    @Override
    public Query visit(final FunctionScoreQuery functionScoreQuery) throws Exception {
        final var baseLuceneQuery = functionScoreQuery.getBaseQuery().accept(this);
        final var scoreFunction = functionScoreQuery.getScoreFunction();
        final var valueSource = getDoubleValuesSource(scoreFunction);

        final org.apache.lucene.queries.function.FunctionScoreQuery finalQuery;

        // Determine if score should be multiplicative (base_score * value) or direct (value only)
        if (isMultiplicativeScoreFunction(scoreFunction)) {
            // Multiplicative: score = base_score * valueSource
            // Used for WEIGHTED_SCORE, SCRIPT_SCORE (when using base score)
            finalQuery = org.apache.lucene.queries.function.FunctionScoreQuery.boostByValue(baseLuceneQuery,
                                                                                            valueSource);
        } else {
            // Direct value: score = valueSource
            // Used for FIELD_VALUE_FACTOR, CONSTANT_SCORE, RANDOM_SCORE, DECAY_FUNCTION. These functions use field values directly as the score
            finalQuery = new org.apache.lucene.queries.function.FunctionScoreQuery(baseLuceneQuery, valueSource);
        }

        // Apply top-level boost if present
        return applyBoost(finalQuery, functionScoreQuery.getBoost());
    }

    /**
     * Determines if a score function should multiply with the base query score
     * or replace it entirely with its value.
     * Multiplicative functions: final_score = base_score × function_value
     * - WEIGHTED_SCORE: Scales relevance by a weight factor
     * - SCRIPT_SCORE: Custom expression that can reference 'score' variable
     * Direct value functions: final_score = function_value (ignores base score)
     * - CONSTANT_SCORE: All matches get the same constant score
     * - FIELD_VALUE_FACTOR: Score equals the field value
     * - RANDOM_SCORE: Deterministic pseudo-random score
     * - DECAY_FUNCTION: Distance-based decay score
     */
    private boolean isMultiplicativeScoreFunction(ScoreFunction scoreFunction) {
        return switch (scoreFunction.getType()) {
            case WEIGHTED_SCORE, SCRIPT_SCORE -> true;
            case CONSTANT_SCORE, FIELD_VALUE_FACTOR, RANDOM_SCORE, DECAY_FUNCTION -> false;
        };
    }

    private DoubleValuesSource getDoubleValuesSource(ScoreFunction scoreFunction) throws ParseException {
        return switch (scoreFunction.getType()) {
            case CONSTANT_SCORE -> {
                final var constantFn = (ConstantScoreFunction) scoreFunction;
                yield DoubleValuesSource.constant(constantFn.getValue());
            }
            case FIELD_VALUE_FACTOR -> {
                final var fieldValueFactorFn = (FieldValueFactorFunction) scoreFunction;
                final var factor = fieldValueFactorFn.getFactor();
                final var fieldSource = DoubleValuesSource.fromDoubleField(fieldValueFactorFn.getField());
                if (factor == 1.0f) {
                    yield fieldSource;
                }

                final var bindings = new SimpleBindings();
                bindings.add(FIELD_VALUE, fieldSource);
                bindings.add(FACTOR, DoubleValuesSource.constant(factor));

                yield PRECOMPILED_EXPRESSION_FIELD_VALUE_FACTOR.getDoubleValuesSource(bindings);
            }
            case SCRIPT_SCORE -> {
                final var scriptFn = (ScriptScoreFunction) scoreFunction;
                final var expr = JavascriptCompiler.compile(scriptFn.getExpression());

                final var bindings = new SimpleBindings();

                // Map the special 'score' variable to the base query's relevance score
                bindings.add("score", DoubleValuesSource.SCORES);

                // Map all other variables used in the script to DocValues fields
                for (String variable : expr.variables) {
                    if ("score".equals(variable)) continue;

                    // Assumes your numeric fields are indexed as Double/Float/Int DocValues
                    bindings.add(variable, DoubleValuesSource.fromDoubleField(variable));
                }

                yield expr.getDoubleValuesSource(bindings);
            }
            case RANDOM_SCORE -> {
                var randomFn = (RandomScoreFunction) scoreFunction;

                /*
                 * We use a simple Linear Congruential Generator or a hash-like math expression.
                 * This formula simulates randomness by oscillating a large prime multiplication.
                 * Formula: (abs(sin(fieldValue * seed)))
                 * Or simpler for Lucene Expressions: abs((fieldValue * seed) % 10000) / 10000.0
                 * */
                final var randomExpr = "abs(sin(fieldValue + seed))";
                final var expr = JavascriptCompiler.compile(randomExpr);
                final var bindings = new SimpleBindings();

                // Bind the user-provided seed
                bindings.add("seed", DoubleValuesSource.constant(randomFn.getSeed().doubleValue()));

                // Bind the unique field from the index
                // IMPORTANT: This field MUST be a NumericDocValuesField (int, long, or double)
                bindings.add(FIELD_VALUE, DoubleValuesSource.fromDoubleField(randomFn.getField()));

                yield expr.getDoubleValuesSource(bindings);
            }
            case WEIGHTED_SCORE -> {
                final var weightFn = (WeightedScoreFunction) scoreFunction;
                yield DoubleValuesSource.constant(weightFn.getWeight());
            }
            case DECAY_FUNCTION -> {
                final var decayFn = (DecayFunction) scoreFunction;

                // Choose the mathematical formula based on the DecayType
                // 'val' is the document's field value, 'origin', 'scale', etc. are your constants
                final var mathExpression = switch (decayFn.getDecayType()) {
                    case GAUSS ->
                        // exp(-0.5 * (max(0, abs(val - origin) - offset) / scale)^2 * -log(decay))
                        // Simplified for Lucene Expressions:
                            "exp(ln(decay) * pow(max(0, abs(fieldValue - origin) - offset) / scale, 2))";
                    case EXP ->
                        // exp(ln(decay) * (max(0, abs(val - origin) - offset) / scale))
                            "exp(ln(decay) * (max(0, abs(fieldValue - origin) - offset) / scale))";
                    case LINEAR ->
                        // max(0, (scale - max(0, abs(val - origin) - offset)) / scale)
                            "max(0, (scale - max(0, abs(fieldValue - origin) - offset)) / (scale / (1 - decay)))";
                };

                // Compile the expression
                final var expr = JavascriptCompiler.compile(mathExpression);

                // Bind variables to the constants and the index field
                final var bindings = new SimpleBindings();
                bindings.add("origin", DoubleValuesSource.constant(decayFn.getOrigin()));
                bindings.add("scale", DoubleValuesSource.constant(decayFn.getScale()));
                bindings.add("offset", DoubleValuesSource.constant(decayFn.getOffset()));
                bindings.add("decay", DoubleValuesSource.constant(decayFn.getDecay()));

                // 'fieldValue' maps to the actual DocValues in the Lucene index
                // Note: You'll need to pass the field name to the ScoreFunction or context
                bindings.add(FIELD_VALUE, DoubleValuesSource.fromDoubleField(decayFn.getField()));

                yield expr.getDoubleValuesSource(bindings);
            }
        };
    }

    private Query applyBoost(Query query, Float boost) {
        if (boost != null && boost != 1.0f) {
            return new BoostQuery(query, boost);
        }
        return query;
    }
}
