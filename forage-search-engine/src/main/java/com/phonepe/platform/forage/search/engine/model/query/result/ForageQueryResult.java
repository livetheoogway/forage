package com.phonepe.platform.forage.search.engine.model.query.result;

import com.phonepe.platform.forage.search.engine.model.query.search.MatchingResult;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ForageQueryResult{
    List<MatchingResult> matchingResults;
    TotalResults total;
    String nextPage;
}