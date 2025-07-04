Langkah 1: Tambahkan Dependensi yang Diperlukan

Kita memerlukan spring-boot-starter-web (yang biasanya sudah ada) dan spring-boot-starter-webflux untuk WebClient, yang merupakan cara modern dan non-blocking
untuk melakukan panggilan HTTP.


Buka file pom.xml Anda dan pastikan dependensi ini ada:



    1 <dependencies>
    2     <!-- Dependensi ini biasanya sudah ada -->
    3     <dependency>
    4         <groupId>org.springframework.boot</groupId>
    5         <artifactId>spring-boot-starter-web</artifactId>
    6     </dependency>
    7 
    8     <!-- Tambahkan ini untuk WebClient yang modern -->
    9     <dependency>
10         <groupId>org.springframework.boot</groupId>
11         <artifactId>spring-boot-starter-webflux</artifactId>
12     </dependency>
13
14     <!-- Dependensi lain yang mungkin sudah Anda miliki -->
15 </dependencies>

Jika Anda menggunakan Gradle, tambahkan `implementation 'org.springframework.boot:spring-boot-starter-webflux'` ke `build.gradle` Anda.

  ---

Langkah 2: Konfigurasi API Key dengan Aman


Buka file src/main/resources/application.properties. Kita akan menyimpan URL dan Kunci API di sini.



1 # ===============================================
2 # PAWTNER AI (GEMINI) CONFIGURATION
3 # ===============================================
4 pawtner.ai.gemini.api-url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
5 pawtner.ai.gemini.api-key=${GEMINI_API_KEY}



Penjelasan:
* pawtner.ai.gemini.api-url: Ini adalah URL endpoint Gemini.
* pawtner.ai.gemini.api-key=${GEMINI_API_KEY}: Ini adalah bagian terpenting. Kita memberitahu Spring untuk mengambil nilai API Key dari Environment Variable
  bernama GEMINI_API_KEY. Ini jauh lebih aman daripada menulis kunci langsung di file.


Untuk Pengembangan Lokal:
Anda bisa mengaturnya sementara di IDE Anda. Di IntelliJ, buka Run/Debug Configurations -> Application -> Environment variables dan tambahkan
GEMINI_API_KEY=kunci_api_anda_disini.

  ---


Langkah 3: Buat DTO (Data Transfer Objects)

Kita perlu membuat kelas Java untuk memodelkan data JSON yang kita kirim dan terima. Menggunakan record adalah cara yang modern dan ringkas.

1. Request & Response untuk Frontend:



1     // src/main/java/com/pawtner/.../dto/AiChatRequest.java
2     public record AiChatRequest(String message) {}
3
4     // src/main/java/com/pawtner/.../dto/AiChatResponse.java
5     public record AiChatResponse(String reply) {}


2. Request & Response untuk Gemini API: (Struktur ini harus cocok dengan dokumentasi Gemini)



    1     // src/main/java/com/pawtner/.../dto/gemini/GeminiRequest.java
    2     public record GeminiRequest(Content[] contents) {
    3         public static record Content(Part[] parts) {}
    4         public static record Part(String text) {}
    5     }
    6 
    7     // src/main/java/com/pawtner/.../dto/gemini/GeminiResponse.java
    8     public record GeminiResponse(Candidate[] candidates) {
    9         public static record Candidate(Content content) {}
10         public static record Content(Part[] parts) {}
11         public static record Part(String text) {}
12     }


  ---

Langkah 4: Buat Service untuk Memanggil Gemini


Ini adalah inti dari logika kita. Service ini akan bertanggung jawab untuk berkomunikasi dengan Gemini.



    1 // src/main/java/com/pawtner/.../service/GeminiAiService.java
    2 import org.springframework.beans.factory.annotation.Value;
    3 import org.springframework.http.MediaType;
    4 import org.springframework.stereotype.Service;
    5 import org.springframework.web.reactive.function.client.WebClient;
    6 import reactor.core.publisher.Mono;
    7 
    8 @Service
    9 public class GeminiAiService {
10
11     private final WebClient webClient;
12
13     @Value("${pawtner.ai.gemini.api-key}")
14     private String apiKey;
15
16     public GeminiAiService(WebClient.Builder webClientBuilder, @Value("${pawtner.ai.gemini.api-url}") String apiUrl) {
17         this.webClient = webClientBuilder.baseUrl(apiUrl).build();
18     }
19
20     public Mono<String> getChatResponse(String userMessage) {
21         // Membuat body request sesuai struktur DTO GeminiRequest
22         var requestBody = new GeminiRequest(
23             new GeminiRequest.Content[]{
24                 new GeminiRequest.Content(
25                     new GeminiRequest.Part[]{
26                         new GeminiRequest.Part(userMessage)
27                     }
28                 )
29             }
30         );
31
32         return webClient.post()
33                 .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
34                 .contentType(MediaType.APPLICATION_JSON)
35                 .bodyValue(requestBody)
36                 .retrieve() // Mengirim request dan mendapatkan response
37                 .bodyToMono(GeminiResponse.class) // Konversi response JSON ke DTO kita
38                 .map(geminiResponse -> {
39                     // Ekstrak teks dari struktur response yang kompleks
40                     if (geminiResponse != null && geminiResponse.candidates() != null && geminiResponse.candidates().length > 0) {
41                         return geminiResponse.candidates()[0].content().parts()[0].text();
42                     }
43                     return "Sorry, I couldn't get a response.";
44                 })
45                 .onErrorResume(e -> {
46                     // Tangani jika terjadi error saat memanggil API
47                     System.err.println("Error calling Gemini API: " + e.getMessage());
48                     return Mono.just("Sorry, there was an error connecting to the AI service.");
49                 });
50     }
51 }



  ---

Langkah 5: Buat Controller untuk Frontend

Ini adalah endpoint yang akan dipanggil oleh aplikasi React Native Anda.



    1 // src/main/java/com/pawtner/.../controller/AiController.java
    2 import org.springframework.web.bind.annotation.*;
    3 import reactor.core.publisher.Mono;
    4 
    5 @RestController
    6 @RequestMapping("/api/ai")
    7 // Penting untuk pengembangan lokal agar tidak kena masalah CORS
    8 @CrossOrigin(origins = "*")
    9 public class AiController {
10
11     private final GeminiAiService geminiAiService;
12
13     public AiController(GeminiAiService geminiAiService) {
14         this.geminiAiService = geminiAiService;
15     }
16
17     @PostMapping("/chat")
18     public Mono<AiChatResponse> handleChat(@RequestBody AiChatRequest request) {
19         return geminiAiService.getChatResponse(request.message())
20                 .map(AiChatResponse::new); // Konversi String response ke AiChatResponse DTO
21     }
22 }


  ---

Langkah 6: Modifikasi Frontend (`PawAiScreen.js`)


Terakhir, ubah PawAiScreen.js untuk memanggil backend Spring Boot Anda, bukan API Gemini secara langsung.



    1 // Di dalam PawAiScreen.js
    2 
    3 // Ganti dengan URL backend Anda. Gunakan IP asli jika menjalankan di device fisik.
    4 const YOUR_BACKEND_API_URL = 'http://localhost:8080/api/ai/chat';
    5 
    6 const handleSend = async () => {
    7     if (input.trim().length === 0) return;
    8 
    9     const userMessageText = input;
10     const userMessage = { id: Math.random().(), text: userMessageText, sender: 'user' };
11     setMessages(prevMessages => [...prevMessages, userMessage]);
12     setInput('');                           o
13                                             S
14     // Logika darurat tetap di frontend untuk respons cepat
15     const lowerCaseInput = userMessageText.toLowerCase();
16     const keywords = ['sick', 'emergency', 'symptom', 'vomit', 'lethargic', 'pain'];
17     if (keywords.some(keyword => lowerCaseInput.includes(keyword))) {
18         const emergencyResponse = { /* ... pesan darurat seperti sebelumnya ... */ };
19         setMessages(prevMessages => [...prevMessages, emergencyResponse]);
20         return;
21     }
22
23     // Tampilkan indikator "mengetik..."
24     const typingMessage = { id: 'typing', text: 'Pawtner is typing...', sender: 'ai' };
25     setMessages(prevMessages => [...prevMessages, typingMessage]);
26
27     try {
28         // Panggil backend Spring Boot Anda
29         const response = await fetch(YOUR_BACKEND_API_URL, {
30             method: 'POST',
31             headers: {
32                 'Content-Type': 'application/json',
33             },
34             body: JSON.stringify({ message: userMessageText }) // Kirim dalam format AiChatRequest
35         });
36
37         if (!response.ok) {
38             throw new Error('Network response was not ok');
39         }
40
41         const data = await response.json(); // Terima dalam format AiChatResponse
42         const aiResponse = { id: Math.random().(), text: data.reply, sender: 'ai' };
43                                                t
44         // Ganti "mengetik..." dengan jawaban AI
45         setMessages(prevMessages => [...prevMessages.filter(m => m.id !== 'typing'), aiResponse]);
46                                                t
47     } catch (error) {                          r
48         console.error("Failed to fetch AI response from backend:", error);
49         const errorResponse = { id: Math.random().(), text: 'Sorry, I am having trouble connecting. Please try again later.', sender: 'ai' };
50         setMessages(prevMessages => [...prevMessages.filter(m => m.id !== 'typing'), errorResponse]);
51     }                                             o
52 };                                                S
t
r
Cara Menjalankan                                      i
n
g
1. Jalankan Backend: Buka terminal di root proyek Spring Boot Anda dan jalankan ./mvnw spring-boot:run (atau jalankan dari IDE Anda).
2. Jalankan Frontend: Pastikan YOUR_BACKEND_API_URL di PawAiScreen.js sudah benar. Jika Anda menjalankan aplikasi di HP fisik, ganti localhost dengan alamat IP
   lokal komputer Anda (misal: http://192.168.1.5:8080/api/ai/chat). Lalu jalankan npx expo start.


Sekarang aplikasi Anda memiliki arsitektur yang aman, kuat, dan siap untuk produksi. API Key Anda aman di backend, dan frontend hanya berinteraksi dengan backend
Anda.
