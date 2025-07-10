-- TRUNCATE ALL TABLES TO ENSURE A CLEAN SLATE
TRUNCATE TABLE
    users,
    businesses,
    pets,
    services,
    products,
    shopping_carts,
    cart_items,
    orders,
    order_items,
    bookings,
    prescriptions,
    prescription_items,
    payments,
    reviews
CASCADE;

-- Dummy Data for Users
INSERT INTO users (id, name, email, phone_number, password_hash, address, image_url, is_verified, role, auth_provider,
                   code_verification, code_expire, reset_password_token, reset_password_token_expire, created_at, is_account_non_expired,
                   is_account_non_locked, is_credentials_non_expired, is_enabled) VALUES
    ('00000000-0000-4000-8000-000000000001', 'Customer User One', 'customer1@example.com', '081234567890', '$2a$10$somehashedpasswordstring',
     '123 Customer St, City, Country', NULL, TRUE, 'CUSTOMER', 'local', NULL, NULL, NULL, NULL, NOW(), TRUE, TRUE, TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000002', 'Customer User Two', 'customer2@example.com', '081234567891', '$2a$10$somehashedpasswordstring',
     '456 Customer Ave, City, Country', NULL, TRUE, 'CUSTOMER', 'local', NULL, NULL, NULL, NULL, NOW(), TRUE, TRUE, TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000003', 'Business Owner One', 'business1@example.com', '081212345678',
     '$2a$10$somehashedpasswordstring', '456 Business Ave, City, Country', NULL, TRUE, 'BUSINESS_OWNER', 'local', NULL, NULL, NULL, NULL,
     NOW(), TRUE, TRUE, TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000004', 'Business Owner Two', 'business2@example.com', '081212345679',
     '$2a$10$somehashedpasswordstring', '789 Business Rd, City, Country', NULL, TRUE, 'BUSINESS_OWNER', 'local', NULL, NULL, NULL, NULL,
     NOW(), TRUE, TRUE, TRUE, TRUE),
    ('00000000-0000-4000-8000-000000000005', 'Admin User', 'admin@example.com', '081298765432', '$2a$10$somehashedpasswordstring', '789 Admin Rd, City, Country', NULL, TRUE, 'ADMIN', 'local', NULL, NULL, NULL, NULL, NOW(), TRUE, TRUE, TRUE, TRUE);

-- Dummy Data for Businesses
INSERT INTO businesses (id, owner_id, name, description, business_type, has_emergency_services, business_email, business_phone,
                        emergency_phone, business_image_url, certificate_image_url, address, location, operation_hours, status_realtime,
                        is_approved, created_at) VALUES
    ('00000000-0000-4000-8000-000000000006', '00000000-0000-4000-8000-000000000003', 'Intan Grooming House', 'Professional pet grooming services.', 'GROOMING_SALON', FALSE, 'intan.grooming@example.com', '081234567881', NULL, 'https://example.com/images/grooming_house.jpg',
     NULL, '10 Grooming Lane, Pet City', ST_SetSRID(ST_MakePoint(106.8456, -6.2088), 4326), '{"monday": {"open": "09:00", "close": "17:00"}, "tuesday": {"open": "09:00", "close": "17:00"}, "wednesday": {"open": "09:00", "close": "17:00"}, "thursday": {"open": "09:00", "close": "17:00"}, "friday": {"open": "09:00", "close": "17:00"}, "saturday": {"open": "10:00", "close": "14:00"}, "sunday": null}', 'ACCEPTING_PATIENTS', TRUE, NOW()),
    ('00000000-0000-4000-8000-000000000007', '00000000-0000-4000-8000-000000000004', 'Pet Clinic Sehat', 'Comprehensive veterinary services.', 'VETERINARY_CLINIC', TRUE, 'clinic.sehat@example.com', '081298765433', '081298765434', 'https://example.com/images/clinic_sehat.jpg',
     NULL, '20 Clinic Road, Animal Town', ST_SetSRID(ST_MakePoint(106.8000, -6.2000), 4326), '{"monday": {"open": "08:00", "close": "18:00"}, "tuesday": {"open": "08:00", "close": "18:00"}, "wednesday": {"open": "08:00", "close": "18:00"}, "thursday": {"open": "08:00", "close": "18:00"}, "friday": {"open": "08:00", "close": "18:00"}, "saturday": {"open": "09:00", "close": "13:00"}, "sunday": null}', 'ACCEPTING_PATIENTS', TRUE, NOW());

-- Dummy Data for Pets
INSERT INTO pets (id, owner_id, name, species, breed, birth_date, age, gender, image_url, notes, created_at) VALUES
    ('00000000-0000-4000-8000-000000000008', '00000000-0000-4000-8000-000000000001', 'Buddy', 'Dog', 'Golden Retriever', '2022-03-10', 2, 'MALE', 'https://example.com/images/buddy.jpg', 'Loves to play fetch, friendly with other dogs.', NOW()),
    ('00000000-0000-4000-8000-000000000009', '00000000-0000-4000-8000-000000000001', 'Whiskers', 'Cat', 'Siamese', '2023-01-15', 1, 'FEMALE', 'https://example.com/images/whiskers.jpg', 'Enjoys naps and chasing laser pointers.', NOW()),
    ('00000000-0000-4000-8000-000000000010', '00000000-0000-4000-8000-000000000002', 'Rocky', 'Dog', 'German Shepherd', '2021-06-20', 3, 'MALE', 'https://example.com/images/rocky.jpg', 'Very energetic, needs lots of exercise.', NOW());

-- Dummy Data for Services
INSERT INTO services (id, business_id, category, name, base_price, capacity_per_day, image_url, is_active, created_at) VALUES
    ('00000000-0000-4000-8000-000000000011', '00000000-0000-4000-8000-000000000006', 'GROOMING', 'Full Grooming Package (Dog)', 250000.00, 5, 'https://example.com/images/dog_grooming.jpg', TRUE, NOW()),
    ('00000000-0000-4000-8000-000000000012', '00000000-0000-4000-8000-000000000006', 'GROOMING', 'Cat Bath & Brush', 150000.00, 8, 'https://example.com/images/cat_bath.jpg', TRUE, NOW()),
    ('00000000-0000-4000-8000-000000000013', '00000000-0000-4000-8000-000000000007', 'VETERINARY', 'Annual Check-up (Dog)', 300000.00, 10, 'https://example.com/images/vet_checkup.jpg', TRUE, NOW()),
    ('00000000-0000-4000-8000-000000000014', '00000000-0000-4000-8000-000000000007', 'VETERINARY', 'Vaccination (Cat)', 200000.00, 15, 'https://example.com/images/cat_vaccine.jpg', TRUE, NOW());

-- Dummy Data for Products
INSERT INTO products (id, business_id, name, category, description, price, stock_quantity, is_active, image_url, created_at) VALUES
    ('00000000-0000-4000-8000-000000000015', '00000000-0000-4000-8000-000000000006', 'Royal Canin Adult', 'FOOD', 'Complete and balanced food for adult dogs.', 150000.00, 100, TRUE, 'https://example.com/images/royal_canin_adult.jpg', NOW()),
    ('00000000-0000-4000-8000-000000000016', '00000000-0000-4000-8000-000000000006', 'Whiskas Dry Cat Food', 'FOOD', 'Delicious and nutritious dry food for cats.', 75000.00, 150, TRUE, 'https://example.com/images/whiskas_cat_food.jpg', NOW()),
    ('00000000-0000-4000-8000-000000000017', '00000000-0000-4000-8000-000000000007', 'Flea & Tick Collar', 'HEALTH', 'Effective protection against fleas and ticks.', 50000.00, 200, TRUE, 'https://example.com/images/flea_collar.jpg', NOW());

-- Dummy Data for Shopping Carts
INSERT INTO shopping_carts (id, customer_id, business_id, created_at) VALUES
    ('00000000-0000-4000-8000-000000000018', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000006', NOW()),
    ('00000000-0000-4000-8000-000000000019', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000006', NOW());

-- Dummy Data for Cart Items
INSERT INTO cart_items (id, shopping_cart_id, product_id, quantity, created_at) VALUES
    ('00000000-0000-4000-8000-000000000020', '00000000-0000-4000-8000-000000000018', '00000000-0000-4000-8000-000000000015', 2, NOW()),
    ('00000000-0000-4000-8000-000000000021', '00000000-0000-4000-8000-000000000018', '00000000-0000-4000-8000-000000000016', 1, NOW()),
    ('00000000-0000-4000-8000-000000000022', '00000000-0000-4000-8000-000000000019', '00000000-0000-4000-8000-000000000017', 3, NOW());

-- Dummy Data for Orders
INSERT INTO orders (id, customer_id, business_id, order_number, total_amount, status, created_at) VALUES
    ('00000000-0000-4000-8000-000000000023', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000006', 'ORD-20240704-0001', 375000.00, 'PENDING_PAYMENT', NOW()),
    ('00000000-0000-4000-8000-000000000024', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000007', 'ORD-20240704-0002', 150000.00, 'COMPLETED', NOW());

-- Dummy Data for Order Items
INSERT INTO order_items (id, order_id, product_id, quantity, price_per_unit, created_at) VALUES
    ('00000000-0000-4000-8000-000000000025', '00000000-0000-4000-8000-000000000023', '00000000-0000-4000-8000-000000000015', 2, 150000.00, NOW()),
    ('00000000-0000-4000-8000-000000000026', '00000000-0000-4000-8000-000000000023', '00000000-0000-4000-8000-000000000016', 1, 75000.00, NOW()),
    ('00000000-0000-4000-8000-000000000027', '00000000-0000-4000-8000-000000000024', '00000000-0000-4000-8000-000000000017', 3, 50000.00, NOW());

-- Dummy Data for Bookings
INSERT INTO bookings (id, customer_id, pet_id, service_id, booking_number, start_time, end_time, total_price, status, created_at)
VALUES
    ('00000000-0000-4000-8000-000000000037', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000008', '00000000-0000-4000-8000-000000000011', 'BOOK-20240704-0001', '2024-07-10 10:00:00', '2024-07-10 11:00:00', 250000.00, 'COMPLETED', NOW()),
    ('00000000-0000-4000-8000-000000000038', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000010', '00000000-0000-4000-8000-000000000013', 'BOOK-20240704-0002', '2024-07-11 09:00:00', '2024-07-11 10:00:00', 300000.00, 'REQUESTED', NOW());

-- Dummy Data for Prescriptions
INSERT INTO prescriptions (id, pet_id, issuing_business_id, issue_date, notes, created_at) VALUES
    ('00000000-0000-4000-8000-000000000028', '00000000-0000-4000-8000-000000000008', '00000000-0000-4000-8000-000000000007', '2024-07-04', 'Daily medication for allergy symptoms.', NOW()),
    ('00000000-0000-4000-8000-000000000029', '00000000-0000-4000-8000-000000000009', '00000000-0000-4000-8000-000000000007', '2024-06-15', 'Antibiotics for bacterial infection.', NOW());

-- Dummy Data for Prescription Items
INSERT INTO prescription_items (id, prescription_id, medication_name, dosage, frequency, duration_days, instructions) VALUES
    ('00000000-0000-4000-8000-000000000030', '00000000-0000-4000-8000-000000000028', 'Amoxicillin', '250mg', 'Twice daily', 7, 'Give with food to avoid stomach upset.'),
    ('00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000029', 'Doxycycline', '100mg', 'Once daily', 10, 'Administer with a full meal.');

-- Dummy Data for Payments
INSERT INTO payments (id, order_id, booking_id, payment_gateway_ref_id, amount, payment_method, status, snap_token, webhook_payload,
                      created_at, updated_at) VALUES
    ('00000000-0000-4000-8000-000000000032', '00000000-0000-4000-8000-000000000023', NULL, 'MIDTRANS-TRX-123456789', 375000.00,
     'BANK_TRANSFER', 'SETTLEMENT', 'a_midtrans_snap_token_for_payment_1', '{"transaction_time": "2024-07-04 12:00:00", "transaction_status": "settlement", "order_id": "ORD-20240704-0001", "gross_amount": "375000.00"}', NOW(), NOW()),
    ('00000000-0000-4000-8000-000000000033', NULL, '00000000-0000-4000-8000-000000000037', 'MIDTRANS-TRX-987654321', 250000.00,
     'CREDIT_CARD', 'SETTLEMENT', 'a_midtrans_snap_token_for_payment_2', '{"transaction_time": "2024-07-05 10:00:00", "transaction_status": "settlement", "booking_id": "BOOK-20240704-0001", "gross_amount": "250000.00"}', NOW(), NOW());

-- Dummy Data for Reviews
INSERT INTO reviews (id, user_id, business_id, product_id, service_id, rating, comment, created_at) VALUES
    ('00000000-0000-4000-8000-000000000034', '00000000-0000-4000-8000-000000000001', NULL, '00000000-0000-4000-8000-000000000015', NULL, 5, 'My dog loves this food! Great quality and good price.', NOW()),
    ('00000000-0000-4000-8000-000000000035', '00000000-0000-4000-8000-000000000001', NULL, NULL, '00000000-0000-4000-8000-000000000011', 4, 'Grooming service was good, but a bit pricey.', NOW()),
    ('00000000-0000-4000-8000-000000000036', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000007', NULL, NULL, 5, 'Excellent veterinary care, very professional staff.', NOW());