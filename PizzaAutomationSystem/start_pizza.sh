#!/bin/bash

cd ~/Desktop/PizzaAutomationSystem

echo "========================================="
echo "Pizza Automation System"
echo "========================================="
echo ""

JAVAFX_PATH="$HOME/Downloads/javafx-sdk-17.0.17/lib"

echo "1. Проверка JavaFX..."
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "JavaFX не найден!"
    exit 1
fi
echo "JavaFX: $JAVAFX_PATH"

echo ""
echo "2. Проверка классов..."
if [ ! -f "target/classes/com/pizzeria/MainApp.class" ]; then
    echo "MainApp.class не найден! Компилируем..."
    mvn clean compile
fi
echo "MainApp.class найден"

echo ""
echo "3. Сборка classpath..."
CP="target/classes"

# H2
H2_JAR="$HOME/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar"
if [ -f "$H2_JAR" ]; then
    CP="$CP:$H2_JAR"
    echo "Добавлен H2"
fi

# Jackson
JACKSON_JAR="$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
if [ -f "$JACKSON_JAR" ]; then
    CP="$CP:$JACKSON_JAR"
    echo "Добавлен Jackson"
else
    # Ищем любую версию Jackson
    JACKSON_ANY=$(find ~/.m2/repository/com/fasterxml/jackson/core/jackson-databind -name "*.jar" 2>/dev/null | head -1)
    if [ -n "$JACKSON_ANY" ]; then
        CP="$CP:$JACKSON_ANY"
        echo "Добавлен Jackson ($JACKSON_ANY)"
    fi
fi

echo ""
echo "4. Запуск приложения..."
echo "Classpath: $CP"
echo "========================================="

java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -Dprism.order=sw \
     -cp "$CP" \
     com.pizzeria.MainApp
