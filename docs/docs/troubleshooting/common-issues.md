# Common Issues

This page covers frequently encountered issues and their solutions.

## No Results Returned

### Symptom
Search returns empty results even though data exists.

### Possible Causes & Solutions

**1. Bootstrap not completed**
```java
// Ensure bootstrap completes before searching
updateEngine.bootstrap();
Thread.sleep(1000);  // Wait for indexing

// Or use proper synchronization
updateEngine.bootstrap();
```

**2. Field name mismatch**
```java
// Document indexed with "bookTitle"
new TextField("bookTitle", book.getTitle())

// But searching "title" - won't match!
QueryBuilder.matchQuery("title", searchTerm)  // Wrong field

// Fix: Use correct field name
QueryBuilder.matchQuery("bookTitle", searchTerm)  // Correct
```

**3. Case sensitivity on StringField**
```java
// StringField is case-sensitive
new StringField("category", "Technology")

// Won't match lowercase
QueryBuilder.matchQuery("category", "technology")  // No match

// Fix: Normalize during indexing
new StringField("category", book.getCategory().toLowerCase())
```

**4. Using TextField for exact match**
```java
// TextField tokenizes - "978-0134685991" becomes ["978", "0134685991"]
new TextField("isbn", book.getIsbn())

// Searching full ISBN won't work as expected
QueryBuilder.matchQuery("isbn", "978-0134685991")

// Fix: Use StringField for exact match
new StringField("isbn", book.getIsbn())
```

## Unexpected Search Results

### Wrong documents returned

**Cause**: Field type mismatch

```java
// Problem: Searching numeric field as text
new IntField("year", new int[]{2023})
QueryBuilder.matchQuery("year", "2023")  // Text search on numeric field

// Fix: Use range query for numeric fields
QueryBuilder.intRangeQuery("year", 2023, 2023)
```

### Results not in expected order

**Cause**: Missing sort criteria

```java
// Default sort is by relevance score
QueryBuilder.matchQuery("title", "java").buildForageQuery()

// To sort by specific field:
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10, Arrays.asList(
        new SortCriteria("rating", SortOrder.DESC)
    ))
```

## NaN Scores

### Symptom
Document scores show as `NaN` (Not a Number).

### Cause
Function score referencing missing or null field values.

```java
// Document missing "rating" field
new ForageDocument(id, data, Arrays.asList(
    new TextField("title", title)
    // No rating field!
));

// Function score on missing field = NaN
QueryBuilder.functionScoreQuery()
    .baseQuery(...)
    .fieldValueFactor("rating")  // Field doesn't exist
```

### Solution
Ensure all documents have the required fields:

```java
new ForageDocument(id, data, Arrays.asList(
    new TextField("title", title),
    new FloatField("rating", new float[]{
        book.getRating() != null ? book.getRating() : 0.0f
    })
));
```

## OutOfMemoryError

### Symptom
```
java.lang.OutOfMemoryError: Java heap space
```

### Solutions

**1. Increase heap size**
```bash
java -Xmx4g -jar myapp.jar
```

**2. Reduce indexed fields**
```java
// Only index what you search
new ForageDocument(id, data, Arrays.asList(
    new TextField("title", title),    // Needed
    new TextField("author", author)   // Needed
    // Remove unnecessary fields
));
```

**3. Check for memory leaks in bootstrap**
```java
// Bad: Loading all into memory
List<Book> allBooks = repository.findAll();  // OOM risk

// Good: Stream processing
try (Stream<Book> stream = repository.streamAll()) {
    stream.forEach(book -> consumer.accept(createDocument(book)));
}
```

## Slow Queries

### Symptom
Queries taking longer than expected.

### Solutions

**1. Use specific queries**
```java
// Slow: Searching all fields
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("field1", term).build())
    .query(QueryBuilder.matchQuery("field2", term).build())
    .query(QueryBuilder.matchQuery("field3", term).build())
    // ... many more fields

// Fast: Search specific field
QueryBuilder.matchQuery("title", term)
```

**2. Avoid short prefixes**
```java
// Slow: Very short prefix
QueryBuilder.prefixMatchQuery("title", "a")

// Fast: Longer prefix
QueryBuilder.prefixMatchQuery("title", "prog")
```

**3. Use FILTER for non-scoring clauses**
```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).build())
    .query(QueryBuilder.matchQuery("category", "books")
        .clauseType(ClauseType.FILTER).build())  // Faster than MUST
    .clauseType(ClauseType.MUST)
```

## Bootstrap Failures

### Symptom
Bootstrap throws exceptions or doesn't complete.

### Solutions

**1. Handle exceptions in bootstrap**
```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    for (Book book : repository.findAll()) {
        try {
            consumer.accept(createDocument(book));
        } catch (Exception e) {
            log.error("Failed to index book {}: {}", book.getId(), e.getMessage());
            // Continue with next document
        }
    }
}
```

**2. Check database connectivity**
```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    try {
        repository.findAll().forEach(book ->
            consumer.accept(createDocument(book))
        );
    } catch (DataAccessException e) {
        log.error("Database error during bootstrap", e);
        throw new BootstrapException("Failed to bootstrap", e);
    }
}
```

## Stale Data

### Symptom
Search returns outdated data.

### Cause
Bootstrap interval too long or bootstrap not running.

### Solutions

**1. Verify periodic updates are running**
```java
// Make sure to start the update engine
updateEngine.bootstrap();
updateEngine.start();  // Don't forget this!
```

**2. Reduce update interval**
```java
new PeriodicUpdateEngine<>(
    bootstrapper,
    consumer,
    30,  // Reduce from 60 to 30 seconds
    TimeUnit.SECONDS
);
```

**3. Trigger manual refresh**
```java
// Force immediate re-bootstrap
updateEngine.bootstrap();
```

## ClassCastException

### Symptom
```
java.lang.ClassCastException: cannot cast ... to ...
```

### Cause
Type mismatch in Store implementation.

```java
// Store returns wrong type
public Map<String, Book> get(List<String> ids) {
    return ids.stream()
        .collect(Collectors.toMap(
            id -> id,
            id -> someOtherTypeObject  // Wrong type!
        ));
}
```

### Solution
Ensure Store returns correct types:

```java
public Map<String, Book> get(List<String> ids) {
    return ids.stream()
        .filter(books::containsKey)
        .collect(Collectors.toMap(
            id -> id,
            id -> books.get(id)  // Correct Book type
        ));
}
```

## Next Steps

- [Best Practices](best-practices.md) - Avoid common pitfalls
- [Performance](../advanced/performance.md) - Optimization guide
