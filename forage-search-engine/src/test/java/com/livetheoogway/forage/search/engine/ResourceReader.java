package com.livetheoogway.forage.search.engine;

import com.google.common.io.Resources;
import com.livetheoogway.forage.search.engine.model.Book;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ResourceReader {

    public static List<Book> extractBooks() throws IOException {
        final URL url = Resources.getResource("books_goodreads.csv");
        return Resources.readLines(url, Charset.defaultCharset())
                .stream()
                .map(line -> {
                    final String[] splits = line.split(",");
                    if (splits.length != 5) {
                        log.error("Error while processing line:" + line);
                        return null;
                    }
                    try {
                        return new Book(TestIdUtils.generateBookId(),
                                        splits[0].trim(),
                                        splits[1].trim(),
                                        Float.parseFloat(splits[2].trim()),
                                        splits[3].trim(),
                                        Integer.parseInt(splits[4].trim()));
                    } catch (Exception e) {
                        log.error("Error while processing line:" + line, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
