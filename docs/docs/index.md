<h1 align="center">Forage</h1>

<p align="center">
  <img src="assets/logo.png#only-light" alt="Forage Logo" width="150">
  <img src="assets/logo-white.png#only-dark" alt="Forage Logo" width="150">
</p>

<p align="center" style="font-size: 1.5em; font-weight: 300;">
  In-memory Search made Easy
</p>

<p align="center">
  <a href="https://github.com/livetheoogway/forage/actions">
    <img src="https://github.com/livetheoogway/forage/actions/workflows/actions.yml/badge.svg" alt="Build Status"/>
  </a>
  <a href="https://s01.oss.sonatype.org/content/repositories/releases/com/livetheoogway/forage/">
    <img src="https://img.shields.io/maven-central/v/com.livetheoogway.forage/forage" alt="Maven Central"/>
  </a>
  <a href="https://github.com/livetheoogway/forage/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/livetheoogway/forage" alt="License"/>
  </a>
</p>

---

**Forage** is a Java library that helps you build an in-memory search index from data residing in your database or
persistence layer. Powered by [Apache Lucene](https://lucene.apache.org/), it provides full-text search capabilities
without the overhead of a dedicated search infrastructure.

## Why Forage?

Consider a scenario where your primary datastore holds a modest amount of data, yet you require sophisticated search
capabilities. Would you really want to provision an entire search engine — such as a dedicated Elasticsearch or Solr
cluster for this purpose? Alternatively, would you resort to cluttering your database with secondary indexes,
only to find that true free-text search remains out of reach?

Whichever path you choose, a few problems are almost inevitable:

1. **Overkill**: Deploying a full-scale search infrastructure is often disproportionate for many use cases,
   particularly when your dataset consists of only a few tens of thousands of rows.
2. **Expensive**: Beyond the hardware and cloud costs of hosting a search engine, you must also architect a pipeline
   to synchronize data from your primary datastore. Maintaining this consistency adds significant overhead.
3. **Latencies**: Modern search engines are fast—especially on well-provisioned hardware — but you still pay the price
   of
   network hops, no matter how optimized the setup is.

Forage aims to address these issues by building a lightweight search index directly in the memory of each application
node.<br>
As long as you can pull all the required data from your primary datastore, Forage makes it easy to spin up a quick,
in-memory search engine that continuously stays in sync with the latest state of your database.

<div class="feature-grid" markdown>

<div class="feature-card" markdown>

### :zap: Zero Infrastructure

No need for dedicated Elasticsearch or Solr clusters. Search runs in your application's memory.

</div>

<div class="feature-card" markdown>

### :rocket: Ultra-Low Latency

Eliminate network hops entirely. Search queries execute in microseconds, not milliseconds.

</div>

<div class="feature-card" markdown>

### :moneybag: Cost Effective

No additional hardware or cloud resources required. Perfect for small to medium datasets.

</div>

<div class="feature-card" markdown>

### :gear: Full Lucene Power

Access the full power of Lucene - fuzzy matching, phrase queries, boolean logic, and more.

</div>

</div>

## Features

| Feature                    | Description                                                            |
|----------------------------|------------------------------------------------------------------------|
| **Full-Text Search**       | Powerful text analysis with tokenization, stemming, and fuzzy matching |
| **Boolean Queries**        | Combine queries with AND, OR, NOT, and FILTER logic                    |
| **Range Queries**          | Filter by numeric or date ranges                                       |
| **Function Scoring**       | Customize ranking with field values, scripts, decay functions          |
| **Phrase Matching**        | Match exact phrases in text fields                                     |
| **Prefix Matching**        | Autocomplete-style prefix searches                                     |
| **Pagination**             | Efficient cursor-based pagination for large result sets                |
| **Dropwizard Integration** | First-class support for Dropwizard applications                        |

## When to Use Forage

Forage is ideal when:

- :white_check_mark: Your dataset is small to medium (up to ~1 million documents)
- :white_check_mark: You need low-latency search responses
- :white_check_mark: You want to avoid operational overhead of search infrastructure
- :white_check_mark: Your data fits comfortably in application memory (at least the fields you want indexed)

Consider alternatives when:

- :x: Your dataset exceeds available heap memory
- :x: You need distributed search across multiple nodes
- :x: You require real-time indexing of high-velocity data streams
- :x: You don't have means to periodically pull all data from your primary datastore

## Getting Started

Ready to add powerful search to your application?

[:material-rocket-launch: Quick Start Guide](getting-started/quick-start.md){ .md-button .md-button--primary }
[:material-book-open-variant: Read the Overview](overview.md){ .md-button }

## Tech Stack

- **Java 17+**
- **Apache Lucene 9.12.3**
- **Dropwizard 2.1.0** (optional)

## License

Forage is released under the [Apache License 2.0](https://github.com/livetheoogway/forage/blob/master/LICENSE).
