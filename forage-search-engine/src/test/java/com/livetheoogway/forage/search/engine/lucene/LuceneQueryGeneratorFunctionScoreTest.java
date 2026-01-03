package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.query.search.FunctionScoreQuery;
import com.livetheoogway.forage.models.query.search.MatchAllQuery;
import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.DecayFunction;
import com.livetheoogway.forage.models.query.search.score.DecayType;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScriptScoreFunction;
import com.livetheoogway.forage.models.query.search.score.WeightedScoreFunction;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserSupplier;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuceneQueryGeneratorFunctionScoreTest {

    private Analyzer analyzer;
    private LuceneQueryGenerator generator;

    @BeforeEach
    void setUp() {
        analyzer = new StandardAnalyzer();
        QueryParserSupplier supplier = field -> new QueryParser(field, analyzer);
        generator = new LuceneQueryGenerator(analyzer, supplier);
    }

    @Test
    void constantScoreFunctionProducesUniformScores() throws Exception {
        Query query = toLuceneQuery(new ConstantScoreFunction(2.5f));

        try (SearchContext context = buildIndex(
                document("doc-1"),
                document("doc-2"))) {
            TopDocs topDocs = context.searcher().search(query, 10);
            assertEquals(2, topDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                assertEquals(2.5f, scoreDoc.score, 1e-4);
            }
        }
    }

    @Test
    void weightedScoreFunctionRespectsProvidedWeight() throws Exception {
        Query query = toLuceneQuery(new WeightedScoreFunction(4.25f));

        try (SearchContext context = buildIndex(
                document("doc-1"),
                document("doc-2"))) {
            TopDocs topDocs = context.searcher().search(query, 10);
            assertEquals(2, topDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                assertEquals(4.25f, scoreDoc.score, 1e-4);
            }
        }
    }

    @Test
    void fieldValueFactorFunctionPullsValuesFromDocValuesFields() throws Exception {
        Query query = toLuceneQuery(new FieldValueFactorFunction("popularity"));

        try (SearchContext context = buildIndex(
                document("low", doc -> doc.add(new DoubleDocValuesField("popularity", 1.0))),
                document("high", doc -> doc.add(new DoubleDocValuesField("popularity", 5.0))))) {
            TopDocs topDocs = context.searcher().search(query, 10);

            ScoreDoc high = scoreDoc(topDocs, context.searcher(), "high");
            ScoreDoc low = scoreDoc(topDocs, context.searcher(), "low");

            assertEquals(5.0f, high.score, 1e-4);
            assertEquals(1.0f, low.score, 1e-4);
            assertTrue(high.score > low.score);
        }
    }

    @Test
    void scriptScoreFunctionEvaluatesExpressionWithBindings() throws Exception {
        ScriptScoreFunction scoreFunction = new ScriptScoreFunction("score + popularity + freshness");
        Query query = toLuceneQuery(scoreFunction);

        try (SearchContext context = buildIndex(
                document("doc-1", doc -> {
                    doc.add(new DoubleDocValuesField("popularity", 2.0));
                    doc.add(new DoubleDocValuesField("freshness", 1.0));
                }),
                document("doc-2", doc -> {
                    doc.add(new DoubleDocValuesField("popularity", 1.0));
                    doc.add(new DoubleDocValuesField("freshness", 0.0));
                }))) {
            TopDocs topDocs = context.searcher().search(query, 10);

            ScoreDoc first = scoreDoc(topDocs, context.searcher(), "doc-1");
            ScoreDoc second = scoreDoc(topDocs, context.searcher(), "doc-2");

            assertEquals(4.0f, first.score, 1e-4);
            assertEquals(2.0f, second.score, 1e-4);
            assertTrue(first.score > second.score);
        }
    }

    @Test
    void randomScoreFunctionUsesSeedAndDocValuesField() throws Exception {
        long seed = 3L;
        RandomScoreFunction scoreFunction = new RandomScoreFunction(seed, "random_value");
        Query query = toLuceneQuery(scoreFunction);

        try (SearchContext context = buildIndex(
                document("doc-1", doc -> doc.add(new DoubleDocValuesField("random_value", 1.0))),
                document("doc-2", doc -> doc.add(new DoubleDocValuesField("random_value", 2.0))))) {
            TopDocs topDocs = context.searcher().search(query, 10);

            ScoreDoc doc1 = scoreDoc(topDocs, context.searcher(), "doc-1");
            ScoreDoc doc2 = scoreDoc(topDocs, context.searcher(), "doc-2");

            float expectedDoc1Score = (float) Math.abs(Math.sin(1.0 + seed));
            float expectedDoc2Score = (float) Math.abs(Math.sin(2.0 + seed));

            assertEquals(expectedDoc1Score, doc1.score, 1e-4);
            assertEquals(expectedDoc2Score, doc2.score, 1e-4);
        }
    }

    @Test
    void decayFunctionSupportsAllDecayTypes() throws Exception {
        for (DecayType decayType : DecayType.values()) {
            DecayFunction scoreFunction = new DecayFunction(0.0, 10.0, 0.0, 0.5, decayType, "freshness");
            Query query = toLuceneQuery(scoreFunction);

            try (SearchContext context = buildIndex(
                    document("near", doc -> doc.add(new DoubleDocValuesField("freshness", 0.0))),
                    document("far", doc -> doc.add(new DoubleDocValuesField("freshness", 8.0))))) {
                TopDocs topDocs = context.searcher().search(query, 10);

                ScoreDoc near = scoreDoc(topDocs, context.searcher(), "near");
                ScoreDoc far = scoreDoc(topDocs, context.searcher(), "far");

                float expectedNear = (float) expectedDecayScore(scoreFunction, 0.0);
                float expectedFar = (float) expectedDecayScore(scoreFunction, 8.0);

                assertEquals(expectedNear, near.score, 1e-4);
                assertEquals(expectedFar, far.score, 1e-4);
                assertTrue(near.score >= far.score,
                        () -> "Decay type " + decayType + " should score nearer docs higher");
            }
        }
    }

    private double expectedDecayScore(DecayFunction function, double fieldValue) {
        double distance = Math.max(0.0, Math.abs(fieldValue - function.getOrigin()) - function.getOffset());
        double scale = function.getScale();
        double decay = function.getDecay();
        return switch (function.getDecayType()) {
            case GAUSS -> Math.exp(Math.log(decay) * Math.pow(distance / scale, 2));
            case EXP -> Math.exp(Math.log(decay) * (distance / scale));
            case LINEAR -> Math.max(0.0, (scale - distance) / (scale / (1 - decay)));
        };
    }

    private Query toLuceneQuery(ScoreFunction scoreFunction) throws Exception {
        FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(new MatchAllQuery(), scoreFunction);
        return generator.visit(functionScoreQuery);
    }

    private SearchContext buildIndex(Document... documents) throws IOException {
        Directory directory = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (Document document : documents) {
                writer.addDocument(document);
            }
        }
        DirectoryReader reader = DirectoryReader.open(directory);
        return new SearchContext(directory, reader);
    }

    private Document document(String id) {
        return document(id, doc -> {});
    }

    private Document document(String id, Consumer<Document> customizer) {
        Document document = new Document();
        document.add(new StringField("id", id, Field.Store.YES));
        customizer.accept(document);
        return document;
    }

    private ScoreDoc scoreDoc(TopDocs topDocs, IndexSearcher searcher, String id) throws IOException {
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            if (id.equals(document.get("id"))) {
                return scoreDoc;
            }
        }
        throw new IllegalStateException("Unable to find document with id " + id);
    }

    private static final class SearchContext implements AutoCloseable {
        private final Directory directory;
        private final DirectoryReader reader;
        private final IndexSearcher searcher;

        private SearchContext(Directory directory, DirectoryReader reader) {
            this.directory = directory;
            this.reader = reader;
            this.searcher = new IndexSearcher(reader);
        }

        private IndexSearcher searcher() {
            return searcher;
        }

        @Override
        public void close() throws IOException {
            reader.close();
            directory.close();
        }
    }
}
