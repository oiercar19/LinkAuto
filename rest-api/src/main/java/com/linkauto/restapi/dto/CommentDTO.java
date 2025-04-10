package com.linkauto.restapi.dto;

public class CommentDTO {
    private String text;

    public CommentDTO() {
    }

    public CommentDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "CommentDTO{" +
                "text='" + text + '\'' +
                '}';
    }

}
