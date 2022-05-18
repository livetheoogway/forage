package com.livetheoogway.forage.search.engine;

import com.livetheoogway.forage.search.engine.model.Book;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryEngineTest {

    @Test
    void testBooksReader() throws IOException {
        final List<Book> books = ResourceReader.extractBooks();
        assertEquals(11123, books.size());
    }



}