package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.opresult.OperationResult;

import java.util.Collections;
import java.util.List;

public interface DocumentIndexer<D> {
    default OperationResult<D> index(D document) throws ForageSearchError {
        return index(Collections.singletonList(document));
    }

    OperationResult<D> index(List<D> document) throws ForageSearchError;

    void flush() throws ForageSearchError;
}
