# Quick Start

This guide walks you through building a searchable book catalog in under 10 minutes.

## Step 1: Define Your Data Model

```java
@Data
@AllArgsConstructor
public class Book {
    private String id;
    private String title;
    private String author;
    private String description;
    private float rating;
    private int numPages;
}
```

## Step 2: Implement the Data Store

!!! warning "Read about the [Bootstrapper](../core-concepts/bootstrapping.md) to implement this correctly"

Create a class that implements both `Bootstrapper` and `Store`:

```java
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.search.engine.store.Store;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.field.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BookStore implements Bootstrapper<IndexableDocument>, Store<Book> {

    // In production, this would be your database connection
    private final Map<String, Book> books = new HashMap<>();

    public void addBook(Book book) {
        books.put(book.getId(), book);
    }

    @Override
    public void bootstrap(Consumer<IndexableDocument> consumer) {
        // Iterate through all books and create indexable documents
        // this is where you would pull data from your actual datastore. (like a `select * from books`)
        for (Book book : books.values()) {
            consumer.accept(new ForageDocument(
                book.getId(),
                Arrays.asList(
                    // Text fields for full-text search
                    new TextField("title", book.getTitle()),
                    new TextField("author", book.getAuthor()),
                    new TextField("description", book.getDescription()),

                    // Numeric fields for filtering and sorting
                    new FloatField("rating", new float[]{book.getRating()}),
                    new IntField("numPages", new int[]{book.getNumPages()})
                )
            ));
        }
    }

    @Override
    public Map<String, Book> get(List<String> ids) {
        // do a bulk fetch from your datastore
        return ids.stream()
            .filter(books::containsKey)
            .collect(Collectors.toMap(id -> id, books::get));
    }
}
```

## Step 3: Initialize the Search Engine

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.core.AsyncQueuedConsumer;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.search.engine.ForageSearchEngineBuilder;
import com.livetheoogway.forage.search.engine.ForageEngine;
import com.livetheoogway.forage.search.engine.SearchEngine;
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;

import java.util.concurrent.TimeUnit;

public class SearchApp {

    private final BookStore bookStore;
    private final ForageEngineIndexer<Book> searchEngine;
    private final PeriodicUpdateEngine<IndexableDocument> updateEngine;

    public SearchApp() {
        // Initialize the data store
        this.bookStore = new BookStore();

        // Build the search engine
        this.searchEngine =
                new ForageEngineIndexer<>(
                        ForageSearchEngineBuilder.<Book>builder()
                                .withDataStore(store)
                                .withObjectMapper(TestUtils.mapper()));

        // Set up periodic updates (every 60 seconds or as needed)
        this.updateEngine = new PeriodicUpdateEngine<>(
            bookStore,
            new AsyncQueuedConsumer<>(searchEngine),
            60, TimeUnit.SECONDS
        );
    }

    public void start() {
        // Add some sample data
        bookStore.addBook(new Book("1", "Effective Java", "Joshua Bloch",
            "Best practices for Java programming", 4.7f, 416));
        bookStore.addBook(new Book("2", "Clean Code", "Robert Martin",
            "A handbook of agile software craftsmanship", 4.4f, 464));
        bookStore.addBook(new Book("3", "Design Patterns", "Gang of Four",
            "Elements of reusable object-oriented software", 4.5f, 395));

        // Bootstrap and start periodic updates
        updateEngine.bootstrap();
        updateEngine.start();
    }

    public SearchEngine<ForageQuery, ForageQueryResult<Book>> getSearchEngine() {
        return searchEngine;
    }
}
```

## Step 4: Execute Searches

```java
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.result.MatchingResult;

public class SearchDemo {

    public static void main(String[] args) {
        SearchApp app = new SearchApp();
        app.start();

        // Wait for indexing to complete
        Thread.sleep(1000);

        var engine = app.getSearchEngine();

        // Simple match query
        System.out.println("=== Search for 'java' ===");
        var results = engine.search(
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
        System.out.println("Found " + results.getTotal().getValue() + " results:");
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
}
```

## Expected Output

```
=== Search for 'java' ===
Found 1 results:
  [0.45] Effective Java by Joshua Bloch (4.7 stars)

=== Search for books by 'martin' OR about 'patterns' ===
Found 2 results:
  [0.47] Clean Code by Robert Martin (4.4 stars)
  [0.45] Design Patterns by Gang of Four (4.5 stars)

=== Books with rating >= 4.5 ===
Found 2 results:
  [1.00] Effective Java by Joshua Bloch (4.7 stars)
  [1.00] Design Patterns by Gang of Four (4.5 stars)

=== All books ranked by rating ===
Found 3 results:
  [4.70] Effective Java by Joshua Bloch (4.7 stars)
  [4.50] Design Patterns by Gang of Four (4.5 stars)
  [4.40] Clean Code by Robert Martin (4.4 stars)
```

## Complete Example

Here's the complete working example in a single file:

??? example "Complete SearchApp.java"
    ```java
    import com.livetheoogway.forage.core.AsyncQueuedConsumer;
    import com.livetheoogway.forage.core.Bootstrapper;
    import com.livetheoogway.forage.core.PeriodicUpdateEngine;
    import com.livetheoogway.forage.models.query.util.QueryBuilder;
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
            store.addBook(new Book("1", "Effective Java", "Joshua Bloch", "", 4.7f, 416));
            store.addBook(new Book("2", "Clean Code", "Robert Martin", "", 4.4f, 464));
    
            final var engine =
                    new ForageEngineIndexer<>(
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
    ```

## Next Steps

- [Core Concepts](../core-concepts/index.md) - Understand the building blocks
- [Query Types](../queries/index.md) - Explore all query options
- [Scoring & Ranking](../scoring/index.md) - Customize result ordering
