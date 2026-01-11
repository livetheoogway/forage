# Match All Query

The Match All Query returns all documents in the index. It's useful for browsing, getting total counts, or as a base for function scoring.

## Basic Usage

```java
QueryBuilder.matchAllQuery().buildForageQuery()
```

## Examples

### Get All Documents

```java
// Retrieve all books (up to 100 by default)
ForageQueryResult<Book> results = engine.search(
    QueryBuilder.matchAllQuery().buildForageQuery()
);
```

### With Limit

```java
// Get first 10 documents
QueryBuilder.matchAllQuery().buildForageQuery(10)
```

### With Sorting

```java
// All documents sorted by rating
QueryBuilder.matchAllQuery()
    .buildForageQuery(100, Arrays.asList(
        new SortCriteria("rating", SortOrder.DESC)
    ))
```

### With Function Score

```java
// All documents ranked by a field value
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchAllQuery().build())
    .fieldValueFactor("rating")
    .buildForageQuery(20)
```

## Use Cases

### Browse All Items

```java
// Product catalog browse
ForageQueryResult<Product> products = engine.search(
    QueryBuilder.matchAllQuery()
        .buildForageQuery(20, Arrays.asList(
            new SortCriteria("popularity", SortOrder.DESC)
        ))
);
```

### Filtered Browse

```java
// All books in a category
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchAllQuery().build())
    .query(QueryBuilder.matchQuery("category", "fiction")
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Count Total Documents

```java
ForageQueryResult<Book> results = engine.search(
    QueryBuilder.matchAllQuery().buildForageQuery(1)
);
long totalBooks = results.getTotal().getValue();
```

### Ranking Without Search Terms

```java
// Show "top rated" without any search query
ForageQueryResult<Book> topRated = engine.search(
    QueryBuilder.functionScoreQuery()
        .baseQuery(QueryBuilder.matchAllQuery().build())
        .fieldValueFactor("rating")
        .buildForageQuery(10)
);
```

### Random Sampling

```java
// Get random documents
ForageQueryResult<Book> random = engine.search(
    QueryBuilder.functionScoreQuery()
        .baseQuery(QueryBuilder.matchAllQuery().build())
        .scoreFunction(new RandomScoreFunction(System.currentTimeMillis(), "id_numeric"))
        .buildForageQuery(5)
);
```

## Scoring Behavior

Match All Query assigns a constant score of 1.0 to all documents:

```java
// All documents get score = 1.0
QueryBuilder.matchAllQuery().buildForageQuery()
```

This makes it ideal for function scoring where you want the score determined entirely by the function:

```java
// Score = rating value (not multiplied by base score)
QueryBuilder.functionScoreQuery()
    .baseQuery(QueryBuilder.matchAllQuery().build())
    .fieldValueFactor("rating")  // Score comes entirely from rating
    .buildForageQuery()
```

## With Boosting

```java
// Boost all matches (rarely needed)
QueryBuilder.matchAllQuery()
    .boost(2.0f)
    .buildForageQuery()
```

## Performance

Match All is very fast because it doesn't require term lookup:

| Aspect | Performance |
|--------|-------------|
| Query execution | O(1) |
| Result iteration | O(n) where n = limit |

!!! warning "Large Result Sets"
    Avoid requesting all documents at once for large indexes. Use pagination:

    ```java
    // Bad: potentially millions of results
    QueryBuilder.matchAllQuery().buildForageQuery(Integer.MAX_VALUE)

    // Good: paginated
    QueryBuilder.matchAllQuery().buildForageQuery(20)
    ```

## Combining with Filters

Match All is often used as a base with filters:

```java
// All books with rating >= 4.0
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchAllQuery().build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f)
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Related Topics

- [Function Score](../scoring/function-score.md) - Custom ranking with Match All
- [Sorting](../scoring/sorting.md) - Order Match All results
- [Boolean Query](boolean-query.md) - Combine with filters
