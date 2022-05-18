package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;

public interface QueryEngine<Q, R> {
    R query(Q query) throws ForageSearchError;
}
