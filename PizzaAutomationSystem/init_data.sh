#!/bin/bash

echo "==========================================="
echo "Инициализация Pizza Automation System"
echo "==========================================="
echo ""

echo "1. Проверка структуры папок..."
mkdir -p logs
mkdir -p data
mkdir -p docs

echo "2. Создание директорий для данных..."
mkdir -p data/{menu,orders,users,reports}

echo "3. Проверка наличия файлов..."
if [ ! -f "pom.xml" ]; then
    echo "Ошибка: pom.xml не найден!"
    exit 1
fi

if [ ! -f "run_with_h2.sh" ]; then
    echo "Предупреждение: run_with_h2.sh не найден"
    echo "   Используйте: ./run.sh или создайте run_with_h2.sh"
fi

echo ""
echo "Инициализация завершена!"
echo ""
echo "Для запуска приложения используйте:"
echo "   ./run_with_h2.sh    # С H2 Database"
echo "   или"
echo "   ./run.sh           # Без H2 (если есть)"
echo ""
echo "Веб-консоль H2 будет доступна по: http://localhost:8082"
