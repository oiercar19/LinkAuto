package com.linkauto.restapi.dto;

import java.util.List;
import java.util.Set;

/**
 * DTO for returning Event data to clients
 */
public class EventReturnerDTO {
    private Long id;
    private String username;
    private String title;
    private String description;
    private String location;
    private long startDate;
    private long endDate;
    private List<String> images;
    private Set<String> participants;
    private List<Long> comment_ids;

    public EventReturnerDTO() {
    }

    public EventReturnerDTO(Long id, String username, String title, String description, String location, long startDate, long endDate, List<String> images, Set<String> participants, List<Long> comment_ids) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.images = images;
        this.participants = participants;
        this.comment_ids = comment_ids;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
    public long getStartDate() {
        return startDate;
    }
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
    public long getEndDate() {
        return endDate;
    }
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }
    public Set<String> getParticipants() {
        return participants;
    }
    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
    public List<Long> getComment_ids() {
        return comment_ids;
    }
    public void setComment_ids(List<Long> comment_ids) {
        this.comment_ids = comment_ids;
    }
    @Override
    public String toString() {
        return "EventReturnerDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", images=" + images +
                ", participants=" + participants +
                ", comment_ids=" + comment_ids +
                '}';
    }
}