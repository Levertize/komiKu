# KomiKu — Android Comic Reader App 📚

![Banner](https://img.shields.io/badge/Status-Development-orange?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?style=for-the-badge&logo=kotlin)
![Room](https://img.shields.io/badge/Room-v2.6.1-blue?style=for-the-badge&logo=sqlite)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-green?style=for-the-badge)

**KomiKu** adalah aplikasi pembaca komik (Manga, Manhwa, Manhua, Webtoon) berbasis Android yang dirancang dengan antarmuka modern, interaktif, dan performa tinggi. Aplikasi ini memungkinkan pengguna untuk menjelajahi koleksi komik, mengelola perpustakaan pribadi, dan membaca chapter melalui sistem pembaca gambar yang mulus.

Dibangun sebagai tugas kuliah **Pemrograman Aplikasi Mobile** oleh:
*   **Mohamad Dimas Arjuna** — 2403040058
*   **Iqbal Dwi Ganjar Saefullah** — 2403040029

---

## ✨ Fitur Utama

### 👤 User Side
*   **Modern Home Dashboard**: Carousel banner trending dan grid komik terpopuler dengan desain "Glassmorphism".
*   **Advanced Explore & Filter**: Cari komik berdasarkan judul, author, atau genre. Filter secara instan berdasarkan format (Manga/Manhwa/etc) dan urutkan berdasarkan update terbaru atau rating.
*   **Personal Library**: Simpan komik favorit Anda (Bookmark) dan pantau progres membaca Anda dengan bar indikator yang akurat.
*   **Reading Mode**: Pengalaman membaca vertikal (scroll) yang mulus dengan fitur navigasi antar chapter yang cepat.
*   **History Persistence**: Aplikasi otomatis mengingat chapter terakhir yang Anda baca.

### 🔐 Admin Side (CMS)
*   **Comic Management**: CRUD (Create, Read, Update, Delete) data komik termasuk kustomisasi warna gradient dan poster cover.
*   **Chapter Management**: Unggah beberapa gambar sekaligus untuk satu chapter langsung dari penyimpanan HP.
*   **Real-time Preview**: Admin dapat melihat preview langsung konten chapter sebelum dipublikasikan.
*   **Internal Storage Sync**: Seluruh gambar (cover & chapter) disalin ke internal storage aplikasi untuk menjamin data tetap ada meski file asli dihapus dari galeri.

---

## 🛠️ Stack Teknologi

| Layer | Teknologi |
|---|---|
| **Bahasa** | Kotlin |
| **UI Architecture** | XML View (View Binding) |
| **Database** | SQLite via **Room Persistence Library** |
| **Concurrency** | Kotlin Coroutines & Flow |
| **Image Loading** | **Glide** (with disk caching) |
| **Local Storage** | Android Internal Storage (Scroped Storage ready) |
| **Navigation** | Jetpack Navigation Component |

---

## 📸 Tampilan Aplikasi

> [!NOTE]
> *Halaman ini masih dalam pengembangan. Screenshot akan segera diperbarui.*

| Home Screen | Detail Comic | Reading Mode |
| :---: | :---: | :---: |
| ![Home Placeholder](https://via.placeholder.com/200x400.png?text=Home+Screen) | ![Detail Placeholder](https://via.placeholder.com/200x400.png?text=Detail+Screen) | ![Read Placeholder](https://via.placeholder.com/200x400.png?text=Reading+Mode) |

| Admin Dashboard | Filter Panel | Library |
| :---: | :---: | :---: |
| ![Admin Placeholder](https://via.placeholder.com/200x400.png?text=Admin+Panel) | ![Filter Placeholder](https://via.placeholder.com/200x400.png?text=Filter+UI) | ![Library Placeholder](https://via.placeholder.com/200x400.png?text=Library+View) |

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

### 🔑 Kredensial Admin
Untuk masuk ke mode manajemen, gunakan akun berikut pada layar Login:
*   **Email**: `admin@komiku.id`
*   **Password**: `admin123`

---

## 🏗️ Struktur Arsitektur
Aplikasi ini mengikuti pola arsitektur **Repository Pattern** untuk memisahkan logika data dan UI:
*   `model/`: Entity data Room.
*   `database/`: Konfigurasi Room Database, DAO, dan Seeder.
*   `repository/`: Layer abstraksi akses data.
*   `adapter/`: Penanganan tampilan data pada RecyclerView (Banner, Grid, List).
*   `admin/`: Logika panel manajemen konten.

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
