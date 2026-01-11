# Architecture

This document provides a deep dive into Forage's internal architecture.

## High-Level Architecture

![Forage Architecture](../assets/architecture.jpg)

## Component Overview

```mermaid
graph TB
    subgraph "Application Layer"
        APP[Your Application]
    end

    subgraph "Forage API"
        FE[ForageEngine]
        QB[QueryBuilder]
        FQ[ForageQuery]
    end

    subgraph "Core Engine"
        LQG[LuceneQueryGenerator]
        LSE[ForageLuceneSearchEngine]
        LI[LuceneIndex]
    end

    subgraph "Data Layer"
        BS[Bootstrapper]
        ST[Store]
    end

    APP --> QB
    QB --> FQ
    FQ --> FE
    FE --> LSE
    LSE --> LQG
    LQG --> LI
    LSE --> ST
    BS --> LSE
```

## Core Components

### ForageEngine

The main entry point that orchestrates search operations:

```java
public class ForageEngine<D> implements SearchEngine<ForageQuery, ForageQueryResult<D>> {

    private final ForageSearchEngine<D> searchEngine;

    @Override
    public ForageQueryResult<D> search(ForageQuery query) {
        return searchEngine.search(query);
    }

    @Override
    public void index(List<IndexableDocument> documents) {
        searchEngine.index(documents);
    }
}
```

### LuceneQueryGenerator

Converts Forage queries to native Lucene queries using the visitor pattern:

```java
public class LuceneQueryGenerator implements QueryVisitor<Query> {

    @Override
    public Query visit(MatchQuery matchQuery) {
        return new TermQuery(new Term(matchQuery.getField(), matchQuery.getValue()));
    }

    @Override
    public Query visit(BooleanQuery booleanQuery) {
        var builder = new org.apache.lucene.search.BooleanQuery.Builder();
        // ... build Lucene boolean query
        return builder.build();
    }

    @Override
    public Query visit(FunctionScoreQuery functionScoreQuery) {
        // Convert to Lucene FunctionScoreQuery with appropriate value source
    }
}
```

### LuceneIndex

Manages the in-memory Lucene index:

```java
public interface LuceneIndex {
    IndexWriter indexWriter();
    IndexSearcher searcher();
    DocRetriever docRetriever();
    void flush();
    void close();
}
```

## Data Flow

### Indexing Flow

```mermaid
sequenceDiagram
    participant BS as Bootstrapper
    participant AC as AsyncQueuedConsumer
    participant LSE as LuceneSearchEngine
    participant DH as DocumentHandler
    participant LI as LuceneIndex

    BS->>AC: consumer.accept(document)
    AC->>LSE: index(document)
    LSE->>DH: convert to Lucene Document
    DH-->>LSE: Lucene Document
    LSE->>LI: indexWriter.addDocument()
    Note over LI: Document added to RAM buffer

    BS->>AC: endOfBootstrap
    AC->>LSE: flush()
    LSE->>LI: commit + refresh
    Note over LI: Index now searchable
```

### Search Flow

```mermaid
sequenceDiagram
    participant APP as Application
    participant FE as ForageEngine
    participant LQG as QueryGenerator
    participant LI as LuceneIndex
    participant ST as Store

    APP->>FE: search(ForageQuery)
    FE->>LQG: query.accept(generator)
    LQG-->>FE: Lucene Query
    FE->>LI: searcher.search(query)
    LI-->>FE: TopDocs (IDs + Scores)
    FE->>ST: store.get(ids)
    ST-->>FE: Map<ID, Data>
    FE-->>APP: ForageQueryResult<D>
```

## Index Management

### Atomic Index Swap

During periodic updates, Forage creates a new index and atomically swaps it:

```mermaid
stateDiagram-v2
    [*] --> Active: Initial Bootstrap
    Active --> Building: Periodic Update Triggered
    Building --> Swapping: New Index Ready
    Swapping --> Active: Atomic Reference Swap
    Active --> Cleanup: Old Index Dereferenced
    Cleanup --> Active: GC Collects Old Index
```

```java
public class SearchEngineSwapReferenceHandler {

    private final AtomicReference<ForageSearchEngine<D>> engineRef;

    public void swap(ForageSearchEngine<D> newEngine) {
        ForageSearchEngine<D> old = engineRef.getAndSet(newEngine);
        // Old engine becomes eligible for GC
        if (old != null) {
            old.close();
        }
    }
}
```

## Thread Safety

### Read Operations

Multiple threads can search simultaneously:

```java
// Thread-safe: Multiple concurrent searches
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> engine.search(query));
}
```

### Write Operations

The `AsyncQueuedConsumer` serializes writes:

```java
public class AsyncQueuedConsumer<T> implements Consumer<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

    @Override
    public void accept(T item) {
        queue.put(item);  // Thread-safe enqueue
    }

    // Single consumer thread processes the queue
    private void processQueue() {
        while (running) {
            T item = queue.take();
            indexer.index(item);  // Single-threaded indexing
        }
    }
}
```

## Query Processing Pipeline

```
User Query
    │
    ▼
┌─────────────────┐
│  QueryBuilder   │  Build type-safe query
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ForageQuery    │  Abstract query representation
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ QueryGenerator  │  Convert to Lucene query
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Lucene Query   │  Native Lucene query
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ IndexSearcher   │  Execute against index
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    TopDocs      │  Document IDs + Scores
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Store       │  Fetch full documents
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ForageResult    │  Combined results
└─────────────────┘
```

## Class Diagram

![Class Diagram](../assets/class-diagram.png)

## Extension Points

### Custom Query Parser

```java
QueryParserFactory customFactory = field ->
    new QueryParser(field, new CustomAnalyzer());

ForageSearchEngineBuilder.<Book>builder()
    .withQueryParserFactory(customFactory)
    // ...
```

### Custom Analyzer

```java
Analyzer customAnalyzer = new Analyzer() {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(tokenizer);
        filter = new StopFilter(filter, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        return new TokenStreamComponents(tokenizer, filter);
    }
};
```

## Related Topics

- [Core Concepts](../core-concepts/index.md) - Fundamental concepts
- [Performance](performance.md) - Optimization guide
