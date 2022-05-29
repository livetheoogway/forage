package com.livetheoogway.forage.dropwizard.bundle;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;

public class ForagePreStartEngine<D> implements ForageSearchEngine<D> {
    @Override
    public ForageQueryResult<D> search(final ForageQuery query) throws ForageSearchError {
        throw new ForageSearchError(ForageErrorCode.SEARCH_ENGINE_INITIALIZATION_ERROR,
                                    "Forage bundle is not started yet");
    }
}
