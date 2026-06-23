-- Initial database seed data for BuildNest – E-Commerce Platform for Home Construction and Décor Products
-- This script populates essential data for application bootstrap

-- ==================================================
-- ROLES: Define user role hierarchy
-- ==================================================
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator with full system access'),
('ROLE_USER', 'Regular customer with shopping capabilities')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ==================================================
-- PERMISSIONS: Define granular access controls
-- ==================================================
INSERT INTO permissions (name, description) VALUES
-- Admin permissions
('MANAGE_USERS', 'Create, read, update, delete users'),
('MANAGE_PRODUCTS', 'Create, read, update, delete products'),
('MANAGE_INVENTORY', 'Manage stock levels and inventory'),
('MANAGE_ORDERS', 'View and process all orders'),
('VIEW_REPORTS', 'Access analytics and reports'),
('VIEW_AUDIT_LOGS', 'View system audit logs'),
-- User permissions
('VIEW_PRODUCTS', 'Browse product catalog'),
('MANAGE_CART', 'Add/remove items from cart'),
('PLACE_ORDER', 'Create and submit orders'),
('VIEW_OWN_ORDERS', 'View own order history'),
('MANAGE_PROFILE', 'Update own profile information')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ==================================================
-- ROLE-PERMISSION MAPPING
-- ==================================================
-- Admin role gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- User role gets limited permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER'
AND p.name IN ('VIEW_PRODUCTS', 'MANAGE_CART', 'PLACE_ORDER', 'VIEW_OWN_ORDERS', 'MANAGE_PROFILE')
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- ==================================================
-- DEFAULT ADMIN USER
-- ==================================================
-- Password: Admin@123 (BCrypt hashed)
-- IMPORTANT: Change this password in production!
INSERT INTO users (username, email, password, first_name, last_name, phone, created_at, updated_at, deleted)
VALUES (
    'admin',
    'admin@civil-ecommerce.com',
    '$2a$10$XPTYZfFvNq/JEVB6d3v0Qe.QZJqkHZb4.YLkBLLJCQYCZ/vvQZGKG',
    'System',
    'Administrator',
    '+1234567890',
    NOW(),
    NOW(),
    false
)
ON DUPLICATE KEY UPDATE updated_at=NOW();

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- ==================================================
-- PRODUCT CATEGORIES
-- ==================================================
INSERT INTO categories (name, description, created_at, updated_at, deleted) VALUES
('Cement', 'Various types of cement for construction', NOW(), NOW(), false),
('Steel & Iron', 'Steel bars, rods, and iron products', NOW(), NOW(), false),
('Bricks & Blocks', 'Building bricks, blocks, and tiles', NOW(), NOW(), false),
('Sand & Aggregates', 'Construction sand, gravel, and aggregates', NOW(), NOW(), false),
('Paint & Finishes', 'Paints, primers, and finishing materials', NOW(), NOW(), false),
('Plumbing', 'Pipes, fittings, and plumbing supplies', NOW(), NOW(), false),
('Electrical', 'Wires, switches, and electrical components', NOW(), NOW(), false),
('Tools & Equipment', 'Construction tools and equipment', NOW(), NOW(), false),
('Hardware', 'Nails, screws, hinges, and hardware', NOW(), NOW(), false),
('Safety Equipment', 'Safety gear and protective equipment', NOW(), NOW(), false)
ON DUPLICATE KEY UPDATE updated_at=NOW();

-- ==================================================
-- SAMPLE PRODUCTS (10 products for testing)
-- ==================================================
INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'OPC 53 Grade Cement', 
    'High-quality Ordinary Portland Cement 53 Grade, ideal for high-strength concrete work. 50kg bag.',
    450.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Cement',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Cement' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'TMT Steel Bars 12mm', 
    'Thermo-Mechanically Treated steel bars, 12mm diameter, 12-meter length. Fe 500D grade.',
    650.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Steel+Bar',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Steel & Iron' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Red Clay Bricks', 
    'Standard size red clay bricks for masonry work. Size: 9"x4"x3". Pack of 100 bricks.',
    800.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Bricks',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Bricks & Blocks' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'M-Sand (Manufactured Sand)', 
    'High-quality manufactured sand for concrete and plastering. 1 ton bag.',
    1200.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Sand',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Sand & Aggregates' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Asian Paints Apex Exterior', 
    'Weather-proof exterior emulsion paint. 20 liters. Available in multiple colors.',
    3500.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Paint',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Paint & Finishes' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'PVC Pipes 2 inch', 
    'Durable PVC pipes for plumbing. 2 inch diameter, 6 meters length. ISI certified.',
    350.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=PVC+Pipe',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Plumbing' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Copper Wire 2.5mm', 
    'Electrical copper wire, 2.5mm gauge. 90 meters coil. ISI marked.',
    1800.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Copper+Wire',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Electrical' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Power Drill Machine', 
    'Professional-grade power drill with 13mm chuck, 650W motor, variable speed.',
    2500.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Drill+Machine',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Tools & Equipment' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Stainless Steel Hinges', 
    'Heavy-duty stainless steel door hinges. 4 inch size. Pack of 10 pieces.',
    450.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Hinges',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Hardware' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

INSERT INTO products (name, description, price, category_id, image_url, created_at, updated_at, deleted)
SELECT 
    'Safety Helmet', 
    'ISI approved safety helmet for construction workers. Adjustable strap.',
    250.00,
    c.id,
    'https://via.placeholder.com/300x300.png?text=Safety+Helmet',
    NOW(),
    NOW(),
    false
FROM categories c WHERE c.name = 'Safety Equipment' LIMIT 1
ON DUPLICATE KEY UPDATE updated_at=NOW();

-- ==================================================
-- INVENTORY: Initial stock for sample products
-- ==================================================
INSERT INTO inventory (product_id, quantity, reserved_quantity, last_updated)
SELECT p.id, 1000, 0, NOW()
FROM products p
WHERE NOT EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id)
ON DUPLICATE KEY UPDATE last_updated=NOW();

-- ==================================================
-- SCRIPT COMPLETION
-- ==================================================
-- Data initialization complete
SELECT 'Database seeding completed successfully!' AS Status;
