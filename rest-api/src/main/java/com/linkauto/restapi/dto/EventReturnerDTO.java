package com.linkauto.restapi.dto;

import java.util.List;

/**
 * DTO for returning Event data to clients
 */
public class EventReturnerDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private long date;
    private String organizer;
    private List<String> participants;
    private String image;

    public EventReturnerDTO() {
    }

    public EventReturnerDTO(Long id, String title, String description, String location,
                         long date, String organizer, List<String> participants,
                         String image) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.organizer = organizer;
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
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
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
                ", organizer='" + organizer + '\'' +
                ", participants=" + participants +
                ", image='" + image + '\'' +
                '}';
    }
}