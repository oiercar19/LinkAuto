package com.linkauto.restapi.dto;

public class CommentDTO {
    private String text;
    private Long creationDate;

    public CommentDTO() {
    }

    public CommentDTO(String text, Long creationDate) {
        this.text = text;
        this.creationDate = creationDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "CommentDTO{" +
                "text='" + text + '\'' +
                ", fecha=" + creationDate +
                '}';
    }

}
