package com.phonepe.platform.forage.models.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ForageQueryResult<D> {
    TotalResults total;
    List<MatchingResult<D>> matchingResults;
    String nextPage;

    @JsonCreator
    public ForageQueryResult(@JsonProperty("total") final TotalResults total,
                             @JsonProperty("matchingResults") final List<MatchingResult<D>> matchingResults,
                             @JsonProperty("nextPage") final String nextPage) {
        this.total = total;
        this.matchingResults = matchingResults;
        this.nextPage = nextPage;
    }
}