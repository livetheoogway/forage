package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;

public interface SearchEngine<Q, R> {
    R search(Q query) throws ForageSearchError;
}
