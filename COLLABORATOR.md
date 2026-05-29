## Daftar Isi

1. [Design System & Warna](#1-design-system--warna)
2. [Arsitektur Aplikasi](#2-arsitektur-aplikasi)
3. [Navigasi & Struktur Layar](#3-navigasi--struktur-layar)
4. [Fitur: Autentikasi (Sign Up & Login)](#4-fitur-autentikasi-sign-up--login)
5. [Fitur: Home](#5-fitur-home)
6. [Fitur: Keuangan](#6-fitur-keuangan)
7. [Fitur: Train & Study](#7-fitur-train--study)
8. [Fitur: Scan](#8-fitur-scan)
9. [Fitur: Akun](#9-fitur-akun)
10. [Database Schema](#10-database-schema)
11. [API Contracts](#11-api-contracts)
12. [Notifikasi](#12-notifikasi)
13. [PDF Export](#13-pdf-export)
14. [Integrasi Google Sheets](#14-integrasi-google-sheets)
15. [Tech Stack & Dependencies](#15-tech-stack--dependencies)
16. [Struktur Folder Proyek](#16-struktur-folder-proyek)

---

## 1. Design System & Warna

### 1.1 Color Palette

| Token Name | Hex Code | Kegunaan |
|---|---|---|
| `primary` | `#2563EB` | Button utama, active tab, accent, hero banner |
| `primary-dark` | `#1D4ED8` | Button hover / pressed state |
| `primary-light` | `#DBEAFE` | Selected chip background, badge bg |
| `primary-pale` | `#EFF6FF` | Section background lembut |
| `white` | `#FFFFFF` | Card background, surface, bottom nav |
| `bg-page` | `#F3F4F6` | Background halaman (abu abu muda) |
| `border` | `#E5E7EB` | Garis pemisah, border input |
| `text-title` | `#111827` | Heading, judul layar, saldo besar |
| `text-body` | `#374151` | Paragraf, label formulir |
| `text-muted` | `#6B7280` | Placeholder, subtitle, tanggal |
| `success` | `#10B981` | Ceklis done, pemasukan, progress selesai |
| `danger` | `#EF4444` | Tanda silang / miss, pengeluaran |
| `warning` | `#F59E0B` | Progress mendekati batas, peringatan |
| `hero-gradient` | `#2563EB → #1A1A2E` | Banner splash, header hero section |

### 1.2 Typography

| Style | Font | Size | Weight | Warna Default |
|---|---|---|---|---|
| `heading-xl` | Inter | 28px | 700 | `#111827` |
| `heading-lg` | Inter | 22px | 700 | `#111827` |
| `heading-md` | Inter | 18px | 600 | `#111827` |
| `body-lg` | Inter | 16px | 400 | `#374151` |
| `body-md` | Inter | 14px | 400 | `#374151` |
| `label` | Inter | 13px | 500 | `#374151` |
| `caption` | Inter | 12px | 400 | `#6B7280` |
| `mono` | Fira Code | 14px | 400 | `#374151` |

### 1.3 Spacing Scale

```
xs   = 4px
sm   = 8px
md   = 12px
lg   = 16px
xl   = 24px
2xl  = 32px
3xl  = 48px
```

### 1.4 Border Radius

```
card        = 16px
button      = 12px
chip/badge  = 999px (pill)
input       = 10px
avatar      = 999px (circle)
```

### 1.5 Shadow

```
card-shadow   : 0px 2px 12px rgba(0,0,0,0.07)
modal-shadow  : 0px 8px 32px rgba(0,0,0,0.15)
nav-shadow    : 0px -2px 8px rgba(0,0,0,0.05)
```

### 1.6 Ikon

- Library: **Lucide React Native** (outline style)
- Ukuran default: 22px
- Warna: mengikuti konteks (aktif = `#2563EB`, non-aktif = `#6B7280`)

---

## 3. Navigasi & Struktur Layar

### 3.1 Bottom Tab Navigation

```
Tab 1: Home      → icon: house         → HomeScreen
Tab 2: Keuangan  → icon: wallet        → FinanceScreen
Tab 3: Scan      → icon: scan (FAB)    → ScanScreen       ← tombol tengah, lebih besar
Tab 4: Jadwal    → icon: calendar      → ScheduleScreen
Tab 5: Akun      → icon: user          → AccountScreen
```

**Catatan Tab Scan (FAB-style):**
- Tombol scan di posisi tengah
- Ukuran lebih besar dari tab lain: 56x56px
- Background: `#2563EB`, icon putih
- Floating sedikit ke atas melewati bar navigasi (gaya bottom nav modern)

### 3.2 Stack Screens per Fitur

```
HomeStack
└── HomeScreen

FinanceStack
├── FinanceScreen (main)
├── AddTransactionScreen
├── TransactionDetailScreen
├── TransactionListScreen
└── FinanceChartScreen

ScheduleStack
├── ScheduleScreen (main - All/Train/Study tabs)
├── AddScheduleScreen
├── ScheduleDetailScreen
└── ScheduleProgressScreen

ScanStack
├── ScanModeScreen (pilih: receipt / study-train)
├── CameraScreen
├── ScanResultScreen
└── ConfirmScanScreen

AccountStack
├── AccountScreen
├── EditProfileScreen
├── SpreadsheetSettingScreen
├── NotificationSettingScreen
└── AboutScreen
```

---

## 4. Fitur: Autentikasi (Sign Up & Login)

### 4.1 Deskripsi

Modul autentikasi menangani seluruh alur pengguna baru (Sign Up / Register) dan pengguna lama (Login), termasuk validasi form, keamanan password, lupa password, dan session management. Ini adalah gerbang utama sebelum masuk ke aplikasi.

### 4.2 Alur Navigasi Auth

```
App Launch
    ↓
SplashScreen (1.5 detik)
    ↓
Cek session token di SecureStore
    ├── Token valid → MainApp (Tab Navigator)
    └── Token tidak ada / expired
            ↓
        OnboardingScreen (hanya tampil pertama kali install)
            ↓
        AuthLandingScreen  ← pintu masuk utama
            ├── [Masuk]   → LoginScreen
            │                   ├── Berhasil → MainApp
            │                   └── Lupa Password → ForgotPasswordScreen
            │                                           ↓
            │                                       OTPVerifyScreen
            │                                           ↓
            │                                       ResetPasswordScreen
            │                                           ↓
            │                                       LoginScreen
            └── [Daftar]  → RegisterScreen
                                ├── Step 1: Data Diri
                                ├── Step 2: Email & Password
                                ├── Step 3: Verifikasi Email (OTP)
                                └── Berhasil → MainApp
```

### 4.3 Layar Splash (SplashScreen)

#### Layout
- Background: Gradient `#2563EB → #1A1A2E` (full screen)
- Logo app (icon + teks "MyTrack") di tengah layar, teks putih
- Tagline di bawah logo: "Track. Train. Thrive." — `body-md`, `rgba(255,255,255,0.75)`
- Loading indicator: titik-titik animasi atau thin progress bar di bawah
- Durasi: 1.5–2 detik, lalu auto-navigate

#### Warna
- Background: gradient `#2563EB` → `#1A1A2E`
- Logo teks: `#FFFFFF`
- Tagline: `rgba(255,255,255,0.75)`
- Loading dots: `rgba(255,255,255,0.5)`

### 4.4 Layar Onboarding (OnboardingScreen)

Tampil hanya sekali saat pertama install. Terdiri dari 3 slide horizontal (swipeable).

#### Slide 1 — Keuangan
```
[Ilustrasi ikon keuangan / chart animasi]

Pantau Keuanganmu
Catat pemasukan & pengeluaran,
lihat laporan dalam grafik yang jelas.

● ○ ○        [Lewati]
```

#### Slide 2 — Train & Study
```
[Ilustrasi jadwal / kalender animasi]

Bangun Konsistensi
Jadwalkan latihan & belajar,
pantau progress setiap harinya.

○ ● ○        [Lewati]
```

#### Slide 3 — Scan
```
[Ilustrasi kamera scan animasi]

Scan & Simpan Otomatis
Foto struk belanja atau catatan,
data langsung tersimpan rapi.

○ ○ ●

[Mulai Sekarang]  ← tombol biru full-width
```

**Detail UI Onboarding:**
- Background: `#FFFFFF`
- Ilustrasi area: 55% tinggi layar, background `#EFF6FF` (rounded bottom)
- Heading: `heading-lg`, `#111827`
- Deskripsi: `body-md`, `#6B7280`, multi-line, text-align center
- Dot indicator aktif: `#2563EB`, non-aktif: `#E5E7EB`, ukuran 8px
- Tombol "Lewati": text button, `#6B7280`, pojok kanan atas
- Tombol "Mulai Sekarang": filled biru, full-width, radius 12px
- Animasi slide: spring transition horizontal

### 4.5 Layar Auth Landing (AuthLandingScreen)

Halaman pilihan masuk atau daftar. Ini layar yang dilihat user setelah onboarding atau saat belum login.

#### Layout
```
┌──────────────────────────────┐
│                              │
│   [Hero Illustration Area]   │  ← 45% tinggi layar
│   Background: gradient biru  │
│   Ilustrasi orang + grafik   │
│                              │
├──────────────────────────────┤
│                              │
│        MyTrack               │  ← Logo + nama app
│   Track. Train. Thrive.      │  ← Tagline
│                              │
│  ┌────────────────────────┐  │
│  │      Masuk             │  │  ← Primary button biru
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │      Daftar            │  │  ← Outline button
│  └────────────────────────┘  │
│                              │
│  ─────── atau masuk dengan ──│
│                              │
│  [G] Lanjutkan dengan Google │  ← Google Sign-In button
│                              │
│  Dengan mendaftar, kamu      │  ← Terms & Privacy text
│  menyetujui Syarat & Ketentuan│
└──────────────────────────────┘
```

**Detail UI:**
- Hero area bg: gradient `#2563EB → #1D4ED8`
- Nama app: `heading-xl` 28px, `#111827`
- Tagline: `body-md`, `#6B7280`
- Tombol "Masuk": background `#2563EB`, teks putih, height 52px, radius 12px
- Tombol "Daftar": background `#FFFFFF`, border 1.5px `#2563EB`, teks `#2563EB`, height 52px
- Divider "atau": garis `#E5E7EB` + teks `caption` `#6B7280` di tengah
- Tombol Google: bg `#FFFFFF`, border `#E5E7EB`, icon Google 20px, teks `#374151`
- Terms text: `caption` 11px, `#6B7280`, link biru untuk "Syarat & Ketentuan" dan "Kebijakan Privasi"

### 4.6 Layar Login (LoginScreen)

#### Layout Lengkap
```
┌──────────────────────────────┐
│ ←  Masuk                     │  ← Back button + judul
│                              │
│  Selamat datang kembali 👋   │  ← Heading
│  Masuk untuk melanjutkan     │  ← Subheading muted
│                              │
│  Email                       │  ← Label
│  [email@example.com      ]   │  ← Input field
│                              │
│  Password                    │  ← Label
│  [••••••••••        👁     ]  │  ← Input + toggle visibilitas
│                              │
│  [x] Ingat saya              │  ← Checkbox + label (kiri)
│              Lupa password?  │  ← Link (kanan)
│                              │
│  ┌────────────────────────┐  │
│  │         Masuk          │  │  ← Primary button biru
│  └────────────────────────┘  │
│                              │
│  ─────── atau ───────────    │
│                              │
│  [G]  Masuk dengan Google    │  ← Google button
│                              │
│  Belum punya akun? Daftar    │  ← Link ke register
└──────────────────────────────┘
```

#### Detail Setiap Komponen

**Header:**
- Tombol back: `←` icon `chevron-left`, 44x44px tap area, `#374151`
- Judul "Masuk": `heading-lg`, `#111827`

**Greeting Section:**
- Heading: "Selamat datang kembali 👋" — `heading-md`, `#111827`
- Sub: "Masuk untuk melanjutkan" — `body-md`, `#6B7280`
- Margin bawah: 24px

**Input Email:**
- Label: `label` 13px, `#374151`, margin bawah 6px
- Input: height 52px, bg `#FFFFFF`, border 1.5px `#E5E7EB`, radius 10px, padding 14px
- Keyboard: `email-address`, `autoCapitalize: none`
- Focus state: border `#2563EB`, subtle shadow `0 0 0 3px rgba(37,99,235,0.1)`
- Error state: border `#EF4444`, pesan error di bawah (teks `caption` merah)
- Icon email di kiri: 18px, `#6B7280`

**Input Password:**
- Identik dengan email input
- Icon kunci di kiri: 18px, `#6B7280`
- Tombol mata (toggle visibilitas) di kanan: icon `eye` / `eye-off`, 18px, `#6B7280`
- Keyboard: `default`, `secureTextEntry: true` (saat tersembunyi)

**Row Ingat Saya + Lupa Password:**
- Checkbox: custom styled, ukuran 18px, border `#E5E7EB`, check `#2563EB`
- "Ingat saya": `body-md`, `#374151`
- "Lupa password?": `body-md`, `#2563EB`, text-decoration underline, di kanan

**Tombol Masuk:**
- Background: `#2563EB`
- Teks: "Masuk", putih, `heading-md`
- Height: 52px, radius 12px, full-width
- Loading state: spinner putih, teks "Memproses..."
- Disabled state: background `#93C5FD`, tidak bisa diklik saat loading

**Divider Google:**
- Garis tipis `#E5E7EB` + label "atau" di tengah, `caption`, `#6B7280`

**Tombol Google:**
- Background `#FFFFFF`, border 1.5px `#E5E7EB`
- Icon SVG Google (warna asli), teks "Masuk dengan Google", `body-md`, `#374151`

**Link ke Register:**
- "Belum punya akun?" — `body-md`, `#6B7280`
- "Daftar" — `body-md`, `#2563EB`, bold, di sebelahnya

#### Validasi Login

| Field | Aturan | Pesan Error |
|---|---|---|
| Email | Wajib, format email valid | "Email tidak valid" |
| Password | Wajib, minimal 1 karakter | "Password wajib diisi" |
| Kombinasi | Cocok di database | "Email atau password salah" |

#### State Setelah Login Gagal

- Field yang salah: border merah `#EF4444`
- Pesan error di bawah field: `caption`, `#EF4444`
- Setelah 5x gagal: tampilkan CAPTCHA atau cooldown 30 detik
- Tampilkan pesan: "Terlalu banyak percobaan, coba lagi dalam 30 detik"

### 4.7 Layar Register / Sign Up (RegisterScreen)

Pendaftaran dibagi **3 langkah (step)** untuk menghindari form yang terlalu panjang. Ditampilkan dengan step indicator di atas.

#### Step Indicator
```
① —————— ② —————— ③
Data Diri   Akun    Verifikasi
```
- Step aktif: lingkaran `#2563EB`, teks putih
- Step selesai: lingkaran `#10B981` dengan icon centang putih
- Step belum: lingkaran `#E5E7EB`, teks `#6B7280`
- Garis penghubung: `#E5E7EB` (belum) / `#10B981` (sudah)

---

#### Step 1 — Data Diri

```
┌──────────────────────────────┐
│ ←   Daftar Akun              │
│  ① ——— ② ——— ③              │
│                              │
│  Siapa namamu?               │  ← Heading
│  Isi data dirimu di bawah    │  ← Sub
│                              │
│  Nama Lengkap                │
│  [Nama lengkap kamu       ]  │
│                              │
│  Nama Pengguna               │
│  [@username               ]  │
│  Nama pengguna hanya huruf,  │
│  angka, dan underscore (_)   │
│                              │
│  [      Selanjutnya →      ] │
└──────────────────────────────┘
```

**Detail Komponen Step 1:**

- Heading "Siapa namamu?": `heading-md`, `#111827`
- Sub: `body-md`, `#6B7280`

**Input Nama Lengkap:**
- Label: `label`, `#374151`
- Placeholder: "Nama lengkap kamu"
- `autoCapitalize: words`
- Validasi: wajib, minimal 3 karakter, maksimal 50 karakter

**Input Nama Pengguna (Username):**
- Label: `label`, `#374151`
- Prefix `@` di dalam input (teks statis kiri), warna `#6B7280`
- Validasi real-time: cek ketersediaan username via API saat user berhenti mengetik (debounce 500ms)
  - Tersedia: icon `check-circle` hijau di kanan input
  - Tidak tersedia: icon `x-circle` merah + pesan "Username sudah digunakan"
  - Sedang cek: spinner kecil di kanan input
- Aturan: lowercase, hanya huruf/angka/underscore, 3–20 karakter
- Helper text: `caption`, `#6B7280`

**Tombol "Selanjutnya":**
- Disabled (abu-abu) jika form belum valid
- Aktif (biru) jika semua field valid

---

#### Step 2 — Email & Password

```
┌──────────────────────────────┐
│ ←   Daftar Akun              │
│  ✓ ——— ② ——— ③              │
│                              │
│  Buat akun kamu              │  ← Heading
│  Email dan password aman kami│  ← Sub
│                              │
│  Email                       │
│  [email@example.com       ]  │
│                              │
│  Password Baru               │
│  [••••••••••         👁    ] │
│                              │
│  Kekuatan password:          │
│  [████████░░░░] Sedang       │  ← Password strength bar
│  ✓ Min. 8 karakter           │
│  ✓ Mengandung angka          │
│  ✗ Huruf kapital             │
│  ✗ Karakter spesial (!@#$)   │
│                              │
│  Konfirmasi Password         │
│  [••••••••••         👁    ] │
│                              │
│  [      Selanjutnya →      ] │
└──────────────────────────────┘
```

**Detail Komponen Step 2:**

**Input Email:**
- Keyboard `email-address`, `autoCapitalize: none`
- Validasi: format email valid, belum terdaftar (cek API real-time)
- Pesan error: "Email sudah terdaftar. Coba masuk?" dengan link ke LoginScreen

**Input Password Baru:**
- `secureTextEntry`, toggle visibilitas (icon mata kanan)
- Password Strength Indicator:
  - Bar progress (full width, 6px tinggi, radius pill)
  - Level:
    - Lemah (1/4): merah `#EF4444`, teks "Lemah"
    - Sedang (2/4): oranye `#F59E0B`, teks "Sedang"
    - Kuat (3/4): biru `#2563EB`, teks "Kuat"
    - Sangat Kuat (4/4): hijau `#10B981`, teks "Sangat Kuat"
  - Cek list requirement (live update):
    - ✓ hijau jika terpenuhi, ✗ abu-abu jika belum
    - Min. 8 karakter
    - Mengandung angka (0-9)
    - Huruf kapital (A-Z)
    - Karakter spesial (!@#$%^&*)
  - `caption` 12px untuk setiap requirement

**Input Konfirmasi Password:**
- Validasi: harus sama persis dengan field password di atas
- Error: "Password tidak cocok" jika berbeda

**Kalkulasi Kekuatan Password:**
```javascript
const getPasswordStrength = (password) => {
  let score = 0;
  if (password.length >= 8)           score++;
  if (/\d/.test(password))            score++;
  if (/[A-Z]/.test(password))         score++;
  if (/[!@#$%^&*]/.test(password))   score++;
  return score; // 0-4
};
```

---

#### Step 3 — Verifikasi Email (OTPVerifyScreen)

```
┌──────────────────────────────┐
│ ←   Verifikasi Email         │
│  ✓ ——— ✓ ——— ③              │
│                              │
│    📧                        │  ← Icon email besar, biru
│                              │
│  Cek emailmu!                │  ← Heading
│  Kami kirim kode 6 digit ke  │  ← Sub
│  email@example.com           │  ← Email dicetak bold
│                              │
│  [ 8 ] [ 4 ] [ 2 ] [ 1 ] [ 9 ] [ 3 ]  │  ← 6 kotak OTP
│                              │
│  Kode berlaku selama 10:00   │  ← Countdown timer
│                              │
│  [     Verifikasi Kode     ] │  ← Tombol biru
│                              │
│  Tidak menerima kode?        │
│  Kirim ulang (02:45)         │  ← Resend dengan countdown
└──────────────────────────────┘
```

**Detail Komponen OTP:**

**6 Kotak OTP:**
- Setiap kotak: 48x56px, border 1.5px `#E5E7EB`, radius 10px, center text
- Font: `heading-lg` (22px bold), `#111827`
- Focus (kotak aktif): border `#2563EB`, shadow `0 0 0 3px rgba(37,99,235,0.15)`
- Terisi: border `#10B981`, background `#F0FDF4`
- Error: border `#EF4444`, semua kotak shake animation
- Auto-fokus ke kotak berikutnya saat karakter diisi
- Auto-submit saat 6 digit terisi semua
- Handle paste: jika paste 6 digit, isi semua kotak sekaligus

**Countdown Timer:**
- Format MM:SS, `body-md`, `#6B7280`
- Saat 0:00: teks berubah merah, "Kode kedaluwarsa"

**Tombol Verifikasi:**
- Disabled (abu-abu) jika OTP belum 6 digit
- Loading state saat memverifikasi

**Resend OTP:**
- Pertama: disabled dengan cooldown 2 menit
- "Kirim ulang (01:30)" — angka countdown, `caption`, `#6B7280`
- Setelah cooldown: "Kirim ulang" menjadi link biru yang bisa diklik
- Maksimal 3x kirim ulang sebelum blokir sementara

**Setelah OTP Berhasil:**
- Animasi centang hijau besar (lottie atau CSS animation)
- Teks "Akun berhasil dibuat!" 
- Auto-navigate ke MainApp setelah 1.5 detik

### 4.8 Alur Lupa Password

#### Layar 1 — Input Email (ForgotPasswordScreen)

```
┌──────────────────────────────┐
│ ←   Lupa Password            │
│                              │
│    🔑                        │  ← Icon kunci, biru
│                              │
│  Reset Password              │  ← Heading
│  Masukkan email akunmu,      │  ← Sub
│  kami kirimkan kode reset.   │
│                              │
│  Email                       │
│  [email@example.com       ]  │
│                              │
│  [   Kirim Kode Reset      ] │  ← Tombol biru
│                              │
│  ← Kembali ke Login          │  ← Link back
└──────────────────────────────┘
```

**Validasi:** email harus terdaftar di sistem. Jika tidak ditemukan, tampilkan error "Email tidak ditemukan".

#### Layar 2 — Verifikasi OTP Reset (OTPResetScreen)

- Sama persis dengan OTPVerifyScreen di atas
- Bedanya: sub-teks menyebut "kode reset password" bukan "verifikasi akun"
- Countdown 10 menit untuk kode OTP reset

#### Layar 3 — Buat Password Baru (ResetPasswordScreen)

```
┌──────────────────────────────┐
│ ←   Buat Password Baru       │
│                              │
│    🔒                        │  ← Icon gembok, biru
│                              │
│  Password Baru               │  ← Heading
│  Buat password yang kuat     │  ← Sub
│  dan mudah diingat.          │
│                              │
│  Password Baru               │
│  [••••••••••         👁    ] │
│                              │
│  [████████████] Sangat Kuat  │  ← Strength indicator
│  ✓ Min. 8 karakter           │
│  ✓ Mengandung angka          │
│  ✓ Huruf kapital             │
│  ✓ Karakter spesial          │
│                              │
│  Konfirmasi Password Baru    │
│  [••••••••••         👁    ] │
│                              │
│  [   Simpan Password Baru  ] │  ← Tombol biru
└──────────────────────────────┘
```

**Setelah berhasil:**
- Tampilkan modal/overlay: "Password berhasil diubah! ✓"
- Tombol "Masuk Sekarang" → navigate ke LoginScreen
- Data session lama di-invalidate (logout semua device lain)

### 4.9 Google Sign-In

#### Alur
```
1. User tap "Masuk/Daftar dengan Google"
2. Buka Google OAuth popup / native sheet
3. User pilih akun Google
4. App terima id_token dari Google
5. Kirim id_token ke backend untuk verifikasi
6. Backend return JWT app sendiri
7. Simpan JWT di SecureStore
8. Navigate ke MainApp
   → Jika akun baru: navigate ke ProfileSetupScreen (isi username)
   → Jika akun lama: langsung MainApp
```

### 4.13 Keamanan Auth

| Aspek | Implementasi |
|---|---|
| Password hashing | bcrypt, cost factor 12 |
| JWT Access Token | Expire 1 jam, signed HS256 |
| JWT Refresh Token | Expire 30 hari, rotasi setiap refresh |
| Token Storage | `expo-secure-store` (Keychain iOS / Keystore Android) |
| OTP | 6 digit random, bcrypt hash di DB, expire 10 menit, maks 3 attempt |
| Brute force login | Rate limit: 5 gagal → cooldown 30 detik, 10 gagal → blokir 15 menit |
| HTTPS | Wajib, semua request harus TLS 1.2+ |
| Refresh token rotation | Setiap kali refresh, token lama di-invalidate |
| Logout semua device | Hapus semua refresh token user dari DB |

### 4.14 Struktur File Auth

```
app/
├── (auth)/
│   ├── _layout.tsx           → Auth Stack Navigator
│   ├── landing.tsx           → AuthLandingScreen
│   ├── login.tsx             → LoginScreen
│   ├── register.tsx          → RegisterScreen (multi-step)
│   ├── otp-verify.tsx        → OTPVerifyScreen
│   ├── forgot-password.tsx   → ForgotPasswordScreen
│   ├── otp-reset.tsx         → OTPResetScreen
│   └── reset-password.tsx    → ResetPasswordScreen
│
components/auth/
├── OTPInput.tsx              → 6-kotak OTP component
├── PasswordInput.tsx         → Input + toggle visibilitas
├── PasswordStrengthBar.tsx   → Kekuatan password indicator
├── StepIndicator.tsx         → Step 1/2/3 indicator
├── SocialButton.tsx          → Tombol Google/Apple
└── UsernameChecker.tsx       → Real-time username availability
│
services/
└── authService.ts            → Semua API call auth
│
hooks/
├── useAuth.ts                → Hook untuk session & user state
├── useOTP.ts                 → Timer, resend, submit OTP
└── usePasswordStrength.ts    → Kalkulasi kekuatan password
│
stores/
└── authStore.ts              → Zustand store: user, token, status
```

---

## 5. Fitur: Home

### 4.1 Deskripsi

Halaman ringkasan terpusat. Menampilkan snapshot data keuangan, latihan, dan belajar dalam satu tampilan tanpa perlu berpindah tab.

### 4.2 Layout Layar

```
┌─────────────────────────────┐
│  Selamat pagi, [Nama] 👋    │  ← Greeting + nama user
│  Hari ini, [Tanggal]        │  ← Tanggal hari ini
│                             │
│  ┌──────────┐ ┌──────────┐  │
│  │ Saldo    │ │Pengeluaran│  │  ← Card keuangan
│  │ Rp 2,5jt │ │Rp 450rb  │  │
│  └──────────┘ └──────────┘  │
│                             │
│  ┌──────────────────────┐   │
│  │ Aktivitas Hari Ini   │   │  ← Section jadwal hari ini
│  │ □ Renang 07.00-08.00 │   │
│  │ □ Belajar Matematika  │   │
│  └──────────────────────┘   │
│                             │
│  ┌───────────┐ ┌──────────┐ │
│  │ Train     │ │ Study    │ │  ← Progress mingguan (mini chart)
│  │ 4/7 sesi  │ │ 5/7 sesi │ │
│  └───────────┘ └──────────┘ │
└─────────────────────────────┘
```

### 4.3 Komponen UI Detail

#### Header Greeting Card
- Background: Gradient `#2563EB → #1A1A2E`
- Teks nama: `heading-lg`, warna putih
- Tanggal: `caption`, warna `rgba(255,255,255,0.7)`
- Avatar user: 40x40px, border putih 2px, pojok kanan atas
- Border radius bawah: 24px

#### Summary Cards Keuangan (2-column grid)
- Card 1: **Saldo Utama**
  - Label: "Saldo" — `caption`, `#6B7280`
  - Nilai: `heading-lg`, `#111827`
  - Icon wallet: 20px, `#2563EB`
- Card 2: **Pengeluaran Bulan Ini**
  - Label: "Keluar" — `caption`, `#6B7280`
  - Nilai: `heading-lg`, `#EF4444`
  - Icon arrow-down: 20px, `#EF4444`
- Card padding: 16px
- Card background: `#FFFFFF`
- Shadow: `card-shadow`
- Border radius: 16px

#### Aktivitas Hari Ini
- Section title: "Aktivitas Hari Ini" — `heading-md`, `#111827`
- Sub-label: "Senin, [tanggal]" — `caption`, `#6B7280`
- List item: 
  - Checkbox kiri (status: todo/done/missed)
  - Nama aktivitas — `body-md`
  - Waktu — `caption`, `#6B7280`
  - Jika sudah done: teks hijau + icon centang
  - Jika missed: teks merah + icon X
- Tombol "Lihat Semua" biru link di kanan bawah section

#### Mini Progress Card (Train & Study)
- Dua card berdampingan
- Icon olahraga (dumbbell) untuk Train, icon buku untuk Study
- Judul: "Train" / "Study" — `label`, `#374151`
- Progress text: "4 dari 7 sesi" — `body-md`, `#111827`
- Mini progress bar: tinggi 6px, background `#E5E7EB`, fill `#2563EB`
- Persentase kecil di pojok kanan: `caption`, `#2563EB`

---

## 6. Fitur: Keuangan

### 5.1 Deskripsi Lengkap

Modul pencatatan keuangan personal dengan visualisasi data dan kemampuan ekspor ke PDF dan Google Spreadsheet.

### 5.2 Layar Utama Keuangan (FinanceScreen)

#### Header Balance Section
- Background: Gradient hero `#2563EB → #1A1A2E`, tinggi ~180px
- Label "Total Saldo": `caption`, putih `rgba(255,255,255,0.8)`
- Nilai saldo: `heading-xl` (28px), bold, putih `#FFFFFF`
- Tombol visibility (mata): toggle tampilkan/sembunyikan saldo
  - Icon: `eye` / `eye-off`, warna putih
  
#### Income / Expense Summary Bar (di bawah header hero)
- Dua chip horizontal dalam satu card:
  - **Pemasukan**: icon `arrow-up-circle`, label hijau `#10B981`, nilai Rp X
  - **Pengeluaran**: icon `arrow-down-circle`, label merah `#EF4444`, nilai Rp X
- Garis pemisah vertikal `#E5E7EB` di tengah
- Card background: `#FFFFFF`, shadow: `card-shadow`
- Margin horizontal: -16px (overlap ke atas hero section untuk efek kartu melayang)

#### Filter Periode
- Chips: "Hari Ini", "Minggu Ini", "Bulan Ini", "Custom"
- Chip aktif: background `#DBEAFE`, border `#2563EB`, teks `#2563EB`
- Chip non-aktif: background `#F3F4F6`, teks `#6B7280`
- Scroll horizontal, tidak ada scrollbar

#### Section Chart
**Bar Chart Mingguan:**
- X-axis: 7 hari terakhir (Sen, Sel, Rab, Kam, Jum, Sab, Min)
- Y-axis: nilai Rp (ribuan)
- 2 bar per hari: Biru `#2563EB` (pemasukan) + Merah `#EF4444` (pengeluaran)
- Legend di bawah chart
- Library: `victory-native`

**Pie Chart Kategori:**
- Tampilkan top 5 kategori pengeluaran
- Warna slice: gunakan palet konsisten per kategori
  - Makan & Minum: `#2563EB`
  - Transport: `#10B981`
  - Belanja: `#F59E0B`
  - Kesehatan: `#EF4444`
  - Lainnya: `#6B7280`
- Legend kanan chart: nama kategori + persentase
- Di tengah donut chart: total pengeluaran periode aktif

#### Tombol Aksi
- **Download PDF**: icon `file-down`, teks "Export PDF", outline button biru
- **Lihat Spreadsheet**: icon `external-link`, teks "Buka Spreadsheet", outline button hijau
- Keduanya di bawah chart dalam 2-column grid

#### Daftar Transaksi
- Section title: "Riwayat Transaksi" + "Lihat Semua" link
- Setiap item:
  - **Kiri**: icon kategori dalam lingkaran berwarna (16x16px icon dalam circle 40x40px)
  - **Tengah**: nama transaksi (`body-md`, bold), tanggal + waktu (`caption`, muted)
  - **Kanan**: jumlah (positif hijau = pemasukan, negatif merah = pengeluaran), `body-md bold`
- Swipe kiri untuk hapus (haptic feedback)
- Tap untuk detail

#### FAB Tambah Transaksi
- Posisi: pojok kanan bawah, 24px dari tepi dan bottom nav
- Ukuran: 56x56px, circle
- Background: `#2563EB`
- Icon: `plus`, warna putih, 24px
- Shadow: `modal-shadow`

### 5.3 Layar Tambah Transaksi (AddTransactionScreen)

#### Tipe Toggle
- Dua tombol besar di atas: "Pemasukan" (hijau) | "Pengeluaran" (merah)
- Aktif: filled background, teks putih
- Non-aktif: outline, teks sesuai warna

#### Form Fields
| Field | Tipe Input | Validasi |
|---|---|---|
| Jumlah | Numpad, Rp prefix | Wajib, > 0 |
| Kategori | Dropdown / grid icon picker | Wajib |
| Tanggal | Date picker | Default hari ini |
| Waktu | Time picker | Default sekarang |
| Catatan | TextArea, max 200 char | Opsional |
| Foto bukti | Upload dari galeri atau kamera | Opsional |

#### Kategori Pengeluaran (icon grid)
```
🍜 Makan     🚗 Transport   🛍 Belanja
🏥 Kesehatan  📚 Pendidikan  🎮 Hiburan
💡 Tagihan    🏠 Rumah       ➕ Lainnya
```

#### Kategori Pemasukan
```
💼 Gaji      🎯 Freelance   💰 Investasi
🎁 Hadiah    📦 Penjualan   ➕ Lainnya
```

### 5.4 Export PDF

#### Isi Laporan PDF
1. **Header**: Logo app + judul "Laporan Keuangan", nama user, periode
2. **Ringkasan**: Total saldo, total pemasukan, total pengeluaran
3. **Bar Chart**: (rendered sebagai image base64)
4. **Pie Chart**: (rendered sebagai image base64)
5. **Tabel Transaksi**: kolom tanggal, keterangan, kategori, jumlah
6. **Footer**: tanggal generate + nama app

### 5.5 Integrasi Spreadsheet (lihat juga Bab 13)

- Tombol "Buka Spreadsheet" → `Linking.openURL(spreadsheetUrl)`
- Sinkronisasi otomatis: setiap kali menambah transaksi, push data ke Google Sheets via API
- Fallback: jika offline, queue data dan sync saat online

---

## 7. Fitur: Train & Study

### 6.1 Deskripsi Lengkap

Modul penjadwalan dan pelacakan konsistensi latihan fisik (Train) dan belajar (Study). Dilengkapi notifikasi pengingat, ceklis harian, dan visualisasi progress.

### 6.2 Layar Utama Jadwal (ScheduleScreen)

#### Filter Tab (Segmented Control)
- Tiga tombol: **All** | **Train** | **Study**
- Tab aktif: background `#2563EB`, teks putih, border-radius pill
- Non-aktif: background `#F3F4F6`, teks `#374151`
- Sticky di bawah header, tidak scroll

#### Progress Summary Bar
- Ditampilkan di atas daftar jadwal (tergantung tab aktif)
- **Tab All**: dua mini stat — Train X/Y, Study A/B minggu ini
- **Tab Train**: "X dari Y sesi latihan minggu ini terpenuhi" + bar progress hijau
- **Tab Study**: "X dari Y sesi belajar minggu ini terpenuhi" + bar progress biru

#### Daftar Jadwal (Schedule Card)

Setiap card jadwal menampilkan:

```
┌─────────────────────────────────┐
│ [icon] Renang                   │  ← Judul + icon olahraga/buku
│        Selasa, 1 Jul – 31 Jul   │  ← Tanggal mulai–akhir
│        07.00 – 09.00            │  ← Jam mulai–akhir
│                                 │
│  ████████░░░░  65%              │  ← Progress bar + persentase
│  Hari ini: [✓] Selesai          │  ← Status hari ini
│                                 │
│  [✓ Selesai]      [✗ Lewati]    │  ← Tombol aksi hari ini
└─────────────────────────────────┘
```

**Detail Warna Card:**
- Background: `#FFFFFF`
- Border kiri: 4px solid — Train `#2563EB`, Study `#10B981`
- Icon container: 40x40px circle, bg sesuai warna fitur
- Progress bar bg: `#E5E7EB`, fill: `#2563EB` (train) / `#10B981` (study)
- Tombol ✓ Selesai: background `#10B981`, teks putih, radius 8px
- Tombol ✗ Lewati: background `#FEE2E2`, teks `#EF4444`, radius 8px
- Jika sudah di-ceklis hari ini: tombol disabled, tampilkan badge status

**Badge Status Hari Ini:**
- "✓ Terpenuhi" — background `#DCFCE7`, teks `#166534`, pill shape
- "✗ Dilewati" — background `#FEE2E2`, teks `#991B1B`, pill shape
- "○ Belum" — background `#F3F4F6`, teks `#6B7280`, pill shape

#### Section Bar Chart Progress (per tab)
- Ditampilkan di bawah daftar jadwal
- **Train Chart**: bar chart 4 minggu terakhir, jumlah sesi terpenuhi vs total
- **Study Chart**: bar chart 4 minggu terakhir, jumlah sesi terpenuhi vs total
- Warna bar: Train = `#2563EB`, Study = `#10B981`
- Ghost bar (total planned): `#E5E7EB`
- X-axis: label minggu (Minggu 1, Minggu 2, dst)
- Y-axis: jumlah sesi (0, 2, 4, 6, ...)

### 6.3 Layar Tambah Jadwal (AddScheduleScreen)

#### Pilih Tipe
- Toggle dua opsi di atas form: **Train** (biru) | **Study** (hijau)

#### Form Fields

| Field | Tipe | Detail |
|---|---|---|
| Judul Aktivitas | TextInput | Cth: "Renang", "Matematika" |
| Fokus Area | TextInput | Cth: "Kaki & cardio", "Bab 3 limit" |
| Tanggal Mulai | DatePicker | Default hari ini |
| Tanggal Akhir | DatePicker | Harus >= tanggal mulai |
| Jam Mulai | TimePicker | Format HH:mm |
| Jam Selesai | TimePicker | Harus > jam mulai |
| Hari Aktif | Multi-select chips | Sen, Sel, Rab, Kam, Jum, Sab, Min |
| Pengingat Notifikasi | Toggle + offset picker | Cth: "15 menit sebelum", "30 menit sebelum" |
| Catatan | TextArea | Opsional, max 300 char |
| Icon / Warna | Picker visual | 10 pilihan icon + 6 warna accent |

#### Validasi
- Semua field wajib kecuali catatan
- Tanggal akhir tidak boleh lebih awal dari tanggal mulai
- Jam selesai tidak boleh lebih awal atau sama dengan jam mulai
- Minimal durasi: 15 menit

### 6.4 Logika Ceklis Harian

```
Setiap hari pada jam jadwal dimulai:
  → Notifikasi pengingat muncul
  → Record log harian dibuat dengan status: "pending"

User tap ✓ Selesai:
  → Log status: "done"
  → Progress +1
  → Haptic feedback: success
  → Badge hari ini berubah ke "✓ Terpenuhi"

User tap ✗ Lewati:
  → Log status: "missed"
  → Progress tidak berubah
  → Haptic feedback: warning
  → Badge hari ini berubah ke "✗ Dilewati"

Jam 23:59 (auto):
  → Jika log masih "pending" → auto set ke "missed"
  → Progress konsistensi berkurang
```

### 6.5 Kalkulasi Progress

```
Completion Rate = (jumlah "done") / (total sesi yang sudah berlalu) × 100%

Streak = jumlah hari berturut-turut dengan status "done"

Weekly Summary:
  done_this_week / planned_this_week × 100%
```

---

## 8. Fitur: Scan

### 7.1 Deskripsi

Fitur kamera untuk memindai (1) struk belanja agar otomatis masuk ke catatan keuangan, dan (2) screenshot/foto catatan agar bisa diekstrak ke jadwal Train/Study.

### 7.2 Layar Pilih Mode (ScanModeScreen)

```
┌─────────────────────────────┐
│         Pilih Mode Scan     │
│                             │
│  ┌───────────────────────┐  │
│  │  📄  Scan Struk       │  │
│  │  Masukan ke Keuangan  │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │  📷  Scan Data Jadwal │  │
│  │  Train / Study        │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

- Dua card besar, masing-masing 50% tinggi layar (minus nav bar)
- Card 1 (Keuangan): background `#EFF6FF`, border `#2563EB`, icon warna `#2563EB`
- Card 2 (Jadwal): background `#F0FDF4`, border `#10B981`, icon warna `#10B981`
- Tap salah satu → buka `CameraScreen` dengan mode yang sesuai

### 7.3 Layar Kamera (CameraScreen)

- Penuh layar (full screen viewfinder)
- Overlay frame: sudut-sudut rounded, animasi shimmer untuk guide user
- Teks instruksi di atas frame: 
  - Mode struk: "Arahkan kamera ke struk, pastikan teks terbaca jelas"
  - Mode jadwal: "Foto catatan / screenshot data latihan atau belajar"
- Tombol capture: circle besar (64x64px) putih dengan border `#2563EB`
- Tombol galeri: pojok kiri, icon `image`, untuk memilih dari galeri
- Tombol flash: pojok kanan, toggle `#FFFFFF` / `#F59E0B` (aktif)
- Back button: pojok kiri atas

### 7.4 Layar Hasil Scan & Konfirmasi (ScanResultScreen)

#### Mode Struk — Ekstraksi Keuangan
- Tampilkan foto struk di atas (40% tinggi layar)
- Teks yang berhasil dideteksi ditampilkan dengan highlight box
- Form pra-isi (auto-fill dari OCR):
  - Nama toko / merchant (editable)
  - Total belanja (editable)
  - Tanggal (editable, default dari OCR atau hari ini)
  - Tipe transaksi: Pengeluaran (default) / Pemasukan
  - Kategori (dropdown, default "Belanja")
  - Catatan (opsional)
- Tombol **Simpan ke Keuangan** (biru, full-width)
- Tombol **Scan Ulang** (outline, full-width)

#### Mode Jadwal — Ekstraksi Train/Study
- Tampilkan foto di atas
- Teks yang terdeteksi dibagi dalam daftar baris
- User menandai baris mana yang ingin dijadikan jadwal (multi-select)
- Form pra-isi:
  - Tipe: Train atau Study
  - Judul aktivitas (dari OCR atau manual)
  - Fokus area (dari OCR atau manual)
  - Tanggal mulai / akhir (manual, karena OCR tidak selalu punya info ini)
  - Jam mulai / selesai (manual)
- Tombol **Simpan ke Jadwal** (hijau, full-width)
- Tombol **Scan Ulang** (outline, full-width)

## 9. Fitur: Akun

### 8.1 Layar Profil Akun (AccountScreen)

#### Header Profil
- Avatar bulat: 80x80px, background `#DBEAFE`, inisial nama user (jika tidak ada foto)
- Nama lengkap: `heading-md`, `#111827`
- Email: `body-md`, `#6B7280`
- Tombol "Edit Profil": outline kecil, di bawah email

#### Settings List

Setiap item settings:
- Icon kiri (24px, `#6B7280`)
- Label teks (`body-md`, `#374151`)
- Value atau chevron kanan

| Item | Icon | Keterangan |
|---|---|---|
| Link Spreadsheet | `table` | Tampilkan URL singkat; tap untuk ubah |
| Notifikasi | `bell` | Toggle on/off + preferensi |
| Tema | `sun` | Terang / Gelap / Otomatis |
| Privasi & Keamanan | `shield` | Lock app, biometrik |
| Backup Data | `cloud-upload` | Export data lokal |
| Tentang App | `info` | Versi, lisensi |
| Logout | `log-out` | Merah `#EF4444`, konfirmasi dialog |

#### Divider antara Group
- Tipis, `#E5E7EB`, 0.5px

### 8.2 Layar Setting Spreadsheet (SpreadsheetSettingScreen)

```
┌─────────────────────────────┐
│  Link Google Spreadsheet    │
│                             │
│  [                       ]  │  ← TextInput, URL Google Sheets
│                             │
│  Panduan:                   │
│  1. Buka Google Sheets      │
│  2. Klik Share → Copy Link  │
│  3. Tempel di atas          │
│                             │
│  [Uji Koneksi]              │  ← Validasi link
│  [Simpan]                   │  ← Simpan ke akun
└─────────────────────────────┘
```

**Validasi Link:**
- Cek format URL Google Sheets: `docs.google.com/spreadsheets/d/[ID]`
- Cek akses baca/tulis via Google Sheets API
- Feedback: sukses (hijau) / gagal (merah) + pesan error

### 8.3 Layar Setting Notifikasi (NotificationSettingScreen)

| Setting | Tipe |
|---|---|
| Aktifkan notifikasi jadwal | Master toggle |
| Waktu pengingat sebelum jadwal | Picker: 5, 10, 15, 30, 60 menit |
| Notifikasi ringkasan harian | Toggle + jam picker |
| Notifikasi ringkasan mingguan | Toggle + hari + jam picker |
| Notifikasi batas anggaran | Toggle (dari fitur keuangan) |

---

## 12. Notifikasi

### 11.1 Tipe Notifikasi

| ID | Judul | Isi | Trigger |
|---|---|---|---|
| `schedule_reminder` | "Waktunya [title]!" | "[focus_area] — mulai jam [start_time]" | X menit sebelum jadwal |
| `daily_summary` | "Ringkasan Hari Ini" | "X jadwal terpenuhi, Y terlewat" | Setiap malam jam 21.00 |
| `weekly_report` | "Progress Minggu Ini 📊" | "Train: X%, Study: Y%" | Setiap Minggu malam |
| `budget_alert` | "Perhatian: Anggaran" | "Pengeluaran sudah X% dari batas" | Saat expense > 80% budget |

## struktur Sheet

**Sheet "Keuangan":**
| A: Tanggal | B: Waktu | C: Tipe | D: Kategori | E: Keterangan | F: Jumlah | G: Saldo |
|---|---|---|---|---|---|---|

**Sheet "Train_Log":**
| A: Tanggal | B: Judul | C: Fokus | D: Jam Mulai | E: Jam Selesai | F: Status |
|---|---|---|---|---|---|

**Sheet "Study_Log":**
| A: Tanggal | B: Judul | C: Fokus | D: Jam Mulai | E: Jam Selesai | F: Status |
|---|---|---|---|---|---|