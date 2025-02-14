package com.gistpetition.api.comment.dto;


import javax.validation.constraints.NotBlank;

public class CommentRequest {
    @NotBlank
    private String content;

    protected CommentRequest() {
    }

    public String getContent() {
        return content;
    }

    public CommentRequest(String content) {
        this.content = content;
    }

}
