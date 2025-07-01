
Ah, paham! Jadi kamu ingin menyisipkan dokumentasi integrasi Midtrans ke dalam file README.md kamu—langsung dalam format markdown tanpa perlu file terpisah. Berikut adalah potongan Markdown siap tempel ke dalam README.md proyek kamu (berbahasa Indonesia, dengan format rapi untuk developer Java/Spring Boot):

## 💳 Integrasi Midtrans (Java - Spring Boot)

Platform **Pawtner** menggunakan Midtrans sebagai payment gateway untuk mendukung pembayaran produk dan layanan secara aman, terverifikasi, dan langsung di dalam aplikasi.

---

### 🔧 1. Persiapan Akun Midtrans

- Daftar di [https://midtrans.com](https://midtrans.com)
- Buat 2 proyek: **Sandbox** (pengujian) & **Production** (live)
- Ambil kredensial:
  - `Server Key`
  - `Client Key`

Tambahkan ke file `application.properties` atau `application.yml`:

```properties
midtrans.server-key=SB-Mid-server-xxxx
midtrans.client-key=SB-Mid-client-xxxx
midtrans.is-production=false

📦 2. Tambahkan Dependency di Maven

<dependency>
  <groupId>com.midtrans</groupId>
  <artifactId>midtrans-java</artifactId>
  <version>1.3.0</version>
</dependency>

🧠 3. Inisialisasi Midtrans Snap API

Midtrans.serverKey = serverKey;
Midtrans.clientKey = clientKey;
Midtrans.isProduction = isProduction;

MidtransSnapApi snapApi = new MidtransSnapApi(serverKey, clientKey, isProduction);

🚀 4. Membuat Transaksi Snap

Map<String, Object> transactionDetails = Map.of(
    "order_id", "ORDER-" + payment.getId(),
    "gross_amount", payment.getAmount().longValue()
);

Map<String, Object> customerDetails = Map.of(
    "first_name", user.getName(),
    "email", user.getEmail()
);

Map<String, Object> params = Map.of(
    "transaction_details", transactionDetails,
    "customer_details", customerDetails
);

JSONObject response = midtransSnapApi.createTransaction(params);
String snapToken = response.getString("token");
String redirectUrl = response.getString("redirect_url");

🔁 5. Menangani Webhook dari Midtrans

    Endpoint publik (misalnya): POST /api/payments/webhook

    Verifikasi signature (opsional tapi disarankan)

    Update status transaksi berdasarkan transaction_status:

        settlement → sukses

        expire, cancel, deny → gagal

    Simpan payload webhook untuk audit trail

🧪 6. Tes Pembayaran

Kartu Kredit Dummy (Sandbox):
Nomor Kartu	CVV	Expired
4811 1111 1111 1114	123	01/2027

Untuk QRIS/GoPay → gunakan popup Snap dan klik tombol “Bayar”.
🔒 7. Tips Keamanan

    Jangan expose server key ke frontend

    Validasi webhook menggunakan signature SHA-512

    Whitelist IP Midtrans (opsional)

📘 Referensi Resmi

    Dokumentasi: https://docs.midtrans.com

    SDK Java: https://github.com/veritrans/veritrans-java

📌 Endpoints Penting
Metode	Endpoint	Fungsi
POST	/api/orders/{id}/pay	Buat transaksi Snap (produk)
POST	/api/bookings/{id}/pay	Buat transaksi Snap (layanan)
POST	/api/payments/webhook	Terima notifikasi dari Midtrans
GET	/api/payments/{id}	Lihat status pembayaran

Dengan setup ini, kamu sudah bisa melakukan pembayaran online dari aplikasi Pawtner langsung ke Midtrans dengan Snap UI.


---

🎯 **Langkah selanjutnya**:
- Tempel langsung bagian ini ke dalam `README.md`
- Jika kamu menggunakan bahasa Inggris, saya bisa bantu translate juga
- Perlu bantuan bikin versi singkat untuk `docs/README.md` khusus frontend/backend? Bisa!

Let me know ya.


