# Development Notes by Gemini CLI (Huda's Branch)

This document summarizes the changes made by the Gemini CLI agent related to authentication, security, and email verification.

## 1. New Features and Modules Implemented

- **Authentication and Authorization**: Implemented core authentication and authorization functionalities using Spring Security and JWT.
- **Email Verification**: Added email verification flow for new user registrations.

## 2. File Changes and Additions

### New Files:

- `Postman/Pawtner.postman_collection.json`: Postman collection for API testing.
- `src/main/java/com/enigmacamp/pawtner/config/AuthTokenFilter.java`: JWT authentication filter.
- `src/main/java/com/enigmacamp/pawtner/config/JwtService.java`: Service for JWT token generation and validation.
- `src/main/java/com/enigmacamp/pawtner/config/SecurityConfig.java`: Spring Security configuration.
- `src/main/java/com/enigmacamp/pawtner/controller/AuthController.java`: REST controller for authentication endpoints (register, login, verify, resend-verification).
- `src/main/java/com/enigmacamp/pawtner/dto/request/LoginRequestDTO.java`: DTO for login requests.
- `src/main/java/com/enigmacamp/pawtner/dto/request/RegisterRequestDTO.java`: DTO for registration requests.
- `src/main/java/com/enigmacamp/pawtner/dto/request/ResendVerificationRequestDTO.java`: DTO for resending verification code requests.
- `src/main/java/com/enigmacamp/pawtner/dto/request/VerificationRequestDTO.java`: DTO for account verification requests.
- `src/main/java/com/enigmacamp/pawtner/dto/response/CommonResponse.java`: Generic common response DTO.
- `src/main/java/com/enigmacamp/pawtner/dto/response/LoginResponseDTO.java`: DTO for login responses.
- `src/main/java/com/enigmacamp/pawtner/dto/response/RegisterResponseDTO.java`: DTO for registration responses.
- `src/main/java/com/enigmacamp/pawtner/repository/AuthRepository.java`: Repository for user authentication operations.
- `src/main/java/com/enigmacamp/pawtner/service/AuthService.java`: Interface for authentication services.
- `src/main/java/com/enigmacamp/pawtner/service/EmailService.java`: Interface for email services.
- `src/main/java/com/enigmacamp/pawtner/service/UserService.java`: Interface for user details service.
- `src/main/java/com/enigmacamp/pawtner/service/impl/AuthServiceImpl.java`: Implementation of `AuthService`.
- `src/main/java/com/enigmacamp/pawtner/service/impl/EmailServiceImpl.java`: Implementation of `EmailService`.
- `src/main/java/com/enigmacamp/pawtner/service/impl/UserServiceImpl.java`: Implementation of `UserService`.
- `src/main/java/com/enigmacamp/pawtner/util/ResponseUtil.java`: Utility class for creating standardized API responses.
- `src/main/resources/templates/verification-email.html`: Thymeleaf template for verification emails.

### Modified Files:

- `pom.xml`: Added new dependencies for Spring Security, JWT, Spring Mail, and Thymeleaf.
- `src/main/java/com/enigmacamp/pawtner/constant/UserRole.java`: Minor formatting change (added a newline).
- `src/main/java/com/enigmacamp/pawtner/entity/Booking.java`: Changed `totalPrice` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/Business.java`: Changed `latitude` and `longitude` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/Order.java`: Changed `totalAmount` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/OrderItem.java`: Changed `priceAtPurchase` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/Payment.java`: Changed `amount` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/Product.java`: Changed `price` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/Service.java`: Changed `basePrice` from `Double` to `BigDecimal`.
- `src/main/java/com/enigmacamp/pawtner/entity/User.java`: Implemented `UserDetails` interface and added methods for Spring Security integration. Also, changed `password` field to `passwordHash` and added `codeVerification` and `codeExpire` fields for email verification.
- `src/main/resources/application.properties`: Added JWT and email configuration properties.

## 3. Conclusion

These changes introduce a robust authentication and authorization system with email verification, enhancing the security and user management capabilities of the Pawtner application. The migration of `Double` to `BigDecimal` for monetary and coordinate values improves precision and avoids floating-point inaccuracies.