package com.proyecto.request;

public class UpdateOrderRequestStatusDto {
    private String status; // "APROBADA", "RECHAZADA"

    public UpdateOrderRequestStatusDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
