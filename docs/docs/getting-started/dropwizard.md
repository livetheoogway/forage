# Dropwizard Integration

Forage provides a first-class integration bundle for [Dropwizard](https://www.dropwizard.io/) applications, simplifying
setup and lifecycle management.

## Installation

Add the Dropwizard bundle dependency:

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

<dependencies>
<!--other stuff-->
<dependency>
    <groupId>com.livetheoogway.forage</groupId>
    <artifactId>forage-dropwizard-bundle</artifactId>
    <version>${project.version}</version>
</dependency>
</dependencies>
```

## Configuration

Add `ForageConfiguration` to your application configuration:

```java
import com.livetheoogway.forage.dropwizard.bundle.ForageConfiguration;
import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MyAppConfiguration extends Configuration {

    private ForageConfiguration forageConfiguration;

    // Other configuration fields...
}
```

### Configuration Options

```yaml
# config.yml
forageConfiguration:
  refreshIntervalInSeconds: 60    # How often to refresh the index

```

## Bundle Registration

Register the `ForageBundle` in your application:

```java
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.dropwizard.bundle.ForageBundle;
import com.livetheoogway.forage.dropwizard.bundle.ForageConfiguration;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MyApplication extends Application<MyAppConfiguration> {

    private BookStore bookStore;

    public static void main(String[] args) throws Exception {
        new MyApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MyAppConfiguration> bootstrap) {
        // Initialize your data store
        this.bookStore = new BookStore();

        // Register the Forage bundle
        bootstrap.addBundle(new ForageBundle<MyAppConfiguration, Book>() {

            @Override
            public Store<Book> dataStore(MyAppConfiguration configuration) {
                return bookStore;
            }

            @Override
            public Bootstrapper<IndexableDocument> bootstrap(MyAppConfiguration configuration) {
                return bookStore;
            }

            @Override
            public ForageConfiguration forageConfiguration(MyAppConfiguration configuration) {
                return configuration.getForageConfiguration();
            }
        });
    }

    @Override
    public void run(MyAppConfiguration configuration, Environment environment) {
        // Register your resources, health checks, etc.
    }
}
```

## Using the Search Engine

The bundle automatically creates and manages the search engine. Access it through dependency injection or by storing a
reference:

```java
public class MyApplication extends Application<MyAppConfiguration> {

    private ForageBundle<MyAppConfiguration, Book> forageBundle;

    @Override
    public void initialize(Bootstrap<MyAppConfiguration> bootstrap) {
        this.forageBundle = new ForageBundle<>() {
            // ... implement methods
        };
        bootstrap.addBundle(forageBundle);
    }

    @Override
    public void run(MyAppConfiguration configuration, Environment environment) {
        // Get the search engine from the bundle
        var searchEngine = forageBundle.searchEngine();

        // Register a search resource
        environment.jersey().register(new SearchResource(searchEngine));
    }
}
```

## Creating a Search Resource

```java
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.SearchEngine;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final SearchEngine<ForageQuery, ForageQueryResult<Book>> searchEngine;

    public SearchResource(SearchEngine<ForageQuery, ForageQueryResult<Book>> searchEngine) {
        this.searchEngine = searchEngine;
    }

    @GET
    @Path("/books")
    public ForageQueryResult<Book> searchBooks(
            @QueryParam("q") String query,
            @QueryParam("field") @DefaultValue("title") String field,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        return searchEngine.search(
                QueryBuilder.matchQuery(field, query).buildForageQuery(limit)
        );
    }

    @GET
    @Path("/books/advanced")
    public ForageQueryResult<Book> advancedSearch(
            @QueryParam("title") String title,
            @QueryParam("author") String author,
            @QueryParam("minRating") Float minRating,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        var queryBuilder = QueryBuilder.booleanQuery();

        if (title != null) {
            queryBuilder.query(QueryBuilder.matchQuery("title", title).boost(2.0f).build());
        }
        if (author != null) {
            queryBuilder.query(QueryBuilder.matchQuery("author", author).build());
        }
        if (minRating != null) {
            queryBuilder.query(QueryBuilder.floatRangeQuery("rating", minRating, 5.0f).build());
        }

        return searchEngine.search(
                queryBuilder.clauseType(ClauseType.MUST).buildForageQuery(limit)
        );
    }
}
```

## Complete Example

Here's a complete Dropwizard application with Forage:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.dropwizard.bundle.ForageBundle;
import com.livetheoogway.forage.dropwizard.bundle.ForageConfiguration;
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.SearchEngine;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.model.index.field.FloatField;
import com.livetheoogway.forage.search.engine.model.index.field.TextField;
import com.livetheoogway.forage.search.engine.store.Store;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BookSearchApplication extends Application<BookSearchApplication.AppConfig> {

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AppConfig extends Configuration {
        private ForageConfiguration forageConfiguration = new ForageConfiguration();
    }

    @Data
    public static class Book {
        private String id, title, author;
        private float rating;
    }

    public static class BookStore implements Bootstrapper<IndexableDocument>, Store<Book> {
        private final Map<String, Book> books = new ConcurrentHashMap<>();

        public void addBook(Book book) {
            books.put(book.getId(), book);
        }

        @Override
        public void bootstrap(Consumer<IndexableDocument> consumer) {
            books.values().forEach(book -> consumer.accept(
                    new ForageDocument(book.getId(), Arrays.asList(
                            new TextField("title", book.getTitle()),
                            new TextField("author", book.getAuthor()),
                            new FloatField("rating", new float[]{book.getRating()})
                    ))
            ));
        }

        @Override
        public Map<String, Book> get(List<String> ids) {
            return ids.stream()
                    .filter(books::containsKey)
                    .collect(Collectors.toMap(id -> id, books::get));
        }
    }

    private final BookStore bookStore = new BookStore();
    private ForageBundle<AppConfig, Book> forageBundle;

    public static void main(String[] args) throws Exception {
        new BookSearchApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        forageBundle = new ForageBundle<>() {
            @Override
            public Store<Book> dataStore(AppConfig config) {
                return bookStore;
            }

            @Override
            public Bootstrapper<IndexableDocument> bootstrap(AppConfig config) {
                return bookStore;
            }

            @Override
            public ForageConfiguration forageConfiguration(AppConfig config) {
                return config.getForageConfiguration();
            }
        };
        bootstrap.addBundle(forageBundle);
    }

    @Override
    public void run(AppConfig config, Environment environment) {
        // Add sample data
        Book book = new Book();
        book.setId("1");
        book.setTitle("Effective Java");
        book.setAuthor("Joshua Bloch");
        book.setRating(4.7f);
        bookStore.addBook(book);

        // Register search resource
        environment.jersey().register(new SearchResource(forageBundle.searchEngine()));
    }

    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public static class SearchResource {
        private final SearchEngine<ForageQuery, ForageQueryResult<Book>> engine;

        public SearchResource(SearchEngine<ForageQuery, ForageQueryResult<Book>> engine) {
            this.engine = engine;
        }

        @GET
        public ForageQueryResult<Book> search(@QueryParam("q") String query) {
            return engine.search(QueryBuilder.matchQuery("title", query).buildForageQuery(10));
        }
    }
}
```

## Benefits of the Bundle

| Feature                  | Description                            |
|--------------------------|----------------------------------------|
| **Lifecycle Management** | Automatic startup and shutdown         |
| **Health Checks**        | Built-in health check for index status |
| **Configuration**        | YAML-based configuration               |
| **Thread Safety**        | Proper handling of concurrent access   |

## Next Steps

- [Core Concepts](../core-concepts/index.md) - Deep dive into Forage internals
- [Query Types](../queries/index.md) - All available query types
- [Scoring & Ranking](../scoring/index.md) - Customize search relevance
