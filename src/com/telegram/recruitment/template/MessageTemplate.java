package com.telegram.recruitment.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageTemplate {

    private static final Path TEMPLATE_DIR = resolveTemplateDir();
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    private static Path resolveTemplateDir() {
        Path base = Paths.get("").toAbsolutePath();
        if (Files.exists(base.resolve("telegramAutoReply").resolve("templates"))) {
            return base.resolve("telegramAutoReply").resolve("templates");
        }
        return base.resolve("templates");
    }

    private static String load(String fileName, Map<String, String> variables) {
        String template = cache.computeIfAbsent(fileName, f -> {
            try {
                return Files.readString(TEMPLATE_DIR.resolve(f));
            } catch (IOException e) {
                System.err.println("Template not found: " + f);
                return "";
            }
        });

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", escapeHtml(entry.getValue()));
        }
        return result.trim();
    }

    public static String welcome(String name) {
        return load("welcome.html", Map.of("name", name));
    }

    public static String welcomeJob(String name, String jobId) {
        return load("welcome_job.html", Map.of("name", name, "jobId", jobId));
    }

    public static String cvReceived(String name, String fileName, String jobId, String timestamp) {
        return load("cv_received.html", Map.of("name", name, "fileName", fileName, "jobId", jobId, "timestamp", timestamp));
    }

    public static String cvReceivedFallback(String name, String fileName) {
        return load("cv_received_fallback.html", Map.of("name", name, "fileName", fileName));
    }

    public static String pleaseAttachFile(String jobId) {
        return load("please_attach.html", Map.of("jobId", jobId));
    }

    public static String pleaseAttachFileGeneral(String name) {
        return load("please_attach_general.html", Map.of("name", name));
    }

    public static String sentPhotoInstead() {
        return load("sent_photo.html", Map.of());
    }

    public static String recruiterNotification(String name, String username, String jobId, String fileName, String timestamp) {
        String usernameStr = (username != null && !username.isBlank()) ? "(@" + username + ")" : "";
        return load("recruiter_notification.html", Map.of(
                "name", name,
                "username", usernameStr,
                "jobId", jobId,
                "fileName", fileName,
                "timestamp", timestamp
        ));
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
