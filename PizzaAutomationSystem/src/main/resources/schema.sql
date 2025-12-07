-- Таблица пицц
CREATE TABLE IF NOT EXISTS pizza (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    ingredients VARCHAR(1000),
    size VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    preparation_time INT NOT NULL,
    category VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE
);

-- Таблица клиентов
CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    registration_date DATE DEFAULT CURRENT_DATE,
    loyalty_points INT DEFAULT 0,
    total_orders INT DEFAULT 0
);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_ready_time TIMESTAMP,
    delivery_address VARCHAR(255),
    delivery_type VARCHAR(20) DEFAULT 'PICKUP',
    notes VARCHAR(500),
    payment_method VARCHAR(20),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Таблица позиций заказа
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(20) NOT NULL,
    pizza_id VARCHAR(10) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (pizza_id) REFERENCES pizza(id)
);

-- Вставка данных пицц только если их нет
INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P001', 'Маргарита', 'Классическая итальянская пицца с томатным соусом и моцареллой',
       'томатный соус, моцарелла, базилик, оливковое масло', 'MEDIUM', 2500.00, 20, 'CLASSIC', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P001');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P002', 'Пепперони', 'Острая пицца с салями пепперони',
       'томатный соус, моцарелла, пепперони, орегано', 'MEDIUM', 3200.00, 25, 'SPICY', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P002');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P003', 'Вегетарианская', 'Для любителей овощей - свежие овощи на хрустящем тесте',
       'томатный соус, моцарелла, болгарский перец, помидоры, грибы, оливки', 'MEDIUM', 2800.00, 22, 'VEGETARIAN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P003');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P004', 'Гавайская', 'Сочетание ветчины и сладкого ананаса',
       'томатный соус, моцарелла, ветчина, ананас', 'MEDIUM', 3000.00, 25, 'SPECIAL', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P004');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P005', 'Четыре сыра', 'Изысканная пицца для настоящих ценителей сыра',
       'моцарелла, пармезан, горгонзола, эмменталь', 'MEDIUM', 3500.00, 30, 'PREMIUM', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P005');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P006', 'Мясная', 'Для настоящих мясоедов - три вида мяса',
       'томатный соус, моцарелла, говядина, курица, бекон', 'LARGE', 4200.00, 35, 'PREMIUM', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P006');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P007', 'Грибная', 'Ароматная пицца с шампиньонами и трюфельным маслом',
       'сливочный соус, моцарелла, шампиньоны, трюфельное масло', 'MEDIUM', 3300.00, 25, 'VEGETARIAN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P007');

INSERT INTO pizza (id, name, description, ingredients, size, price, preparation_time, category, is_available)
SELECT 'P008', 'Диабло', 'Очень острая пицца для любителей огненного вкуса',
       'томатный соус, моцарелла, халапеньо, чили, острая салями', 'MEDIUM', 3400.00, 25, 'SPICY', TRUE
WHERE NOT EXISTS (SELECT 1 FROM pizza WHERE id='P008');

-- Вставка тестового клиента только если его нет
