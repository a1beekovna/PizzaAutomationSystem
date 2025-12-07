package com.pizzeria;

import com.pizzeria.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainApp extends Application {
    private static final Logger logger = LogManager.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Запуск Pizza Automation System с H2 Database");

            // Загрузка FXML файла
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pizzeria/views/main.fxml"));
            Parent root = loader.load();

            // Настройка сцены
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/pizzeria/styles/styles.css").toExternalForm());

            // Настройка Stage
            primaryStage.setTitle("Pizza Automation System v2.0");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // Обработка закрытия окна
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Завершение работы приложения...");
                try {
                    DatabaseConnection.close();
                } catch (Exception e) {
                    logger.error("Ошибка при закрытии БД", e);
                }
            });

            // Показ окна
            primaryStage.show();

            logger.info("Приложение успешно запущено с H2 БД");

        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
            showErrorAlert("Критическая ошибка", "Не удалось запустить приложение:\n" + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        // Инициализация Log4j2
        System.setProperty("log4j.configurationFile", "log4j2.xml");

        // Запуск JavaFX приложения
        launch(args);
    }
}
