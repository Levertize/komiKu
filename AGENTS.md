# AGENTS.md — KomiKu Android App

Dokumen ini adalah panduan untuk AI agent (Claude Code, Copilot, dll) yang bekerja di proyek ini.
Baca seluruh file ini sebelum menulis atau mengubah kode apapun.

---

## Gambaran Proyek

**KomiKu** adalah aplikasi Android untuk membaca komik (Manga, Manhwa, Manhua, Webtoon).
Dibangun sebagai tugas kuliah **Pemrograman Aplikasi Mobile** oleh:
- Mohamad Dimas Arjuna — 2403040058
- Iqbal Dwi Ganjar — 2403040029

---

## Stack Teknologi

| Layer | Teknologi |
|---|---|
| Bahasa | Kotlin |
| UI | XML View (bukan Jetpack Compose) |
| Database | **SQLite via Room** |
| Storage | **Internal Storage app** (file PDF lokal) |
| Auth | **SharedPreferences** (session login sederhana) |
| Image loading | Glide |
| PDF reader | AndroidPdfViewer (barteksc) |
| Navigation | Navigation Component (NavController) |
| View Binding | Aktif di semua Activity & Fragment |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 |

---

## Struktur Package

```
com.kelompok1.komiku/
├── model/
│   ├── Comic.kt              # data class + Room @Entity
│   └── Chapter.kt            # data class + Room @Entity
├── database/
│   ├── KomiKuDatabase.kt     # Room Database singleton
│   ├── ComicDao.kt           # DAO query untuk komik
│   ├── ChapterDao.kt         # DAO query untuk chapter
│   └── DatabaseSeeder.kt     # insert data awal saat install pertama
├── repository/
│   ├── ComicRepository.kt    # akses data komik (Room)
│   └── ChapterRepository.kt  # akses data chapter (Room)
├── admin/
│   └── AdminActivity.kt      # halaman admin CRUD komik & chapter
├── adapter/
│   ├── BannerAdapter.kt
│   ├── ComicGridAdapter.kt
│   ├── ComicListAdapter.kt
│   ├── LibraryAdapter.kt
│   ├── ChapterAdapter.kt
│   └── ReadingPageAdapter.kt
├── HomeFragment.kt
├── JelajahiFragment.kt
├── LibraryFragment.kt
├── PengaturanFragment.kt
├── DetailActivity.kt
├── ReadingActivity.kt
├── LoginActivity.kt
├── SplashActivity.kt
└── MainActivity.kt
```

---

## Struktur Resource

```
res/
├── layout/
│   ├── activity_splash.xml
│   ├── activity_login.xml
│   ├── activity_main.xml
│   ├── activity_detail.xml
│   ├── activity_reading.xml
│   ├── activity_admin.xml           # list komik & chapter untuk admin
│   ├── activity_admin_comic.xml     # form tambah/edit komik
│   ├── activity_admin_chapter.xml   # form tambah chapter + pilih PDF
│   ├── fragment_home.xml
│   ├── fragment_jelajahi.xml
│   ├── fragment_library.xml
│   ├── fragment_pengaturan.xml
│   ├── item_comic_grid.xml
│   ├── item_comic_list.xml
│   ├── item_library_card.xml
│   ├── item_chapter.xml
│   ├── item_banner.xml
│   └── item_reading_page.xml
├── values/
│   ├── colors.xml
│   ├── themes.xml
│   ├── strings.xml
│   └── dimens.xml
├── drawable/
├── font/
│   └── plus_jakarta_sans.xml
└── menu/
    └── menu_bottom_nav.xml
```

---

## Design System

### Warna utama (colors.xml)
```
accent          #7C5CFC   → tombol, active state, badge, chip aktif
accent_soft     #C084FC   → secondary accent, gradient
dk_bg           #0D0D14   → background dark mode
dk_card         #13131F   → card, input, bottom nav dark
dk_card2        #1A1A2E   → chip background, secondary card
dk_text         #F0EEF8   → teks utama dark mode
dk_muted        61% putih → teks sekunder
lt_bg           #F4F2FC   → background light mode
lt_card         #FFFFFF   → card light mode
color_hot       #F43F5E   → badge HOT
color_new       #10B981   → badge NEW
color_update    #F59E0B   → badge UPDATE, rating bintang
read_bg         #07070F   → background reading mode
```

### Font
- **Plus Jakarta Sans** — satu-satunya font
- Weight: 400, 600, 700, 800
- Referensi: `@font/plus_jakarta_sans`

### Radius & Spacing (dimens.xml)
```
radius_sm 8dp   radius_md 11dp   radius_lg 14dp   radius_xl 18dp   radius_pill 20dp
spacing_xs 4dp  spacing_sm 8dp   spacing_md 12dp  spacing_lg 16dp  spacing_xl 24dp
```

---

## Aturan Coding

### Wajib diikuti
- Selalu gunakan **View Binding** — jangan `findViewById` langsung
- Semua warna dari `@color/...` — jangan hardcode hex di XML layout
- Semua teks dari `@string/...` — jangan hardcode string di XML layout
- Semua ukuran dari `@dimen/...` — jangan hardcode angka dp/sp di XML
- Gunakan `app:tint` bukan `android:imageTintList`
- Tambahkan `android:fitsSystemWindows="true"` di root layout setiap Activity
- Jangan gunakan `enableEdgeToEdge()` — sudah di-handle di theme
- Semua akses database lewat **Repository** — jangan panggil DAO langsung dari Activity/Fragment
- Query Room selalu di **background thread** pakai Coroutine / lifecycleScope

### Fragment pattern
```kotlin
class XxxFragment : Fragment() {
    private var _binding: FragmentXxxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(...): View {
        _binding = FragmentXxxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // wajib untuk mencegah memory leak
    }
}
```

### Room query pattern (selalu background thread)
```kotlin
// Di Fragment/Activity
lifecycleScope.launch(Dispatchers.IO) {
    val comics = comicRepository.getAllComics()
    withContext(Dispatchers.Main) {
        adapter.updateData(comics)
    }
}
```

### Adapter pattern
- Cover gradient: `GradientDrawable` di-set sebagai `.background` pada View — bukan `setImageDrawable`
- Badge: buat `GradientDrawable` baru per item — jangan reuse

---

## Database Schema (Room)

### Table: `comics`
```
id                INTEGER  PRIMARY KEY AUTOINCREMENT
title             TEXT     NOT NULL
author            TEXT     NOT NULL
format            TEXT                        -- Manga/Manhwa/Manhua/Webtoon
genre             TEXT                        -- JSON array string e.g. '["Action","Fantasy"]'
description       TEXT
status            TEXT     DEFAULT 'Ongoing'  -- Ongoing/Completed
rating            REAL     DEFAULT 0.0
views             INTEGER  DEFAULT 0
badge             TEXT     DEFAULT ''         -- HOT/NEW/UPDATE/""
cover_color_start TEXT                        -- hex e.g. "#1A0E35"
cover_color_end   TEXT
last_update       TEXT
created_at        INTEGER                     -- unix timestamp
```

### Table: `chapters`
```
id           INTEGER  PRIMARY KEY AUTOINCREMENT
comic_id     INTEGER  NOT NULL    -- FK ke comics.id
number       INTEGER  NOT NULL
title        TEXT     NOT NULL
pdf_path     TEXT                 -- path absolut file PDF di internal storage
upload_date  TEXT
```

### Table: `library` (bookmark user)
```
id              INTEGER  PRIMARY KEY AUTOINCREMENT
comic_id        INTEGER  NOT NULL
current_chapter INTEGER  DEFAULT 0
total_chapter   INTEGER  DEFAULT 0
saved_at        INTEGER               -- unix timestamp
```

---

## Alur PDF Chapter

```
Admin tambah chapter (di AdminActivity)
    ↓
Pilih file PDF dari storage HP (Intent.ACTION_OPEN_DOCUMENT)
    ↓
PDF disalin ke internal storage:
  /data/data/com.kelompok1.komiku/files/chapters/{comicId}/{chapterNumber}.pdf
    ↓
pdf_path disimpan di Room table chapters
    ↓
User klik chapter di DetailActivity
    ↓
App baca pdf_path dari Room
    ↓
Render PDF pakai AndroidPdfViewer (barteksc)
```

---

## Admin Panel (Activity dalam app)

Admin panel adalah **Activity khusus dalam app** yang diakses dengan kredensial khusus.

### Cara masuk admin
Di LoginActivity, gunakan:
- Email: `admin@komiku.id`
- Password: `admin123`

Session admin disimpan di SharedPreferences key `is_admin = true`.

### Fitur admin (AdminActivity)
- Lihat semua komik
- Tambah komik baru (isi form + pilih warna cover gradient)
- Edit detail komik
- Hapus komik (otomatis hapus semua chapter-nya)
- Tambah chapter (nomor, judul, pilih PDF dari storage HP)
- Edit/hapus chapter

---

## Fitur yang Sudah Ada

- [x] Splash screen dengan animasi dots
- [x] Login screen (email + password + SSO placeholder)
- [x] Home: banner carousel auto-scroll + grid 3 kolom
- [x] Jelajahi: search + filter format/genre/sort + toggle DESC/ASC
- [x] Library: grid 2 kolom + progress bar + local search
- [x] Pengaturan: dark/light mode toggle + notifikasi switch
- [x] Detail komik: cover, info, sinopsis, genre chips, daftar chapter
- [x] Reading mode: scroll vertikal, prev/next chapter
- [x] Bottom nav 4 tab: Home · Jelajahi · Library · Akun
- [x] Data dummy 12 komik + chapter list (di DummyData.kt)

---

## Fitur yang Belum Ada (Next Steps — urutan pengerjaan)

- [ ] **Step 1**: Setup Room Database (entity, DAO, database class)
- [ ] **Step 2**: DatabaseSeeder — insert 12 komik dummy ke Room saat install pertama
- [ ] **Step 3**: Repository layer (ComicRepository, ChapterRepository)
- [ ] **Step 4**: Ganti DummyData → Room di HomeFragment, JelajahiFragment, LibraryFragment, DetailActivity
- [ ] **Step 5**: PDF reader — AndroidPdfViewer di ReadingActivity
- [ ] **Step 6**: Admin Activity — CRUD komik & chapter + upload PDF
- [ ] **Step 7**: Bookmark tersimpan ke Room (table library)
- [ ] **Step 8**: History baca chapter tersimpan ke Room

---

## Cara Tambah Chapter Baru

### Setelah Admin Activity selesai (Step 6)
1. Buka app → Login sebagai admin (`admin@komiku.id` / `admin123`)
2. Masuk ke Admin Panel
3. Pilih komik → Tambah Chapter
4. Isi nomor chapter & judul
5. Klik "Pilih PDF" → pilih file PDF dari storage HP
6. Klik Simpan → chapter langsung muncul di detail komik

### Sementara (sebelum Admin selesai) — via DatabaseSeeder.kt
```kotlin
// Tambahkan baris ini di DatabaseSeeder.kt
ChapterEntity(
    comicId = 1,          // id komik (1 = One Piece)
    number = 1112,
    title = "Chapter 1112 - Nama Chapter",
    pdfPath = "",         // kosong dulu kalau belum ada PDF
    uploadDate = "baru saja"
)
```
Simpan → Build ulang → chapter langsung muncul.

---

## Navigation Flow

```
SplashActivity (2.5s)
    ├── user biasa  → LoginActivity → MainActivity
    └── admin       → LoginActivity → AdminActivity (CRUD komik & chapter)

MainActivity
    └── BottomNav
          ├── HomeFragment
          │     └── klik komik → DetailActivity
          │                         └── klik chapter → ReadingActivity (PDF viewer)
          ├── JelajahiFragment
          │     └── klik komik → DetailActivity
          ├── LibraryFragment
          │     └── klik komik → DetailActivity
          └── PengaturanFragment
```

---

## Dependency (build.gradle app)

```gradle
plugins {
    id 'kotlin-kapt'  // WAJIB untuk Room annotation processor
}

dependencies {
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    // Navigation Component
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
    // ViewPager2
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
    // Glide
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    // RecyclerView & ConstraintLayout
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Room (SQLite)
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'

    // Coroutines (wajib untuk Room di background thread)
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

    // PDF Reader
    implementation 'com.github.barteksc:android-pdf-viewer:3.2.0-beta.1'
}
```

Tambahkan di `build.gradle (app)` block `android { }`:
```gradle
buildFeatures {
    viewBinding = true
}
```

Tambahkan di `settings.gradle` (untuk barteksc PDF):
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

---

## Hal yang JANGAN Dilakukan Agent

- Jangan ganti XML View ke Jetpack Compose
- Jangan hardcode warna, string, atau ukuran di XML layout
- Jangan gunakan `enableEdgeToEdge()` di Activity manapun
- **Jangan gunakan Firebase** — proyek ini pakai SQLite Room
- Jangan akses DAO langsung dari Activity/Fragment — lewat Repository
- Jangan jalankan query Room di main thread — wajib pakai coroutine
- Jangan hapus `_binding = null` di `onDestroyView()` Fragment
- Jangan gunakan `android:imageTintList` — pakai `app:tint`
- Jangan ganti font dari Plus Jakarta Sans

---

*File ini harus diupdate setiap kali ada perubahan arsitektur, fitur baru, atau aturan baru.*
*Terakhir diupdate: Mei 2026*
