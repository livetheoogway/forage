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
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserFactory;
import com.livetheoogway.forage.search.engine.store.Store;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;
import java.util.concurrent.locks.StampedLock;

/**
 * This class uses a {@link StampedLock} to ensure that the {@link StampedLockCloseForageLuceneSearchEngine#close()}
 * happens only after taking a write lock, and no more new search queries are accepted
 */
public class StampedLockCloseForageLuceneSearchEngine<D>
        extends ForageLuceneSearchEngine<D> {

    private final StampedLock lock = new StampedLock();

    public StampedLockCloseForageLuceneSearchEngine(final ObjectMapper mapper,
                                                    final QueryParserFactory queryParserFactory,
                                                    final Store<D> dataStore,
                                                    final Analyzer analyzer) {
        super(mapper, queryParserFactory, dataStore, analyzer);
    }

    @SneakyThrows
    @Override
    public ForageQueryResult<D> search(final ForageQuery forageQuery) throws ForageSearchError {
        final long readStamp = lock.readLock();
        try {
            return super.search(forageQuery);
        } finally {
            lock.unlockRead(readStamp);
        }
    }

    @Override
    public void close() throws IOException {
        final long writeStamp = lock.writeLock();
        try {
            super.close();
        } finally {
            lock.unlockWrite(writeStamp);
        }
    }
}

