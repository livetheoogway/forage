package com.livetheoogway.forage.dropwizard.bundle;

import com.livetheoogway.forage.core.AsyncQueuedConsumer;
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.lucene.ForageEngineIndexer;
import com.livetheoogway.forage.search.engine.lucene.ForageSearchEngineBuilder;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Environment;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

public abstract class ForageBundle<T extends Configuration, D> implements ConfiguredBundle<T> {

    @Getter
    private ForageSearchEngine<D> searchEngine;

    public abstract Store<D> dataStore(final T configuration);

    public abstract Bootstrapper<IndexableDocument> bootstrap(final T configuration);

    public abstract ForageConfiguration forageConfiguration(final T configuration);

    @Override
    public void run(final T configuration, final Environment environment) {
        final ForageSearchEngineBuilder<D> engineBuilder = ForageSearchEngineBuilder.<D>builder()
                .withObjectMapper(environment.getObjectMapper())
                .withDataStore(dataStore(configuration));

        final ForageEngineIndexer<D> forageEngineIndexer = new ForageEngineIndexer<>(engineBuilder);
        this.searchEngine = forageEngineIndexer;

        final PeriodicUpdateEngine<IndexableDocument> updateEngine =
                new PeriodicUpdateEngine<>(
                        bootstrap(configuration),
                        new AsyncQueuedConsumer<>(forageEngineIndexer),
                        forageConfiguration(configuration).getRefreshIntervalInSeconds(),
                        TimeUnit.SECONDS);

        updateEngine.start();
    }
}