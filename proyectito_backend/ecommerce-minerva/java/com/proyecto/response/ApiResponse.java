package com.proyecto.response;

public class ApiResponse<T> {
    private String message;
    private boolean result;
    private T data;

    // Constructor vacío
    public ApiResponse() {}

    // Constructor con parámetros
    public ApiResponse(String message, boolean result, T data) {
        this.message = message;
        this.result = result;
        this.data = data;
    }

    // Constructor sin datos
    public ApiResponse(String message, boolean result) {
        this.message = message;
        this.result = result;
    }

    // Getters y setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
