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

package com.livetheoogway.forage.models.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortCriteriaTest {

    @Test
    void testSortCriteriaWithAllParameters() {
        SortCriteria criteria = new SortCriteria("rating", SortOrder.ASC, SortType.FIELD);
        
        assertEquals("rating", criteria.getField());
        assertEquals(SortOrder.ASC, criteria.getOrder());
        assertEquals(SortType.FIELD, criteria.getType());
    }

    @Test
    void testSortCriteriaWithDefaultType() {
        SortCriteria criteria = new SortCriteria("title", SortOrder.DESC);
        
        assertEquals("title", criteria.getField());
        assertEquals(SortOrder.DESC, criteria.getOrder());
        assertEquals(SortType.FIELD, criteria.getType());
    }

    @Test
    void testSortCriteriaWithDefaultOrderAndType() {
        SortCriteria criteria = new SortCriteria("author");
        
        assertEquals("author", criteria.getField());
        assertEquals(SortOrder.DESC, criteria.getOrder());
        assertEquals(SortType.FIELD, criteria.getType());
    }

    @Test
    void testSortCriteriaByScore() {
        SortCriteria criteria = SortCriteria.byScore();
        
        assertNull(criteria.getField());
        assertEquals(SortOrder.DESC, criteria.getOrder());
        assertEquals(SortType.SCORE, criteria.getType());
    }

    @Test
    void testSortCriteriaByScoreWithOrder() {
        SortCriteria criteria = SortCriteria.byScore(SortOrder.ASC);
        
        assertNull(criteria.getField());
        assertEquals(SortOrder.ASC, criteria.getOrder());
        assertEquals(SortType.SCORE, criteria.getType());
    }

    @Test
    void testSortCriteriaWithNullOrder() {
        SortCriteria criteria = new SortCriteria("rating", null, SortType.FIELD);
        
        assertEquals("rating", criteria.getField());
        assertEquals(SortOrder.DESC, criteria.getOrder()); // Should default to DESC
        assertEquals(SortType.FIELD, criteria.getType());
    }

    @Test
    void testSortCriteriaWithNullType() {
        SortCriteria criteria = new SortCriteria("rating", SortOrder.ASC, null);
        
        assertEquals("rating", criteria.getField());
        assertEquals(SortOrder.ASC, criteria.getOrder());
        assertEquals(SortType.SCORE, criteria.getType()); // Should default to SCORE
    }

    @Test
    void testSortCriteriaFieldSorting() {
        SortCriteria ascCriteria = new SortCriteria("title", SortOrder.ASC, SortType.FIELD);
        SortCriteria descCriteria = new SortCriteria("title", SortOrder.DESC, SortType.FIELD);
        
        assertEquals(SortType.FIELD, ascCriteria.getType());
        assertEquals(SortType.FIELD, descCriteria.getType());
        assertEquals(SortOrder.ASC, ascCriteria.getOrder());
        assertEquals(SortOrder.DESC, descCriteria.getOrder());
    }

    @Test
    void testSortCriteriaScoreSorting() {
        SortCriteria scoreAsc = SortCriteria.byScore(SortOrder.ASC);
        SortCriteria scoreDesc = SortCriteria.byScore(SortOrder.DESC);
        
        assertEquals(SortType.SCORE, scoreAsc.getType());
        assertEquals(SortType.SCORE, scoreDesc.getType());
        assertNull(scoreAsc.getField());
        assertNull(scoreDesc.getField());
    }
}