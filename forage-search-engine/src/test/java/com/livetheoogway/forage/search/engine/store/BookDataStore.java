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

package com.livetheoogway.forage.search.engine.store;

import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.search.engine.ResourceReader;
import com.livetheoogway.forage.search.engine.model.Book;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.util.MapEntry;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookDataStore implements Bootstrapper<IndexableDocument>, Store<Book> {
    private final AtomicInteger indexPosition;
    private final Map<String, Book> booksAvailableForIndexing;
    private final List<Book> fullGlossaryOfBook;

    public BookDataStore() throws IOException {
        this.fullGlossaryOfBook = ResourceReader.extractBooks();
        this.indexPosition = new AtomicInteger(0);
        this.booksAvailableForIndexing = new HashMap<>();
    }

    public void addBooks(int numberOfBooksToBeAddedForIndexing) {
        this.booksAvailableForIndexing.putAll(
                fullGlossaryOfBook.subList(indexPosition.get(),
                                           indexPosition.get() + numberOfBooksToBeAddedForIndexing)
                        .stream().collect(Collectors.toMap(Book::id, Function.identity())));
        indexPosition.compareAndSet(indexPosition.get(), indexPosition.get() + numberOfBooksToBeAddedForIndexing);
    }

    public void addAllBooks() {
        this.addBooks(fullGlossaryOfBook.size() - 1);
    }

    @Override
    public void bootstrap(final Consumer<IndexableDocument> itemConsumer) {
        booksAvailableForIndexing
                .forEach((key, value) -> itemConsumer.accept(new ForageDocument(key, value.fields())));
    }

    @Override
    public Map<String, Book> get(final List<String> ids) {
        return ids.stream().map(k -> MapEntry.of(k, booksAvailableForIndexing.get(k))).collect(MapEntry.mapCollector());
    }
}
