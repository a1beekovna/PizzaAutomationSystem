package com.pizzeria.dao;

import com.pizzeria.models.*;
import com.pizzeria.models.Order.OrderStatus;
import com.pizzeria.models.Order.DeliveryType;
import com.pizzeria.models.Payment.PaymentMethod;
import com.pizzeria.models.Payment.PaymentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAODB {

    // ==================== CUSTOMER OPERATIONS ====================

    public Customer saveCustomer(Customer customer) {
        String checkSql = "SELECT id FROM customers WHERE phone = ?";
        String insertSql = "INSERT INTO customers (id, name, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        String updateSql = "UPDATE customers SET name = ?, email = ?, address = ? WHERE phone = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, customer.getPhone());
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                String existingId = rs.getString("id");
                rs.close();
                checkStmt.close();
                
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, customer.getName());
                updateStmt.setString(2, customer.getEmail());
                updateStmt.setString(3, customer.getAddress());
                updateStmt.setString(4, customer.getPhone());
                updateStmt.executeUpdate();
                updateStmt.close();
                
                return new Customer(existingId, customer.getName(), customer.getPhone(),
                        customer.getEmail(), customer.getAddress(), LocalDate.now(), 0, 0);
            }
            rs.close();
            checkStmt.close();

            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, customer.getId());
            insertStmt.setString(2, customer.getName());
            insertStmt.setString(3, customer.getPhone());
            insertStmt.setString(4, customer.getEmail());
            insertStmt.setString(5, customer.getAddress());
            insertStmt.executeUpdate();
            insertStmt.close();
            
            return customer;

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении клиента: " + e.getMessage());
            return null;
        }
    }

    public Optional<Customer> getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                rs.close();
                pstmt.close();
                return Optional.of(customer);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске клиента: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ==================== ORDER OPERATIONS ====================

    public Order createOrder(Order order) {
        String orderSql = "INSERT INTO orders (order_id, customer_id, status, order_time, estimated_ready_time, " +
                "delivery_address, delivery_type, notes, payment_method, payment_status, total_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, pizza_id, quantity, unit_price)" +
                "VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            try {
                PreparedStatement orderStmt = conn.prepareStatement(orderSql);
                orderStmt.setString(1, order.getOrderId());
                orderStmt.setString(2, order.getCustomer() != null ? order.getCustomer().getId() : null);
                orderStmt.setString(3, order.getStatus().name());
                orderStmt.setTimestamp(4, Timestamp.valueOf(order.getOrderTime()));
                orderStmt.setTimestamp(5, Timestamp.valueOf(order.getEstimatedReadyTime()));
                orderStmt.setString(6, order.getDeliveryAddress());
                orderStmt.setString(7, order.getDeliveryType() != null ? order.getDeliveryType().name() : "PICKUP");
                orderStmt.setString(8, order.getNotes());
                orderStmt.setString(9, order.getPayment() != null ? order.getPayment().getMethod().name() : "CASH");
                orderStmt.setString(10, order.getPayment() != null ? order.getPayment().getStatus().name() : "PENDING");
                orderStmt.setDouble(11, order.calculateTotal());
                orderStmt.executeUpdate();
                orderStmt.close();

                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                for (OrderItem item : order.getItems()) {
                    itemStmt.setString(1, order.getOrderId());
                    itemStmt.setString(2, item.getPizza().getId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getPizza().getPrice());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
                itemStmt.close();

                conn.commit();
                return order;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            return null;
        }
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET status = ?, estimated_ready_time = ?, delivery_address = ?, " +
                "payment_status = ? WHERE order_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, order.getStatus().name());
            pstmt.setTimestamp(2, Timestamp.valueOf(order.getEstimatedReadyTime()));
            pstmt.setString(3, order.getDeliveryAddress());
            pstmt.setString(4, order.getPayment() != null ? order.getPayment().getStatus().name() : "PENDING");
            pstmt.setString(5, order.getOrderId());

            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении заказа: " + e.getMessage());
            return false;
        }
    }

    public boolean updateOrderStatus(String orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, status.name());
            pstmt.setString(2, orderId);

            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при смене статуса заказа: " + e.getMessage());
            return false;
        }
    }

    public boolean completeOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    public boolean cancelOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_time DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при получении заказов: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getAllActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY order_time DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при получении активных заказов: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getCompletedOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'COMPLETED' ORDER BY order_time DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при получении завершенных заказов: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_time DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при фильтрации по статусу: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getTodayOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE CAST(order_time AS DATE) = CURRENT_DATE ORDER BY order_time DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при получении заказов за сегодня: " + e.getMessage());
        }
        return orders;
    }

    public Optional<Order> getOrderById(String orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                rs.close();
                pstmt.close();
                return Optional.of(order);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске заказа: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ==================== STATISTICS ====================

    public int getTotalOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчета заказов: " + e.getMessage());
        }
        return 0;
    }

    public int getTodayOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE CAST(order_time AS DATE) = CURRENT_DATE";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчета заказов за сегодня: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED'";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble(1);
                rs.close();
                stmt.close();
                return revenue;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчета выручки: " + e.getMessage());
        }
        return 0;
    }

    public double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED' AND CAST(order_time AS DATE) = CURRENT_DATE";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble(1);
                rs.close();
                stmt.close();
                return revenue;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчета выручки за сегодня: " + e.getMessage());
        }
        return 0;
    }

    public int getOrdersCountByStatus(OrderStatus status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                pstmt.close();
                return count;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчета по статусу: " + e.getMessage());
        }
        return 0;
    }

    // ==================== HELPERS ====================

    private void loadOrderItems(Order order) {
        String sql = "SELECT oi.*, p.name, p.description, p.ingredients, p.size, p.price, " +
                "p.preparation_time, p.category, p.is_available " +
                "FROM order_items oi JOIN pizza p ON oi.pizza_id = p.id WHERE oi.order_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, order.getOrderId());
            ResultSet rs = pstmt.executeQuery();

            List<OrderItem> items = new ArrayList<>();
            while (rs.next()) {
                Pizza pizza = new Pizza(
                        rs.getString("pizza_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        List.of(rs.getString("ingredients").split(", ")),
                        Pizza.PizzaSize.valueOf(rs.getString("size")),
                        rs.getDouble("unit_price"),
                        rs.getInt("preparation_time"),
                        Pizza.Category.valueOf(rs.getString("category")),
                        rs.getBoolean("is_available")
                );
                OrderItem item = new OrderItem(pizza, rs.getInt("quantity"));
                items.add(item);
            }
            rs.close();
            pstmt.close();
            order.getItems().addAll(items);

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки позиций заказа: " + e.getMessage());
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        String paymentMethodStr = rs.getString("payment_method");
        String paymentStatusStr = rs.getString("payment_status");
        Payment payment = null;
        if (paymentMethodStr != null) {
            payment = new Payment(
                    PaymentMethod.valueOf(paymentMethodStr),
                    rs.getDouble("total_amount")
            );
            if (paymentStatusStr != null) {
                payment.setStatus(PaymentStatus.valueOf(paymentStatusStr));
            }
        }

        String deliveryTypeStr = rs.getString("delivery_type");
        DeliveryType deliveryType = deliveryTypeStr != null ? DeliveryType.valueOf(deliveryTypeStr) : DeliveryType.PICKUP;

        return new Order(
                rs.getString("order_id"),
                null,
                new ArrayList<>(),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("order_time").toLocalDateTime(),
                rs.getTimestamp("estimated_ready_time").toLocalDateTime(),
                payment,
                rs.getString("notes"),
                rs.getString("delivery_address"),
                deliveryType
        );
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getDate("registration_date").toLocalDate(),
                rs.getInt("loyalty_points"),
                rs.getInt("total_orders")
        );
    }
}
