# Advanced Usage

This section covers advanced topics for getting the most out of Forage.

## Topics

| Topic | Description |
|-------|-------------|
| [Pagination](pagination.md) | Efficiently navigate large result sets |
| [Performance](performance.md) | Optimization tips and best practices |
| [Architecture](architecture.md) | Deep dive into Forage internals |

## Advanced Patterns

### Custom Analyzer

Forage uses Lucene's `StandardAnalyzer` by default. For custom analysis:

```java
// Create engine with custom analyzer
Analyzer customAnalyzer = new EnglishAnalyzer();

ForageSearchEngineBuilder<Book> builder = ForageSearchEngineBuilder.<Book>builder()
    .withDataStore(dataStore)
    .withObjectMapper(new ObjectMapper())
    .withAnalyzer(customAnalyzer);
```

### Multi-Tenant Search

Implement tenant isolation using filters:

```java
public ForageQueryResult<Document> searchForTenant(String tenantId, String query) {
    return engine.search(
        QueryBuilder.booleanQuery()
            .query(QueryBuilder.matchQuery("content", query).build())
            .query(QueryBuilder.matchQuery("tenantId", tenantId)
                .clauseType(ClauseType.FILTER).build())
            .clauseType(ClauseType.MUST)
            .buildForageQuery(20)
    );
}
```

### Faceted Search

Implement faceted search by running parallel queries:

```java
public SearchResponse searchWithFacets(String query) {
    // Main search
    ForageQueryResult<Product> results = engine.search(
        QueryBuilder.matchQuery("name", query).buildForageQuery(20)
    );

    // Category facets (run in parallel)
    Map<String, Long> categoryFacets = Arrays.asList("electronics", "books", "clothing")
        .parallelStream()
        .collect(Collectors.toMap(
            cat -> cat,
            cat -> countCategory(query, cat)
        ));

    return new SearchResponse(results, categoryFacets);
}

private long countCategory(String query, String category) {
    return engine.search(
        QueryBuilder.booleanQuery()
            .query(QueryBuilder.matchQuery("name", query).build())
            .query(QueryBuilder.matchQuery("category", category).build())
            .clauseType(ClauseType.MUST)
            .buildForageQuery(1)
    ).getTotal().getValue();
}
```

### Search Suggestions

Implement "did you mean" suggestions:

```java
public List<String> getSuggestions(String query, String field) {
    // Try prefix match first
    ForageQueryResult<Document> prefixResults = engine.search(
        QueryBuilder.prefixMatchQuery(field, query).buildForageQuery(5)
    );

    if (!prefixResults.getMatchingResults().isEmpty()) {
        return extractFieldValues(prefixResults, field);
    }

    // Fall back to fuzzy
    ForageQueryResult<Document> fuzzyResults = engine.search(
        QueryBuilder.fuzzyMatchQuery(field, query).buildForageQuery(5)
    );

    return extractFieldValues(fuzzyResults, field);
}
```

### Conditional Boosting

Apply business rules to boost specific content:

```java
public ForageQuery buildSmartQuery(String searchTerm, User user) {
    var queryBuilder = QueryBuilder.booleanQuery()
        .query(QueryBuilder.matchQuery("title", searchTerm).build());

    // Boost premium content for premium users
    if (user.isPremium()) {
        queryBuilder.query(QueryBuilder.matchQuery("tier", "premium")
            .boost(2.0f).clauseType(ClauseType.SHOULD).build());
    }

    // Boost user's preferred categories
    for (String category : user.getPreferredCategories()) {
        queryBuilder.query(QueryBuilder.matchQuery("category", category)
            .boost(1.5f).clauseType(ClauseType.SHOULD).build());
    }

    return queryBuilder.clauseType(ClauseType.MUST).buildForageQuery(20);
}
```

### Hybrid Scoring

Combine multiple scoring strategies:

```java
// Step 1: Get relevance-based results
ForageQueryResult<Product> relevanceResults = engine.search(
    QueryBuilder.matchQuery("name", query).buildForageQuery(100)
);

// Step 2: Re-rank with business logic
List<ScoredProduct> reranked = relevanceResults.getMatchingResults().stream()
    .map(r -> {
        float relevance = r.getDocScore().getScore();
        float business = calculateBusinessScore(r.getData());
        float combined = (relevance * 0.6f) + (business * 0.4f);
        return new ScoredProduct(r.getData(), combined);
    })
    .sorted(Comparator.comparing(ScoredProduct::getScore).reversed())
    .limit(20)
    .collect(Collectors.toList());
```

## Configuration Options

### ForageSearchEngineBuilder Options

```java
ForageSearchEngineBuilder.<Book>builder()
    .withDataStore(dataStore)           // Required: Store implementation
    .withObjectMapper(mapper)           // Required: JSON serialization
    .withAnalyzer(analyzer)             // Optional: Custom Lucene analyzer
    .withQueryParserFactory(factory)    // Optional: Custom query parsing
```

### PeriodicUpdateEngine Options

```java
new PeriodicUpdateEngine<>(
    bootstrapper,                    // Data source
    new AsyncQueuedConsumer<>(engine), // Consumer
    interval,                        // Update frequency
    timeUnit                         // Time unit
);
```

## Next Steps

- [Pagination](pagination.md) - Navigate large result sets
- [Performance](performance.md) - Optimize your implementation
- [Architecture](architecture.md) - Understand the internals
