# Sorting

Sorting controls the order in which search results are returned. You can sort by relevance score, field values, or a combination.

## Basic Usage

```java
// Sort by score (default)
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10)

// Sort by field
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10, Arrays.asList(
        new SortCriteria("rating", SortOrder.DESC)
    ))
```

## Sort Criteria

### By Score

```java
// Highest score first (default for most searches)
SortCriteria.byScore(SortOrder.DESC)

// Lowest score first (unusual, but available)
SortCriteria.byScore(SortOrder.ASC)
```

### By Field

```java
// By rating, highest first
new SortCriteria("rating", SortOrder.DESC)

// By year, oldest first
new SortCriteria("year", SortOrder.ASC)

// By title, alphabetical
new SortCriteria("title", SortOrder.ASC)
```

## Sort Order

| Order | Description | Example |
|-------|-------------|---------|
| `DESC` | Descending (high to low) | 5, 4, 3, 2, 1 |
| `ASC` | Ascending (low to high) | 1, 2, 3, 4, 5 |

## Multiple Sort Criteria

Results are sorted by the first criterion, then by subsequent criteria as tiebreakers:

```java
QueryBuilder.matchQuery("title", "programming")
    .buildForageQuery(20, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC),       // Primary: relevance
        new SortCriteria("rating", SortOrder.DESC), // Tiebreaker: rating
        new SortCriteria("year", SortOrder.DESC)    // Second tiebreaker: year
    ))
```

## Examples

### Relevance Only

```java
// Default behavior - sort by relevance score
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10)

// Explicit relevance sort
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC)
    ))
```

### By Rating

```java
// Top rated books matching "programming"
QueryBuilder.matchQuery("title", "programming")
    .buildForageQuery(10, Arrays.asList(
        new SortCriteria("rating", SortOrder.DESC)
    ))
```

### By Date

```java
// Most recent first
QueryBuilder.matchQuery("category", "news")
    .buildForageQuery(20, Arrays.asList(
        new SortCriteria("publishedAt", SortOrder.DESC)
    ))

// Oldest first
QueryBuilder.matchQuery("category", "archive")
    .buildForageQuery(20, Arrays.asList(
        new SortCriteria("publishedAt", SortOrder.ASC)
    ))
```

### Relevance + Tiebreaker

```java
// Sort by relevance, use rating to break ties
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(20, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC),
        new SortCriteria("rating", SortOrder.DESC)
    ))
```

### Price Sorting

```java
// Low to high price
QueryBuilder.matchQuery("category", "laptops")
    .buildForageQuery(50, Arrays.asList(
        new SortCriteria("price", SortOrder.ASC)
    ))

// High to low price
QueryBuilder.matchQuery("category", "laptops")
    .buildForageQuery(50, Arrays.asList(
        new SortCriteria("price", SortOrder.DESC)
    ))
```

## With Function Score

When using function score, sort by score to see the computed scores:

```java
// Function score results sorted by computed score
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", "java").build())
    .fieldValueFactor("rating")
    .buildForageQuery(10, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC)
    ))
```

## With Filters

```java
// Filter then sort by field
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("category", "technology").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery(20, Arrays.asList(
        new SortCriteria("rating", SortOrder.DESC),
        new SortCriteria("year", SortOrder.DESC)
    ))
```

## Field Types and Sorting

| Field Type | Sortable | Notes |
|------------|----------|-------|
| `IntField` | Yes | Numeric sort |
| `FloatField` | Yes | Numeric sort |
| `StringField` | Yes | Lexicographic sort |
| `TextField` | No | Use StringField for sortable text |

!!! warning "TextField Sorting"
    `TextField` is analyzed and tokenized, making it unsuitable for sorting. If you need to sort by a text field, index it as both:

    ```java
    // Searchable + Sortable
    new TextField("title", book.getTitle()),      // For searching
    new StringField("title_sort", book.getTitle()) // For sorting
    ```

## Common Patterns

### E-Commerce Sorting Options

```java
public ForageQuery buildProductQuery(String searchTerm, String sortBy) {
    List<SortCriteria> sort = switch (sortBy) {
        case "relevance" -> Arrays.asList(SortCriteria.byScore(SortOrder.DESC));
        case "price_low" -> Arrays.asList(new SortCriteria("price", SortOrder.ASC));
        case "price_high" -> Arrays.asList(new SortCriteria("price", SortOrder.DESC));
        case "rating" -> Arrays.asList(new SortCriteria("rating", SortOrder.DESC));
        case "newest" -> Arrays.asList(new SortCriteria("createdAt", SortOrder.DESC));
        default -> Arrays.asList(SortCriteria.byScore(SortOrder.DESC));
    };

    return QueryBuilder.matchQuery("name", searchTerm)
        .buildForageQuery(20, sort);
}
```

### Blog Post Listing

```java
// Most recent posts first
QueryBuilder.matchQuery("status", "published")
    .buildForageQuery(20, Arrays.asList(
        new SortCriteria("publishedAt", SortOrder.DESC)
    ))
```

### Leaderboard

```java
// Top scores
QueryBuilder.matchAllQuery()
    .buildForageQuery(100, Arrays.asList(
        new SortCriteria("score", SortOrder.DESC),
        new SortCriteria("timestamp", SortOrder.ASC)  // Earlier wins ties
    ))
```

## Performance Considerations

- Field-based sorting is fast when fields have DocValues
- All numeric fields (`IntField`, `FloatField`) automatically have DocValues
- Sorting doesn't significantly impact query performance

## Related Topics

- [Function Score](function-score.md) - Custom scoring functions
- [Query Boosts](query-boosts.md) - Influence relevance scores
- [Minimum Score](minimum-score.md) - Filter by score threshold
