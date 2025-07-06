# Pawtner API Documentation (v2.0 - Complete)

This document provides a complete and detailed reference for all available endpoints in the Pawtner backend API, including concrete examples for request and response bodies.

## 1. Base URL
All API endpoints are prefixed with the base URL of the server. The paths in this document are relative to that base URL.

## 2. Authentication
Access to protected endpoints is controlled via JWT. A valid token must be included in the `Authorization` header for all protected requests, prefixed with `Bearer `.

**Example**: `Authorization: Bearer <your_jwt_token>`

## 3. Common Response Wrapper
All responses (unless specified otherwise) are wrapped in a `CommonResponse` object.

**Structure**
```json
{
    "status": 200,
    "message": "Descriptive message here",
    "data": { ... } // The actual response data
}
```

---

## Authentication (`/api/auth`)

### `POST /api/auth/register/customer`
Registers a new user with the `CUSTOMER` role.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "customer@example.com",
  "phoneNumber": "081234567890",
  "password": "password123",
  "name": "John Doe",
  "address": "123 Main Street, Anytown"
}
```

### `POST /api/auth/register/business-owner`
Registers a new user with the `BUSINESS_OWNER` role.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "owner@example.com",
  "phoneNumber": "089876543210",
  "password": "password123",
  "name": "Jane Smith",
  "address": "456 Business Ave, Anytown"
}
```

### `POST /api/auth/login`
Authenticates a user and returns a JWT.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "customer@example.com",
  "password": "password123"
}
```

**Response Data (`LoginResponseDTO`)**
```json
{
    "userId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "name": "John Doe",
    "email": "customer@example.com",
    "token": "ey...<jwt>..."
}
```

### `POST /api/auth/verify`
Verifies a user's account with a code sent to their email.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "customer@example.com",
  "verificationCode": "123456"
}
```

### `POST /api/auth/resend-verification`
Resends the verification code to the user's email.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "customer@example.com"
}
```

### `POST /api/auth/forgot-password`
Initiates the password reset process.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "email": "customer@example.com"
}
```

### `POST /api/auth/reset-password`
Resets the user's password using a valid token.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "token": "valid-reset-token-from-email-link",
  "newPassword": "newStrongPassword123"
}
```

---

## Users (`/api/users`)

### `GET /api/users`
Retrieves a list of all users.
- **Roles Permitted**: `ADMIN`

### `GET /api/users/{id}`
Retrieves a specific user by their ID.
- **Roles Permitted**: `Authenticated`

### `PUT /api/users`
Updates a user's profile information and profile image.
- **Roles Permitted**: `Authenticated`

**Request Body (`multipart/form-data`)**
- `user` (form-part): JSON string: `{"name": "John Doe Updated", "address": "124 New Street", "phone": "081234567891"}`
- `profileImage` (file-part): The user's profile image file.

### `DELETE /api/users/{id}`
Deletes a user by their ID.
- **Roles Permitted**: `ADMIN`

### `PATCH /api/users/{id}/status`
Updates a user's status (e.g., `isVerified`, `isActive`).
- **Roles Permitted**: `ADMIN`
- **Query Parameters**: `action` (String, e.g., "isVerified"), `value` (Boolean, e.g., "true")

---

## Businesses (`/api/business`)

### `POST /api/business/register`
Creates a new business profile.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `business` (form-part): JSON string for `BusinessRequestDTO`
- `businessImage` (file-part): The business's main image.
- `certificateImage` (file-part): The business's certificate image.

### `GET /api/business`
Retrieves a list of all businesses for admin review.
- **Roles Permitted**: `ADMIN`

### `GET /api/business/my-business`
Retrieves the business profiles owned by the current user.
- **Roles Permitted**: `BUSINESS_OWNER`

### `PATCH /api/business/{id}`
Approves or rejects a business registration.
- **Roles Permitted**: `ADMIN`

**Request Body (`application/json`)**
```json
{
  "approve": true
}
```

---

## Products (`/api/products`)

### `POST /api/products`
Creates a new product.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `businessId`: "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9"
- `name`: "Premium Dog Food"
- `category`: "FOOD"
- `description`: "High-quality dog food for all breeds."
- `price`: 250000
- `stockQuantity`: 100
- `image`: (file)

### `GET /api/products`
Retrieves a paginated list of all products.
- **Roles Permitted**: `Public`

### `GET /api/products/{id}`
Retrieves a specific product by its ID.
- **Roles Permitted**: `Public`

### `PUT /api/products/{id}`
Updates an existing product.
- **Roles Permitted**: `BUSINESS_OWNER`

### `DELETE /api/products/{id}`
Deletes a product.
- **Roles Permitted**: `BUSINESS_OWNER`

---

## Services (`/api/services`)

### `POST /api/services`
Creates a new service offering.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `businessId`: "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9"
- `category`: "GROOMING"
- `name`: "Full Grooming Package"
- `basePrice`: 150000
- `capacityPerDay`: 10
- `image`: (file)

### `GET /api/services`
Retrieves a paginated list of all services.
- **Roles Permitted**: `Public`

### `GET /api/services/{id}`
Retrieves a specific service by its ID.
- **Roles Permitted**: `Public`

### `PUT /api/services/{id}`
Updates an existing service.
- **Roles Permitted**: `BUSINESS_OWNER`

### `DELETE /api/services/{id}`
Deletes a service.
- **Roles Permitted**: `BUSINESS_OWNER`

---

## Shopping Cart (`/api/cart`)

### `POST /api/cart`
Adds an item to the cart. Creates a cart if one doesn't exist for the business.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`application/json`)**
```json
{
  "productId": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "quantity": 2,
  "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6"
}
```

### `GET /api/cart`
Retrieves the current user's shopping cart.
- **Roles Permitted**: `CUSTOMER`

### `PUT /api/cart`
Updates the quantity of an item in the cart.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`application/json`)**
```json
{
  "cartItemId": "d1e2f3a4-b5c6-d7e8-f9a0-b1c2d3e4f5a6",
  "quantity": 3
}
```

### `DELETE /api/cart/{cartItemId}`
Removes a single item from the cart.
- **Roles Permitted**: `CUSTOMER`

### `DELETE /api/cart`
Clears all items from the user's shopping cart.
- **Roles Permitted**: `CUSTOMER`

---

## Orders (`/api/orders`)

### `POST /api/orders/checkout`
Creates a new order from the user's cart and returns a `snap_token` for payment.
- **Roles Permitted**: `CUSTOMER`

**Response Data (`OrderResponseDTO`)**
```json
{
    "id": "o1r2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "orderNumber": "ORDER-ABC-123",
    "customerId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "customerName": "John Doe",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Shop",
    "totalAmount": 500000.00,
    "status": "PENDING",
    "createdAt": "2025-07-05T14:30:00Z",
    "items": [
        {
            "id": "i1t2e3m4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
            "productId": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "productName": "Premium Dog Food",
            "quantity": 2,
            "pricePerUnit": 250000.00,
            "subTotal": 500000.00
        }
    ],
    "snapToken": "...<midtrans-snap-token>..."
}
```

### `GET /api/orders`
Retrieves a paginated list of the current user's orders.
- **Roles Permitted**: `CUSTOMER`

### `GET /api/orders/{order_id}`
Retrieves a specific order by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

### `PUT /api/orders/{order_id}/status`
Updates the status of a specific order.
- **Roles Permitted**: `BUSINESS_OWNER`

**Query Parameters**: `status` (String, e.g., "PROCESSING", "COMPLETED", "CANCELLED")

### `GET /api/orders/business`
Retrieves a paginated list of orders associated with the authenticated business owner's business.
- **Roles Permitted**: `BUSINESS_OWNER`

---

## Bookings (`/api/bookings`)

### `POST /api/bookings`
Creates a new service booking.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`application/json`)**
```json
{
  "petId": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "startTime": "2025-12-24T10:00:00",
  "endTime": "2025-12-26T12:00:00"
}
```

### `GET /api/bookings`
Retrieves a paginated list of bookings for the user/business.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

### `GET /api/bookings/{id}`
Retrieves a specific booking by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

### `PUT /api/bookings/{id}/status`
Updates a booking's status.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`text/plain`)**
```
CONFIRMED
```

### `DELETE /api/bookings/{id}`
Cancels and deletes a booking.
- **Roles Permitted**: `CUSTOMER`, `ADMIN`

---

## Payments (`/api/payments`)

### `POST /api/payments/webhook`
Handles incoming payment notification webhooks from Midtrans.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "transaction_time": "2025-07-05 21:30:10",
  "transaction_status": "settlement",
  "order_id": "ORDER-ABC-123",
  "payment_type": "qris",
  "gross_amount": "500000.00"
}
```

---

## Pets (`/api/pets`)

### `POST /api/pets`
Creates a new pet profile.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`multipart/form-data`)**
- `pet` (form-part): JSON string for `PetRequestDTO` (excluding the image field, which is handled separately)
- `image` (file-part): The pet's image file.

**Example `pet` JSON string:**
```json
{
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "age": 5,
  "gender": "MALE",
  "notes": "Loves to play fetch."
}
```

### `GET /api/pets`
Retrieves a paginated list of the customer's pets.
- **Roles Permitted**: `CUSTOMER`

### `GET /api/pets/{id}`
Retrieves a specific pet by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

### `PUT /api/pets/{id}`
Updates a pet's profile.
- **Roles Permitted**: `CUSTOMER`

### `DELETE /api/pets/{id}`
Deletes a pet's profile.
- **Roles Permitted**: `CUSTOMER`

---

## Reviews (`/api/reviews`)

### `POST /api/reviews`
Creates a new review for a business, product, or service.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`application/json`)**
```json
{
  "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
  "rating": 5,
  "comment": "Excellent service!"
}
```

### `GET /api/reviews`
Retrieves a paginated list of all reviews.
- **Roles Permitted**: `Public`

### `GET /api/reviews/{id}`
Retrieves a specific review by its ID.
- **Roles Permitted**: `Public`

### `PUT /api/reviews/{id}`
Updates an existing review.
- **Roles Permitted**: `CUSTOMER`

### `DELETE /api/reviews/{id}`
Deletes a review.
- **Roles Permitted**: `CUSTOMER`, `ADMIN`

---

## Prescriptions (`/api/prescriptions`)

### `POST /api/prescriptions`
Creates a new prescription for a pet.
- **Roles Permitted**: `BUSINESS_OWNER` (Vet), `ADMIN`

**Request Body (`application/json`)**
```json
{
  "petId": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "issuingBusinessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
  "issueDate": "2025-07-05",
  "notes": "Follow up in 2 weeks.",
  "prescriptionItems": [
    {
      "medicationName": "Amoxicillin",
      "dosage": "250mg",
      "frequency": "Twice a day",
      "durationDays": 10,
      "instructions": "Take with food."
    }
  ]
}
```

### `GET /api/prescriptions`
Retrieves a paginated list of all prescriptions.
- **Roles Permitted**: `BUSINESS_OWNER`, `ADMIN`

### `GET /api/prescriptions/{id}`
Retrieves a specific prescription by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

### `DELETE /api/prescriptions/{id}`
Deletes a prescription.
- **Roles Permitted**: `BUSINESS_OWNER`, `ADMIN`

---

## AI Assistant (`/api/ai`)

### `POST /api/ai/chat`
Sends a message to the Gemini AI assistant for a response.
- **Roles Permitted**: `Authenticated`

**Request Body (`application/json`)**
```json
{
  "message": "What are common symptoms of dog flu?"
}
```

**Response Data (`AiChatResponse`)**
```json
{
  "reply": "Common symptoms of dog flu include coughing, sneezing, fever, and a runny nose..."
}
```
