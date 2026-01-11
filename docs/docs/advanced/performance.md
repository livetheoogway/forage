# Performance

This guide covers optimization strategies for getting the best performance from Forage.

## Memory Management

### Heap Sizing

Forage maintains the index in JVM heap memory. Plan for:

```
Required Heap ≈ Raw Data Size × 2-4
```

| Data Size | Minimum Heap | Recommended Heap |
|-----------|--------------|------------------|
| 10 MB     | 40 MB        | 80 MB            |
| 100 MB    | 400 MB       | 800 MB           |
| 1 GB      | 4 GB         | 8 GB             |

### JVM Configuration

```bash
# For 500K documents (~500MB data)
java -Xms2g -Xmx4g -XX:+UseG1GC -jar myapp.jar
```

### Monitoring Memory

```java
// Log memory usage after bootstrap
Runtime runtime = Runtime.getRuntime();
long usedMemory = runtime.totalMemory() - runtime.freeMemory();
log.info("Index memory usage: {} MB", usedMemory / (1024 * 1024));
```

## Indexing Performance

### Minimize Fields

Only index fields you'll search:

```java
// Good: Minimal fields
new ForageDocument(book.getId(), book, Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor())
));

// Avoid: Unnecessary fields
new ForageDocument(book.getId(), book, Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor()),
    new TextField("internalNotes", book.getInternalNotes()),    // Never searched
    new TextField("legacyDescription", book.getLegacyDesc())    // Never searched
));
```

### Use Appropriate Field Types

```java
// Efficient: Right field type for the job
new StringField("isbn", book.getIsbn()),     // Exact match only
new IntField("year", new int[]{book.getYear()})  // Numeric

// Less efficient: TextField for non-searchable data
new TextField("isbn", book.getIsbn())  // Unnecessarily analyzed
```

### Batch Bootstrap

Stream data efficiently:

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    // Good: Stream processing
    try (Stream<Book> books = repository.streamAll()) {
        books.forEach(book -> consumer.accept(createDocument(book)));
    }

    // Avoid: Loading all into memory first
    // List<Book> allBooks = repository.findAll(); // OOM risk!
}
```

## Query Performance

### Be Specific

```java
// Fast: Specific field
QueryBuilder.matchQuery("title", searchTerm)

// Slower: Multiple fields
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("author", searchTerm).build())
    .query(QueryBuilder.matchQuery("description", searchTerm).build())
    .query(QueryBuilder.matchQuery("tags", searchTerm).build())
    .clauseType(ClauseType.SHOULD)
```

### Use Filters

Filters are cached and faster than scoring queries:

```java
// Fast: FILTER doesn't compute scores
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("category", "technology")
        .clauseType(ClauseType.FILTER).build())  // Cached filter
    .clauseType(ClauseType.MUST)

// Slower: MUST computes scores
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("category", "technology").build())  // Scores computed
    .clauseType(ClauseType.MUST)
```

### Limit Results

```java
// Good: Reasonable limit
QueryBuilder.matchQuery("title", searchTerm).buildForageQuery(20)

// Bad: Excessive results
QueryBuilder.matchQuery("title", searchTerm).buildForageQuery(10000)
```

### Avoid Short Prefixes

```java
// Slow: Very short prefix
QueryBuilder.prefixMatchQuery("title", "a")  // Matches too many

// Fast: Longer prefix
QueryBuilder.prefixMatchQuery("title", "prog")  // More selective
```

## Caching Strategies

### Query Result Caching

```java
private final Cache<String, ForageQueryResult<Book>> queryCache =
    CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

public ForageQueryResult<Book> search(String query) {
    String cacheKey = "search:" + query.toLowerCase();
    return queryCache.get(cacheKey, () -> engine.search(
        QueryBuilder.matchQuery("title", query).buildForageQuery(20)
    ));
}
```

### Autocomplete Caching

```java
private final Cache<String, List<String>> autocompleteCache =
    CacheBuilder.newBuilder()
        .maximumSize(5000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
```

## Bootstrap Optimization

### Parallel Processing

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    List<Book> allBooks = repository.findAll();

    // Process in parallel (consumer is thread-safe)
    allBooks.parallelStream()
        .map(this::createDocument)
        .forEach(consumer);
}
```

### Optimize Update Frequency

```java
// Consider your data freshness requirements
new PeriodicUpdateEngine<>(
    bootstrapper,
    new AsyncQueuedConsumer<>(engine),
    300,  // 5 minutes - balance freshness vs CPU
    TimeUnit.SECONDS
);
```

## Benchmarking

### Query Timing

```java
public ForageQueryResult<Book> searchWithTiming(String query) {
    long start = System.nanoTime();

    ForageQueryResult<Book> results = engine.search(
        QueryBuilder.matchQuery("title", query).buildForageQuery(20)
    );

    long elapsed = System.nanoTime() - start;
    log.debug("Query '{}' took {} ms, found {} results",
        query, elapsed / 1_000_000.0, results.getTotal().getValue());

    return results;
}
```

### Bootstrap Timing

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    long start = System.currentTimeMillis();
    AtomicInteger count = new AtomicInteger();

    repository.streamAll().forEach(book -> {
        consumer.accept(createDocument(book));
        count.incrementAndGet();
    });

    log.info("Bootstrapped {} documents in {} ms",
        count.get(), System.currentTimeMillis() - start);
}
```

## Performance Checklist

- [ ] Heap sized appropriately (2-4x data size)
- [ ] Only necessary fields indexed
- [ ] Using correct field types
- [ ] Result limits applied
- [ ] Filters used where scoring not needed
- [ ] Caching implemented for frequent queries
- [ ] Bootstrap interval tuned for use case
- [ ] Monitoring in place

## Typical Performance

| Operation | Documents | Typical Time |
|-----------|-----------|--------------|
| Simple match query | 100K | < 5ms |
| Boolean query (3 clauses) | 100K | < 10ms |
| Function score query | 100K | < 15ms |
| Full bootstrap | 100K | 5-15 seconds |

## Related Topics

- [Bootstrapping](../core-concepts/bootstrapping.md) - Data loading
- [Architecture](architecture.md) - System internals
