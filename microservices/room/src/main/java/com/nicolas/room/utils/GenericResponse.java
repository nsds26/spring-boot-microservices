package com.nicolas.room.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenericResponse<T> {
    private boolean success;
    private int statusCode;
    private String errorMessage;
    private T data;

    public GenericResponse(boolean success, int statusCode, String errorMessage, T data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.data = data;
    }

    public GenericResponse(boolean success, int statusCode, T data) {
        this.success = success;
        this.statusCode = statusCode;
        this.data = data;
    }

    public GenericResponse(int statusCode, boolean success) {
        this.statusCode = statusCode;
        this.success = success;
    }

    public GenericResponse(int statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.success = false;
        this.errorMessage = errorMessage;
    }
}
