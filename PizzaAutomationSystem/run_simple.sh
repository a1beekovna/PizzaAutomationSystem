//!/bin/bash

cd ~/Desktop/PizzaAutomationSystem

// Укажите ваш путь к JavaFX
FX_PATH="/Users/zhuldyzaibekqyzy/javafx/javafx-sdk-17.0.17/lib"

echo "Запуск Pizza Automation System..."
echo ""

// Собираем JAR файл
mvn clean package -DskipTests

// Запускаем
java --module-path "$FX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/PizzaAutomationSystem-1.0.0-jar-with-dependencies.jar
