# API Usage Examples

This document provides examples of how to use the Chatbot Service API.

## Base URL
```
http://localhost:8080
```

## 1. Send a Chat Message (New Session)

Creates a new chat session and sends the first message:

```bash
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Hello! Can you help me understand what Spring Boot is?"
  }'
```

Response:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Hello! Of course, I'd be happy to help. Spring Boot is a framework...",
  "userId": "user123",
  "timestamp": 1708257600000
}
```

## 2. Continue a Conversation (Existing Session)

Send another message in the same session:

```bash
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "What are the main advantages?",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

## 3. Send Message with Context Data

Provide additional context for more accurate responses:

```bash
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Based on this documentation, how do I configure the database?",
    "contextData": "Database Configuration: You can configure MongoDB using spring.data.mongodb.uri property..."
  }'
```

## 4. Get All Sessions for a User

Retrieve all chat sessions for a specific user:

```bash
curl -X GET http://localhost:8080/api/chat/sessions/user123
```

Response:
```json
[
  {
    "id": "65a1b2c3d4e5f6789abcdef0",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "createdAt": "2024-02-18T10:30:00",
    "updatedAt": "2024-02-18T10:35:00",
    "initialPrompt": "You are a helpful assistant...",
    "messages": [...]
  }
]
```

## 5. Get Specific Session Details

Retrieve full details including message history for a session:

```bash
curl -X GET http://localhost:8080/api/chat/session/550e8400-e29b-41d4-a716-446655440000
```

Response:
```json
{
  "id": "65a1b2c3d4e5f6789abcdef0",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "createdAt": "2024-02-18T10:30:00",
  "updatedAt": "2024-02-18T10:35:00",
  "initialPrompt": "You are a helpful assistant. Answer questions based on the provided context.",
  "messages": [
    {
      "role": "user",
      "content": "Hello! Can you help me understand what Spring Boot is?",
      "timestamp": "2024-02-18T10:30:00"
    },
    {
      "role": "assistant",
      "content": "Hello! Of course, I'd be happy to help. Spring Boot is a framework...",
      "timestamp": "2024-02-18T10:30:05"
    },
    {
      "role": "user",
      "content": "What are the main advantages?",
      "timestamp": "2024-02-18T10:35:00"
    },
    {
      "role": "assistant",
      "content": "The main advantages of Spring Boot include...",
      "timestamp": "2024-02-18T10:35:03"
    }
  ]
}
```

## 6. Python Example

```python
import requests

BASE_URL = "http://localhost:8080"

# Send a message
response = requests.post(
    f"{BASE_URL}/api/chat/send",
    json={
        "userId": "user123",
        "message": "What is Docker?",
        "contextData": "Docker is a platform for developing, shipping, and running applications in containers."
    }
)

data = response.json()
session_id = data["sessionId"]
print(f"AI Response: {data['message']}")

# Continue conversation
response = requests.post(
    f"{BASE_URL}/api/chat/send",
    json={
        "userId": "user123",
        "message": "Can you explain more about containers?",
        "sessionId": session_id
    }
)

print(f"AI Response: {response.json()['message']}")

# Get session history
history = requests.get(f"{BASE_URL}/api/chat/session/{session_id}").json()
print(f"Total messages in session: {len(history['messages'])}")
```

## 7. JavaScript/Node.js Example

```javascript
const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

async function chatWithBot() {
  try {
    // Send first message
    const response1 = await axios.post(`${BASE_URL}/api/chat/send`, {
      userId: 'user123',
      message: 'What is Kubernetes?',
    });
    
    const sessionId = response1.data.sessionId;
    console.log('AI:', response1.data.message);
    
    // Continue conversation
    const response2 = await axios.post(`${BASE_URL}/api/chat/send`, {
      userId: 'user123',
      message: 'How does it differ from Docker?',
      sessionId: sessionId,
    });
    
    console.log('AI:', response2.data.message);
    
    // Get full session history
    const history = await axios.get(`${BASE_URL}/api/chat/session/${sessionId}`);
    console.log('Session has', history.data.messages.length, 'messages');
    
  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
  }
}

chatWithBot();
```

## Health Check

Check if the service is running:

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

## Swagger UI

For interactive API documentation, visit:
```
http://localhost:8080/swagger-ui.html
```

## Error Handling

### Session Not Found (404)
```json
{
  "status": 404,
  "message": "Session not found: invalid-session-id",
  "timestamp": "2024-02-18T10:30:00"
}
```

### Validation Error (400)
```json
{
  "timestamp": "2024-02-18T10:30:00",
  "status": 400,
  "errors": {
    "userId": "User ID is required",
    "message": "Message is required"
  }
}
```

### OpenAI API Error (500)
```json
{
  "status": 500,
  "message": "Failed to process request: No response received from OpenAI API",
  "timestamp": "2024-02-18T10:30:00"
}
```
