package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.search.engine.model.Book;
import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;
import java.util.function.Function;

@UtilityClass
public class ResultUtil {
    public <T, R> R getRepresentation(final ForageQueryResult<T> result,
                                      final Function<MatchingResult<T>, R> mapper,
                                      final BinaryOperator<R> reducer) {
        return result.getMatchingResults()
                .stream()
                .map(mapper)
                .reduce(reducer)
                .orElse(null);
    }

    public String getBookRepresentation(final ForageQueryResult<Book> result) {
        return getRepresentation(result,
                                 matchingResult -> {
                                     final Book data = matchingResult.getData();
                                     return matchingResult.getId() + ":" + data.getTitle();
                                 },
                                 (a, b) -> a + "\n" + b);
    }
}
