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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.parser.CachedQueryParserFactory;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserFactory;
import com.livetheoogway.forage.search.engine.store.Store;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class ForageSearchEngineBuilder<T> {
    private static final int DEFAULT_FIELD_SIZE_SUGGESTION = 10;

    private ObjectMapper mapper;
    private Analyzer analyzer;
    private int maxFieldSizeHint;
    private QueryParserFactory queryParserFactory;
    private Store<T> store;

    public ForageSearchEngineBuilder<T> withObjectMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public ForageSearchEngineBuilder<T> withAnalyser(final Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public ForageSearchEngineBuilder<T> withQueryParserFactory(final QueryParserFactory queryParserFactory) {
        this.queryParserFactory = queryParserFactory;
        return this;
    }

    public ForageSearchEngineBuilder<T> withMaxFieldSizeHint(final int maxFieldSizeHint) {
        this.maxFieldSizeHint = maxFieldSizeHint;
        return this;
    }

    public ForageSearchEngineBuilder<T> withDataStore(final Store<T> store) {
        this.store = store;
        return this;
    }

    public static <T> ForageSearchEngineBuilder<T> builder() {
        return new ForageSearchEngineBuilder<>();
    }

    public ForageLuceneSearchEngine<T> build() throws ForageSearchError {
        /* validations */
        if (this.store == null) {
            throw ForageSearchError.raise(ForageErrorCode.DATASTORE_INVALID, "no datastore passed to builder");
        }

        /* defaults */
        if (this.analyzer == null) {
            this.analyzer = new StandardAnalyzer();
        }
        if (this.maxFieldSizeHint < 1) {
            this.maxFieldSizeHint = DEFAULT_FIELD_SIZE_SUGGESTION;
        }
        if (queryParserFactory == null) {
            queryParserFactory = new CachedQueryParserFactory(analyzer, maxFieldSizeHint);
        }
        if (mapper == null) {
            this.mapper = new ObjectMapper();
        }

        /* create a new engine */
        return new ForageLuceneSearchEngine<>(mapper, queryParserFactory, store, analyzer);
    }
}
