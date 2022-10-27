package com.livetheoogway.forage.search.engine.lucene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ForageSearchSetting {
    boolean failOnEmptyBootstrap;

    @JsonCreator
    public ForageSearchSetting(@JsonProperty("failOnEmptyBootstrap") final boolean failOnEmptyBootstrap) {
        this.failOnEmptyBootstrap = failOnEmptyBootstrap;
    }
}
