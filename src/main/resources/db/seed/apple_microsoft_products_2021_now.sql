-- =============================================================================
-- Database Seed Script: Apple & Microsoft Products (2021 - Present)
-- Target Database: PostgreSQL (ecommerce_db)
-- Modules Involved: Catalog (Brand, Category, Product, Template, Attribute, Variant, Option)
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. BRANDS
-- -----------------------------------------------------------------------------
INSERT INTO tbl_brand (id, name, description, created_at, updated_at)
VALUES 
    (1, 'Apple', 'Apple Inc. - Innovative designer of MacBooks, iPhones, iPads, and industry-leading silicon chipsets.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Microsoft', 'Microsoft Corporation - Creator of premium Surface computing hardware, Copilot+ PCs, and Xbox gaming systems.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
    (4, 'Tablets & 2-in-1s', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Gaming Consoles', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
    (3, 'Tablet & 2-in-1 Specification Template', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Gaming Console Specification Template', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 4. PRODUCT ATTRIBUTES
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_attribute (id, name, created_at, updated_at)
VALUES 
    (1, 'Release Year', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Processor / Chipset', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Display Specification', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Operating System', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Wireless & Connectivity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'Battery & Power', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'Dimensions & Weight', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
    (7, 7, 1, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Smartphone template attributes
    (8, 1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 2, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 3, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 4, 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 5, 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 6, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Tablet & 2-in-1 template attributes
    (14, 1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 2, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 3, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 4, 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 5, 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 7, 3, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Console template attributes
    (20, 1, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 2, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 5, 4, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    position = EXCLUDED.position, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 6. PRODUCT OPTIONS (Variant Dimensions)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option (id, name, created_at, updated_at)
VALUES 
    (1, 'Color', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Storage Capacity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Unified Memory / RAM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 7. PRODUCTS (Apple & Microsoft: 2021 to Present)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product (id, name, slug, description, meta_title, meta_keyword, meta_description, category_id, brand_id, created_at, updated_at)
VALUES 
    -- 1. MacBook Pro 14" (2021)
    (1, 
     'MacBook Pro 14" (2021) M1 Pro', 
     'macbook-pro-14-2021-m1-pro', 
     'Apple MacBook Pro 14-inch (2021) powered by the groundbreaking Apple M1 Pro chip, featuring Liquid Retina XDR display with ProMotion 120Hz, MagSafe 3 charging, HDMI port, and SD card reader.',
     'MacBook Pro 14" (2021) M1 Pro | Apple',
     'apple, macbook pro 14, m1 pro, 2021, liquid retina xdr',
     'Discover Apple MacBook Pro 14-inch (2021) with M1 Pro chip, 120Hz Liquid Retina XDR screen and all-day battery life.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 2. MacBook Pro 16" (2021)
    (2, 
     'MacBook Pro 16" (2021) M1 Max', 
     'macbook-pro-16-2021-m1-max', 
     'Apple flagship workstation laptop with 16.2-inch Liquid Retina XDR display, up to 10-core CPU and 32-core GPU M1 Max silicon, delivering extreme performance for pro creative workflows.',
     'MacBook Pro 16" (2021) M1 Max | Apple',
     'apple, macbook pro 16, m1 max, 2021, professional creator',
     'Experience pro performance with the Apple MacBook Pro 16-inch (2021) featuring M1 Max chip and Liquid Retina XDR display.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 3. MacBook Air 13" (2022)
    (3, 
     'MacBook Air 13" (2022) M2', 
     'macbook-air-13-2022-m2', 
     'Redesigned Apple MacBook Air with strikingly thin aluminum enclosure, vibrant 13.6-inch Liquid Retina display, 1080p FaceTime HD camera, and the power-efficient M2 processor.',
     'MacBook Air 13" (2022) M2 | Apple',
     'apple, macbook air 13, m2, 2022, midnight, ultra-thin laptop',
     'Ultra-portable and redesigned: Apple MacBook Air 13-inch (2022) with M2 chip, MagSafe, and silent fanless operation.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 4. MacBook Pro 14" (2023)
    (4, 
     'MacBook Pro 14" (2023) M2 Pro', 
     'macbook-pro-14-2023-m2-pro', 
     'Apple MacBook Pro 14-inch (Early 2023) powered by M2 Pro, boasting up to 12-core CPU, 19-core GPU, Wi-Fi 6E, HDMI 2.1 supporting 8K displays, and up to 18 hours of battery life.',
     'MacBook Pro 14" (2023) M2 Pro | Apple',
     'apple, macbook pro 14, m2 pro, 2023, 8k hdmi, wifi 6e',
     'Supercharged by M2 Pro: Apple MacBook Pro 14-inch (2023) with pro connectivity, Wi-Fi 6E and Liquid Retina XDR.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 5. MacBook Air 15" (2023)
    (5, 
     'MacBook Air 15" (2023) M2', 
     'macbook-air-15-2023-m2', 
     'The worlds thinnest 15-inch laptop featuring an expansive 15.3-inch Liquid Retina display, six-speaker sound system with Spatial Audio, and up to 18 hours of battery with M2 efficiency.',
     'MacBook Air 15" (2023) M2 | Apple',
     'apple, macbook air 15, m2, 2023, large screen, light laptop',
     'Apple MacBook Air 15-inch (2023) brings a big display and sleek portability with the Apple M2 chip.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 6. MacBook Pro 14" (Late 2023)
    (6, 
     'MacBook Pro 14" (Late 2023) M3 Pro', 
     'macbook-pro-14-late-2023-m3-pro', 
     'Next-generation 3nm Apple M3 Pro chip featuring hardware-accelerated ray tracing, Dynamic Caching, Space Black finish, and stunning Liquid Retina XDR display.',
     'MacBook Pro 14" (Late 2023) M3 Pro | Apple',
     'apple, macbook pro 14, m3 pro, space black, ray tracing, 2023',
     'Explore MacBook Pro 14-inch (Late 2023) equipped with 3-nanometer M3 Pro chip and sleek Space Black enclosure.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 7. MacBook Air 13" (2024)
    (7, 
     'MacBook Air 13" (2024) M3', 
     'macbook-air-13-2024-m3', 
     'The 2024 MacBook Air with M3 chip delivers up to 60% faster speed, dual external display support with lid closed, Wi-Fi 6E, and enhanced voice isolation in meetings.',
     'MacBook Air 13" (2024) M3 | Apple',
     'apple, macbook air 13, m3, 2024, dual display, ai laptop',
     'Power and portability elevated: Apple MacBook Air 13-inch (2024) with M3 processor and dual monitor support.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 8. MacBook Pro 16" (2024)
    (8, 
     'MacBook Pro 16" (2024) M4 Pro', 
     'macbook-pro-16-2024-m4-pro', 
     'State-of-the-art pro laptop featuring M4 Pro silicon, Thunderbolt 5 speeds up to 120 Gb/s, nano-texture display option, and integrated Apple Intelligence capabilities.',
     'MacBook Pro 16" (2024) M4 Pro | Apple',
     'apple, macbook pro 16, m4 pro, thunderbolt 5, apple intelligence',
     'Ultimate pro workstation: Apple MacBook Pro 16-inch (2024) with M4 Pro, Thunderbolt 5, and Apple Intelligence.',
     2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 9. iPhone 13 (2021)
    (9, 
     'iPhone 13 (2021)', 
     'iphone-13-2021', 
     'Apple iPhone 13 featuring durable Ceramic Shield front, Super Retina XDR OLED display, A15 Bionic chip, Cinematic mode in 1080p 30 fps, and advanced dual-camera system with Sensor-shift OIS.',
     'iPhone 13 (2021) 5G Smartphone | Apple',
     'apple, iphone 13, 2021, a15 bionic, ceramic shield, oled',
     'Shop iPhone 13 (2021) with A15 Bionic chip, dual camera system with Cinematic mode, and long battery life.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 10. iPhone 13 Pro (2021)
    (10, 
     'iPhone 13 Pro (2021)', 
     'iphone-13-pro-2021', 
     'Pro grade smartphone with 120Hz ProMotion Super Retina XDR display, triple lens system with 3x optical zoom, Macro photography, ProRes video recording, and surgical-grade stainless steel.',
     'iPhone 13 Pro (2021) 120Hz | Apple',
     'apple, iphone 13 pro, promotion 120hz, prores, macro camera',
     'Explore iPhone 13 Pro with 120Hz ProMotion display, macro photography, and A15 Bionic 5-core GPU.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 11. iPhone 14 (2022)
    (11, 
     'iPhone 14 (2022)', 
     'iphone-14-2022', 
     'Apple iPhone 14 features Emergency SOS via satellite, Crash Detection, Photonic Engine for low-light photography, Action mode for smooth handheld video, and all-day battery life.',
     'iPhone 14 (2022) Smartphone | Apple',
     'apple, iphone 14, 2022, satellite sos, crash detection, photonic engine',
     'Get iPhone 14 with safety features like Crash Detection and Emergency SOS via satellite, plus enhanced dual cameras.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 12. iPhone 14 Pro Max (2022)
    (12, 
     'iPhone 14 Pro Max (2022)', 
     'iphone-14-pro-max-2022', 
     'Introducing the Dynamic Island, Always-On display, 48MP Main camera with quad-pixel sensor, and the powerful A16 Bionic chipset encased in aerospace-grade stainless steel.',
     'iPhone 14 Pro Max (2022) | Apple',
     'apple, iphone 14 pro max, dynamic island, 48mp camera, a16 bionic',
     'Discover iPhone 14 Pro Max featuring Dynamic Island, Always-On 6.7-inch display, and 48MP camera system.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 13. iPhone 15 (2023)
    (13, 
     'iPhone 15 (2023)', 
     'iphone-15-2023', 
     'Featuring Dynamic Island on all models, universal USB-C charging, 48MP main camera with 2x telephoto optical zoom, color-infused back glass, and contoured edge aluminum design.',
     'iPhone 15 (2023) USB-C | Apple',
     'apple, iphone 15, 2023, usb-c, dynamic island, 48mp, color-infused',
     'Apple iPhone 15 with USB-C connector, Dynamic Island, 48MP camera, and color-infused matte glass design.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 14. iPhone 15 Pro Max (2023)
    (14, 
     'iPhone 15 Pro Max (2023)', 
     'iphone-15-pro-max-2023', 
     'Crafted with aerospace-grade Titanium, customizable Action Button, 5x optical zoom tetraprism telephoto camera, USB 3 transfer speeds, and console-grade gaming with A17 Pro silicon.',
     'iPhone 15 Pro Max (2023) Titanium | Apple',
     'apple, iphone 15 pro max, titanium, a17 pro, 5x telephoto, action button',
     'Shop iPhone 15 Pro Max: strong and lightweight titanium frame, 5x zoom camera, Action button, and A17 Pro chip.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 15. iPhone 16 (2024)
    (15, 
     'iPhone 16 (2024)', 
     'iphone-16-2024', 
     'Engineered for Apple Intelligence with the all-new A18 chip, tactile Camera Control touch sensor, Action Button, Spatial Audio capture, and ultra-vibrant saturated color palette.',
     'iPhone 16 (2024) Apple Intelligence | Apple',
     'apple, iphone 16, 2024, a18, camera control, apple intelligence',
     'Meet iPhone 16 with dedicated Camera Control button, powerful A18 processor, and built-in Apple Intelligence.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 16. iPhone 16 Pro Max (2024)
    (16, 
     'iPhone 16 Pro Max (2024)', 
     'iphone-16-pro-max-2024', 
     'The ultimate iPhone with largest ever 6.9-inch borderless Super Retina XDR display, A18 Pro silicon, 4K 120 fps Dolby Vision recording, Camera Control, and grade 5 titanium chassis.',
     'iPhone 16 Pro Max (2024) Flagship | Apple',
     'apple, iphone 16 pro max, 6.9 inch, a18 pro, 4k 120fps, desert titanium',
     'Experience iPhone 16 Pro Max with huge 6.9-inch display, A18 Pro chip, 4K 120 fps video, and Camera Control.',
     3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 17. Microsoft Surface Laptop 4 (2021)
    (17, 
     'Surface Laptop 4 13.5" (2021)', 
     'surface-laptop-4-13-5-2021', 
     'Microsoft Surface Laptop 4 delivers style and speed with 11th Gen Intel Core i7 processors, vibrant 3:2 PixelSense touchscreen, Omnisonic speakers with Dolby Atmos, and typing comfort.',
     'Surface Laptop 4 (2021) | Microsoft',
     'microsoft, surface laptop 4, 2021, intel core i7, pixelsense',
     'Sleek and versatile: Microsoft Surface Laptop 4 with 13.5-inch PixelSense touchscreen and all-day battery.',
     2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 18. Microsoft Surface Pro 8 (2021)
    (18, 
     'Surface Pro 8 (2021)', 
     'surface-pro-8-2021', 
     'The iconic 2-in-1 PC reimagined with 13-inch 120Hz PixelSense Flow touch display, Thunderbolt 4 connectivity, Surface Slim Pen 2 haptic stylus support, and Intel Evo certification.',
     'Surface Pro 8 2-in-1 Tablet | Microsoft',
     'microsoft, surface pro 8, 2021, 2-in-1 tablet, thunderbolt 4, 120hz',
     'Unlock versatility with Surface Pro 8 2-in-1 PC featuring 120Hz PixelSense Flow touchscreen and Intel Evo platform.',
     4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 19. Microsoft Surface Laptop Studio (2021)
    (19, 
     'Surface Laptop Studio (2021)', 
     'surface-laptop-studio-2021', 
     'Innovative transforming laptop with dynamic woven hinge transitioning smoothly from standard laptop, stage mode for presenting, to a flat studio canvas with NVIDIA RTX 3050 Ti graphics.',
     'Surface Laptop Studio (2021) | Microsoft',
     'microsoft, surface laptop studio, rtx 3050 ti, woven hinge, creator',
     'Boundary-pushing design: Microsoft Surface Laptop Studio transitions between laptop, stage, and studio easel modes.',
     2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 20. Microsoft Surface Laptop 5 (2022)
    (20, 
     'Surface Laptop 5 13.5" (2022)', 
     'surface-laptop-5-13-5-2022', 
     'Fast, lightweight touchscreen laptop built on Intel Evo 12th Gen processors with Thunderbolt 4, long battery life, premium aluminum finish, and Windows 11 biometric authentication.',
     'Surface Laptop 5 (2022) | Microsoft',
     'microsoft, surface laptop 5, 2022, intel evo 12th gen, thunderbolt 4',
     'Sleek computing with Microsoft Surface Laptop 5 featuring 12th Gen Intel Core and responsive PixelSense touch.',
     2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 21. Microsoft Surface Pro 9 (2022)
    (21, 
     'Surface Pro 9 (2022)', 
     'surface-pro-9-2022', 
     'Flexible 2-in-1 device offering choice of 12th Gen Intel Core processors or Microsoft SQ3 ARM processor with 5G connectivity, vibrant colors (Sapphire, Forest), and adjustable kickstand.',
     'Surface Pro 9 2-in-1 (2022) | Microsoft',
     'microsoft, surface pro 9, 2022, 5g sq3, intel 12th gen, sapphire',
     'Work anywhere with Microsoft Surface Pro 9 2-in-1 laptop-tablet with optional 5G and stunning 13-inch display.',
     4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 22. Microsoft Surface Laptop Studio 2 (2023)
    (22, 
     'Surface Laptop Studio 2 (2023)', 
     'surface-laptop-studio-2-2023', 
     'Microsofts most powerful Surface ever with 13th Gen Intel Core i7, NVIDIA GeForce RTX 4060 graphics, Intel Gen3 Movidius NPU, AI-powered Studio Camera, and customizable tactile touchpad.',
     'Surface Laptop Studio 2 (2023) | Microsoft',
     'microsoft, surface laptop studio 2, rtx 4060, npu, intel 13th gen',
     'Empower creative and 3D workloads with Surface Laptop Studio 2 featuring RTX 4060 GPU and versatile form factor.',
     2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 23. Microsoft Surface Laptop 7th Edition (2024)
    (23, 
     'Surface Laptop 7th Edition (2024)', 
     'surface-laptop-7th-edition-copilot-plus-2024', 
     'A revolutionary Copilot+ PC powered by Snapdragon X Elite processor with 45 TOPS NPU, breakthrough all-day battery life, ultra-thin bezels, and local AI experience on Windows 11.',
     'Surface Laptop 7th Edition Copilot+ PC',
     'microsoft, surface laptop 7, copilot+ pc, snapdragon x elite, 45 tops',
     'Next-gen AI era: Microsoft Surface Laptop 7th Edition Copilot+ PC with Snapdragon X Elite and all-day battery life.',
     2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 24. Microsoft Surface Pro 11th Edition (2024)
    (24, 
     'Surface Pro 11th Edition (2024)', 
     'surface-pro-11th-edition-copilot-plus-2024', 
     'Copilot+ PC 2-in-1 featuring optional OLED HDR display, Snapdragon X Plus / X Elite chipsets, Surface Pro Flex Keyboard with detached wireless mode, and 45 TOPS neural processing.',
     'Surface Pro 11th Edition Copilot+ PC',
     'microsoft, surface pro 11, copilot+ pc, oled, snapdragon x, flex keyboard',
     'The ultimate 2-in-1 AI PC: Microsoft Surface Pro 11th Edition with Snapdragon X Elite and vibrant OLED screen.',
     4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 25. Microsoft Xbox Series X (2021-2024)
    (25, 
     'Xbox Series X Console', 
     'xbox-series-x-console', 
     'The fastest and most powerful Xbox console delivering true 4K gaming, up to 120 FPS, 12 teraflops of GPU compute, Xbox Velocity Architecture with custom NVMe SSD, and Quick Resume.',
     'Xbox Series X 4K Gaming Console | Microsoft',
     'microsoft, xbox series x, 12 teraflops, 4k 120fps, quick resume',
     'Experience high-fidelity 4K gaming at up to 120 FPS on Microsoft Xbox Series X with 1TB custom SSD.',
     5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 26. Microsoft Xbox Series S (2021-2024)
    (26, 
     'Xbox Series S Console', 
     'xbox-series-s-console', 
     'All-digital next-gen gaming in the smallest Xbox chassis ever. Enjoy 1440p gaming up to 120 FPS, Ray Tracing, Quick Resume, and seamless Xbox Game Pass library access.',
     'Xbox Series S All-Digital Console | Microsoft',
     'microsoft, xbox series s, all-digital, 120fps, game pass',
     'Next-generation speed and performance in a compact, all-digital design with Microsoft Xbox Series S.',
     5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name, 
    slug = EXCLUDED.slug, 
    description = EXCLUDED.description, 
    meta_title = EXCLUDED.meta_title, 
    meta_keyword = EXCLUDED.meta_keyword, 
    meta_description = EXCLUDED.meta_description, 
    category_id = EXCLUDED.category_id, 
    brand_id = EXCLUDED.brand_id, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 8. PRODUCT ATTRIBUTE VALUES (Specifications for each Product)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_attribute_value (id, product_attribute_id, product_id, value, created_at, updated_at)
VALUES 
    -- 1. MacBook Pro 14" (2021)
    (1, 1, 1, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 1, 'Apple M1 Pro (8-core or 10-core CPU, up to 16-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, 1, '14.2-inch Liquid Retina XDR (3024x1964), 120Hz ProMotion', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 4, 1, 'macOS Monterey (Upgradable to macOS Sequoia)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 5, 1, 'Wi-Fi 6 (802.11ax), Bluetooth 5.0, 3x Thunderbolt 4, HDMI, MagSafe 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 2. MacBook Pro 16" (2021)
    (6, 1, 2, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 2, 2, 'Apple M1 Max (10-core CPU, 32-core GPU, 16-core Neural Engine)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 3, 2, '16.2-inch Liquid Retina XDR (3456x2234), 120Hz ProMotion', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 4, 2, 'macOS Monterey (Upgradable to macOS Sequoia)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 3. MacBook Air 13" (2022)
    (10, 1, 3, '2022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 2, 3, 'Apple M2 (8-core CPU, up to 10-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 3, 3, '13.6-inch Liquid Retina Display (2560x1664), 500 nits', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 4, 3, 'macOS Ventura (Upgradable to macOS Sequoia)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 4. MacBook Pro 14" (2023)
    (14, 1, 4, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 2, 4, 'Apple M2 Pro (10-core / 12-core CPU, up to 19-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 3, 4, '14.2-inch Liquid Retina XDR (3024x1964), 1600 nits peak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 5, 4, 'Wi-Fi 6E, Bluetooth 5.3, HDMI 2.1 (supports 8K display)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 5. MacBook Air 15" (2023)
    (18, 1, 5, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 2, 5, 'Apple M2 (8-core CPU, 10-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 3, 5, '15.3-inch Liquid Retina Display (2880x1864), 500 nits', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 6. MacBook Pro 14" (Late 2023)
    (21, 1, 6, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 2, 6, 'Apple M3 Pro (3nm architecture, hardware ray tracing)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 3, 6, '14.2-inch Liquid Retina XDR, 600 nits SDR / 1600 nits HDR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 7. MacBook Air 13" (2024)
    (24, 1, 7, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 2, 7, 'Apple M3 (8-core CPU, 10-core GPU, 16-core Neural Engine)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 3, 7, '13.6-inch Liquid Retina (Dual external monitor support)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 4, 7, 'macOS Sonoma (Upgradable to macOS Sequoia)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 8. MacBook Pro 16" (2024)
    (28, 1, 8, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 2, 8, 'Apple M4 Pro (Up to 14-core CPU, 20-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (30, 3, 8, '16.2-inch Liquid Retina XDR with optional Nano-texture glass', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 5, 8, 'Thunderbolt 5 (up to 120 Gb/s transfer speeds)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 9. iPhone 13 (2021)
    (32, 1, 9, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, 2, 9, 'Apple A15 Bionic (6-core CPU, 4-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (34, 3, 9, '6.1-inch Super Retina XDR OLED (2532x1170), Ceramic Shield', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 4, 9, 'iOS 15 (Upgradable to iOS 18)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 10. iPhone 13 Pro (2021)
    (36, 1, 10, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, 2, 10, 'Apple A15 Bionic (6-core CPU, 5-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, 3, 10, '6.1-inch Super Retina XDR with ProMotion 120Hz', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 11. iPhone 14 (2022)
    (39, 1, 11, '2022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, 2, 11, 'Apple A15 Bionic (5-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, 3, 11, '6.1-inch Super Retina XDR OLED (800 nits typical / 1200 nits HDR)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 12. iPhone 14 Pro Max (2022)
    (42, 1, 12, '2022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (43, 2, 12, 'Apple A16 Bionic (4nm, 6-core CPU, 5-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, 3, 12, '6.7-inch Always-On Dynamic Island Super Retina XDR (2000 nits peak)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 13. iPhone 15 (2023)
    (45, 1, 13, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, 2, 13, 'Apple A16 Bionic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, 3, 13, '6.1-inch Dynamic Island Super Retina XDR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (48, 5, 13, 'USB-C connector, 5G NR, Wi-Fi 6, Second-Gen Ultra Wideband', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 14. iPhone 15 Pro Max (2023)
    (49, 1, 14, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, 2, 14, 'Apple A17 Pro (3nm, hardware ray tracing, 6-core GPU)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, 3, 14, '6.7-inch Super Retina XDR ProMotion, Always-On, Titanium frame', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, 5, 14, 'USB-C supporting USB 3 (up to 10 Gb/s), Wi-Fi 6E, Thread networking', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 15. iPhone 16 (2024)
    (53, 1, 15, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, 2, 15, 'Apple A18 (Apple Intelligence, 16-core Neural Engine)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (55, 3, 15, '6.1-inch Super Retina XDR, Camera Control tactile button', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 16. iPhone 16 Pro Max (2024)
    (56, 1, 16, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (57, 2, 16, 'Apple A18 Pro (3nm Gen 2, Apple Intelligence)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (58, 3, 16, '6.9-inch Borderless Super Retina XDR ProMotion, 120Hz', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 17. Surface Laptop 4 (2021)
    (59, 1, 17, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (60, 2, 17, '11th Gen Intel Core i7-1185G7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (61, 3, 17, '13.5-inch PixelSense Display (2256x1504), 3:2 aspect ratio touch', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (62, 4, 17, 'Windows 10 / Windows 11 Home', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 18. Surface Pro 8 (2021)
    (63, 1, 18, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (64, 2, 18, '11th Gen Intel Core i7-1185G7 (Intel Evo platform)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (65, 3, 18, '13.0-inch PixelSense Flow (2880x1920), up to 120Hz refresh rate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (66, 5, 18, '2x USB-C with Thunderbolt 4, Surface Connect, Wi-Fi 6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 19. Surface Laptop Studio (2021)
    (67, 1, 19, '2021', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (68, 2, 19, '11th Gen Intel Core i7-11370H + NVIDIA GeForce RTX 3050 Ti', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (69, 3, 19, '14.4-inch PixelSense Flow 120Hz (2400x1600) with dynamic woven hinge', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 20. Surface Laptop 5 (2022)
    (70, 1, 20, '2022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (71, 2, 20, '12th Gen Intel Core i7-1255U (Intel Evo platform)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (72, 3, 20, '13.5-inch PixelSense touchscreen with Dolby Vision IQ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 21. Surface Pro 9 (2022)
    (73, 1, 21, '2022', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (74, 2, 21, '12th Gen Intel Core i7-1255U / Microsoft SQ3 ARM processor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (75, 3, 21, '13.0-inch PixelSense Flow display, 120Hz, Gorilla Glass 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 22. Surface Laptop Studio 2 (2023)
    (76, 1, 22, '2023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (77, 2, 22, '13th Gen Intel Core i7-13700H + NVIDIA GeForce RTX 4060 GPU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (78, 3, 22, '14.4-inch PixelSense Flow 120Hz HDR display', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 23. Surface Laptop 7th Edition (2024)
    (79, 1, 23, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (80, 2, 23, 'Qualcomm Snapdragon X Elite (12 cores) + Qualcomm Hexagon 45 TOPS NPU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (81, 3, 23, '13.8-inch PixelSense Flow (2304x1536), 120Hz dynamic refresh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (82, 4, 23, 'Windows 11 Home Copilot+ PC edition', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 24. Surface Pro 11th Edition (2024)
    (83, 1, 24, '2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (84, 2, 24, 'Qualcomm Snapdragon X Elite + 45 TOPS Hexagon NPU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (85, 3, 24, '13.0-inch OLED PixelSense Flow (2880x1920), 120Hz, 1M:1 contrast', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (86, 4, 24, 'Windows 11 Home Copilot+ PC edition', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 25. Xbox Series X (2021-2024)
    (87, 1, 25, '2021-2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (88, 2, 25, 'Custom AMD Zen 2 (8 Cores @ 3.8 GHz) + RDNA 2 GPU (12.15 TFLOPS)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (89, 5, 25, 'HDMI 2.1, 3x USB 3.1 Gen 1, Gigabit Ethernet, 802.11ac Wi-Fi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- 26. Xbox Series S (2021-2024)
    (90, 1, 26, '2021-2024', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (91, 2, 26, 'Custom AMD Zen 2 (8 Cores @ 3.6 GHz) + RDNA 2 GPU (4 TFLOPS)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (92, 5, 26, 'HDMI 2.1, 3x USB 3.1 Gen 1, Gigabit Ethernet, 802.11ac Wi-Fi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    value = EXCLUDED.value, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 9. PRODUCT OPTION COMBINATIONS (Which Options apply to which Product)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option_combination (product_id, product_option_id, position)
VALUES 
    -- MacBooks (Color: 1, Storage: 2, RAM: 3)
    (1, 1, 1), (1, 2, 2), (1, 3, 3),
    (2, 1, 1), (2, 2, 2), (2, 3, 3),
    (3, 1, 1), (3, 2, 2), (3, 3, 3),
    (4, 1, 1), (4, 2, 2), (4, 3, 3),
    (5, 1, 1), (5, 2, 2), (5, 3, 3),
    (6, 1, 1), (6, 2, 2), (6, 3, 3),
    (7, 1, 1), (7, 2, 2), (7, 3, 3),
    (8, 1, 1), (8, 2, 2), (8, 3, 3),

    -- iPhones (Color: 1, Storage: 2)
    (9, 1, 1), (9, 2, 2),
    (10, 1, 1), (10, 2, 2),
    (11, 1, 1), (11, 2, 2),
    (12, 1, 1), (12, 2, 2),
    (13, 1, 1), (13, 2, 2),
    (14, 1, 1), (14, 2, 2),
    (15, 1, 1), (15, 2, 2),
    (16, 1, 1), (16, 2, 2),

    -- Surface Laptops & 2-in-1s (Color: 1, Storage: 2, RAM: 3)
    (17, 1, 1), (17, 2, 2), (17, 3, 3),
    (18, 1, 1), (18, 2, 2), (18, 3, 3),
    (19, 1, 1), (19, 2, 2), (19, 3, 3),
    (20, 1, 1), (20, 2, 2), (20, 3, 3),
    (21, 1, 1), (21, 2, 2), (21, 3, 3),
    (22, 1, 1), (22, 2, 2), (22, 3, 3),
    (23, 1, 1), (23, 2, 2), (23, 3, 3),
    (24, 1, 1), (24, 2, 2), (24, 3, 3),

    -- Xbox Consoles (Color: 1, Storage: 2)
    (25, 1, 1), (25, 2, 2),
    (26, 1, 1), (26, 2, 2)
ON CONFLICT (product_id, product_option_id) DO UPDATE SET 
    position = EXCLUDED.position;

-- -----------------------------------------------------------------------------
-- 10. PRODUCT OPTION VALUES (Values available for specific products)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_option_value (id, value, position, product_id, product_option_id, created_at, updated_at)
VALUES 
    -- Product 1: MacBook Pro 14" (2021)
    (1, 'Space Gray', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Silver', 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '512GB SSD', 1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '1TB SSD', 2, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '16GB Unified Memory', 1, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, '32GB Unified Memory', 2, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 3: MacBook Air 13" (2022) M2
    (7, 'Midnight', 1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'Starlight', 2, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'Space Gray', 3, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, '256GB SSD', 1, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, '512GB SSD', 2, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, '8GB Unified Memory', 1, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, '16GB Unified Memory', 2, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 6: MacBook Pro 14" (Late 2023) M3 Pro
    (14, 'Space Black', 1, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'Silver', 2, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, '512GB SSD', 1, 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, '1TB SSD', 2, 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, '18GB Unified Memory', 1, 6, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, '36GB Unified Memory', 2, 6, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 7: MacBook Air 13" (2024) M3
    (20, 'Midnight', 1, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'Starlight', 2, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, '256GB SSD', 1, 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, '512GB SSD', 2, 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, '16GB Unified Memory', 1, 7, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 9: iPhone 13 (2021)
    (25, 'Midnight', 1, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 'Starlight', 2, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'Blue', 3, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, '128GB', 1, 9, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, '256GB', 2, 9, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 12: iPhone 14 Pro Max (2022)
    (30, 'Deep Purple', 1, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 'Space Black', 2, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 'Gold', 3, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (33, '128GB', 1, 12, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (34, '256GB', 2, 12, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, '512GB', 3, 12, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 14: iPhone 15 Pro Max (2023)
    (36, 'Natural Titanium', 1, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (37, 'Blue Titanium', 2, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, 'Black Titanium', 3, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (39, '256GB', 1, 14, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, '512GB', 2, 14, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (41, '1TB', 3, 14, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 16: iPhone 16 Pro Max (2024)
    (42, 'Desert Titanium', 1, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (43, 'Natural Titanium', 2, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, 'Black Titanium', 3, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45, '256GB', 1, 16, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (46, '512GB', 2, 16, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (47, '1TB', 3, 16, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 18: Surface Pro 8 (2021)
    (48, 'Platinum', 1, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (49, 'Graphite', 2, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (50, '256GB SSD', 1, 18, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (51, '512GB SSD', 2, 18, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (52, '16GB RAM', 1, 18, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 21: Surface Pro 9 (2022)
    (53, 'Sapphire', 1, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (54, 'Forest', 2, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (55, 'Platinum', 3, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (56, '256GB SSD', 1, 21, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (57, '512GB SSD', 2, 21, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (58, '16GB RAM', 1, 21, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 23: Surface Laptop 7th Edition (2024)
    (59, 'Sapphire', 1, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (60, 'Dune', 2, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (61, 'Black', 3, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (62, '512GB SSD', 1, 23, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (63, '1TB SSD', 2, 23, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (64, '16GB RAM', 1, 23, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (65, '32GB RAM', 2, 23, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 24: Surface Pro 11th Edition (2024)
    (66, 'Sapphire (OLED)', 1, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (67, 'Dune (OLED)', 2, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (68, 'Platinum (LCD)', 3, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (69, '512GB SSD', 1, 24, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (70, '1TB SSD', 2, 24, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (71, '16GB RAM', 1, 24, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (72, '32GB RAM', 2, 24, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 25: Xbox Series X
    (73, 'Carbon Black', 1, 25, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (74, 'Robot White Digital Edition', 2, 25, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (75, '1TB SSD', 1, 25, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (76, '2TB SSD Special Edition', 2, 25, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 26: Xbox Series S
    (77, 'Robot White', 1, 26, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (78, 'Carbon Black', 2, 26, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (79, '512GB SSD', 1, 26, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (80, '1TB SSD', 2, 26, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    value = EXCLUDED.value, 
    position = EXCLUDED.position, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 11. PRODUCT VARIANTS (SKUs, Prices, Inventories)
-- -----------------------------------------------------------------------------
INSERT INTO tbl_product_variant (id, title, sku, price, quantity, status, media_id, product_id, created_at, updated_at)
VALUES 
    -- Product 1: MacBook Pro 14" (2021)
    (1, 'MacBook Pro 14" (2021) - Space Gray / 16GB / 512GB', 'MBP14-2021-M1P-SG-16-512', 1999.00, 25, 'ACTIVE', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'MacBook Pro 14" (2021) - Silver / 32GB / 1TB', 'MBP14-2021-M1P-SL-32-1TB', 2599.00, 15, 'ACTIVE', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 2: MacBook Pro 16" (2021)
    (3, 'MacBook Pro 16" (2021) - Space Gray / 32GB / 1TB', 'MBP16-2021-M1M-SG-32-1TB', 3499.00, 18, 'ACTIVE', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 3: MacBook Air 13" (2022) M2
    (4, 'MacBook Air 13" (2022) M2 - Midnight / 8GB / 256GB', 'MBA13-2022-M2-MD-8-256', 1099.00, 40, 'ACTIVE', NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'MacBook Air 13" (2022) M2 - Starlight / 16GB / 512GB', 'MBA13-2022-M2-SL-16-512', 1499.00, 30, 'ACTIVE', NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 4: MacBook Pro 14" (2023) M2 Pro
    (6, 'MacBook Pro 14" (2023) M2 Pro - Space Gray / 16GB / 512GB', 'MBP14-2023-M2P-SG-16-512', 1999.00, 20, 'ACTIVE', NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 5: MacBook Air 15" (2023) M2
    (7, 'MacBook Air 15" (2023) M2 - Midnight / 16GB / 512GB', 'MBA15-2023-M2-MD-16-512', 1499.00, 22, 'ACTIVE', NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 6: MacBook Pro 14" (Late 2023) M3 Pro
    (8, 'MacBook Pro 14" (Late 2023) M3 Pro - Space Black / 18GB / 512GB', 'MBP14-2023L-M3P-SB-18-512', 1999.00, 35, 'ACTIVE', NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'MacBook Pro 14" (Late 2023) M3 Pro - Space Black / 36GB / 1TB', 'MBP14-2023L-M3P-SB-36-1TB', 2599.00, 18, 'ACTIVE', NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 7: MacBook Air 13" (2024) M3
    (10, 'MacBook Air 13" (2024) M3 - Midnight / 16GB / 256GB', 'MBA13-2024-M3-MD-16-256', 1099.00, 50, 'ACTIVE', NULL, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'MacBook Air 13" (2024) M3 - Starlight / 16GB / 512GB', 'MBA13-2024-M3-ST-16-512', 1299.00, 45, 'ACTIVE', NULL, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 8: MacBook Pro 16" (2024) M4 Pro
    (12, 'MacBook Pro 16" (2024) M4 Pro - Space Black / 24GB / 512GB', 'MBP16-2024-M4P-SB-24-512', 2499.00, 30, 'ACTIVE', NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 'MacBook Pro 16" (2024) M4 Pro - Silver / 48GB / 1TB', 'MBP16-2024-M4P-SL-48-1TB', 3299.00, 15, 'ACTIVE', NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 9: iPhone 13 (2021)
    (14, 'iPhone 13 (2021) - Midnight / 128GB', 'IP13-2021-MD-128', 699.00, 50, 'ACTIVE', NULL, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 'iPhone 13 (2021) - Blue / 256GB', 'IP13-2021-BL-256', 799.00, 30, 'ACTIVE', NULL, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 10: iPhone 13 Pro (2021)
    (16, 'iPhone 13 Pro (2021) - Sierra Blue / 256GB', 'IP13P-2021-SB-256', 999.00, 20, 'ACTIVE', NULL, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 11: iPhone 14 (2022)
    (17, 'iPhone 14 (2022) - Midnight / 128GB', 'IP14-2022-MD-128', 799.00, 45, 'ACTIVE', NULL, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 12: iPhone 14 Pro Max (2022)
    (18, 'iPhone 14 Pro Max (2022) - Deep Purple / 256GB', 'IP14PM-2022-DP-256', 1199.00, 35, 'ACTIVE', NULL, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 'iPhone 14 Pro Max (2022) - Space Black / 512GB', 'IP14PM-2022-SB-512', 1399.00, 20, 'ACTIVE', NULL, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 13: iPhone 15 (2023)
    (20, 'iPhone 15 (2023) - Black / 128GB', 'IP15-2023-BK-128', 799.00, 60, 'ACTIVE', NULL, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'iPhone 15 (2023) - Blue / 256GB', 'IP15-2023-BL-256', 899.00, 45, 'ACTIVE', NULL, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 14: iPhone 15 Pro Max (2023)
    (22, 'iPhone 15 Pro Max (2023) - Natural Titanium / 256GB', 'IP15PM-2023-NT-256', 1199.00, 50, 'ACTIVE', NULL, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'iPhone 15 Pro Max (2023) - Black Titanium / 512GB', 'IP15PM-2023-BT-512', 1399.00, 30, 'ACTIVE', NULL, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 15: iPhone 16 (2024)
    (24, 'iPhone 16 (2024) - Ultramarine / 128GB', 'IP16-2024-UM-128', 799.00, 80, 'ACTIVE', NULL, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 'iPhone 16 (2024) - Teal / 256GB', 'IP16-2024-TL-256', 899.00, 60, 'ACTIVE', NULL, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 16: iPhone 16 Pro Max (2024)
    (26, 'iPhone 16 Pro Max (2024) - Desert Titanium / 256GB', 'IP16PM-2024-DT-256', 1199.00, 70, 'ACTIVE', NULL, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27, 'iPhone 16 Pro Max (2024) - Natural Titanium / 512GB', 'IP16PM-2024-NT-512', 1399.00, 40, 'ACTIVE', NULL, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 'iPhone 16 Pro Max (2024) - Black Titanium / 1TB', 'IP16PM-2024-BT-1TB', 1599.00, 25, 'ACTIVE', NULL, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 17: Surface Laptop 4 (2021)
    (29, 'Surface Laptop 4 13.5" (2021) - Platinum / 16GB / 512GB', 'SL4-2021-PL-16-512', 1299.00, 20, 'ACTIVE', NULL, 17, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 18: Surface Pro 8 (2021)
    (30, 'Surface Pro 8 (2021) - Platinum / 16GB / 256GB', 'SP8-2021-PL-16-256', 1199.00, 25, 'ACTIVE', NULL, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 'Surface Pro 8 (2021) - Graphite / 16GB / 512GB', 'SP8-2021-GR-16-512', 1399.00, 15, 'ACTIVE', NULL, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 19: Surface Laptop Studio (2021)
    (32, 'Surface Laptop Studio (2021) - Platinum / 16GB / 512GB RTX3050Ti', 'SLS-2021-PL-16-512-RTX', 1899.00, 15, 'ACTIVE', NULL, 19, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 20: Surface Laptop 5 (2022)
    (33, 'Surface Laptop 5 13.5" (2022) - Platinum / 16GB / 512GB', 'SL5-2022-PL-16-512', 1299.00, 25, 'ACTIVE', NULL, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 21: Surface Pro 9 (2022)
    (34, 'Surface Pro 9 (2022) - Sapphire / 16GB / 256GB', 'SP9-2022-SA-16-256', 1299.00, 30, 'ACTIVE', NULL, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (35, 'Surface Pro 9 (2022) - Forest / 16GB / 512GB', 'SP9-2022-FO-16-512', 1499.00, 20, 'ACTIVE', NULL, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 22: Surface Laptop Studio 2 (2023)
    (36, 'Surface Laptop Studio 2 (2023) - Platinum / 32GB / 1TB RTX4060', 'SLS2-2023-PL-32-1TB-RTX', 2799.00, 12, 'ACTIVE', NULL, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 23: Surface Laptop 7th Edition (2024)
    (37, 'Surface Laptop 7th Edition (2024) - Sapphire / 16GB / 512GB', 'SL7-2024-SA-16-512', 1199.00, 45, 'ACTIVE', NULL, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (38, 'Surface Laptop 7th Edition (2024) - Dune / 32GB / 1TB', 'SL7-2024-DU-32-1TB', 1599.00, 30, 'ACTIVE', NULL, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 24: Surface Pro 11th Edition (2024)
    (39, 'Surface Pro 11th Edition (2024) - Sapphire OLED / 16GB / 512GB', 'SP11-2024-SA-16-512-OLED', 1499.00, 40, 'ACTIVE', NULL, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (40, 'Surface Pro 11th Edition (2024) - Platinum LCD / 16GB / 512GB', 'SP11-2024-PL-16-512-LCD', 1199.00, 35, 'ACTIVE', NULL, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 25: Xbox Series X
    (41, 'Xbox Series X 1TB Console - Carbon Black', 'XBX-1TB-CB', 499.99, 50, 'ACTIVE', NULL, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (42, 'Xbox Series X 2TB Console - Galaxy Black Special Edition', 'XBX-2TB-GB-SE', 599.99, 25, 'ACTIVE', NULL, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Product 26: Xbox Series S
    (43, 'Xbox Series S 512GB Console - Robot White', 'XBS-512GB-RW', 299.99, 60, 'ACTIVE', NULL, 26, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (44, 'Xbox Series S 1TB Console - Carbon Black', 'XBS-1TB-CB', 349.99, 45, 'ACTIVE', NULL, 26, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    title = EXCLUDED.title, 
    sku = EXCLUDED.sku, 
    price = EXCLUDED.price, 
    quantity = EXCLUDED.quantity, 
    status = EXCLUDED.status, 
    updated_at = CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 12. VARIANT OPTION VALUE RELATIONS
-- -----------------------------------------------------------------------------
INSERT INTO tbl_variant_option_value (id, product_variant_id, option_value_id, created_at, updated_at)
VALUES 
    -- Variant 1: MBP 14 (Space Gray, 512GB, 16GB)
    (1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 2: MBP 14 (Silver, 1TB, 32GB)
    (4, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 4: MBA 13 (Midnight, 256GB, 8GB)
    (7, 4, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 4, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 4, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 8: MBP 14 M3 Pro (Space Black, 512GB, 18GB)
    (10, 8, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 8, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 8, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 14: iPhone 13 (Midnight, 128GB)
    (13, 14, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 14, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 18: iPhone 14 Pro Max (Deep Purple, 256GB)
    (15, 18, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 18, 34, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 22: iPhone 15 Pro Max (Natural Titanium, 256GB)
    (17, 22, 36, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 22, 39, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 26: iPhone 16 Pro Max (Desert Titanium, 256GB)
    (19, 26, 42, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 26, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 30: Surface Pro 8 (Platinum, 256GB, 16GB)
    (21, 30, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 30, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 30, 52, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 34: Surface Pro 9 (Sapphire, 256GB, 16GB)
    (24, 34, 53, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25, 34, 56, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (26, 34, 58, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 37: Surface Laptop 7 (Sapphire, 512GB, 16GB)
    (27, 37, 59, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (28, 37, 62, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29, 37, 64, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 39: Surface Pro 11 (Sapphire OLED, 512GB, 16GB)
    (30, 39, 66, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (31, 39, 69, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32, 39, 71, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 41: Xbox Series X (Carbon Black, 1TB)
    (33, 41, 73, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (34, 41, 75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Variant 43: Xbox Series S (Robot White, 512GB)
    (35, 43, 77, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36, 43, 79, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (product_variant_id, option_value_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 13. RE-SYNC POSTGRESQL SEQUENCES
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

COMMIT;
