package com.phonepe.platform.forage.models.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class DocScore {
    float score;
    int doc;
    int shardIndex;

    @JsonCreator
    public DocScore(@JsonProperty("score") final float score, @JsonProperty("doc") final int doc,
                    @JsonProperty("shardIndexZ") final int shardIndex) {
        this.score = score;
        this.doc = doc;
        this.shardIndex = shardIndex;
    }
}
