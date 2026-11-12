-- =====================================================================
--  WOLFSPORT - FILE SQL HOÀN CHỈNH (tiếng Việt có dấu)
--  Dùng để chuyển dữ liệu sang máy khác: tạo database, bảng và dữ liệu mẫu.
--
--  CÁCH DÙNG:
--    Cách 1 (Workbench): File > Open SQL Script > chọn file này > bấm nút tia sét.
--    Cách 2 (dòng lệnh): mysql -u root -p --default-character-set=utf8mb4 < wolfsport_full.sql
--
--  DÙNG CHO MÁY MỚI: chưa có database hay bảng nào. File sẽ tạo database,
--  tạo bảng và nhập dữ liệu mẫu. Chỉ chạy MỘT lần (chạy lại sẽ báo bảng đã tồn tại).
--
--  Tài khoản mẫu (mật khẩu đã mã hóa BCrypt, mật khẩu đăng nhập là 123456):
--    admin@wolfsport.com  (ADMIN)
--    an@gmail.com         (CUSTOMER)
--    binh@gmail.com       (CUSTOMER)
-- =====================================================================

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS wolfsport
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE wolfsport;

-- =====================================================================
-- 1. TẠO BẢNG
-- =====================================================================

-- Bảng người dùng
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng danh mục
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng sản phẩm
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_categories
        FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_product_price CHECK (price >= 0),
    CONSTRAINT chk_product_stock CHECK (stock_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng giỏ hàng
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_carts_users
        FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng chi tiết giỏ hàng
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_items_carts
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    CONSTRAINT fk_cart_items_products
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng đơn hàng
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_users
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_order_amount CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng chi tiết đơn hàng
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_order_items_orders
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_products
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price CHECK (unit_price >= 0 AND subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 2. DỮ LIỆU MẪU
-- =====================================================================

-- Người dùng (mật khẩu BCrypt của "123456")
INSERT INTO users (full_name, email, password, phone, address, role, status) VALUES
('Admin Wolfsport', 'admin@wolfsport.com',
 '$2a$10$PVyMGrH7cVPNxaZkFac18.pZdGDLD7HdB3ltkUHQqanlYT4.N9Zoi', '0901234567', 'Hà Nội', 'ADMIN', 'ACTIVE'),
('Nguyễn Văn An', 'an@gmail.com',
 '$2a$10$PVyMGrH7cVPNxaZkFac18.pZdGDLD7HdB3ltkUHQqanlYT4.N9Zoi', '0912345678', 'Hà Nội', 'CUSTOMER', 'ACTIVE'),
('Trần Thị Bình', 'binh@gmail.com',
 '$2a$10$PVyMGrH7cVPNxaZkFac18.pZdGDLD7HdB3ltkUHQqanlYT4.N9Zoi', '0987654321', 'Bắc Ninh', 'CUSTOMER', 'ACTIVE');

-- Danh mục
INSERT INTO categories (category_name, description, status) VALUES
('Bóng đá', 'Các sản phẩm phục vụ môn bóng đá', 'ACTIVE'),
('Bóng rổ', 'Dụng cụ và phụ kiện bóng rổ', 'ACTIVE'),
('Cầu lông', 'Vợt, cầu và phụ kiện cầu lông', 'ACTIVE'),
('Dụng cụ tập gym', 'Tạ, thiết bị và dụng cụ tập gym', 'ACTIVE'),
('Bóng chuyền', 'Dụng cụ phục vụ môn bóng chuyền', 'ACTIVE');

-- Sản phẩm
-- (ảnh đặt trong thư mục src/main/resources/static/images/ của project)
INSERT INTO products
(category_id, product_name, description, price, stock_quantity, image_url, status) VALUES
-- Bóng đá (category_id = 1)
(1, 'Bóng đá Adidas', 'Bóng đá tập luyện Adidas',
 450000, 50, 'images/adidas-ball.jpg', 'ACTIVE'),
(1, 'Giày đá bóng Nike', 'Giày đá bóng sân cỏ nhân tạo',
 1200000, 25, 'images/nike-football.jpg', 'ACTIVE'),
-- Bóng rổ (category_id = 2)
(2, 'Bóng rổ Spalding', 'Bóng rổ cao su chuyên nghiệp',
 650000, 30, 'images/spalding.jpg', 'ACTIVE'),
(2, 'Trụ bóng rổ mini', 'Trụ bóng rổ dành cho tập luyện',
 1500000, 10, 'images/basketball-hoop.jpg', 'ACTIVE'),
-- Cầu lông (category_id = 3)
(3, 'Vợt cầu lông Yonex', 'Vợt cầu lông cho người mới',
 850000, 20, 'images/yonex.jpg', 'ACTIVE'),
(3, 'Ống cầu lông', 'Ống cầu lông tập luyện',
 180000, 60, 'images/shuttlecock.jpg', 'ACTIVE'),
-- Dụng cụ tập gym (category_id = 4)
(4, 'Tạ tay 5kg', 'Tạ tay bọc cao su',
 250000, 40, 'images/dumbbell.jpg', 'ACTIVE'),
(4, 'Thảm yoga', 'Thảm tập yoga chống trơn trượt',
 200000, 35, 'images/yoga-mat.jpg', 'ACTIVE'),
-- Bóng chuyền (category_id = 5)
(5, 'Bóng chuyền Mikasa', 'Bóng chuyền đa năng',
 550000, 25, 'images/mikasa.jpg', 'ACTIVE'),
(5, 'Lưới bóng chuyền', 'Lưới bóng chuyền tập luyện',
 700000, 15, 'images/volleyball-net.jpg', 'ACTIVE');

-- Giỏ hàng của 2 khách hàng
INSERT INTO carts (user_id) VALUES (2), (3);

-- Sản phẩm trong giỏ hàng
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 1, 2),
(1, 3, 1),
(1, 5, 1),
(2, 7, 2);

-- Đơn hàng mẫu (tổng tiền khớp với chi tiết đơn hàng)
INSERT INTO orders
(user_id, receiver_name, receiver_phone, shipping_address, total_amount, status, note) VALUES
(2, 'Nguyễn Văn An', '0912345678', 'Hà Nội', 1750000, 'COMPLETED',
 'Giao hàng giờ hành chính'),
(3, 'Trần Thị Bình', '0987654321', 'Bắc Ninh', 500000, 'PENDING',
 'Vui lòng gọi trước khi giao');

-- Chi tiết đơn hàng
INSERT INTO order_items
(order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
-- Đơn hàng 1: 900.000 + 850.000 = 1.750.000
(1, 1, 'Bóng đá Adidas', 2, 450000, 900000),
(1, 5, 'Vợt cầu lông Yonex', 1, 850000, 850000),
-- Đơn hàng 2
(2, 7, 'Tạ tay 5kg', 2, 250000, 500000);

-- =====================================================================
-- 3. KIỂM TRA SAU KHI NHẬP
-- =====================================================================
SHOW TABLES;

SELECT user_id, full_name, email, role, status FROM users;

SELECT p.product_id, p.product_name, c.category_name, p.price, p.stock_quantity
FROM products p
JOIN categories c ON p.category_id = c.category_id;

-- Đơn hàng: cột chenh_lech phải bằng 0
SELECT o.order_id, u.full_name, o.total_amount, o.status,
       SUM(oi.subtotal) AS tong_chi_tiet,
       o.total_amount - SUM(oi.subtotal) AS chenh_lech
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN order_items oi ON oi.order_id = o.order_id
GROUP BY o.order_id, u.full_name, o.total_amount, o.status;
