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

**Response Data (`RegisterResponseDTO`)**
```json
{
    "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "email": "customer@example.com",
    "name": "John Doe",
    "phoneNumber": "081234567890",
    "address": "123 Main Street, Anytown",
    "imageUrl": null,
    "isVerified": false,
    "role": "CUSTOMER"
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

**Response Data (`RegisterResponseDTO`)**
```json
{
    "id": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "email": "owner@example.com",
    "name": "Jane Smith",
    "phoneNumber": "089876543210",
    "address": "456 Business Ave, Anytown",
    "imageUrl": null,
    "isVerified": false,
    "role": "BUSINESS_OWNER"
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

### `PATCH /api/auth/user/set-role`
Sets the role of a user.
- **Roles Permitted**: `Public` (Service-level authorization might apply)

**Request Body (`application/json`)**
```json
{
  "email": "user@example.com",
  "role": "BUSINESS_OWNER"
}
```

**Response Data (`UserRole`)**
```json
"BUSINESS_OWNER"
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

**Response Data (`String`)**
```json
null
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

**Response Data (`String`)**
```json
null
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

**Response Data (`String`)**
```json
null
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

**Response Data (`String`)**
```json
null
```

---

## Bookings (`/api/bookings`)

### `POST /api/bookings`
Creates a new service booking, including calculation of delivery fee based on user and business location. Returns a `snap_token` for payment.
- **Roles Permitted**: `CUSTOMER`

**Request Body (`application/json`)**
```json
{
  "petId": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "startTime": "2025-12-24T10:00:00",
  "endTime": "2025-12-26T12:00:00",
  "latitude": -6.200000,  // User's latitude for delivery calculation
  "longitude": 106.816666 // User's longitude for delivery calculation
}
```

**Response Data (`BookingResponseDTO`)**
```json
{
    "id": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customer": {
        "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
        "name": "John Doe",
        "email": "customer@example.com",
        "address": "123 Main Street, Anytown",
        "phone": "081234567890",
        "imageUrl": null
    },
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy",
        "species": "Dog",
        "breed": "Golden Retriever",
        "age": 5,
        "gender": "MALE",
        "imageUrl": null,
        "notes": "Loves to play fetch.",
        "ownerName": "John Doe"
    },
    "petName": "Buddy",
    "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "serviceName": "Full Grooming Package",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Grooming",
    "bookingNumber": "BOOK-ABC-123",
    "startTime": "2025-12-24T10:00:00",
    "endTime": "2025-12-26T12:00:00",
    "totalPrice": 150000.00,
    "status": "PENDING_PAYMENT",
    "snapToken": "...<midtrans-snap-token>...",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `POST /api/bookings/webhook`
Handles incoming payment notification webhooks from Midtrans for bookings.
- **Roles Permitted**: `Public`

**Request Body (`application/json`)**
```json
{
  "transaction_time": "2025-07-05 21:30:10",
  "transaction_status": "settlement",
  "order_id": "BOOK-ABC-123",
  "payment_type": "qris",
  "gross_amount": "150000.00"
}
```

**Response Data (`String`)**
```json
null
```

### `GET /api/bookings/{id}`
Retrieves a specific booking by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

**Path Variable**: `id` (UUID)

**Response Data (`BookingResponseDTO`)**
```json
{
    "id": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customer": {
        "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
        "name": "John Doe",
        "email": "customer@example.com",
        "address": "123 Main Street, Anytown",
        "phone": "081234567890",
        "imageUrl": null
    },
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy",
        "species": "Dog",
        "breed": "Golden Retriever",
        "age": 5,
        "gender": "MALE",
        "imageUrl": null,
        "notes": "Loves to play fetch.",
        "ownerName": "John Doe"
    },
    "petName": "Buddy",
    "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "serviceName": "Full Grooming Package",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Grooming",
    "bookingNumber": "BOOK-ABC-123",
    "startTime": "2025-12-24T10:00:00",
    "endTime": "2025-12-26T12:00:00",
    "totalPrice": 150000.00,
    "status": "PENDING_PAYMENT",
    "snapToken": "...<midtrans-snap-token>...",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/bookings`
Retrieves a paginated list of bookings for the user/business.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<BookingResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "customer": {
                "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
                "name": "John Doe",
                "email": "customer@example.com",
                "address": "123 Main Street, Anytown",
                "phone": "081234567890",
                "imageUrl": null
            },
            "pet": {
                "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
                "name": "Buddy",
                "species": "Dog",
                "breed": "Golden Retriever",
                "age": 5,
                "gender": "MALE",
                "imageUrl": null,
                "notes": "Loves to play fetch.",
                "ownerName": "John Doe"
            },
            "petName": "Buddy",
            "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "serviceName": "Full Grooming Package",
            "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
            "businessName": "Pawtner Pet Grooming",
            "bookingNumber": "BOOK-ABC-123",
            "startTime": "2025-12-24T10:00:00",
            "endTime": "2025-12-26T12:00:00",
            "totalPrice": 150000.00,
            "status": "PENDING_PAYMENT",
            "snapToken": "...<midtrans-snap-token>...",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/bookings/{id}/status`
Updates a booking's status.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `id` (UUID)

**Request Body (`text/plain`)**
```
CONFIRMED
```

**Response Data (`BookingResponseDTO`)**
```json
{
    "id": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customer": {
        "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
        "name": "John Doe",
        "email": "customer@example.com",
        "address": "123 Main Street, Anytown",
        "phone": "081234567890",
        "imageUrl": null
    },
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy",
        "species": "Dog",
        "breed": "Golden Retriever",
        "age": 5,
        "gender": "MALE",
        "imageUrl": null,
        "notes": "Loves to play fetch.",
        "ownerName": "John Doe"
    },
    "petName": "Buddy",
    "serviceId": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "serviceName": "Full Grooming Package",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Grooming",
    "bookingNumber": "BOOK-ABC-123",
    "startTime": "2025-12-24T10:00:00",
    "endTime": "2025-12-26T12:00:00",
    "totalPrice": 150000.00,
    "status": "CONFIRMED",
    "snapToken": "...<midtrans-snap-token>...",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `DELETE /api/bookings/{id}`
Cancels and deletes a booking.
- **Roles Permitted**: `CUSTOMER`, `ADMIN`

**Path Variable**: `id` (UUID)

**Response Data (`Void`)**
```json
null
```

---

## Businesses (`/api/business`)

### `POST /api/business/register`
Registers a new business profile.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `business` (form-part): JSON string for `BusinessRequestDTO`
- `businessImage` (file-part): The business's main image.
- `certificateImage` (file-part): The business's certificate image.

**Example `business` JSON string:**
```json
{
  "nameBusiness": "Pawtner Pet Shop",
  "descriptionBusiness": "One-stop shop for all your pet needs.",
  "businessAddress": "123 Pet Street, Anytown",
  "businessType": "PET_SHOP",
  "hasEmergencyServices": false,
  "businessEmail": "shop@pawtner.com",
  "businessPhone": "081122334455",
  "emergencyPhone": null,
  "latitude": -6.200000,
  "longitude": 106.816666,
  "businessStatus": "OPEN",
  "operationHours": {
    "MONDAY": { "open": "09:00", "close": "17:00" },
    "TUESDAY": { "open": "09:00", "close": "17:00" },
    "WEDNESDAY": { "open": "09:00", "close": "17:00" },
    "THURSDAY": { "open": "09:00", "close": "17:00" },
    "FRIDAY": { "open": "09:00", "close": "17:00" },
    "SATURDAY": { "open": "10:00", "close": "16:00" },
    "SUNDAY": null
  }
}
```

**Response Data (`BusinessResponseDTO`)**
```json
null
```

### `GET /api/business/{businessId}`
Retrieves a specific business by its ID.
- **Roles Permitted**: `Public`

**Path Variable**: `businessId` (UUID)

**Response Data (`BusinessResponseDTO`)**
```json
{
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "ownerName": "Jane Smith",
    "businessName": "Pawtner Pet Shop",
    "description": "One-stop shop for all your pet needs.",
    "businessType": "PET_SHOP",
    "hasEmergencyServices": false,
    "businessEmail": "shop@pawtner.com",
    "businessPhone": "081122334455",
    "emergencyPhone": null,
    "businessImageUrl": "http://example.com/business_image.jpg",
    "certificateImageUrl": "http://example.com/certificate_image.jpg",
    "latitude": -6.200000,
    "longitude": 106.816666,
    "statusRealTime": "OPEN",
    "businessAddress": "123 Pet Street, Anytown",
    "operationHours": {
        "MONDAY": { "open": "09:00", "close": "17:00" },
        "TUESDAY": { "open": "09:00", "close": "17:00" },
        "WEDNESDAY": { "open": "09:00", "close": "17:00" },
        "THURSDAY": { "open": "09:00", "close": "17:00" },
        "FRIDAY": { "open": "09:00", "close": "17:00" },
        "SATURDAY": { "open": "10:00", "close": "16:00" },
        "SUNDAY": null
    },
    "statusApproved": "Approved"
}
```

### `PUT /api/business/{businessId}/update`
Updates an existing business profile.
- **Roles Permitted**: `Authenticated` (Service-level authorization might apply)

**Path Variable**: `businessId` (UUID)

**Request Body (`multipart/form-data`)**
- `business` (form-part): JSON string for `BusinessRequestDTO`
- `businessImage` (file-part): The business's main image.
- `certificateImage` (file-part): The business's certificate image.

**Example `business` JSON string:**
```json
{
  "nameBusiness": "Pawtner Pet Shop Updated",
  "descriptionBusiness": "Updated one-stop shop for all your pet needs.",
  "businessAddress": "456 New Pet Street, Anytown",
  "businessType": "PET_SHOP",
  "hasEmergencyServices": true,
  "businessEmail": "updated_shop@pawtner.com",
  "businessPhone": "089988776655",
  "emergencyPhone": "081234567890",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "businessStatus": "CLOSED",
  "operationHours": {
    "MONDAY": { "open": "08:00", "close": "18:00" },
    "TUESDAY": { "open": "08:00", "close": "18:00" },
    "WEDNESDAY": { "open": "08:00", "close": "18:00" },
    "THURSDAY": { "open": "08:00", "close": "18:00" },
    "FRIDAY": { "open": "08:00", "close": "18:00" },
    "SATURDAY": { "open": "09:00", "close": "17:00" },
    "SUNDAY": null
  }
}
```

**Response Data (`BusinessResponseDTO`)**
```json
{
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "ownerName": "Jane Smith",
    "businessName": "Pawtner Pet Shop Updated",
    "description": "Updated one-stop shop for all your pet needs.",
    "businessType": "PET_SHOP",
    "hasEmergencyServices": true,
    "businessEmail": "updated_shop@pawtner.com",
    "businessPhone": "089988776655",
    "emergencyPhone": "081234567890",
    "businessImageUrl": "http://example.com/updated_business_image.jpg",
    "certificateImageUrl": "http://example.com/updated_certificate_image.jpg",
    "latitude": -6.200000,
    "longitude": 106.816666,
    "statusRealTime": "CLOSED",
    "businessAddress": "456 New Pet Street, Anytown",
    "operationHours": {
        "MONDAY": { "open": "08:00", "close": "18:00" },
        "TUESDAY": { "open": "08:00", "close": "18:00" },
        "WEDNESDAY": { "open": "08:00", "close": "18:00" },
        "THURSDAY": { "open": "08:00", "close": "18:00" },
        "FRIDAY": { "open": "08:00", "close": "18:00" },
        "SATURDAY": { "open": "09:00", "close": "17:00" },
        "SUNDAY": null
    },
    "statusApproved": "Approved"
}
```

### `PATCH /api/business/{id}`
Approves or rejects a business registration.
- **Roles Permitted**: `ADMIN`

**Path Variable**: `id` (UUID)

**Request Body (`application/json`)**
```json
{
  "approve": true
}
```

**Response Data (`BusinessResponseDTO`)**
```json
{
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "ownerName": "Jane Smith",
    "businessName": "Pawtner Pet Shop",
    "description": "One-stop shop for all your pet needs.",
    "businessType": "PET_SHOP",
    "hasEmergencyServices": false,
    "businessEmail": "shop@pawtner.com",
    "businessPhone": "081122334455",
    "emergencyPhone": null,
    "businessImageUrl": "http://example.com/business_image.jpg",
    "certificateImageUrl": "http://example.com/certificate_image.jpg",
    "latitude": -6.200000,
    "longitude": 106.816666,
    "statusRealTime": "OPEN",
    "businessAddress": "123 Pet Street, Anytown",
    "operationHours": {
        "MONDAY": { "open": "09:00", "close": "17:00" },
        "TUESDAY": { "open": "09:00", "close": "17:00" },
        "WEDNESDAY": { "open": "09:00", "close": "17:00" },
        "THURSDAY": { "open": "09:00", "close": "17:00" },
        "FRIDAY": { "open": "09:00", "close": "17:00" },
        "SATURDAY": { "open": "10:00", "close": "16:00" },
        "SUNDAY": null
    },
    "statusApproved": "Approved"
}
```

### `GET /api/business`
Retrieves a list of all businesses for admin review.
- **Roles Permitted**: `ADMIN`

**Response Data (`List<BusinessResponseDTO>`)**
```json
[
    {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "ownerName": "Jane Smith",
        "businessName": "Pawtner Pet Shop",
        "description": "One-stop shop for all your pet needs.",
        "businessType": "PET_SHOP",
        "hasEmergencyServices": false,
        "businessEmail": "shop@pawtner.com",
        "businessPhone": "081122334455",
        "emergencyPhone": null,
        "businessImageUrl": "http://example.com/business_image.jpg",
        "certificateImageUrl": "http://example.com/certificate_image.jpg",
        "latitude": -6.200000,
        "longitude": 106.816666,
        "statusRealTime": "OPEN",
        "businessAddress": "123 Pet Street, Anytown",
        "operationHours": {
            "MONDAY": { "open": "09:00", "close": "17:00" },
            "TUESDAY": { "open": "09:00", "close": "17:00" },
            "WEDNESDAY": { "open": "09:00", "close": "17:00" },
            "THURSDAY": { "open": "09:00", "close": "17:00" },
            "FRIDAY": { "open": "09:00", "close": "17:00" },
            "SATURDAY": { "open": "10:00", "close": "16:00" },
            "SUNDAY": null
        },
        "statusApproved": "Approved"
    }
]
```

### `GET /api/business/my-business`
Retrieves the business profiles owned by the current user.
- **Roles Permitted**: `Authenticated` (Service-level authorization might apply)

**Response Data (`List<BusinessResponseDTO>`)**
```json
[
    {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "ownerName": "Jane Smith",
        "businessName": "Pawtner Pet Shop",
        "description": "One-stop shop for all your pet needs.",
        "businessType": "PET_SHOP",
        "hasEmergencyServices": false,
        "businessEmail": "shop@pawtner.com",
        "businessPhone": "081122334455",
        "emergencyPhone": null,
        "businessImageUrl": "http://example.com/business_image.jpg",
        "certificateImageUrl": "http://example.com/certificate_image.jpg",
        "latitude": -6.200000,
        "longitude": 106.816666,
        "statusRealTime": "OPEN",
        "businessAddress": "123 Pet Street, Anytown",
        "operationHours": {
            "MONDAY": { "open": "09:00", "close": "17:00" },
            "TUESDAY": { "open": "09:00", "close": "17:00" },
            "WEDNESDAY": { "open": "09:00", "close": "17:00" },
            "THURSDAY": { "open": "09:00", "close": "17:00" },
            "FRIDAY": { "open": "09:00", "close": "17:00" },
            "SATURDAY": { "open": "10:00", "close": "16:00" },
            "SUNDAY": null
        },
        "statusApproved": "Approved"
    }
]
```

### `PATCH /api/business/{businessId}/toggle-open`
Toggles the real-time status of a business (open/closed).
- **Roles Permitted**: `Authenticated` (Service-level authorization might apply)

**Path Variable**: `businessId` (UUID)

**Request Body (`application/json`)**
```json
{
  "businessStatus": "OPEN"
}
```

**Response Data (`BusinessResponseDTO`)**
```json
{
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "ownerName": "Jane Smith",
    "businessName": "Pawtner Pet Shop",
    "description": "One-stop shop for all your pet needs.",
    "businessType": "PET_SHOP",
    "hasEmergencyServices": false,
    "businessEmail": "shop@pawtner.com",
    "businessPhone": "081122334455",
    "emergencyPhone": null,
    "businessImageUrl": "http://example.com/business_image.jpg",
    "certificateImageUrl": "http://example.com/certificate_image.jpg",
    "latitude": -6.200000,
    "longitude": 106.816666,
    "statusRealTime": "OPEN",
    "businessAddress": "123 Pet Street, Anytown",
    "operationHours": {
        "MONDAY": { "open": "09:00", "close": "17:00" },
        "TUESDAY": { "open": "09:00", "close": "17:00" },
        "WEDNESDAY": { "open": "09:00", "close": "17:00" },
        "THURSDAY": { "open": "09:00", "close": "17:00" },
        "FRIDAY": { "open": "09:00", "close": "17:00" },
        "SATURDAY": { "open": "10:00", "close": "16:00" },
        "SUNDAY": null
    },
    "statusApproved": "Approved"
}
```

### `DELETE /api/business/{businessId}/delete`
Deletes a business profile.
- **Roles Permitted**: `Authenticated` (Service-level authorization might apply)

**Path Variable**: `businessId` (UUID)

**Response Data (`BusinessResponseDTO`)**
```json
null
```

### `GET /api/business/nearby`
Retrieves a list of businesses near a specified location, optionally within a given radius.
- **Roles Permitted**: `Public`

**Query Parameters**
- `lat` (double, required): Latitude of the user's location.
- `lon` (double, required): Longitude of the user's location.
- `radiusKm` (double, optional): Search radius in kilometers. Defaults to 15km if not provided.

**Example Request**
```
GET /api/business/nearby?lat=-6.200000&lon=106.816666&radiusKm=5
```

**Response Data (`List<BusinessResponseDTO>`)**
```json
[
    {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "ownerName": "Jane Smith",
        "businessName": "Pawtner Pet Shop",
        "description": "One-stop shop for all your pet needs.",
        "businessType": "PET_SHOP",
        "hasEmergencyServices": false,
        "businessEmail": "shop@pawtner.com",
        "businessPhone": "081122334455",
        "emergencyPhone": null,
        "businessImageUrl": "http://example.com/business_image.jpg",
        "certificateImageUrl": "http://example.com/certificate_image.jpg",
        "latitude": -6.200000,
        "longitude": 106.816666,
        "statusRealTime": "OPEN",
        "businessAddress": "123 Pet Street, Anytown",
        "operationHours": {
            "MONDAY": { "open": "09:00", "close": "17:00" },
            "TUESDAY": { "open": "09:00", "close": "17:00" },
            "WEDNESDAY": { "open": "09:00", "close": "17:00" },
            "THURSDAY": { "open": "09:00", "close": "17:00" },
            "FRIDAY": { "open": "09:00", "close": "17:00" },
            "SATURDAY": { "open": "10:00", "close": "16:00" },
            "SUNDAY": null
        },
        "statusApproved": "Approved"
    }
]
```

---

## Notifications (`/api/notifications`)

### `POST /api/notifications/fcm-token`
Updates the FCM token for push notifications for the authenticated user.
- **Roles Permitted**: `Authenticated`

**Request Body (`application/json`)**
```json
{
  "fcmToken": "your_fcm_token_string"
}
```

**Response Data (`String`)**
```json
null
```

### `DELETE /api/notifications/fcm-token`
Deletes the FCM token for the authenticated user.
- **Roles Permitted**: `Authenticated`

**Request Body (`application/json`)**
```json
{
  "fcmToken": "your_fcm_token_string"
}
```

**Response Data (`String`)**
```json
null
```

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

### `GET /api/orders/{order_id}`
Retrieves a specific order by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

**Path Variable**: `order_id` (UUID)

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

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<OrderResponseDTO>`)**
```json
{
    "content": [
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
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/orders/{order_id}/status`
Updates the status of a specific order.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `order_id` (UUID)

**Query Parameters**: `status` (String, e.g., "PROCESSING", "COMPLETED", "CANCELLED")

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
    "status": "COMPLETED",
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

### `GET /api/orders/business`
Retrieves a paginated list of orders associated with a specific business.
- **Roles Permitted**: `BUSINESS_OWNER`

**Query Parameters**: `businessId` (UUID), `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<OrderResponseDTO>`)**
```json
{
    "content": [
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
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

---

## Payments (`/api/payments`)

### `POST /api/payments/webhook`
Handles incoming payment notification webhooks from Midtrans for both orders and bookings.
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

**Response Data (`String`)**
```json
null
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

**Response Data (`PetResponseDTO`)**
```json
{
    "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 5,
    "gender": "MALE",
    "imageUrl": "http://example.com/buddy.jpg",
    "notes": "Loves to play fetch.",
    "ownerName": "John Doe"
}
```

### `GET /api/pets/{id}`
Retrieves a specific pet by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

**Path Variable**: `id` (UUID)

**Response Data (`PetResponseDTO`)**
```json
{
    "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 5,
    "gender": "MALE",
    "imageUrl": "http://example.com/buddy.jpg",
    "notes": "Loves to play fetch.",
    "ownerName": "John Doe"
}
```

### `GET /api/pets`
Retrieves a paginated list of the customer's pets.
- **Roles Permitted**: `CUSTOMER`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<PetResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
            "name": "Buddy",
            "species": "Dog",
            "breed": "Golden Retriever",
            "age": 5,
            "gender": "MALE",
            "imageUrl": "http://example.com/buddy.jpg",
            "notes": "Loves to play fetch.",
            "ownerName": "John Doe"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/pets/{id}`
Updates a pet's profile.
- **Roles Permitted**: `CUSTOMER`

**Path Variable**: `id` (UUID)

**Request Body (`multipart/form-data`)**
- `pet` (form-part): JSON string for `PetRequestDTO` (excluding the image field, which is handled separately)
- `image` (file-part): The pet's image file.

**Example `pet` JSON string:**
```json
{
  "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
  "name": "Buddy Updated",
  "species": "Dog",
  "breed": "Golden Retriever",
  "age": 6,
  "gender": "MALE",
  "notes": "Still loves to play fetch."
}
```

**Response Data (`PetResponseDTO`)**
```json
{
    "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
    "name": "Buddy Updated",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 6,
    "gender": "MALE",
    "imageUrl": "http://example.com/buddy_updated.jpg",
    "notes": "Still loves to play fetch.",
    "ownerName": "John Doe"
}
```

### `DELETE /api/pets/{id}`
Deletes a pet's profile.
- **Roles Permitted**: `CUSTOMER`

**Path Variable**: `id` (UUID)

**Response Data (`Void`)**
```json
null
```

---

## Prescriptions (`/api/prescriptions`)

### `POST /api/prescriptions`
Creates a new prescription for a pet.
- **Roles Permitted**: `BUSINESS_OWNER`, `ADMIN`

**Request Body (`application/json`)**
```json
{
  "petId": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "bookingId": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
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

**Response Data (`PrescriptionResponseDTO`)**
```json
{
    "id": "pr1e2s3c4-r5e6-p7t8-i9o0-n1a2b3c4d5e6",
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy"
    },
    "issuingBusiness": {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "businessName": "Pawtner Vet Clinic"
    },
    "bookingId": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "issueDate": "2025-07-05",
    "notes": "Follow up in 2 weeks.",
    "prescriptionItems": [
        {
            "id": "pi1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "medicationName": "Amoxicillin",
            "dosage": "250mg",
            "frequency": "Twice a day",
            "durationDays": 10,
            "instructions": "Take with food."
        }
    ],
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/prescriptions/{id}`
Retrieves a specific prescription by its ID.
- **Roles Permitted**: `CUSTOMER`, `BUSINESS_OWNER`, `ADMIN`

**Path Variable**: `id` (String)

**Response Data (`PrescriptionResponseDTO`)**
```json
{
    "id": "pr1e2s3c4-r5e6-p7t8-i9o0-n1a2b3c4d5e6",
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy"
    },
    "issuingBusiness": {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "businessName": "Pawtner Vet Clinic"
    },
    "bookingId": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "issueDate": "2025-07-05",
    "notes": "Follow up in 2 weeks.",
    "prescriptionItems": [
        {
            "id": "pi1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "medicationName": "Amoxicillin",
            "dosage": "250mg",
            "frequency": "Twice a day",
            "durationDays": 10,
            "instructions": "Take with food."
        }
    ],
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/prescriptions`
Retrieves a paginated list of all prescriptions.
- **Roles Permitted**: `BUSINESS_OWNER`, `ADMIN`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<PrescriptionResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "pr1e2s3c4-r5e6-p7t8-i9o0-n1a2b3c4d5e6",
            "pet": {
                "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
                "name": "Buddy"
            },
            "issuingBusiness": {
                "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
                "businessName": "Pawtner Vet Clinic"
            },
            "bookingId": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "issueDate": "2025-07-05",
            "notes": "Follow up in 2 weeks.",
            "prescriptionItems": [
                {
                    "id": "pi1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
                    "medicationName": "Amoxicillin",
                    "dosage": "250mg",
                    "frequency": "Twice a day",
                    "durationDays": 10,
                    "instructions": "Take with food."
                }
            ],
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `GET /api/prescriptions/booking/{bookingId}`
Retrieves a prescription by its associated booking ID.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `bookingId` (UUID)

**Response Data (`PrescriptionResponseDTO`)**
```json
{
    "id": "pr1e2s3c4-r5e6-p7t8-i9o0-n1a2b3c4d5e6",
    "pet": {
        "id": "p1e2t3p4-e5f6-a7b8-c9d0-e1f2a3c4d5e6",
        "name": "Buddy"
    },
    "issuingBusiness": {
        "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "businessName": "Pawtner Vet Clinic"
    },
    "bookingId": "b1o2o3k4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "issueDate": "2025-07-05",
    "notes": "Follow up in 2 weeks.",
    "prescriptionItems": [
        {
            "id": "pi1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "medicationName": "Amoxicillin",
            "dosage": "250mg",
            "frequency": "Twice a day",
            "durationDays": 10,
            "instructions": "Take with food."
        }
    ],
    "createdAt": "2025-07-05T14:30:00"
}
```

### `DELETE /api/prescriptions/{id}`
Deletes a prescription.
- **Roles Permitted**: `BUSINESS_OWNER`, `ADMIN`

**Path Variable**: `id` (String)

**Response Data (`Void`)**
```json
null
```

---

## Products (`/api/products`)

### `POST /api/products`
Creates a new product.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `product` (form-part): JSON string for `ProductRequestDTO`
- `image` (file-part): The product's image file.

**Example `product` JSON string:**
```json
{
  "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
  "name": "Premium Dog Food",
  "category": "FOOD",
  "description": "High-quality dog food for all breeds.",
  "price": 250000,
  "stockQuantity": 100
}
```

**Response Data (`ProductResponseDTO`)**
```json
{
    "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Shop",
    "name": "Premium Dog Food",
    "category": "FOOD",
    "description": "High-quality dog food for all breeds.",
    "price": 250000.00,
    "stockQuantity": 100,
    "isActive": true,
    "imageUrl": "http://example.com/dog_food.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/products/{id}`
Retrieves a specific product by its ID.
- **Roles Permitted**: `Public`

**Path Variable**: `id` (UUID)

**Response Data (`ProductResponseDTO`)**
```json
{
    "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Shop",
    "name": "Premium Dog Food",
    "category": "FOOD",
    "description": "High-quality dog food for all breeds.",
    "price": 250000.00,
    "stockQuantity": 100,
    "isActive": true,
    "imageUrl": "http://example.com/dog_food.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/products`
Retrieves a paginated list of all products.
- **Roles Permitted**: `Public`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<ProductResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
            "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
            "businessName": "Pawtner Pet Shop",
            "name": "Premium Dog Food",
            "category": "FOOD",
            "description": "High-quality dog food for all breeds.",
            "price": 250000.00,
            "stockQuantity": 100,
            "isActive": true,
            "imageUrl": "http://example.com/dog_food.jpg",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `GET /api/products/my-products/{businessId}`
Retrieves a paginated list of products for a specific business.
- **Roles Permitted**: `Public`

**Path Variable**: `businessId` (UUID)

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<ProductResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
            "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
            "businessName": "Pawtner Pet Shop",
            "name": "Premium Dog Food",
            "category": "FOOD",
            "description": "High-quality dog food for all breeds.",
            "price": 250000.00,
            "stockQuantity": 100,
            "isActive": true,
            "imageUrl": "http://example.com/dog_food.jpg",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/products/{id}`
Updates an existing product.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `id` (UUID)

**Request Body (`multipart/form-data`)**
- `product` (form-part): JSON string for `ProductRequestDTO`
- `image` (file-part): The product's image file.

**Example `product` JSON string:**
```json
{
  "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
  "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
  "name": "Premium Dog Food Updated",
  "category": "FOOD",
  "description": "Updated high-quality dog food for all breeds.",
  "price": 260000,
  "stockQuantity": 90
}
```

**Response Data (`ProductResponseDTO`)**
```json
{
    "id": "p1r2o3d4-u5c6-t7i8-d9e0-f1a2b3c4d5e6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Shop",
    "name": "Premium Dog Food Updated",
    "category": "FOOD",
    "description": "Updated high-quality dog food for all breeds.",
    "price": 260000.00,
    "stockQuantity": 90,
    "isActive": true,
    "imageUrl": "http://example.com/dog_food_updated.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `DELETE /api/products/{id}`
Deletes a product.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `id` (UUID)

**Response Data (`Void`)**
```json
null
```

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

**Response Data (`ReviewResponseDTO`)**
```json
{
    "id": "r1e2v3i4e5w6-i7d8-a9b0-c1d2-e3f4a5b6c7d8",
    "userId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "userName": "John Doe",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "productId": null,
    "serviceId": null,
    "rating": 5,
    "comment": "Excellent service!",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/reviews/{id}`
Retrieves a specific review by its ID.
- **Roles Permitted**: `Public`

**Path Variable**: `id` (UUID)

**Response Data (`ReviewResponseDTO`)**
```json
{
    "id": "r1e2v3i4e5w6-i7d8-a9b0-c1d2-e3f4a5b6c7d8",
    "userId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "userName": "John Doe",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "productId": null,
    "serviceId": null,
    "rating": 5,
    "comment": "Excellent service!",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/reviews`
Retrieves a paginated list of all reviews.
- **Roles Permitted**: `Public`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<ReviewResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "r1e2v3i4e5w6-i7d8-a9b0-c1d2-e3f4a5b6c7d8",
            "userId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
            "userName": "John Doe",
            "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
            "productId": null,
            "serviceId": null,
            "rating": 5,
            "comment": "Excellent service!",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/reviews/{id}`
Updates an existing review.
- **Roles Permitted**: `CUSTOMER`

**Path Variable**: `id` (UUID)

**Request Body (`application/json`)**
```json
{
  "rating": 4,
  "comment": "Good service, but could be better."
}
```

**Response Data (`ReviewResponseDTO`)**
```json
{
    "id": "r1e2v3i4e5w6-i7d8-a9b0-c1d2-e3f4a5b6c7d8",
    "userId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "userName": "John Doe",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "productId": null,
    "serviceId": null,
    "rating": 4,
    "comment": "Good service, but could be better.",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `DELETE /api/reviews/{id}`
Deletes a review.
- **Roles Permitted**: `CUSTOMER`, `ADMIN`

**Path Variable**: `id` (UUID)

**Response Data (`Void`)**
```json
null
```

---

## Services (`/api/services`)

### `POST /api/services`
Creates a new service offering.
- **Roles Permitted**: `BUSINESS_OWNER`

**Request Body (`multipart/form-data`)**
- `service` (form-part): JSON string for `ServiceRequestDTO`
- `image` (file-part): The service's image file.

**Example `service` JSON string:**
```json
{
  "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
  "category": "GROOMING",
  "name": "Full Grooming Package",
  "basePrice": 150000,
  "capacityPerDay": 10
}
```

**Response Data (`ServiceResponseDTO`)**
```json
{
    "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Grooming",
    "category": "GROOMING",
    "name": "Full Grooming Package",
    "basePrice": 150000.00,
    "capacityPerDay": 10,
    "isActive": true,
    "imageUrl": "http://example.com/grooming.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/services/{id}`
Retrieves a specific service by its ID.
- **Roles Permitted**: `Public`

**Path Variable**: `id` (UUID)

**Response Data (`ServiceResponseDTO`)**
```json
{
    "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Grooming",
    "category": "GROOMING",
    "name": "Full Grooming Package",
    "basePrice": 150000.00,
    "capacityPerDay": 10,
    "isActive": true,
    "imageUrl": "http://example.com/grooming.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `GET /api/services`
Retrieves a paginated list of all services.
- **Roles Permitted**: `Public`

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<ServiceResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
            "businessName": "Pawtner Pet Grooming",
            "category": "GROOMING",
            "name": "Full Grooming Package",
            "basePrice": 150000.00,
            "capacityPerDay": 10,
            "isActive": true,
            "imageUrl": "http://example.com/grooming.jpg",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `GET /api/services/my-services/{businessId}`
Retrieves a paginated list of services for a specific business.
- **Roles Permitted**: `Public`

**Path Variable**: `businessId` (UUID)

**Query Parameters**: `page` (int), `size` (int), `sort` (String)

**Response Data (`Page<ServiceResponseDTO>`)**
```json
{
    "content": [
        {
            "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
            "businessName": "Pawtner Pet Grooming",
            "category": "GROOMING",
            "name": "Full Grooming Package",
            "basePrice": 150000.00,
            "capacityPerDay": 10,
            "isActive": true,
            "imageUrl": "http://example.com/grooming.jpg",
            "createdAt": "2025-07-05T14:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
```

### `PUT /api/services/{id}`
Updates an existing service.
- **Roles Permitted**: `BUSINESS_OWNER`

**Path Variable**: `id` (UUID)

**Request Body (`multipart/form-data`)**
- `service` (form-part): JSON string for `ServiceRequestDTO`
- `image` (file-part): The service's image file.

**Example `service` JSON string:**
```json
{
  "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
  "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
  "category": "GROOMING",
  "name": "Full Grooming Package Updated",
  "basePrice": 160000,
  "capacityPerDay": 9
}
```

**Response Data (`ServiceResponseDTO`)**
```json
{
    "id": "s1e2r3v4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "businessId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessName": "Pawtner Pet Grooming",
    "category": "GROOMING",
    "name": "Full Grooming Package Updated",
    "basePrice": 160000.00,
    "capacityPerDay": 9,
    "isActive": true,
    "imageUrl": "http://example.com/grooming_updated.jpg",
    "createdAt": "2025-07-05T14:30:00"
}
```

### `DELETE /api/services/{id}`
Deletes a service offering.
- **Roles Permitted**: `BUSINESS_OWNER`

---

## Users (`/api/users`)

### `PATCH /api/users/change-password`
Changes the password for the currently authenticated user.
- **Roles Permitted**: `Authenticated`

**Request Body (`application/json`)**
```json
{
  "oldPassword": "currentStrongPassword123",
  "newPassword": "newStrongerPassword456"
}
```

**Response Data (`String`)**
```json
null
```


**Path Variable**: `id` (UUID)

**Response Data (`Void`)**
```json
null
```

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

**Response Data (`ShoppingCartResponseDTO`)**
```json
{
    "id": "sc1a2r3t4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customerId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Shop",
    "items": [
        {
            "id": "ci1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "productId": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "productName": "Premium Dog Food",
            "productPrice": 250000.00,
            "quantity": 2,
            "subTotal": 500000.00
        }
    ],
    "totalPrice": 500000.00
}
```

### `GET /api/cart`
Retrieves the current user's shopping cart.
- **Roles Permitted**: `CUSTOMER`

**Response Data (`ShoppingCartResponseDTO`)**
```json
{
    "id": "sc1a2r3t4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customerId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Shop",
    "items": [
        {
            "id": "ci1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "productId": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "productName": "Premium Dog Food",
            "productPrice": 250000.00,
            "quantity": 2,
            "subTotal": 500000.00
        }
    ],
    "totalPrice": 500000.00
}
```

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

**Response Data (`ShoppingCartResponseDTO`)**
```json
{
    "id": "sc1a2r3t4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
    "customerId": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "businessId": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
    "businessName": "Pawtner Pet Shop",
    "items": [
        {
            "id": "ci1t2e3m4-i5d6-a7b8-c9d0-e1f2a3b4c5d6",
            "productId": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
            "productName": "Premium Dog Food",
            "productPrice": 250000.00,
            "quantity": 3,
            "subTotal": 750000.00
        }
    ],
    "totalPrice": 750000.00
}
```

### `DELETE /api/cart/{cartItemId}`
Removes a single item from the cart.
- **Roles Permitted**: `CUSTOMER`

**Path Variable**: `cartItemId` (UUID)

**Response Data (`Void`)**
```json
null
```

### `DELETE /api/cart`
Clears all items from the user's shopping cart.
- **Roles Permitted**: `CUSTOMER`

**Response Data (`Void`)**
```json
null
```

---

## Users (`/api/users`)

### `GET /api/users/{id}`
Retrieves a specific user by their ID.
- **Roles Permitted**: `Public`

**Path Variable**: `id` (String)

**Response Data (`UserResponseDTO`)**
```json
{
    "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "email": "customer@example.com",
    "name": "John Doe",
    "address": "123 Main Street, Anytown",
    "phone": "081234567890",
    "imageUrl": null
}
```

### `PUT /api/users`
Updates a user's profile information and profile image.
- **Roles Permitted**: `Authenticated` (Service-level authorization might apply)

**Request Body (`multipart/form-data`)**
- `user` (form-part): JSON string for `UserRequestDTO`
- `profileImage` (file-part, optional): The user's profile image file.

**Example `user` JSON string:**
```json
{
  "name": "John Doe Updated",
  "address": "124 New Street",
  "phone": "081234567891"
}
```

**Response Data (`UserResponseDTO`)**
```json
{
    "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "email": "customer@example.com",
    "name": "John Doe Updated",
    "address": "124 New Street",
    "phone": "081234567891",
    "imageUrl": "http://example.com/profile_updated.jpg"
}
```

### `GET /api/users`
Retrieves a list of all users.
- **Roles Permitted**: `ADMIN`

**Response Data (`List<UserResponseDTO>`)**
```json
[
    {
        "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
        "email": "customer@example.com",
        "name": "John Doe",
        "address": "123 Main Street, Anytown",
        "phone": "081234567890",
        "imageUrl": null
    },
    {
        "id": "b1c2d3e4-f5a6-b7c8-d9e0-f1a2b3c4d5e6",
        "email": "owner@example.com",
        "name": "Jane Smith",
        "address": "456 Business Ave, Anytown",
        "phone": "089876543210",
        "imageUrl": null
    }
]
```

### `DELETE /api/users/{id}`
Deletes a user by their ID.
- **Roles Permitted**: `ADMIN`

**Path Variable**: `id` (String)

**Response Data (`String`)**
```json
null
```

### `PATCH /api/users/{id}/status`
Updates a user's status (e.g., `ban`, `suspend`).
- **Roles Permitted**: `ADMIN`

**Path Variable**: `id` (UUID)

**Query Parameters**
- `action` (String, e.g., "ban", "suspend")
- `value` (Boolean, e.g., "true")

**Response Data (`UserResponseDTO`)**
```json
{
    "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9",
    "email": "customer@example.com",
    "name": "John Doe",
    "address": "123 Main Street, Anytown",
    "phone": "081234567890",
    "imageUrl": null
}
```
