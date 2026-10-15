package com.telegram.recruitment.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class BotConfig {

    private String botToken = "";
    private String botUsername = "";
    private String channelId = "";
    private String saveDirectory = "received_cvs";
    private String apiBaseUrl = "https://api.telegram.org";
    private String recruiterChatId = "";

    public static BotConfig load() {
        BotConfig config = new BotConfig();

        Path base = Paths.get("").toAbsolutePath();
        File configFile = Files.exists(base.resolve("telegramAutoReply").resolve("config.properties"))
                ? base.resolve("telegramAutoReply").resolve("config.properties").toFile()
                : base.resolve("config.properties").toFile();

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(in);

                config.botToken = props.getProperty("bot.token", "").trim();
                config.botUsername = props.getProperty("bot.username", "").replace("@", "").trim();
                config.channelId = props.getProperty("channel.id", "").trim();
                config.saveDirectory = props.getProperty("save.directory", "received_cvs").trim();
                config.apiBaseUrl = props.getProperty("api.base.url", "https://api.telegram.org").trim();
                config.recruiterChatId = props.getProperty("recruiter.chat.id", "").trim();
            } catch (Exception e) {
                System.err.println("Error reading config.properties: " + e.getMessage());
            }
        } else {
            System.err.println("Warning: config.properties file not found at " + configFile.getAbsolutePath());
        }

        return config;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getRecruiterChatId() {
        return recruiterChatId;
    }

    public boolean hasRecruiterChatId() {
        return recruiterChatId != null && !recruiterChatId.isBlank();
    }

    public boolean isConfigured() {
        return botToken != null && !botToken.isBlank();
    }
}
