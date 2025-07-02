# Konfigurasi Lingkungan Lokal (.env)

Proyek ini menggunakan file `.env` untuk mengelola variabel lingkungan yang sensitif atau spesifik untuk lingkungan pengembangan lokal. File `.env` **TIDAK** di-commit ke repositori Git.

## Langkah-langkah untuk Menyiapkan Lingkungan Lokal Anda:

1.  **Buat File `.env`:**
    Di direktori root proyek Anda (`/home/alrifqidarmawan/Projects/Backend/final-project/backend-pawtner/`), buat file baru bernama `.env`.

2.  **Salin Konten Berikut ke File `.env` Anda:**
    ```dotenv
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/pawtner_db
DB_USERNAME=postgres
DB_PASSWORD=root

# JWT Configuration
JWT_SECRET=secretkeysecretkeysecretkeysecretkey
JWT_APP_NAME=pawtner
JWT_EXPIRATION=86400

# Email Configuration
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=90c09c001@smtp-brevo.com
MAIL_PASSWORD=RCjXSP6JDHhxBzNq
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLE=true
MAIL_TRANSPORT_PROTOCOL=smtp
MAIL_DEBUG=true

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=dhqueh6fp
CLOUDINARY_API_KEY=243335233355126
CLOUDINARY_API_SECRET=AJJ5PyKUraSazml_ZJxnSULw03Y

# Midtrans Configuration
MIDTRANS_SERVER_KEY=SB-Mid-server-rR6embYnc2Lz0zXHE4QzzFB_
MIDTRANS_CLIENT_KEY=SB-Mid-client-hoO-dwyPyxu-bGXx
MIDTRANS_IS_PRODUCTION=false

# Google OAuth2 Configuration
GOOGLE_CLIENT_ID=739333513795-qeorp8uavenl8ru78vhu6g2bhlqgam5t.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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
GOOGLE_SCOPE=openid, profile, email
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Frontend URL Configuration
RESET_PASSWORD_URL=http://localhost:3000/reset-password

# Delivery Fee Configuration
DELIVERY_FEE=10000
    ```

3.  **Pastikan Dependensi Terinstal:**
    Pastikan Anda memiliki dependensi `dotenv-java` di `pom.xml` Anda. Jika tidak, tambahkan:
    ```xml
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>2.3.2</version>
    </dependency>
    ```

4.  **Jalankan Aplikasi Anda:**
    Aplikasi Spring Boot Anda sekarang akan secara otomatis memuat variabel-variabel ini saat startup.

## Catatan Penting:

*   **Jangan Commit `.env`:** File `.env` Anda sudah ditambahkan ke `.gitignore`. Pastikan Anda tidak pernah secara manual menambahkan atau melakukan commit file ini ke repositori Git.
*   **Variabel Lingkungan Sistem:** Sebagai alternatif, Anda juga dapat mengatur variabel lingkungan ini langsung di sistem operasi Anda atau melalui konfigurasi IDE Anda. Namun, menggunakan file `.env` adalah cara yang disarankan untuk pengembangan lokal.
