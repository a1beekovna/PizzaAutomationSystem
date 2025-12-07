package com.pizzeria.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseConnection {
    private static Connection connection;
    private static boolean initialized = false;

    static {
        try {
            // Подключение к файловой H2 базе
            // База будет храниться в папке "database" рядом с проектом
            connection = DriverManager.getConnection(
                    "jdbc:h2:file:./database/pizzeria;DB_CLOSE_DELAY=-1",
                    "sa",
                    ""
            );

            initializeDatabase();
            initialized = true;
            System.out.println("H2 Database подключена (файловая база)");

        } catch (Exception e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();

            InputStream inputStream = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("schema.sql");

            if (inputStream == null) {
                throw new RuntimeException("Не найден schema.sql в resources");
            }

            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Выполняем все SQL-запросы из schema.sql
            for (String sqlStatement : sql.split(";")) {
                String trimmed = sqlStatement.trim();
                if (!trimmed.isEmpty()) {
                    System.out.println("Выполняем SQL: " + trimmed);
                    stmt.execute(trimmed);
                }
            }

            System.out.println("Таблицы созданы и данные инициализированы");

        } catch (SQLException e) {
            System.err.println("Ошибка создания таблиц: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    "jdbc:h2:file:./database/pizzeria;DB_CLOSE_DELAY=-1",
                    "sa",
                    ""
            );
        }
        return connection;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }
}