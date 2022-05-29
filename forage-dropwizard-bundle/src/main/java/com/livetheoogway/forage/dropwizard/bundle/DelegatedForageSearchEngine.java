package com.livetheoogway.forage.dropwizard.bundle;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;

import java.util.concurrent.atomic.AtomicReference;

public class DelegatedForageSearchEngine<D> implements ForageSearchEngine<D> {
    private final AtomicReference<ForageSearchEngine<D>> reference;

    public DelegatedForageSearchEngine(ForageSearchEngine<D> preStart) {
        this.reference = new AtomicReference<>(preStart);
    }

    public void onStart(ForageSearchEngine<D> engine) {
        reference.set(engine);
    }

    @Override
    public ForageQueryResult<D> search(final ForageQuery query) throws ForageSearchError {
        return reference.get().search(query);
    }
}
