package com.pizzeria.services;

import com.pizzeria.models.*;
import com.pizzeria.dao.OrderDAODB;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private final OrderDAODB orderDAODB;
    private final PizzaService pizzaService;

    public OrderService(PizzaService pizzaService) {
        this.orderDAODB = new OrderDAODB();
        this.pizzaService = pizzaService;
    }

    public Order createOrder(Customer customer, List<OrderItem> items,
                             String notes, String deliveryAddress,
                             Order.DeliveryType deliveryType) throws IOException {

        // Сначала сохраняем клиента в БД
        Customer savedCustomer = orderDAODB.saveCustomer(customer);
        if (savedCustomer == null) {
            throw new IOException("Не удалось сохранить клиента");
        }

        int maxPrepTime = items.stream()
                .mapToInt(OrderItem::getPreparationTime)
                .max()
                .orElse(0);

        Order order = new Order(savedCustomer, items, notes, deliveryAddress, deliveryType);
        order.setEstimatedReadyTime(LocalDateTime.now().plusMinutes(maxPrepTime));

        return orderDAODB.createOrder(order);
    }

    public List<Order> getActiveOrders() throws IOException {
        return orderDAODB.getAllActiveOrders();
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) throws IOException {
        return orderDAODB.getOrdersByStatus(status);
    }

    public List<Order> getTodayOrders() throws IOException {
        return orderDAODB.getTodayOrders();
    }

    public Optional<Order> getOrderById(String orderId) throws IOException {
        return orderDAODB.getOrderById(orderId);
    }

    public boolean updateOrderStatus(String orderId, Order.OrderStatus newStatus) throws IOException {
        Optional<Order> orderOpt = orderDAODB.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            return orderDAODB.updateOrder(order);
        }
        return false;
    }

    public boolean completeOrder(String orderId) throws IOException {
        return orderDAODB.completeOrder(orderId);
    }

    public double calculateOrderTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    public int getTotalOrdersCount() throws IOException {
        return orderDAODB.getAllActiveOrders().size() + orderDAODB.getCompletedOrders().size();
    }

    public double getTotalRevenue() throws IOException {
        double activeRevenue = orderDAODB.getAllActiveOrders().stream()
                .mapToDouble(Order::calculateTotal)
                .sum();

        double completedRevenue = orderDAODB.getCompletedOrders().stream()
                .mapToDouble(Order::calculateTotal)
                .sum();

        return activeRevenue + completedRevenue;
    }
}
