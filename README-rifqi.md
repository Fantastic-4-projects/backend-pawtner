# Development Notes by Gemini CLI (Rifqi's Branch)

This document summarizes the initial review and setup steps performed by the Gemini CLI agent to prepare the project for further development.

## 1. Project Overview and Database Schema Review

- **`README.md`**: The main `README.md` was thoroughly reviewed. It provides a comprehensive and well-structured plan for the Pawtner application, including:
    - Executive Summary
    - Core Problem Statement
    - Proposed Solution & Core Features
    - Technical Architecture (with detailed Database Design and ERD)
    - Core User Workflows
    - Technical Specifications
    - MVP Execution Plan and Roadmap

    The `README.md` serves as an excellent foundation for understanding the project's vision and technical direction.

## 2. Entity Class Review and Validation

All Java entity classes located in `src/main/java/com/enigmacamp/pawtner/entity/` were reviewed against the database schema defined in `README.md`.

The following entity classes were checked:
- `Booking.java`
- `Business.java`
- `CartItem.java`
- `Order.java`
- `OrderItem.java`
- `Payment.java`
- `Pet.java`
- `Prescription.java`
- `PrescriptionItem.java`
- `Product.java`
- `Review.java`
- `Service.java`
- `ShoppingCart.java`
- `User.java`

**Findings:**
- The entity classes are generally well-implemented, with correct JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@OneToOne`, `@JoinColumn`).
- Relationships between entities are correctly defined.
- Validation constraints (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Min`, `@Max`, `@DecimalMin`, `@Digits`, `@Pattern`) are appropriately used, aligning with the schema's data integrity requirements.
- `PrePersist` and `PreUpdate` lifecycle callbacks are used for `createdAt` and `updatedAt` timestamps where applicable.

**Correction Made:**
- **`User.java`**: The `name` field was identified as non-nullable in the database schema but lacked an explicit `nullable = false` in its `@Column` annotation within the Java entity. This was corrected to ensure consistency and proper database schema generation/validation.

    **Before:**
    ```java
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;
    ```

    **After:**
    ```java
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    ```

## 3. Introduction of Enums for Type Safety

To enhance type safety, readability, and prevent invalid data, several fields with a fixed set of allowed values were converted from `String` types to custom Java `enum` types. A new package `com.enigmacamp.pawtner.constant` was created to house these enum definitions.

**Created Enum Classes:**
- `UserRole.java`
- `BusinessType.java`
- `BusinessStatus.java`
- `ProductCategory.java`
- `ServiceCategory.java`
- `OrderStatus.java`
- `BookingStatus.java`
- `PaymentStatus.java`

**Entity Classes Updated to Use Enums:**
- **`User.java`**: The `role` field now uses `UserRole` enum.
- **`Business.java`**: The `businessType` field now uses `BusinessType` enum, and `statusRealtime` uses `BusinessStatus` enum.
- **`Product.java`**: The `category` field now uses `ProductCategory` enum.
- **`Service.java`**: The `category` field now uses `ServiceCategory` enum.
- **`Order.java`**: The `status` field now uses `OrderStatus` enum.
- **`Booking.java`**: The `status` field now uses `BookingStatus` enum.
- **`Payment.java`**: The `status` field now uses `PaymentStatus` enum.

For each updated field, the `@Pattern` annotation (which was previously used for string-based validation) was removed, and `@Enumerated(EnumType.STRING)` was added to ensure that JPA persists the enum values as their string names in the database.

## 4. Postman Collection Updates and User Flow Clarification

This section details the updates made to the `Pawtner.postman_collection.json` file to accurately reflect the user registration and business creation flow, along with a bug fix.

**User Registration Flow Clarification:**
- It was clarified that all new user registrations via the `/api/auth/register` endpoint are *always* assigned the `CUSTOMER` role by default. There is no direct registration endpoint for `BUSINESS_OWNER` roles.
- To become a "business owner" in the application's context, a user first registers as a `CUSTOMER`, logs in, and then creates a business entity. This association grants them the necessary permissions to manage products and services for that specific business.

**Postman Collection Changes:**
- **Removed "Register Business Owner" Request:** The misleading "Register Business Owner" request was removed from the "Authentication" folder, as it implied a direct registration path for business owners which does not exist in the current backend implementation.
- **Added "Business Management" Folder and "Create Business" Request:** A new folder named "Business Management" was added. Inside this folder, a "Create Business (Sets businessId)" request was added. This request demonstrates the correct flow for a logged-in `CUSTOMER` to create a business, and it automatically captures the `businessId` from the response for subsequent requests.

**Bug Found and Fixed:**
- **JSON Escaping in "Create Business" Request:** An error was identified and fixed in the `raw` body of the "Create Business (Sets businessId)" request. The double quotes within the JSON string were not properly escaped, leading to an invalid JSON format. This has been corrected to ensure the request body is valid.
- **`role` field in "Register Customer" Request:** The `role` field was incorrectly present in the request body for "Register Customer". This was removed as the backend automatically assigns the `CUSTOMER` role upon registration.

## 5. Conclusion

The project's `README.md` provides a solid blueprint, and the existing entity classes are in good shape, closely mirroring the intended database design. The minor correction in `User.java` and the introduction of enums ensure even tighter alignment, improved type safety, and better maintainability. The Postman collection has been updated to accurately reflect the user and business registration flow, and critical bugs related to JSON formatting and incorrect request parameters have been resolved.

The project is now well-prepared for the next development phases, such as implementing repositories, services, and controllers based on these entities.