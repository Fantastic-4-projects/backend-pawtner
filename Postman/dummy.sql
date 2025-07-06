INSERT INTO users (id, name, email, phone_number, password_hash, address, image_url, is_verified, role, auth_provider,
                   code_verification, code_expire, reset_password_token, reset_password_token_expire, created_at, is_account_non_expired,
                   is_account_non_locked, is_credentials_non_expired, is_enabled) VALUES
                                                                                      ('c1d2e3f4-g5h6-7890-1234-567890abcdef', 'Customer User', 'customer@example.com', '081234567890', '$2a$10$somehashedpasswordstring',
                                                                                       '123 Customer St, City, Country', NULL, TRUE, 'CUSTOMER', 'local', NULL, NULL, NULL, NULL, NOW(), TRUE, TRUE, TRUE, TRUE),
                                                                                      ('b1c2d3e4-f5g6-7890-1234-567890abcdef', 'Business Owner User', 'business@example.com', '081212345678',
                                                                                       '$2a$10$somehashedpasswordstring', '456 Business Ave, City, Country', NULL, TRUE, 'BUSINESS_OWNER', 'local', NULL, NULL, NULL, NULL,
                                                                                       NOW(), TRUE, TRUE, TRUE, TRUE),
                                                                                      ('a1b2c3d4-e5f6-7890-1234-567890admin', 'Admin User', 'admin@example.com', '081298765432', '$2a$10$somehashedpasswordstring', '789
      Admin Rd, City, Country', NULL, TRUE, 'ADMIN', 'local', NULL, NULL, NULL, NULL, NOW(), TRUE, TRUE, TRUE, TRUE);
-- Dummy Data for Businesses (owned by Business Owner User)
INSERT INTO businesses (id, owner_id, name, description, business_type, has_emergency_services, business_email, business_phone,
                        emergency_phone, business_image_url, certificate_image_url, address, latitude, longitude, operation_hours, status_realtime,
                        is_approved, created_at) VALUES
    ('b1u2s3i4-n5e6-7890-1234-567890abcdef', 'b1c2d3e4-f5g6-7890-1234-567890abcdef', 'Intan Grooming House', 'Professional pet grooming
      services.', 'GROOMING', FALSE, 'intan.grooming@example.com', '081234567891', NULL, 'https://example.com/images/grooming_house.jpg',
     NULL, '10 Grooming Lane, Pet City', -6.2088, 106.8456, '{"monday": {"open": "09:00", "close": "17:00"}, "tuesday": {"open": "09:00",
      "close": "17:00"}, "wednesday": {"open": "09:00", "close": "17:00"}, "thursday": {"open": "09:00", "close": "17:00"}, "friday":
      {"open": "09:00", "close": "17:00"}, "saturday": {"open": "10:00", "close": "14:00"}, "sunday": null}', 'OPEN', TRUE, NOW());
-- Dummy Data for Pets (owned by Customer User)
INSERT INTO pets (id, owner_id, name, species, breed, birth_date, age, image_url, notes, created_at) VALUES
    ('p1e2t3i4-d5e6-7890-1234-567890abcdef', 'c1d2e3f4-g5h6-7890-1234-567890abcdef', 'Buddy', 'Dog', 'Golden Retriever', '2022-03-10', 2
    , 'https://example.com/images/buddy.jpg', 'Loves to play fetch, friendly with other dogs.', NOW());
-- Dummy Data for Services (offered by Intan Grooming House)
INSERT INTO services (id, business_id, category, name, base_price, capacity_per_day, image_url, is_active, created_at) VALUES
    ('s1e2r3v4-i5c6-7890-1234-567890abcdef', 'b1u2s3i4-n5e6-7890-1234-567890abcdef', 'GROOMING', 'Full Grooming Package (Dog)',
     250000.00, 5, 'https://example.com/images/dog_grooming.jpg', TRUE, NOW());
-- Dummy Data for Products (offered by Intan Grooming House)
INSERT INTO products (id, business_id, name, category, description, price, stock_quantity, is_active, image_url, created_at) VALUES
    ('p1r2o3d4-u5c6-7890-1234-567890abcdef', 'b1u2s3i4-n5e6-7890-1234-567890abcdef', 'Royal Canin Adult', 'FOOD', 'Complete and balanced
      food for adult dogs.', 150000.00, 100, TRUE, 'https://example.com/images/royal_canin_adult.jpg', NOW());
-- Dummy Data for Shopping Carts (for Customer User at Intan Grooming House)
INSERT INTO shopping_carts (id, customer_id, business_id, created_at) VALUES
    ('s1h2o3p4-c5a6-7890-1234-567890abcdef', 'c1d2e3f4-g5h6-7890-1234-567890abcdef', 'b1u2s3i4-n5e6-7890-1234-567890abcdef', NOW());
-- Dummy Data for Cart Items (in the above Shopping Cart)
INSERT INTO cart_items (id, shopping_cart_id, product_id, quantity, created_at) VALUES
    ('c1a2r3t4-i5t6-7890-1234-567890abcdef', 's1h2o3p4-c5a6-7890-1234-567890abcdef', 'p1r2o3d4-u5c6-7890-1234-567890abcdef', 2, NOW());
-- Dummy Data for Orders (by Customer User from Intan Grooming House)
INSERT INTO orders (id, customer_id, business_id, order_number, total_amount, status, created_at) VALUES
    ('o1r2d3e4-r5i6-7890-1234-567890abcdef', 'c1d2e3f4-g5h6-7890-1234-567890abcdef', 'b1u2s3i4-n5e6-7890-1234-567890abcdef',
     'ORD-20240704-0001', 150000.00, 'PENDING_PAYMENT', NOW());
-- Dummy Data for Order Items (for the above Order)
INSERT INTO order_items (id, order_id, product_id, quantity, price_per_unit, created_at) VALUES
    ('i1j2k3l4-m5n6-7890-1234-567890abcdef', 'o1r2d3e4-r5i6-7890-1234-567890abcdef', 'p1r2o3d4-u5c6-7890-1234-567890abcdef', 1,
     150000.00, NOW());
-- Dummy Data for Prescriptions (for Buddy from Intan Grooming House)
INSERT INTO prescriptions (id, pet_id, issuing_business_id, issue_date, notes, created_at) VALUES
    ('p1r2e3s4-c5r6-7890-1234-567890abcdef', 'p1e2t3i4-d5e6-7890-1234-567890abcdef', 'b1u2s3i4-n5e6-7890-1234-567890abcdef',
     '2024-07-04', 'Daily medication for allergy symptoms.', NOW());
-- Dummy Data for Prescription Items (for the above Prescription)
INSERT INTO prescription_items (id, prescription_id, medication_name, dosage, frequency, duration_days, instructions) VALUES
    ('p1i2t3e4-m5i6-7890-1234-567890abcdef', 'p1r2e3s4-c5r6-7890-1234-567890abcdef', 'Amoxicillin', '250mg', 'Twice daily', 7, 'Give
      with food to avoid stomach upset.');
-- Dummy Data for Payments (for the above Order)
INSERT INTO payments (id, order_id, booking_id, payment_gateway_ref_id, amount, payment_method, status, snap_token, webhook_payload,
                      created_at, updated_at) VALUES
    ('p1a2y3m4-e5n6-7890-1234-567890abcdef', 'o1r2d3e4-r5i6-7890-1234-567890abcdef', NULL, 'MIDTRANS-TRX-123456789', 150000.00,
     'BANK_TRANSFER', 'SETTLEMENT', 'a_midtrans_snap_token_for_payment', '{"transaction_time": "2024-07-04 12:00:00",
      "transaction_status": "settlement", "order_id": "ORD-20240704-0001", "gross_amount": "150000.00"}', NOW(), NOW());
-- Dummy Data for Reviews (by Customer User for Royal Canin Adult product)
INSERT INTO reviews (id, user_id, business_id, product_id, service_id, rating, comment, created_at) VALUES ('r1e2v3i4-e5w6-7890-1234-567890abcdef', 'c1d2e3f4-g5h6-7890-1234-567890abcdef', NULL, 'p1r2o3d4-u5c6-7890-1234-567890abcdef', NULL,
                                                                                                            5, 'My dog loves this food! Great quality and good price.', NOW());

-- Dummy Data for Bookings (by Customer User for Full Grooming Package)
INSERT INTO bookings (id, customer_id, pet_id, service_id, booking_number, start_time, end_time, total_price, status, created_at)
VALUES('b1o2o3k4-i5n6-7890-1234-567890abcdef', 'c1d2e3f4-g5h6-7890-1234-567890abcdef', 'p1e2t3i4-d5e6-7890-1234-567890abcdef',
       's1e2r3v4-i5c6-7890-1234-567890abcdef', 'BOOK-20240704-0001', '2024-07-10 10:00:00', '2024-07-10 11:00:00', 75000.00, 'REQUESTED',
       NOW());
