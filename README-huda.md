## 1. New Features and Modules Implemented

- **Authentication and Authorization**: Implemented core authentication and authorization functionalities using Spring Security and JWT.
- **Email Verification**: Added email verification flow for new user registrations.
- **Admin User Initialization**: Added a mechanism to create an admin user on application startup.
- **Business Profile Management**: Implemented features for creating and viewing business profiles, with role-based access control.

## 2. File Changes and Additions

### New Files:

- `src/main/java/com/enigmacamp/pawtner/config/AdminInitializer.java`: Initializes the admin user.
- `src/main/java/com/enigmacamp/pawtner/controller/BusinessController.java`: REST controller for managing business profiles.
- `src/main/java/com/enigmacamp/pawtner/dto/request/BusinessRequestDTO.java`: DTO for business registration requests.
- `src/main/java/com/enigmacamp/pawtner/dto/response/BusinessResponseDTO.java`: DTO for business profile responses.
- `src/main/java/com/enigmacamp/pawtner/repository/BusinessRepository.java`: Repository for business profile operations.
- `src/main/java/com/enigmacamp/pawtner/service/BusinessService.java`: Interface for business profile services.
- `src/main/java/com/enigmacamp/pawtner/service/impl/BusinessServiceImpl.java`: Implementation of `BusinessService`.

### Modified Files:

- `pom.xml`: Added `hibernate-types-60` dependency for JSON mapping.
- `src/main/java/com/enigmacamp/pawtner/config/SecurityConfig.java`: Enabled method-level security (`@EnableMethodSecurity`) to allow for role-based authorization on controller methods and configured CORS.
- `src/main/java/com/enigmacamp/pawtner/constant/UserRole.java`: Added `ADMIN` role to support administrative privileges.
- `src/main/java/com/enigmacamp/pawtner/controller/AuthController.java`: Added input validation (`@Valid`) and improved user-facing response messages.
- `src/main/java/com/enigmacamp/pawtner/dto/request/RegisterRequestDTO.java`: Updated to include `phoneNumber` and `address` fields for more complete user profiles.
- `src/main/java/com/enigmacamp/pawtner/entity/Business.java`: Refactored `operationHours` to use `Map<String, String>` with `JsonType` for better structure and updated column definitions for `description` and `address`.
- `src/main/java/com/enigmacamp/pawtner/entity/User.java`: Updated `address` column definition.
- `src/main/java/com/enigmacamp/pawtner/repository/AuthRepository.java`: Added `existsByEmail` method to check for existing users during admin initialization.
- `src/main/java/com/enigmacamp/pawtner/service/impl/AuthServiceImpl.java`: Hardcoded new user registrations to the `CUSTOMER` role and added a check to prevent login for unverified accounts.

## 3. Conclusion

These changes introduce a robust authentication and authorization system with email verification, enhancing the security and user management capabilities of the Pawtner application. The addition of an admin role and business profile management features, secured by method-level authorization, provides a foundation for core application functionality. The migration of `Double` to `BigDecimal` for monetary and coordinate values improves precision and avoids floating-point inaccuracies.

## 4. Post-Implementation Refinements

Following the initial implementation, several refinements were made to improve consistency and address feedback:

- **`BusinessController.java`**: Removed `@PreAuthorize("hasRole('ROLE_ADMIN')")` from the `viewBusiness` endpoint to allow broader access and add endpoint in front `/api`.
- **`AuthController.java`**: add endpoint in front `/api`.
- **`BusinessRequestDTO.java`**: Renamed the `operationHour` field to `operationHours` for clarity and consistency.
- **`Business.java`**: Changed the `columnDefinition` for the `operationHours` field from `jsonb` to `TEXT` to simplify data handling.
- **`BusinessServiceImpl.java`**: Updated the `create` method to use `getOperationHours()` in alignment with the DTO changes.

## 5. Refactoring `operationHours`

To improve the structure and maintainability of how business operation hours are handled :

- **DTO and Converter**: Introduced `OperationHoursDTO` to replace the generic `Map<String, String>` and added a custom `OperationHoursConverter` to handle serialization to and from JSON in the database.
- **Dependency Removal**: Removed the `hibernate-types-60` dependency, favoring the custom converter for a more lightweight solution.
- **Codebase Update**: Updated the `Business` entity, DTOs, and service layer to utilize the new `OperationHoursDTO` and converter, ensuring a more robust and type-safe implementation.

## 6. Forgot Password Feature

To enhance user account management, a "forgot password" feature has been implemented. This allows users to securely reset their password if they forget it.

- **New Endpoints**: Added `/api/auth/forgot-password` to initiate the password reset process and `/api/auth/reset-password` to set a new password using a token.
- **Email Integration**: The system now sends a password reset link to the user's registered email address.
- **Data Model Update**: The `User` entity has been updated with `resetPasswordToken` and `resetPasswordTokenExpire` fields to manage the reset process securely.
- **New DTOs**: Introduced `ForgotPasswordRequestDTO` and `ResetPasswordRequestDTO` to handle the request data.
- **Service Layer**: Implemented the necessary logic in `AuthService` and `EmailService` to handle token generation, email sending, and password updates.
- **Email Template**: Added a new `password-reset-email.html` template for the password reset email.
<<<<<<< HEAD
- **Postman Collection**: Updated the Postman collection with new requests to test the "Forgot Password" and "Reset Password" functionality.

## 7. Google OAuth2 Login

To provide a more seamless user experience, Google OAuth2 login has been integrated into the application. This allows users to register and log in using their Google accounts, reducing friction and improving accessibility.

- **New Handler**: Implemented `OAuth2SuccessHandler` to manage the authentication process after a successful Google login. This handler is responsible for creating a new user if one does not already exist and generating a JWT token for the authenticated user.
- **Security Configuration**: Updated `SecurityConfig` to include the OAuth2 login flow and configure the new success handler.
- **Repository Update**: Modified `AuthRepository` to use `Optional<User>` instead of `Optional<UserDetails>` for better type safety and consistency.
- **Service Layer**: Refactored `AuthServiceImpl` to align with the repository changes.
- **Configuration**: Added OAuth2 client configuration properties to `application.properties` for the Google provider.

## 8. Role-Based Access Control and Registration Refinements

To enhance security and provide more granular control over user roles, the registration process has been updated and role-based access control has been implemented.

- **Endpoint Security**: Secured the `/api/business/**` endpoint, ensuring that only users with the `BUSINESS_OWNER` role can access it.
- **Separated Registration**: The user registration endpoint has been split into `/api/auth/register/customer` and `/api/auth/register/business-owner` to clearly distinguish between different user types upon registration.
- **Role Management**: A new endpoint `PATCH /api/auth/user/set-role` was added to allow for dynamic role assignment to users.
- **Service Layer**: The `AuthService` and its implementation were updated to support role assignment during registration and modification.
- **Business Logic**: The responsibility of assigning the `BUSINESS_OWNER` role was removed from the `BusinessServiceImpl` to decouple business creation from role management.
