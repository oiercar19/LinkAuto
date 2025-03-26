/**
 *
 * @author AdriianFdz
 */

package com.linkauto.restapi.dto;

import java.util.List;

public class PostDTO {
    private String message;
    private List<String> images;

    public PostDTO() {
    }

    public PostDTO(String message, List<String> images) {
        this.message = message;
        this.images = images;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getImages() {
        return images;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
