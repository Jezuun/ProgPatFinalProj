package com.conference.model;

public class Conference {
    private int conferenceId;
    private String title;
    private int capacity;
    private String startDateTime;
    private String endDateTime;
    private String location;

    public Conference(int conferenceId, String title, int capacity, String startDateTime, String endDateTime, String location) {
        this.conferenceId = conferenceId;
        this.title = title;
        this.capacity = capacity;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
    }

    public int getConferenceId() { return conferenceId; }
    public void setConferenceId(int conferenceId) { this.conferenceId = conferenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }

    public String getEndDateTime() { return endDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}