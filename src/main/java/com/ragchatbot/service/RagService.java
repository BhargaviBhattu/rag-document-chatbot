package com.ragchatbot.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final OpenAiChatModel chatModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private boolean documentLoaded = false;

    public RagService(@Value("${groq.api.key}") String apiKey) {

        // Free local embedding — no API needed
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // Groq uses OpenAI-compatible API — just different baseUrl and model
        this.chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName("llama-3.3-70b-versatile")
                .temperature(0.3)
                .build();

        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    public String ingestPdf(MultipartFile file) throws IOException {

        byte[] bytes = file.getBytes();
        PDDocument pdDocument = Loader.loadPDF(bytes);
        PDFTextStripper stripper = new PDFTextStripper();
        String rawText = stripper.getText(pdDocument);
        pdDocument.close();

        if (rawText.isBlank()) {
            return "PDF appears to be empty or scanned (no extractable text).";
        }

        Document document = Document.from(rawText);
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        this.embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        this.documentLoaded = true;
        return "Successfully processed " + segments.size() + " chunks from your PDF.";
    }

    public String answer(String question) {

        if (!documentLoaded) {
            return "Please upload a PDF document first before asking questions.";
        }

        Embedding questionEmbedding = embeddingModel.embed(question).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(3)
                .build();

        List<EmbeddingMatch<TextSegment>> relevantMatches =
                embeddingStore.search(searchRequest).matches();

        if (relevantMatches.isEmpty()) {
            return "No relevant content found in the document for your question.";
        }

        String context = relevantMatches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        String ragPrompt = """
                You are a helpful assistant that answers questions based ONLY on the provided document context.
                If the answer is not found in the context, say "I couldn't find this information in the document."
                Do not make up information.
                
                DOCUMENT CONTEXT:
                %s
                
                QUESTION: %s
                
                ANSWER:
                """.formatted(context, question);

        return chatModel.generate(ragPrompt);
    }

    public boolean isDocumentLoaded() {
        return documentLoaded;
    }
}

