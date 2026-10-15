package com.telegram.recruitment.client;

import com.telegram.recruitment.config.BotConfig;
import com.telegram.recruitment.util.JsonUtils;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramApiClient {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final BotConfig config;
    private final HttpClient httpClient;

    public TelegramApiClient(BotConfig config) {
        this.config = config;
        System.setProperty("java.net.preferIPv4Stack", "true");

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .proxy(HttpClient.Builder.NO_PROXY)
                .build();
    }

    public boolean testConnection() {
        try {
            String url = config.getApiBaseUrl() + "/bot" + config.getBotToken() + "/getMe";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("\"ok\":true")) {
                String botName = JsonUtils.extractJsonValue(response.body(), "username");
                System.out.println("Connected to Telegram API: " + config.getApiBaseUrl());
                System.out.println("Bot Verified: @" + (botName != null ? botName : config.getBotUsername()));
                return true;
            } else {
                System.err.println("Telegram API HTTP " + response.statusCode() + ": " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public String sendPost(String method, String jsonBody) throws Exception {
        String url = config.getApiBaseUrl() + "/bot" + config.getBotToken() + "/" + method;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public void sendTextMessage(long chatId, String text, String parseMode) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"chat_id\": ").append(chatId).append(",\n");
            json.append("  \"text\": ").append(JsonUtils.quoteJson(text));
            if (parseMode != null) {
                json.append(",\n  \"parse_mode\": \"").append(parseMode).append("\"");
            }
            json.append("\n}");

            String resp = sendPost("sendMessage", json.toString());
            if (resp != null && resp.contains("\"ok\":true")) {
            } else {
                System.err.println("Telegram sendMessage error: " + resp);
            }
        } catch (Exception e) {
            System.err.println("Error sending message to " + chatId + ": " + e.getMessage());
        }
    }

    public boolean sendDocument(long chatId, String fileId, String caption, String parseMode) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"chat_id\": ").append(chatId).append(",\n");
            json.append("  \"document\": ").append(JsonUtils.quoteJson(fileId));
            if (caption != null && !caption.isBlank()) {
                json.append(",\n  \"caption\": ").append(JsonUtils.quoteJson(caption));
            }
            if (parseMode != null) {
                json.append(",\n  \"parse_mode\": \"").append(parseMode).append("\"");
            }
            json.append("\n}");

            String resp = sendPost("sendDocument", json.toString());
            if (resp != null && resp.contains("\"ok\":true")) {
                return true;
            } else {
                System.err.println("Telegram sendDocument error: " + resp);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error sending document to " + chatId + ": " + e.getMessage());
            return false;
        }
    }

    public String getUpdates(long offset, int timeoutSeconds) throws Exception {
        String url = config.getApiBaseUrl() + "/bot" + config.getBotToken() + "/getUpdates?offset=" + offset + "&timeout=" + timeoutSeconds;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(timeoutSeconds + 15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getFilePath(String fileId) {
        try {
            String url = config.getApiBaseUrl() + "/bot" + config.getBotToken() + "/getFile?file_id=" + fileId;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return JsonUtils.extractJsonValue(resp.body(), "file_path");
        } catch (Exception e) {
            System.err.println("Failed to get file path: " + e.getMessage());
            return null;
        }
    }

    public InputStream downloadFileStream(String filePath) throws Exception {
        String downloadUrl = config.getApiBaseUrl() + "/file/bot" + config.getBotToken() + "/" + filePath;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<InputStream> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() == 200) {
            return resp.body();
        } else {
            throw new RuntimeException("Download failed with status " + resp.statusCode());
        }
    }
}
