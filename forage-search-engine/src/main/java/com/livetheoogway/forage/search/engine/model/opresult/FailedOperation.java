package com.livetheoogway.forage.search.engine.model.opresult;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.Value;

@Value
public class FailedOperation<T> {
    T item;
    ForageSearchError error;
}
