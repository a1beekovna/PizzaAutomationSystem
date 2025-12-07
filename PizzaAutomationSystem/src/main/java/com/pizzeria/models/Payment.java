package com.pizzeria.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private String paymentId;
    private PaymentMethod method;
    private double amount;
    private PaymentStatus status;
    private LocalDateTime paymentTime;

    public enum PaymentMethod {
        CASH("Наличные"),
        CARD("Карта"),
        ONLINE("Онлайн"),
        LOYALTY_POINTS("Баллы лояльности");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        PENDING("В ожидании"),
        COMPLETED("Завершен"),
        FAILED("Неудачно"),
        REFUNDED("Возвращен");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Payment(String paymentId, PaymentMethod method, double amount, PaymentStatus status, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.paymentTime = paymentTime;
    }

    public Payment(PaymentMethod method, double amount) {
        this(UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                method, amount, PaymentStatus.PENDING, null);
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public PaymentMethod getMethod() { return method; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getPaymentTime() { return paymentTime; }

    // Setters
    public void setStatus(PaymentStatus status) { this.status = status; }

    public boolean processPayment() {
        if (status == PaymentStatus.PENDING) {
            status = PaymentStatus.COMPLETED;
            paymentTime = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public String getFormattedAmount() {
        return String.format("%.2f₸", amount);
    }
}
