package com.workspace.dto;

import java.util.List;

public class AiDTOs {

    public static class AiGenerateRequest {
        private String prompt;
        private String actionType; // e.g. "summary", "action_items", "improve", "translate_khmer", "brainstorm", "code", "kanban_subtasks"
        private String contextText;
        private String imageUrl;

        public AiGenerateRequest() {}

        public AiGenerateRequest(String prompt, String actionType, String contextText) {
            this.prompt = prompt;
            this.actionType = actionType;
            this.contextText = contextText;
        }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getContextText() { return contextText; }
        public void setContextText(String contextText) { this.contextText = contextText; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class AiGenerateResponse {
        private String result;
        private String actionType;
        private List<String> items;

        public AiGenerateResponse() {}

        public AiGenerateResponse(String result, String actionType, List<String> items) {
            this.result = result;
            this.actionType = actionType;
            this.items = items;
        }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }

    public static class AiChatMessage {
        private String role; // "user" or "assistant"
        private String content;
        private String imageUrl;

        public AiChatMessage() {}

        public AiChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class AiChatRequest {
        private String message;
        private String pageContext;
        private String imageUrl;
        private List<AiChatMessage> history;

        public AiChatRequest() {}

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getPageContext() { return pageContext; }
        public void setPageContext(String pageContext) { this.pageContext = pageContext; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public List<AiChatMessage> getHistory() { return history; }
        public void setHistory(List<AiChatMessage> history) { this.history = history; }
    }

    public static class AiChatResponse {
        private String reply;

        public AiChatResponse() {}

        public AiChatResponse(String reply) {
            this.reply = reply;
        }

        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
    }
}
