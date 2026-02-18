# Chatbot Service - OpenAI Integration

OpenAI Chatbot Integration Service built with Java 17, Spring Boot, and MongoDB. This service provides RESTful APIs to interact with OpenAI's GPT models while maintaining conversation history in MongoDB.

## Features

- ✅ OpenAI GPT integration (GPT-3.5-turbo/GPT-4)
- ✅ MongoDB for persistent chat history storage
- ✅ Session management with UUID-based session IDs
- ✅ User-based conversation tracking
- ✅ Configurable initial prompts
- ✅ Context-aware responses
- ✅ RESTful API endpoints
- ✅ Swagger/OpenAPI documentation
- ✅ Docker and Docker Compose support
- ✅ Health checks and monitoring

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- OpenAI API key

## Quick Start

### Option 1: Quick Start Script (Recommended)

```bash
# Run the quick start script
./start.sh
```

The script will:
- Check prerequisites
- Create .env from template if needed
- Validate configuration
- Start all services
- Display service URLs

### Option 2: Manual Start with Docker Compose

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=your-api-key-here

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f chatbot-service
```

The service will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
- Health Check: http://localhost:8080/actuator/health

### Option 3: Run Locally (Development)

#### Start MongoDB

```bash
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  --name mongodb mongo:7.0
```

#### Run the application

```bash
# Set environment variables
export OPENAI_API_KEY=your-api-key-here
export MONGODB_URI=mongodb://admin:admin123@localhost:27017/chatbot?authSource=admin

# Build and run
mvn clean package
java -jar target/chatbot-service-1.0.0.jar
```

## API Endpoints

### 1. Send Message

Send a message to the chatbot and receive an AI-generated response.

```bash
POST /api/chat/send
Content-Type: application/json

{
  "userId": "user123",
  "message": "Hello, how are you?",
  "sessionId": "optional-existing-session-id",
  "contextData": "Optional context information for the AI"
}
```

Response:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Hello! I'm doing well, thank you for asking. How can I help you today?",
  "userId": "user123",
  "timestamp": 1708257600000
}
```

### 2. Get User Sessions

Retrieve all chat sessions for a specific user.

```bash
GET /api/chat/sessions/{userId}
```

### 3. Get Session Details

Retrieve details of a specific chat session including full message history.

```bash
GET /api/chat/session/{sessionId}
```

## Configuration

Configuration is done through environment variables or `application.yml`:

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | Your OpenAI API key | Required |
| `MONGODB_URI` | MongoDB connection string | mongodb://localhost:27017/chatbot |
| `OPENAI_MODEL` | OpenAI model to use | gpt-3.5-turbo |
| `INITIAL_PROMPT` | System prompt for AI | You are a helpful assistant... |

## Docker Commands

### Build the image

```bash
docker build -t chatbot-service .
```

### Run with custom configuration

```bash
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  -e MONGODB_URI=mongodb://host:27017/chatbot \
  --name chatbot-service \
  chatbot-service
```

### Stop services

```bash
docker-compose down
```

### Remove all data

```bash
docker-compose down -v
```

## Development

### Build the project

```bash
mvn clean package
```

### Run tests

```bash
mvn test
```

### Access Swagger UI

Once the application is running, visit:
http://localhost:8080/swagger-ui.html

## Architecture

### Components

- **Controller Layer**: REST endpoints (`ChatController`)
- **Service Layer**: Business logic and OpenAI integration (`ChatbotService`)
- **Repository Layer**: MongoDB data access (`ChatSessionRepository`)
- **Model Layer**: Domain entities (`ChatSession`, `ChatMessage`)
- **DTO Layer**: Data transfer objects (`ChatRequest`, `ChatResponse`)

### Data Model

```
ChatSession
├── id: String (MongoDB ID)
├── sessionId: String (UUID)
├── userId: String
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
├── initialPrompt: String
└── messages: List<ChatMessage>
    ├── role: String (system/user/assistant)
    ├── content: String
    └── timestamp: LocalDateTime
```

## Monitoring

Health check endpoint:
```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### MongoDB Connection Issues

Ensure MongoDB is running and accessible:
```bash
docker-compose ps
docker-compose logs mongodb
```

### OpenAI API Errors

- Verify your API key is correct
- Check your OpenAI account has sufficient credits
- Review rate limits and quotas

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue on GitHub.
