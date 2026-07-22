package com.adithyak.serverlessapis.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " with id '" + id + "' not found");
    }
}
