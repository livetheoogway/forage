package com.livetheoogway.forage.search.engine.util;

import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionExecution {
    public <T> T get(final ESupplier<T> supplier, final ForageErrorCode errorCode) throws ForageSearchError {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new ForageSearchError(errorCode, e);
        }
    }
}