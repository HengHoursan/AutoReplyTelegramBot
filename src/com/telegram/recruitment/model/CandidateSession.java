package com.telegram.recruitment.model;

import java.time.LocalDateTime;

/**
 * Tracks the state of a candidate applying for a job.
 */
public class CandidateSession {

    private final String jobId;
    private final String candidateName;
    private String status; // "WAITING_FOR_CV", "COMPLETED"
    private final LocalDateTime startedAt;

    public CandidateSession(String jobId, String candidateName, String status) {
        this.jobId = jobId;
        this.candidateName = candidateName;
        this.status = status;
        this.startedAt = LocalDateTime.now();
    }

    public String getJobId() {
        return jobId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}
