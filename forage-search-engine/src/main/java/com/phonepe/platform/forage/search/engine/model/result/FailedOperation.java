package com.phonepe.platform.forage.search.engine.model.result;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import lombok.Value;

@Value
public class FailedOperation<T> {
    T item;
    ForageSearchError error;
}
