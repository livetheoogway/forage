package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.core.AsyncQueuedConsumer;
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.TextField;
import com.livetheoogway.forage.search.engine.lucene.ForageEngineIndexer;
import com.livetheoogway.forage.search.engine.lucene.ForageSearchEngineBuilder;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchApp {

    public static void main(String[] args) throws Exception {
        BookStore store = new BookStore();
        store.addBook(new Book("1", "Effective Java", "Joshua Bloch",
                                   "Best practices for Java programming", 4.7f, 416));
        store.addBook(new Book("2", "Clean Code", "Robert Martin",
                                   "A handbook of agile software craftsmanship", 4.4f, 464));
        store.addBook(new Book("3", "Design Patterns", "Gang of Four",
                                   "Elements of reusable object-oriented software", 4.5f, 395));


        ForageEngineIndexer<Book> engine = new ForageEngineIndexer<>(
                ForageSearchEngineBuilder.<Book>builder()
                        .withDataStore(store)
                        .withObjectMapper(TestUtils.mapper()));

        // Periodic Update Engine to refresh index every second. Ideally this should be initialized as a singleton,
        // and started once. We are directly bootstrapping it here for simplicity.
        var updateEngine = new PeriodicUpdateEngine<>(store, new AsyncQueuedConsumer<>(engine),
                                                      1, TimeUnit.SECONDS);
        updateEngine.bootstrap();

        // once the engine is started, you can run queries against it
        var results = engine.search(QueryBuilder.matchQuery("author", "bloch")
                                            .buildForageQuery(10));

        results.getMatchingResults()
                .forEach(book -> System.out.println(book.getData().getTitle() + " - " + book.getDocScore().getScore()));

        engine.get().close();

        System.out.println("=== Search for 'java' ===");
        results = engine.search(
                QueryBuilder.matchQuery("title", "java").buildForageQuery(10)
        );
        printResults(results);

        // Boolean query
        System.out.println("\n=== Search for books by 'martin' OR about 'patterns' ===");
        results = engine.search(
                QueryBuilder.booleanQuery()
                        .query(QueryBuilder.matchQuery("author", "martin").build())
                        .query(QueryBuilder.matchQuery("title", "patterns").build())
                        .clauseType(ClauseType.SHOULD)
                        .buildForageQuery(10)
        );
        printResults(results);

        // Range query
        System.out.println("\n=== Books with rating >= 4.5 ===");
        results = engine.search(
                QueryBuilder.floatRangeQuery("rating", 4.5f, 5.0f).buildForageQuery(10)
        );
        printResults(results);

        // Function score query - rank by rating
        System.out.println("\n=== All books ranked by rating ===");
        results = engine.search(
                QueryBuilder.functionScoreQuery()
                        .baseQuery(QueryBuilder.matchAllQuery().build())
                        .fieldValueFactor("rating")
                        .buildForageQuery(10)
        );
        printResults(results);
    }

    private static void printResults(ForageQueryResult<Book> results) {
        System.out.println("Found " + results.getTotal().getTotal() + " results:");
        for (MatchingResult<Book> match : results.getMatchingResults()) {
            Book book = match.getData();
            System.out.printf("  [%.2f] %s by %s (%.1f stars)%n",
                              match.getDocScore().getScore(),
                              book.getTitle(),
                              book.getAuthor(),
                              book.getRating()
            );
        }
    }

    @Data
    @AllArgsConstructor
    static class Book {
        String id, title, author, description;
        float rating;
        int numPages;
    }

    static class BookStore implements Bootstrapper<IndexableDocument>, Store<Book> {
        private final Map<String, Book> books = new HashMap<>();

        void addBook(Book book) {
            books.put(book.getId(), book);
        }

        @Override
        public void bootstrap(Consumer<IndexableDocument> consumer) {
            books.values().forEach(book -> consumer.accept(new ForageDocument(
                    book.getId(), Arrays.asList(
                    new TextField("title", book.getTitle()),
                    new TextField("author", book.getAuthor()),
                    new FloatField("rating", new float[]{book.getRating()})
            ))));
        }

        @Override
        public Map<String, Book> get(List<String> ids) {
            return ids.stream().filter(books::containsKey)
                    .collect(Collectors.toMap(id -> id, books::get));
        }
    }
}