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

package com.livetheoogway.forage.models.query.search;

@SuppressWarnings("java:S112")
public interface QueryVisitor<T> {

    T visit(BooleanQuery booleanQuery) throws Exception;

    T visit(MatchQuery matchQuery) throws Exception;

    T visit(ParsableQuery parsableQuery) throws Exception;

    T visit(RangeQuery rangeQuery) throws Exception;

    T visit(FuzzyMatchQuery fuzzyMatchQuery) throws Exception;

    T visit(PhraseMatchQuery phraseMatchQuery) throws Exception;

    T visit(MatchAllQuery matchAllQuery) throws Exception;

    T visit(PrefixMatchQuery prefixMatchQuery);
}
