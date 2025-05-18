package com.linkauto.restapi.dto;

import java.util.List;

/**
 * DTO for returning Event data to clients
 */
public class EventReturnerDTO {
    private Long id;
    private String username;
    private String title;
    private String description;
    private String location;
    private long date;
    private List<String> participants;
    private String image;

    public EventReturnerDTO() {
    }

    public EventReturnerDTO(Long id, String username, String title, String description, String location,
                         long date, List<String> participants,
                         String image) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.participants = participants;
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getOrganizer() {
        return username;
    }

    public void setOrganizer(String username) {
        this.username = username;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "EventReturnerDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", date=" + date +
                ", organizer='" + username + '\'' +
                ", participants=" + participants +
                ", image='" + image + '\'' +
                '}';
    }
}