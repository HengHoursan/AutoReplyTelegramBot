package com.telegram.recruitment.service;

import com.telegram.recruitment.client.TelegramApiClient;
import com.telegram.recruitment.config.BotConfig;
import com.telegram.recruitment.model.CandidateSession;
import com.telegram.recruitment.model.ParsedUpdate;
import com.telegram.recruitment.template.MessageTemplate;
import com.telegram.recruitment.util.JsonUtils;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BotListenerService {

    private final TelegramApiClient client;
    private final CvStorageService storageService;
    private final BotConfig config;
    private final Map<Long, CandidateSession> activeSessions = new ConcurrentHashMap<>();

    public BotListenerService(TelegramApiClient client, CvStorageService storageService, BotConfig config) {
        this.client = client;
        this.storageService = storageService;
        this.config = config;
    }

    public void startListening() {
        System.out.println("Bot listening for incoming messages...\n");
        long lastUpdateId = 0;

        while (true) {
            try {
                String responseBody = client.getUpdates(lastUpdateId + 1, 10);
                List<ParsedUpdate> updates = JsonUtils.parseUpdates(responseBody);

                for (ParsedUpdate update : updates) {
                    lastUpdateId = Math.max(lastUpdateId, update.getUpdateId());
                    handleUpdate(update);
                }
            } catch (Exception e) {
                String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
                System.err.println("Polling: " + msg + ". Retrying in 3s...");
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void handleUpdate(ParsedUpdate update) {
        long chatId = update.getChatId();
        String name = (update.getSenderName() != null && !update.getSenderName().isBlank())
                ? update.getSenderName() : "Candidate";

        if (update.getText() != null && update.getText().startsWith("/start")) {
            handleStart(chatId, name, update.getText());
            return;
        }

        if (update.hasDocument()) {
            handleDocument(chatId, name, update);
            return;
        }

        if (update.isHasPhoto()) {
            client.sendTextMessage(chatId, MessageTemplate.sentPhotoInstead(), "HTML");
            return;
        }

        if (update.getText() != null && !update.getText().startsWith("/")) {
            handleText(chatId, name);
        }
    }

    private void handleStart(long chatId, String name, String text) {
        String[] parts = text.split("\\s+", 2);
        boolean hasSpecificJob = parts.length > 1 && !parts[1].trim().isBlank();
        String jobId = hasSpecificJob ? parts[1].trim() : "GENERAL-APPLICATION";

        activeSessions.put(chatId, new CandidateSession(jobId, name, "WAITING_FOR_CV"));
        System.out.println("Candidate [" + name + "] started session" + (hasSpecificJob ? " for: " + jobId : ""));

        String welcomeMsg = hasSpecificJob
                ? MessageTemplate.welcomeJob(name, jobId.replace("_", " "))
                : MessageTemplate.welcome(name);

        client.sendTextMessage(chatId, welcomeMsg, "HTML");
    }

    private void handleDocument(long chatId, String name, ParsedUpdate update) {
        CandidateSession session = activeSessions.get(chatId);
        String jobId = (session != null) ? session.getJobId() : "DIRECT-SUBMISSION";

        System.out.println("CV received from [" + name + "]: " + update.getDocumentFileName());

        Path savedFile = null;
        try {
            String filePath = client.getFilePath(update.getDocumentFileId());
            if (filePath != null) {
                try (InputStream in = client.downloadFileStream(filePath)) {
                    savedFile = storageService.saveCv(in, update.getDocumentFileName(), name, jobId);
                }
            }
        } catch (Exception e) {
            System.err.println("Download error: " + e.getMessage());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String displayJob = (jobId == null || jobId.equals("GENERAL-APPLICATION") || jobId.equals("DIRECT-SUBMISSION"))
                ? "General Application"
                : jobId.replace("_", " ");

        String reply;
        if (savedFile != null) {
            reply = MessageTemplate.cvReceived(name, savedFile.getFileName().toString(), displayJob, timestamp);
        } else {
            reply = MessageTemplate.cvReceivedFallback(name, update.getDocumentFileName());
        }

        client.sendTextMessage(chatId, reply, "HTML");

        // Forward CV file & notification to Recruiter's personal Telegram
        if (config.hasRecruiterChatId()) {
            try {
                long recruiterId = Long.parseLong(config.getRecruiterChatId());
                String recruiterCaption = MessageTemplate.recruiterNotification(
                        name,
                        update.getUsername(),
                        displayJob,
                        (savedFile != null ? savedFile.getFileName().toString() : update.getDocumentFileName()),
                        timestamp
                );
                boolean sent = client.sendDocument(recruiterId, update.getDocumentFileId(), recruiterCaption, "HTML");
                if (sent) {
                    System.out.println("Alert + CV file forwarded to Recruiter (chatId=" + recruiterId + ")");
                } else {
                    System.out.println("Document forward failed, sending text alert fallback to Recruiter...");
                    client.sendTextMessage(recruiterId, recruiterCaption, "HTML");
                }
            } catch (Exception e) {
                System.err.println("Could not forward CV to recruiter: " + e.getMessage());
            }
        }

        if (session != null) {
            session.setStatus("COMPLETED");
        }
    }

    private void handleText(long chatId, String name) {
        CandidateSession session = activeSessions.get(chatId);
        if (session != null && "WAITING_FOR_CV".equals(session.getStatus())) {
            String job = session.getJobId();
            if (job != null && !job.equals("GENERAL-APPLICATION") && !job.equals("DIRECT-SUBMISSION")) {
                client.sendTextMessage(chatId, MessageTemplate.pleaseAttachFile(job.replace("_", " ")), "HTML");
            } else {
                client.sendTextMessage(chatId, MessageTemplate.pleaseAttachFileGeneral(name), "HTML");
            }
        } else {
            activeSessions.put(chatId, new CandidateSession("GENERAL-APPLICATION", name, "WAITING_FOR_CV"));
            client.sendTextMessage(chatId, MessageTemplate.pleaseAttachFileGeneral(name), "HTML");
        }
    }
}
