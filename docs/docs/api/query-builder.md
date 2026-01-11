# QueryBuilder API Reference

The `QueryBuilder` class provides static factory methods for creating all query types.

## Import

```java
import com.livetheoogway.forage.models.query.util.QueryBuilder;
```

## Match Query

Creates a term match query.

```java
QueryBuilder.matchQuery(String field, String value)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `field` | String | Field name to search |
| `value` | String | Term to match |

**Returns**: `MatchQueryBuilder`

**Methods on MatchQueryBuilder**:

| Method | Description |
|--------|-------------|
| `.boost(float)` | Set relevance boost |
| `.build()` | Build as sub-query |
| `.buildForageQuery()` | Build as complete query |
| `.buildForageQuery(int size)` | Build with result limit |
| `.buildForageQuery(int size, List<SortCriteria> sort)` | Build with sorting |
| `.buildForageQuery(int size, List<SortCriteria> sort, Float minScore)` | Build with all options |

**Example**:
```java
QueryBuilder.matchQuery("title", "java")
    .boost(2.0f)
    .buildForageQuery(10)
```

## Boolean Query

Creates a boolean query combining multiple sub-queries.

```java
QueryBuilder.booleanQuery()
```

**Returns**: `BooleanQueryBuilder`

**Methods on BooleanQueryBuilder**:

| Method | Description |
|--------|-------------|
| `.query(Query)` | Add a sub-query |
| `.clauseType(ClauseType)` | Set clause type (MUST, SHOULD, MUST_NOT, FILTER) |
| `.boost(float)` | Set relevance boost |
| `.build()` | Build as sub-query |
| `.buildForageQuery(...)` | Build as complete query |

**Example**:
```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.matchQuery("author", "bloch").build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery(10)
```

## Range Queries

### Integer Range

```java
QueryBuilder.intRangeQuery(String field, int low, int high)
```

### Float Range

```java
QueryBuilder.floatRangeQuery(String field, float low, float high)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `field` | String | Numeric field name |
| `low` | int/float | Lower bound (inclusive) |
| `high` | int/float | Upper bound (inclusive) |

**Example**:
```java
QueryBuilder.intRangeQuery("pages", 100, 500).buildForageQuery()
QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).buildForageQuery()
```

## Fuzzy Query

Creates a fuzzy match query with typo tolerance.

```java
QueryBuilder.fuzzyMatchQuery(String field, String value)
```

**Example**:
```java
QueryBuilder.fuzzyMatchQuery("title", "progamming").buildForageQuery()
```

## Phrase Query

Creates an exact phrase match query.

```java
QueryBuilder.phraseMatchQuery(String field, String phrase)
```

**Example**:
```java
QueryBuilder.phraseMatchQuery("title", "clean code").buildForageQuery()
```

## Prefix Query

Creates a prefix match query for autocomplete.

```java
QueryBuilder.prefixMatchQuery(String field, String prefix)
```

**Example**:
```java
QueryBuilder.prefixMatchQuery("author", "mart").buildForageQuery()
```

## Match All Query

Returns all documents.

```java
QueryBuilder.matchAllQuery()
```

**Example**:
```java
QueryBuilder.matchAllQuery().buildForageQuery(100)
```

## Function Score Query

Creates a query with custom scoring functions.

```java
QueryBuilder.functionScoreQuery()
```

**Returns**: `FunctionScoreQueryBuilder`

**Methods on FunctionScoreQueryBuilder**:

| Method | Description |
|--------|-------------|
| `.baseQuery(Query)` | Set the base query |
| `.scoreFunction(ScoreFunction)` | Set custom score function |
| `.constantScore(float)` | Use constant score |
| `.fieldValueFactor(String field)` | Score by field value |
| `.fieldValueFactor(String field, float factor)` | Score by field value × factor |
| `.boost(float)` | Final score multiplier |
| `.build()` | Build as sub-query |
| `.buildForageQuery(...)` | Build as complete query |

**Example**:
```java
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchQuery("title", "java").build())
    .fieldValueFactor("rating", 1.2f)
    .boost(1.1f)
    .buildForageQuery(20)
```

## ClauseType Enum

```java
public enum ClauseType {
    MUST,      // AND - all must match
    SHOULD,    // OR - at least one must match
    MUST_NOT,  // NOT - must not match
    FILTER     // Must match, no scoring
}
```

## SortCriteria

```java
// Sort by score
SortCriteria.byScore(SortOrder order)

// Sort by field
new SortCriteria(String field, SortOrder order)
```

## SortOrder Enum

```java
public enum SortOrder {
    ASC,   // Ascending (low to high)
    DESC   // Descending (high to low)
}
```

## Complete Example

```java
ForageQuery query = QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "programming").boost(3.0f).build())
    .query(QueryBuilder.matchQuery("author", "martin").boost(2.0f).build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f)
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery(
        20,
        Arrays.asList(SortCriteria.byScore(SortOrder.DESC)),
        0.5f
    );

ForageQueryResult<Book> results = engine.search(query);
```
