# Rangkuman Implementasi Biaya Pengiriman Berbasis Wilayah (Perhitungan Jarak Sederhana)

Dokumen ini merangkum diskusi mengenai implementasi biaya pengiriman yang dinamis berdasarkan lokasi, sebagai alternatif dari biaya tetap yang ada saat ini. Fokus utama adalah pada pendekatan perhitungan jarak sederhana menggunakan koordinat geografis yang sudah ada.

## 1. Latar Belakang Masalah

Saat ini, proyek Pawtner menerapkan biaya pengiriman tetap sebesar Rp 10.000 per pesanan yang berlaku. Untuk meningkatkan akurasi dan relevansi biaya pengiriman, diperlukan sistem yang menghitung biaya berdasarkan jarak antara lokasi bisnis dan lokasi pelanggan.

## 2. Solusi yang Dipilih: Perhitungan Jarak Sederhana

Pendekatan yang dipilih untuk tahap awal adalah menggunakan perhitungan jarak garis lurus (straight-line distance) antara koordinat lintang (latitude) dan bujur (longitude) bisnis dan pelanggan. Biaya pengiriman akan ditentukan berdasarkan tingkatan jarak yang telah ditetapkan. Pendekatan ini dipilih karena lebih cepat diimplementasikan dibandingkan integrasi API eksternal seperti RajaOngkir, yang dapat menjadi peningkatan di masa mendatang.

## 3. Perubahan pada Database

Untuk mendukung perhitungan biaya pengiriman berbasis lokasi, beberapa penyesuaian pada skema database diperlukan:

*   **Tabel `users`**:
    *   Tambahkan kolom `latitude` (DECIMAL(9, 6)) dan `longitude` (DECIMAL(9, 6)).
    *   **Penting**: Kolom `address` (string) tetap dipertahankan. `address` digunakan untuk input yang dapat dibaca manusia dan keperluan logistik, sementara `latitude`/`longitude` digunakan untuk perhitungan geografis yang akurat dan efisien. Data `latitude`/`longitude` akan diisi melalui proses geocoding dari alamat yang dimasukkan pengguna saat pendaftaran atau pembaruan profil.

*   **Tabel `delivery_fee_tiers` (Direkomendasikan)**:
    *   Buat tabel baru untuk menyimpan tingkatan biaya pengiriman. Ini memungkinkan konfigurasi biaya yang fleksibel tanpa perlu mengubah kode backend.
    *   **Struktur Contoh**:
        ```sql
        CREATE TABLE delivery_fee_tiers (
            id SERIAL PRIMARY KEY,
            min_distance_km DECIMAL(10, 2) NOT NULL,
            max_distance_km DECIMAL(10, 2) NOT NULL,
            fee DECIMAL(10, 2) NOT NULL,
            UNIQUE (min_distance_km, max_distance_km)
        );
        ```
    *   **Contoh Data Awal**:
        ```sql
        INSERT INTO delivery_fee_tiers (min_distance_km, max_distance_km, fee) VALUES
        (0.0, 5.0, 15000.00),   -- 0-5 km
        (5.01, 10.0, 25000.00),  -- 5.01-10 km
        (10.01, 20.0, 40000.00), -- 10.01-20 km
        (20.01, 9999.99, 60000.00); -- >20 km (atau jarak maksimum yang masuk akal)
        ```

*   **Tabel `orders`**:
    *   Tambahkan kolom `delivery_fee` (DECIMAL(10, 2) DEFAULT 0.00) untuk menyimpan biaya pengiriman spesifik yang diterapkan pada setiap pesanan.

## 4. Logika Backend (Java/Spring Boot)

Logika inti akan diimplementasikan di sisi backend:

*   **Utilitas Perhitungan Jarak (`DistanceCalculator.java`)**:
    *   Sebuah kelas utilitas akan dibuat untuk menghitung jarak geografis antara dua pasang koordinat lintang/bujur menggunakan rumus Haversine.
    *   **Contoh Fungsi**: `public static double calculateDistance(double lat1, double lon1, double lat2, double lon2)`

*   **Layanan Biaya Pengiriman (`DeliveryFeeService.java`)**:
    *   Layanan baru ini akan bertanggung jawab untuk:
        1.  Menerima ID pelanggan dan ID bisnis.
        2.  Mengambil koordinat `latitude` dan `longitude` pelanggan dari tabel `users`.
        3.  Mengambil koordinat `latitude` dan `longitude` bisnis dari tabel `businesses`.
        4.  Memanggil `DistanceCalculator.calculateDistance()` untuk mendapatkan jarak dalam kilometer.
        5.  Mencari di tabel `delivery_fee_tiers` (atau konfigurasi hardcoded jika tabel tidak digunakan) untuk menentukan biaya pengiriman yang sesuai dengan jarak yang dihitung.
        6.  Mengembalikan nilai biaya pengiriman.
    *   **Penanganan Kasus Khusus**: Perlu dipertimbangkan penanganan jika lokasi tidak tersedia atau jika jarak melebihi tingkatan yang ditentukan.

*   **Integrasi ke `ShoppingCartService`**:
    *   Metode `getShoppingCartByCustomerId` akan diperbarui untuk memanggil `DeliveryFeeService` dan menambahkan biaya pengiriman ke total keranjang belanja (`ShoppingCartResponseDTO` akan diperbarui untuk menyertakan `deliveryFee` dan `totalAmountWithDelivery`).

*   **Integrasi ke `OrderService` (Checkout)**:
    *   Di metode `createOrderFromCart`, setelah menghitung subtotal produk, `DeliveryFeeService` akan dipanggil untuk mendapatkan biaya pengiriman.
    *   Biaya pengiriman ini akan ditambahkan ke `total_amount` pesanan sebelum disimpan ke database dan diteruskan ke Midtrans.
    *   Nilai `delivery_fee` juga akan disimpan ke kolom baru di tabel `orders`.

## 5. Pertimbangan Frontend (React Native)

Perubahan pada frontend akan berfokus pada input lokasi pengguna dan tampilan biaya:

*   **Pengambilan Lokasi Pengguna**:
    *   Saat pendaftaran atau di halaman profil, pengguna akan diminta untuk memasukkan alamat mereka.
    *   Alamat ini akan di-geocoding (diubah menjadi `latitude` dan `longitude`) di backend (atau frontend lalu dikirim ke backend) dan disimpan di profil pengguna.
    *   Ini memastikan bahwa koordinat lokasi pengguna selalu tersedia tanpa perlu input ulang saat checkout.

*   **Tampilan Keranjang & Checkout**:
    *   UI keranjang belanja akan diperbarui untuk menampilkan "Biaya Pengiriman" sebagai item terpisah.
    *   "Total Pembayaran" akan mencerminkan penambahan biaya pengiriman ini.
    *   Pesan yang jelas akan ditampilkan jika pengiriman ke lokasi pengguna tidak memungkinkan atau di luar jangkauan.

## 6. Pengujian

Pengujian menyeluruh sangat penting untuk memastikan fungsionalitas yang benar:

*   **Unit Test**: Untuk `DistanceCalculator` dan `DeliveryFeeService` dengan berbagai skenario jarak dan tingkatan biaya.
*   **Integration Test**: Untuk alur keranjang belanja dan checkout, memverifikasi bahwa biaya pengiriman dihitung dan diterapkan dengan benar.
*   **End-to-End Test**: Mensimulasikan pengguna di lokasi yang berbeda untuk memvalidasi variasi biaya pengiriman dan penanganan kasus-kasus ekstrem.

Dengan langkah-langkah ini, Pawtner dapat mengimplementasikan sistem biaya pengiriman berbasis wilayah yang lebih akurat dan dinamis, meningkatkan pengalaman pengguna dan fleksibilitas bisnis.
