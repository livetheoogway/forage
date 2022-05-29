package com.livetheoogway.forage.search.engine;

import java.util.List;

/**
 *
 * @param <D>
 * @param <Q>
 * @param <R>
 */
public interface IndexBuilder<D, Q, R> {
    void writeDocument(List<D> documents);
    SearchEngine<Q, R> build();
}
