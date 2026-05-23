# KomiKu — Android Comic Reader App 📚

![Banner](https://img.shields.io/badge/Status-Development-orange?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue?style=for-the-badge&logo=jetpackcompose)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-green?style=for-the-badge)

**KomiKu** adalah aplikasi pembaca komik (Manga, Manhwa, Manhua, Webtoon) berbasis Android yang dirancang dengan antarmuka modern menggunakan **Jetpack Compose** dan **Material 3**. Aplikasi ini memungkinkan pengguna untuk menjelajahi koleksi komik, melihat detail komik, dan **membaca chapter dalam format PDF** dengan pengalaman membaca yang mulus.

Dibangun sebagai tugas kuliah **Pemrograman Aplikasi Mobile** oleh:
*   **Mohamad Dimas Arjuna** — 2403040058
*   **Iqbal Dwi Ganjar Saefullah** — 2403040029

---

## ✨ Fitur Utama

*   **Home Screen**: Menampilkan daftar komik yang tersedia dengan tampilan grid/card yang menarik.
*   **Detail Komik**: Melihat informasi lengkap komik beserta daftar chapter yang tersedia.
*   **PDF Chapter Reader**: Membaca chapter komik dalam **format PDF** dengan dukungan scroll vertikal dan zoom menggunakan library **Bouquet**.
*   **Modern UI**: Antarmuka yang dibangun sepenuhnya dengan **Jetpack Compose** dan mengikuti **Material Design 3**.

---

## 🛠️ Stack Teknologi

| Layer | Teknologi |
|---|---|
| **Bahasa** | Kotlin |
| **UI Toolkit** | Jetpack Compose |
| **Design System** | Material 3 |
| **Networking** | Retrofit + Gson Converter |
| **Image Loading** | Coil |
| **PDF Viewer** | Bouquet (`io.github.grizzi91:bouquet`) |
| **Dependency Injection** | Hilt (Dagger) |
| **Navigation** | Navigation Compose |
| **Concurrency** | Kotlin Coroutines & StateFlow |

---

## 📸 Tampilan Aplikasi

> [!NOTE]
> *Screenshot akan segera diperbarui.*

| Home Screen | Detail Comic | PDF Reader |
| :---: | :---: | :---: |
| Daftar komik | Info & daftar chapter | Baca chapter (PDF) |

---

## 📂 Struktur Proyek

```
com.iqbal.komiku
├── data/
│   ├── model/              # Data classes (Komik, KomikDetail, Chapter, ChapterDetail)
│   ├── remote/             # Retrofit API service (KomikuApiService)
│   └── repository/         # Repository untuk akses data (KomikuRepository)
├── di/                     # Hilt dependency injection modules (AppModule)
├── ui/
│   ├── components/         # Reusable composables (KomikCard, dll.)
│   ├── navigation/         # Navigation graph setup (KomikuNavigation)
│   ├── screens/
│   │   ├── home/           # HomeScreen & HomeViewModel
│   │   ├── detail/         # DetailScreen & DetailViewModel
│   │   └── reader/         # ReaderScreen & ReaderViewModel (PDF viewer)
│   └── theme/              # Material 3 theme (Color, Theme, Type)
├── KomikuApp.kt            # Application class (Hilt entry point)
└── MainActivity.kt         # Single Activity host
```

---

## 🏗️ Arsitektur

Aplikasi ini menggunakan arsitektur **MVVM (Model-View-ViewModel)** dengan pola **Repository**:

*   **Data Layer** — `model/`, `remote/`, `repository/`: Mengelola data dari API menggunakan Retrofit.
*   **Presentation Layer** — `screens/`, `components/`: UI menggunakan Jetpack Compose, dengan state dikelola oleh ViewModel melalui `StateFlow`.
*   **DI Layer** — `di/`: Menyediakan dependensi menggunakan Hilt.

### Alur Baca Chapter (PDF)
1. User memilih chapter dari halaman detail komik.
2. `ReaderViewModel` memanggil repository untuk mengambil `ChapterDetail` (berisi `pdfUrl`).
3. `ReaderScreen` menggunakan `VerticalPDFReader` dari library **Bouquet** untuk merender file PDF dari URL tersebut.

---

## 🚀 Cara Menjalankan Project

1.  **Clone Repository**
    ```bash
    git clone https://github.com/username/komiku.git
    ```
2.  **Buka di Android Studio**
    *   Pastikan menggunakan versi Android Studio terbaru (Iguana/Jellyfish ke atas).
    *   Gunakan JDK 17 atau yang lebih baru.
3.  **Sync Gradle**
    *   Tunggu hingga proses sinkronisasi dependensi selesai.
4.  **Jalankan Aplikasi**
    *   Klik tombol **Run** dan pilih Emulator atau Perangkat Fisik (Min SDK 24 / Android 7.0).

---

## 📝 API

Aplikasi ini menggunakan REST API untuk mendapatkan data komik dan chapter. Endpoint utama:

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/komik` | Mendapatkan daftar komik |
| `GET` | `/api/komik/{id}` | Mendapatkan detail komik & daftar chapter |
| `GET` | `/api/chapter/detail/{id}` | Mendapatkan detail chapter (termasuk `pdfUrl`) |

---

## 🤝 Kontribusi
Project ini dibangun untuk tujuan edukasi. Jika Anda ingin berkontribusi:
1. Fork project ini.
2. Buat feature branch (`git checkout -b feature/FiturBaru`).
3. Commit perubahan Anda (`git commit -m 'Menambah Fitur Baru'`).
4. Push ke branch (`git push origin feature/FiturBaru`).
5. Buat Pull Request.

---

## 📄 Lisensi
Didistribusikan di bawah lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.

---
*Dibuat dengan ❤️ oleh Kelompok 1 - Pemrograman Aplikasi Mobile.*
