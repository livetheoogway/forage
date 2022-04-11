package com.phonepe.platform.forage.search.engine.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.models.result.MatchingResult;
import com.phonepe.platform.forage.models.result.Relation;
import com.phonepe.platform.forage.models.result.TotalResults;
import com.phonepe.platform.forage.search.engine.ForageSearchEngine;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.pagination.LucenePagination;
import com.phonepe.platform.forage.search.engine.lucene.pagination.SearchAfter;
import com.phonepe.platform.forage.search.engine.lucene.parser.QueryParserFactory;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageQuery;
import com.phonepe.platform.forage.search.engine.model.query.ForageQueryVisitor;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.PageQuery;
import com.phonepe.platform.forage.search.engine.model.result.OperationResult;
import com.phonepe.platform.forage.search.engine.operation.OperationExecutor;
import com.phonepe.platform.forage.search.engine.util.ArrayUtils;
import com.phonepe.platform.forage.search.engine.util.ForageConverters;
import lombok.val;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LuceneSearchEngine<Data>
        extends ForageSearchEngine<IndexableDocument<Data>, ForageQuery, ForageQueryResult<Data>> {

    private final LuceneDocumentHandler<Data> documentHandler;
    private final LuceneIndex luceneIndex;
    private final LuceneQueryGenerator luceneQueryGenerator;
    private final LucenePagination lucenePagination;
    private final InMemoryHashStore<Data> inMemoryHashStore;
    private final QueryParser queryParser;

    public LuceneSearchEngine(final ObjectMapper mapper,
                              final QueryParserFactory queryParserFactory,
                              final Analyzer analyzer) throws ForageSearchError {
        this.documentHandler = new LuceneDocumentHandler<>();
        this.luceneIndex = new LuceneIndexInstance(analyzer);
        this.luceneQueryGenerator = new LuceneQueryGenerator(queryParserFactory);
        this.lucenePagination = new LucenePagination(mapper);
        this.inMemoryHashStore = new InMemoryHashStore<>(); //todo
        this.queryParser = new QueryParser("TEMP", analyzer); //todo
    }

    @Override
    public OperationResult<IndexableDocument<Data>> index(final List<IndexableDocument<Data>> documents) {
        return OperationExecutor.execute(documents, document -> {
            luceneIndex.indexWriter().addDocument(document.accept(documentHandler));
            inMemoryHashStore.store(document);
        });
    }

    @Override
    public void flush() throws ForageSearchError {
        luceneIndex.flush();
    }

    @Override
    public ForageQueryResult<Data> query(final ForageQuery forageQuery) throws ForageSearchError {
        val searcher = luceneIndex.searcher();
        return forageQuery.accept(new ForageQueryVisitor<>() {
            @Override
            public ForageQueryResult<Data> visit(final ForageSearchQuery forageSearchQuery) throws ForageSearchError {
                try {
                    val query = forageSearchQuery.getQuery().accept(luceneQueryGenerator);
                    val topDocs = searcher.search(query, forageSearchQuery.getSize());
                    return extractResult(query.toString(), topDocs);
                } catch (IOException e) {
                    throw new ForageSearchError(ForageErrorCode.SEARCH_ERROR, e);
                }
            }

            @Override
            public ForageQueryResult<Data> visit(final PageQuery pageQuery) throws ForageSearchError {
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

    private ForageQueryResult<Data> extractResult(final String query,
                                            final TopDocs topDocs) throws ForageSearchError {
        if (topDocs == null || topDocs.scoreDocs.length <= 0) {
            return ForageQueryResult.<Data>builder()
                    .total(new TotalResults(0, Relation.EQUAL_TO))
                    .build();
        }
        val last = ArrayUtils.last(topDocs.scoreDocs);
        val searchAfter = last
                .map(value -> new SearchAfter(ForageConverters.toDocScore(value), query))
                .orElse(null);
        val docRetriever = luceneIndex.docRetriever();

        final List<MatchingResult<Data>> matchingResults = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> {
                    val doc = docRetriever.document(scoreDoc.doc);
                    final String docId = documentHandler.extractId(doc);
                    return new MatchingResult<>(docId, inMemoryHashStore.get(docId), ForageConverters.toDocScore(scoreDoc));
                }).collect(Collectors.toList());

        return ForageQueryResult.<Data>builder()
                .matchingResults(matchingResults)
                .total(ForageConverters.toTotalResults(topDocs.totalHits))
                .nextPage(lucenePagination.generatePage(searchAfter))
                .build();
    }

    @Override
    public void close() throws IOException {
        luceneIndex.close();
    }
}

