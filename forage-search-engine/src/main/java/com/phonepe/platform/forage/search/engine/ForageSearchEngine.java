package com.phonepe.platform.forage.search.engine;

import java.io.Closeable;

/**
 * The search engine is the primary facade to do all things search
 *
 * @param <D> Document type which is supposed to be indexed
 * @param <Q> Query model
 * @param <R> Result model
 */
public abstract class ForageSearchEngine<D, Q, R> implements QueryEngine<Q, R>, DocumentIndexer<D>, Closeable {
}
