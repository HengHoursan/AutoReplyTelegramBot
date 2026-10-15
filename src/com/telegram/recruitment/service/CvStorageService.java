package com.telegram.recruitment.service;

import com.telegram.recruitment.config.BotConfig;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CvStorageService {

    private final Path storageDirectory;

    public CvStorageService(BotConfig config) {
        Path base = Paths.get("").toAbsolutePath();

        if (Files.exists(base.resolve("telegramAutoReply"))) {
            this.storageDirectory = base.resolve("telegramAutoReply").resolve(config.getSaveDirectory());
        } else {
            this.storageDirectory = base.resolve(config.getSaveDirectory());
        }

        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (Exception e) {
            System.err.println("Could not create storage directory: " + e.getMessage());
        }
    }

    /**
     * Saves the incoming CV keeping the candidate's exact original file name.
     * If a file with the same name already exists, appends (1), (2), etc.
     */
    public Path saveCv(InputStream inputStream, String originalFileName, String candidateName, String jobId) {
        try {
            String fileName = (originalFileName != null && !originalFileName.isBlank())
                    ? originalFileName.trim()
                    : "resume.pdf";

            // Sanitize invalid Windows filename characters: \ / : * ? " < > |
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

            Path targetPath = storageDirectory.resolve(fileName);

            // Handle filename collisions without overwriting existing candidate files
            if (Files.exists(targetPath)) {
                String namePart = fileName;
                String extPart = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    namePart = fileName.substring(0, dotIndex);
                    extPart = fileName.substring(dotIndex);
                }

                int counter = 1;
                while (Files.exists(targetPath)) {
                    targetPath = storageDirectory.resolve(namePart + " (" + counter + ")" + extPart);
                    counter++;
                }
            }

            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved CV as original name: " + targetPath.getFileName());
            return targetPath;
        } catch (Exception e) {
            System.err.println("Failed to save CV: " + e.getMessage());
            return null;
        }
    }

    public Path getStorageDirectory() {
        return storageDirectory;
    }
}
