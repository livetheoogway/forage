package com.livetheoogway.forage.search.engine.lucene.pagination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.livetheoogway.forage.models.result.DocScore;
import lombok.Value;

@Value
public class SearchAfter {
    DocScore after;
    String query;

    @JsonCreator
    public SearchAfter(@JsonProperty("after") final DocScore after, @JsonProperty("query") final String query) {
        this.after = after;
        this.query = query;
    }
}
