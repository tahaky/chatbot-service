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

## 6. Continue Conversation (Explicit Method) 🆕

Explicitly continue an existing conversation:

```bash
curl -X POST http://localhost:8080/api/chat/continue \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "message": "Can you give me more details?"
  }'
```

**Note:** If the session doesn't exist and you provide a `userId`, a new session will be created:

```bash
curl -X POST http://localhost:8080/api/chat/continue \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "non-existent-session-id",
    "message": "Hello!",
    "userId": "user123"
  }'
```

If the sessionId is invalid and no userId is provided, it returns a 404 error.

## 7. Get User Session Summaries 🆕

Get a quick overview of all user sessions without full message history:

```bash
curl -X GET http://localhost:8080/api/chat/sessions/user123/summaries
```

Response:
```json
[
  {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "createdAt": "2024-02-18T10:30:00",
    "updatedAt": "2024-02-18T10:35:00",
    "messageCount": 4,
    "lastMessage": "The main advantages of Spring Boot include...",
    "lastMessageRole": "assistant"
  },
  {
    "sessionId": "650e8400-e29b-41d4-a716-446655440001",
    "userId": "user123",
    "createdAt": "2024-02-18T09:00:00",
    "updatedAt": "2024-02-18T09:15:00",
    "messageCount": 6,
    "lastMessage": "I hope that helps! Let me know if you need anything else.",
    "lastMessageRole": "assistant"
  }
]
```

**Use case:** Display a list of conversations in a UI without loading full message history.

## 8. Get Session Message History 🆕

Get message history for a session with optional limit:

```bash
# Get all messages
curl -X GET http://localhost:8080/api/chat/session/550e8400-e29b-41d4-a716-446655440000/history

# Get only the last 10 messages
curl -X GET "http://localhost:8080/api/chat/session/550e8400-e29b-41d4-a716-446655440000/history?limit=10"
```

Response:
```json
[
  {
    "role": "user",
    "content": "Hello! Can you help me understand what Spring Boot is?",
    "timestamp": "2024-02-18T10:30:00"
  },
  {
    "role": "assistant",
    "content": "Hello! Of course, I'd be happy to help...",
    "timestamp": "2024-02-18T10:30:05"
  }
]
```

**Use case:** Load conversation history incrementally or paginate through messages.

## 9. Python Example

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

## 10. JavaScript/Node.js Example

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

## 11. Advanced Python Example - Using New Features 🆕

This example demonstrates using session summaries, continuing conversations, and retrieving history:

```python
import requests

BASE_URL = "http://localhost:8080"

def list_user_conversations(user_id):
    """Get all conversation summaries for a user"""
    response = requests.get(f"{BASE_URL}/api/chat/sessions/{user_id}/summaries")
    summaries = response.json()
    
    print(f"\nConversations for {user_id}:")
    for i, summary in enumerate(summaries, 1):
        print(f"{i}. Session {summary['sessionId'][:8]}...")
        print(f"   Messages: {summary['messageCount']}")
        print(f"   Last: {summary['lastMessage'][:50]}...")
        print(f"   Updated: {summary['updatedAt']}")
    
    return summaries

def continue_conversation(session_id, message, user_id=None):
    """Continue an existing conversation. Creates new session if userId is provided and session doesn't exist."""
    payload = {
        "sessionId": session_id,
        "message": message
    }
    if user_id:
        payload["userId"] = user_id
    
    response = requests.post(
        f"{BASE_URL}/api/chat/continue",
        json=payload
    )
    
    if response.status_code == 200:
        data = response.json()
        return data['message']
    elif response.status_code == 404:
        print("Error: Session not found!")
        return None
    else:
        print(f"Error: {response.status_code}")
        return None

def get_recent_messages(session_id, limit=5):
    """Get the last N messages from a session"""
    response = requests.get(
        f"{BASE_URL}/api/chat/session/{session_id}/history",
        params={"limit": limit}
    )
    return response.json()

# Example usage
if __name__ == "__main__":
    user_id = "user123"
    
    # Start a new conversation
    print("Starting new conversation...")
    response = requests.post(
        f"{BASE_URL}/api/chat/send",
        json={
            "userId": user_id,
            "message": "Hello! I'd like to learn about microservices."
        }
    )
    session_id = response.json()["sessionId"]
    print(f"Created session: {session_id}")
    print(f"AI: {response.json()['message']}\n")
    
    # Continue the conversation using the new endpoint
    print("Continuing conversation...")
    ai_response = continue_conversation(
        session_id, 
        "What are the main benefits?"
    )
    print(f"AI: {ai_response}\n")
    
    # Add one more message
    ai_response = continue_conversation(
        session_id,
        "Can you give me an example?"
    )
    print(f"AI: {ai_response}\n")
    
    # Get recent messages
    print("Recent conversation history:")
    recent = get_recent_messages(session_id, limit=4)
    for msg in recent:
        role = msg['role'].upper()
        content = msg['content'][:80] + "..." if len(msg['content']) > 80 else msg['content']
        print(f"{role}: {content}")
    
    # List all user conversations
    print("\n" + "="*50)
    summaries = list_user_conversations(user_id)
    
    # Try to continue with an invalid session (demonstrates validation)
    print("\n" + "="*50)
    print("Testing with invalid session ID (no userId)...")
    result = continue_conversation("invalid-session-id", "Hello")
    
    # Try to continue with an invalid session but with userId (auto-creates session)
    print("\n" + "="*50)
    print("Testing with invalid session ID (with userId - auto-creates)...")
    result = continue_conversation("another-invalid-session", "Hello, I want to start fresh!", user_id=user_id)
    if result:
        print(f"AI (new session): {result}")
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
