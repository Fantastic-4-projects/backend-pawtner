# Panduan Implementasi Push Notification Pawtner (Expo)

Dokumen ini adalah panduan lengkap bagi tim Mobile (Expo) untuk mengintegrasikan sistem push notification dari backend Pawtner. Tujuannya adalah untuk mengimplementasikan fungsionalitas notifikasi dasar sesuai dengan fitur yang sudah ada di backend.

## Daftar Isi
1.  [Prasyarat: Konfigurasi Proyek Expo](#1-prasyarat-konfigurasi-proyek-expo)
2.  [Langkah 1: Mendapatkan Expo Push Token](#2-langkah-1-mendapatkan-expo-push-token)
3.  [Langkah 2: Mengirim Token ke Backend](#3-langkah-2-mengirim-token-ke-backend)
4.  [Langkah 3: Menangani Notifikasi yang Masuk](#4-langkah-3-menangani-notifikasi-yang-masuk)
5.  [Struktur Payload Notifikasi & Deep Linking](#5-struktur-payload-notifikasi--deep-linking)
6.  [Daftar Pemicu Notifikasi yang Ada](#6-daftar-pemicu-notifikasi-yang-ada)

---

## 1. Prasyarat: Konfigurasi Proyek Expo

Pastikan proyek Expo Anda dikonfigurasi dengan benar untuk bisa menerima notifikasi.

### 1.1 Instalasi Dependensi

Jalankan perintah berikut di root proyek Anda:
```bash
npx expo install expo-notifications expo-device expo-constants
```

### 1.2 Konfigurasi `app.json`

Ini adalah langkah **KRUSIAL**. Buka file `app.json` Anda dan pastikan konfigurasi berikut ada.

```json
{
  "expo": {
    // ...konfigurasi expo Anda yang lain...
    "android": {
      "adaptiveIcon": {
        // ...
      },
      "package": "com.namaperusahaan.pawtner" // Ganti dengan package name unik Anda
    },
    // ---- BLOK WAJIB UNTUK NOTIFIKASI ----
    "plugins": [
      [
        "expo-notifications",
        {
          "icon": "./assets/notification-icon.png", // Path ke ikon notifikasi (misal: 96x96px)
          "color": "#FF6347" // Warna aksen notifikasi di Android (sesuaikan dengan brand)
        }
      ]
    ],
    "extra": {
      "eas": {
        "projectId": "YOUR_EXPO_PROJECT_ID_HERE" // Salin dari dashboard.expo.dev
      }
    }
    // ---- AKHIR DARI BLOK WAJIB ----
  }
}
```
> **Penting:** Ganti `YOUR_EXPO_PROJECT_ID_HERE` dengan Project ID yang Anda dapatkan dari [dashboard Expo](https://expo.dev/projects) Anda. Tanpa ini, `getExpoPushTokenAsync` akan gagal.

---

## 2. Langkah 1: Mendapatkan Expo Push Token

Kita perlu meminta izin kepada pengguna dan mendapatkan *Expo Push Token* yang unik untuk setiap perangkat. Kode ini sebaiknya dijalankan saat aplikasi pertama kali dimuat.

```javascript
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { Platform } from 'react-native';

// Fungsi untuk mendapatkan token
export async function registerForPushNotificationsAsync() {
  let token;

  // Wajib untuk Android
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
  }

  // Notifikasi hanya berfungsi di perangkat fisik
  if (!Device.isDevice) {
    alert('Push Notifications hanya berfungsi di perangkat fisik, bukan di emulator.');
    return;
  }
  
  // Meminta izin dari pengguna
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;
  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }
  if (finalStatus !== 'granted') {
    alert('Izin notifikasi ditolak oleh pengguna.');
    return;
  }

  // Mendapatkan token
  try {
    const projectId = Constants.expoConfig?.extra?.eas?.projectId;
    if (!projectId) {
        throw new Error('Project ID tidak ditemukan di app.json. Pastikan sudah dikonfigurasi.');
    }
    token = (await Notifications.getExpoPushTokenAsync({ projectId })).data;
    console.log('Expo Push Token Anda:', token);
  } catch (error) {
    console.error("Gagal mendapatkan push token:", error);
    alert('Gagal mendapatkan push token: ' + error.message);
  }

  return token;
}
```
**Rekomendasi:** Panggil fungsi ini di komponen root aplikasi Anda (misal `App.js`) dan simpan token yang didapat ke dalam sebuah `state` agar bisa digunakan saat login.

---

## 3. Langkah 2: Mengirim Token ke Backend

Token perangkat **harus** dikirim ke backend agar server tahu ke mana harus mengirim notifikasi.

### Kapan Mengirim Token?
**Saat Pengguna Login.** Ini adalah momen paling logis untuk mengasosiasikan sebuah perangkat dengan seorang pengguna.

### Cara Mengirim
Modifikasi request login Anda untuk menyertakan `fcmToken`.

*   **Endpoint:** `POST /api/auth/login`
*   **Request Body:**

```json
{
  "email": "customer@example.com",
  "password": "password123",
  "fcmToken": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]" // Token yang didapat dari Langkah 1
}
```

Backend akan secara otomatis menyimpan token ini dan mengaitkannya dengan akun pengguna yang berhasil login.

---

## 4. Langkah 3: Menangani Notifikasi yang Masuk

Ada dua skenario utama yang harus ditangani:
1.  Notifikasi diterima saat aplikasi sedang **terbuka (foreground)**.
2.  Pengguna **mengetuk (tap)** notifikasi dari *notification tray* (saat aplikasi di background/tertutup).

Gunakan *listeners* dari `expo-notifications` di komponen root aplikasi Anda.

```javascript
import React, { useEffect, useRef } from 'react';
import *. as Notifications from 'expo-notifications';
import { useNavigation } from '@react-navigation/native'; // Contoh jika pakai React Navigation

// Di dalam komponen root App.js
function App() {
  const notificationListener = useRef();
  const responseListener = useRef();
  const navigation = useNavigation(); // Contoh untuk deep linking

  useEffect(() => {
    // Handler global untuk notifikasi
    Notifications.setNotificationHandler({
      handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
      }),
    });

    // Listener untuk notifikasi saat aplikasi TERBUKA
    notificationListener.current = Notifications.addNotificationReceivedListener(notification => {
      console.log('Notifikasi diterima saat foreground:', notification);
      // Anda bisa menampilkan banner custom di sini jika mau
    });

    // Listener saat pengguna MENGETUK notifikasi
    responseListener.current = Notifications.addNotificationResponseReceivedListener(response => {
      console.log('Notifikasi diketuk:', response);
      const data = response.notification.request.content.data;
      
      // Implementasi Deep Linking
      handleNotificationNavigation(data);
    });

    return () => {
      Notifications.removeNotificationSubscription(notificationListener.current);
      Notifications.removeNotificationSubscription(responseListener.current);
    };
  }, []);

  // Fungsi untuk navigasi (deep linking)
  const handleNotificationNavigation = (data) => {
    if (data && data.type) {
      switch (data.type) {
        case 'ORDER_UPDATE':
        case 'NEW_ORDER':
          // Arahkan ke halaman detail pesanan dengan ID yang diberikan
          navigation.navigate('OrderDetailScreen', { orderId: data.id });
          break;
        // case lain bisa ditambahkan di sini jika ada fitur baru
        default:
          console.log('Tipe notifikasi tidak dikenal:', data.type);
      }
    }
  };

  // ... sisa kode komponen aplikasi Anda
}
```

---

## 5. Struktur Payload Notifikasi & Deep Linking

Backend akan mengirim notifikasi dengan struktur yang konsisten. Perhatikan `data`, karena ini adalah kunci untuk *deep linking* (membuka halaman spesifik saat notifikasi diketuk).

**Contoh Payload dari Backend:**

```json
{
  "to": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]",
  "sound": "default",
  "title": "Status Pesanan Diperbarui! 📦",
  "body": "Pesanan Anda #ORD-ABC123 telah dikonfirmasi oleh Pawtner Pet Shop.",
  "data": {
    "type": "ORDER_UPDATE",
    "id": "c4a5b6c7-d8e9-f0a1-b2c3-d4e5f6a7b8c9" // UUID dari pesanan
  }
}
```

Aplikasi mobile akan menggunakan `data.type` dan `data.id` untuk menavigasi pengguna ke halaman yang tepat, seperti yang ditunjukkan pada contoh di [Langkah 3](#4-langkah-3-menangani-notifikasi-yang-masuk).

---

## 6. Daftar Pemicu Notifikasi yang Ada

Saat ini, backend akan mengirimkan notifikasi untuk kejadian berikut:

| Pemicu | Target Notifikasi | Contoh Pesan | `data.type` | `data.id` |
| :--- | :--- | :--- | :--- | :--- |
| **Checkout Pesanan** | Pelanggan | "Order Successful! Your order [orderNumber] has been placed." | `NEW_ORDER` | `orderId` |
| **Update Status (via Webhook)** | Pelanggan | "Order Status Updated. Your order [orderNumber] is now [newStatus]." | `ORDER_UPDATE` | `orderId` |

Fitur notifikasi untuk modul lain seperti **Booking** atau **Resep** belum diimplementasikan di backend.