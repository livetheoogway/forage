/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.search.engine.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.ForageQueryVisitor;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.PageQuery;
import com.livetheoogway.forage.models.result.DocScore;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.models.result.Relation;
import com.livetheoogway.forage.models.result.TotalResults;
import com.livetheoogway.forage.search.engine.DocumentIndexer;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.pagination.LucenePagination;
import com.livetheoogway.forage.search.engine.lucene.pagination.SearchAfter;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserFactory;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.model.opresult.OperationResult;
import com.livetheoogway.forage.search.engine.operation.OperationExecutor;
import com.livetheoogway.forage.search.engine.store.Store;
import com.livetheoogway.forage.search.engine.util.ArrayUtils;
import com.livetheoogway.forage.search.engine.util.ForageConverters;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ForageLuceneSearchEngine<D>
        implements ForageSearchEngine<D>, DocumentIndexer<IndexableDocument>, Closeable {

    private final LuceneDocumentHandler documentHandler;
    private final LuceneIndex luceneIndex;
    private final LuceneQueryGenerator luceneQueryGenerator;
    private final LucenePagination lucenePagination;
    private final Store<D> dataStore;
    private final QueryParser queryParser;

    /**
     * @param mapper             mapper for encoding page info
     * @param queryParserFactory supplier of query parsers
     * @param dataStore          store that gives the data element given anid
     * @param analyzer           lucene analyser class
     */
    public ForageLuceneSearchEngine(final ObjectMapper mapper,
                                    final QueryParserFactory queryParserFactory,
                                    final Store<D> dataStore,
                                    final Analyzer analyzer) {
        this.documentHandler = new LuceneDocumentHandler();
        this.luceneIndex = new LuceneIndexInstance(analyzer);
        this.luceneQueryGenerator = new LuceneQueryGenerator(analyzer, queryParserFactory);
        this.lucenePagination = new LucenePagination(mapper);
        this.dataStore = dataStore;
        this.queryParser = new QueryParser("TEMP", analyzer); //todo needs a more elegant solution for page parsing
    }

    @Override
    public OperationResult<IndexableDocument> index(final List<IndexableDocument> documents) {
        return OperationExecutor.execute(documents, document
                -> luceneIndex.indexWriter().addDocument(document.accept(documentHandler)));
    }

    @Override
    public void flush() throws ForageSearchError {
        log.info("Flushing lucene with all newly indexed items");
        luceneIndex.flush();
        log.info("Flushing done");
    }

    @SneakyThrows
    @Override
    public ForageQueryResult<D> search(final ForageQuery forageQuery) throws ForageSearchError {
        val searcher = luceneIndex.searcher();
        return forageQuery.accept(new ForageQueryVisitor<>() {
            @Override
            public ForageQueryResult<D> visit(final ForageSearchQuery forageSearchQuery) throws ForageSearchError {
                try {
                    val query = forageSearchQuery.getQuery().accept(luceneQueryGenerator);
                    val topDocs = searcher.search(query, forageSearchQuery.getSize());
                    return extractResult(query.toString(), topDocs);
                } catch (Exception e) {
                    throw ForageSearchError.propagate(ForageErrorCode.SEARCH_ERROR, e);
                }
            }

            @Override
            public ForageQueryResult<D> visit(final PageQuery pageQuery) throws ForageSearchError {
                try {
                    val searchAfter = lucenePagination.parsePage(pageQuery.getPage());
                    val topDocs = searcher.searchAfter(ForageConverters.toScoreDoc(searchAfter.getAfter()),
                                                       queryParser.parse(searchAfter.getQuery()),
                                                       pageQuery.getSize());
                    return extractResult(searchAfter.getQuery(), topDocs);

                } catch (IOException e) {
                    throw new ForageSearchError(ForageErrorCode.SEARCH_ERROR, e);
                } catch (ParseException e) {
                    throw new ForageSearchError(ForageErrorCode.PAGE_QUERY_PARSE_ERROR, e);
                }
            }
        });
    }

    private ForageQueryResult<D> extractResult(final String query,
                                               final TopDocs topDocs) throws ForageSearchError {
        if (topDocs == null || topDocs.scoreDocs.length <= 0) {
            return ForageQueryResult.<D>builder()
                    .matchingResults(Collections.emptyList())
                    .total(new TotalResults(0, Relation.EQUAL_TO))
                    .build();
        }
        val last = ArrayUtils.last(topDocs.scoreDocs);
        val searchAfter = last
                .map(value -> new SearchAfter(ForageConverters.toDocScore(value), query))
                .orElse(null);
        val docRetriever = luceneIndex.docRetriever();

        final List<IntermediateDocResult> intermediateDocResults = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> {
                    val doc = docRetriever.document(scoreDoc.doc);
                    final String docId = documentHandler.extractId(doc);
                    return new IntermediateDocResult(docId, ForageConverters.toDocScore(scoreDoc));
                }).collect(Collectors.toList());

        final Map<String, D> dataStoreResults = dataStore.get(
                intermediateDocResults.stream().map(IntermediateDocResult::getDocId).collect(Collectors.toList()));

        final List<MatchingResult<D>> matchingResults = intermediateDocResults
                .stream()
                .map(docResult -> new MatchingResult<>(docResult.getDocId(), dataStoreResults.get(docResult.getDocId()),
                                                   docResult.getDocScore())).collect(Collectors.toList());

        return ForageQueryResult.<D>builder()
                .matchingResults(matchingResults)
                .total(ForageConverters.toTotalResults(topDocs.totalHits))
                .nextPage(lucenePagination.generatePage(searchAfter))
                .build();
    }

    @Override
    public void close() throws IOException {
        luceneIndex.close();
    }

    @Value
    public static class IntermediateDocResult {
        String docId;
        DocScore docScore;
    }
}

