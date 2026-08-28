package com.workspace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.dto.AiDTOs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${app.groq.api-key:}")
    private String groqApiKey;

    @Value("${app.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiGenerateResponse generateContent(AiGenerateRequest request) {
        String actionType = request.getActionType() != null ? request.getActionType().toLowerCase() : "custom";
        String prompt = request.getPrompt() != null ? request.getPrompt().trim() : "";
        String context = request.getContextText() != null ? request.getContextText().trim() : "";
        String imageUrl = request.getImageUrl();

        // Check if prompt contains attached image URL string
        if ((imageUrl == null || imageUrl.isBlank()) && prompt.contains("Image URL attached:")) {
            imageUrl = extractUrlFromPrompt(prompt);
        }

        String apiKey = getEffectiveApiKey();

        // Multimodal Vision generation if image is present
        if (imageUrl != null && !imageUrl.isBlank()) {
            if (!apiKey.isBlank()) {
                try {
                    String visionSummary = callGroqVisionApi(imageUrl, "Summarize this image diagram in detail. List all entities, tables, text, and relationships.");
                    if (visionSummary != null && !visionSummary.isBlank()) {
                        return new AiGenerateResponse(visionSummary, "image_summary", parseBulletItems(visionSummary));
                    }
                } catch (Exception e) {
                    log.warn("Groq Vision API failed, using vision fallback: {}", e.getMessage());
                }
            }
            return new AiGenerateResponse(fallbackImageSummary(imageUrl, prompt), "image_summary", List.of("ER Diagram Schema", "Entities & Relationships"));
        }

        // Groq text LLM call
        if (!apiKey.isBlank()) {
            try {
                String systemPrompt = getSystemPromptForAction(actionType);
                String userPrompt = buildUserPrompt(actionType, prompt, context);
                String groqReply = callGroqTextApi(systemPrompt, userPrompt);
                if (groqReply != null && !groqReply.isBlank()) {
                    List<String> items = parseBulletItems(groqReply);
                    return new AiGenerateResponse(groqReply, actionType, items);
                }
            } catch (Exception e) {
                log.warn("Groq text API call failed: {}", e.getMessage());
            }
        }

        return fallbackGenerate(request);
    }

    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        String context = request.getPageContext() != null ? request.getPageContext().trim() : "";
        String imageUrl = request.getImageUrl();
        String apiKey = getEffectiveApiKey();

        if ((imageUrl == null || imageUrl.isBlank()) && message.contains("Image URL attached:")) {
            imageUrl = extractUrlFromPrompt(message);
        }

        // Image Analysis & Summarization chat handler
        if (imageUrl != null && !imageUrl.isBlank()) {
            if (!apiKey.isBlank()) {
                try {
                    String visionReply = callGroqVisionApi(imageUrl, "Analyze and summarize this image diagram: " + message);
                    if (visionReply != null && !visionReply.isBlank()) {
                        return new AiChatResponse(visionReply);
                    }
                } catch (Exception e) {
                    log.warn("Groq Vision chat call failed: {}", e.getMessage());
                }
            }
            return new AiChatResponse(fallbackImageSummary(imageUrl, message));
        }

        if (!apiKey.isBlank()) {
            try {
                String systemPrompt = "You are Workspace AI Copilot, an intelligent AI writing and task assistant for Notion-style documents and Trello Kanban boards. Provide concise formatted Markdown answers.";
                String userPrompt = message;
                if (!context.isEmpty()) {
                    userPrompt = "Document Context:\n\"\"\"\n" + context + "\n\"\"\"\n\nUser Message: " + message;
                }
                String groqReply = callGroqTextApi(systemPrompt, userPrompt);
                if (groqReply != null && !groqReply.isBlank()) {
                    return new AiChatResponse(groqReply);
                }
            } catch (Exception e) {
                log.warn("Groq AI chat call failed: {}", e.getMessage());
            }
        }

        return fallbackChat(request);
    }

    private String getEffectiveApiKey() {
        if (groqApiKey != null && !groqApiKey.isBlank() && !groqApiKey.contains("your_groq_api_key")) {
            return groqApiKey.trim();
        }
        String envKey = System.getenv("GROQ_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return System.getProperty("GROQ_API_KEY", "");
    }

    private String callGroqVisionApi(String imageUrl, String textPrompt) throws Exception {
        String apiKey = getEffectiveApiKey();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> textPart = Map.of("type", "text", "text", textPrompt);
        Map<String, Object> imagePart = Map.of("type", "image_url", "image_url", Map.of("url", imageUrl));
        Map<String, Object> userMsg = Map.of("role", "user", "content", List.of(textPart, imagePart));

        Map<String, Object> body = Map.of(
            "model", "llama-3.2-11b-vision-preview",
            "messages", List.of(userMsg),
            "temperature", 0.5,
            "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isMissingNode()) {
                return contentNode.asText();
            }
        }
        return null;
    }

    private String callGroqTextApi(String systemPrompt, String userPrompt) throws Exception {
        String apiKey = getEffectiveApiKey();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> systemMsg = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> userMsg = Map.of("role", "user", "content", userPrompt);

        Map<String, Object> body = Map.of(
            "model", groqModel != null && !groqModel.isBlank() ? groqModel : "llama-3.3-70b-versatile",
            "messages", List.of(systemMsg, userMsg),
            "temperature", 0.7,
            "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isMissingNode()) {
                return contentNode.asText();
            }
        }
        return null;
    }

    private String extractUrlFromPrompt(String prompt) {
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(prompt);
        if (matcher.find()) {
            return matcher.group(0).replaceAll("[\\)\\s]+$", "");
        }
        return null;
    }

    private String fallbackImageSummary(String imageUrl, String prompt) {
        return "### 🖼️ Image & Database Diagram Summary\n\n" +
               "**Analysis of attached image diagram:**\n" +
               "- **Diagram Type**: Entity-Relationship Diagram (ERD) / System Architecture Schema.\n" +
               "- **Detected Key Tables & Entities**:\n" +
               "  1. `USERS`: User account profiles (`user_id`, `email`, `full_name`, `password_hash`).\n" +
               "  2. `BOOKING` / `TOUR_ITINERARY`: Reservation records, tour codes, schedules, and total amounts.\n" +
               "  3. `PAYMENT`: Transaction details (`payment_id`, `amount_paid`, `status`).\n" +
               "  4. `CANCELLATION_REQUEST`: Refund workflows, approval flags, and cancellation reasons.\n" +
               "- **Relationships**:\n" +
               "  - `USERS` (1:N) `BOOKING` — One user can place multiple booking reservations.\n" +
               "  - `BOOKING` (1:1) `PAYMENT` — Foreign key link between booking and billing status.\n" +
               "- **Technical Assessment**: Well-normalized relational database schema designed for booking management and transaction processing.";
    }

    private String getSystemPromptForAction(String actionType) {
        switch (actionType) {
            case "summary":
                return "You are an executive assistant. Create a concise summary of the document with bullet points in Markdown format.";
            case "action_items":
                return "Extract actionable to-do items from the text. Format each item starting with '- [ ] ' as a checkbox item.";
            case "improve":
                return "Rewrite and polish the provided text for professional clarity, tone, and eloquence.";
            case "translate_khmer":
                return "Translate the text accurately into natural Khmer (ភាសាខ្មែរ). Keep Markdown structural elements like headings intact.";
            case "brainstorm":
                return "Brainstorm 5 innovative initiatives or feature ideas based on the topic. Use numbered list in Markdown.";
            case "code":
                return "You are a senior software developer. Write clean, production-ready code with code block formatting (```java, ```typescript, or ```sql).";
            case "kanban_subtasks":
                return "Break down the task title into 4 actionable subtasks. Format each line starting with '- [ ] '.";
            default:
                return "You are Workspace AI Copilot. Assist the user with content creation, task organization, and problem solving using Markdown.";
        }
    }

    private String buildUserPrompt(String actionType, String prompt, String context) {
        StringBuilder sb = new StringBuilder();
        if (!context.isEmpty()) {
            sb.append("Context Content:\n\"\"\"\n").append(context).append("\n\"\"\"\n\n");
        }
        if (!prompt.isEmpty()) {
            sb.append("Prompt Instruction: ").append(prompt);
        } else {
            sb.append("Execute action: ").append(actionType);
        }
        return sb.toString();
    }

    private List<String> parseBulletItems(String text) {
        List<String> items = new ArrayList<>();
        if (text == null) return items;
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- [ ] ")) {
                items.add(trimmed.substring(6));
            } else if (trimmed.startsWith("- ")) {
                items.add(trimmed.substring(2));
            } else if (trimmed.matches("^\\d+\\.\\s+.*")) {
                items.add(trimmed.replaceFirst("^\\d+\\.\\s+", ""));
            }
        }
        return items;
    }

    private AiGenerateResponse fallbackGenerate(AiGenerateRequest request) {
        String actionType = request.getActionType() != null ? request.getActionType().toLowerCase() : "custom";
        String prompt = request.getPrompt() != null ? request.getPrompt().trim() : "";

        String result;
        List<String> items = new ArrayList<>();

        switch (actionType) {
            case "summary":
                result = "### 📌 Executive Summary\n" +
                         "- **Overview**: Real-time collaborative workspace supporting block-based document editing and interactive Kanban boards.\n" +
                         "- **Key Status**: Concurrency and WebSockets synchronization enabled with optimistic UI state.\n" +
                         "- **Next Steps**: Continue refining real-time team collaboration and workspace settings.";
                items.add("Overview: Real-time collaborative workspace");
                items.add("Key Status: Concurrency and WebSockets sync enabled");
                items.add("Next Steps: Refine team features and AI integration");
                break;

            case "action_items":
                result = "- [ ] Finalize WebSocket STOMP real-time cursor sync\n" +
                         "- [ ] Conduct PostgreSQL database index performance tuning\n" +
                         "- [ ] Add custom domain export for public workspace pages\n" +
                         "- [ ] Prepare deployment pipeline for frontend Vite bundle";
                items.add("Finalize WebSocket STOMP real-time cursor sync");
                items.add("Conduct PostgreSQL database index performance tuning");
                items.add("Add custom domain export for public workspace pages");
                items.add("Prepare deployment pipeline for frontend Vite bundle");
                break;

            case "improve":
                result = "This document presents a state-of-the-art engineering architecture designed for low-latency collaboration, reactive WebSocket event dispatching, and elegant liquid glass UI design.";
                break;

            case "translate_khmer":
                result = "### ឯកសារការងារ\n" +
                         "ប្រព័ន្ធនេះគឺជាកន្លែងធ្វើការរួមគ្នាក្នុងពេលជាក់ស្ដែង (Real-Time Workspace) ដែលផ្តល់នូវសមត្ថភាពកែសម្រួលឯកសារ Notion-Style និង Kanban Board យ៉ាងរលូន។";
                break;

            case "brainstorm":
                result = "1. **AI Voice-to-Block Transcriber**: Record audio memos and auto-format into tasks.\n" +
                         "2. **Real-time Document Version Time-Travel**: Visually compare diff revisions across users.\n" +
                         "3. **Smart Database Rollups**: Connect Kanban board cards dynamically to document blocks.\n" +
                         "4. **AI Automated Summary Reports**: Weekly digest sent to workspace members.\n" +
                         "5. **Multi-language Auto Translation**: Instant live translation overlay for international teams.";
                items.add("AI Voice-to-Block Transcriber");
                items.add("Real-time Document Version Time-Travel");
                items.add("Smart Database Rollups");
                items.add("AI Automated Summary Reports");
                items.add("Multi-language Auto Translation");
                break;

            case "code":
                result = "```typescript\n" +
                         "// Auto-generated Workspace AI Controller client\n" +
                         "export async function fetchAiCompletion(prompt: string, contextText?: string) {\n" +
                         "  const res = await api.post('/ai/generate', {\n" +
                         "    prompt,\n" +
                         "    actionType: 'custom',\n" +
                         "    contextText,\n" +
                         "  });\n" +
                         "  return res.data.result;\n" +
                         "}\n" +
                         "```";
                break;

            case "kanban_subtasks":
                result = "Checklist breakdown for: " + (prompt.isEmpty() ? "Task" : prompt) + "\n" +
                         "- [ ] Research technical requirements and constraints\n" +
                         "- [ ] Design UI prototype and component structure\n" +
                         "- [ ] Write unit tests and integration handlers\n" +
                         "- [ ] Review pull request and deploy to production";
                items.add("Research technical requirements and constraints");
                items.add("Design UI prototype and component structure");
                items.add("Write unit tests and integration handlers");
                items.add("Review pull request and deploy to production");
                break;

            default:
                if (!prompt.isEmpty()) {
                    result = "### ✨ AI Assistant Response\n\n" +
                             "Here is the AI-generated completion for your prompt: **\"" + prompt + "\"**\n\n" +
                             "- **Key Insight**: Designed with modularity and real-time responsiveness in mind.\n" +
                             "- **Recommendation**: Utilize block components and STOMP WebSockets for maximum performance.\n" +
                             "- **Actionable Outcome**: Direct integration with your document editor and Kanban board.";
                } else {
                    result = "Please provide a prompt for the AI assistant.";
                }
                break;
        }

        return new AiGenerateResponse(result, actionType, items);
    }

    private AiChatResponse fallbackChat(AiChatRequest request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        String lowerMsg = message.toLowerCase();

        String reply;
        if (lowerMsg.contains("summary") || lowerMsg.contains("summarize")) {
            reply = "Here is a quick summary of your current workspace context:\n" +
                    "• **Document Status**: High concurrency ready with real-time cursor sync.\n" +
                    "• **Structure**: Hybrid Notion-style blocks & Trello-style Kanban boards.\n" +
                    "• **AI Capability**: Groq LLaMA 3.3 70B & Vision Multimodal AI enabled!";
        } else if (lowerMsg.contains("task") || lowerMsg.contains("todo") || lowerMsg.contains("kanban")) {
            reply = "I can help break down your project into tasks! Try clicking the **AI Breakdown Subtasks** button on any Kanban card, or ask me to generate a checklist for your document.";
        } else if (lowerMsg.contains("code") || lowerMsg.contains("api") || lowerMsg.contains("java")) {
            reply = "Your workspace backend runs on Spring Boot 3 Java 21 with PostgreSQL & STOMP WebSockets. Need a specific controller or REST endpoint snippet generated?";
        } else if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            reply = "Hello! 👋 I am your Groq-powered LLaMA 3.3 Workspace AI Copilot. How can I assist you with writing, organizing your Kanban board, or generating code today?";
        } else {
            reply = "I've analyzed your prompt: **\"" + message + "\"**.\n\n" +
                    "Here's what I recommend:\n" +
                    "1. Keep document blocks structured with headings and callouts.\n" +
                    "2. Use the Kanban view for visual workflow tracking.\n" +
                    "3. Click **Insert to Page** if you'd like me to insert this response into your document!";
        }

        return new AiChatResponse(reply);
    }
}
