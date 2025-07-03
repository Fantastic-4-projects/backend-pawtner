
## Order Endpoints (`/api/orders`)

### 1. Checkout
Creates an order from the user's shopping cart.

- **URL:** `/api/orders/checkout`
- **Method:** `POST`
- **Authentication:** Requires Customer token.
- **Request Body:** None
- **Response Body:** `OrderResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Order created successfully",
      "data": {
        "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "orderNumber": "ORD-20240703-0001",
        "customerId": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
        "customerName": "Customer User",
        "businessId": "b1c2d3e4-f5g6-7890-1234-567890abcdef",
        "businessName": "Intan Grooming House",
        "totalAmount": 150000.00,
        "status": "PENDING",
        "createdAt": "2024-07-03T10:00:00",
        "items": [
          {
            "id": "i1j2k3l4-m5n6-7890-1234-567890abcdef",
            "productId": "p1q2r3s4-t5u6-7890-1234-567890abcdef",
            "productName": "Royal Canin Adult",
            "quantity": 1,
            "pricePerUnit": 150000.00,
            "subTotal": 150000.00
          }
        ],
        "snapToken": "a_midtrans_snap_token"
      }
    }
    ```
    - `id` (UUID): Unique identifier for the order.
    - `orderNumber` (String): Unique order number.
    - `customerId` (UUID): ID of the customer who placed the order.
    - `customerName` (String): Name of the customer.
    - `businessId` (UUID): ID of the business associated with the order.
    - `businessName` (String): Name of the business.
    - `totalAmount` (BigDecimal): Total amount of the order.
    - `status` (OrderStatus Enum): Current status of the order (e.g., `PENDING`, `COMPLETED`, `CANCELLED`).
    - `createdAt` (LocalDateTime): Timestamp when the order was created.
    - `items` (List<OrderItemResponseDTO>): List of items in the order.
        - `id` (UUID): Unique identifier for the order item.
        - `productId` (UUID): ID of the product.
        - `productName` (String): Name of the product.
        - `quantity` (Integer): Quantity of the product.
        - `pricePerUnit` (BigDecimal): Price per unit of the product.
        - `subTotal` (BigDecimal): Subtotal for the order item.
    - `snapToken` (String): Midtrans Snap token for payment (if applicable).

### 2. Get Order By ID
Retrieves a specific order by its ID.

- **URL:** `/api/orders/{order_id}`
- **Method:** `GET`
- **Authentication:** Requires Customer, Business Owner, or Admin token.
- **Path Variable:**
    - `order_id` (UUID): The ID of the order to retrieve.
- **Request Body:** None
- **Response Body:** `OrderResponseDTO` (Same as Checkout response)

### 3. Get My Orders
Retrieves a paginated list of orders for the authenticated customer.

- **URL:** `/api/orders`
- **Method:** `GET`
- **Authentication:** Requires Customer token.
- **Query Parameters (Pageable):**
    - `page` (Integer, optional): Page number (0-indexed). Default is 0.
    - `size` (Integer, optional): Number of items per page. Default is 10.
    - `sort` (String, optional): Sorting criteria in the format `property,(asc|desc)`. Default is `createdAt,desc`.
- **Request Body:** None
- **Response Body:** `CommonResponse<Page<OrderResponseDTO>>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully fetched all orders",
      "data": {
        "content": [
          // List of OrderResponseDTO objects
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
    }
    ```

## Payment Endpoints (`/api/payments`)

### 1. Payment Webhook
Handles incoming payment webhooks from the payment gateway (e.g., Midtrans).

- **URL:** `/api/payments/webhook`
- **Method:** `POST`
- **Authentication:** None (This endpoint is typically secured by the payment gateway's own mechanisms, e.g., IP whitelisting or signature verification, which are handled internally by the backend).
- **Request Body:** `Map<String, Object>` (The structure depends on the payment gateway. Below is an example for Midtrans.)
    ```json
    {
      "transaction_time": "2024-07-03 10:30:00",
      "transaction_status": "settlement",
      "transaction_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "status_message": "Success, transaction is settled",
      "status_code": "200",
      "signature_key": "your_signature_key",
      "payment_type": "bank_transfer",
      "order_id": "ORD-20240703-0001",
      "merchant_id": "your_merchant_id",
      "gross_amount": "150000.00",
      "fraud_status": "accept",
      "currency": "IDR"
    }
    ```
    - The fields in the request body will vary based on the payment gateway's webhook payload. The backend will parse this map.
- **Response Body:** `CommonResponse<String>`
    ```json
    {
      "statusCode": "OK",
      "message": "Webhook received",
      "data": null
    }
    ```

## Pet Endpoints (`/api/pets`)

### 1. Create Pet
Registers a new pet for the authenticated customer.

- **URL:** `/api/pets`
- **Method:** `POST`
- **Authentication:** Requires Customer token.
- **Request Body:** `PetRequestDTO`
    ```json
    {
      "name": "Doggy",
      "species": "Dog",
      "breed": "Golden Retriever",
      "gender": "MALE",
      "dateOfBirth": "2022-01-15",
      "colour": "Golden",
      "weight": 25.5
    }
    ```
    - `name` (String): Name of the pet.
    - `species` (String): Species of the pet (e.g., "Dog", "Cat").
    - `breed` (String): Breed of the pet.
    - `gender` (String Enum): Gender of the pet (e.g., "MALE", "FEMALE").
    - `dateOfBirth` (String): Date of birth of the pet in "YYYY-MM-DD" format.
    - `colour` (String): Color of the pet.
    - `weight` (Double): Weight of the pet.
- **Response Body:** `PetResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Successfully created pet",
      "data": {
        "id": "p1e2t3i4-d5e6-7890-1234-567890abcdef",
        "name": "Doggy",
        "species": "Dog",
        "breed": "Golden Retriever",
        "age": 2,
        "imageUrl": null,
        "notes": null,
        "ownerName": "Customer User"
      }
    }
    ```
    - `id` (UUID): Unique identifier for the pet.
    - `name` (String): Name of the pet.
    - `species` (String): Species of the pet.
    - `breed` (String): Breed of the pet.
    - `age` (Integer): Age of the pet in years.
    - `imageUrl` (String): URL of the pet's image.
    - `notes` (String): Additional notes about the pet.
    - `ownerName` (String): Name of the pet's owner.

### 2. Get Pet By ID
Retrieves a specific pet by its ID.

- **URL:** `/api/pets/{id}`
- **Method:** `GET`
- **Authentication:** Requires Customer, Business Owner, or Admin token.
- **Path Variable:**
    - `id` (UUID): The ID of the pet to retrieve.
- **Request Body:** None
- **Response Body:** `PetResponseDTO` (Same as Create Pet response)

### 3. Get My Pets
Retrieves a paginated list of pets owned by the authenticated customer.

- **URL:** `/api/pets`
- **Method:** `GET`
- **Authentication:** Requires Customer token.
- **Query Parameters (Pageable):**
    - `page` (Integer, optional): Page number (0-indexed). Default is 0.
    - `size` (Integer, optional): Number of items per page. Default is 10.
    - `sort` (String, optional): Sorting criteria in the format `property,(asc|desc)`. Default is `id,asc`.
- **Request Body:** None
- **Response Body:** `CommonResponse<Page<PetResponseDTO>>` (Similar to Get My Orders response structure, but with `PetResponseDTO` content)

### 4. Update Pet
Updates an existing pet's information.

- **URL:** `/api/pets/{id}`
- **Method:** `PUT`
- **Authentication:** Requires Customer token.
- **Path Variable:**
    - `id` (UUID): The ID of the pet to update.
- **Request Body:** `PetRequestDTO` (All fields are optional for update, but typically you'd send the fields you want to change)
    ```json
    {
      "name": "Doggy Updated",
      "species": "Dog",
      "breed": "Golden Retriever",
      "gender": "MALE",
      "dateOfBirth": "2022-01-15",
      "colour": "Dark Golden",
      "weight": 26.0
    }
    ```
- **Response Body:** `PetResponseDTO` (Same as Create Pet response)

### 5. Delete Pet
Deletes a pet by its ID.

- **URL:** `/api/pets/{id}`
- **Method:** `DELETE`
- **Authentication:** Requires Customer token.
- **Path Variable:**
    - `id` (UUID): The ID of the pet to delete.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully deleted pet",
      "data": null
    }
    ```

## Prescription Endpoints (`/api/prescriptions`)

### 1. Create Prescription
Creates a new prescription.

- **URL:** `/api/prescriptions`
- **Method:** `POST`
- **Authentication:** Requires Business Owner or Admin token.
- **Request Body:** `PrescriptionRequestDTO`
    ```json
    {
      "petId": "{{pet_id}}",
      "issuingBusinessId": "{{business_id}}",
      "issueDate": "2024-07-03",
      "notes": "Daily medication for pet",
      "prescriptionItems": [
        {
          "medicationName": "Medication A",
          "dosage": "10mg",
          "frequency": "Once daily",
          "durationDays": 30,
          "instructions": "Give with food"
        },
        {
          "medicationName": "Medication B",
          "dosage": "5ml",
          "frequency": "Twice daily",
          "durationDays": 15,
          "instructions": "Administer orally"
        }
      ]
    }
    ```
    - `petId` (String): ID of the pet for which the prescription is issued.
    - `issuingBusinessId` (String): ID of the business issuing the prescription.
    - `issueDate` (String): Date the prescription was issued in "YYYY-MM-DD" format.
    - `notes` (String, optional): Any additional notes for the prescription.
    - `prescriptionItems` (List<PrescriptionItemRequestDTO>): List of medication items in the prescription.
        - `medicationName` (String): Name of the medication.
        - `dosage` (String): Dosage of the medication (e.g., "10mg", "5ml").
        - `frequency` (String): Frequency of medication (e.g., "Once daily", "Twice daily").
        - `durationDays` (Integer): Duration of medication in days.
        - `instructions` (String, optional): Specific instructions for administering the medication.
- **Response Body:** `PrescriptionResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Successfully created prescription",
      "data": {
        "id": "p1r2e3s4-c5r6-7890-1234-567890abcdef",
        "pet": {
          "id": "{{pet_id}}",
          "name": "Doggy"
        },
        "issuingBusiness": {
          "businessId": "{{business_id}}",
          "businessName": "Intan Grooming House"
        },
        "issueDate": "2024-07-03",
        "notes": "Daily medication for pet",
        "prescriptionItems": [
          {
            "id": "item1-id",
            "medicationName": "Medication A",
            "dosage": "10mg",
            "frequency": "Once daily",
            "durationDays": 30,
            "instructions": "Give with food"
          },
          {
            "id": "item2-id",
            "medicationName": "Medication B",
            "dosage": "5ml",
            "frequency": "Twice daily",
            "durationDays": 15,
            "instructions": "Administer orally"
          }
        ],
        "createdAt": "2024-07-03T10:00:00"
      }
    }
    ```
    - `id` (String): Unique identifier for the prescription.
    - `pet` (PetResponseDTO): Details of the pet.
        - `id` (String): ID of the pet.
        - `name` (String): Name of the pet.
    - `issuingBusiness` (BusinessResponseDTO): Details of the issuing business.
        - `businessId` (String): ID of the business.
        - `businessName` (String): Name of the business.
    - `issueDate` (String): Date the prescription was issued.
    - `notes` (String): Notes for the prescription.
    - `prescriptionItems` (List<PrescriptionItemResponseDTO>): List of prescription items.
        - `id` (String): Unique identifier for the prescription item.
        - `medicationName` (String): Name of the medication.
        - `dosage` (String): Dosage of the medication.
        - `frequency` (String): Frequency of medication.
        - `durationDays` (Integer): Duration of medication in days.
        - `instructions` (String): Instructions for administering the medication.
    - `createdAt` (LocalDateTime): Timestamp when the prescription was created.

### 2. Get Prescription By ID
Retrieves a specific prescription by its ID.

- **URL:** `/api/prescriptions/{id}`
- **Method:** `GET`
- **Authentication:** Requires Business Owner, Admin, or Customer token.
- **Path Variable:**
    - `id` (String): The ID of the prescription to retrieve.
- **Request Body:** None
- **Response Body:** `PrescriptionResponseDTO` (Same as Create Prescription response)

### 3. Get All Prescriptions
Retrieves a paginated list of all prescriptions.

- **URL:** `/api/prescriptions`
- **Method:** `GET`
- **Authentication:** Requires Business Owner or Admin token.
- **Query Parameters (Pageable):**
    - `page` (Integer, optional): Page number (0-indexed). Default is 0.
    - `size` (Integer, optional): Number of items per page. Default is 10.
    - `sort` (String, optional): Sorting criteria in the format `property,(asc|desc)`. Default is `createdAt,desc`.
- **Request Body:** None
- **Response Body:** `CommonResponse<Page<PrescriptionResponseDTO>>` (Similar to Get My Orders response structure, but with `PrescriptionResponseDTO` content)

### 4. Delete Prescription
Deletes a prescription by its ID.

- **URL:** `/api/prescriptions/{id}`
- **Method:** `DELETE`
- **Authentication:** Requires Business Owner or Admin token.
- **Path Variable:**
    - `id` (String): The ID of the prescription to delete.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully deleted prescription",
      "data": null
    }
    ```

## Product Endpoints (`/api/products`)

### 1. Create Product
Creates a new product for a business.

- **URL:** `/api/products`
- **Method:** `POST`
- **Authentication:** Requires Business Owner token.
- **Request Body:** `ProductRequestDTO` (multipart/form-data)
    ```form-data
    name: Royal Canin Adult
    description: Makanan anjing dewasa
    price: 150000
    stockQuantity: 100
    category: FOOD
    businessId: {{business_id}}
    image: (file)
    ```
    - `businessId` (UUID): ID of the business owning the product.
    - `name` (String): Name of the product.
    - `category` (ProductCategory Enum): Category of the product (e.g., `FOOD`, `TOYS`, `ACCESSORIES`, `MEDICINE`).
    - `description` (String, optional): Description of the product.
    - `price` (BigDecimal): Price of the product.
    - `stockQuantity` (Integer): Quantity of the product in stock.
    - `image` (MultipartFile, optional): Product image file.
- **Response Body:** `ProductResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Successfully created product",
      "data": {
        "id": "p1r2o3d4-u5c6-7890-1234-567890abcdef",
        "businessId": "b1c2d3e4-f5g6-7890-1234-567890abcdef",
        "name": "Royal Canin Adult",
        "category": "FOOD",
        "description": "Makanan anjing dewasa",
        "price": 150000.00,
        "stockQuantity": 100,
        "imageUrl": "https://example.com/images/royal-canin.jpg",
        "isActive": true
      }
    }
    ```
    - `id` (UUID): Unique identifier for the product.
    - `businessId` (UUID): ID of the business that owns the product.
    - `name` (String): Name of the product.
    - `category` (ProductCategory Enum): Category of the product.
    - `description` (String): Description of the product.
    - `price` (BigDecimal): Price of the product.
    - `stockQuantity` (Integer): Quantity of the product in stock.
    - `imageUrl` (String): URL of the product image.
    - `isActive` (Boolean): Indicates if the product is active.

### 2. Get Product By ID
Retrieves a specific product by its ID.

- **URL:** `/api/products/{id}`
- **Method:** `GET`
- **Authentication:** None
- **Path Variable:**
    - `id` (UUID): The ID of the product to retrieve.
- **Request Body:** None
- **Response Body:** `ProductResponseDTO` (Same as Create Product response)

### 3. Get All Products
Retrieves a paginated list of all products.

- **URL:** `/api/products`
- **Method:** `GET`
- **Authentication:** None
- **Query Parameters (Pageable):**
    - `page` (Integer, optional): Page number (0-indexed). Default is 0.
    - `size` (Integer, optional): Number of items per page. Default is 10.
    - `sort` (String, optional): Sorting criteria in the format `property,(asc|desc)`. Default is `id,asc`.
- **Request Body:** None
- **Response Body:** `CommonResponse<Page<ProductResponseDTO>>` (Similar to Get My Orders response structure, but with `ProductResponseDTO` content)

### 4. Update Product
Updates an existing product's information.

- **URL:** `/api/products/{id}`
- **Method:** `PUT`
- **Authentication:** Requires Business Owner token.
- **Path Variable:**
    - `id` (UUID): The ID of the product to update.
- **Request Body:** `ProductRequestDTO` (multipart/form-data, all fields are optional for update, but typically you'd send the fields you want to change)
    ```form-data
    name: Royal Canin Adult Updated
    description: Makanan anjing dewasa terbaru
    price: 160000
    stockQuantity: 90
    category: FOOD
    businessId: {{business_id}}
    image: (file)
    ```
- **Response Body:** `ProductResponseDTO` (Same as Create Product response)

### 5. Delete Product
Deletes a product by its ID.

- **URL:** `/api/products/{id}`
- **Method:** `DELETE`
- **Authentication:** Requires Business Owner token.
- **Path Variable:**
    - `id` (UUID): The ID of the product to delete.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully deleted product",
      "data": null
    }
    ```

## Service Endpoints (`/api/services`)

### 1. Create Service
Creates a new service offered by a business.

- **URL:** `/api/services`
- **Method:** `POST`
- **Authentication:** Requires Business Owner token.
- **Request Body:** `ServiceRequestDTO` (multipart/form-data)
    ```form-data
    name: Grooming Kucing
    description: Layanan grooming lengkap untuk kucing
    basePrice: 75000
    category: GROOMING
    businessId: {{business_id}}
    image: (file)
    ```
    - `businessId` (UUID): ID of the business offering the service.
    - `category` (ServiceCategory Enum): Category of the service (e.g., `GROOMING`, `VACCINATION`, `CONSULTATION`).
    - `name` (String): Name of the service.
    - `basePrice` (BigDecimal): Base price of the service.
    - `capacityPerDay` (Integer, optional): Maximum number of times the service can be booked per day.
    - `image` (MultipartFile, optional): Service image file.
- **Response Body:** `ServiceResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Successfully created service",
      "data": {
        "id": "s1e2r3v4-i5c6-7890-1234-567890abcdef",
        "businessId": "b1c2d3e4-f5g6-7890-1234-567890abcdef",
        "category": "GROOMING",
        "name": "Grooming Kucing",
        "basePrice": 75000.00,
        "capacityPerDay": null,
        "imageUrl": "https://example.com/images/grooming-kucing.jpg",
        "isActive": true
      }
    }
    ```
    - `id` (UUID): Unique identifier for the service.
    - `businessId` (UUID): ID of the business offering the service.
    - `category` (ServiceCategory Enum): Category of the service.
    - `name` (String): Name of the service.
    - `basePrice` (BigDecimal): Base price of the service.
    - `capacityPerDay` (Integer): Maximum number of times the service can be booked per day.
    - `imageUrl` (String): URL of the service image.
    - `isActive` (Boolean): Indicates if the service is active.

### 2. Get Service By ID
Retrieves a specific service by its ID.

- **URL:** `/api/services/{id}`
- **Method:** `GET`
- **Authentication:** None
- **Path Variable:**
    - `id` (UUID): The ID of the service to retrieve.
- **Request Body:** None
- **Response Body:** `ServiceResponseDTO` (Same as Create Service response)

### 3. Get All Services
Retrieves a paginated list of all services.

- **URL:** `/api/services`
- **Method:** `GET`
- **Authentication:** None
- **Query Parameters (Pageable):**
    - `page` (Integer, optional): Page number (0-indexed). Default is 0.
    - `size` (Integer, optional): Number of items per page. Default is 10.
    - `sort` (String, optional): Sorting criteria in the format `property,(asc|desc)`. Default is `id,asc`.
- **Request Body:** None
- **Response Body:** `CommonResponse<Page<ServiceResponseDTO>>` (Similar to Get My Orders response structure, but with `ServiceResponseDTO` content)

### 4. Update Service
Updates an existing service's information.

- **URL:** `/api/services/{id}`
- **Method:** `PUT`
- **Authentication:** Requires Business Owner token.
- **Path Variable:**
    - `id` (UUID): The ID of the service to update.
- **Request Body:** `ServiceRequestDTO` (multipart/form-data, all fields are optional for update, but typically you'd send the fields you want to change)
    ```form-data
    name: Grooming Kucing Premium
    description: Layanan grooming lengkap untuk kucing dengan tambahan spa
    basePrice: 100000
    category: GROOMING
    businessId: {{business_id}}
    image: (file)
    ```
- **Response Body:** `ServiceResponseDTO` (Same as Create Service response)

### 5. Delete Service
Deletes a service by its ID.

- **URL:** `/api/services/{id}`
- **Method:** `DELETE`
- **Authentication:** Requires Business Owner token.
- **Path Variable:**
    - `id` (UUID): The ID of the service to delete.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully deleted service",
      "data": null
    }
    ```

## Shopping Cart Endpoints (`/api/cart`)

### 1. Add Item to Cart
Adds a product to the authenticated customer's shopping cart.

- **URL:** `/api/cart`
- **Method:** `POST`
- **Authentication:** Requires Customer token.
- **Request Body:** `AddToCartRequestDTO`
    ```json
    {
      "productId": "{{product_id}}",
      "quantity": 1,
      "businessId": "{{business_id}}"
    }
    ```
    - `productId` (UUID): ID of the product to add.
    - `quantity` (Integer): Quantity of the product to add (minimum 1).
    - `businessId` (UUID): ID of the business associated with the product.
- **Response Body:** `ShoppingCartResponseDTO`
    ```json
    {
      "statusCode": "CREATED",
      "message": "Successfully added item to cart",
      "data": {
        "id": "s1h2o3p4-c5a6-7890-1234-567890abcdef",
        "customerId": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
        "businessId": "b1c2d3e4-f5g6-7890-1234-567890abcdef",
        "businessName": "Intan Grooming House",
        "items": [
          {
            "id": "c1a2r3t4-i5t6-7890-1234-567890abcdef",
            "productId": "p1q2r3s4-t5u6-7890-1234-567890abcdef",
            "productName": "Royal Canin Adult",
            "productPrice": 150000.00,
            "quantity": 1,
            "subTotal": 150000.00
          }
        ],
        "totalPrice": 150000.00
      }
    }
    ```
    - `id` (UUID): Unique identifier for the shopping cart.
    - `customerId` (UUID): ID of the customer who owns the cart.
    - `businessId` (UUID): ID of the business whose products are in the cart.
    - `businessName` (String): Name of the business.
    - `items` (List<CartItemResponseDTO>): List of items in the cart.
        - `id` (UUID): Unique identifier for the cart item.
        - `productId` (UUID): ID of the product in the cart.
        - `productName` (String): Name of the product.
        - `productPrice` (BigDecimal): Price of the product per unit.
        - `quantity` (Integer): Quantity of the product in the cart.
        - `subTotal` (BigDecimal): Subtotal for this cart item.
    - `totalPrice` (BigDecimal): Total price of all items in the cart.

### 2. Get Shopping Cart
Retrieves the authenticated customer's shopping cart.

- **URL:** `/api/cart`
- **Method:** `GET`
- **Authentication:** Requires Customer token.
- **Request Body:** None
- **Response Body:** `ShoppingCartResponseDTO` (Same as Add Item to Cart response)

### 3. Update Cart Item Quantity
Updates the quantity of a specific item in the shopping cart.

- **URL:** `/api/cart`
- **Method:** `PUT`
- **Authentication:** Requires Customer token.
- **Request Body:** `UpdateCartItemRequestDTO`
    ```json
    {
      "cartItemId": "{{cart_item_id}}",
      "quantity": 2
    }
    ```
    - `cartItemId` (UUID): ID of the cart item to update.
    - `quantity` (Integer): New quantity for the cart item (minimum 1).
- **Response Body:** `ShoppingCartResponseDTO` (Same as Add Item to Cart response)

### 4. Remove Cart Item
Removes a specific item from the shopping cart.

- **URL:** `/api/cart/{cartItemId}`
- **Method:** `DELETE`
- **Authentication:** Requires Customer token.
- **Path Variable:**
    - `cartItemId` (UUID): The ID of the cart item to remove.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully removed item from cart",
      "data": null
    }
    ```

### 5. Clear Shopping Cart
Clears all items from the authenticated customer's shopping cart.

- **URL:** `/api/cart`
- **Method:** `DELETE`
- **Authentication:** Requires Customer token.
- **Request Body:** None
- **Response Body:** `CommonResponse<Void>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully cleared shopping cart",
      "data": null
    }
    ```

## User Endpoints (`/api/users`)

### 1. Get User By ID
Retrieves a specific user by their ID.

- **URL:** `/api/users/{id}`
- **Method:** `GET`
- **Authentication:** Requires Admin token.
- **Path Variable:**
    - `id` (String): The ID of the user to retrieve.
- **Request Body:** None
- **Response Body:** `UserResponseDTO`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully get user by id",
      "data": {
        "id": "u1s2e3r4-i5d6-7890-1234-567890abcdef",
        "email": "user@example.com",
        "name": "User Name",
        "address": "User Address",
        "phone": "081234567890"
      }
    }
    ```
    - `id` (String): Unique identifier for the user.
    - `email` (String): User's email address.
    - `name` (String): User's full name.
    - `address` (String): User's address.
    - `phone` (String): User's phone number.

### 2. Update User
Updates an existing user's information.

- **URL:** `/api/users`
- **Method:** `PUT`
- **Authentication:** Requires authenticated user's token.
- **Request Body:** `UserRequestDTO`
    ```json
    {
      "name": "New User Name",
      "address": "New User Address",
      "phone": "081234567899"
    }
    ```
    - `name` (String, optional): New name for the user.
    - `address` (String, optional): New address for the user.
    - `phone` (String, optional): New phone number for the user.
- **Response Body:** `UserResponseDTO` (Same as Get User By ID response)

### 3. Get All Users (Admin Only)
Retrieves a list of all registered users.

- **URL:** `/api/users`
- **Method:** `GET`
- **Authentication:** Requires Admin token.
- **Request Body:** None
- **Response Body:** `CommonResponse<List<UserResponseDTO>>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully get all users",
      "data": [
        // List of UserResponseDTO objects
      ]
    }
    ```

### 4. Delete User (Admin Only)
Deletes a user by their ID.

- **URL:** `/api/users/{id}`
- **Method:** `DELETE`
- **Authentication:** Requires Admin token.
- **Path Variable:**
    - `id` (String): The ID of the user to delete.
- **Request Body:** None
- **Response Body:** `CommonResponse<String>`
    ```json
    {
      "statusCode": "OK",
      "message": "Successfully delete user",
      "data": null
    }
    ```

### 5. Update User Status (Admin Only)
Updates the status (e.g., enable/disable) of a user.

- **URL:** `/api/users/{id}/status`
- **Method:** `PATCH`
- **Authentication:** Requires Admin token.
- **Path Variable:**
    - `id` (UUID): The ID of the user to update.
- **Query Parameters:**
    - `action` (String): The action to perform (e.g., `isEnable`, `isAccountNonLocked`).
    - `value` (Boolean): The boolean value to set for the action (e.g., `true`, `false`).
- **Request Body:** None
- **Response Body:** `UserResponseDTO`
    ```json
    {
      "statusCode": "OK",
      "message": "Berhasil mengubah status isEnable menjadi true",
      "data": {
        "id": "u1s2e3r4-i5d6-7890-1234-567890abcdef",
        "email": "user@example.com",
        "name": "User Name",
        "address": "User Address",
        "phone": "081234567890"
      }
    }
    ```
