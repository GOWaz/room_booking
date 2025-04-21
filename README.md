# Room Booking System

## Project Overview

This is a **Room Booking REST API** built using **Java** and **Spring Boot**. It allows hotel customers to view available rooms, book rooms, and cancel bookings. The application uses an in-memory H2 database for simplicity and supports advanced features like JWT-based security, caching, and logging.

### Core Entities

- **Room**
  - `Long id`: Unique identifier for each room
  - `String roomNumber`: Room number or identifier
  - `int capacity`: The capacity of the room
  - `BigDecimal pricePerNight`: The price per night for the room
  - `boolean available`: Availability status of the room (true or false)

- **Booking**
  - `Long id`: Unique identifier for each booking
  - `String customerName`: Name of the customer who booked the room
  - `LocalDate checkIn`: Check-in date
  - `LocalDate checkOut`: Check-out date
  - `BookingStatus status`: Status of the booking (Confirmed or Cancelled)
  - `Room room`: The room that has been booked

- **BookingStatus (Enum)**
  - `CONFIRMED`: The booking is confirmed
  - `CANCELLED`: The booking is cancelled

## API Endpoints

### 1. Rooms
- **GET /rooms**  
  List available rooms, filtered by availability.

- **POST /rooms/add**  
  Add a new room to the system.

### 2. Bookings
- **POST /bookings**  
  Create a new booking:
  - Validates room availability and the date range.
  - Marks room as unavailable upon successful booking.
  - Calculates the total price for the booking (nights × price_per_night).

- **PUT /bookings/cancel/{id}**  
  Cancel a booking:
  - Changes the booking status to `CANCELLED`.
  - Marks the room as available again.

- **GET /bookings/{id}**  
  View the details of a booking.

## Features

- **Room Availability**: Real-time updates on room availability.
- **Booking Management**: Create and cancel bookings.
- **Logging**: Audit logging for tracking operations (including booking creation, cancellation, etc.) logs are saved to `app.log`.
- **Caching**: Enhanced performance with room availability caching.
- **Security**: JWT-based security for API endpoints.
- **Auditing**: Ensure tracking database records changes.
- **Transactional Safety**: Ensures atomic operations (booking and cancelling) for data consistency.

### Booking Logic

#### Create Booking
- **Checks**:
  - Room exists and is available.
  - Valid dates (check-in is in the future, stay is between 1 and 30 nights).
  - No date conflicts with existing bookings.

- **Actions**:
  - Marks the room as unavailable.
  - Creates the booking with the status `CONFIRMED`.
  - Calculates the total price: `nights × price_per_night`.

- **Errors**:
  - `404` - Room not found.
  - `409` - Room unavailable or already booked.
  - `400` - Invalid dates or booking conflict.

#### Cancel Booking
- **Checks**:
  - Booking exists.
  - Booking is not already cancelled.

- **Actions**:
  - Marks the room as available.
  - Updates the booking status to `CANCELLED`.

- **Errors**:
  - `404` - Booking not found.
  - `400` - Booking already cancelled.

### Bonus Features
- **Prevent Overlapping Bookings**: Ensures no two bookings can overlap for the same room.
- **Email Confirmation**: Simulated email confirmation with a log message upon successful booking.
- **Swagger Documentation**: API documentation using `springdoc-openapi`.

### Email Simulation (Log Example)

When a booking is created, a simulated email confirmation log will be displayed in the application logs. The log starts with `[NOTIFY]` and contains all the booking details.

**Example:**
```
[NOTIFY] Booking created - ID: 1, Name: GOWaz, Room: 1A, Dates: 2025-04-22 to 2025-04-28, Total: $2.40, Status: CONFIRMED
```

## Setup Instructions

### Prerequisites
1. **JDK 17+**
2. **Maven** or **Gradle** (depending on your project setup)
3. **Postman** or any API client to test the endpoints.

### Clone the Repository

```bash
git clone https://github.com/GOWaz/room_booking.git
```

### Build the Project

1. Open the project in your preferred IDE (e.g., IntelliJ IDEA, Eclipse).
2. Install dependencies and build the project:

For Maven:
```bash
mvn clean install
```

For Gradle:
```bash
gradle build
```

### Run the Application

Start the Spring Boot application:
```bash
java -jar target/room_booking.jar
```

### API Documentation

Once the application is running, you can view the API documentation at:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### H2 Console

You can access the H2 database console to view and manage your in-memory database at:

- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:roomsDB`
  - **Username**: `sa`
  - **Password**: (leave blank)

### Authentication (Login and Token Retrieval)

To interact with the secured endpoints in the API, you must first log in to obtain a JWT token.
1. **POST /api/auth/register**
   Use the following credentials to register and retrieve a JWT token:
   - **Request**:
   ```bash
   POST /api/auth/login
   {
     "username": "your_username",
     "password": "your_password"
     "role": "ADMIN or USER"
   }
   ```

   - **Response**:
   ```json
   {
     "token": "your_jwt_token",
     "refresh_token":"your_refresh_token"
   }
   ```

2. **POST /api/auth/login**
   Use the following credentials to log in and retrieve a JWT token:

   - **Request**:
   ```bash
   POST /api/auth/login
   {
     "username": "your_username",
     "password": "your_password"
   }
   ```

   - **Response**:
   ```json
   {
     "token": "your_jwt_token",
     "refresh_token":"your_refresh_token"
   }
   ```

3. Once you have the JWT token, you can use it in Swagger UI for testing secured endpoints.
   - **In Swagger UI**, click the "Authorize" button on the top right.
   - Enter the token like this: `Bearer your_jwt_token`.
   - copy it from the log displayed in the application logs, `debug` log start with `Role`.

**Example:**
```
Role [ADMIN] Token [eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJHT1dheiIsImlhdCI6MTc0NTI2MDU5OSwiZXhwIjoxNzQ2MTI0NTk5fQ.tUYJ4OiB6H85PYiLggiYuMmvlVVmqad7lO6jhJI-rDc]
```

### Security

- JWT tokens are required for accessing restricted endpoints. 
- The login endpoint for obtaining the JWT token is:
  - **POST /api/auth/login**: Accepts user credentials and returns a JWT token.
  - **POST /api/auth/register**: Create user and returns a JWT token.

## Technologies Used

- **Java** - Core language.
- **Spring Boot** - Framework for building the REST API.
- **H2 Database** - In-memory database for simplicity.
- **JWT** - For secure API access.
- **Caching** - To optimize room availability checks.
- **Logging** - Audit logging for tracking actions.
- **Swagger** - For API documentation.

---

## 👤 Author

- [GOWaz](https://github.com/GOWaz)

---