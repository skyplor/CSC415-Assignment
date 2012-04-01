/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csc415;

/**
 *
 * @author Sky
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
 
/**
 * Simple class that keeps article.
 */
class Article {
  public static final String AUTHOR_FIELD = "AUTHOR_FIELD";
  public static final String TEXT_FIELD = "TEXT_FIELD";
  public static final String ID_FIELD = "ID_FIELD";
  private Integer id;
  private String text;
  private String author;
  public Article(Integer id, String text, String author) {
    this.id = id;
    this.author = author;
    this.text = text;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }
  /**
   * Creates document for Lucene searching.
   *
   * @return Lucene's document
   */
  public Document createDocument() {
    Document document = new Document();
    document.add(new Field(AUTHOR_FIELD, author, Store.YES,
        Index.ANALYZED));
    document.add(new Field(TEXT_FIELD, text, Store.YES, Index.ANALYZED));
    document.add(new Field(ID_FIELD, id.toString(), Store.YES, Index.NO));
    return document;
  }
}
 
public class LuceneTest {
  /**
   * Directory to keep index data.
   * In this case we will use RAM directory,
   * which stands for keeping data in memory,
   * with no persitence.
   */
  private final Directory directory;
  /**
   * Analyzer for documents. Simple analyzer
   * will be enough for this case.
   */
  private final Analyzer analyzer;
  /**
   * The object for searching operations.
   */
  private IndexSearcher indexSearcher;
  public LuceneTest() throws Exception {
    directory = new RAMDirectory();
    analyzer = new StandardAnalyzer(Version.LUCENE_35);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
    /**
     * IndexWriter is used in writing data to index.
     */
    IndexWriter indexWriter = new IndexWriter(directory, config);
    /**
     * Create some data to be search.
     */
    for (Article art : new Article[] {
        new Article(1,
            "lucene has the great speed",
            "lucene developer"),
        new Article(2,
            "text-search engine - lucene - is for you",
            "pr"),
        new Article(3,
            "let's check out what lucene can give to us!",
            "codesmuggler")
    }) {
      /**
       * Adding document to our index.
       */
      indexWriter.addDocument(art.createDocument());
    }
    /**
     * Don't forget to close index writer!
     */
    indexWriter.close();
  }
  private void ensureIndexSearcherOpened() throws Exception {
    if (indexSearcher == null) {
      indexSearcher = new IndexSearcher(directory);
    }
  }
  public void searchAndPrint(String author, String text) throws Exception {
    ensureIndexSearcherOpened();
    /**
     * Query we will use in searching.
     */
    String q = Article.AUTHOR_FIELD + ":\"" + author + "\" OR "
        + Article.TEXT_FIELD + ": \"" + text + "\"";
    System.out.println("\n" + q);
    /**
     * Query parser is used for parsing string query.
     * It uses the same analyzer and Lucene Version for
     * parsing.
     */
    Query query = new QueryParser(Version.LUCENE_30, "", analyzer)
          .parse(q);
    /**
     * Searches and return max 3 docs from index.
     */
    printDocs(indexSearcher.search(query, 3).scoreDocs);
  }
  private void printDocs(ScoreDoc[] docs) throws Exception {
    ensureIndexSearcherOpened();
    System.out.println("MATCHES:");
    int i = 1;
    for (ScoreDoc scoreDoc : docs) {
      /**
       * Get document from index!
       */
      Document doc = indexSearcher.doc(scoreDoc.doc);
      /**
       * Document could be null if in the same time
       * someone has thrown out this document from index.
       */
      if (doc != null) {
        System.out.println(i++ + ":");
        System.out.println("AUTHOR: " + doc.get(Article.AUTHOR_FIELD));
        System.out.println("TEXT: " + doc.get(Article.TEXT_FIELD));
      }
    }
  }
  public void close() throws Exception {
    if (indexSearcher != null) {
      indexSearcher.close();
    }
    if (directory != null) {
      directory.close();
    }
    if (analyzer != null) {
      analyzer.close();
    }
  }
 
  /**
   * Do some tests!
   */
  public static void main(String[] args) throws Exception {
    LuceneTest lucene = new LuceneTest();
    lucene.searchAndPrint("", "speed"); // 1 doc
    lucene.searchAndPrint("codesmuggler", ""); // 1 doc
    lucene.searchAndPrint("", "lucene"); // 3 docs
    lucene.searchAndPrint("unknown", "weird words"); // 0 docs
    lucene.close();
  }
}
