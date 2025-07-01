# Development Notes by Gemini CLI (Rifqi's Branch)

This document summarizes the initial review and setup steps performed by the Gemini CLI agent to prepare the project for further development, along with recent updates and next steps.

## 1. Project Overview and Database Schema Review

- **`README.md`**: The main `README.md` was thoroughly reviewed. It provides a comprehensive and well-structured plan for the Pawtner application.
- **Conclusion**: The `README.md` serves as an excellent foundation for understanding the project's vision and technical direction.

## 2. Entity Class Review and Validation

All Java entity classes located in `src/main/java/com/enigmacamp/pawtner/entity/` were reviewed against the database schema defined in `README.md`.

**Corrections Made:**
- **`User.java`**: The `name` field was identified as non-nullable in the database schema but lacked an explicit `nullable = false` in its `@Column` annotation. This was corrected.
- **`Service.java`**: The `imageUrl` field was missing and has been added.
- **`Business.java`**: Confirmed `id` uses `UUID` and no `publicId` field exists. Added `columnDefinition = "uuid"` to the `@Id` annotation for `Business` entity to ensure proper UUID column creation in the database.
- **Related Entities (`Order`, `Prescription`, `Product`, `Review`, `Service`, `ShoppingCart`)**: Added `columnDefinition = "uuid"` to `@JoinColumn` annotations where they reference the `Business` entity's ID, to ensure correct UUID foreign key mapping.

## 3. Introduction of Enums for Type Safety

To enhance type safety and readability, several fields were converted from `String` types to custom Java `enum` types in the `com.enigmacamp.pawtner.constant` package.

## 4. Completed Development Tasks

Based on the MVP plan in `README.md`, the following core features have been implemented or updated:

### a. Product Management (CRUD)
- **`ProductController`**: Exposes REST endpoints for creating, reading, updating, and deleting products.
- **`ProductService`**: Contains the business logic for managing products.
- **`ProductRepository`**: Handles data access for the `Product` entity.
- **DTOs**: `ProductRequestDTO` and `ProductResponseDTO` were created for handling data transfer.
- **Security**: Endpoints are secured, allowing only `business_owner` roles for modification operations.
- **Postman Collection Update**: Corrected field names (`productName` to `name`, `productPrice` to `price`, `productStock` to `stockQuantity`, `productCategory` to `category`) in "Create Product" and "Update Product" requests.

### b. Service Management (CRUD)
- **`ServiceController`**: Exposes REST endpoints for creating, reading, updating, and deleting services.
- **`ServiceService`**: Contains the business logic for managing services.
- **`ServiceRepository`**: Handles data access for the `Service` entity.
- **DTOs**: `ServiceRequestDTO` and `ServiceResponseDTO` were created for handling data transfer.
- **Security**: Endpoints are secured, allowing only `business_owner` roles for modification operations.
- **Postman Collection Update**: Corrected field names (`serviceName` to `name`, `servicePrice` to `basePrice`, `serviceCategory` to `category`) in "Create Service" and "Update Service" requests.

### c. Image Upload Service
- **`ImageUploadService`**: A reusable service for handling image uploads.
- **Cloudinary Integration**: The service is now configured to use Cloudinary for image storage.
- **Configuration**: Cloudinary credentials have been externalized to `application.properties` for better security and maintainability.

### d. Common Response Structure
- **`CommonResponse.java`**: The response structure was adjusted to explicitly include the HTTP status code, alongside the message and data.
- **`ResponseUtil.java`**: Updated to correctly populate the `status` field in `CommonResponse`.

## 5. Current Status of APIs

- **Product and Service APIs**: Still in progress. While the backend code and Postman collection have been updated, a "403 Forbidden" error is currently preventing successful product/service creation. This indicates an authorization issue.

## 6. Next Steps

With the foundation for managing products and services now in place, the next logical steps are to resolve the current authorization issue and then implement the core e-commerce and booking workflows as defined in the MVP.

### a. Resolve 403 Forbidden Error for Product/Service Creation
The "403 Forbidden" error indicates that the authenticated user does not have the required `BUSINESS_OWNER` role to perform these actions.

**Recommended Action for Testing:**
- **Manually update user role in the database**: Connect to your PostgreSQL database and update the `role` of your test user (the one you are logging in with via Postman) to `'BUSINESS_OWNER'` in the `users` table. After updating, log in again via Postman to obtain a new JWT token that reflects the updated role.

### b. Implement Shopping Cart Functionality
- Create `CartService` and `CartController` to allow customers to add products to a shopping cart.
- Implement logic for adding, removing, and clearing cart items.

### c. Implement Order and Checkout Process
- Create `OrderService` and `OrderController` to allow customers to convert their cart into an order.
- Implement the main `createOrderFromCart` method.

### d. Implement Payment Integration (Midtrans)
- Create `PaymentService` and `PaymentController`.
- Integrate with the Midtrans API to create transactions and handle webhooks.

### e. Implement Booking Functionality
- Create `BookingService` and `BookingController` to allow customers to book services.
- Implement logic for checking availability and creating bookings.
- Integrate with the `PaymentService` to handle payments for bookings.