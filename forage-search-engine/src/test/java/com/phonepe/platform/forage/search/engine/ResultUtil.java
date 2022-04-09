package com.phonepe.platform.forage.search.engine;

import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.models.result.MatchingResult;
import com.phonepe.platform.forage.search.engine.model.Book;
import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;
import java.util.function.Function;

@UtilityClass
public class ResultUtil {
    public <T> T getRepresentation(final ForageQueryResult result,
                                   final Function<MatchingResult, T> mapper,
                                   final BinaryOperator<T> reducer) {
        return result.getMatchingResults()
                .stream()
                .map(mapper)
                .reduce(reducer)
                .orElse(null);
    }

    public String getBookRepresentation(final ForageQueryResult result) {
        return getRepresentation(result,
                                 matchingResult -> {
                                     final Book data = (Book) matchingResult.getData();
                                     return matchingResult.getId()+ ":" + data.getTitle();
                                 },
                                 (a, b) -> a + "\n" + b);
    }
}
