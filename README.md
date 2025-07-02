# Pawtner: The All-in-One Pet Care Platform

A comprehensive digital ecosystem designed to connect pet owners with verified local businesses, simplifying everything from product purchases and service bookings to critical health management.

---

## Table of Contents

- [1. Executive Summary](#1-executive-summary)
- [2. Core Problem Statement](#2-core-problem-statement)
- [3. Proposed Solution & Core Features](#3-proposed-solution--core-features)
- [4. Technical Architecture](#4-technical-architecture)
  - [4.1. Problem-Solution Mapping via Database Design](#41-problem-solution-mapping-via-database-design)
  - [4.2. Complete Database Schema](#42-complete-database-schema)
  - [4.3. Explanation of Key SQL Concepts](#43-explanation-of-key-sql-concepts)
- [5. Core User Workflows](#5-core-user-workflows)
  - [5.1. E-commerce Workflow](#51-e-commerce-workflow-add-to-cart---successful-payment)
  - [5.2. Service Booking Workflow](#52-service-booking-workflow)
  - [5.3. Emergency Assistance Workflow](#53-emergency-assistance-workflow)
- [6. Technical Specifications](#6-technical-specifications)
  - [6.1. Recommended Technology Stack](#61-recommended-technology-stack)
  - [6.2. High-Level API Endpoint Design](#62-high-level-api-endpoint-design)
- [7. MVP Execution Plan](#7-mvp-execution-plan)
- [8. Roadmap Beyond MVP](#8-roadmap-beyond-mvp)

## 1. Executive Summary

**Pawtner** is a digital platform designed to be the ultimate partner for modern pet owners. It addresses critical daily challenges by consolidating pet-related needs—including product commerce, service booking, and health management—into a single, user-friendly mobile application. The platform connects pet owners (Customers) with local pet businesses (Veterinarians, Shops, Daycares) to create a seamless and integrated care ecosystem with **secure, integrated payment processing powered by Midtrans**.

This document outlines the plan for a **Minimum Viable Product (MVP)** to be delivered in a series of focused sprints. The goal is to solve the most pressing user problems and validate the core transactional value proposition of the application.

## 2. Core Problem Statement

The current pet care landscape is fragmented, leading to significant frustrations for pet owners. Pawtner aims to solve the following key issues:

*   **Limited Access to Emergency Assistance:** Difficulty in quickly locating nearby, open veterinary clinics with available emergency contact information during urgent situations.
*   **Fragmented & Insecure E-commerce:** The online purchasing and service booking experience is often clunky, lacks transparent costs, and requires users to leave the app for payment, creating a disjointed and untrustworthy process.
*   **Holiday Boarding Challenges:** During peak seasons, pet boarding facilities are frequently overbooked. Owners face inconsistent service standards, unclear policies, and no real-time availability checks.
*   **Vulnerable Paper-Based Prescriptions:** Reliance on paper prescriptions creates a high risk of loss or damage, jeopardizing a pet's treatment regimen and continuity of care.
*   **Absence of Medication Tracking:** Without a digital system, owners must rely on memory for medication administration, increasing the risk of missed doses and negatively impacting a pet's recovery.
*   **Need for Reliable Advice & Trust:** A gap exists for a quick, trustworthy source of information. Users need to trust businesses, products, and services, which is difficult without community feedback or a clear verification process.

## 3. Proposed Solution & Core Features

Pawtner will address these problems through a suite of interconnected features built upon a robust database architecture.

*   **Feature 1: Verified Local Business Discovery:** A searchable directory of pet businesses with verified badges and precise classifications to build trust and relevance.
*   **Feature 2: Multi-Store Integrated Marketplace:** An in-app marketplace with a separate shopping cart for each business.
*   **Feature 3: Centralized Service Booking:** A robust system for booking services with real-time capacity checks to prevent overbooking.
*   **Feature 4: Digital Pet Health Records & Prescriptions:** Digital pet profiles where vets can issue, and owners can view, permanent and accessible medication histories.
*   **Feature 5: Reviews and Rating System:** A comprehensive review system for businesses, products, and services to build a trusted community.
*   **Feature 6: AI-Powered Assistant (Client-Side):** A chat interface for general queries, escalating serious questions to professionals.
*   **Feature 7: Integrated & Secure Payment Gateway (Midtrans):** A fully integrated payment system allowing users to pay directly within the app, completing the business cycle and solidifying user trust.

## 4. Technical Architecture

### 4.1. Problem-Solution Mapping via Database Design

| Problem | Relevant Tables | Key Columns | Solution Mechanism |
| :--- | :--- | :--- | :--- |
| **Limited Emergency Access** | `businesses` | `business_type`, `has_emergency_services`, `status_realtime`, `latitude`, `longitude` | **Precise filtering.** The system filters for `business_type = 'VETERINARY_CLINIC'` AND `has_emergency_services = TRUE` to instantly identify qualified emergency providers, then checks `status_realtime` to ensure they are open. |
| **Complex E-commerce & Booking** | `shopping_carts`, `orders`, `bookings`, **`payments`** | `payments.snap_token`, `orders.status`, `bookings.status` | **A complete transactional flow.** A user checks out, an order/booking is created, Midtrans is called for a `snap_token`, and a successful payment (via webhook) automatically updates the order status to `processing` or `confirmed`. |
| **Holiday Boarding Difficulty** | `services`, `bookings`, **`payments`** | `services.capacity_per_day`, `bookings.start_time`, `bookings.status` | Real-time availability is checked on booking request. The slot is only truly secured (`status = 'confirmed'`) **after a successful payment** via Midtrans, preventing "ghost" bookings from blocking capacity. |
| **Vulnerable Prescriptions** | `prescriptions`, `prescription_items` | `prescriptions.pet_id`, `prescription_items.medication_name` | Full digitalization of prescriptions, linked to a pet's profile, creating a permanent, secure, and easily accessible record. |
| **Lack of Medication Schedule** | `prescription_items` | `medication_name`, `dosage`, `frequency`, `duration_days` | Provides a detailed, reliable digital reference for each medication, solving the core problem of forgotten details. |
| **Need for Trusted Advice** | `reviews`, `businesses` | `reviews.rating`, `businesses.is_verified` | Users can make informed decisions based on community `reviews`. The `is_verified` badge, backed by an admin workflow, builds a foundational layer of trust. |

---

### 4.2. Complete Database Schema

![image](https://github.com/user-attachments/assets/aa3fac28-85ff-4043-bd13-c558e56ca5d7)

[ERD Link](https://dbdiagram.io/d/ERD-Pawtner-685bc37bf413ba3508cb0af2)

---

```sql
-- Manages user accounts for both customers and business owners
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    address TEXT,
    image_url VARCHAR(255),
    is_verified BOOLEAN DEFAULT false,
    role VARCHAR(50) NOT NULL CHECK (role IN ('business_owner', 'customer')),
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'local',
    provider_id VARCHAR(255),
    code_verification VARCHAR(20),
    code_expire TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (auth_provider, provider_id)
);

-- Contains profiles for all service-providing businesses
CREATE TABLE businesses (
    id SERIAL PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    -- [NEW] Classifies the business for targeted filtering (e.g., show only clinics)
    business_type VARCHAR(50) NOT NULL CHECK (business_type IN ('VETERINARY_CLINIC', 'PET_SHOP', 'GROOMING_SALON', 'BOARDING_DAYCARE', 'HYBRID')),
    -- [NEW] A specific flag for businesses equipped to handle urgent care
    has_emergency_services BOOLEAN DEFAULT false,
    business_email VARCHAR(255) UNIQUE,
    business_phone VARCHAR(50),
    emergency_phone VARCHAR(50),
    business_image_url VARCHAR(255),
    certificate_image_url VARCHAR(255), -- For verification process
    address TEXT,
    latitude DECIMAL(9, 6),
    longitude DECIMAL(9, 6),
    is_verified BOOLEAN DEFAULT false,
    operation_hours JSONB,
    status_realtime VARCHAR(50) DEFAULT 'Closed', -- e.g., 'Accepting Patients', 'At Capacity', 'Closed'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Contains profiles for all customer-owned pets
CREATE TABLE pets (
    id SERIAL PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(100),
    breed VARCHAR(100),
    birth_date DATE,
    image_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- A catalog of all physical products sold by businesses
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    business_id INT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL CHECK (category IN ('food', 'toys', 'accessories', 'health', 'grooming_kit')),
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true, -- Allows hiding a product without deleting it
    image_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- A catalog of all services (e.g., boarding, grooming) offered by businesses
CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    business_id INT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL CHECK (category IN ('boarding', 'daycare', 'grooming', 'veterinary')),
    name VARCHAR(255) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    capacity_per_day INT, -- For real-time availability checks on services like boarding
    is_active BOOLEAN DEFAULT true, -- Allows hiding a service without deleting it
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Represents a user's shopping cart for a specific business
CREATE TABLE shopping_carts (
    id SERIAL PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_id INT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (customer_id, business_id)
);

-- Contains the individual products within a shopping cart
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    cart_id INT NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);

-- Represents a transaction receipt for product purchases
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    business_id INT NOT NULL REFERENCES businesses(id),
    order_number VARCHAR(255) UNIQUE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending_payment' CHECK (status IN ('pending_payment', 'processing', 'shipped', 'completed', 'cancelled', 'failed', 'refunded')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Junction table for many-to-many relationship between orders and products
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL -- Logs the price at the time of sale
);

-- Represents a reservation for a specific service
CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    pet_id INT NOT NULL REFERENCES pets(id),
    service_id INT NOT NULL REFERENCES services(id),
    booking_number VARCHAR(255) UNIQUE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'requested' CHECK (status IN ('requested', 'pending_approval', 'awaiting_payment', 'confirmed', 'cancelled', 'completed')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Records all payment transactions processed via Midtrans
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id) ON DELETE SET NULL,
    booking_id INT REFERENCES bookings(id) ON DELETE SET NULL,
    payment_gateway_ref_id VARCHAR(255) UNIQUE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(100),
    status VARCHAR(50) NOT NULL CHECK (status IN ('pending', 'settlement', 'capture', 'expire', 'failure', 'cancel')),
    snap_token VARCHAR(255),
    webhook_payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_target CHECK ((order_id IS NOT NULL)::int + (booking_id IS NOT NULL)::int = 1)
);

-- A digital record of veterinary prescriptions
CREATE TABLE prescriptions (
    id SERIAL PRIMARY KEY,
    pet_id INT NOT NULL REFERENCES pets(id),
    issuing_business_id INT NOT NULL REFERENCES businesses(id),
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual medication items within a single prescription
CREATE TABLE prescription_items (
    id SERIAL PRIMARY KEY,
    prescription_id INT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255) NOT NULL,
    frequency VARCHAR(255) NOT NULL,
    duration_days INT NOT NULL,
    instructions TEXT
);

-- Reviews for businesses, products, or services
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    business_id INT REFERENCES businesses(id) ON DELETE CASCADE,
    product_id INT REFERENCES products(id) ON DELETE CASCADE,
    service_id INT REFERENCES services(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_review_target CHECK ((business_id IS NOT NULL)::int + (product_id IS NOT NULL)::int + (service_id IS NOT NULL)::int = 1)
);
```

### 4.3. Explanation of Key SQL Concepts

*   **`ON DELETE CASCADE`**: This rule ensures data integrity. For example, if a `user` deletes their account, all their associated `pets` and `bookings` are automatically deleted, preventing orphaned data.
*   **`UNIQUE` Constraints**: These are used to enforce business rules. For example, `UNIQUE (customer_id, business_id)` in `shopping_carts` ensures a user can only have one cart per store.
*   **`CHECK` Constraints**: These enforce valid values for a column, like ensuring a `rating` is between 1 and 5, or that a `business_type` is one of the predefined options.
*   **`chk_payment_target` & `check_review_target`**: These are powerful constraints ensuring that a payment or review record is linked to *exactly one* target entity (e.g., a payment is for an order OR a booking, but never both).

## 5. Core User Workflows

### 5.1. E-commerce Workflow (Add to Cart -> Successful Payment)
1.  **Add to Cart**: User adds a product. Backend manages `shopping_carts` and `cart_items`.
2.  **Checkout**:
    *   User clicks "Checkout". Backend creates an `orders` record with `status = 'pending_payment'`.
    *   Backend calls the **Midtrans API** to create a transaction, receiving a `snap_token`.
    *   Backend creates a `payments` record with `status = 'pending'` and saves the `snap_token`.
    *   The `snap_token` is sent back to the frontend.
3.  **Payment**: Frontend uses the `snap_token` to render the Midtrans Snap payment pop-up. User completes payment.
4.  **Confirmation (Webhook)**:
    *   Midtrans sends a webhook notification to our backend.
    *   Backend verifies the webhook, updates the `payments` status to `'settlement'`, and the `orders` status to `'processing'`.
    *   Finally, it decrements `products.stock_quantity` and clears the user's `shopping_cart`.

### 5.2. Service Booking Workflow
1.  **Availability Check**: User selects a service and dates. Backend validates `services.capacity_per_day` against existing confirmed `bookings`.
2.  **Request Booking**: If available, a `bookings` record is created with `status = 'requested'` or `pending_approval`.
3.  **Payment & Confirmation**: Once approved (if needed), the user is prompted to pay. The flow mirrors e-commerce: call Midtrans, get a `snap_token`, and await the webhook. Upon successful payment, the `bookings` status is changed to `'confirmed'`. The slot is now officially reserved.

### 5.3. Emergency Assistance Workflow
1.  **User Action**: The user taps the "Emergency" button in the app.
2.  **Backend Logic**: The app sends the user's current GPS coordinates to the backend. The backend executes a query to find the most suitable help:
    ```sql
    SELECT name, emergency_phone, address, latitude, longitude
    FROM businesses
    WHERE
        business_type = 'VETERINARY_CLINIC' -- Must be a clinic
        AND has_emergency_services = TRUE     -- Must be equipped for emergencies
        AND is_verified = TRUE                -- Must be a trusted partner
        AND status_realtime != 'Closed'       -- Must be currently open
    ORDER BY
        -- A function to calculate distance from user's location
        distance(latitude, longitude, user_lat, user_lon) ASC
    LIMIT 5;
    ```
3.  **Result**: The frontend receives a list of the 5 closest, open, and verified emergency clinics, allowing the user to make an informed call immediately.

## 6. Technical Specifications

### 6.1. Recommended Technology Stack
| Component | Technology | Rationale |
|---|---|---|
| **Database** | PostgreSQL | Robust, scalable, and excellent support for geospatial queries (PostGIS) and JSONB. |
| **Backend** | Go (Golang) or Java (Spring Boot) | High performance, strong typing, and excellent concurrency for handling many requests. |
| **Frontend (Mobile)** | React Native | Cross-platform development to reach both iOS and Android users with a single codebase. |
| **Authentication** | JWT (JSON Web Tokens) | Stateless, secure, and widely supported standard for API authentication. |
| **Payment Gateway** | **Midtrans** | Secure, reliable, well-documented API, and handles the complexity of multiple payment methods. |

### 6.2. High-Level API Endpoint Design
- `AuthController`: `/api/auth/register`, `/api/auth/login`
- `BusinessController`: `GET /api/businesses`, `GET /api/businesses/emergency`
- `ProductController`: `GET /api/businesses/{id}/products`
- `CartController`: `GET /api/cart`, `POST /api/cart/items`
- `OrderController`: `POST /api/orders/checkout`
- **`PaymentController`**:
    - `POST /api/orders/{orderId}/pay` -> Returns a `snap_token`.
    - `POST /api/bookings/{bookingId}/pay` -> Returns a `snap_token`.
    - `POST /api/payments/webhook` -> Public endpoint for Midtrans notifications.

## 7. MVP Execution Plan

To ensure high quality, the MVP will focus on validating **one core business cycle: E-commerce End-to-End**.

**Sprint 0 (Week 0): Foundation & Setup**
- **Goal**: Prepare the development environment and infrastructure.
- **Tasks**: Setup Git repository, configure CI/CD pipeline, provision database and servers, establish local development environment for all team members.

**Sprint 1 (Weeks 1-2): Backend Foundation & Core E-commerce Logic**
- **Goal**: Build the complete backend logic for a user to buy a product.
- **Tasks**: Implement all database entities, configure security (JWT), implement logic for `Carts` & `Orders`, and **critically, implement the `PaymentService` to integrate with the Midtrans API (create transaction, handle webhook).**

**Sprint 2 (Weeks 3-4): Frontend Development & Integration**
- **Goal**: Create the user-facing application for the e-commerce flow and connect it to the backend.
- **Tasks**:
    - **React Native (Customer App)**: Build screens for Login, Product Discovery, Cart, and **implement the Checkout screen with Midtrans Snap.js integration.**
    - **Web (Business Dashboard)**: Build screens for Login, Business/Product Management, and viewing incoming orders.
    - **Integration**: Connect all frontend components to the backend APIs.

**Sprint 3 (Week 5): Testing, Refinement & Deployment**
- **Goal**: Ensure the application is stable, bug-free, and ready for deployment.
- **Tasks**: End-to-end testing of the entire user journey. **Crucially, test the payment flow thoroughly using the Midtrans Sandbox and webhook simulator.** Bug fixing, UI polishing, and deployment.

## 8. Roadmap Beyond MVP

### Phase 2: Service Expansion
-   **Service Booking System**: Implement the full booking workflow, leveraging the already-built payment logic.
-   **Digital Prescriptions**: Build the interface for Vets to issue, and customers to view, digital prescriptions.
-   **Enhanced Reviews**: Add features like replies to reviews and content flagging.

### Future Enhancements
-   **AI-Powered Assistant**: Implement the client-side AI chat for general advice.
-   **Business Intelligence Dashboard**: Provide analytics for business owners on sales and booking trends.
-   **Advanced Logistics**: Integrate with shipping APIs for real-time delivery tracking.
-   **Push Notifications**: Implement reminders for medication, appointments, and order status updates.
-   **Pet Profiles & Social Features**: Allow users to connect and share experiences.

### Data Model & Core Entities:
The application's functionality is built around a set of core data entities:

*   **`User`**: Represents an individual who can log in to the platform. Each user has one of three roles (`ADMIN`, `business_owner`, `customer`) that dictates their permissions.
*   **`Business`**: Represents a business profile created and managed by a `business_owner`. It contains all relevant information, such as name, address, contact details, and services offered.
*   **`Product`**: An item or good sold by a `Business`. Each product has details like name, price, stock quantity, and an image.
*   **`ServiceOffering`**: A service (e.g., grooming, veterinary check-up) offered by a `Business`. It includes details like base price and capacity.
*   **`Pet`**: A profile for a pet, created and managed by a `customer`. It stores information like name, species, and breed.
*   **`Order`**: A record of a `customer`'s purchase of one or more `Product` items from a single `Business`.
*   **`Booking`**: A record of a `customer`'s appointment for a `ServiceOffering` provided by a `Business`.
*   **`Payment`**: The financial transaction record for an `Order` or `Booking`. It is integrated with the Midtrans payment gateway and tracks the payment status.
*   **`Review`**: Feedback provided by a `customer` for a specific `Business`, `Product`, or `ServiceOffering`.
*   **`ShoppingCart`**: A temporary container for `Product` items that a `customer` intends to purchase from a `Business`.

### User Roles & Permissions:
*   **`ADMIN`**: The project owner with full administrative access. This role is for system maintenance and oversight. The default admin user is created via a database seed (`admin@example.com` / `admin123`) and cannot be registered through the public API.
*   **`business_owner`**: A user who owns and manages businesses. They can create and update their business profile, add/remove products and services, and manage incoming orders and bookings.
*   **`customer`**: A regular user (pet owner). They can browse businesses, purchase products, book services, manage their pets, and write reviews. **All new users registered via the public API are automatically assigned this role.**

## 2. Authentication & Authorization

Access to the API is controlled via JWT (JSON Web Tokens).

1.  A user (of any role) submits their credentials to the `POST /api/auth/login` endpoint.
2.  On successful authentication, the server returns a JWT.
3.  This token must be included in the `Authorization` header for all subsequent requests to protected endpoints, prefixed with `Bearer `.
    *   **Example**: `Authorization: Bearer <your_jwt_token>`

Authorization is role-based. Most endpoints are protected and require a specific role for access, which is enforced by Spring Security. The "Roles Permitted" column in the endpoint reference below specifies the required role(s).

## 3. API Endpoint Reference

This section provides a detailed list of all API endpoints with their full, absolute paths.

---

### **Authentication (`/api/auth`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registers a new user. All new registrations default to the `customer` role. | Public |
| `POST` | `/api/auth/login` | Authenticates a user and returns a JWT. | Public |
| `GET` | `/api/auth/verify` | Verifies a user's account using a code sent to their email. | Public |
| `GET` | `/oauth2/authorization/google` | Initiates the Google OAuth2 login flow. | Public |
| `GET` | `/api/auth/oauth2/success` | Callback endpoint for successful Google OAuth2 login. | Public |

---

### **Businesses (`/api/businesses`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/businesses` | Creates a new business profile. | `business_owner` |
| `GET` | `/api/businesses` | Retrieves a paginated list of all businesses. | Authenticated |
| `GET` | `/api/businesses/{id}` | Retrieves a specific business by its ID. | Authenticated |
| `PUT` | `/api/businesses/{id}` | Updates an existing business profile. | `business_owner` |
| `DELETE` | `/api/businesses/{id}` | Deletes a business. | `ADMIN`, `business_owner` |
| `GET` | `/api/businesses/emergency` | Retrieves businesses with emergency services, sorted by proximity. | Authenticated |

---

### **Products (`/api/products`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/products` | Creates a new product with an image. (Multipart request) | `business_owner` |
| `GET` | `/api/products` | Retrieves a paginated list of all products. | Authenticated |
| `GET` | `/api/products/{id}` | Retrieves a specific product by its ID. | Authenticated |
| `PUT` | `/api/products/{id}` | Updates a product, with an option to upload a new image. (Multipart request) | `business_owner` |
| `DELETE` | `/api/products/{id}` | Deletes a product. | `business_owner` |

---

### **Services (`/api/services`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/services` | Creates a new service offering. | `business_owner` |
| `GET` | `/api/services` | Retrieves a paginated list of all services. | Authenticated |
| `GET` | `/api/services/{id}` | Retrieves a specific service by its ID. | Authenticated |
| `PUT` | `/api/services/{id}` | Updates an existing service. | `business_owner` |
| `DELETE` | `/api/services/{id}` | Deletes a service. | `business_owner` |

---

### **Shopping Cart (`/api/carts`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/carts` | Gets or creates a shopping cart for the logged-in customer. | `customer` |
| `POST` | `/api/carts/{cartId}/items` | Adds a product to the specified shopping cart. | `customer` |
| `DELETE` | `/api/carts/{cartId}/items/{cartItemId}` | Removes a single item from the cart. | `customer` |
| `DELETE` | `/api/carts/{cartId}/clear` | Clears all items from the cart. | `customer` |

---

### **Orders (`/api/orders`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/orders` | Creates a new order from cart items and initiates payment. | `customer` |
| `GET` | `/api/orders` | Retrieves a paginated list of orders. | Authenticated |
| `GET` | `/api/orders/{id}` | Retrieves a specific order by its ID. | Authenticated |
| `PUT` | `/api/orders/{id}` | Updates an order's status (e.g., to 'shipped'). | `business_owner` |
| `DELETE` | `/api/orders/{id}` | Deletes an order. | `ADMIN`, `business_owner` |

---

### **Bookings (`/api/bookings`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/bookings` | Creates a new service booking and initiates payment. | `customer` |
| `GET` | `/api/bookings` | Retrieves a paginated list of bookings. | Authenticated |
| `GET` | `/api/bookings/{id}` | Retrieves a specific booking by its ID. | Authenticated |
| `PUT` | `/api/bookings/{id}` | Updates a booking's status (e.g., to 'confirmed'). | `business_owner` |
| `DELETE` | `/api/bookings/{id}` | Deletes a booking. | `ADMIN`, `business_owner` |

---

### **Payments (`/api/payments`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/payments/order/{orderId}` | Creates a payment transaction for a given order. | `customer` |
| `POST` | `/api/payments/booking/{bookingId}` | Creates a payment transaction for a given booking. | `customer` |
| `POST` | `/api/payments/webhook` | Handles incoming payment notification webhooks from Midtrans. | Public |
| `GET` | `/api/payments/{id}` | Retrieves payment details by ID. | Authenticated |

---

### **Pets (`/api/pets`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/pets` | Creates a new pet profile for the logged-in customer. | `customer` |
| `GET` | `/api/pets` | Retrieves a paginated list of the customer's pets. | `customer` |
| `GET` | `/api/pets/{id}` | Retrieves a specific pet by its ID. | `customer` |
| `PUT` | `/api/pets/{id}` | Updates a pet's profile. | `customer` |
| `DELETE` | `/api/pets/{id}` | Deletes a pet's profile. | `customer` |

---

### **Reviews (`/api/reviews`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/reviews` | Creates a new review for a business, product, or service. | `customer` |
| `GET` | `/api/reviews` | Retrieves a paginated list of all reviews. | Authenticated |
| `GET` | `/api/reviews/{id}` | Retrieves a specific review by its ID. | Authenticated |
| `PUT` | `/api/reviews/{id}` | Updates a review. | `customer` |
| `DELETE` | `/api/reviews/{id}` | Deletes a review. | `ADMIN`, `customer` |

---

### **Prescriptions (`/api/prescriptions`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/prescriptions` | Creates a new prescription for a pet. | `business_owner` (Vet) |
| `GET` | `/api/prescriptions` | Retrieves a paginated list of prescriptions. | Authenticated |
| `GET` | `/api/prescriptions/{id}` | Retrieves a specific prescription by its ID. | Authenticated |
| `PUT` | `/api/prescriptions/{id}` | Updates a prescription. | `business_owner` (Vet) |
| `DELETE` | `/api/prescriptions/{id}` | Deletes a prescription. | `business_owner` (Vet) |

---

### **Prescription Items (`/api/prescription-items`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `POST` | `/api/prescription-items` | Creates a new item within a prescription. | `business_owner` (Vet) |
| `GET` | `/api/prescription-items` | Retrieves a paginated list of all prescription items. | Authenticated |
| `GET` | `/api/prescription-items/{id}` | Retrieves a specific prescription item by its ID. | Authenticated |
| `PUT` | `/api/prescription-items/{id}` | Updates a prescription item. | `business_owner` (Vet) |
| `DELETE` | `/api/prescription-items/{id}` | Deletes a prescription item. | `business_owner` (Vet) |

---

### **Test (`/api/test`)**

| Method | Full Endpoint | Description | Roles Permitted |
|---|---|---|---|
| `GET` | `/api/test/protected` | A simple protected endpoint to verify that a JWT is valid. | Authenticated |# trigger test

## 9. Additional Business Logic & Features

This section outlines specific business logic and features implemented or planned for the Pawtner platform, serving as a quick reference for development.

### 9.1. Platform Fee Implementation

*   **Concept**: Pawtner will implement a platform fee, which is a commission or percentage taken from each successful transaction (product sales or service bookings) facilitated through the platform.
*   **Mechanism**: This will be primarily handled via **Midtrans Split Payment**. When a customer makes a payment, Midtrans will automatically disburse a predefined percentage (the platform fee) to Pawtner's account, and the remaining amount to the respective business owner's account. This simplifies reconciliation and ensures Pawtner's revenue stream.
*   **Impact**: The total amount paid by the customer remains unchanged; the fee is deducted from the business's payout.

### 9.2. Delivery Fee & Mechanism

*   **Delivery Fee**: A fixed delivery fee of **Rp 10,000** will be applied per applicable order (e.g., product delivery). This fee is added to the customer's total payment.
*   **Delivery Mechanism**: For product deliveries and home-service bookings (non-boarding), the **businesses themselves are responsible for managing their own deliveries**. Pawtner's role is to facilitate the order/booking and payment, providing the necessary address information to the business. Pawtner does not manage the logistics or provide delivery personnel.

### 9.3. Address Handling for Home Services

*   **Source of Address**: For services or product deliveries that require a physical address (i.e., not boarding/in-store services), the delivery/service address will be automatically retrieved from the **user's registered address data** in their profile. This ensures convenience for the customer and provides the necessary information to the business for delivery/service provision.

### 9.4. "Petshop Nearby" Feature

*   **Concept**: The mobile application will include a "Petshop Nearby" feature, allowing users to discover pet shops and products within their vicinity.
*   **Functionality**: 
    *   Users can tap a dedicated button to find nearby businesses.
    *   The system will use the user's current location (via GPS) or allow them to **manually "pin point" an area on a map**.
    *   **Backend Logic**: The backend will perform spatial queries (using latitude and longitude data stored for businesses) to filter and return businesses and their products that fall within a specified radius of the user's or pinned location. This ensures that only relevant local options are displayed to the user.
    *   **Filtering**: This feature will enable filtering of businesses and products based on their geographical proximity, enhancing the user's shopping and service discovery experience.

