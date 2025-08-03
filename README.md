# EventHub Backend

A comprehensive event management system backend built with Spring Boot, providing RESTful APIs for event creation, booking management, payment processing, and real-time notifications.

## 🚀 Features

### Core Functionality
- **Event Management**: Create, update, delete, and search events with advanced filtering
- **User Authentication**: JWT-based authentication with role-based access control (USER/ADMIN)
- **Booking System**: Complete booking lifecycle with ticket generation and QR codes
- **Payment Processing**: Multi-method payment support (Card, UPI, Net Banking, Wallet)
- **Review System**: User reviews and ratings for events
- **Real-time Updates**: WebSocket integration for live notifications and updates

### Advanced Features
- **Admin Dashboard**: Comprehensive analytics and statistics
- **PDF Generation**: Ticket and invoice generation
- **QR Code Generation**: For ticket verification
- **File Upload Support**: Event images and documents
- **Audit Logging**: Complete audit trail for all operations
- **Health Checks**: System health monitoring endpoints

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **Security**: Spring Security with JWT
- **Real-time**: WebSocket with STOMP
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Java Version**: 17+

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Git

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/eventhub-backend.git
cd eventhub-backend
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE eventhub_db;

-- Create user (optional)
CREATE USER 'eventhub_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON eventhub_db.* TO 'eventhub_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration
Update `src/main/resources/application.yml` or `application.properties`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/eventhub_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: root
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: your_jwt_secret_key_here
  expiration: 86400000
```

### 4. Build and Run
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/eventhub-backend-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
- **URL**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

### Health Check
- **Health**: `http://localhost:8080/api/health`
- **Detailed Health**: `http://localhost:8080/api/health/detailed`

## 🔐 Authentication

### User Registration
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890"
}
```

### User Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Admin Registration
```bash
POST /api/auth/register-admin
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "admin123",
  "phone": "+1234567890"
}
```

## 🎯 API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/register` | User registration | Public |
| POST | `/api/auth/register-admin` | Admin registration | Public |
| POST | `/api/auth/validate-token` | Token validation | Public |

### Events
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/events` | Get all active events | Public |
| GET | `/api/events/{id}` | Get event by ID | Public |
| POST | `/api/events` | Create new event | Admin |
| PUT | `/api/events/{id}` | Update event | Admin |
| DELETE | `/api/events/{id}` | Delete event | Admin |
| GET | `/api/events/search?keyword={keyword}` | Search events | Public |
| GET | `/api/events/category/{category}` | Get events by category | Public |
| GET | `/api/events/filter` | Filter events with criteria | Public |

### Bookings
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/bookings` | Create booking | User/Admin |
| GET | `/api/bookings/{id}` | Get booking by ID | User/Admin |
| GET | `/api/bookings/user/current` | Get current user bookings | User/Admin |
| PUT | `/api/bookings/{id}/cancel` | Cancel booking | User/Admin |
| PUT | `/api/bookings/{id}/confirm` | Confirm booking | Admin |
| GET | `/api/bookings/{id}/ticket/pdf` | Download ticket PDF | User/Admin |

### Payments
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/payments/process` | Process payment | User/Admin |
| GET | `/api/payments/transaction/{transactionId}` | Get payment by transaction ID | User/Admin |
| POST | `/api/payments/refund` | Process refund | Admin |
| POST | `/api/payments/card` | Process card payment | User/Admin |
| POST | `/api/payments/upi` | Process UPI payment | User/Admin |

### Reviews
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/reviews` | Create review | User/Admin |
| GET | `/api/reviews/event/{eventId}` | Get reviews for event | Public |
| PUT | `/api/reviews/{id}` | Update review | User/Admin |
| DELETE | `/api/reviews/{id}` | Delete review | User/Admin |

### Admin Dashboard
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/admin/dashboard` | Get dashboard data | Admin |
| GET | `/api/admin/analytics` | Get analytics data | Admin |
| GET | `/api/admin/revenue-report` | Get revenue report | Admin |

### Users
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users/profile` | Get current user profile | User/Admin |
| PUT | `/api/users/profile/{id}` | Update user profile | User/Admin |
| GET | `/api/users` | Get all users | Admin |
| GET | `/api/users/search` | Search users | Admin |

## 🔄 Real-time Features

### WebSocket Connection
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to event updates
    stompClient.subscribe('/topic/events', function (message) {
        console.log('Event update:', JSON.parse(message.body));
    });
    
    // Subscribe to specific event
    stompClient.subscribe('/topic/events/123', function (message) {
        console.log('Event 123 update:', JSON.parse(message.body));
    });
    
    // Subscribe to seat updates
    stompClient.subscribe('/topic/seats/123', function (message) {
        console.log('Seat update:', JSON.parse(message.body));
    });
});
```

### Available Subscriptions
- `/topic/events` - Global event notifications
- `/topic/events/{eventId}` - Specific event updates
- `/topic/seats/{eventId}` - Seat availability updates
- `/topic/reviews/{eventId}` - Review updates
- `/topic/admin/dashboard` - Admin dashboard updates
- `/user/queue/notifications` - User-specific notifications
- `/user/queue/bookings` - User booking updates

## 📊 Database Schema

### Core Entities
- **Users**: User accounts with roles
- **Events**: Event information and availability
- **Bookings**: Booking records with status tracking
- **Payments**: Payment transactions and methods
- **Reviews**: User reviews and ratings
- **Roles**: User role definitions

### Relationships
- User 1:N Bookings
- User 1:N Reviews
- Event 1:N Bookings
- Event 1:N Reviews
- Booking 1:1 Payment
- User N:M Roles

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EventControllerTest

# Run with coverage
mvn test jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer testing
- **Integration Tests**: Controller and repository testing
- **Security Tests**: Authentication and authorization testing

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim

COPY target/eventhub-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=eventhub_db
DB_USERNAME=root
DB_PASSWORD=password

# JWT
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
```

## 📈 Monitoring

### Health Checks
- **Basic Health**: `/api/health`
- **Detailed Health**: `/api/health/detailed`
- **Actuator**: `/actuator/health`

### Metrics
- Database connection health
- Memory usage
- Active WebSocket connections
- API response times

## 🔧 Configuration

### Application Properties
Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/eventhub_db
spring.datasource.username=root
spring.datasource.password=password

# JWT
jwt.secret=your_secret_key
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# WebSocket
spring.websocket.sockjs.enabled=true
spring.websocket.sockjs.heartbeat-time=25000
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Check MySQL is running
   - Verify database credentials
   - Ensure database exists

2. **JWT Token Issues**
   - Check token expiration
   - Verify JWT secret configuration
   - Ensure proper token format

3. **WebSocket Connection Problems**
   - Check CORS configuration
   - Verify WebSocket endpoints
   - Check firewall settings

4. **File Upload Issues**
   - Check file size limits
   - Verify upload directory permissions
   - Check file type restrictions

## 📝 Development

### Code Structure
```
src/
├── main/
│   ├── java/com/eventhub/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── model/          # Entity classes
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.yml # Application configuration
│       └── static/         # Static resources
└── test/                   # Test classes
```

### Development Guidelines
- Follow Spring Boot best practices
- Use proper exception handling
- Implement comprehensive logging
- Write unit and integration tests
- Document API changes
- Follow RESTful conventions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 📞 Support

For support and questions:
- **Issues**: [GitHub Issues](https://github.com/your-username/eventhub-backend/issues)

## 🎯 Roadmap

### Upcoming Features
- [ ] Email notifications
- [ ] SMS integration
- [ ] Advanced analytics
- [ ] Event recommendations
- [ ] Social media integration
- [ ] Mobile app API enhancements
- [ ] Multi-language support
- [ ] Advanced reporting
- [ ] Integration with external payment gateways
- [ ] Event recurring functionality

---

**EventHub Backend** - Powering seamless event management experiences 🎉
