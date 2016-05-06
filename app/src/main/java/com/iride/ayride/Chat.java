package com.iride.ayride;

/**
 * Created by user on 6.05.2016.
 */
public class Chat {
    private String message;
    private String author;

    @SuppressWarnings("unused")
    private Chat() {

    }

    Chat(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }
}

