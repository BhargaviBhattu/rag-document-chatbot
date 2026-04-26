# 📄 RAG Document Q&A Chatbot

AI-powered chatbot that answers questions from any PDF document.

## 🛠️ Tech Stack
- Java 17 + Spring Boot 3.2
- LangChain4j (RAG Pipeline)
- AllMiniLM (Local Embeddings)
- Groq API - LLaMA 3.3 (Free LLM)
- Apache PDFBox (PDF Parsing)
- HTML/CSS/JS Frontend

## 🏗️ Architecture
PDF → Extract Text → Chunk → Embed → Vector Store
Question → Embed → Similarity Search → Groq LLM → Answer

## 🚀 Setup
1. Clone the repo
2. Get free API key at https://console.groq.com
3. Set environment variable:
   $env:GROQ_API_KEY="your_key_here"
4. Run: mvn spring-boot:run
5. Open: http://localhost:8080
<img width="1783" height="957" alt="Screenshot 2026-04-26 143853" src="https://github.com/user-attachments/assets/f5dff5d4-97cc-4477-b220-6df3d5f9d911" />

<img width="1783" height="958" alt="Screenshot 2026-04-26 143910" src="https://github.com/user-attachments/assets/7279f5d0-66ed-47d7-9918-fb6dae1fce01" />
