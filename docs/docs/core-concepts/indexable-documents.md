# Indexable Documents

Indexable documents are the fundamental unit of data in Forage. They define what gets stored in the Lucene index and how it can be searched.

## ForageDocument

The primary implementation of `IndexableDocument`:

```java
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.field.*;

ForageDocument document = new ForageDocument(
    "unique-id-123",           // Document ID
    Arrays.asList(             // List of indexed fields
        new TextField("title", "Effective Java"),
        new TextField("author", "Joshua Bloch"),
        new FloatField("rating", new float[]{4.7f})
    )
);
```

## Document Structure

### Document ID

Every document requires a unique identifier:

```java
new ForageDocument("book-123", ...)  // String ID
```

!!! warning "ID Uniqueness"
    Document IDs must be unique across your entire index. Duplicate IDs will result in overwritten documents.

### Fields

The second parameter is a list of fields to index:

```java
Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor()),
    new StringField("isbn", book.getIsbn()),
    new FloatField("rating", new float[]{book.getRating()}),
    new IntField("pages", new int[]{book.getPages()})
)
```

## Creating Documents in Bootstrap

Typically, you create documents during the bootstrap phase:

```java
@Override
public void bootstrap(Consumer<IndexableDocument> consumer) {
    for (Book book : getAllBooks()) {
        consumer.accept(createDocument(book));
    }
}

private ForageDocument createDocument(Book book) {
    List<Field> fields = new ArrayList<>();

    // Required: searchable fields
    fields.add(new TextField("title", book.getTitle()));
    fields.add(new TextField("author", book.getAuthor()));

    // Optional: exact match field
    if (book.getIsbn() != null) {
        fields.add(new StringField("isbn", book.getIsbn()));
    }

    // Numeric fields for filtering and sorting
    fields.add(new FloatField("rating", new float[]{book.getRating()}));
    fields.add(new IntField("pages", new int[]{book.getPages()}));

    return new ForageDocument(book.getId(), fields);
}
```

## Field Selection Strategy

Choose fields based on your search requirements:

| Search Need      | Field Type               | Example                           |
|------------------|--------------------------|-----------------------------------|
| Full-text search | `TextField`              | Title, description, content       |
| Exact matching   | `StringField`            | ISBN, category codes, status      |
| Range filtering  | `IntField`, `FloatField` | Price, rating, page count         |
| Sorting          | Any numeric field        | Date (as epoch), popularity score |
| Function scoring | Numeric fields           | Rating, freshness score           |

## Best Practices

### 1. Index Only What You Search

```java
// Good: Only index searchable fields
new ForageDocument(book.getId(), Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("author", book.getAuthor())
));

// Avoid: Indexing fields you never search
new ForageDocument(book.getId(), book, Arrays.asList(
    new TextField("title", book.getTitle()),
    new TextField("internalNotes", book.getInternalNotes()),  // Never searched
    new TextField("auditLog", book.getAuditLog())             // Never searched
));
```

### 2. Handle Null Values

```java
private ForageDocument createDocument(Book book) {
    List<Field> fields = new ArrayList<>();

    // Always add required fields
    fields.add(new TextField("title",
        book.getTitle() != null ? book.getTitle() : ""));

    // Conditionally add optional fields
    if (book.getDescription() != null && !book.getDescription().isEmpty()) {
        fields.add(new TextField("description", book.getDescription()));
    }

    return new ForageDocument(book.getId(), book, fields);
}
```

### 3. Normalize Text

```java
// Normalize before indexing for consistent search
fields.add(new TextField("title", book.getTitle().toLowerCase().trim()));
```

### 4. Composite Fields

Create composite fields for cross-field searching:

```java
// Index individual fields
fields.add(new TextField("title", book.getTitle()));
fields.add(new TextField("author", book.getAuthor()));

// Create a composite field for "search all"
String allText = String.join(" ",
    book.getTitle(),
    book.getAuthor(),
    book.getDescription()
);
fields.add(new TextField("_all", allText));
```

Then search across all content:

```java
QueryBuilder.matchQuery("_all", "java programming").buildForageQuery();
```

## Document Size Considerations

```java
// Monitor document complexity
private ForageDocument createDocument(Book book) {
    List<Field> fields = new ArrayList<>();

    // Text fields consume more memory
    fields.add(new TextField("title", book.getTitle()));           // ~100 bytes
    fields.add(new TextField("description", book.getDescription())); // ~2KB average

    // Numeric fields are compact
    fields.add(new FloatField("rating", new float[]{book.getRating()})); // ~8 bytes
    fields.add(new IntField("pages", new int[]{book.getPages()}));       // ~8 bytes

    return new ForageDocument(book.getId(), book, fields);
}
```

!!! tip "Memory Estimation"
    For a rough memory estimate:

    - Text fields: 2-3x the raw text size
    - Numeric fields: ~8-16 bytes per field
    - Per-document overhead: ~100-200 bytes

## Next Steps

- [Field Types](field-types.md) - Detailed guide to each field type
- [Bootstrapping](bootstrapping.md) - How to feed documents into Forage
