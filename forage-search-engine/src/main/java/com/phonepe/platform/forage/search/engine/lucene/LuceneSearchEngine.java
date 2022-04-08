package com.phonepe.platform.forage.search.engine.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.platform.forage.search.engine.ForageSearchEngine;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.pagination.LucenePagination;
import com.phonepe.platform.forage.search.engine.lucene.pagination.SearchAfter;
import com.phonepe.platform.forage.search.engine.lucene.parser.QueryParserFactory;
import com.phonepe.platform.forage.search.engine.model.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageQuery;
import com.phonepe.platform.forage.search.engine.model.query.ForageQueryVisitor;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.PageQuery;
import com.phonepe.platform.forage.search.engine.model.query.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.model.query.result.Relation;
import com.phonepe.platform.forage.search.engine.model.query.result.TotalResults;
import com.phonepe.platform.forage.search.engine.model.query.search.MatchingResult;
import com.phonepe.platform.forage.search.engine.model.result.OperationResult;
import com.phonepe.platform.forage.search.engine.operation.OperationExecutor;
import com.phonepe.platform.forage.search.engine.util.ArrayUtils;
import com.phonepe.platform.forage.search.engine.util.Converters;
import lombok.val;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LuceneSearchEngine
        extends ForageSearchEngine<IndexableDocument, ForageQuery, ForageQueryResult> {

    private final LuceneDocumentHandler documentHandler;
    private final LuceneIndex luceneIndex;
    private final LuceneQueryGenerator luceneQueryGenerator;
    private final LucenePagination lucenePagination;
    private final ObjectStore objectStore;

    public LuceneSearchEngine(final ObjectMapper mapper,
                              final QueryParserFactory queryParserFactory,
                              final Analyzer analyzer) throws ForageSearchError {
        this.documentHandler = new LuceneDocumentHandler();
        this.luceneIndex = new LuceneIndexInstance(analyzer);
        this.luceneQueryGenerator = new LuceneQueryGenerator(queryParserFactory);
        this.lucenePagination = new LucenePagination(mapper);
        this.objectStore = new ObjectStore();
    }

    @Override
    public OperationResult<IndexableDocument> index(final List<IndexableDocument> documents) {
        return OperationExecutor.execute(documents, document -> {
            luceneIndex.indexWriter().addDocument(document.accept(documentHandler));
            objectStore.store(document);
        });
    }

    @Override
    public void flush() throws ForageSearchError {
        luceneIndex.flush();
    }

    @Override
    public ForageQueryResult query(final ForageQuery forageQuery) throws ForageSearchError {
        val searcher = luceneIndex.searcher();
        return forageQuery.accept(new ForageQueryVisitor<>() {
            @Override
            public ForageQueryResult visit(final ForageSearchQuery forageSearchQuery) throws ForageSearchError {
                try {
                    val query = forageSearchQuery.getQuery().accept(luceneQueryGenerator);
                    val topDocs = searcher.search(query, forageSearchQuery.getSize());
                    return extractResult(query, topDocs);
                } catch (IOException e) {
                    throw new ForageSearchError(ForageErrorCode.SEARCH_ERROR, e);
                }
            }

            @Override
            public ForageQueryResult visit(final PageQuery pageQuery) throws ForageSearchError {
                try {
                    val searchAfter = lucenePagination.parsePage(pageQuery.getPage());
                    val topDocs = searcher.searchAfter(searchAfter.getAfter(), searchAfter.getQuery(),
                                                       pageQuery.getSize());
                    return extractResult(searchAfter.getQuery(), topDocs);

                } catch (IOException e) {
                    throw new ForageSearchError(ForageErrorCode.SEARCH_ERROR, e);
                }
            }
        });
    }

    private ForageQueryResult extractResult(final Query query,
                                            final TopDocs topDocs) throws ForageSearchError {
        if (topDocs == null || topDocs.scoreDocs.length <= 0) {
            return ForageQueryResult.builder()
                    .total(new TotalResults(0, Relation.EQUAL_TO))
                    .build();
        }
        val last = ArrayUtils.last(topDocs.scoreDocs);
        val searchAfter = last.map(value -> new SearchAfter(value, query)).orElse(null);
        val docRetriever = luceneIndex.docRetriever();

        final List<MatchingResult> matchingResults = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> {
                    val doc = docRetriever.document(scoreDoc.doc);
                    final String docId = documentHandler.extractId(doc);
//                    val fields = doc.getFields().stream()
//                            .map(LuceneFieldHandler::extractField)
//                            .collect(Collectors.toList());
                    return new MatchingResult(objectStore.get(docId), Converters.toDocScore(scoreDoc));
                }).collect(Collectors.toList());

        return ForageQueryResult.builder()
                .matchingResults(matchingResults)
                .total(Converters.toTotalResults(topDocs.totalHits))
                .nextPage(lucenePagination.generatePage(searchAfter))
                .build();
    }

}

