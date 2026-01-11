# Phrase Query

Phrase queries match documents containing an exact sequence of words.

## Basic Usage

```java
QueryBuilder.phraseMatchQuery("title", "clean code").buildForageQuery()
```

## How It Works

Unlike match queries that find any occurrence of terms, phrase queries require terms to appear in the exact order:

```
Document: "Clean Code: A Handbook of Agile Software Craftsmanship"
          ↓ (tokenized)
Tokens:   ["clean", "code", "a", "handbook", "of", "agile", "software", "craftsmanship"]

Query: "clean code"
       ↓ (requires sequence)
Matches: Position 0: "clean", Position 1: "code" → ✓ Match!

Query: "code clean"
       ↓ (wrong order)
Result: ✗ No match
```

## Examples

### Exact Phrase Match

```java
// Find books with exact phrase "machine learning"
QueryBuilder.phraseMatchQuery("title", "machine learning").buildForageQuery()

// Matches: "Machine Learning for Beginners"
// Matches: "Introduction to Machine Learning"
// No match: "Learning Machine Algorithms"
```

### Multi-Word Phrases

```java
// Longer phrases
QueryBuilder.phraseMatchQuery("title", "design patterns").buildForageQuery()
QueryBuilder.phraseMatchQuery("description", "object oriented programming").buildForageQuery()
```

### With Boost

```java
QueryBuilder.phraseMatchQuery("title", "clean code")
    .boost(2.0f)
    .buildForageQuery()
```

## Phrase vs Match Query

| Query Type | Search | Matches |
|------------|--------|---------|
| Match | "clean code" | "clean", "code" anywhere |
| Phrase | "clean code" | "clean code" as sequence |

```java
// Match Query: finds documents with either "clean" OR "code"
QueryBuilder.matchQuery("title", "clean code")
// Matches: "Clean Code", "Code Review", "Clean Architecture"

// Phrase Query: finds documents with "clean code" sequence
QueryBuilder.phraseMatchQuery("title", "clean code")
// Matches: "Clean Code" only
```

## Combining with Other Queries

### Phrase with Filters

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.phraseMatchQuery("title", "design patterns").build())
    .query(QueryBuilder.floatRangeQuery("rating", 4.0f, 5.0f).build())
    .clauseType(ClauseType.MUST)
    .buildForageQuery()
```

### Phrase OR Match (Flexible Search)

```java
// Prefer phrase match, but allow individual term matches
QueryBuilder.booleanQuery()
    .query(QueryBuilder.phraseMatchQuery("title", "clean code").boost(3.0f).build())
    .query(QueryBuilder.matchQuery("title", "clean").build())
    .query(QueryBuilder.matchQuery("title", "code").build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

### Multi-Field Phrase Search

```java
QueryBuilder.booleanQuery()
    .query(QueryBuilder.phraseMatchQuery("title", "machine learning").boost(2.0f).build())
    .query(QueryBuilder.phraseMatchQuery("description", "machine learning").build())
    .clauseType(ClauseType.SHOULD)
    .buildForageQuery()
```

## Use Cases

### Book/Article Titles

```java
// Find exact book title
QueryBuilder.phraseMatchQuery("title", "the pragmatic programmer")
```

### Technical Terms

```java
// Multi-word technical terms
QueryBuilder.phraseMatchQuery("content", "dependency injection")
QueryBuilder.phraseMatchQuery("content", "test driven development")
```

### Quoted Search

```java
// Implement quoted search like Google
String userQuery = "\"clean code\"";  // User typed with quotes

if (userQuery.startsWith("\"") && userQuery.endsWith("\"")) {
    String phrase = userQuery.substring(1, userQuery.length() - 1);
    query = QueryBuilder.phraseMatchQuery("title", phrase);
} else {
    query = QueryBuilder.matchQuery("title", userQuery);
}
```

## Analysis and Phrase Matching

Phrase queries work with analyzed text:

```java
// Document: "The Quick Brown Fox"
// Analyzed tokens: ["the", "quick", "brown", "fox"]

// These all match:
QueryBuilder.phraseMatchQuery("title", "quick brown")      // ✓
QueryBuilder.phraseMatchQuery("title", "QUICK BROWN")      // ✓ (case-insensitive)
QueryBuilder.phraseMatchQuery("title", "Quick Brown")      // ✓

// These don't match:
QueryBuilder.phraseMatchQuery("title", "brown quick")      // ✗ (wrong order)
QueryBuilder.phraseMatchQuery("title", "quick fox")        // ✗ (missing "brown")
```

## Performance

Phrase queries are efficient but slightly more expensive than simple match queries:

| Aspect | Impact |
|--------|--------|
| Short phrases (2-3 words) | Fast |
| Long phrases (5+ words) | Moderate |
| Common first word | Slightly slower |

## Limitations

- Requires exact word sequence
- No fuzzy matching within phrases
- Stop words may be removed during analysis

## Related Queries

- [Match Query](match-query.md) - Individual term matching
- [Prefix Query](prefix-query.md) - Term prefix matching
- [Fuzzy Query](fuzzy-query.md) - Typo-tolerant matching
