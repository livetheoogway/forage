# Minimum Score

Minimum score filtering removes low-quality results by setting a score threshold. Only documents with scores at or above the threshold are returned.

## Basic Usage

```java
// Only return results with score >= 0.5
QueryBuilder.matchQuery("title", "programming")
    .buildForageQuery(10, null, 0.5f)
```

## How It Works

```
Query Results:
  Doc1: score = 2.3  ✓ Returned
  Doc2: score = 1.8  ✓ Returned
  Doc3: score = 0.6  ✓ Returned
  Doc4: score = 0.4  ✗ Filtered (below 0.5)
  Doc5: score = 0.2  ✗ Filtered (below 0.5)

Minimum Score = 0.5
Returned: [Doc1, Doc2, Doc3]
```

## Examples

### Basic Threshold

```java
// Filter out weak matches
QueryBuilder.matchQuery("title", "java")
    .buildForageQuery(10, null, 0.5f)
```

### With Sorting

```java
// Sort by score, minimum 0.3
QueryBuilder.matchQuery("content", searchTerm)
    .buildForageQuery(20, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC)
    ), 0.3f)
```

### With Function Score

```java
// Custom scoring with threshold
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", "programming").build())
    .fieldValueFactor("rating")
    .buildForageQuery(10, Arrays.asList(
        SortCriteria.byScore(SortOrder.DESC)
    ), 2.0f)  // Minimum computed score of 2.0
```

## Use Cases

### Quality Control

Filter out marginally relevant results:

```java
// Only highly relevant matches
ForageQueryResult<Article> results = engine.search(
    QueryBuilder.matchQuery("content", searchTerm)
        .buildForageQuery(20, null, 1.0f)
);
```

### Fuzzy Query Cleanup

Fuzzy queries can produce many weak matches:

```java
// Filter out poor fuzzy matches
QueryBuilder.fuzzyMatchQuery("title", userInput)
    .buildForageQuery(10, null, 0.3f)
```

### Multi-Field Search

When searching across many fields, filter noise:

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).boost(3.0f).build())
    .query(QueryBuilder.matchQuery("author", term).boost(2.0f).build())
    .query(QueryBuilder.matchQuery("description", term).build())
    .query(QueryBuilder.matchQuery("tags", term).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery(20, null, 0.5f)  // Filter weak matches
```

### Relevance Guarantee

Ensure all results are truly relevant:

```java
public ForageQueryResult<Book> search(String query) {
    return engine.search(
        QueryBuilder.matchQuery("title", query)
            .buildForageQuery(10, Arrays.asList(
                SortCriteria.byScore(SortOrder.DESC)
            ), 0.5f)
    );
}
```

## Choosing Threshold Values

| Threshold | Behavior | Use Case |
|-----------|----------|----------|
| `0.0` | No filtering | Show all matches |
| `0.1 - 0.3` | Light filtering | Remove very weak matches |
| `0.3 - 0.5` | Moderate filtering | Quality-focused search |
| `0.5 - 1.0` | Aggressive filtering | High precision required |
| `> 1.0` | Very selective | Only strong matches |

!!! tip "Score Scales"
    Score values depend on your query type and data. Test with your actual data to find appropriate thresholds.

## Combining with Other Features

### Minimum Score + Pagination

```java
// First page with quality filter
ForageQueryResult<Book> firstPage = engine.search(
    QueryBuilder.matchQuery("title", searchTerm)
        .buildForageQuery(20, null, 0.5f)
);

// Pagination maintains the same filter
if (firstPage.getNextPage() != null) {
    ForageQueryResult<Book> nextPage = engine.search(
        new PageQuery(firstPage.getNextPage(), 20)
    );
}
```

### Dynamic Thresholds

Adjust threshold based on query type:

```java
public ForageQueryResult<Book> search(String query, QueryType type) {
    float minScore = switch (type) {
        case FUZZY -> 0.3f;      // Lower for typo-tolerant
        case PHRASE -> 0.8f;     // Higher for exact phrases
        case BOOLEAN -> 0.5f;    // Moderate for combinations
        default -> 0.4f;
    };

    return engine.search(
        buildQuery(query, type)
            .buildForageQuery(20, null, minScore)
    );
}
```

## Impact on Results

### Before Filtering

```
Results: 100 documents
Scores range: 0.1 to 3.5
Average score: 0.8
```

### After Filtering (min = 0.5)

```
Results: 45 documents
Scores range: 0.5 to 3.5
Average score: 1.4
```

## Performance

Minimum score filtering is applied **after** the query executes:

1. Query returns top N results
2. Filter removes results below threshold
3. Remaining results are returned

!!! warning "Result Count"
    If you request 20 results with a minimum score, you may get fewer than 20 if many are filtered out.

    ```java
    // May return 0-20 results depending on score distribution
    QueryBuilder.matchQuery("title", "obscure term")
        .buildForageQuery(20, null, 1.0f)
    ```

## Related Topics

- [Sorting](sorting.md) - Order results by score or field
- [Function Score](function-score.md) - Custom score computation
- [Query Boosts](query-boosts.md) - Influence base scores
