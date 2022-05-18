package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;

public interface Pagination<T, P> {
    P generatePage(T t) throws ForageSearchError;

    T parsePage(P r) throws ForageSearchError;
}
