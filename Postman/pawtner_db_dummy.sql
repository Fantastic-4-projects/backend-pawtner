-- Pastikan ekstensi yang diperlukan sudah ada
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- Hapus data yang ada untuk memastikan kebersihan (opsional, hati-hati di production)
-- DELETE FROM reviews;
-- DELETE FROM prescription_items;
-- DELETE FROM prescriptions;
-- DELETE FROM payments;
-- DELETE FROM bookings;
-- DELETE FROM order_items;
-- DELETE FROM orders;
-- DELETE FROM cart_items;
-- DELETE FROM shopping_carts;
-- DELETE FROM services;
-- DELETE FROM products;
-- DELETE FROM pets;
-- DELETE FROM businesses;
-- DELETE FROM m_fcm_token;
-- DELETE FROM users;
	
-- ====================================================================================
-- 1. USERS
-- Password untuk semua user adalah "password123"
-- ====================================================================================

-- Definisikan variabel dan masukkan data User
DO $$
DECLARE
    admin_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
    owner_john_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12';
    owner_jane_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13';
    customer_peter_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14';
BEGIN
    INSERT INTO users (id, name, email, phone_number, password_hash, address, image_url, is_verified, role, auth_provider, is_enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, created_at) VALUES
    (admin_id, 'Admin Pawtner', 'admin@pawtner.com', '089999999999', '$2a$10$wTf/2gVd4w1fVf5/Al3xS.D4iY/R.8D.2x/4j.C/v1LgXk2O7E5mK', 'Pawtner HQ', 'https://example.com/images/admin.png', true, 'ADMIN', 'local', true, true, true, true, NOW()),
    (owner_john_id, 'Dr. John Vet', 'john.vet@pawtner.com', '081211112222', '$2a$10$wTf/2gVd4w1fVf5/Al3xS.D4iY/R.8D.2x/4j.C/v1LgXk2O7E5mK', 'Jl. Dokter Hewan No. 1, Jakarta', 'https://example.com/images/john.png', true, 'BUSINESS_OWNER', 'local', true, true, true, true, NOW()),
    (owner_jane_id, 'Jane Groomer', 'jane.groomer@pawtner.com', '081233334444', '$2a$10$wTf/2gVd4w1fVf5/Al3xS.D4iY/R.8D.2x/4j.C/v1LgXk2O7E5mK', 'Jl. Peliharaan Indah No. 2, Jakarta', 'https://example.com/images/jane.png', true, 'BUSINESS_OWNER', 'local', true, true, true, true, NOW()),
    (customer_peter_id, 'Peter PetLover', 'peter.lover@pawtner.com', '081255556666', '$2a$10$wTf/2gVd4w1fVf5/Al3xS.D4iY/R.8D.2x/4j.C/v1LgXk2O7E5mK', 'Jl. Kucing Anjing No. 3, Jakarta', 'https://example.com/images/peter.png', true, 'CUSTOMER', 'local', true, true, true, true, NOW());
END $$;

-- ====================================================================================
-- 2. BUSINESSES
-- ====================================================================================

-- Definisikan variabel dan masukkan data Business
DO $$
DECLARE
    vet_clinic_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11';
    pet_shop_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12';
    grooming_salon_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13';
    
    owner_john_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12';
    owner_jane_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13';
BEGIN
    INSERT INTO businesses (id, owner_id, name, description, business_type, has_emergency_services, business_email, business_phone, emergency_phone, business_image_url, certificate_image_url, address, location, operation_hours, status_realtime, is_active, is_approved, created_at) VALUES
    (vet_clinic_id, owner_john_id, 'Pawtner Vet Clinic', 'Klinik hewan modern dengan fasilitas lengkap untuk keadaan darurat dan perawatan rutin.', 'VETERINARY_CLINIC', true, 'contact@pawtner-vet.com', '021-555-0101', '0812-911-0911', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/vet-clinic.jpg', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/certificate.jpg', 'Jl. Kesehatan Hewan No. 10, Jakarta Selatan', ST_SetSRID(ST_MakePoint(106.8011, -6.2255), 4326), '{"monday":"08:00-20:00", "tuesday":"08:00-20:00", "wednesday":"08:00-20:00", "thursday":"08:00-20:00", "friday":"08:00-20:00", "saturday":"09:00-17:00", "sunday":"Closed"}', 'ACCEPTING_PATIENTS', true, true, NOW()),
    (pet_shop_id, owner_jane_id, 'Pawtner Pet Shop', 'Menyediakan semua kebutuhan hewan peliharaan Anda, dari makanan hingga mainan berkualitas.', 'PET_SHOP', false, 'shop@pawtner-shop.com', '021-555-0202', null, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/pet-shop.jpg', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/certificate.jpg', 'Jl. Kebutuhan Peliharaan No. 20, Jakarta Pusat', ST_SetSRID(ST_MakePoint(106.8272, -6.1751), 4326), '{"monday":"09:00-21:00", "tuesday":"09:00-21:00", "wednesday":"09:00-21:00", "thursday":"09:00-21:00", "friday":"09:00-21:00", "saturday":"09:00-21:00", "sunday":"10:00-19:00"}', 'CLOSED', true, true, NOW()),
    (grooming_salon_id, owner_jane_id, 'Glamour Paws Grooming', 'Salon perawatan profesional untuk anjing dan kucing agar tampil menawan dan sehat.', 'GROOMING_SALON', false, 'hello@glamourpaws.com', '021-555-0303', null, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/grooming-salon.jpg', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/business/certificate.jpg', 'Jl. Gaya Peliharaan No. 30, Jakarta Barat', ST_SetSRID(ST_MakePoint(106.7497, -6.1681), 4326), '{"monday":"Closed", "tuesday":"10:00-18:00", "wednesday":"10:00-18:00", "thursday":"10:00-18:00", "friday":"10:00-18:00", "saturday":"09:00-19:00", "sunday":"09:00-19:00"}', 'CLOSED', true, true, NOW());
END $$;


-- ====================================================================================
-- 3. PETS
-- ====================================================================================

-- Definisikan variabel dan masukkan data Pet
DO $$
DECLARE
    pet_buddy_id UUID := 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11';
    pet_mochi_id UUID := 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12';
    
    customer_peter_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14';
BEGIN
    INSERT INTO pets (id, owner_id, name, species, breed, age, gender, image_url, notes, created_at) VALUES
    (pet_buddy_id, customer_peter_id, 'Buddy', 'Dog', 'Golden Retriever', 5, 'MALE', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/pets/buddy.jpg', 'Sangat aktif dan suka bermain lempar bola. Alergi terhadap gandum.', NOW()),
    (pet_mochi_id, customer_peter_id, 'Mochi', 'Cat', 'Domestic Shorthair', 2, 'FEMALE', 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/pets/mochi.jpg', 'Sedikit pemalu dengan orang baru tapi sangat manja jika sudah kenal.', NOW());
END $$;

-- ====================================================================================
-- 4. PRODUCTS
-- ====================================================================================

-- Definisikan variabel dan masukkan data Product
DO $$
DECLARE
    product_food_id UUID := 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11';
    product_toy_id UUID := 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d12';
    product_catnip_id UUID := 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d13';

    pet_shop_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12';
BEGIN
    INSERT INTO products (id, business_id, name, category, description, price, stock_quantity, is_active, image_url, created_at) VALUES
    (product_food_id, pet_shop_id, 'Premium Dog Food (Grain-Free)', 'FOOD', 'Makanan anjing kering bebas gandum dengan nutrisi seimbang untuk anjing dewasa.', 250000.00, 50, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/products/dog-food.jpg', NOW()),
    (product_toy_id, pet_shop_id, 'Durable Squeaky Bone Toy', 'TOYS', 'Mainan tulang yang tahan lama dengan bunyi mencicit untuk menjaga anjing tetap aktif.', 75000.00, 100, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/products/dog-toy.jpg', NOW()),
    (product_catnip_id, pet_shop_id, 'Organic Catnip Spray', 'HEALTH', 'Semprotan catnip organik untuk merangsang kucing Anda bermain dan mengurangi stres.', 95000.00, 75, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/products/catnip.jpg', NOW());
END $$;

-- ====================================================================================
-- 5. SERVICES
-- ====================================================================================

-- Definisikan variabel dan masukkan data Service
DO $$
DECLARE
    service_vet_id UUID := 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11';
    service_grooming_id UUID := 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12';
    service_boarding_id UUID := 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e13';

    vet_clinic_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11';
    grooming_salon_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13';
BEGIN
    INSERT INTO services (id, business_id, category, name, base_price, capacity_per_day, is_active, image_url, created_at) VALUES
    (service_vet_id, vet_clinic_id, 'VETERINARY', 'Pemeriksaan Kesehatan Rutin', 200000.00, 20, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/services/vet-checkup.jpg', NOW()),
    (service_grooming_id, grooming_salon_id, 'GROOMING', 'Paket Grooming Lengkap (Mandi, Potong Kuku & Bulu)', 150000.00, 10, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/services/grooming.jpg', NOW()),
    (service_boarding_id, vet_clinic_id, 'BOARDING', 'Penitipan Harian (Termasuk Makan & Bermain)', 100000.00, 15, true, 'https://res.cloudinary.com/dhqueh6fp/image/upload/v1720194460/pawtner/services/boarding.jpg', NOW());
END $$;

-- ====================================================================================
-- 6. ORDERS, BOOKINGS, PRESCRIPTIONS, and REVIEWS
-- Skenario: Peter (customer) membuat 1 pesanan produk dan 2 booking layanan
-- ====================================================================================
DO $$
DECLARE
    -- IDs from previous sections
    customer_peter_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14';
    pet_buddy_id UUID := 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11';
    pet_mochi_id UUID := 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12';
    pet_shop_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12';
    vet_clinic_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11';
    grooming_salon_id UUID := 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13';
    product_food_id UUID := 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11';
    product_toy_id UUID := 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d12';
    service_vet_id UUID := 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11';
    service_grooming_id UUID := 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e12';

    -- New IDs for this section
    order_id UUID := uuid_generate_v4();
    order_item1_id UUID := uuid_generate_v4();
    order_item2_id UUID := uuid_generate_v4();
    booking_vet_id UUID := uuid_generate_v4();
    booking_groom_id UUID := uuid_generate_v4();
    payment_order_id UUID := uuid_generate_v4();
    payment_booking_vet_id UUID := uuid_generate_v4();
    payment_booking_groom_id UUID := uuid_generate_v4();
    prescription_id UUID := uuid_generate_v4();
    review_shop_id UUID := uuid_generate_v4();
    review_grooming_id UUID := uuid_generate_v4();
    
BEGIN
    -- 6.1. ORDER
    INSERT INTO orders (id, customer_id, business_id, order_number, total_amount, status, created_at) VALUES
    (order_id, customer_peter_id, pet_shop_id, 'ORD-20240705-001', 325000.00, 'PENDING_PAYMENT', NOW());

    -- 6.2. ORDER ITEMS
    INSERT INTO order_items (id, order_id, product_id, quantity, price_per_unit) VALUES
    (order_item1_id, order_id, product_food_id, 1, 250000.00),
    (order_item2_id, order_id, product_toy_id, 1, 75000.00);

    -- 6.3. BOOKINGS
    INSERT INTO bookings (id, customer_id, pet_id, service_id, booking_number, start_time, end_time, total_price, status, created_at) VALUES
    (booking_vet_id, customer_peter_id, pet_buddy_id, service_vet_id, 'BOOK-20240710-001', NOW() + INTERVAL '5 day', NOW() + INTERVAL '5 day 2 hour', 200000.00, 'AWAITING_PAYMENT', NOW()),
    (booking_groom_id, customer_peter_id, pet_mochi_id, service_grooming_id, 'BOOK-20240712-002', NOW() + INTERVAL '7 day', NOW() + INTERVAL '7 day 3 hour', 150000.00, 'CONFIRMED', NOW());

    -- 6.4. PAYMENTS
    INSERT INTO payments (id, order_id, booking_id, payment_gateway_ref_id, amount, payment_method, status, snap_token, created_at, updated_at) VALUES
    (payment_order_id, order_id, null, 'ORD-20240705-001', 325000.00, null, 'PENDING', 'snap-token-for-order-001', NOW(), NOW()),
    (payment_booking_vet_id, null, booking_vet_id, 'BOOK-20240710-001', 200000.00, null, 'PENDING', 'snap-token-for-booking-vet-001', NOW(), NOW()),
    (payment_booking_groom_id, null, booking_groom_id, 'BOOK-20240712-002', 150000.00, 'QRIS', 'SETTLEMENT', 'snap-token-for-booking-groom-002', NOW(), NOW());

    -- Link payment back to booking for snap token retrieval if needed
    UPDATE bookings SET snap_token = 'snap-token-for-booking-vet-001', payment_id = payment_booking_vet_id WHERE id = booking_vet_id;
    UPDATE bookings SET snap_token = 'snap-token-for-booking-groom-002', payment_id = payment_booking_groom_id WHERE id = booking_groom_id;

    -- 6.5. PRESCRIPTION (related to the vet booking)
    INSERT INTO prescriptions (id, pet_id, issuing_business_id, booking_id, issue_date, notes, created_at) VALUES
    (prescription_id, pet_buddy_id, vet_clinic_id, booking_vet_id, (NOW() + INTERVAL '5 day')::date, 'Diberikan setelah makan. Awasi jika ada reaksi alergi.', NOW());

    -- 6.6. PRESCRIPTION ITEMS
    INSERT INTO prescription_items (id, prescription_id, medication_name, dosage, frequency, duration_days, instructions) VALUES
    (uuid_generate_v4(), prescription_id, 'Amoxicillin', '250mg', '2 kali sehari', 7, 'Habiskan seluruh antibiotik meskipun sudah terlihat sehat.'),
    (uuid_generate_v4(), prescription_id, 'Carprofen (Pain Relief)', '50mg', '1 kali sehari jika perlu', 3, 'Berikan hanya jika anjing terlihat kesakitan atau pincang.');

    -- 6.7. REVIEWS (related to completed/confirmed transactions)
    INSERT INTO reviews (id, user_id, business_id, product_id, service_id, rating, comment, created_at) VALUES
    (review_shop_id, customer_peter_id, pet_shop_id, null, null, 5, 'Pelayanannya sangat ramah dan produknya lengkap! Pasti akan kembali lagi.', NOW()),
    (review_grooming_id, customer_peter_id, null, null, service_grooming_id, 4, 'Hasil groomingnya bagus, Mochi jadi wangi dan rapi. Hanya saja tempat tunggunya agak kecil.', NOW());
END $$;

-- ====================================================================================
-- 7. FCM TOKENS
-- ====================================================================================

DO $$
DECLARE
    customer_peter_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14';
BEGIN
    INSERT INTO m_fcm_token(id, user_id, fcm_token, created_at) VALUES
    (uuid_generate_v4(), customer_peter_id, 'ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]', NOW());
END $$;