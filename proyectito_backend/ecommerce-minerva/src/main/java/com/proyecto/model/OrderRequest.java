package com.proyecto.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Usuario user;

    private String type; // "DEVOLUCION" o "CANCELACION"
    
    private String status; // "PENDIENTE", "APROBADA", "RECHAZADA"

    @Column(length = 1000)
    private String reason;

    private LocalDateTime requestDate;

    private LocalDateTime resolutionDate;

    public OrderRequest() {
    }

    public OrderRequest(Order order, Usuario user, String type, String status, String reason, LocalDateTime requestDate) {
        this.order = order;
        this.user = user;
        this.type = type;
        this.status = status;
        this.reason = reason;
        this.requestDate = requestDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
}
