package com.telegram.recruitment.util;

import com.telegram.recruitment.model.ParsedUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON & string utilities for Telegram API payloads.
 * No external JSON library required.
 */
public class JsonUtils {

    public static String quoteJson(String text) {
        if (text == null) return "\"\"";
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    public static String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }

    public static String extractJsonValue(String json, String key) {
        if (json == null) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
        }
        return null;
    }

    public static String extractJsonRegex(String text, String regex) {
        if (text == null) return null;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static List<ParsedUpdate> parseUpdates(String json) {
        List<ParsedUpdate> list = new ArrayList<>();
        if (json == null || !json.contains("\"result\"")) return list;

        Pattern updatePattern = Pattern.compile("\\{\"update_id\":(\\d+),.*?(?=\\{\"update_id\"|\\Z)", Pattern.DOTALL);
        Matcher matcher = updatePattern.matcher(json);

        while (matcher.find()) {
            String chunk = matcher.group(0);
            ParsedUpdate u = new ParsedUpdate();
            u.setUpdateId(Long.parseLong(matcher.group(1)));

            // Extract chat id
            Pattern chatIdPat = Pattern.compile("\"chat\":\\{[^}]*?\"id\":(-?\\d+)");
            Matcher chatMatcher = chatIdPat.matcher(chunk);
            if (chatMatcher.find()) {
                u.setChatId(Long.parseLong(chatMatcher.group(1)));
            }

            // Extract sender name and username
            u.setSenderName(extractJsonValue(chunk, "first_name"));
            u.setUsername(extractJsonValue(chunk, "username"));

            // Extract text
            u.setText(extractJsonValue(chunk, "text"));

            // Extract document
            int docIndex = chunk.indexOf("\"document\":");
            if (docIndex != -1) {
                String docChunk = chunk.substring(docIndex);
                // Strip nested thumbnail objects so we extract the real document file_id, not thumbnail
                String cleanDoc = docChunk.replaceAll("\"thumb(nail)?\":\\s*\\{[^}]*\\}", "");

                u.setDocumentFileId(extractJsonValue(cleanDoc, "file_id"));
                String fileName = extractJsonValue(cleanDoc, "file_name");
                if (fileName != null && !fileName.isBlank()) {
                    u.setDocumentFileName(fileName);
                }
                String sizeStr = extractJsonRegex(cleanDoc, "\"file_size\":(\\d+)");
                if (sizeStr != null) {
                    try {
                        u.setDocumentFileSize(Long.parseLong(sizeStr));
                    } catch (Exception ignored) {}
                }
            }

            // Check if photo was sent
            if (chunk.contains("\"photo\":")) {
                u.setHasPhoto(true);
            }

            list.add(u);
        }
        return list;
    }
}
