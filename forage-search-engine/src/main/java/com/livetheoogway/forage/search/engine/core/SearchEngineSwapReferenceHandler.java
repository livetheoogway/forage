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

package com.livetheoogway.forage.search.engine.core;

import com.livetheoogway.forage.core.ItemConsumer;
import com.livetheoogway.forage.search.engine.DocumentIndexer;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * This class implements the core libraries {@link ItemConsumer}.
 * It is responsible for swapping the search engine during every bootstrap.
 * Here, we keep a reference of the current search engine using an {@link AtomicReference}.
 * During every bootstrap, we initialize a new search engine, and once all items are consumed, we flush (lucene flush
 * makes the documents searchable), and then swap the newly created engine, with the current one, so that it can
 * serve all new search requests
 *
 * @param <T> Type of data being stored in the main database
 */
@Slf4j
public class SearchEngineSwapReferenceHandler<T, R extends DocumentIndexer<T>> implements ItemConsumer<T> {

    private final Supplier<R> newSearchEngineSupplier;
    private final AtomicReference<R> liveReference;
    private final AtomicReference<R> newReferenceBeingBuilt;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final StampedLock lock = new StampedLock();

    public SearchEngineSwapReferenceHandler(final Supplier<R> newSearchEngineSupplier) {
        this.newSearchEngineSupplier = newSearchEngineSupplier;
        this.liveReference = new AtomicReference<>();
        this.newReferenceBeingBuilt = new AtomicReference<>();
    }

    @Override
    public void init() throws Exception {
        var stamp = lock.readLock();
        try {
            if (newReferenceBeingBuilt.get() != null) {
                /* This can happen if the previous thread trying to bootstrap, did not finish() entirely */
                log.warn("[forage] The previous engine was not entirely swapped out. We are now going to clean it up "
                                 + "and then recreate a new searchEngine");
                val writeLock = lock.tryConvertToWriteLock(stamp);
                if (writeLock == 0) {
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                } else {
                    stamp = writeLock;
                }
                newReferenceBeingBuilt.get().close();
            }
            newReferenceBeingBuilt.set(newSearchEngineSupplier.get());
            counter.set(0);
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public void consume(final T indexableDocument) throws Exception {
        newReferenceBeingBuilt.get().index(indexableDocument);
        counter.incrementAndGet();
    }

    @Override
    public void finish() throws IOException, ForageSearchError {
        val writeStamp = lock.writeLock();
        try (DocumentIndexer<T> ignored = liveReference.get()) {
            newReferenceBeingBuilt.get().flush();
            liveReference.set(newReferenceBeingBuilt.get());
            log.info("[forage] reference successfully swapped. Indexed: {}", counter.get());
        } finally {
            newReferenceBeingBuilt.set(null);
            lock.unlockWrite(writeStamp);
        }
    }

    public R searchEngine() {
        return liveReference.get();
    }
}
