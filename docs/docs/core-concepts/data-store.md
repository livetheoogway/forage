# Data Store

The `Store` interface is responsible for retrieving full data objects when search results are returned. While the Lucene index stores document IDs and indexed fields, the `Store` provides the complete data.

## The Store Interface

```java
public interface Store<D> {
    Map<String, D> get(List<String> ids);
}
```

### Parameters

- `ids`: List of document IDs that matched the search query
- Returns: Map of ID to full data object

## Basic Implementation

```java
public class BookStore implements Store<Book> {

    private final Map<String, Book> booksCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Book> get(List<String> ids) {
        Map<String, Book> results = new HashMap<>();
        for (String id : ids) {
            Book book = booksCache.get(id);
            if (book != null) {
                results.put(id, book);
            }
        }
        return results;
    }
}
```

## Database-Backed Implementation

In production, you typically fetch from a database:

```java
public class BookStore implements Store<Book> {

    private final BookRepository repository;  // Your JPA/JDBC repository

    public BookStore(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, Book> get(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        // Batch fetch from database
        List<Book> books = repository.findAllById(ids);

        return books.stream()
            .collect(Collectors.toMap(Book::getId, book -> book));
    }
}
```

## Caching Considerations

Since `Store.get()` is called for every search result, consider caching:

### Option 1: In-Memory Cache

```java
public class CachedBookStore implements Store<Book> {

    private final BookRepository repository;
    private final Cache<String, Book> cache;

    public CachedBookStore(BookRepository repository) {
        this.repository = repository;
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public Map<String, Book> get(List<String> ids) {
        Map<String, Book> results = new HashMap<>();
        List<String> missingIds = new ArrayList<>();

        // Check cache first
        for (String id : ids) {
            Book cached = cache.getIfPresent(id);
            if (cached != null) {
                results.put(id, cached);
            } else {
                missingIds.add(id);
            }
        }

        // Fetch missing from database
        if (!missingIds.isEmpty()) {
            List<Book> fromDb = repository.findAllById(missingIds);
            for (Book book : fromDb) {
                results.put(book.getId(), book);
                cache.put(book.getId(), book);
            }
        }

        return results;
    }
}
```

### Option 2: Full In-Memory Store

For small datasets, keep everything in memory:

```java
public class InMemoryBookStore implements Bootstrapper<IndexableDocument>, Store<Book> {

    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final BookRepository repository;

    @Override
    public void bootstrap(Consumer<IndexableDocument> consumer) {
        // Load all books into memory during bootstrap
        List<Book> allBooks = repository.findAll();
        for (Book book : allBooks) {
            books.put(book.getId(), book);
            consumer.accept(createDocument(book));
        }
    }

    @Override
    public Map<String, Book> get(List<String> ids) {
        // Fast in-memory lookup
        return ids.stream()
            .filter(books::containsKey)
            .collect(Collectors.toMap(id -> id, books::get));
    }
}
```

## Handling Missing Data

What happens when a document ID exists in the index but not in the store?

```java
@Override
public Map<String, Book> get(List<String> ids) {
    Map<String, Book> results = new HashMap<>();

    for (String id : ids) {
        Book book = fetchFromDatabase(id);
        if (book != null) {
            results.put(id, book);
        } else {
            // Option 1: Log and skip
            log.warn("Book not found for id: {}", id);

            // Option 2: Return a placeholder
            // results.put(id, Book.notFound(id));
        }
    }

    return results;
}
```

!!! warning "Index-Store Consistency"
    If data is deleted from your database between bootstrap cycles, search results may include stale IDs. Handle this gracefully in your `Store` implementation.

## Combining Bootstrapper and Store

Typically, one class implements both interfaces:

```java
public class BookDataStore implements Bootstrapper<IndexableDocument>, Store<Book> {

    private final BookRepository repository;
    private final Map<String, Book> cache = new ConcurrentHashMap<>();

    public BookDataStore(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void bootstrap(Consumer<IndexableDocument> consumer) {
        // Clear and rebuild cache during bootstrap
        cache.clear();

        repository.findAll().forEach(book -> {
            cache.put(book.getId(), book);
            consumer.accept(createDocument(book));
        });
    }

    @Override
    public Map<String, Book> get(List<String> ids) {
        // Use cache populated during bootstrap
        return ids.stream()
            .filter(cache::containsKey)
            .collect(Collectors.toMap(id -> id, cache::get));
    }

    private ForageDocument createDocument(Book book) {
        return new ForageDocument(book.getId(), Arrays.asList(
            new TextField("title", book.getTitle()),
            new TextField("author", book.getAuthor()),
            new FloatField("rating", new float[]{book.getRating()})
        ));
    }
}
```

## Performance Tips

### 1. Batch Database Queries

```java
// Good: Single batch query
List<Book> books = repository.findAllById(ids);

// Bad: N+1 queries
for (String id : ids) {
    Book book = repository.findById(id);
}
```

### 2. Limit Result Size

```java
// Limit search results to reduce Store.get() calls
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10)  // Only fetch top 10
```

### 3. Use Projections for Large Objects

```java
// If Book has large fields you don't need in search results
public class BookSummary {
    private String id;
    private String title;
    private String author;
    private float rating;
    // Omit large fields like 'fullContent'
}

public class BookStore implements Store<BookSummary> {
    @Override
    public Map<String, BookSummary> get(List<String> ids) {
        return repository.findSummariesByIds(ids);
    }
}
```

## Next Steps

- [Bootstrapping](bootstrapping.md) - Feed data into Forage
- [Query Types](../queries/index.md) - Search your data
