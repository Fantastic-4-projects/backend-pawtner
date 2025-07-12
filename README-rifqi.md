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

To enhance type safety and readability, several fields were converted from `String` types to custom Java `enum` types in the `com.enigmacamp.pawtner/constant` package.

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

### e. Global Exception Handling Framework
- **Goal**: To build a robust, centralized error handling mechanism that ensures API stability and provides consistent, developer-friendly error responses, which is crucial for building the "trustworthy" platform described in `README.md`.
- **`ErrorController.java`**: A new controller using `@RestControllerAdvice` was created to intercept exceptions application-wide.
- **Standardized Responses**: All error responses now use the existing `CommonResponse` DTO for consistency.
- **Specific Handlers Implemented**:
    - **`MethodArgumentNotValidException`**: Handles validation failures from `@Valid` on request bodies, returning a `400 Bad Request` with a map of fields and error messages.
    - **`DataIntegrityViolationException`**: Catches database errors like unique constraint failures (e.g., duplicate email). Returns a clear `409 Conflict` instead of a generic server error.
    - **`ResponseStatusException`**: Allows services to throw exceptions with specific HTTP statuses (e.g., `404 Not Found`) for business logic failures.
    - **Spring Security Exceptions**: Handles `AuthenticationException` (`401 Unauthorized`) for login failures and `AccessDeniedException` (`403 Forbidden`) for authorization issues.
    - **`Exception` (Catch-All)**: A safety net that catches any other unhandled exception and returns a `500 Internal Server Error`, preventing application crashes.
- **Logging**: Integrated SLF4J logging into `ErrorController` to record detailed error information on the server, which is essential for debugging.

## 5. Recent API Updates (Implemented by Gemini CLI)

The following changes have been implemented to enhance the Booking and Order APIs, particularly for frontend consumption and improved data handling:

### a. Booking API: Timezone Handling (`ZonedDateTime`)
- **Change**: The `startTime` and `endTime` fields in `Booking.java` (entity), `BookingRequestDTO.java`, and `BookingResponseDTO.java` have been changed from `LocalDateTime` to `ZonedDateTime`.
- **Impact for Frontend**: 
    - When sending booking requests, ensure `startTime` and `endTime` are sent in a format compatible with `ZonedDateTime` (e.g., ISO 8601 with timezone offset, like `2025-07-12T23:45:00+07:00`).
    - When receiving booking responses, parse `startTime` and `endTime` as `ZonedDateTime` to correctly handle timezone information.
- **Backend Adjustments**: `BookingServiceImpl.java`, `BookingRepository.java`, and `SchedulingServiceImpl.java` have been updated to correctly process and store `ZonedDateTime`.

### b. Booking API: Enhanced Address and Location Details
- **Change**: `BookingResponseDTO.java` now includes additional fields to provide comprehensive address and location information for bookings:
    - `deliveryType` (String): Indicates "PICKUP" or "DELIVERY".
    - `deliveryAddress` (String): Formatted delivery address if `deliveryType` is "DELIVERY". `null` otherwise.
    - `deliveryLatitude` (Double): Latitude of delivery location if `deliveryType` is "DELIVERY". `null` otherwise.
    - `deliveryLongitude` (Double): Longitude of delivery location if `deliveryType` is "DELIVERY". `null` otherwise.
    - `businessAddress` (String): Address of the associated business if `deliveryType` is "PICKUP". `null` otherwise.
- **Impact for Frontend**: 
    - Update your `BookingResponseDTO` model to include these new fields.
    - Implement logic to dynamically display the correct address (`deliveryAddress` or `businessAddress`) based on `deliveryType`.
    - `deliveryLatitude` and `deliveryLongitude` can be used for map integration or reverse geocoding on the frontend to provide human-readable locations.
- **Backend Adjustments**: `BookingMapper.java` has been updated to correctly populate these new fields based on the booking's delivery type and associated entities.

### c. Order API: Enhanced Address and Location Details
- **Change**: `OrderResponseDTO.java` now includes additional fields, similar to `BookingResponseDTO`, for comprehensive address and location information for orders:
    - `deliveryType` (String): Indicates "PICKUP" or "DELIVERY".
    - `deliveryAddress` (String): Formatted delivery address if `deliveryType` is "DELIVERY". `null` otherwise.
    - `deliveryLatitude` (Double): Latitude of delivery location if `deliveryType` is "DELIVERY". `null` otherwise.
    - `deliveryLongitude` (Double): Longitude of delivery location if `deliveryType` is "DELIVERY". `null` otherwise.
    - `businessAddress` (String): Address of the associated business if `deliveryType` is "PICKUP". `null` otherwise.
- **Impact for Frontend**: 
    - Update your `OrderResponseDTO` model to include these new fields.
    - Implement logic to dynamically display the correct address (`deliveryAddress` or `businessAddress`) based on `deliveryType`.
    - `deliveryLatitude` and `deliveryLongitude` can be used for map integration or reverse geocoding on the frontend to provide human-readable locations.
- **Backend Adjustments**: `OrderMapper.java` has been updated to correctly populate these new fields based on the order's delivery type and associated entities.

## 6. Unresolved Problems & Next Steps

While the foundational structure and error handling are now very strong, several key pieces of business logic and integration described in `README.md` are still pending implementation.

### a. Core Business Logic Implementation
The error handling framework is in place, but the actual business logic that *triggers* these errors needs to be written inside the service layer.
- **Task**: Implement checks within services, such as:
    - Validating `services.capacity_per_day` before confirming a booking.
    - Checking `products.stock_quantity` before allowing a checkout.
    - Throwing `new ResponseStatusException(HttpStatus.CONFLICT, "...")` when business rules are violated.

### b. "Petshop Nearby" & Emergency Assistance Feature
This is a major unsolved feature that is critical to the problem statement in `README.md`.
- **Problem**: The backend logic to calculate distances and find nearby businesses based on `latitude` and `longitude` has not been implemented.
- **Task**: 
    - Investigate and implement a geospatial solution, likely using the **PostGIS** extension for PostgreSQL.
    - Create the specific repository methods and service logic to handle queries like `findNearest(user_lat, user_lon)`.

### c. Full Payment & Transactional Flow
The `README.md` outlines a complete e-commerce and booking workflow that is heavily dependent on payment integration.
- **Problem**: The actual integration with the Midtrans API is not yet implemented.
- **Task**: 
    - Implement the `PaymentService` to make API calls to Midtrans for creating transactions and receiving a `snap_token`.
    - Implement the `POST /api/payments/webhook` endpoint to securely handle and process incoming notifications from Midtrans.
    - Ensure that a successful payment webhook correctly updates the status of `Orders` and `Bookings` and adjusts product stock.

### d. Resolve 403 Forbidden Error for Testing
The authorization issue mentioned previously still needs to be addressed for efficient development and testing.
- **Recommended Action**: For any developer needing to test `business_owner` endpoints, manually update their user's `role` in the database to `'BUSINESS_OWNER'` and then re-login to get a new JWT.

## 7. Checkout and Booking Flow Enhancement

To support new frontend features for `CheckoutScreen` and `BookServiceScreen`, the backend has been updated to handle different delivery and pickup options.

### a. Backend Modifications for Delivery/Pickup Options
- **New Enums**: Created `DeliveryType.java` (`DELIVERY`, `PICKUP`) and `DeliveryLocationType.java` (`CURRENT_GPS`, `HOME_ADDRESS`) to ensure type safety for the new options.
- **Entity Updates**: 
    - `Order.java` and `Booking.java` were updated with new fields to store the selected delivery/pickup choice:
        - `deliveryType` (Enum)
        - `deliveryLocationType` (Enum)
        - `deliveryLatitude` (Double)
        - `deliveryLongitude` (Double)
        - `deliveryAddressDetail` (String)
        - `pickupBusiness` (a `@ManyToOne` relationship to the `Business` entity).
- **Service Logic Enhancement**: 
    - `OrderServiceImpl` and `BookingServiceImpl` were refactored to:
        - Process the new delivery/pickup fields from the request DTOs.
        - For `DELIVERY`, calculate shipping fees based on user and business location.
        - For `PICKUP`, validate that the selected pickup business is valid for the order/booking and skip shipping fee calculations.
- **Response DTO Enhancement**: 
    - Confirmed that `ProductResponseDTO` and `ServiceResponseDTO` already included full `Business` details.
    - `ShoppingCartResponseDTO` was updated to include the full `BusinessResponseDTO` object instead of just `businessId` and `businessName`. This provides the frontend with all necessary business location data directly from the cart view, simplifying the implementation of the "Pick up at Store" option.
- **Assumption Confirmation**: The backend logic confirms the frontend's assumption: the "Pick up at Store" option for product orders is only valid if all items in the cart belong to the same business. This is enforced by the application's shopping cart design and validated during checkout.