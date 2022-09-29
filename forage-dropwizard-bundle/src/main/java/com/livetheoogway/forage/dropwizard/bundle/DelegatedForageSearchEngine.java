package com.livetheoogway.forage.dropwizard.bundle;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DelegatedForageSearchEngine<D> implements ForageSearchEngine<D> {
    private final AtomicReference<ForageSearchEngine<D>> reference;

    public DelegatedForageSearchEngine(ForageSearchEngine<D> preStart) {
        this.reference = new AtomicReference<>(preStart);
    }

    public void onStart(ForageSearchEngine<D> engine) {
        log.info("[forage] Engine reference swapped.");
        reference.set(engine);
    }

    @Override
    public ForageQueryResult<D> search(final ForageQuery query) throws ForageSearchError {
        return reference.get().search(query);
    }
}
