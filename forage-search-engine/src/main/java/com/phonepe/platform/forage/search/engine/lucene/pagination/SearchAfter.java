package com.phonepe.platform.forage.search.engine.lucene.pagination;

import lombok.Value;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

@Value
public class SearchAfter {
    ScoreDoc after;
    Query query;
}
