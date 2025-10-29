package com.eureka;

import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides functionality for indexing and searching notes using Apache Lucene.
 * Manages a Lucene index directory to store searchable note data.
 */
public class SearchService {

    /**
     * The Lucene Directory where the search index is stored.
     */
    private final Directory indexDirectory;
    /**
     * The Lucene Analyzer used for indexing and querying (tokenizing text).
     */
    private final StandardAnalyzer analyzer;

    /**
     * Represents a single search result found within a note.
     * Includes details about the note, the match position, and the query.
     * @param noteId    The unique ID of the {@link Note} containing the match.
     * @param title     The title of the {@link Note}.
     * @param setName   The name of the {@link NoteSet} the note belongs to.
     * @param position  The starting character index of the query match within the note's content.
     * @param wordIndex The approximate word count preceding the query match.
     * @param query     The original search query string that produced this result.
     */
    public record SearchResult(String noteId, String title, String setName, int position, int wordIndex, String query) {}

    /**
     * Constructs the SearchService.
     * Opens or creates the Lucene index directory at the specified path.
     * Initializes the analyzer and performs an initial index rebuild.
     * @param storagePath The base {@link Path} where the 'search_index' subdirectory will be created/opened.
     * @throws IOException If an error occurs opening or creating the index directory.
     */
    public SearchService(Path storagePath) throws IOException {
        Path indexPath = storagePath.resolve("search_index");
        this.indexDirectory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();
        System.out.println("SearchService: Initializing and rebuilding index at " + indexPath.toAbsolutePath() + "...");
        rebuildIndex();
        System.out.println("SearchService: Index rebuild complete.");
    }

    /**
     * Clears the existing search index and rebuilds it entirely from the current AppState.
     * Iterates through all note sets and notes, creating a Lucene document for each note.
     * @throws IOException If an error occurs while writing to the index.
     */
    public void rebuildIndex() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            writer.deleteAll();
            for (NoteSet set : AppState.getInstance().getSets()) {
                for (Note note : AppState.getInstance().getNotesForSet(set.getId())) {
                    writer.addDocument(createDocument(note, set.getName()));
                }
            }
        }
    }

    /**
     * Adds a new note to the index or updates an existing one.
     * If a document with the same note ID already exists, it is replaced.
     * Retrieves the set name from AppState to include in the document.
     * @param note The {@link Note} object to add or update in the index.
     */
    public void addOrUpdateNote(Note note) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            Optional<NoteSet> setOpt = AppState.getInstance().getSetById(note.getSetId());
            Consumer<NoteSet> updateAction = noteSet -> {
                try {
                    Document doc = createDocument(note, noteSet.getName());
                    writer.updateDocument(new Term("id", note.getId()), doc);
                } catch (IOException e) {
                    System.err.println("Error creating/updating document for note ID: " + note.getId());
                    e.printStackTrace();
                }
            };
            setOpt.ifPresent(updateAction);

        } catch (IOException e) {
            System.err.println("Error opening IndexWriter for add/update operation:");
            e.printStackTrace();
        }
    }

    /**
     * Deletes a note from the search index based on its ID.
     * @param note The {@link Note} object to delete from the index. The ID field is used.
     */
    public void deleteNote(Note note) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            writer.deleteDocuments(new Term("id", note.getId()));
        } catch (IOException e) {
            System.err.println("Error deleting document for note ID: " + note.getId());
            e.printStackTrace();
        }
    }

    /**
     * Searches the index for notes containing the given query string in their title or content.
     * Returns a list of SearchResult objects, where each object represents one occurrence
     * of the query string within a note.
     * @param queryString The text to search for. Case-insensitive matching is performed.
     * @return A List of {@link SearchResult} objects representing all occurrences found.
     * Returns an empty list if the query is blank or no matches are found.
     * @throws ParseException If the query string has syntax errors according to Lucene's query parser.
     * @throws IOException    If an error occurs while reading from the index.
     * @throws Exception      General exception catch-all (consider refining).
     */
    public List<SearchResult> search(String queryString) throws ParseException, IOException {
        System.out.println("DEBUG: SearchService.search called with query: '" + queryString + "'");

        if (queryString == null || queryString.isBlank()) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(QueryParser.escape(queryString.toLowerCase()));

            TopDocs topDocs = searcher.search(query, 100);
            ScoreDoc[] hits = topDocs.scoreDocs;
            System.out.println("DEBUG: Lucene found " + hits.length + " matching documents.");

            StoredFields storedFields = searcher.storedFields();

            for (ScoreDoc hit : hits) {
                Document doc = storedFields.document(hit.doc);
                String noteId = doc.get("id");

                Optional<Note> noteOpt = AppState.getInstance().getNoteById(noteId);
                if (noteOpt.isPresent()) {
                    Note note = noteOpt.get();
                    String content = note.getContent();
                    if (content == null) continue;

                    String contentLower = content.toLowerCase();
                    String queryLower = queryString.toLowerCase();

                    int index = contentLower.indexOf(queryLower);
                    while (index >= 0) {
                        String textBeforeMatch = content.substring(0, index);
                        int wordCount = textBeforeMatch.trim().isEmpty() ? 1 : textBeforeMatch.trim().split("\\s+").length;

                        results.add(new SearchResult(
                                note.getId(),
                                note.getTitle(),
                                doc.get("setName"),
                                index,
                                wordCount,
                                queryString
                        ));
                        index = contentLower.indexOf(queryLower, index + 1);
                    }
                } else {
                    System.err.println("Warning: Note ID " + noteId + " found in index but not in AppState.");
                }
            }
        }
        System.out.println("DEBUG: SearchService returning " + results.size() + " total occurrences.");
        return results;
    }

    /**
     * Creates a Lucene {@link Document} for a given {@link Note}.
     * The document includes fields for ID, title, content (combined title and body for searching),
     * and set name.
     * @param note    The {@link Note} to index.
     * @param setName The name of the {@link NoteSet} the note belongs to.
     * @return A Lucene {@link Document} ready for indexing.
     * @throws IOException (Potentially thrown by field constructors, though unlikely here).
     */
    private Document createDocument(Note note, String setName) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", note.getId(), Field.Store.YES));
        doc.add(new TextField("title", note.getTitle(), Field.Store.YES));
        doc.add(new TextField("content", note.getTitle() + " " + note.getContent(), Field.Store.NO));
        doc.add(new StringField("setName", setName, Field.Store.YES));
        return doc;
    }
}