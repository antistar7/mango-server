package com.mango.exception;

public class ContentNotFoundException extends RuntimeException {

    public ContentNotFoundException(Long id) {
        super("Content not found: " + id);
    }
}