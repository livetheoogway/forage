package com.phonepe.platform.forage.search.engine;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;

public interface Pagination<T, P> {
    P generatePage(T t) throws ForageSearchError;

    T parsePage(P r) throws ForageSearchError;
}
