<p align="center">
  <img height="150" src="./resources/forage-logo.png" />
  <h1 align="center">Forage</h1>
  <p align="center">In-memory Search made Easy<p>
  <p align="center">
    <a href="https://github.com/livetheoogway/forage/actions">
    	<img src="https://github.com/livetheoogway/forage/actions/workflows/actions.yml/badge.svg"/>
    </a>
    <a href="https://s01.oss.sonatype.org/content/repositories/releases/com/livetheoogway/forage/">
    	<img src="https://img.shields.io/maven-central/v/com.livetheoogway.forage/forage"/>
    </a>
    <a href="https://github.com/livetheoogway/forage/blob/master/LICENSE">
    	<img src="https://img.shields.io/github/license/livetheoogway/forage" alt="license" />
    </a>
    <a href="https://livetheoogway.github.io/forage/">
    	<img src="https://img.shields.io/badge/documentation-forage-teal" alt="forage" />
    </a>
  </p>
  <p align="center">
    <a href="https://sonarcloud.io/project/overview?id=livetheoogway_forage">
    	<img src="https://sonarcloud.io/api/project_badges/measure?project=livetheoogway_forage&metric=alert_status"/>
    </a>
    <a href="https://sonarcloud.io/project/overview?id=livetheoogway_forage">
    	<img src="https://sonarcloud.io/api/project_badges/measure?project=livetheoogway_forage&metric=coverage"/>
    </a>
    <a href="https://sonarcloud.io/project/overview?id=livetheoogway_forage">
    	<img src="https://sonarcloud.io/api/project_badges/measure?project=livetheoogway_forage&metric=bugs"/>
    </a>
    <a href="https://sonarcloud.io/project/overview?id=livetheoogway_forage">
    	<img src="https://sonarcloud.io/api/project_badges/measure?project=livetheoogway_forage&metric=vulnerabilities"/>
    </a>
  </p>
</p>

## What is it?

A library that helps you build an in-memory search index, out of the data residing in your database/persistence layer. 
Powered by [Apache Lucene](https://lucene.apache.org/), it provides full-text search capabilities without the overhead of a dedicated search infrastructure.

## Documentation
For full documentation, please visit [Forage Docs](https://livetheoogway.github.io/forage)

## Contributions

Please raise Issues, Bugs or any feature requests at [Github Issues](https://github.com/livetheoogway/forage/issues)
. <br>
If you plan on contributing to the code, fork the repository and raise a Pull Request back here.

## Feature Roadmap

### ✅ Completed
- [x] Helpers for query creation
- [x] Fuzzy Query Support
- [x] Dropwizard bundle for simpler integrations
- [x] Phrase Query Support
- [x] Prefix Match Query Support
- [x] **Query-level Scoring and Boosting**
- [x] **Custom Sorting (by score, field values)**
- [x] **Minimum Score Filtering**
- [x] **Function Scoring (field value factors, constant scores)**
- [x] **Script Score Function** (JavaScript expressions with field access)
- [x] **Random Score Function** (deterministic shuffle with seed)
- [x] **Decay Functions** (Gaussian, Exponential, Linear distance-based scoring)
- [x] **Weighted Score Function**

### 📋 Planned
- [ ] Off-Heap index storage support
- [ ] Auto complete query Support
- [ ] Expose explain query (IndexSearcher.explain)
- [ ] Query performance analytics
- [ ] Embeddings and vector search
- [ ] Geo-spatial queries
- [ ] Score normalization options
