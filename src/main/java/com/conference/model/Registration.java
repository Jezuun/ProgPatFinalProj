package com.conference.model;

public class Registration {
    private int registrationId;
    private int userId;
    private int conferenceId;
    private RegistrationStatus status;
    private Integer waitlistPosition;
    private String registrationDate;

    public Registration(int registrationId, int userId, int conferenceId, RegistrationStatus status, Integer waitlistPosition, String registrationDate) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.conferenceId = conferenceId;
        this.status = status;
        this.waitlistPosition = waitlistPosition;
        this.registrationDate = registrationDate;
    }

    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getConferenceId() { return conferenceId; }
    public void setConferenceId(int conferenceId) { this.conferenceId = conferenceId; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public Integer getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(Integer waitlistPosition) { this.waitlistPosition = waitlistPosition; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}