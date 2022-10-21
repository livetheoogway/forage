/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.dropwizard.bundle;

import com.livetheoogway.forage.core.AsyncQueuedConsumer;
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.core.UpdateEngine;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.lucene.ForageEngineIndexer;
import com.livetheoogway.forage.search.engine.lucene.ForageSearchEngineBuilder;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
public abstract class ForageBundle<T extends Configuration, D> implements ConfiguredBundle<T> {

    /* the reference to the search engine that will get swapped with the actual one on start up */
    private final DelegatedForageSearchEngine<D> delegatedForageSearchEngine
            = new DelegatedForageSearchEngine<>(new ForagePreStartEngine<>());

    private final AtomicReference<PeriodicUpdateEngine<IndexableDocument>> updateEngineRef = new AtomicReference<>();

    /**
     * @param configuration application configuration
     * @return supply the Store that can retrieve data given a document id
     */
    public abstract Store<D> dataStore(final T configuration);

    /**
     * @param configuration application configuration
     * @return Supply the Bootstrapper, that can create a bunch of indexable documents
     */
    public abstract Bootstrapper<IndexableDocument> bootstrap(final T configuration);

    /**
     * @param configuration application configuration
     * @return the forage configuration with refresh details
     */
    public abstract ForageConfiguration forageConfiguration(final T configuration);

    public ForageSearchEngine<D> searchEngine() {
        return delegatedForageSearchEngine;
    }

    /**
     * Use this to do adhoc {@link UpdateEngine#bootstrap()}, other than the periodic bootstrap that happens as part
     * of this bundle.
     *
     * @return supplier of the {@link UpdateEngine}
     */
    public Supplier<UpdateEngine<IndexableDocument>> updateEngine() {
        return updateEngineRef::get;
    }

    @Override
    public void run(final T configuration, final Environment environment) {
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
                log.info("[forage][startup] Starting the engine...");
                final ForageSearchEngineBuilder<D> engineBuilder = ForageSearchEngineBuilder.<D>builder()
                        .withObjectMapper(environment.getObjectMapper())
                        .withDataStore(dataStore(configuration));

                final ForageEngineIndexer<D> forageEngineIndexer = new ForageEngineIndexer<>(engineBuilder);

                delegatedForageSearchEngine.onStart(forageEngineIndexer);

                final PeriodicUpdateEngine<IndexableDocument> updateEngine = new PeriodicUpdateEngine<>(
                        bootstrap(configuration),
                        new AsyncQueuedConsumer<>(forageEngineIndexer),
                        forageConfiguration(configuration).getRefreshIntervalInSeconds(),
                        TimeUnit.SECONDS);

                updateEngineRef.set(updateEngine);

                updateEngineRef.get().start();
                log.info("[forage][startup] .. Done starting engine and setting up periodic updates");
            }

            @Override
            public void stop() {
                updateEngineRef.get().stop();
            }
        });
    }
}