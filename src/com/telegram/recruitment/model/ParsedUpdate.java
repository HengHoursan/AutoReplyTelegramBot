package com.telegram.recruitment.model;

public class ParsedUpdate {

    private long updateId;
    private long chatId;
    private String senderName;
    private String username;
    private String text;
    private String documentFileId;
    private String documentFileName = "document.pdf";
    private long documentFileSize;
    private boolean hasPhoto;

    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDocumentFileId() {
        return documentFileId;
    }

    public void setDocumentFileId(String documentFileId) {
        this.documentFileId = documentFileId;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }

    public void setDocumentFileName(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public long getDocumentFileSize() {
        return documentFileSize;
    }

    public void setDocumentFileSize(long documentFileSize) {
        this.documentFileSize = documentFileSize;
    }

    public boolean isHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public boolean hasDocument() {
        return documentFileId != null && !documentFileId.isBlank();
    }
}
