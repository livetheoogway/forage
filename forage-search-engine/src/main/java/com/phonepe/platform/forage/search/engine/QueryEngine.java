package com.phonepe.platform.forage.search.engine;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;

public interface QueryEngine<Q, R> {
    R query(Q query) throws ForageSearchError;
}
