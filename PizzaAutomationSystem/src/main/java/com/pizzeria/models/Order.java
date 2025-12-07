package com.pizzeria.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private String orderId;
    private Customer customer;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    private Payment payment;
    private String notes;
    private String deliveryAddress;
    private DeliveryType deliveryType;

    public enum OrderStatus {
        PENDING("В ожидании"),
        CONFIRMED("Подтвержден"),
        PREPARING("Готовится"),
        BAKING("Выпекается"),
        READY("Готов"),
        DELIVERING("Доставляется"),
        COMPLETED("Завершен"),
        CANCELLED("Отменен");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryType {
        PICKUP("Самовывоз"),
        DELIVERY("Доставка");

        private final String displayName;

        DeliveryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Order(String orderId, Customer customer, List<OrderItem> items, OrderStatus status,
                 LocalDateTime orderTime, LocalDateTime estimatedReadyTime, Payment payment,
                 String notes, String deliveryAddress, DeliveryType deliveryType) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = items;
        this.status = status;
        this.orderTime = orderTime;
        this.estimatedReadyTime = estimatedReadyTime;
        this.payment = payment;
        this.notes = notes;
        this.deliveryAddress = deliveryAddress;
        this.deliveryType = deliveryType;
    }

    public Order(Customer customer, List<OrderItem> items, String notes,
                 String deliveryAddress, DeliveryType deliveryType) {
        this(UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                customer, items, OrderStatus.PENDING, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30), null, notes,
                deliveryAddress, deliveryType);
    }

    public double calculateTotal() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public int calculateTotalPreparationTime() {
        return items.stream().mapToInt(OrderItem::getPreparationTime).max().orElse(0);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public LocalDateTime getEstimatedReadyTime() { return estimatedReadyTime; }
    public Payment getPayment() { return payment; }
    public String getNotes() { return notes; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public DeliveryType getDeliveryType() { return deliveryType; }

    // Setters
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public void setEstimatedReadyTime(LocalDateTime estimatedReadyTime) { this.estimatedReadyTime = estimatedReadyTime; }

    public String getFormattedTotal() {
        return String.format("%.2f₸", calculateTotal());
    }

    public String getFormattedOrderTime() {
        return orderTime.toString().replace("T", " ");
    }

    @Override
    public String toString() {
        return String.format("Заказ #%s - %s - %s", orderId, status.getDisplayName(), getFormattedTotal());
    }
}
