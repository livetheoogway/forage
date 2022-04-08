package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public interface LuceneIndex {
    IndexSearcher searcher() throws ForageSearchError;

    IndexWriter indexWriter() throws ForageSearchError;

    void flush() throws ForageSearchError;

    DocRetriever docRetriever();

    LuceneIndex freshIndex() throws ForageSearchError;
}
