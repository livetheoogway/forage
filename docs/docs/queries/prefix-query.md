# Prefix Query

Prefix queries match documents where a field starts with a specified prefix. They're ideal for autocomplete functionality.

## Basic Usage

```java
QueryBuilder.prefixMatchQuery("author", "mart").buildForageQuery()
// Matches: "martin", "martinez", "martha", etc.
```

## How It Works

```
Index terms: ["martin", "martinez", "martha", "mark", "mary"]

Query prefix: "mart"
              ↓ (prefix expansion)
Matches: ["martin", "martinez", "martha"]
```

## Examples

### Autocomplete

```java
// User types "prog" in search box
String userInput = "prog";
QueryBuilder.prefixMatchQuery("title", userInput).buildForageQuery()
// Matches: "programming", "progress", "program", "progressive"
```

### Author Name Autocomplete

```java
// Find authors starting with "jo"
QueryBuilder.prefixMatchQuery("author", "jo").buildForageQuery()
// Matches: "joshua bloch", "john doe", "jonathan swift"
```

### With Boost

```java
QueryBuilder.prefixMatchQuery("title", "java")
    .boost(1.5f)
    .buildForageQuery()
```

### With Result Limit

```java
// Autocomplete suggestions (top 5)
QueryBuilder.prefixMatchQuery("title", userInput)
    .buildForageQuery(5)
```

## Combining with Other Queries

### Prefix with Filters

```java
// Autocomplete within a category
QueryBuilder.booleanQuery()
    .query(QueryBuilder.prefixMatchQuery("title", "prog").build())
    .query(QueryBuilder.matchQuery("category", "technology")
        .clauseType(ClauseType.FILTER).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery(10)
```

### Multi-Field Prefix

```java
// Search prefix in multiple fields
QueryBuilder.booleanQuery()
    .query(QueryBuilder.prefixMatchQuery("title", prefix).boost(2.0f).build())
    .query(QueryBuilder.prefixMatchQuery("author", prefix).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Prefix OR Exact

```java
// Boost exact matches higher than prefix matches
QueryBuilder.booleanQuery()
    .query(QueryBuilder.matchQuery("title", term).boost(3.0f).build())
    .query(QueryBuilder.prefixMatchQuery("title", term).boost(1.0f).build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Autocomplete Implementation

### Basic Autocomplete Endpoint

```java
public List<String> autocomplete(String prefix, int limit) {
    ForageQueryResult<Book> results = engine.search(
        QueryBuilder.prefixMatchQuery("title", prefix.toLowerCase())
            .buildForageQuery(limit)
    );

    return results.getMatchingResults().stream()
        .map(r -> r.getData().getTitle())
        .distinct()
        .collect(Collectors.toList());
}
```

### Autocomplete with Ranking

```java
public List<String> autocomplete(String prefix, int limit) {
    // Rank by popularity/rating
    ForageQueryResult<Book> results = engine.search(
        QueryBuilder.functionScoreQuery()
            .baseQuery(QueryBuilder.prefixMatchQuery("title", prefix).build())
            .fieldValueFactor("popularity")
            .buildForageQuery(limit)
    );

    return results.getMatchingResults().stream()
        .map(r -> r.getData().getTitle())
        .collect(Collectors.toList());
}
```

### Multi-Category Autocomplete

```java
public Map<String, List<String>> autocomplete(String prefix) {
    Map<String, List<String>> suggestions = new HashMap<>();

    // Book titles
    suggestions.put("books", autocompleteField("title", prefix, 5));

    // Authors
    suggestions.put("authors", autocompleteField("author", prefix, 5));

    return suggestions;
}

private List<String> autocompleteField(String field, String prefix, int limit) {
    return engine.search(
        QueryBuilder.prefixMatchQuery(field, prefix).buildForageQuery(limit)
    ).getMatchingResults().stream()
        .map(r -> extractField(r.getData(), field))
        .distinct()
        .collect(Collectors.toList());
}
```

## Field Type Behavior

### TextField

Prefix matching works on individual tokens:

```java
// Document: new TextField("title", "Java Programming Guide")
// Tokens: ["java", "programming", "guide"]

QueryBuilder.prefixMatchQuery("title", "prog")  // ✓ Matches "programming"
QueryBuilder.prefixMatchQuery("title", "java")  // ✓ Matches "java"
QueryBuilder.prefixMatchQuery("title", "jav")   // ✓ Matches "java"
```

### StringField

Prefix matching works on the entire value:

```java
// Document: new StringField("isbn", "978-0134685991")

QueryBuilder.prefixMatchQuery("isbn", "978")     // ✓ Matches
QueryBuilder.prefixMatchQuery("isbn", "978-01")  // ✓ Matches
QueryBuilder.prefixMatchQuery("isbn", "0134")    // ✗ No match (not prefix)
```

## Performance Considerations

| Factor | Impact |
|--------|--------|
| Short prefix (1-2 chars) | Slow (many matches) |
| Long prefix (4+ chars) | Fast (fewer matches) |
| Common prefix | Slower |

!!! warning "Short Prefixes"
    Very short prefixes (1-2 characters) can be expensive. Consider:

    - Requiring minimum prefix length (3+ chars)
    - Adding rate limiting
    - Caching common prefixes

## Best Practices

### 1. Minimum Prefix Length

```java
public List<String> autocomplete(String prefix) {
    if (prefix.length() < 3) {
        return Collections.emptyList();  // Require 3+ chars
    }
    // ... execute query
}
```

### 2. Debounce on Frontend

Wait for user to stop typing before sending request:

```javascript
// Frontend debouncing
let timeout;
input.addEventListener('input', (e) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => fetchSuggestions(e.target.value), 300);
});
```

### 3. Cache Results

```java
private final Cache<String, List<String>> autocompleteCache =
    CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

public List<String> autocomplete(String prefix) {
    return autocompleteCache.get(prefix, () -> executeAutocomplete(prefix));
}
```

## Related Queries

- [Match Query](match-query.md) - Full term matching
- [Fuzzy Query](fuzzy-query.md) - Typo-tolerant matching
- [Phrase Query](phrase-query.md) - Exact phrase matching
