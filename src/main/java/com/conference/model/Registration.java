package com.conference.model;

import java.time.LocalDateTime;

/** Links a participant to a conference, with a status and waiting-list position. */
public class Registration {

    private int id;
    private int userId;
    private int conferenceId;
    private RegistrationStatus status;
    private Integer waitlistPosition;
    private LocalDateTime registeredAt;

    /**
     * Creates a registration with all fields.
     *
     * @param id               database id (0 if not saved yet)
     * @param userId           id of the participant
     * @param conferenceId     id of the conference
     * @param status           current status
     * @param waitlistPosition position in the waiting list, or null if not waitlisted
     * @param registeredAt     when the registration was made
     */
    public Registration(int id, int userId, int conferenceId, RegistrationStatus status,
                        Integer waitlistPosition, LocalDateTime registeredAt) {
        this.id = id;
        this.userId = userId;
        this.conferenceId = conferenceId;
        this.status = status;
        this.waitlistPosition = waitlistPosition;
        this.registeredAt = registeredAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    /** @return waiting-list position, or null if not waitlisted */
    public Integer getWaitlistPosition() {
        return waitlistPosition;
    }

    /** @param waitlistPosition waiting-list position, or null */
    public void setWaitlistPosition(Integer waitlistPosition) {
        this.waitlistPosition = waitlistPosition;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", user=" + userId + ", conference=" + conferenceId
                + ", status=" + status + ", position=" + waitlistPosition + "}";
    }
}
