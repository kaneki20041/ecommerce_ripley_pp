package com.proyecto.request;

import lombok.Data;

@Data
public class PaymentProcessRequest {
    private Long orderId;
    private String token;
    private String issuerId;
    private String paymentMethodId;
    private Float transactionAmount;
    private Integer installments;
    private Payer payer;

    @Data
    public static class Payer {
        private String email;
        private Identification identification;
    }

    @Data
    public static class Identification {
        private String type;
        private String number;
    }
}
