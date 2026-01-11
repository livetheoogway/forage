# Query Boosts

Query boosts allow you to influence relevance by increasing or decreasing the importance of specific query clauses.

## Basic Concept

Boosting multiplies the score contribution of a query clause:

```java
// Without boost: title match contributes score S
QueryBuilder.matchQuery("title", "java").build()

// With boost 2.0: title match contributes score S × 2
QueryBuilder.matchQuery("title", "java").boost(2.0f).build()
```

## Applying Boosts

### Single Query Boost

```java
QueryBuilder.matchQuery("title", "programming")
    .boost(2.0f)
    .buildForageQuery()
```

### Boolean Query with Boosts

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").boost(3.0f).build())   // 3x weight
    .query(QueryBuilder.matchQuery("author", "java").boost(2.0f).build())  // 2x weight
    .query(QueryBuilder.matchQuery("content", "java").boost(1.0f).build()) // 1x weight
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Nested Boolean with Boosts

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.booleanQuery()
        .query(QueryBuilder.matchQuery("title", searchTerm).build())
        .query(QueryBuilder.phraseMatchQuery("title", searchTerm).boost(2.0f).build())
        .clauseType(ClauseType.SHOULD)
        .boost(3.0f)  // Boost entire title sub-query
        .build())
    .query(QueryBuilder.matchQuery("description", searchTerm).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Boost Strategies

### Field Importance

Boost fields based on their relevance to search intent:

```java
// For a book search
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).boost(5.0f).build())       // Most important
    .query(QueryBuilder.matchQuery("author", term).boost(3.0f).build())      // Very important
    .query(QueryBuilder.matchQuery("publisher", term).boost(1.5f).build())   // Somewhat important
    .query(QueryBuilder.matchQuery("description", term).boost(1.0f).build()) // Baseline
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Query Type Priority

Boost more specific queries higher:

```java
// Prefer exact matches over partial matches
QueryBuilder.booleanQuery()
    .query(QueryBuilder.phraseMatchQuery("title", term).boost(3.0f).build()) // Exact phrase
    .query(QueryBuilder.matchQuery("title", term).boost(1.5f).build())        // Any word
    .query(QueryBuilder.prefixMatchQuery("title", term).boost(1.0f).build())  // Prefix
    .query(QueryBuilder.fuzzyMatchQuery("title", term).boost(0.5f).build())   // Fuzzy
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Recency Bias

Boost recent content (combine with date range):

```java
int currentYear = Year.now().getValue();

QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    // Boost recent publications
    .query(QueryBuilder.intRangeQuery("year", currentYear - 1, currentYear)
        .boost(2.0f).build())
    .query(QueryBuilder.intRangeQuery("year", currentYear - 3, currentYear - 2)
        .boost(1.5f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Boost Values

| Boost Value | Effect |
|-------------|--------|
| `< 1.0` | Reduces importance |
| `= 1.0` | No change (default) |
| `> 1.0` | Increases importance |

```java
// Reduce importance
QueryBuilder.matchQuery("legacy_field", term).boost(0.5f)  // Half weight

// Increase importance
QueryBuilder.matchQuery("title", term).boost(2.0f)  // Double weight

// Significantly increase
QueryBuilder.matchQuery("exact_match", term).boost(5.0f)  // 5x weight
```

## Boost Combinations

### Additive Effect (SHOULD)

With `SHOULD`, boosted clauses add to the total score:

```java
// Document matching both: score = (title_score × 3) + (author_score × 2)
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).boost(3.0f).build())
    .query(QueryBuilder.matchQuery("author", term).boost(2.0f).build())
    .clauseType(ClauseType.SHOULD)
```

### Required with Bonus (MUST + SHOULD)

Require some clauses, boost others:

```java
QueryBuilder.booleanQuery()
    // Required: must match title
    .query(QueryBuilder.matchQuery("title", term).build())
    // Bonus: author match boosts score
    .query(QueryBuilder.matchQuery("author", term).boost(2.0f)
        .clauseType(ClauseType.SHOULD).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Real-World Examples

### E-Commerce Product Search

```java
public ForageQuery buildProductQuery(String searchTerm) {
    return QueryBuilder.booleanQuery()
        // Product name is most important
        .query(QueryBuilder.matchQuery("name", searchTerm).boost(5.0f).build())
        // Brand matches are valuable
        .query(QueryBuilder.matchQuery("brand", searchTerm).boost(3.0f).build())
        // Category context
        .query(QueryBuilder.matchQuery("category", searchTerm).boost(2.0f).build())
        // Description provides context
        .query(QueryBuilder.matchQuery("description", searchTerm).boost(1.0f).build())
        // SKU exact match (for power users)
        .query(QueryBuilder.matchQuery("sku", searchTerm).boost(10.0f).build())
        .clauseType(ClauseType.SHOULD)
        .buildForageQuery(20);
}
```

### Documentation Search

```java
public ForageQuery buildDocsQuery(String searchTerm) {
    return QueryBuilder.booleanQuery()
        // Section title exact match
        .query(QueryBuilder.phraseMatchQuery("title", searchTerm).boost(10.0f).build())
        // Title contains words
        .query(QueryBuilder.matchQuery("title", searchTerm).boost(5.0f).build())
        // Code examples
        .query(QueryBuilder.matchQuery("code", searchTerm).boost(3.0f).build())
        // Body content
        .query(QueryBuilder.matchQuery("body", searchTerm).boost(1.0f).build())
        .clauseType(ClauseType.SHOULD)
        .buildForageQuery(15);
}
```

## Best Practices

1. **Start simple**: Begin with default boosts, tune based on results
2. **Use relative values**: Boost ratios matter more than absolute values
3. **Test with real queries**: Verify ranking with actual search terms
4. **Document your boosts**: Record why each boost was chosen

## Related Topics

- [Function Score](function-score.md) - More advanced scoring control
- [Boolean Query](../queries/boolean-query.md) - Combining queries
- [Sorting](sorting.md) - Explicit result ordering
