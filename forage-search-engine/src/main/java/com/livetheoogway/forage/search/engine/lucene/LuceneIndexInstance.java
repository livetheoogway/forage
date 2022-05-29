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

import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.util.ExceptionExecution;
import com.livetheoogway.forage.search.engine.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LuceneIndexInstance implements LuceneIndex {

    private final Directory memoryIndex;
    private final Analyzer analyzer;
    private final AtomicReference<IndexWriter> indexWriterReference;
    private final AtomicReference<DocRetriever> indexReaderReference;
    private final AtomicBoolean indexWriterReferenceChanged;

    public LuceneIndexInstance(Analyzer analyzer) {
        this.analyzer = analyzer;
        memoryIndex = newInMemoryIndex();
        indexWriterReference = new AtomicReference<>();
        indexWriterReferenceChanged = new AtomicBoolean(false);
        indexReaderReference = new AtomicReference<>(null);
    }

    public static Directory newInMemoryIndex() {
        return new ByteBuffersDirectory();
    }

    @Override
    public void close() {
        if (indexReaderReference.get() != null) {
            Utils.closeSafe(indexReaderReference.get().getIndexReader(), "IndexReader");
        }
        Utils.closeSafe(indexWriterReference.get(), "IndexWriter");
        Utils.closeSafe(memoryIndex, "MemoryIndex");
    }

    @Override
    public IndexSearcher searcher() throws ForageSearchError {
        if (indexWriterReferenceChanged.get()) {
            synchronized (indexWriterReferenceChanged) {
                if (indexWriterReferenceChanged.get()) {
                    final IndexReader indexReader = ExceptionExecution.get(() -> DirectoryReader.open(memoryIndex),
                                                                           ForageErrorCode.INDEX_READER_IO_ERROR);
                    final IndexSearcher searcher = new IndexSearcher(indexReader);
                    final DocRetriever docRetriever = new DocRetriever(indexReader, searcher);
                    final DocRetriever docRetrieverToBeClosed = indexReaderReference.get();
                    indexReaderReference.set(docRetriever);
                    if (docRetrieverToBeClosed != null) {
                        Utils.closeSafe(docRetrieverToBeClosed.getIndexReader(), "IndexReader");
                    }
                    indexWriterReferenceChanged.set(false);
                }
            }
        }
        return indexReaderReference.get().getSearcher();
    }

    @Override
    public IndexWriter indexWriter() throws ForageSearchError {
        if (indexWriterReference.get() == null) {
            synchronized (indexWriterReference) {
                /* this synchronization is being done so that indexWriter is created only once, iff there exists no
                current writer */
                if (indexWriterReference.get() == null) {
                    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
                    final IndexWriter indexWriter = ExceptionExecution.get(
                            () -> new IndexWriter(memoryIndex, indexWriterConfig),
                            ForageErrorCode.INDEX_WRITER_IO_ERROR);
                    indexWriterReference.set(indexWriter);
                }
            }
        }
        return indexWriterReference.get();
    }

    @Override
    public synchronized void flush() throws ForageSearchError {
        if (indexWriterReference.get() == null) {
            throw ForageSearchError.raise(ForageErrorCode.INDEX_FLUSH_ERROR, "Nothing to flush");
        }
        final IndexWriter indexWriter = indexWriterReference.get();
        try {
            indexWriter.flush();
            Utils.closeSafe(indexWriter, "IndexWriter");
            indexWriterReference.set(null);
            indexWriterReferenceChanged.set(true);
        } catch (IOException e) {
            throw ForageSearchError.raise(ForageErrorCode.INDEX_FLUSH_ERROR, "Unable to flush", e);
        }
    }

    @Override
    public DocRetriever docRetriever() {
        return indexReaderReference.get();
    }

    @Override
    public LuceneIndex freshIndex() {
        return new LuceneIndexInstance(this.analyzer);
    }
}
