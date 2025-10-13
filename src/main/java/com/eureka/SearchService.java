package com.eureka;

import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchService {

    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;

    public record SearchResult(String noteId, String title, String setName, int position, int wordIndex, String query) {}

    public SearchService(Path storagePath) throws IOException {
        this.indexDirectory = FSDirectory.open(storagePath.resolve("search_index"));
        this.analyzer = new StandardAnalyzer();
        System.out.println("SearchService: Initializing and rebuilding index...");
        rebuildIndex();
        System.out.println("SearchService: Index rebuild complete.");
    }

    public void rebuildIndex() throws IOException {
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            writer.deleteAll();
            for (NoteSet set : AppState.getInstance().getSets()) {
                for (Note note : AppState.getInstance().getNotesForSet(set.getId())) {
                    writer.addDocument(createDocument(note, set.getName()));
                }
            }
        }
    }

    public void addOrUpdateNote(Note note) {
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            Optional<NoteSet> set = AppState.getInstance().getSetById(note.getSetId());
            set.ifPresent(noteSet -> {
                try {
                    Document doc = createDocument(note, noteSet.getName());
                    writer.updateDocument(new Term("id", note.getId()), doc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(Note note) {
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            writer.deleteDocuments(new Term("id", note.getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<SearchResult> search(String queryString) throws Exception {
        System.out.println("DEBUG: SearchService.search called with query: '" + queryString + "'");

        if (queryString == null || queryString.isBlank()) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(QueryParser.escape(queryString.toLowerCase()));

            ScoreDoc[] hits = searcher.search(query, 100).scoreDocs;
            System.out.println("DEBUG: Lucene found " + hits.length + " matching documents.");

            for (ScoreDoc hit : hits) {
                Document doc = searcher.storedFields().document(hit.doc);
                String noteId = doc.get("id");

                Optional<Note> noteOpt = AppState.getInstance().getNoteById(noteId);
                if (noteOpt.isPresent()) {
                    Note note = noteOpt.get();
                    String content = note.getContent();
                    String contentLower = content.toLowerCase();
                    String queryLower = queryString.toLowerCase();

                    int index = contentLower.indexOf(queryLower);
                    while (index >= 0) {
                        String textBeforeMatch = content.substring(0, index);
                        int wordCount = textBeforeMatch.trim().isEmpty() ? 1 : textBeforeMatch.trim().split("\\s+").length;

                        results.add(new SearchResult(
                                note.getId(), note.getTitle(), doc.get("setName"),
                                index, wordCount, queryString
                        ));
                        index = contentLower.indexOf(queryLower, index + 1);
                    }
                }
            }
        }
        System.out.println("DEBUG: SearchService returning " + results.size() + " total occurrences.");
        return results;
    }

    private Document createDocument(Note note, String setName) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", note.getId(), Field.Store.YES));
        doc.add(new TextField("title", note.getTitle(), Field.Store.YES));
        doc.add(new TextField("content", note.getTitle() + " " + note.getContent(), Field.Store.NO));
        doc.add(new StringField("setName", setName, Field.Store.YES));
        return doc;
    }
}