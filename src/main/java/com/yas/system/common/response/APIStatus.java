package com.yas.system.common.response;

public enum APIStatus {


    OK(200, "OK"),
    CREATED(201, "No more result"),
    ERR_BAD_REQUEST(400, "Bad Request"),
    ERR_UNAUTHORIZED(401, "Unauthorized or Access Token is expired"),
    ERR_FORBIDDEN(403, "Forbidden! Access denied"),
    ERR_NOT_FOUND(404, "Not Found"),
    ERR_INTERNAL_SERVER(500, "Internal Server Error"),
    ;
    private final int code;
    private final String description;


    private APIStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
