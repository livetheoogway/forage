# Forage

[![Java CI with Maven](https://github.com/livetheoogway/forage/actions/workflows/actions.yml/badge.svg)](https://github.com/livetheoogway/teflon/actions/workflows/actions.yml)
[![Release](https://img.shields.io/maven-central/v/com.livetheoogway.forage/forage)](https://s01.oss.sonatype.org/content/repositories/releases/com/livetheoogway/teflon/)
[![License](https://img.shields.io/github/license/livetheoogway/forage)](https://github.com/livetheoogway/forage/blob/master/LICENSE)
![Coverage](.github/badges/jacoco.svg)
![Coverage](.github/badges/branches.svg)

### What is it?

A library that helps you build an in-memory search index, out of the data residing in your database.
This should be possible as long as you are able to pipe data out of the database, into your application.

### Why is it required?

Say you have small amount of data in your primary datastore, but you want simple search capabilities on top of this
data, would you spin up an entire search engine for this? Like a dedicated Elasticsearch or Solr?
There are some obvious problems with it:

1. Overkill: It is definitely an overkill in most use-cases, like the times when your database has only a few 1000 rows
2. Expensive: Depending on what hardware/cloud you choose to use to host the search engine
3. Latencies: Search engines today are really fast (especially if you provision on the hardware), but whatever you do,
   you still incur the network hop cost.

The library attempts to solve the above, by creating a simple search index, in every application node's memory.

### Any prerequisites and callouts?

- One important prerequisite is that, you should be able to pull all data from your database, ie, you should be able to
  stream it out as a batched select query (on your relational DB), or a scan (Aerospike, Redis, HBase or any other
  non-relational DB), depending on what database you are using.
- Size of data should be limited. This library has been tested for 100k rows in memory should be (todo)
- Ensure your application is supplied with sufficient memory. A ballpark for calculating the (todo)

### How is it happening though?

We've finished the _What_ and the _Why_, now let's look at the _How_.
At its heart is [Lucene](https://lucene.apache.org/). Why lucene you ask? Well, lucene is what powers Nutch, Solr,
Elasticsearch etc. It is the most evolved open-source java search engine libraries out there. It is well maintained,
supported by the Apache Software Foundation, and has continuous contributions. Need I say more?!

Essentially, the problem can be divided into 3 critical steps:

1. Bootstrapping: Ship all data from your database to Lucene:
2. Periodic Update: Do this at regular intervals (to account for changes in your database)
3. Indexing Rules: Be able to define what parts of the Data, you want indexed in Lucene
4. Search Queries: Be able to retrieve documents by querying on the indexed fields.

The following is a high level sketch of what is happening:

![core-class-diagram](resources/forage-HLD.jpg)

#### 1. Bootstrapping from your database:

- handles parallel callbacks
- ensure single threaded  
  (todo)

#### 2. Periodic

A `PeriodicUpdateEngine` ensures that the bootstrapping process is called
You can define how often the full bootstrap happens

# Getting started

### Maven Dependency

```xml

<dependency> 
    <groupId>com.livetheoogway.forage</groupId>
    <artifactId>forage-search-engine</artifactId>
    <version>${forage.version}</version> <!--look for the latest version on top-->
</dependency>
```

### Usage

(todo)

### Tech Dependencies

- Java 11
- Lucene 9.1.0

# Contributions

# Under the Hood

![core-class-diagram](resources/forage-core-classDiagram.png)
(todo)

0. Core and the bootstrapper diagram with the queued listeners
1. Lucene internals being masked
2. Searchers
3. Attributes being stored for field conversion

## Todos

- [ ] Expose Scoring and boosting
- [ ] Expose explain query IndexSearcher.explain(Query, doc)

