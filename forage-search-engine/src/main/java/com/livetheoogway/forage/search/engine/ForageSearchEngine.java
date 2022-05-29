package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;

public interface ForageSearchEngine<D> extends SearchEngine<ForageQuery, ForageQueryResult<D>> {
}
