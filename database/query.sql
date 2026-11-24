-- create Datebase
CREATE DATABASE IF NOT EXISTS db_storemate;

-- select the created Database
USE db_storemate;

-- Table: categories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- Table: products
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    image_path VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);

-- Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subtotal DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    amount_received DECIMAL(10, 2) NOT NULL,
    change_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: order_item
CREATE TABLE IF NOT EXISTS orders_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    order_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);

-- INSERT: 4 item to categories Food, Snacks, Beverages, Personal Care
INSERT IGNORE INTO categories (category_name)
VALUES
    ('Food'),
    ('Snacks'),
    ('Beverages'),
    ('Personal Care');


-- INSERT: Sample Products
INSERT IGNORE INTO products (category_id, product_name, price)
SELECT c.id, p.product_name, p.price
FROM (
    SELECT 'Food' AS category_name, 'Century Tuna' AS product_name, 41.00 AS price
    UNION ALL SELECT 'Food', 'Pancit Canton', 23.00
    UNION ALL SELECT 'Food', 'Mega Sardines', 26.00
    UNION ALL SELECT 'Food', 'Argentina Meatloaf', 32.00
    UNION ALL SELECT 'Food', 'Lucky Me Beef', 12.00
    UNION ALL SELECT 'Food', '555 Sardines', 28.00
    UNION ALL SELECT 'Food', 'Argentina Corned Beef', 45.00
    UNION ALL SELECT 'Food', 'Spam', 120.00
    UNION ALL SELECT 'Food', 'Purefoods Hotdog', 75.00
    UNION ALL SELECT 'Food', 'Egg (piece)', 9.00
    UNION ALL SELECT 'Food', 'Tocino', 60.00
    UNION ALL SELECT 'Food', 'Longganisa', 70.00

    UNION ALL SELECT 'Snacks', 'Piattos', 15.00
    UNION ALL SELECT 'Snacks', 'Nova', 15.00
    UNION ALL SELECT 'Snacks', 'Chippy', 15.00
    UNION ALL SELECT 'Snacks', 'Cream-O', 10.00
    UNION ALL SELECT 'Snacks', 'Skyflakes', 10.00
    UNION ALL SELECT 'Snacks', 'Boy Bawang', 15.00
    UNION ALL SELECT 'Snacks', 'Pillows', 12.00
    UNION ALL SELECT 'Snacks', 'Rebisco Crackers', 8.00
    UNION ALL SELECT 'Snacks', 'Hansel', 9.00

    UNION ALL SELECT 'Beverages', 'Coke 1.5L', 85.00
    UNION ALL SELECT 'Beverages', 'Milo', 10.00
    UNION ALL SELECT 'Beverages', 'Royal 1.5L', 82.00
    UNION ALL SELECT 'Beverages', 'Sprite 1.5L', 82.00
    UNION ALL SELECT 'Beverages', 'C2 Green Tea', 25.00
    UNION ALL SELECT 'Beverages', 'Nescafe 3in1', 12.00
    UNION ALL SELECT 'Beverages', 'Kopiko 3in1', 10.00
    UNION ALL SELECT 'Beverages', 'Gatorade', 30.00
    UNION ALL SELECT 'Beverages', 'Bottled Water 500ml', 20.00

    UNION ALL SELECT 'Personal Care', 'Safeguard', 23.00
    UNION ALL SELECT 'Personal Care', 'Palmolive Shampoo', 8.00
    UNION ALL SELECT 'Personal Care', 'Colgate', 95.00
    UNION ALL SELECT 'Personal Care', 'Dove Soap', 55.00
    UNION ALL SELECT 'Personal Care', 'Head & Shoulders', 8.00
    UNION ALL SELECT 'Personal Care', 'Rexona', 8.00
    UNION ALL SELECT 'Personal Care', 'Johnson''s Baby Powder', 45.00
    UNION ALL SELECT 'Personal Care', 'Cotton Buds', 25.00
) AS p
INNER JOIN categories c
    ON c.category_name = p.category_name;