package com.pizzeria.dao;

import com.pizzeria.models.Pizza;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PizzaDAODB {

    public List<Pizza> getAllPizzas() {
        List<Pizza> pizzas = new ArrayList<>();
        String sql = "SELECT * FROM pizza ORDER BY name";

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                pizzas.add(mapResultSetToPizza(rs));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при получении пицц: " + e.getMessage());
        }

        return pizzas;
    }

    public Optional<Pizza> getPizzaById(String id) {
        String sql = "SELECT * FROM pizza WHERE id = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Pizza pizza = mapResultSetToPizza(rs);
                rs.close();
                pstmt.close();
                return Optional.of(pizza);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пиццы: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Pizza> getPizzasByCategory(Pizza.Category category) {
        List<Pizza> pizzas = new ArrayList<>();
        String sql = "SELECT * FROM pizza WHERE category = ? ORDER BY name";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                pizzas.add(mapResultSetToPizza(rs));
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Ошибка при фильтрации пицц по категории: " + e.getMessage());
        }

        return pizzas;
    }

    public boolean addPizza(Pizza pizza) {
        String sql = """
            INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available, image_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, pizza.getId());
            pstmt.setString(2, pizza.getName());
            pstmt.setString(3, pizza.getDescription());
            pstmt.setString(4, String.join(", ", pizza.getIngredients()));
            pstmt.setString(5, pizza.getSize().name());
            pstmt.setDouble(6, pizza.getPrice());
            pstmt.setInt(7, pizza.getPreparationTime());
            pstmt.setString(8, pizza.getCategory().name());
            pstmt.setBoolean(9, pizza.isAvailable());

            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пиццы: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePizza(Pizza pizza) {
        String sql = """
            UPDATE pizza 
            SET name = ?, description = ?, ingredients = ?, size = ?, price = ?, preparation_time = ?, 
                category = ?, is_available = ?
            WHERE id = ?
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, pizza.getName());
            pstmt.setString(2, pizza.getDescription());
            pstmt.setString(3, String.join(", ", pizza.getIngredients()));
            pstmt.setString(4, pizza.getSize().name());
            pstmt.setDouble(5, pizza.getPrice());
            pstmt.setInt(6, pizza.getPreparationTime());
            pstmt.setString(7, pizza.getCategory().name());
            pstmt.setBoolean(8, pizza.isAvailable());
            pstmt.setString(9, pizza.getId());

            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении пиццы: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePizza(String id) {
        String sql = "DELETE FROM pizza WHERE id = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пиццы: " + e.getMessage());
            return false;
        }
    }

    private Pizza mapResultSetToPizza(ResultSet rs) throws SQLException {
        List<String> ingredients = List.of(rs.getString("ingredients").split(", "));
        return new Pizza(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                ingredients,
                Pizza.PizzaSize.valueOf(rs.getString("size")),
                rs.getDouble("price"),
                rs.getInt("preparation_time"),
                Pizza.Category.valueOf(rs.getString("category")),
                rs.getBoolean("is_available")
        );
    }

    public void initializeSampleData() {
        System.out.println("Данные инициализированы в H2");
    }
}
