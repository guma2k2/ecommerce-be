-- =============================================================================
-- Database Seed Script: 10 Base Products (MacBook, iPhone, iPad, Surface)
-- Target Database: PostgreSQL (ecommerce_db)
-- Modules Involved: Catalog (Brand, Category, Product, Template, Attribute, Variant, Option)
-- Features: Multi-variant per product, Variant-level searchable attributes (RAM, CPU, Storage, Color, OS)
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. BRANDS
-- -----------------------------------------------------------------------------
INSERT INTO tbl_brand (id, name, description, created_at, updated_at)
VALUES 
    (1, 'Apple', 'Apple Inc. - Innovative designer of MacBooks, iPhones, iPads, and industry-leading silicon chipsets.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Microsoft', 'Microsoft Corporation - Creator of premium Surface computing hardware, Copilot+ PCs, and innovative 2-in-1s.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    description = EXCLUDED.description, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 2. CATEGORIES
-- -----------------------------------------------------------------------------
INSERT INTO tbl_category (id, name, parent_id, created_at, updated_at)
VALUES 
    (1, 'Electronics & Computers', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Laptops', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Smartphones', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Tablets & 2-in-1s', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    parent_id = EXCLUDED.parent_id, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 3. PRODUCT TEMPLATES
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_template (id, name, created_at, updated_at)
VALUES 
    (1, 'Laptop Specification Template', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Smartphone Specification Template', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Tablet & 2-in-1 Specification Template', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 4. PRODUCT ATTRIBUTES (For search filtering and facet aggregation)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_attribute (id, name, created_at, updated_at)
VALUES 
    (1, 'Processor / CPU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'RAM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Storage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Display Size', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Color', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'Operating System', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 5. ATTRIBUTE TEMPLATE MAPPINGS
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_attribute_template (id, product_attribute_id, product_template_id, position, created_at, updated_at)
VALUES 
    -- Laptop template attributes
    (1, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 4, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 5, 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 6, 1, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Smartphone template attributes
    (7, 1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 2, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 3, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 4, 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 5, 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 6, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Tablet template attributes
    (13, 1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 2, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 3, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 4, 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 5, 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 6, 3, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    position = EXCLUDED.position, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 6. PRODUCT OPTIONS (Color, Storage, RAM for variant selection)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option (id, name, created_at, updated_at)
VALUES 
    (1, 'Color', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Storage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'RAM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 7. 10 BASE PRODUCTS (MacBook, iPhone, iPad, Surface)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product (id, name, slug, description, meta_title, meta_keyword, meta_description, category_id, brand_id, created_at, updated_at)
VALUES 
    -- Product 1: MacBook Air 13" M3
    (1, 'MacBook Air 13-inch M3', 'macbook-air-13-inch-m3',
     'Strikingly thin and fast MacBook Air 13 with M3 chip, Liquid Retina display, and up to 18 hours of battery life.',
     'MacBook Air 13 M3 | Apple Laptop', 'Apple, MacBook Air, M3, Laptop, Ultrabook',
     'Buy MacBook Air 13 inch with Apple M3 chip.', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 2: MacBook Pro 14" M3 Pro
    (2, 'MacBook Pro 14-inch M3 Pro', 'macbook-pro-14-inch-m3-pro',
     'Pro laptop powerhouse featuring M3 Pro chip, Liquid Retina XDR display, advanced connectivity, and extreme battery life.',
     'MacBook Pro 14 M3 Pro | Apple Pro Laptop', 'Apple, MacBook Pro, M3 Pro, 14 inch, Pro Laptop',
     'Explore MacBook Pro 14 inch with Apple M3 Pro chip.', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 3: MacBook Pro 16" M3 Max
    (3, 'MacBook Pro 16-inch M3 Max', 'macbook-pro-16-inch-m3-max',
     'The ultimate professional laptop with M3 Max chip, expansive 16.2-inch XDR screen, and extreme computing performance.',
     'MacBook Pro 16 M3 Max | Extreme Performance', 'Apple, MacBook Pro 16, M3 Max, Workstation Laptop',
     'Shop MacBook Pro 16 inch with Apple M3 Max.', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 4: iPhone 15
    (4, 'iPhone 15', 'iphone-15',
     'iPhone 15 with Dynamic Island, 48MP Main camera with 2x Telephoto, durable color-infused glass, and USB-C.',
     'iPhone 15 | Apple Smartphone', 'Apple, iPhone 15, Smartphone, iOS, Dynamic Island',
     'Buy the new iPhone 15 with A16 Bionic.', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 5: iPhone 15 Pro Max
    (5, 'iPhone 15 Pro Max', 'iphone-15-pro-max',
     'Forged in aerospace-grade titanium with ground-breaking A17 Pro chip, customizable Action button, and 5x Telephoto camera.',
     'iPhone 15 Pro Max | Apple Flagship Phone', 'Apple, iPhone 15 Pro Max, A17 Pro, Titanium, Flagship',
     'Experience iPhone 15 Pro Max in durable titanium.', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 6: iPad Air 11-inch M2
    (6, 'iPad Air 11-inch M2', 'ipad-air-11-inch-m2',
     'Freshly redesigned iPad Air 11-inch powered by the astonishingly fast Apple M2 chip and landscape stereo speakers.',
     'iPad Air 11 M2 | Apple Tablet', 'Apple, iPad Air, M2, Tablet, iPadOS',
     'Discover iPad Air 11 inch with M2 chip.', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 7: iPad Pro 13-inch M4
    (7, 'iPad Pro 13-inch M4', 'ipad-pro-13-inch-m4',
     'Impossibly thin iPad Pro 13 featuring the revolutionary Ultra Retina XDR OLED display and next-generation M4 chip.',
     'iPad Pro 13 M4 | Ultra Retina XDR Tablet', 'Apple, iPad Pro, M4, OLED, Pro Tablet',
     'Order iPad Pro 13 inch with Tandem OLED and M4 chip.', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 8: Surface Laptop 7 13.8" Copilot+ PC
    (8, 'Surface Laptop 7 13.8-inch Copilot+ PC', 'surface-laptop-7-13-8-inch-copilot-pc',
     'Next-gen AI Copilot+ PC powered by Snapdragon X processor, PixelSense Flow touchscreen, and all-day battery life.',
     'Surface Laptop 7 13.8 Copilot+ | Microsoft AI Laptop', 'Microsoft, Surface Laptop 7, Snapdragon X, Copilot, Windows 11',
     'Buy Microsoft Surface Laptop 7 13.8 inch Copilot+ PC.', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 9: Surface Laptop 7 15" Copilot+ PC
    (9, 'Surface Laptop 7 15-inch Copilot+ PC', 'surface-laptop-7-15-inch-copilot-pc',
     'Larger 15-inch HDR touchscreen Copilot+ laptop with Snapdragon X Elite, sleek aluminum casing, and studio microphones.',
     'Surface Laptop 7 15 Copilot+ | Microsoft Laptop', 'Microsoft, Surface Laptop 7 15, Snapdragon X Elite, Copilot+ PC',
     'Discover Microsoft Surface Laptop 7 15 inch with Snapdragon X Elite.', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 10: Surface Pro 11 Copilot+ 2-in-1
    (10, 'Surface Pro 11 Copilot+ 2-in-1 PC', 'surface-pro-11-copilot-2-in-1-pc',
     'The ultimate flexible 2-in-1 AI PC with optional OLED display, Snapdragon X performance, and detachable keyboard support.',
     'Surface Pro 11 Copilot+ 2-in-1 | Microsoft Tablet Laptop', 'Microsoft, Surface Pro 11, 2-in-1, Tablet, Copilot+ PC',
     'Experience Microsoft Surface Pro 11 with Snapdragon X processors.', 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    slug = EXCLUDED.slug,
    description = EXCLUDED.description,
    category_id = EXCLUDED.category_id,
    brand_id = EXCLUDED.brand_id,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 8. PRODUCT OPTION COMBINATIONS (Declares which options each product uses)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option_combination (product_id, product_option_id, position)
VALUES 
    -- Product 1 (MacBook Air 13): Color (1), Storage (2), RAM (3)
    (1, 1, 1), (1, 2, 2), (1, 3, 3),
    -- Product 2 (MacBook Pro 14): Color (1), Storage (2), RAM (3)
    (2, 1, 1), (2, 2, 2), (2, 3, 3),
    -- Product 3 (MacBook Pro 16): Color (1), Storage (2), RAM (3)
    (3, 1, 1), (3, 2, 2), (3, 3, 3),
    -- Product 4 (iPhone 15): Color (1), Storage (2)
    (4, 1, 1), (4, 2, 2),
    -- Product 5 (iPhone 15 Pro Max): Color (1), Storage (2)
    (5, 1, 1), (5, 2, 2),
    -- Product 6 (iPad Air 11): Color (1), Storage (2), RAM (3)
    (6, 1, 1), (6, 2, 2), (6, 3, 3),
    -- Product 7 (iPad Pro 13): Color (1), Storage (2), RAM (3)
    (7, 1, 1), (7, 2, 2), (7, 3, 3),
    -- Product 8 (Surface Laptop 7 13.8): Color (1), Storage (2), RAM (3)
    (8, 1, 1), (8, 2, 2), (8, 3, 3),
    -- Product 9 (Surface Laptop 7 15): Color (1), Storage (2), RAM (3)
    (9, 1, 1), (9, 2, 2), (9, 3, 3),
    -- Product 10 (Surface Pro 11): Color (1), Storage (2), RAM (3)
    (10, 1, 1), (10, 2, 2), (10, 3, 3)
ON CONFLICT (product_id, product_option_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 9. PRODUCT OPTION VALUES (Values available per product option)
-- (id, value, position, product_id, product_option_id, created_at, updated_at)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option_value (id, value, position, product_id, product_option_id, created_at, updated_at)
VALUES 
    -- P1: MacBook Air 13
    (1, 'Midnight', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Space Gray', 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Starlight', 3, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '256GB', 1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '512GB', 2, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, '8GB', 1, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, '16GB', 2, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, '24GB', 3, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P2: MacBook Pro 14
    (9, 'Space Black', 1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'Silver', 2, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, '512GB', 1, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, '1TB', 2, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, '18GB', 1, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, '36GB', 2, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P3: MacBook Pro 16
    (15, 'Space Black', 1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 'Silver', 2, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, '1TB', 1, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, '2TB', 2, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, '36GB', 1, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, '48GB', 2, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, '64GB', 3, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P4: iPhone 15
    (22, 'Black', 1, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'Blue', 2, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'Pink', 3, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, '128GB', 1, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, '256GB', 2, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, '512GB', 3, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P5: iPhone 15 Pro Max
    (28, 'Natural Titanium', 1, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'Blue Titanium', 2, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'Black Titanium', 3, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, '256GB', 1, 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, '512GB', 2, 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, '1TB', 3, 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P6: iPad Air 11
    (34, 'Space Gray', 1, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 'Blue', 2, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36, 'Starlight', 3, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, '128GB', 1, 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, '256GB', 2, 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, '512GB', 3, 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, '8GB', 1, 6, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P7: iPad Pro 13
    (41, 'Space Black', 1, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, 'Silver', 2, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (43, '256GB', 1, 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, '512GB', 2, 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, '1TB', 3, 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, '8GB', 1, 7, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, '16GB', 2, 7, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P8: Surface Laptop 7 13.8
    (48, 'Platinum', 1, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (49, 'Black', 2, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, 'Sapphire', 3, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, '256GB', 1, 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, '512GB', 2, 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (53, '1TB', 3, 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, '16GB', 1, 8, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (55, '32GB', 2, 8, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P9: Surface Laptop 7 15
    (56, 'Platinum', 1, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (57, 'Black', 2, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (58, '512GB', 1, 9, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (59, '1TB', 2, 9, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (60, '16GB', 1, 9, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (61, '32GB', 2, 9, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (62, '64GB', 3, 9, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P10: Surface Pro 11
    (63, 'Platinum', 1, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (64, 'Sapphire', 2, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (65, 'Black', 3, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (66, '256GB', 1, 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (67, '512GB', 2, 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (68, '1TB', 3, 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (69, '16GB', 1, 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (70, '32GB', 2, 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    value = EXCLUDED.value,
    position = EXCLUDED.position,
    product_id = EXCLUDED.product_id,
    product_option_id = EXCLUDED.product_option_id,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 10. PRODUCT VARIANTS (3 variants per product = 30 total variants)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_variant (id, title, sku, price, quantity, status, media_id, product_id, created_at, updated_at)
VALUES 
    -- Product 1: MacBook Air 13" M3
    (1, 'MacBook Air 13" (Midnight, 8GB, 256GB)', 'MBA-13-M3-MDN-8-256', 1099.00, 45, 'ACTIVE', 'media-mba13-1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'MacBook Air 13" (Space Gray, 16GB, 512GB)', 'MBA-13-M3-SGY-16-512', 1499.00, 30, 'ACTIVE', 'media-mba13-2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'MacBook Air 13" (Starlight, 24GB, 512GB)', 'MBA-13-M3-STL-24-512', 1699.00, 18, 'ACTIVE', 'media-mba13-3', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 2: MacBook Pro 14" M3 Pro
    (4, 'MacBook Pro 14" (Space Black, 18GB, 512GB)', 'MBP-14-M3P-SBK-18-512', 1999.00, 40, 'ACTIVE', 'media-mbp14-1', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'MacBook Pro 14" (Silver, 18GB, 1TB)', 'MBP-14-M3P-SLV-18-1TB', 2399.00, 25, 'ACTIVE', 'media-mbp14-2', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'MacBook Pro 14" (Space Black, 36GB, 1TB)', 'MBP-14-M3P-SBK-36-1TB', 2799.00, 15, 'ACTIVE', 'media-mbp14-3', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 3: MacBook Pro 16" M3 Max
    (7, 'MacBook Pro 16" (Space Black, 36GB, 1TB)', 'MBP-16-M3M-SBK-36-1TB', 3499.00, 20, 'ACTIVE', 'media-mbp16-1', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'MacBook Pro 16" (Silver, 48GB, 1TB)', 'MBP-16-M3M-SLV-48-1TB', 3999.00, 12, 'ACTIVE', 'media-mbp16-2', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'MacBook Pro 16" (Space Black, 64GB, 2TB)', 'MBP-16-M3M-SBK-64-2TB', 4899.00, 8, 'ACTIVE', 'media-mbp16-3', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 4: iPhone 15
    (10, 'iPhone 15 (Black, 128GB)', 'IPH-15-BLK-128', 799.00, 100, 'ACTIVE', 'media-iph15-1', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'iPhone 15 (Blue, 256GB)', 'IPH-15-BLU-256', 899.00, 85, 'ACTIVE', 'media-iph15-2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'iPhone 15 (Pink, 512GB)', 'IPH-15-PNK-512', 1099.00, 50, 'ACTIVE', 'media-iph15-3', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 5: iPhone 15 Pro Max
    (13, 'iPhone 15 Pro Max (Natural Titanium, 256GB)', 'IPH-15PM-NAT-256', 1199.00, 60, 'ACTIVE', 'media-iph15pm-1', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'iPhone 15 Pro Max (Blue Titanium, 512GB)', 'IPH-15PM-BLU-512', 1399.00, 45, 'ACTIVE', 'media-iph15pm-2', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'iPhone 15 Pro Max (Black Titanium, 1TB)', 'IPH-15PM-BLK-1TB', 1599.00, 25, 'ACTIVE', 'media-iph15pm-3', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 6: iPad Air 11" M2
    (16, 'iPad Air 11" (Space Gray, 128GB, 8GB)', 'IPA-11-M2-SGY-128', 599.00, 70, 'ACTIVE', 'media-ipa11-1', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'iPad Air 11" (Blue, 256GB, 8GB)', 'IPA-11-M2-BLU-256', 699.00, 55, 'ACTIVE', 'media-ipa11-2', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'iPad Air 11" (Starlight, 512GB, 8GB)', 'IPA-11-M2-STL-512', 899.00, 30, 'ACTIVE', 'media-ipa11-3', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 7: iPad Pro 13" M4
    (19, 'iPad Pro 13" (Space Black, 256GB, 8GB)', 'IPP-13-M4-SBK-256', 1299.00, 35, 'ACTIVE', 'media-ipp13-1', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 'iPad Pro 13" (Silver, 512GB, 8GB)', 'IPP-13-M4-SLV-512', 1499.00, 25, 'ACTIVE', 'media-ipp13-2', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'iPad Pro 13" (Space Black, 1TB, 16GB)', 'IPP-13-M4-SBK-1TB', 1899.00, 15, 'ACTIVE', 'media-ipp13-3', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 8: Surface Laptop 7 13.8" Copilot+ PC
    (22, 'Surface Laptop 7 13.8" (Platinum, 16GB, 256GB)', 'SL7-13-PLT-16-256', 999.00, 50, 'ACTIVE', 'media-sl713-1', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'Surface Laptop 7 13.8" (Black, 16GB, 512GB)', 'SL7-13-BLK-16-512', 1199.00, 40, 'ACTIVE', 'media-sl713-2', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'Surface Laptop 7 13.8" (Sapphire, 32GB, 1TB)', 'SL7-13-SPH-32-1TB', 1599.00, 20, 'ACTIVE', 'media-sl713-3', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 9: Surface Laptop 7 15" Copilot+ PC
    (25, 'Surface Laptop 7 15" (Platinum, 16GB, 512GB)', 'SL7-15-PLT-16-512', 1299.00, 35, 'ACTIVE', 'media-sl715-1', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'Surface Laptop 7 15" (Black, 32GB, 1TB)', 'SL7-15-BLK-32-1TB', 1799.00, 25, 'ACTIVE', 'media-sl715-2', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'Surface Laptop 7 15" (Black, 64GB, 1TB)', 'SL7-15-BLK-64-1TB', 2199.00, 10, 'ACTIVE', 'media-sl715-3', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 10: Surface Pro 11 Copilot+ 2-in-1
    (28, 'Surface Pro 11 (Platinum, 16GB, 256GB)', 'SP11-PLT-16-256', 999.00, 45, 'ACTIVE', 'media-sp11-1', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 'Surface Pro 11 (Sapphire, 16GB, 512GB)', 'SP11-SPH-16-512', 1499.00, 30, 'ACTIVE', 'media-sp11-2', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 'Surface Pro 11 (Black, 32GB, 1TB)', 'SP11-BLK-32-1TB', 1999.00, 20, 'ACTIVE', 'media-sp11-3', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    title = EXCLUDED.title,
    sku = EXCLUDED.sku,
    price = EXCLUDED.price,
    quantity = EXCLUDED.quantity,
    status = EXCLUDED.status,
    media_id = EXCLUDED.media_id,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 11. VARIANT OPTION VALUES (Maps Variant -> Selected Option Values)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_variant_option_value (id, product_variant_id, option_value_id, created_at, updated_at)
VALUES 
    -- V1: MBA 13 (Midnight, 8GB, 256GB)
    (1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V2: MBA 13 (Space Gray, 16GB, 512GB)
    (4, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 2, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V3: MBA 13 (Starlight, 24GB, 512GB)
    (7, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V4: MBP 14 (Space Black, 18GB, 512GB)
    (10, 4, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 4, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 4, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V5: MBP 14 (Silver, 18GB, 1TB)
    (13, 5, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 5, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V6: MBP 14 (Space Black, 36GB, 1TB)
    (16, 6, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 6, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 6, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V7: MBP 16 (Space Black, 36GB, 1TB)
    (19, 7, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 7, 17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 7, 19, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V8: MBP 16 (Silver, 48GB, 1TB)
    (22, 8, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 8, 17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 8, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V9: MBP 16 (Space Black, 64GB, 2TB)
    (25, 9, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 9, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 9, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V10: iPhone 15 (Black, 128GB)
    (28, 10, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 10, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V11: iPhone 15 (Blue, 256GB)
    (30, 11, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 11, 26, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V12: iPhone 15 (Pink, 512GB)
    (32, 12, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, 12, 27, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V13: iPhone 15 PM (Natural Titanium, 256GB)
    (34, 13, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 13, 31, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V14: iPhone 15 PM (Blue Titanium, 512GB)
    (36, 14, 29, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, 14, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V15: iPhone 15 PM (Black Titanium, 1TB)
    (38, 15, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, 15, 33, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V16: iPad Air 11 (Space Gray, 128GB, 8GB)
    (40, 16, 34, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, 16, 37, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, 16, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V17: iPad Air 11 (Blue, 256GB, 8GB)
    (43, 17, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, 17, 38, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, 17, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V18: iPad Air 11 (Starlight, 512GB, 8GB)
    (46, 18, 36, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, 18, 39, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (48, 18, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V19: iPad Pro 13 (Space Black, 256GB, 8GB)
    (49, 19, 41, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, 19, 43, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, 19, 46, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V20: iPad Pro 13 (Silver, 512GB, 8GB)
    (52, 20, 42, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (53, 20, 44, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, 20, 46, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V21: iPad Pro 13 (Space Black, 1TB, 16GB)
    (55, 21, 41, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (56, 21, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (57, 21, 47, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V22: Surface Laptop 7 13.8 (Platinum, 16GB, 256GB)
    (58, 22, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (59, 22, 51, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (60, 22, 54, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V23: Surface Laptop 7 13.8 (Black, 16GB, 512GB)
    (61, 23, 49, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (62, 23, 52, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (63, 23, 54, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V24: Surface Laptop 7 13.8 (Sapphire, 32GB, 1TB)
    (64, 24, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (65, 24, 53, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (66, 24, 55, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V25: Surface Laptop 7 15 (Platinum, 16GB, 512GB)
    (67, 25, 56, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (68, 25, 58, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (69, 25, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V26: Surface Laptop 7 15 (Black, 32GB, 1TB)
    (70, 26, 57, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (71, 26, 59, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (72, 26, 61, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V27: Surface Laptop 7 15 (Black, 64GB, 1TB)
    (73, 27, 57, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (74, 27, 59, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (75, 27, 62, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- V28: Surface Pro 11 (Platinum, 16GB, 256GB)
    (76, 28, 63, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (77, 28, 66, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (78, 28, 69, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V29: Surface Pro 11 (Sapphire, 16GB, 512GB)
    (79, 29, 64, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (80, 29, 67, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (81, 29, 69, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- V30: Surface Pro 11 (Black, 32GB, 1TB)
    (82, 30, 65, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (83, 30, 68, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (84, 30, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (product_variant_id, option_value_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 12. PRODUCT ATTRIBUTE VALUES (Product-level general attributes)
-- (id, product_attribute_id, product_id, value, created_at, updated_at)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_attribute_value (id, product_attribute_id, product_id, value, created_at, updated_at)
VALUES 
    -- P1: MacBook Air 13
    (1, 1, 1, 'Apple M3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 4, 1, '13.6 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 6, 1, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P2: MacBook Pro 14
    (4, 1, 2, 'Apple M3 Pro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 4, 2, '14.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 6, 2, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P3: MacBook Pro 16
    (7, 1, 3, 'Apple M3 Max', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 4, 3, '16.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 6, 3, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P4: iPhone 15
    (10, 1, 4, 'Apple A16 Bionic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 4, 4, '6.1 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 6, 4, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P5: iPhone 15 Pro Max
    (13, 1, 5, 'Apple A17 Pro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 4, 5, '6.7 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 6, 5, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P6: iPad Air 11
    (16, 1, 6, 'Apple M2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 4, 6, '11 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 6, 6, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P7: iPad Pro 13
    (19, 1, 7, 'Apple M4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 4, 7, '13 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 6, 7, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P8: Surface Laptop 7 13.8
    (22, 1, 8, 'Snapdragon X Plus / Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 4, 8, '13.8 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 6, 8, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P9: Surface Laptop 7 15
    (25, 1, 9, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 4, 9, '15.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 6, 9, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- P10: Surface Pro 11
    (28, 1, 10, 'Snapdragon X Plus / Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 4, 10, '13.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 6, 10, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    value = EXCLUDED.value,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 13. VARIANT ATTRIBUTE VALUES (Searchable attributes per Variant: RAM, CPU, Storage, etc.)
-- Attributes: 1=CPU, 2=RAM, 3=Storage, 4=Display Size, 5=Color, 6=Operating System
-- (id, product_attribute_id, variant_id, value, created_at, updated_at)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_variant_attribute_value (id, product_attribute_id, variant_id, value, created_at, updated_at)
VALUES 
    -- Variant 1: MBA 13 (Midnight, 8GB, 256GB)
    (1, 1, 1, 'Apple M3 (8-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 1, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, 1, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 4, 1, '13.6 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 5, 1, 'Midnight', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 6, 1, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 2: MBA 13 (Space Gray, 16GB, 512GB)
    (7, 1, 2, 'Apple M3 (8-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 2, 2, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 3, 2, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 4, 2, '13.6 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 5, 2, 'Space Gray', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 6, 2, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 3: MBA 13 (Starlight, 24GB, 512GB)
    (13, 1, 3, 'Apple M3 (8-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 2, 3, '24GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 3, 3, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 4, 3, '13.6 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 5, 3, 'Starlight', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 6, 3, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 4: MBP 14 (Space Black, 18GB, 512GB)
    (19, 1, 4, 'Apple M3 Pro (11-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 2, 4, '18GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 3, 4, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 4, 4, '14.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 5, 4, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 6, 4, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 5: MBP 14 (Silver, 18GB, 1TB)
    (25, 1, 5, 'Apple M3 Pro (12-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 2, 5, '18GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 3, 5, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 4, 5, '14.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 5, 5, 'Silver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 6, 5, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 6: MBP 14 (Space Black, 36GB, 1TB)
    (31, 1, 6, 'Apple M3 Pro (12-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 2, 6, '36GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, 3, 6, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (34, 4, 6, '14.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 5, 6, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36, 6, 6, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 7: MBP 16 (Space Black, 36GB, 1TB)
    (37, 1, 7, 'Apple M3 Max (14-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, 2, 7, '36GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, 3, 7, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, 4, 7, '16.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, 5, 7, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, 6, 7, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 8: MBP 16 (Silver, 48GB, 1TB)
    (43, 1, 8, 'Apple M3 Max (16-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, 2, 8, '48GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, 3, 8, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, 4, 8, '16.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, 5, 8, 'Silver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (48, 6, 8, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 9: MBP 16 (Space Black, 64GB, 2TB)
    (49, 1, 9, 'Apple M3 Max (16-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, 2, 9, '64GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, 3, 9, '2TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, 4, 9, '16.2 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (53, 5, 9, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, 6, 9, 'macOS Sonoma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 10: iPhone 15 (Black, 128GB)
    (55, 1, 10, 'Apple A16 Bionic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (56, 2, 10, '6GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (57, 3, 10, '128GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (58, 4, 10, '6.1 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (59, 5, 10, 'Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (60, 6, 10, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 11: iPhone 15 (Blue, 256GB)
    (61, 1, 11, 'Apple A16 Bionic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (62, 2, 11, '6GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (63, 3, 11, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (64, 4, 11, '6.1 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (65, 5, 11, 'Blue', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (66, 6, 11, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 12: iPhone 15 (Pink, 512GB)
    (67, 1, 12, 'Apple A16 Bionic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (68, 2, 12, '6GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (69, 3, 12, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (70, 4, 12, '6.1 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (71, 5, 12, 'Pink', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (72, 6, 12, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 13: iPhone 15 PM (Natural Titanium, 256GB)
    (73, 1, 13, 'Apple A17 Pro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (74, 2, 13, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (75, 3, 13, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (76, 4, 13, '6.7 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (77, 5, 13, 'Natural Titanium', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (78, 6, 13, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 14: iPhone 15 PM (Blue Titanium, 512GB)
    (79, 1, 14, 'Apple A17 Pro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (80, 2, 14, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (81, 3, 14, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (82, 4, 14, '6.7 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (83, 5, 14, 'Blue Titanium', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (84, 6, 14, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 15: iPhone 15 PM (Black Titanium, 1TB)
    (85, 1, 15, 'Apple A17 Pro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (86, 2, 15, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (87, 3, 15, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (88, 4, 15, '6.7 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (89, 5, 15, 'Black Titanium', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (90, 6, 15, 'iOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 16: iPad Air 11 (Space Gray, 128GB, 8GB)
    (91, 1, 16, 'Apple M2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (92, 2, 16, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (93, 3, 16, '128GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (94, 4, 16, '11 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (95, 5, 16, 'Space Gray', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (96, 6, 16, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 17: iPad Air 11 (Blue, 256GB, 8GB)
    (97, 1, 17, 'Apple M2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (98, 2, 17, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (99, 3, 17, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (100, 4, 17, '11 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (101, 5, 17, 'Blue', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (102, 6, 17, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 18: iPad Air 11 (Starlight, 512GB, 8GB)
    (103, 1, 18, 'Apple M2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (104, 2, 18, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (105, 3, 18, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (106, 4, 18, '11 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (107, 5, 18, 'Starlight', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (108, 6, 18, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 19: iPad Pro 13 (Space Black, 256GB, 8GB)
    (109, 1, 19, 'Apple M4 (9-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (110, 2, 19, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (111, 3, 19, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (112, 4, 19, '13 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (113, 5, 19, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (114, 6, 19, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 20: iPad Pro 13 (Silver, 512GB, 8GB)
    (115, 1, 20, 'Apple M4 (9-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (116, 2, 20, '8GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (117, 3, 20, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (118, 4, 20, '13 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (119, 5, 20, 'Silver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (120, 6, 20, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 21: iPad Pro 13 (Space Black, 1TB, 16GB)
    (121, 1, 21, 'Apple M4 (10-core CPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (122, 2, 21, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (123, 3, 21, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (124, 4, 21, '13 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (125, 5, 21, 'Space Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (126, 6, 21, 'iPadOS 17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 22: Surface Laptop 7 13.8 (Platinum, 16GB, 256GB)
    (127, 1, 22, 'Snapdragon X Plus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (128, 2, 22, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (129, 3, 22, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (130, 4, 22, '13.8 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (131, 5, 22, 'Platinum', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (132, 6, 22, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 23: Surface Laptop 7 13.8 (Black, 16GB, 512GB)
    (133, 1, 23, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (134, 2, 23, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (135, 3, 23, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (136, 4, 23, '13.8 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (137, 5, 23, 'Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (138, 6, 23, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 24: Surface Laptop 7 13.8 (Sapphire, 32GB, 1TB)
    (139, 1, 24, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (140, 2, 24, '32GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (141, 3, 24, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (142, 4, 24, '13.8 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (143, 5, 24, 'Sapphire', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (144, 6, 24, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 25: Surface Laptop 7 15 (Platinum, 16GB, 512GB)
    (145, 1, 25, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (146, 2, 25, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (147, 3, 25, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (148, 4, 25, '15.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (149, 5, 25, 'Platinum', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (150, 6, 25, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 26: Surface Laptop 7 15 (Black, 32GB, 1TB)
    (151, 1, 26, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (152, 2, 26, '32GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (153, 3, 26, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (154, 4, 26, '15.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (155, 5, 26, 'Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (156, 6, 26, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 27: Surface Laptop 7 15 (Black, 64GB, 1TB)
    (157, 1, 27, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (158, 2, 27, '64GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (159, 3, 27, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (160, 4, 27, '15.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (161, 5, 27, 'Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (162, 6, 27, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 28: Surface Pro 11 (Platinum, 16GB, 256GB)
    (163, 1, 28, 'Snapdragon X Plus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (164, 2, 28, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (165, 3, 28, '256GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (166, 4, 28, '13.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (167, 5, 28, 'Platinum', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (168, 6, 28, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 29: Surface Pro 11 (Sapphire, 16GB, 512GB)
    (169, 1, 29, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (170, 2, 29, '16GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (171, 3, 29, '512GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (172, 4, 29, '13.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (173, 5, 29, 'Sapphire', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (174, 6, 29, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 30: Surface Pro 11 (Black, 32GB, 1TB)
    (175, 1, 30, 'Snapdragon X Elite', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (176, 2, 30, '32GB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (177, 3, 30, '1TB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (178, 4, 30, '13.0 inch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (179, 5, 30, 'Black', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (180, 6, 30, 'Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    value = EXCLUDED.value,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 14. MEDIA ASSETS (tbl_medias - Real UUIDs and Image URLs)
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
-- 15. PRODUCT MEDIA (Primary thumbnails for search display)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_media (id, product_id, media_id, position, created_at, updated_at)
VALUES 
    (1, 1, 'a0000001-0000-0000-0000-000000000001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 'a0000001-0000-0000-0000-000000000002', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, 'a0000001-0000-0000-0000-000000000003', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 4, 'a0000001-0000-0000-0000-000000000004', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 5, 'a0000001-0000-0000-0000-000000000005', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 6, 'a0000001-0000-0000-0000-000000000006', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 7, 'a0000001-0000-0000-0000-000000000007', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 8, 'a0000001-0000-0000-0000-000000000008', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 9, 'a0000001-0000-0000-0000-000000000009', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 10, 'a0000001-0000-0000-0000-000000000010', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    media_id = EXCLUDED.media_id,
    position = EXCLUDED.position,
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 16. RE-SYNC POSTGRESQL SEQUENCES
-- -----------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('tbl_brand', 'id'), COALESCE((SELECT MAX(id) FROM tbl_brand), 1));
SELECT setval(pg_get_serial_sequence('tbl_category', 'id'), COALESCE((SELECT MAX(id) FROM tbl_category), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_template', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_template), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_attribute', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_attribute), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_attribute_template', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_attribute_template), 1));
SELECT setval(pg_get_serial_sequence('tbl_product', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_attribute_value', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_attribute_value), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_option', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_option), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_option_value', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_option_value), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_variant', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_variant), 1));
SELECT setval(pg_get_serial_sequence('tbl_variant_option_value', 'id'), COALESCE((SELECT MAX(id) FROM tbl_variant_option_value), 1));
SELECT setval(pg_get_serial_sequence('tbl_variant_attribute_value', 'id'), COALESCE((SELECT MAX(id) FROM tbl_variant_attribute_value), 1));
SELECT setval(pg_get_serial_sequence('tbl_product_media', 'id'), COALESCE((SELECT MAX(id) FROM tbl_product_media), 1));

COMMIT;
