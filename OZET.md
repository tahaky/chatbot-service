# Chatbot Servisi - Proje Özeti

## Genel Bakış

Bu proje, OpenAI API'si ile entegre edilmiş, MongoDB ile chat geçmişini saklayan, Java 17 ve Spring Boot kullanılarak geliştirilmiş tam özellikli bir chatbot servisidir.

## İstenen Özellikler ve Gerçekleştirme Durumu

### ✅ Tamamlanan Özellikler

1. **Java 17 ve Spring Boot**
   - ✅ Java 17 ile geliştirildi
   - ✅ Spring Boot 3.2.2 kullanıldı
   - ✅ Maven build sistemi

2. **OpenAI API Entegrasyonu**
   - ✅ GPT-3.5-turbo ve GPT-4 desteği
   - ✅ Yapılandırılabilir model seçimi
   - ✅ Özelleştirilebilir initial prompt
   - ✅ Context-aware (bağlam farkında) yanıtlar

3. **MongoDB Entegrasyonu**
   - ✅ Chat geçmişi MongoDB'de saklanıyor
   - ✅ Kullanıcı bazlı oturum takibi
   - ✅ Tam mesaj geçmişi
   - ✅ Timestamp desteği

4. **Session Yönetimi**
   - ✅ UUID ile otomatik session ID oluşturma
   - ✅ Kullanıcı ID parametresi
   - ✅ Oturum sürekliliği
   - ✅ Çoklu oturum desteği

5. **REST API Endpoints**
   - ✅ POST /api/chat/send - Mesaj gönder
   - ✅ GET /api/chat/sessions/{userId} - Kullanıcı oturumları
   - ✅ GET /api/chat/session/{sessionId} - Oturum detayları

6. **Swagger Dokümantasyonu**
   - ✅ OpenAPI 3.0 spesifikasyonu
   - ✅ İnteraktif Swagger UI
   - ✅ Tam API dokümantasyonu

7. **Docker Desteği**
   - ✅ Dockerfile
   - ✅ docker-compose.yml
   - ✅ MongoDB container yapılandırması
   - ✅ Health check'ler

## Proje Yapısı

```
chatbot-service/
├── src/
│   ├── main/
│   │   ├── java/com/chatbot/service/
│   │   │   ├── ChatbotServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenAiConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   └── ChatController.java
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequest.java
│   │   │   │   └── ChatResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── OpenAiException.java
│   │   │   │   └── SessionNotFoundException.java
│   │   │   ├── model/
│   │   │   │   ├── ChatMessage.java
│   │   │   │   └── ChatSession.java
│   │   │   ├── repository/
│   │   │   │   └── ChatSessionRepository.java
│   │   │   └── services/
│   │   │       └── ChatbotService.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/chatbot/service/
│           └── ChatbotServiceApplicationTests.java
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
├── API_EXAMPLES.md
└── start.sh
```

## Kullanılan Teknolojiler

### Backend Framework
- **Spring Boot 3.2.2**: Ana framework
- **Spring Data MongoDB**: MongoDB entegrasyonu
- **Spring Web**: REST API
- **Spring Validation**: Input validasyonu
- **Spring Actuator**: Health check ve monitoring

### Kütüphaneler
- **OpenAI GPT-3 Java 0.18.2**: OpenAI API client
- **SpringDoc OpenAPI 2.3.0**: Swagger/OpenAPI dokümantasyonu
- **Lombok**: Boilerplate kod azaltma
- **MongoDB Driver**: MongoDB bağlantısı

### DevOps
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration
- **Maven**: Build tool

## API Kullanımı

### 1. Mesaj Gönderme

```bash
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Merhaba! Spring Boot nedir?",
    "contextData": "İsteğe bağlı context bilgisi"
  }'
```

### 2. Oturum Geçmişi Alma

```bash
curl http://localhost:8080/api/chat/session/{sessionId}
```

### 3. Kullanıcı Oturumlarını Listeleme

```bash
curl http://localhost:8080/api/chat/sessions/{userId}
```

## Yapılandırma

### Ortam Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|-----------|
| `OPENAI_API_KEY` | OpenAI API anahtarınız | Zorunlu |
| `MONGODB_URI` | MongoDB bağlantı string'i | mongodb://localhost:27017/chatbot |
| `OPENAI_MODEL` | Kullanılacak model | gpt-3.5-turbo |
| `INITIAL_PROMPT` | Sistem prompt'u | You are a helpful assistant... |

## Çalıştırma

### Hızlı Başlangıç

```bash
# 1. Otomatik başlatma scripti
./start.sh

# 2. Manuel başlatma
export OPENAI_API_KEY=your-api-key-here
docker-compose up -d

# 3. Logları görüntüleme
docker-compose logs -f chatbot-service
```

### Erişim URL'leri

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## Güvenlik

### Yapılan Kontroller

1. ✅ **CodeQL Analizi**: Güvenlik açığı bulunamadı
2. ✅ **Bağımlılık Kontrolü**: Bilinen zafiyet yok
3. ✅ **Code Review**: Tüm öneriler uygulandı

### Güvenlik Özellikleri

- ✅ Custom exception handling
- ✅ Input validation
- ✅ Error sanitization
- ✅ MongoDB injection koruması (Spring Data)
- ✅ Non-root Docker user
- ✅ Health check endpoints

## Özellikler

### İş Mantığı Özellikleri

1. **Otomatik Session Yönetimi**
   - UUID ile benzersiz session ID
   - Kullanıcı bazlı session izolasyonu
   - Session sürekliliği

2. **Mesaj Geçmişi**
   - Tam konuşma geçmişi
   - Timestamp'li mesajlar
   - Role-based mesaj türleri (user, assistant, system)

3. **Context Support**
   - İsteğe bağlı context data
   - Initial prompt yapılandırması
   - Geçmiş mesajlar ile context oluşturma

4. **Hata Yönetimi**
   - Özel exception sınıfları
   - Global exception handler
   - Kullanıcı dostu hata mesajları

### Teknik Özellikler

1. **Modüler Mimari**
   - Katmanlı mimari (Controller-Service-Repository)
   - Dependency Injection
   - Separation of Concerns

2. **Yapılandırılabilirlik**
   - Environment variables
   - External configuration
   - Profile-based config

3. **Monitoring**
   - Spring Actuator
   - Health checks
   - MongoDB connection monitoring

4. **Documentation**
   - Swagger/OpenAPI
   - README
   - API örnekleri
   - Kod yorumları

## Test

```bash
# Unit testleri çalıştırma
mvn test

# Build
mvn clean package

# Docker build
docker build -t chatbot-service .
```

## Dokümantasyon

- **README.md**: Genel dokümantasyon ve kurulum kılavuzu
- **API_EXAMPLES.md**: Detaylı API kullanım örnekleri
- **Swagger UI**: İnteraktif API dokümantasyonu

## Gelecek Geliştirmeler İçin Öneriler

1. **Authentication & Authorization**
   - JWT token tabanlı auth
   - User management
   - Role-based access control

2. **Rate Limiting**
   - API rate limiting
   - User-based quotas
   - Cost tracking

3. **Caching**
   - Redis cache
   - Response caching
   - Session caching

4. **Advanced Features**
   - File upload support
   - Image generation
   - Multi-language support
   - Conversation export

5. **Monitoring & Observability**
   - Prometheus metrics
   - Grafana dashboards
   - Distributed tracing
   - ELK stack integration

## Lisans

MIT License

## Destek

Sorular ve öneriler için GitHub issue açabilirsiniz.
