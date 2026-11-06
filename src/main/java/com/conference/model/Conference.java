package com.conference.model;

import java.time.LocalDateTime;

/** A conference that participants can register for. */
public class Conference {

    private int id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int capacity;

    public Conference(String title, String description, String location,
                      LocalDateTime startTime, LocalDateTime endTime, int capacity) {
        this(0, title, description, location, startTime, endTime, capacity);
    }

    public Conference(int id, String title, String description, String location,
                      LocalDateTime startTime, LocalDateTime endTime, int capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Conference{id=" + id + ", title=" + title + ", start=" + startTime
                + ", capacity=" + capacity + "}";
    }
}
