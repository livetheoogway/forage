# Fuzzy Query

Fuzzy queries find documents that match terms similar to the search term, allowing for typos and misspellings.

## Basic Usage

```java
QueryBuilder.fuzzyMatchQuery("title", "jva").buildForageQuery()
// Matches: "java", "jva", "jave", etc.
```

## How It Works

Fuzzy matching uses **edit distance** (Levenshtein distance) to find similar terms:

- Edit distance 1: One character change (insert, delete, substitute, transpose)
- Edit distance 2: Two character changes

```
Search: "jva" (misspelled "java")
        ↓ (fuzzy expansion)
Matches: "java" (edit distance 1: insert 'a')
         "jva"  (edit distance 0: exact)
```

## Examples

### Typo Tolerance

```java
// All these find "effective"
QueryBuilder.fuzzyMatchQuery("title", "effective")   // Exact
QueryBuilder.fuzzyMatchQuery("title", "efectiv")     // Missing letters
QueryBuilder.fuzzyMatchQuery("title", "effektive")   // Wrong letter
QueryBuilder.fuzzyMatchQuery("title", "efective")    // Missing letter
```

### User Search Input

```java
// Handle user typos gracefully
String userInput = "progamming";  // Typo: missing 'r'
QueryBuilder.fuzzyMatchQuery("title", userInput).buildForageQuery()
// Matches documents with "programming"
```

### With Boost

```java
QueryBuilder.fuzzyMatchQuery("title", "java")
    .boost(1.5f)
    .buildForageQuery()
```

## Combining with Other Queries

### Fuzzy OR Exact

```java
// Try exact match first, fall back to fuzzy
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", searchTerm).boost(2.0f).build())
    .query(QueryBuilder.fuzzyMatchQuery("title", searchTerm).boost(1.0f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Multi-Field Fuzzy Search

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.fuzzyMatchQuery("title", searchTerm).boost(2.0f).build())
    .query(QueryBuilder.fuzzyMatchQuery("author", searchTerm).boost(1.5f).build())
    .query(QueryBuilder.fuzzyMatchQuery("description", searchTerm).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Fuzzy with Filters

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.fuzzyMatchQuery("title", "progamming").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f)
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

## Edit Distance Behavior

Lucene's default fuzzy query uses automatic edit distance based on term length:

| Term Length | Max Edit Distance |
|-------------|-------------------|
| 1-2 chars | 0 (exact only) |
| 3-5 chars | 1 |
| 6+ chars | 2 |

```java
// Short terms: exact match only
QueryBuilder.fuzzyMatchQuery("title", "go")  // Only matches "go"

// Medium terms: 1 edit allowed
QueryBuilder.fuzzyMatchQuery("title", "java")  // Matches "java", "jav", "javaa"

// Long terms: 2 edits allowed
QueryBuilder.fuzzyMatchQuery("title", "programming")  // More flexibility
```

## Performance Considerations

Fuzzy queries are more expensive than exact matches:

| Aspect | Impact |
|--------|--------|
| Short terms | Fast (limited expansion) |
| Long terms | Slower (more potential matches) |
| Common terms | Slower (many expansions match) |
| Large index | Slower |

!!! tip "Performance Tip"
    Use fuzzy queries sparingly. Consider:

    - Applying minimum score filters to reduce low-quality matches
    - Combining with exact match queries (boost exact higher)
    - Limiting result size

## Best Practices

### 1. Combine with Exact Match

```java
// Prefer exact matches, allow fuzzy as fallback
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).boost(3.0f).build())
    .query(QueryBuilder.fuzzyMatchQuery("title", term).boost(1.0f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### 2. Use Minimum Score

```java
// Filter out low-quality fuzzy matches
QueryBuilder.fuzzyMatchQuery("title", "java")
    .buildForageQuery(10, null, 0.3f)  // Minimum score 0.3
```

### 3. Limit to User-Facing Search

```java
// Use fuzzy for user search
if (isUserSearch) {
    query = QueryBuilder.fuzzyMatchQuery("title", searchTerm);
} else {
    // Use exact for programmatic queries
    query = QueryBuilder.matchQuery("title", searchTerm);
}
```

## Limitations

- **Short terms**: Very short terms (1-2 chars) won't expand
- **Performance**: Can be slow for large indexes
- **Relevance**: May return unexpected matches

## Related Queries

- [Match Query](match-query.md) - Exact term matching
- [Prefix Query](prefix-query.md) - Prefix matching (autocomplete)
- [Phrase Query](phrase-query.md) - Exact phrase matching
