# Installation

## Requirements

- **Java 17** or later
- **Maven** or **Gradle** build system

!!! info "Latest Version"
    The latest stable version of Forage is [<img src="https://img.shields.io/maven-central/v/com.livetheoogway.forage/forage" alt="Maven Central"/>](https://central.sonatype.com/namespace/com.livetheoogway.forage)

## Maven

Add the following

```xml

<dependencyManagement>
    <dependencies>
        <!-- other stuff -->
        <dependency>
            <groupId>com.livetheoogway.forage</groupId>
            <artifactId>forage-bom</artifactId>
            <version>${forage.version}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>com.livetheoogway.forage</groupId>
    <artifactId>forage-search-engine</artifactId>
    <version>${forage.version}</version>
</dependency>
```

### Dropwizard Integration

If you're using Dropwizard, add this dependency instead:

```xml

<dependency>
    <groupId>com.livetheoogway.forage</groupId>
    <artifactId>forage-dropwizard-bundle</artifactId>
    <version>${forage.version}</version>
</dependency>
```

## Gradle

Add to your `build.gradle`:

```groovy
implementation 'com.livetheoogway.forage:forage-search-engine:${forage.version}'
```

Or for Dropwizard:

```groovy
implementation 'com.livetheoogway.forage:forage-dropwizard-bundle:${forageVersion}'
```

## Gradle (Kotlin DSL)

```kotlin
implementation("com.livetheoogway.forage:forage-search-engine:$forageVersion")
```

## Modules

Forage is organized into several modules:

| Module                     | Description                                   |
|----------------------------|-----------------------------------------------|
| `forage-models`            | Query models and result types                 |
| `forage-core`              | Core abstractions (Bootstrapper, Store, etc.) |
| `forage-search-engine`     | Main search engine implementation             |
| `forage-dropwizard-bundle` | Dropwizard integration bundle                 |

!!! tip "Which module to use?"
    For most applications, you only need `forage-search-engine`. It transitively includes `forage-models` and `forage-core`.

## Verify Installation

Create a simple test to verify the installation:

```java
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.query.ForageQuery;

public class VerifyInstallation {
    public static void main(String[] args) {
        ForageQuery query = QueryBuilder.matchQuery("field", "value").buildForageQuery();
        System.out.println("Forage installed successfully!");
        System.out.println("Query: " + query);
    }
}
```

## Transitive Dependencies

Forage brings in the following key dependencies:

| Dependency    | Version | Purpose            |
|---------------|---------|--------------------|
| Apache Lucene | 9.12.3  | Core search engine |
| Jackson       | 2.13.x  | JSON serialization |
| Guava         | 31.1    | Utilities          |
| SLF4J         | 1.7.x   | Logging facade     |

!!! warning "Dependency Conflicts"
    If you have existing Lucene or Jackson dependencies, ensure version compatibility. Forage requires Lucene 9.x and Jackson 2.13+.

## Next Steps

Now that you have Forage installed:

- [Quick Start Guide](quick-start.md) - Build your first search engine
- [Dropwizard Integration](dropwizard.md) - Simplified setup for Dropwizard apps
