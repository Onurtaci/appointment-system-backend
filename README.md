# Randevu ve Hasta Takip Sistemi

Bu proje, Ege Üniversitesi Mühendislik Fakültesi Bilgisayar Mühendisliği Bölümü Back-End Software Development dersi için geliştirilmiş bir randevu ve hasta takip sistemidir.

## 🚀 Özellikler

### Kullanıcı Rolleri
- **Hasta**: Kayıt olur, giriş yapar, randevu alır, geçmiş randevularını görür
- **Doktor**: Giriş yapar, kendisine gelen randevuları görür, onaylar veya reddeder, not ekler

### Randevu Sistemi
- ✅ Randevu tarihi ve saati seçimi
- ✅ Aynı saatte çakışan randevuların engellenmesi
- ✅ Randevu durumları: Beklemede, Onaylandı, Reddedildi
- ✅ **Gelişmiş randevu süresi yönetimi** (15-120 dakika arası, 15 dakikalık katları)
- ✅ **Otomatik müsait saat hesaplama**
- ✅ **Gelişmiş çakışma kontrolü**

### Hasta Takibi
- ✅ Doktorlar randevulara özel not ekleyebilir
- ✅ Hasta geçmiş randevularını ve doktor notlarını görebilir
- ✅ **Gelişmiş hasta geçmişi takibi**

### Doktor Çalışma Saatleri Yönetimi
- ✅ **Esnek çalışma saatleri** (Sabah, Öğleden Sonra, Tam Gün vardiyaları)
- ✅ **Öğle arası otomatik kontrolü** (12:00-13:00)
- ✅ **Haftalık çalışma programı yönetimi**
- ✅ **Müsait saat hesaplama algoritması**
- ✅ **Çalışma programı özeti**

### Güvenlik ve Kimlik Doğrulama
- ✅ JWT tabanlı kimlik doğrulama
- ✅ Rol tabanlı yetkilendirme (PATIENT, DOCTOR)
- ✅ Şifre hash'leme (BCrypt)

## 🛠️ Teknolojiler

### Backend
- **Spring Boot 3.2.3** - Ana framework
- **Spring Security** - Güvenlik ve kimlik doğrulama
- **Spring Data JPA** - Veritabanı erişimi
- **PostgreSQL** - Veritabanı
- **Flyway** - Veritabanı migration yönetimi
- **MapStruct** - DTO mapping
- **Lombok** - Kod kısaltma
- **JWT** - Token tabanlı kimlik doğrulama

### Frontend
- **React 18** - UI framework
- **TypeScript** - Tip güvenliği
- **Material-UI** - UI bileşenleri
- **Redux Toolkit** - State management
- **React Router** - Sayfa yönlendirme
- **Axios** - HTTP istekleri
- **Formik & Yup** - Form yönetimi ve validasyon

## 📋 Gereksinimler

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

## 🚀 Kurulum

### 1. Veritabanı Kurulumu

```sql
-- PostgreSQL'de veritabanı oluştur
CREATE DATABASE clinic_db;
CREATE USER clinic WITH PASSWORD 'clinic123';
GRANT ALL PRIVILEGES ON DATABASE clinic_db TO clinic;
```

### 2. Backend Kurulumu

```bash
cd AppointmentSystem

# Bağımlılıkları yükle
mvn clean install

# Veritabanı migration'larını çalıştır
mvn flyway:migrate

# Uygulamayı başlat
mvn spring-boot:run
```

Backend `http://localhost:8080` adresinde çalışacaktır.

### 3. Frontend Kurulumu

```bash
cd AppointmentSystemUI

# Bağımlılıkları yükle
npm install

# Geliştirme sunucusunu başlat
npm run dev
```

Frontend `http://localhost:3001` adresinde çalışacaktır.

## 📚 API Dokümantasyonu

Uygulama çalıştıktan sonra Swagger UI'a `http://localhost:8080/swagger-ui.html` adresinden erişebilirsiniz.

### Ana Endpoint'ler

#### Kimlik Doğrulama
- `POST /api/auth/register` - Kullanıcı kaydı
- `POST /api/auth/login` - Kullanıcı girişi
- `GET /api/auth/me` - Mevcut kullanıcı bilgileri

#### Randevu Yönetimi
- `POST /api/appointments` - Yeni randevu oluşturma
- `GET /api/appointments/me` - Hasta randevuları
- `GET /api/appointments/doctor/me` - Doktor randevuları
- `PATCH /api/appointments/{id}/status` - Randevu durumu güncelleme
- `POST /api/appointments/{id}/notes` - Randevuya not ekleme
- `GET /api/appointments/available-slots` - Müsait saatler

#### Doktor Çalışma Programı
- `POST /api/doctor-schedules/{doctorId}` - Çalışma programı oluşturma
- `GET /api/doctor-schedules/{doctorId}` - Çalışma programı listesi
- `PUT /api/doctor-schedules/{doctorId}/{scheduleId}` - Program güncelleme
- `DELETE /api/doctor-schedules/{doctorId}/{scheduleId}` - Program silme
- `GET /api/doctor-schedules/{doctorId}/available-slots` - Müsait saatler
- `GET /api/doctor-schedules/{doctorId}/availability` - Müsaitlik kontrolü
- `GET /api/doctor-schedules/{doctorId}/weekly-summary` - Haftalık özet

## 🧪 Test

```bash
# Backend testleri
cd AppointmentSystem
mvn test

# Frontend testleri
cd AppointmentSystemUI
npm test
```

## 📁 Proje Yapısı

```
AppointmentSystem/
├── src/
│   ├── main/
│   │   ├── java/com/clinic/appointmentsystem/
│   │   │   ├── domain/           # İş mantığı varlıkları
│   │   │   ├── application/      # Uygulama servisleri
│   │   │   ├── persistence/      # Veri erişim katmanı
│   │   │   ├── webapi/           # REST API controller'ları
│   │   │   └── infrastructure/   # Altyapı bileşenleri
│   │   └── resources/
│   │       ├── db/migration/     # Veritabanı migration'ları
│   │       └── application.yaml  # Konfigürasyon
│   └── test/                     # Test dosyaları
└── pom.xml

AppointmentSystemUI/
├── src/
│   ├── components/               # React bileşenleri
│   ├── pages/                    # Sayfa bileşenleri
│   ├── services/                 # API servisleri
│   ├── store/                    # Redux store
│   └── types/                    # TypeScript tipleri
└── package.json
```

## 🔧 Konfigürasyon

### Backend Konfigürasyonu (`application.yaml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/clinic_db
    username: clinic
    password: clinic123
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

jwt:
  secret: your-secret-key-here
  expiration: 3600000  # 1 saat
```

### Frontend Konfigürasyonu

API base URL'ini `src/services/api.ts` dosyasında değiştirebilirsiniz.

## 🚀 Deployment

### Backend Deployment

```bash
# JAR dosyası oluştur
mvn clean package

# JAR dosyasını çalıştır
java -jar target/appointment-system-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment

```bash
# Production build
npm run build

# Build dosyalarını web sunucusuna kopyala
```

## 📝 Geliştirici Notları

### Yeni Özellikler (v2.0)

1. **Gelişmiş Randevu Süresi Yönetimi**
   - 15-120 dakika arası esnek randevu süreleri
   - 15 dakikalık katları zorunluluğu
   - Otomatik süre validasyonu

2. **Akıllı Müsait Saat Hesaplama**
   - Doktor çalışma saatlerine göre otomatik hesaplama
   - Öğle arası otomatik çıkarılması
   - Çakışan randevuların otomatik filtrelenmesi

3. **Gelişmiş Çalışma Programı Yönetimi**
   - Haftalık program özeti
   - Müsaitlik kontrolü
   - Esnek vardiya sistemi

### Kod Kalitesi

- **Clean Architecture** prensipleri uygulanmıştır
- **SOLID** prensipleri takip edilmiştir
- **Comprehensive testing** ile test coverage sağlanmıştır
- **Detailed documentation** ile kod dokümantasyonu yapılmıştır

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add some amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.

## 👥 Geliştirici

- **Ege Üniversitesi Mühendislik Fakültesi**
- **Bilgisayar Mühendisliği Bölümü**
- **Back-End Software Development Dersi**

---

**Proje Teslim Tarihi**: 18 Haziran 2025 