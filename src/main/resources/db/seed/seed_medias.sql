-- =============================================================================
-- Seed Script: Media Assets for 10 Base Products
-- Target Database: PostgreSQL (ecommerce_db)
-- Modules Involved: Media (tbl_medias) and Catalog (tbl_product_media, tbl_product_variant)
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. INSERT MEDIA ASSETS INTO tbl_medias
-- -----------------------------------------------------------------------------
INSERT INTO tbl_medias (id, name, url, active, size, alt_text, duration, file_type, type, created_at, updated_at)
VALUES 
    ('a0000001-0000-0000-0000-000000000001', 'macbook-air-13-m3.jpg', 
     'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', 
     true, 185420, 'Apple MacBook Air 13-inch M3', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000002', 'macbook-pro-14-m3-pro.jpg', 
     'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&w=800&q=80', 
     true, 224150, 'Apple MacBook Pro 14-inch M3 Pro', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000003', 'macbook-pro-16-m3-max.jpg', 
     'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=800&q=80', 
     true, 260300, 'Apple MacBook Pro 16-inch M3 Max', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000004', 'iphone-15.jpg', 
     'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?auto=format&fit=crop&w=800&q=80', 
     true, 168900, 'Apple iPhone 15', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000005', 'iphone-15-pro-max.jpg', 
     'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=800&q=80', 
     true, 195400, 'Apple iPhone 15 Pro Max', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000006', 'ipad-air-11-m2.jpg', 
     'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80', 
     true, 172800, 'Apple iPad Air 11-inch M2', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000007', 'ipad-pro-13-m4.jpg', 
     'https://images.unsplash.com/photo-1561154464-82e9adf32764?auto=format&fit=crop&w=800&q=80', 
     true, 210600, 'Apple iPad Pro 13-inch M4', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000008', 'surface-laptop-7-13.jpg', 
     'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80', 
     true, 189200, 'Microsoft Surface Laptop 7 13.8-inch', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000009', 'surface-laptop-7-15.jpg', 
     'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80', 
     true, 215400, 'Microsoft Surface Laptop 7 15-inch', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('a0000001-0000-0000-0000-000000000010', 'surface-pro-11.jpg', 
     'https://images.unsplash.com/photo-1587614382346-4ec70e388b28?auto=format&fit=crop&w=800&q=80', 
     true, 198300, 'Microsoft Surface Pro 11 2-in-1 PC', NULL, 'image/jpeg', 'IMAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    url = EXCLUDED.url,
    active = EXCLUDED.active,
    size = EXCLUDED.size,
    alt_text = EXCLUDED.alt_text,
    file_type = EXCLUDED.file_type,
    type = EXCLUDED.type,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 2. UPDATE PRODUCT MEDIA THUMBNAILS WITH VALID UUIDs
-- -----------------------------------------------------------------------------
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000001' WHERE product_id = 1;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000002' WHERE product_id = 2;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000003' WHERE product_id = 3;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000004' WHERE product_id = 4;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000005' WHERE product_id = 5;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000006' WHERE product_id = 6;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000007' WHERE product_id = 7;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000008' WHERE product_id = 8;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000009' WHERE product_id = 9;
UPDATE tbl_product_media SET media_id = 'a0000001-0000-0000-0000-000000000010' WHERE product_id = 10;

-- -----------------------------------------------------------------------------
-- 3. UPDATE PRODUCT VARIANT MEDIA WITH VALID UUIDs
-- -----------------------------------------------------------------------------
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000001' WHERE product_id = 1;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000002' WHERE product_id = 2;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000003' WHERE product_id = 3;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000004' WHERE product_id = 4;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000005' WHERE product_id = 5;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000006' WHERE product_id = 6;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000007' WHERE product_id = 7;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000008' WHERE product_id = 8;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000009' WHERE product_id = 9;
UPDATE tbl_product_variant SET media_id = 'a0000001-0000-0000-0000-000000000010' WHERE product_id = 10;

COMMIT;
