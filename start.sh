#!/bin/bash

# Chatbot Service Quick Start Script
# This script helps you quickly start the chatbot service

set -e

echo "=========================================="
echo "Chatbot Service Quick Start"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed."
    echo "Please install Docker from https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: docker-compose is not installed."
    echo "Please install docker-compose from https://docs.docker.com/compose/install/"
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  No .env file found. Creating from .env.example..."
    cp .env.example .env
    echo ""
    echo "⚠️  IMPORTANT: Please edit .env and set your OPENAI_API_KEY"
    echo "   You can get an API key from https://platform.openai.com/api-keys"
    echo ""
    read -p "Press Enter after you've set your API key in .env..."
fi

# Check if OPENAI_API_KEY is set
source .env
if [ -z "$OPENAI_API_KEY" ] || [ "$OPENAI_API_KEY" = "your-openai-api-key-here" ]; then
    echo "❌ Error: OPENAI_API_KEY is not set in .env file"
    echo "Please edit .env and set your OpenAI API key"
    exit 1
fi

echo "✅ Configuration validated"
echo ""

# Start services
echo "🚀 Starting services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
if docker-compose ps | grep -q "chatbot-service.*Up"; then
    echo ""
    echo "=========================================="
    echo "✅ Chatbot Service is running!"
    echo "=========================================="
    echo ""
    echo "📝 Swagger UI:      http://localhost:8080/swagger-ui.html"
    echo "📋 API Docs:        http://localhost:8080/api-docs"
    echo "❤️  Health Check:   http://localhost:8080/actuator/health"
    echo ""
    echo "📖 See API_EXAMPLES.md for usage examples"
    echo ""
    echo "To view logs:       docker-compose logs -f chatbot-service"
    echo "To stop services:   docker-compose down"
    echo "=========================================="
else
    echo ""
    echo "❌ Error: Service failed to start"
    echo "Check logs with: docker-compose logs chatbot-service"
    exit 1
fi
