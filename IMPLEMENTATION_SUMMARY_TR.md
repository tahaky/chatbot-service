# Geçmiş Mesajlarla Sohbete Devam Etme Özelliği

## Özet

Bu PR, chatbot servisine geçmiş mesajlarla sohbete devam etme özelliklerini ekler ve geliştirir. Kullanıcılar artık önceki konuşmalarını daha kolay bir şekilde takip edebilir ve devam ettirebilir.

## Eklenen Özellikler

### 1. Açık Devam Etme Endpoint'i 🆕
**Endpoint:** `POST /api/chat/continue`

Var olan bir sohbeti açıkça devam ettirmek için yeni bir endpoint. Bu endpoint, oturum kimliğinin geçerli olup olmadığını doğrular.

**Örnek Kullanım:**
```bash
curl -X POST http://localhost:8080/api/chat/continue \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "message": "Daha fazla detay verebilir misin?"
  }'
```

**Özellikler:**
- Oturum var mı kontrol eder
- Geçersiz oturum kimliği için 404 hatası döner
- Tam mesaj geçmişi ile AI'ya gönderir

### 2. Oturum Özetleri 🆕
**Endpoint:** `GET /api/chat/sessions/{userId}/summaries`

Kullanıcının tüm sohbetlerinin hızlı bir özetini alır. Tam mesaj geçmişi olmadan sadece özet bilgiler döner.

**Örnek Kullanım:**
```bash
curl http://localhost:8080/api/chat/sessions/user123/summaries
```

**Dönen Bilgiler:**
- Oturum kimliği
- Mesaj sayısı
- Son mesaj (ilk 100 karakter)
- Son mesajın rolü (user/assistant)
- Oluşturma ve güncelleme tarihleri

**Kullanım Senaryosu:** Kullanıcı arayüzünde konuşma listesi göstermek için idealdir.

### 3. Sayfalanmış Mesaj Geçmişi 🆕
**Endpoint:** `GET /api/chat/session/{sessionId}/history?limit=N`

Bir oturumun mesaj geçmişini isteğe bağlı limit ile alır.

**Örnek Kullanım:**
```bash
# Tüm mesajları al
curl http://localhost:8080/api/chat/session/{sessionId}/history

# Sadece son 10 mesajı al
curl "http://localhost:8080/api/chat/session/{sessionId}/history?limit=10"
```

**Kullanım Senaryosu:** Mesaj geçmişini aşamalı olarak yüklemek veya sayfalamak için kullanılır.

## İyileştirmeler

### Gelişmiş Oturum Doğrulama
Önceden, geçersiz bir `sessionId` sağlandığında sistem sessizce yeni bir oturum oluşturuyordu. Bu kafa karıştırıcıydı.

**Şimdi:** Geçersiz `sessionId` için `SessionNotFoundException` fırlatılır.

```java
// Önceki davranış (kafa karıştırıcı):
if (sessionId != null && !sessionId.isEmpty()) {
    return repository.findBySessionId(sessionId)
        .orElseGet(() -> createNewSession(userId));  // Sessizce yeni oturum
}

// Yeni davranış (açık):
if (sessionId != null && !sessionId.isEmpty()) {
    return repository.findBySessionId(sessionId)
        .orElseThrow(() -> new SessionNotFoundException(...));  // Hata fırlat
}
```

## Teknik Değişiklikler

### Yeni DTO'lar
1. **ContinueChatRequest**: Sohbete devam etmek için
2. **SessionSummary**: Oturum özetleri için

### Yeni Servis Metodları
1. `continueConversation()` - Mevcut sohbeti devam ettir
2. `getUserSessionSummaries()` - Kullanıcı oturum özetlerini al
3. `getSessionHistory()` - Oturum geçmişini al (limit ile)

### Yeni Controller Endpoint'leri
1. `POST /api/chat/continue`
2. `GET /api/chat/sessions/{userId}/summaries`
3. `GET /api/chat/session/{sessionId}/history`

## Test Kapsamı

7 yeni test eklendi:
- ✅ `testGetUserSessionSummaries` - Oturum özetlerini alma
- ✅ `testGetSessionHistory` - Tam geçmişi alma
- ✅ `testGetSessionHistoryWithLimit` - Sınırlı geçmiş alma
- ✅ `testGetSessionHistoryNotFound` - Geçersiz oturum hatası
- ✅ `testContinueConversationSuccess` - Başarılı devam etme
- ✅ `testContinueConversationSessionNotFound` - Geçersiz oturum hatası
- ✅ `testSessionSummaryTruncatesLongMessage` - Uzun mesaj kesme

**Tüm testler geçti:** 7/7 ✓

## Güvenlik

- ✅ CodeQL analizi: Sorun bulunamadı
- ✅ Code review: Sorun bulunamadı
- ✅ Oturum doğrulama geliştirildi
- ✅ Kullanıcı yetkilendirmesi korundu

## Dokümantasyon

### Güncellenen Dosyalar
- ✅ `README.md` - Yeni özellikler eklendi
- ✅ `API_EXAMPLES.md` - Detaylı örnekler ve kullanım senaryoları
- ✅ Python örnekleri - Yeni özellikler gösterildi

## Kullanım Örnekleri

### Senaryo 1: Önceki Sohbeti Devam Ettir

```python
import requests

BASE_URL = "http://localhost:8080"

# Kullanıcının oturumlarını listele
summaries = requests.get(
    f"{BASE_URL}/api/chat/sessions/user123/summaries"
).json()

# En son oturumu seç
latest_session = summaries[0]['sessionId']

# Sohbete devam et
response = requests.post(
    f"{BASE_URL}/api/chat/continue",
    json={
        "sessionId": latest_session,
        "message": "Önceki konuşmaya devam edelim"
    }
)

print(response.json()['message'])
```

### Senaryo 2: Konuşma Geçmişini Göster

```python
# Son 5 mesajı al
history = requests.get(
    f"{BASE_URL}/api/chat/session/{session_id}/history",
    params={"limit": 5}
).json()

for msg in history:
    role = msg['role'].upper()
    content = msg['content']
    print(f"{role}: {content}")
```

## Geriye Dönük Uyumluluk

✅ **Tüm mevcut API'ler korundu**
- Eski endpoint'ler aynı şekilde çalışıyor
- Sadece yeni özellikler eklendi
- Mevcut istemciler etkilenmedi

**Tek değişiklik:** `POST /api/chat/send` endpoint'i artık geçersiz `sessionId` için hata fırlatıyor (yeni oturum oluşturmak yerine). Bu daha doğru bir davranıştır.

## Sonraki Adımlar

Bu özellikler şunları mümkün kılar:
1. ✅ Kullanıcılar önceki sohbetleri listeleyebilir
2. ✅ Kullanıcılar herhangi bir oturuma dönebilir
3. ✅ Mesaj geçmişi artımlı olarak yüklenebilir
4. ✅ UI'da konuşma önizlemeleri gösterilebilir
5. ✅ Hatalı durumlarda açık geri bildirim verilir

## Test Etme

Yeni özellikleri test etmek için:

```bash
# Servisi başlat
docker-compose up -d

# Test scriptini çalıştır
chmod +x /tmp/test_new_features.sh
/tmp/test_new_features.sh
```

Veya Swagger UI'ı kullan:
http://localhost:8080/swagger-ui.html

## Özet

Bu PR, "geçmiş mesajlarla sohbete devam etme" özelliğini ekler ve geliştirir. Kullanıcılar artık:
- Önceki konuşmalarını görebilir
- Herhangi bir oturuma dönüp devam edebilir  
- Mesaj geçmişini sayfalayabilir
- Hızlı oturum özetleri alabilir

Tüm değişiklikler test edildi, güvenlik açıkları kontrol edildi ve dokümante edildi.
