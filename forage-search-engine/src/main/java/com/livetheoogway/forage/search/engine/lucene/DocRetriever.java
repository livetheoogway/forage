package com.livetheoogway.forage.search.engine.lucene;

import lombok.SneakyThrows;
import lombok.Value;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

@Value
public class DocRetriever {
    IndexReader indexReader;
    IndexSearcher searcher;

    @SneakyThrows
    Document document(int docId) {
        return searcher.doc(docId);
    }
}
