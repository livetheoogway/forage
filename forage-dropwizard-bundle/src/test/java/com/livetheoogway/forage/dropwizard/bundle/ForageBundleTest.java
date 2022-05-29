package com.livetheoogway.forage.dropwizard.bundle;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.dropwizard.bundle.model.Book;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.util.QueryBuilder;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Environment;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForageBundleTest {
    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final LifecycleEnvironment lifecycleEnvironment = new LifecycleEnvironment(metricRegistry);
    private final Environment environment = mock(Environment.class);
    private final AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);

    @BeforeEach
    public void setUp() {
        when(jerseyEnvironment.getResourceConfig()).thenReturn(new DropwizardResourceConfig());
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.admin()).thenReturn(adminEnvironment);
        when(environment.getObjectMapper()).thenReturn(new ObjectMapper());
        when(environment.metrics()).thenReturn(metricRegistry);
    }

    @Test
    void testBundleExecution() throws ForageSearchError {
        final BookStore store = new BookStore();
        final ForageBundle<SampleConfig, Book> bundle = new ForageBundle<>() {

            @Override
            public Store<Book> dataStore(final SampleConfig configuration) {
                return store;
            }

            @Override
            public Bootstrapper<IndexableDocument> bootstrap(final SampleConfig configuration) {
                return store;
            }

            @Override
            public ForageConfiguration forageConfiguration(final SampleConfig configuration) {
                return new ForageConfiguration(1);
            }
        };

        bundle.run(new SampleConfig(), environment);

        final ForageSearchError error = Assertions.assertThrows(
                ForageSearchError.class,
                () -> bundle.getSearchEngine().search(QueryBuilder.matchQuery("author", "rowling").build()));
        Assertions.assertEquals(ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET, error.getForageErrorCode());

        store.put(new Book("id1", "Harry Potter and the Half-Blood Prince", "J.K. Rowling", 4.57f, "eng", 652));

        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> {
                    try {
                        final ForageQueryResult<Book> query2 = bundle.getSearchEngine().search(
                                QueryBuilder.matchQuery("author", "rowling").build());
                        return query2.getTotal().getTotal() == 1;
                    } catch (ForageSearchError e) {
                        if (e.getForageErrorCode() != ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET) {
                            throw e;
                        }
                        return false;
                    }
                });
        final ForageQueryResult<Book> results = bundle.getSearchEngine().search(
                QueryBuilder.matchQuery("author", "rowling").build());
        Assertions.assertEquals("id1", results.getMatchingResults().get(0).getId());
        Assertions.assertEquals("Harry Potter and the Half-Blood Prince", results.getMatchingResults()
                .get(0)
                .getData()
                .getTitle());
    }

    private static class BookStore implements Store<Book>, Bootstrapper<IndexableDocument> {
        private final HashMap<String, Book> books = new HashMap<>();

        public void put(Book book) {
            books.put(book.getId(), book);
        }

        @Override
        public Book get(final String id) {
            return books.get(id);
        }

        @Override
        public void bootstrap(final Consumer<IndexableDocument> itemConsumer) {
            books.forEach((key, value) -> itemConsumer.accept(new ForageDocument(key, value.fields())));
        }
    }

    private static class SampleConfig extends Configuration {
    }
}