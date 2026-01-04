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

package com.livetheoogway.forage.models.query.search;

import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.DecayFunction;
import com.livetheoogway.forage.models.query.search.score.DecayType;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScoreFunctionType;
import com.livetheoogway.forage.models.query.search.score.ScriptScoreFunction;
import com.livetheoogway.forage.models.query.search.score.WeightedScoreFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionScoreQueryTest {

    @Test
    void testFunctionScoreQueryWithConstantScore() {
        MatchQuery baseQuery = new MatchQuery("title", "java");
        ConstantScoreFunction scoreFunction = new ConstantScoreFunction(2.0f);
        
        FunctionScoreQuery query = new FunctionScoreQuery(baseQuery, scoreFunction, 1.5f);
        
        assertEquals(baseQuery, query.getBaseQuery());
        assertEquals(scoreFunction, query.getScoreFunction());
        assertEquals(1.5f, query.getBoost());
        assertEquals(QueryType.FUNCTION_SCORE, query.getType());
    }

    @Test
    void testFunctionScoreQueryWithFieldValueFactor() {
        MatchQuery baseQuery = new MatchQuery("category", "programming");
        FieldValueFactorFunction scoreFunction = new FieldValueFactorFunction("popularity", 1.5f);
        
        FunctionScoreQuery query = new FunctionScoreQuery(baseQuery, scoreFunction);
        
        assertEquals(baseQuery, query.getBaseQuery());
        assertEquals(scoreFunction, query.getScoreFunction());
        assertNull(query.getBoost());
        assertEquals(QueryType.FUNCTION_SCORE, query.getType());
    }

    @Test
    void testConstantScoreFunction() {
        ConstantScoreFunction function = new ConstantScoreFunction(3.0f);
        
        assertEquals(3.0f, function.getValue());
        assertEquals(ScoreFunctionType.CONSTANT_SCORE, function.getType());
    }

    @Test
    void testConstantScoreFunctionWithDefaultValue() {
        ConstantScoreFunction function = new ConstantScoreFunction();
        
        assertEquals(1.0f, function.getValue());
        assertEquals(ScoreFunctionType.CONSTANT_SCORE, function.getType());
    }

    @Test
    void testConstantScoreFunctionWithNullValue() {
        ConstantScoreFunction function = new ConstantScoreFunction(null);
        
        assertEquals(1.0f, function.getValue()); // Should default to 1.0f
        assertEquals(ScoreFunctionType.CONSTANT_SCORE, function.getType());
    }

    @Test
    void testFieldValueFactorFunction() {
        FieldValueFactorFunction function = new FieldValueFactorFunction("rating", 2.0f);
        
        assertEquals("rating", function.getField());
        assertEquals(2.0f, function.getFactor());
        assertEquals(ScoreFunctionType.FIELD_VALUE_FACTOR, function.getType());
    }

    @Test
    void testFieldValueFactorFunctionWithDefaults() {
        FieldValueFactorFunction function = new FieldValueFactorFunction("rating");
        
        assertEquals("rating", function.getField());
        assertEquals(1.0f, function.getFactor());
        assertEquals(ScoreFunctionType.FIELD_VALUE_FACTOR, function.getType());
    }

    @Test
    void testFieldValueFactorFunctionWithFactor() {
        FieldValueFactorFunction function = new FieldValueFactorFunction("popularity", 1.5f);
        
        assertEquals("popularity", function.getField());
        assertEquals(1.5f, function.getFactor());
        assertEquals(ScoreFunctionType.FIELD_VALUE_FACTOR, function.getType());
    }

    @Test
    void testFieldValueFactorFunctionWithNullValues() {
        FieldValueFactorFunction function = new FieldValueFactorFunction("rating", null);
        
        assertEquals("rating", function.getField());
        assertEquals(1.0f, function.getFactor()); // Should default to 1.0f
        assertEquals(ScoreFunctionType.FIELD_VALUE_FACTOR, function.getType());
    }

    @Test
    void testScoreFunctionTypes() {
        assertEquals(ScoreFunctionType.CONSTANT_SCORE, new ConstantScoreFunction().getType());
        assertEquals(ScoreFunctionType.FIELD_VALUE_FACTOR, new FieldValueFactorFunction("field").getType());
        assertEquals(ScoreFunctionType.SCRIPT_SCORE, new ScriptScoreFunction("score").getType());
        assertEquals(ScoreFunctionType.RANDOM_SCORE, new RandomScoreFunction("field").getType());
        assertEquals(ScoreFunctionType.WEIGHTED_SCORE, new WeightedScoreFunction().getType());
        assertEquals(ScoreFunctionType.DECAY_FUNCTION, new DecayFunction("field", 0.0, 1.0).getType());
    }

    @Test
    void testFunctionScoreQueryConstructorVariations() {
        MatchQuery baseQuery = new MatchQuery("title", "test");
        ConstantScoreFunction scoreFunction = new ConstantScoreFunction(2.0f);
        
        // With boost
        FunctionScoreQuery queryWithBoost = new FunctionScoreQuery(baseQuery, scoreFunction, 1.5f);
        assertEquals(1.5f, queryWithBoost.getBoost());
        
        // Without boost
        FunctionScoreQuery queryWithoutBoost = new FunctionScoreQuery(baseQuery, scoreFunction);
        assertNull(queryWithoutBoost.getBoost());
        
        // Both should have same base query and score function
        assertEquals(baseQuery, queryWithBoost.getBaseQuery());
        assertEquals(baseQuery, queryWithoutBoost.getBaseQuery());
        assertEquals(scoreFunction, queryWithBoost.getScoreFunction());
        assertEquals(scoreFunction, queryWithoutBoost.getScoreFunction());
    }

    @Test
    void testWeightedScoreFunctionDefaultsToUnity() {
        WeightedScoreFunction function = new WeightedScoreFunction();

        assertEquals(1.0f, function.getWeight());
        assertEquals(ScoreFunctionType.WEIGHTED_SCORE, function.getType());
    }

    @Test
    void testWeightedScoreFunctionRespectsCustomWeight() {
        WeightedScoreFunction function = new WeightedScoreFunction(2.5f);

        assertEquals(2.5f, function.getWeight());
        assertEquals(ScoreFunctionType.WEIGHTED_SCORE, function.getType());
    }

    @Test
    void testRandomScoreFunctionDefaultsSeed() {
        RandomScoreFunction function = new RandomScoreFunction("docId");

        assertEquals(1L, function.getSeed());
        assertEquals("docId", function.getField());
        assertEquals(ScoreFunctionType.RANDOM_SCORE, function.getType());
    }

    @Test
    void testRandomScoreFunctionCustomSeed() {
        RandomScoreFunction function = new RandomScoreFunction(42L, "docId");

        assertEquals(42L, function.getSeed());
        assertEquals("docId", function.getField());
        assertEquals(ScoreFunctionType.RANDOM_SCORE, function.getType());
    }

    @Test
    void testScriptScoreFunctionStoresExpression() {
        String expression = "score + popularity";
        ScriptScoreFunction function = new ScriptScoreFunction(expression);

        assertEquals(expression, function.getExpression());
        assertEquals(ScoreFunctionType.SCRIPT_SCORE, function.getType());
    }

    @Test
    void testDecayFunctionDefaults() {
        DecayFunction function = new DecayFunction(null, null, null, null, null, "freshness");

        assertEquals(0.0, function.getOrigin());
        assertEquals(1.0, function.getScale());
        assertEquals(0.0, function.getOffset());
        assertEquals(0.5, function.getDecay());
        assertEquals(DecayType.EXP, function.getDecayType());
        assertEquals("freshness", function.getField());
        assertEquals(ScoreFunctionType.DECAY_FUNCTION, function.getType());
    }

    @Test
    void testDecayFunctionCustomValues() {
        DecayFunction function = new DecayFunction(10.0, 5.0, 1.0, 0.2, DecayType.LINEAR, "freshness");

        assertEquals(10.0, function.getOrigin());
        assertEquals(5.0, function.getScale());
        assertEquals(1.0, function.getOffset());
        assertEquals(0.2, function.getDecay());
        assertEquals(DecayType.LINEAR, function.getDecayType());
        assertEquals("freshness", function.getField());
    }
}
