package com.ragchatbot.controller;

    

import com.ragchatbot.model.AnswerResponse;
import com.ragchatbot.model.QuestionRequest;
import com.ragchatbot.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // Allow frontend to call backend
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG Chatbot is running!");
    }

    // Upload and process PDF
    @PostMapping("/upload")
    public ResponseEntity<AnswerResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AnswerResponse("No file provided.", "error"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(new AnswerResponse("Please upload a PDF file only.", "error"));
            }

            String result = ragService.ingestPdf(file);
            return ResponseEntity.ok(new AnswerResponse(result, "success"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new AnswerResponse("Error processing PDF: " + e.getMessage(), "error"));
        }
    }

    // Ask a question
    @PostMapping("/ask")
    public ResponseEntity<AnswerResponse> askQuestion(@RequestBody QuestionRequest request) {
        try {
            if (request.getQuestion() == null || request.getQuestion().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new AnswerResponse("Question cannot be empty.", "error"));
            }

            String answer = ragService.answer(request.getQuestion());
            return ResponseEntity.ok(new AnswerResponse(answer, "success"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new AnswerResponse("Error generating answer: " + e.getMessage(), "error"));
        }
    }

    // Check if document is loaded
    @GetMapping("/status")
    public ResponseEntity<AnswerResponse> status() {
        boolean loaded = ragService.isDocumentLoaded();
        String msg = loaded ? "Document loaded. Ready to answer questions." : "No document loaded yet.";
        return ResponseEntity.ok(new AnswerResponse(msg, loaded ? "ready" : "idle"));
    }
}

