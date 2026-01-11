# Match Query

The Match Query is the most fundamental query type. It searches for documents containing a specific term in a field.

## Basic Usage

```java
QueryBuilder.matchQuery("title", "java").buildForageQuery()
```

## How It Works

1. The search term is analyzed (tokenized, lowercased)
2. Lucene searches the inverted index for matching terms
3. Documents containing the term are returned with relevance scores

```
Search: "Java"
       ↓ (analysis)
Token: "java"
       ↓ (index lookup)
Matches: [doc1, doc5, doc12]
```

## Examples

### Simple Match

```java
// Find books with "effective" in the title
ForageQueryResult<Book> results = engine.search(
    QueryBuilder.matchQuery("title", "effective").buildForageQuery()
);
```

### Case Insensitive

Match queries are case-insensitive (due to standard analysis):

```java
// All of these find the same documents
QueryBuilder.matchQuery("title", "java")
QueryBuilder.matchQuery("title", "Java")
QueryBuilder.matchQuery("title", "JAVA")
```

### With Boost

Increase the relevance weight of matches:

```java
// Title matches are twice as important
QueryBuilder.matchQuery("title", "java")
    .boost(2.0f)
    .buildForageQuery()
```

### With Result Limit

```java
// Get top 5 matches
QueryBuilder.matchQuery("title", "programming")
    .buildForageQuery(5)
```

### With Sorting

```java
// Sort by score descending
QueryBuilder.matchQuery("author", "martin")
    .buildForageQuery(10, Arrays.asList(SortCriteria.byScore(SortOrder.DESC)))
```

## Field Types

Match Query behavior depends on the field type:

### TextField (Analyzed)

```java
// Document indexed with: new TextField("title", "Effective Java Programming")
// Tokens: ["effective", "java", "programming"]

QueryBuilder.matchQuery("title", "java")        // ✓ Matches
QueryBuilder.matchQuery("title", "effective")   // ✓ Matches
QueryBuilder.matchQuery("title", "eff")         // ✗ No match (partial)
QueryBuilder.matchQuery("title", "jav")         // ✗ No match (partial)
```

### StringField (Not Analyzed)

```java
// Document indexed with: new StringField("isbn", "978-0134685991")
// Token: ["978-0134685991"]

QueryBuilder.matchQuery("isbn", "978-0134685991")  // ✓ Matches
QueryBuilder.matchQuery("isbn", "978")             // ✗ No match
```

## Multiple Terms

When searching for multiple terms, each term is matched separately:

```java
// Searches for "clean" OR "code" (Lucene default)
QueryBuilder.matchQuery("title", "clean code")
```

For exact phrase matching, use [Phrase Query](phrase-query.md).

## Combining with Other Queries

```java
// Match query as part of boolean query
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", "java").boost(2.0f).build())
    .query(QueryBuilder.matchQuery("author", "bloch").build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Performance Considerations

| Factor | Impact |
|--------|--------|
| Common terms | Slower (match many documents) |
| Rare terms | Faster (match few documents) |
| Large fields | Slightly slower |
| Boosting | Minimal overhead |

## Best Practices

1. **Use specific fields**: Search specific fields rather than a catch-all field when possible
2. **Limit results**: Always specify a reasonable result limit
3. **Use boosting**: Boost important fields to improve relevance

## Related Queries

- [Fuzzy Query](fuzzy-query.md) - Match with typo tolerance
- [Phrase Query](phrase-query.md) - Match exact phrases
- [Prefix Query](prefix-query.md) - Match prefixes
- [Boolean Query](boolean-query.md) - Combine multiple match queries
