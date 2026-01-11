# Boolean Query

Boolean queries combine multiple queries using logical operators. They're the foundation for complex search expressions.

## Basic Usage

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.matchQuery("author", "bloch").build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Clause Types

| Clause Type | Meaning | SQL Equivalent |
|-------------|---------|----------------|
| `MUST` | All queries must match | AND |
| `SHOULD` | At least one query must match | OR |
| `MUST_NOT` | Query must not match | NOT |
| `FILTER` | Must match, but doesn't affect scoring | WHERE (no boost) |

## Examples

### AND Logic (MUST)

```java
// Books about "java" AND by "bloch"
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.matchQuery("author", "bloch").build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### OR Logic (SHOULD)

```java
// Books about "java" OR "python"
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.matchQuery("title", "python").build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### NOT Logic (MUST_NOT)

```java
// Programming books, excluding "beginner" level
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("category", "programming").build())
    .query(QueryBuilder.matchQuery("level", "beginner")
        .clauseType(ClauseType.MUST_NOT).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

!!! warning "MUST_NOT Alone"
    `MUST_NOT` alone won't return results. Always combine with `MUST` or `SHOULD` clauses.

### Filter Without Scoring (FILTER)

```java
// Search "java" in title, filter by category (category doesn't affect score)
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.matchQuery("category", "programming")
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Complex Queries

### Nested Boolean Queries

```java
// (title:java OR title:python) AND author:bloch
QueryBuilder.booleanQuery()
    .query(QueryBuilder.booleanQuery()
        .query(QueryBuilder.matchQuery("title", "java").build())
        .query(QueryBuilder.matchQuery("title", "python").build())
        .clauseType(ClauseType.SHOULD)
        .build())
    .query(QueryBuilder.matchQuery("author", "bloch").build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Search with Range Filter

```java
// Books about "programming" with rating >= 4.0
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "programming").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Multi-Field Search with Boosting

```java
// Search across fields with different weights
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").boost(3.0f).build())
    .query(QueryBuilder.matchQuery("author", "java").boost(2.0f).build())
    .query(QueryBuilder.matchQuery("description", "java").boost(1.0f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Scoring Behavior

| Clause Type | Contributes to Score |
|-------------|---------------------|
| `MUST` | Yes |
| `SHOULD` | Yes |
| `MUST_NOT` | No |
| `FILTER` | No |

```java
// Title and description both contribute to score
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").boost(2.0f).build())      // Score: 2x
    .query(QueryBuilder.matchQuery("description", "java").boost(1.0f).build()) // Score: 1x
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()

// Category is required but doesn't boost score
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())        // Contributes to score
    .query(QueryBuilder.matchQuery("category", "programming")
        .clauseType(ClauseType.FILTER).build())                     // No score contribution
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Common Patterns

### Search with Multiple Filters

```java
QueryBuilder.booleanQuery()
    // Search query (contributes to score)
    .query(QueryBuilder.matchQuery("title", "programming").boost(2.0f).build())

    // Filters (required but don't affect score)
    .query(QueryBuilder.matchQuery("category", "technology")
        .clauseType(ClauseType.FILTER).build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f)
        .clauseType(ClauseType.FILTER).build())
    .query(QueryBuilder.intRangeQuery("year", 2020, 2024)
        .clauseType(ClauseType.FILTER).build())

    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Faceted Search

```java
// Search with multiple OR conditions for facets
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).build())
    .query(QueryBuilder.booleanQuery()
        .query(QueryBuilder.matchQuery("category", "fiction").build())
        .query(QueryBuilder.matchQuery("category", "non-fiction").build())
        .clauseType(ClauseType.SHOULD)
        .build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Performance Tips

1. **Use FILTER for non-scoring clauses**: Filters are cached and faster
2. **Put selective clauses first**: More selective queries narrow results faster
3. **Avoid deep nesting**: Flatten boolean queries when possible

## Related Queries

- [Match Query](match-query.md) - Basic term matching
- [Range Query](range-query.md) - Numeric filtering
- [Function Score](../scoring/function-score.md) - Advanced scoring
