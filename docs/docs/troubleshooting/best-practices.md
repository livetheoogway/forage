# Best Practices

Follow these best practices to build robust, performant search functionality with Forage.

## Indexing Best Practices

### 1. Index Only What You Search

```java
// Good: Minimal, purposeful fields
new ForageDocument(book.getId(), book, Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor()),
    new FloatField("rating", new float[]{book.getRating()})
));

// Avoid: Indexing everything
new ForageDocument(book.getId(), book, Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor()),
    new TextField("description", book.getDescription()),
    new TextField("tableOfContents", book.getToc()),      // Never searched
    new TextField("publisherNotes", book.getNotes()),     // Never searched
    new TextField("internalComments", book.getComments()) // Never searched
));
```

### 2. Use Correct Field Types

| Data | Field Type | Reason |
|------|------------|--------|
| Searchable text | `TextField` | Tokenized for full-text search |
| IDs, codes, exact values | `StringField` | Not tokenized, exact match |
| Numbers for filtering/sorting | `IntField`/`FloatField` | Range queries, sorting |
| Numbers for scoring | `FloatField` | Function score access |

```java
new ForageDocument(id, data, Arrays.asList(
    new TextField("title", title),           // Full-text search
    new StringField("isbn", isbn),           // Exact match
    new StringField("status", status),       // Exact match
    new IntField("year", new int[]{year}),   // Range queries
    new FloatField("rating", new float[]{rating}) // Sorting, scoring
));
```

### 3. Handle Null Values

```java
private ForageDocument createDocument(Book book) {
    List<Field> fields = new ArrayList<>();

    // Required fields
    fields.add(new TextField("title",
        book.getTitle() != null ? book.getTitle() : ""));

    // Optional fields - only add if present
    if (book.getDescription() != null && !book.getDescription().isEmpty()) {
        fields.add(new TextField("description", book.getDescription()));
    }

    // Numeric fields - use defaults for null
    fields.add(new FloatField("rating", new float[]{
        book.getRating() != null ? book.getRating() : 0.0f
    }));

    return new ForageDocument(book.getId(), book, fields);
}
```

### 4. Normalize Data

```java
// Normalize text for consistent searching
fields.add(new TextField("title", book.getTitle().trim()));
fields.add(new StringField("category", book.getCategory().toLowerCase()));
```

## Query Best Practices

### 1. Be Specific

```java
// Good: Search specific field
QueryBuilder.matchQuery("title", searchTerm)

// Less optimal: Search many fields
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("author", searchTerm).build())
    .query(QueryBuilder.matchQuery("description", searchTerm).build())
    .clauseType(ClauseType.SHOULD)
```

### 2. Use Filters for Non-Scoring Clauses

```java
// Good: FILTER for non-scoring conditions
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("category", "technology")
        .clauseType(ClauseType.FILTER).build())  // No scoring overhead
    .clauseType(ClauseType.MUST)

// Less optimal: MUST computes unnecessary scores
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.matchQuery("category", "technology").build())
    .clauseType(ClauseType.MUST)
```

### 3. Limit Results

```java
// Good: Reasonable limit
QueryBuilder.matchQuery("title", searchTerm).buildForageQuery(20)

// Avoid: Excessive results
QueryBuilder.matchQuery("title", searchTerm).buildForageQuery(10000)
```

### 4. Use Minimum Score for Quality

```java
// Filter out weak matches
QueryBuilder.matchQuery("title", searchTerm)
    .buildForageQuery(20, null, 0.3f)
```

## Scoring Best Practices

### 1. Start Simple, Add Complexity

```java
// Start with basic search
QueryBuilder.matchQuery("title", searchTerm)

// Then add boosting if needed
QueryBuilder.matchQuery("title", searchTerm).boost(2.0f)

// Then add function scoring if needed
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", searchTerm).build())
    .fieldValueFactor("rating")
```

### 2. Document Your Boosts

```java
// Documented boost strategy
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term)
        .boost(3.0f).build())       // Title: highest priority
    .query(QueryBuilder.matchQuery("author", term)
        .boost(2.0f).build())       // Author: high priority
    .query(QueryBuilder.matchQuery("description", term)
        .boost(1.0f).build())       // Description: baseline
    .clauseType(ClauseType.SHOULD)
```

### 3. Test with Real Data

```java
// Verify ranking with actual queries
@Test
void testSearchRanking() {
    var results = engine.search(
        QueryBuilder.matchQuery("title", "java").buildForageQuery(10)
    );

    // Verify expected order
    assertEquals("Effective Java", results.getMatchingResults().get(0).getData().getTitle());
}
```

## Performance Best Practices

### 1. Size Heap Appropriately

```bash
# Rule of thumb: 2-4x raw data size
java -Xmx4g -jar myapp.jar
```

### 2. Stream Large Datasets

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    // Good: Stream processing
    try (Stream<Book> stream = repository.streamAll()) {
        stream.forEach(book -> consumer.accept(createDocument(book)));
    }

    // Avoid: Loading all into memory
    // List<Book> all = repository.findAll();
}
```

### 3. Cache Frequent Queries

```java
private final Cache<String, ForageQueryResult<Book>> cache =
    CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();
```

### 4. Monitor Performance

```java
public ForageQueryResult<Book> search(String query) {
    long start = System.nanoTime();
    var results = engine.search(buildQuery(query));
    long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

    if (elapsed > 100) {
        log.warn("Slow query: '{}' took {}ms", query, elapsed);
    }

    return results;
}
```

## Error Handling Best Practices

### 1. Handle Bootstrap Errors Gracefully

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    int errors = 0;
    for (Book book : repository.findAll()) {
        try {
            consumer.accept(createDocument(book));
        } catch (Exception e) {
            errors++;
            log.error("Failed to index {}: {}", book.getId(), e.getMessage());
        }
    }
    if (errors > 0) {
        log.warn("Bootstrap completed with {} errors", errors);
    }
}
```

### 2. Handle Missing Store Data

```java
@Override
public Map<String, Book> get(List<String> ids) {
    Map<String, Book> results = new HashMap<>();
    for (String id : ids) {
        Book book = repository.findById(id).orElse(null);
        if (book != null) {
            results.put(id, book);
        } else {
            log.warn("Book not found in store: {}", id);
        }
    }
    return results;
}
```

### 3. Validate Queries

```java
public ForageQueryResult<Book> search(String query) {
    if (query == null || query.trim().isEmpty()) {
        return ForageQueryResult.empty();
    }

    if (query.length() > 1000) {
        throw new IllegalArgumentException("Query too long");
    }

    return engine.search(buildQuery(query.trim()));
}
```

## Testing Best Practices

### 1. Test Different Query Types

```java
@Test
void testMatchQuery() { ... }

@Test
void testBooleanQuery() { ... }

@Test
void testRangeQuery() { ... }

@Test
void testFunctionScoreQuery() { ... }
```

### 2. Test Edge Cases

```java
@Test
void testEmptyQuery() { ... }

@Test
void testSpecialCharacters() { ... }

@Test
void testVeryLongQuery() { ... }

@Test
void testNoResults() { ... }
```

### 3. Test Ranking

```java
@Test
void testResultsOrderedByRelevance() {
    // Exact match should rank higher
    var results = engine.search(query);
    assertTrue(results.getMatchingResults().get(0).getDocScore().getScore() >=
               results.getMatchingResults().get(1).getDocScore().getScore());
}
```

## Summary Checklist

- [ ] Index only necessary fields
- [ ] Use correct field types
- [ ] Handle null values
- [ ] Use FILTER for non-scoring clauses
- [ ] Limit result sizes
- [ ] Apply minimum score thresholds
- [ ] Size heap appropriately
- [ ] Stream large datasets
- [ ] Cache frequent queries
- [ ] Handle errors gracefully
- [ ] Write comprehensive tests
