package com.vermenia.nailong.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * 调用外部API进行对话（例如OpenAI、通义千问等兼容OpenAI格式的API）
 */
public final class ApiChatClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public String chat(String apiEndpoint, String apiKey, String model, List<Message> messages)
            throws IOException, InterruptedException {
        JsonArray messagesArray = new JsonArray();
        for (Message msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("model", model);
        requestBody.addProperty("temperature", 0.9);
        requestBody.addProperty("max_tokens", 512);
        requestBody.addProperty("stream", false);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(apiEndpoint))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8));

        if (apiKey != null && !apiKey.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IOException("API返回错误 HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!root.has("choices") || root.getAsJsonArray("choices").isEmpty()) {
            throw new IOException("API没有返回有效的对话内容。");
        }

        String content = root.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString().trim();

        if (content.isEmpty()) {
            throw new IOException("奶龙没有说话，请再试一次。");
        }

        return content;
    }
}
