package com.proyecto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.proyecto.model.Address;
import com.proyecto.model.Order;
import com.proyecto.model.PaymentDetails;

public class OrderResponse {
    private Long id;
    private String orderId;
    private UsuarioResponse usuario;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private Address shippingAddress;
    private PaymentDetails paymentDetails;
    private double totalPrice;
    private Integer totalDiscountedPrice;
    private Integer discounte;
    private String orderStatus;
    private int totalItem;
    private LocalDateTime createdAt;
    private String trackingNumber;
    private String shippingLabelUrl;
    private String shippingMethod;
    private String storeName;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        if (order == null) return;
        this.id = order.getId();
        this.orderId = order.getOrderId();
        if (order.getUser() != null) {
            this.usuario = new UsuarioResponse(order.getUser());
        }
        if (order.getOrderItems() != null) {
            this.orderItems = order.getOrderItems().stream().map(item -> {
                OrderItemResponse oir = new OrderItemResponse();
                oir.setId(item.getId());
                oir.setProduct(item.getProduct());
                oir.setSize(item.getSize());
                oir.setColor(item.getColor());
                oir.setQuantity(item.getQuantity());
                oir.setPrice(item.getPrice());
                oir.setDiscountedPrice(item.getDiscountedPrice());
                oir.setDeliveryDate(item.getDeliveryDate());
                return oir;
            }).collect(Collectors.toList());
        }
        this.orderDate = order.getOrderDate();
        this.deliveryDate = order.getDeliveryDate();
        this.shippingAddress = order.getShippingAddress();
        this.paymentDetails = order.getPaymentDetails();
        this.totalPrice = order.getTotalPrice();
        this.totalDiscountedPrice = order.getTotalDiscountedPrice();
        this.discounte = order.getDiscounte();
        this.orderStatus = order.getOrderStatus();
        this.totalItem = order.getTotalItem();
        this.createdAt = order.getCreateAt();
        this.trackingNumber = order.getTrackingNumber();
        this.shippingLabelUrl = order.getShippingLabelUrl();
        this.shippingMethod = order.getShippingMethod();
        this.storeName = order.getStoreName();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public UsuarioResponse getUsuario() { return usuario; }
    public void setUsuario(UsuarioResponse usuario) { this.usuario = usuario; }
    public List<OrderItemResponse> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResponse> orderItems) { this.orderItems = orderItems; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
    public PaymentDetails getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetails paymentDetails) { this.paymentDetails = paymentDetails; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public Integer getTotalDiscountedPrice() { return totalDiscountedPrice; }
    public void setTotalDiscountedPrice(Integer totalDiscountedPrice) { this.totalDiscountedPrice = totalDiscountedPrice; }
    public Integer getDiscounte() { return discounte; }
    public void setDiscounte(Integer discounte) { this.discounte = discounte; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public int getTotalItem() { return totalItem; }
    public void setTotalItem(int totalItem) { this.totalItem = totalItem; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getShippingLabelUrl() { return shippingLabelUrl; }
    public void setShippingLabelUrl(String shippingLabelUrl) { this.shippingLabelUrl = shippingLabelUrl; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
}
