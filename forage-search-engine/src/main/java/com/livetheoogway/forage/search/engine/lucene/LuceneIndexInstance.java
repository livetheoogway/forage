package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.util.ExceptionExecution;
import com.livetheoogway.forage.search.engine.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LuceneIndexInstance implements LuceneIndex {

    private final Directory memoryIndex;
    private final Analyzer analyzer;
    private final AtomicReference<IndexWriter> indexWriterReference;
    private final AtomicReference<DocRetriever> indexReaderReference;
    private final AtomicBoolean indexWriterReferenceChanged;

    public LuceneIndexInstance(Analyzer analyzer) throws ForageSearchError {
        this.analyzer = analyzer;
        memoryIndex = newInMemoryIndex();
//        final IndexReader indexReader = ExceptionExecution.get(() -> DirectoryReader.open(memoryIndex),
//                                                               ForageErrorCode.INDEX_READER_IO_ERROR);
//        final IndexSearcher searcher = new IndexSearcher(indexReader);
//        final DocRetriever docRetriever = new DocRetriever(indexReader, searcher);
        indexWriterReference = new AtomicReference<>();
        indexWriterReferenceChanged = new AtomicBoolean(false);
        indexReaderReference = new AtomicReference<>(null);
    }

    public static Directory newInMemoryIndex() {
        return new ByteBuffersDirectory();
    }

    @Override
    public void close() {
        if (indexReaderReference.get() != null) {
            Utils.closeSafe(indexReaderReference.get().getIndexReader(), "IndexReader");
        }
        Utils.closeSafe(indexWriterReference.get(), "IndexWriter");
        Utils.closeSafe(memoryIndex, "MemoryIndex");
    }

    @Override
    public IndexSearcher searcher() throws ForageSearchError {
        if (indexWriterReferenceChanged.get()) {
            synchronized (indexWriterReferenceChanged) {
                if (indexWriterReferenceChanged.get()) {
                    final IndexReader indexReader = ExceptionExecution.get(() -> DirectoryReader.open(memoryIndex),
                                                                           ForageErrorCode.INDEX_READER_IO_ERROR);
                    final IndexSearcher searcher = new IndexSearcher(indexReader);
                    final DocRetriever docRetriever = new DocRetriever(indexReader, searcher);
                    final DocRetriever docRetrieverToBeClosed = indexReaderReference.get();
                    indexReaderReference.set(docRetriever);
                    if (docRetrieverToBeClosed != null) {
                        Utils.closeSafe(docRetrieverToBeClosed.getIndexReader(), "IndexReader");
                    }
                    indexWriterReferenceChanged.set(false);
                }
            }
        }
        return indexReaderReference.get().getSearcher();
    }

    @Override
    public IndexWriter indexWriter() throws ForageSearchError {
        if (indexWriterReference.get() == null) {
            synchronized (indexWriterReference) {
                /* this synchronization is being done so that indexWriter is created only once, iff there exists no
                current writer */
                if (indexWriterReference.get() == null) {
                    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
                    final IndexWriter indexWriter = ExceptionExecution.get(
                            () -> new IndexWriter(memoryIndex, indexWriterConfig),
                            ForageErrorCode.INDEX_WRITER_IO_ERROR);
                    indexWriterReference.set(indexWriter);
                }
            }
        }
        return indexWriterReference.get();
    }

    @Override
    public synchronized void flush() throws ForageSearchError {
        if (indexWriterReference.get() == null) {
            throw ForageSearchError.raise(ForageErrorCode.INDEX_FLUSH_ERROR, "Nothing to flush");
        }
        final IndexWriter indexWriter = indexWriterReference.get();
        try {
            indexWriter.flush();
            Utils.closeSafe(indexWriter, "IndexWriter");
            indexWriterReference.set(null);
            indexWriterReferenceChanged.set(true);
        } catch (IOException e) {
            throw ForageSearchError.raise(ForageErrorCode.INDEX_FLUSH_ERROR, "Unable to flush", e);
        }
    }

    @Override
    public DocRetriever docRetriever() {
        return indexReaderReference.get();
    }

    @Override
    public LuceneIndex freshIndex() throws ForageSearchError {
        return new LuceneIndexInstance(this.analyzer);
    }

    public static void main(String[] args) throws IOException {
        Directory memoryIndex = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writter = new IndexWriter(memoryIndex, indexWriterConfig);
        writter.addDocument(getDocument("1", "Zero to one", "Peter theil"));
        writter.addDocument(getDocument("2", "Designing data intensive applications to do cool stuff", "Kannemann"));
        writter.addDocument(getDocument("3", "Harry potter", "jk rowling"));
        writter.close();


        IndexReader ir = DirectoryReader.open(memoryIndex);
        IndexSearcher indexSearcher = new IndexSearcher(ir);
        TopDocs search = indexSearcher.search(new TermQuery(new Term("title", "to")), 10);
        System.out.println("search = " + Arrays.toString(search.scoreDocs));
        indexWriterConfig = new IndexWriterConfig(analyzer);
        writter = new IndexWriter(memoryIndex, indexWriterConfig);
        writter.addDocument(getDocument("4", "To many more things coming", "jk rowling"));
        writter.close();

        ir = DirectoryReader.open(memoryIndex);
        indexSearcher = new IndexSearcher(ir);
        search = indexSearcher.search(new TermQuery(new Term("title", "to")), 10);
        System.out.println("search = " + Arrays.toString(search.scoreDocs));

        memoryIndex.close();
        ir.close();

    }


    public static Document getDocument(String id, String title, String author) {
        Document document = new Document();
        document.add(new org.apache.lucene.document.StringField("id", id, Field.Store.NO));
        document.add(new org.apache.lucene.document.TextField("title", title, Field.Store.NO));
        document.add(new org.apache.lucene.document.TextField("author", author, Field.Store.NO));
        return document;
    }
}
