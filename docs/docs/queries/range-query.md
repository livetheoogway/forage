# Range Query

Range queries filter documents based on numeric field values within a specified range.

## Basic Usage

```java
// Integer range
QueryBuilder.intRangeQuery("pages", 100, 500).buildForageQuery()

// Float range
QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).buildForageQuery()
```

## Types

### Integer Range Query

```java
// Books with 200-400 pages
QueryBuilder.intRangeQuery("pages", 200, 400).buildForageQuery()

// Books published after 2020
QueryBuilder.intRangeQuery("year", 2020, Integer.MAX_VALUE).buildForageQuery()

// Books published before 2020
QueryBuilder.intRangeQuery("year", Integer.MIN_VALUE, 2019).buildForageQuery()
```

### Float Range Query

```java
// Books rated 4.0 to 5.0
QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).buildForageQuery()

// Products under $50
QueryBuilder.floatRangeQuery("price", 0f, 49.99f).buildForageQuery()

// Products $100 and above
QueryBuilder.floatRangeQuery("price", 100f, Float.MAX_VALUE).buildForageQuery()
```

## Range Boundaries

Both boundaries are **inclusive** by default:

```java
// Includes pages=100 and pages=500
QueryBuilder.intRangeQuery("pages", 100, 500)

// Document with pages=100 → ✓ Match
// Document with pages=300 → ✓ Match
// Document with pages=500 → ✓ Match
// Document with pages=501 → ✗ No match
```

## Combined with Other Queries

### Range as Filter

```java
// Search "programming" with rating filter
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "programming").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Multiple Range Filters

```java
// Books: 200-500 pages, rating 4+, published 2020+
QueryBuilder.booleanQuery()
    .query(QueryBuilder.intRangeQuery("pages", 200, 500).build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).build())
    .query(QueryBuilder.intRangeQuery("year", 2020, 2024).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Non-Scoring Filter

```java
// Range filter that doesn't affect score
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f)
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## With Boosting

```java
// Boost documents in the "sweet spot" page range
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "programming").build())
    .query(QueryBuilder.intRangeQuery("pages", 200, 400).boost(1.5f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Field Requirements

Range queries require numeric fields:

```java
// In your document creation
new ForageDocument(book.getId(), book, Arrays.asList(
    new IntField("pages", new int[]{book.getPages()}),     // ✓ Range queryable
    new IntField("year", new int[]{book.getYear()}),       // ✓ Range queryable
    new FloatField("rating", new float[]{book.getRating()}), // ✓ Range queryable
    new FloatField("price", new float[]{book.getPrice()})    // ✓ Range queryable
));
```

!!! warning "TextField Not Supported"
    Range queries don't work on `TextField` or `StringField`. Use `IntField` or `FloatField`.

## Common Use Cases

### Price Filtering

```java
// Products in price range
QueryBuilder.floatRangeQuery("price", minPrice, maxPrice)
```

### Date Range (as Epoch)

```java
// Store dates as epoch milliseconds
new IntField("publishedAt", new int[]{(int)(date.toEpochMilli() / 1000)})

// Query recent books (last 30 days)
long thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond();
QueryBuilder.intRangeQuery("publishedAt", (int)thirtyDaysAgo, Integer.MAX_VALUE)
```

### Rating Tiers

```java
// Excellent: 4.5+
QueryBuilder.floatRangeQuery("rating", 4.5f, 5.0f)

// Good: 4.0-4.5
QueryBuilder.floatRangeQuery("rating", 4.0f, 4.49f)

// Average: 3.0-4.0
QueryBuilder.floatRangeQuery("rating", 3.0f, 3.99f)
```

### Quantity Filters

```java
// In stock (quantity > 0)
QueryBuilder.intRangeQuery("quantity", 1, Integer.MAX_VALUE)

// Low stock (1-5 items)
QueryBuilder.intRangeQuery("quantity", 1, 5)

// Out of stock
QueryBuilder.intRangeQuery("quantity", 0, 0)
```

## Performance

Range queries on numeric fields are very efficient due to Lucene's point-based indexing:

| Dataset Size | Typical Query Time |
|--------------|-------------------|
| 10K docs | < 1ms |
| 100K docs | 1-5ms |
| 1M docs | 5-20ms |

## Related Topics

- [Boolean Query](boolean-query.md) - Combine range with other queries
- [Sorting](../scoring/sorting.md) - Sort by numeric fields
- [Field Types](../core-concepts/field-types.md) - Numeric field types
