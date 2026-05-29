# AGENTS.md — CuOne: Pencatatan Keuangan Cerdas

> Dokumen ini adalah panduan lengkap untuk AI coding agent dalam membangun aplikasi Android
> pencatatan keuangan berbasis Kotlin + Jetpack Compose. Ikuti setiap aturan, struktur, dan
> spesifikasi fitur dengan ketat.

---

## DAFTAR ISI

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Design System](#3-design-system)
4. [Coding Rules](#4-coding-rules)
5. [Architecture Overview](#5-architecture-overview)
6. [File Structure](#6-file-structure)
7. [Data Models](#7-data-models)
8. [Feature Specifications](#8-feature-specifications)
9. [API Integrations](#9-api-integrations)
10. [Navigation & UI Flow](#10-navigation--ui-flow)
11. [State Management](#11-state-management)
12. [Build.Gradle Dependencies](#12-buildgradle-dependencies)
13. [UI Components Contract](#13-ui-components-contract)
14. [AndroidManifest — Permissions](#14-androidmanifestxml--permissions--konfigurasi)
15. [Error & Empty States](#15-error--empty-states)

---

## 1. PROJECT OVERVIEW

**Nama Aplikasi:** CuOne  
**Platform:** Android (min SDK 26 / Android 8.0)  
**Bahasa:** Kotlin 2.x  
**UI:** Jetpack Compose + Material Design 3

### Konsep Utama

- Pengguna pertama kali install → input **nama** (disimpan lokal, bukan di Sheets)
- Database utama adalah **Google Spreadsheet milik user** (bukan server pihak ketiga)
- Semua data hilang jika uninstall (by design — data ada di Sheets user)
- AI engine: **OpenRouter API** model `nvidia/llama-3.1-nemotron-ultra-253b-v1`
- Fitur scan struk/QRIS menggunakan **ML Kit OCR** + AI untuk ekstraksi data

### Filosofi Desain

- **Data sovereignty:** user pegang kendali penuh data mereka via Google Sheets
- **AI-first:** setiap friction point di-solve dengan AI (scan, kategorisasi, chat)
- **Offline-first:** transaksi bisa dicatat tanpa internet, sync otomatis saat online
- **Zero backend:** tidak ada server milik app, hanya Sheets API + OpenRouter API

---

## 2. TECH STACK

### Core

| Komponen        | Library/Tool                           |
| --------------- | -------------------------------------- |
| Language        | Kotlin 2.0+                            |
| UI              | Jetpack Compose BOM 2024.x             |
| DI              | Hilt 2.51+                             |
| Async           | Coroutines + Flow                      |
| Local Storage   | DataStore Preferences (nama, settings) |
| Offline Queue   | Room 2.6+                              |
| Navigation      | Navigation Compose 2.7+                |
| Background Sync | WorkManager 2.9+                       |

### Network & API

| Komponen      | Library/Tool                             |
| ------------- | ---------------------------------------- |
| HTTP Client   | Retrofit 2.11 + OkHttp 4.12              |
| JSON          | Kotlinx Serialization                    |
| Google Sheets | Google Sheets API v4 (REST via Retrofit) |
| AI            | OpenRouter API (REST)                    |
| Auth Google   | Google Sign-In + OAuth2                  |

### AI & Vision

| Komponen        | Library/Tool                                           |
| --------------- | ------------------------------------------------------ |
| OCR             | ML Kit Text Recognition v2                             |
| Image Crop/Pick | UCrop + Photo Picker API                               |
| AI Model        | nvidia/llama-3.1-nemotron-ultra-253b-v1 via OpenRouter |

### UI & Visualization

| Komponen      | Library/Tool                                 |
| ------------- | -------------------------------------------- |
| Charts        | Vico 1.x (Jetpack Compose native)            |
| Image Loading | Coil 2.6                                     |
| Animations    | Compose Animation + Lottie                   |
| Share Image   | Jetpack Compose Canvas → Bitmap → ShareSheet |

---

## 3. DESIGN SYSTEM

### Palet Warna — Komposisi 60/30/10

| Token        | Hex       | Komposisi | Penggunaan                                                 |
| ------------ | --------- | --------- | ---------------------------------------------------------- |
| `Background` | `#FCFBF4` | 60%       | Background semua screen, surface card, scaffold            |
| `Secondary`  | `#84A98C` | 30%       | Header, bottom navigation, kartu saldo, section title      |
| `Accent`     | `#E76F51` | 10%       | Sorotan pengeluaran, tombol aksi utama, badge anomali, FAB |

**Warna pendukung (turunan dari palet utama):**

| Token                  | Hex       | Penggunaan                                       |
| ---------------------- | --------- | ------------------------------------------------ |
| `OnBackground`         | `#2C2C2C` | Teks utama di atas Background                    |
| `OnSecondary`          | `#FFFFFF` | Teks/ikon di atas Secondary                      |
| `OnAccent`             | `#FFFFFF` | Teks/ikon di atas Accent                         |
| `SecondaryContainer`   | `#C8DFC0` | Card ringan, chip kategori, selected state       |
| `OnSecondaryContainer` | `#1A3D22` | Teks di atas SecondaryContainer                  |
| `BackgroundVariant`    | `#F0EEE4` | Divider, input field background, shimmer base    |
| `TextSecondary`        | `#6B7280` | Label, caption, placeholder text                 |
| `IncomeGreen`          | `#52A675` | Highlight angka pemasukan                        |
| `ExpenseRed`           | `#E76F51` | Highlight angka pengeluaran (sama dengan Accent) |
| `SurfaceError`         | `#FDF0ED` | Background banner error/warning ringan           |

**Implementasi di `Color.kt`:**

```kotlin
// ui/theme/Color.kt
val Background         = Color(0xFFFCFBF4)
val Secondary          = Color(0xFF84A98C)
val Accent             = Color(0xFFE76F51)
val OnBackground       = Color(0xFF2C2C2C)
val OnSecondary        = Color(0xFFFFFFFF)
val OnAccent           = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFC8DFC0)
val OnSecondaryContainer = Color(0xFF1A3D22)
val BackgroundVariant  = Color(0xFFF0EEE4)
val TextSecondary      = Color(0xFF6B7280)
val IncomeGreen        = Color(0xFF52A675)
val ExpenseRed         = Color(0xFFE76F51)
val SurfaceError       = Color(0xFFFDF0ED)
```

**Implementasi di `Theme.kt`:**

```kotlin
// ui/theme/Theme.kt
private val CuOnecolorScheme = lightColorScheme(
    primary          = Secondary,          // Elemen primer Material3 → warna hijau
    onPrimary        = OnSecondary,
    primaryContainer = SecondaryContainer,
    onPrimaryContainer = OnSecondaryContainer,
    secondary        = Accent,             // Elemen sekunder Material3 → oranye
    onSecondary      = OnAccent,
    background       = Background,
    onBackground     = OnBackground,
    surface          = Background,
    onSurface        = OnBackground,
    surfaceVariant   = BackgroundVariant,
    error            = Accent
)
```

---

### Aturan Penggunaan Warna

**60% Background (`#FCFBF4`):**

- `Scaffold` background
- `LazyColumn` / `LazyRow` background
- Surface di dalam card yang tidak memiliki warna khusus
- Background screen Analytics, Goals, Settings

**30% Secondary (`#84A98C`):**

- `TopAppBar` / Header setiap screen
- `BottomNavigationBar`
- `BalanceCard` (kartu saldo utama di dashboard)
- Section title chip/label
- Selected state pada BottomNav item
- Progress bar pada SavingsGoalCard (track)

**10% Accent (`#E76F51`):**

- FAB (Floating Action Button)
- Nominal pengeluaran (teks angka merah-oranye)
- Tombol "Simpan" / "Konfirmasi" pada form transaksi
- Badge notifikasi anomali
- Highlight pada chart saat pengeluaran melonjak
- Tombol aksi destruktif (hapus, reset)

---

### Sistem Ikon — Lucide Icons

**Aturan wajib:**

- **Tidak ada emoji** di seluruh UI aplikasi
- Semua ikon menggunakan library **Lucide Android** (`com.lucide:lucide-android`)
- Ukuran ikon standar: `18.dp` (inline), `24.dp` (navigasi/list), `32.dp` (ilustrasi kecil)
- Warna ikon mengikuti konteks: `OnSecondary` di atas Secondary, `OnBackground` di atas Background

**Mapping ikon per fitur:**

| Konteks                | Lucide Icon            |
| ---------------------- | ---------------------- |
| Pemasukan              | `TrendingUp`           |
| Pengeluaran            | `TrendingDown`         |
| Scan struk             | `ScanLine`             |
| Input teks bebas       | `MessageSquare`        |
| Input manual           | `PenLine`              |
| Dashboard / Home       | `LayoutDashboard`      |
| Analytics              | `BarChart3`            |
| AI Chat                | `Bot`                  |
| Goals / Tabungan       | `Target`               |
| Settings / Preferences | `Settings2`            |
| Profil user            | `UserRound`            |
| Pekerjaan              | `Briefcase`            |
| Anggaran bulanan       | `Wallet`               |
| Penghasilan            | `Banknote`             |
| Google Sheets          | `Sheet` (atau `Table`) |
| Notifikasi             | `Bell`                 |
| Sync / loading         | `RefreshCw`            |
| Anomali / warning      | `AlertCircle`          |
| Sukses / centang       | `CircleCheck`          |
| Hapus                  | `Trash2`               |
| Edit                   | `Pencil`               |
| Tambah                 | `Plus`                 |
| Kembali                | `ChevronLeft`          |
| Share                  | `Share2`               |
| Buka link eksternal    | `ExternalLink`         |
| Offline                | `WifiOff`              |
| Kamera                 | `Camera`               |
| Galeri                 | `Image`                |

---

### Tipografi

```kotlin
// ui/theme/Type.kt
val CuOneTypography = Typography(
    // Judul besar (nama di dashboard, total saldo)
    headlineLarge  = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold,   color = OnBackground),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall  = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    // Body
    bodyLarge      = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,  color = OnBackground),
    bodySmall      = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,  color = TextSecondary),
    // Label
    labelLarge     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall     = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium,  color = TextSecondary),
)
```

---

## 4. CODING RULES

### Wajib Diikuti

- **Maksimal 400 baris per file** — jika melebihi, pecah menjadi file terpisah
- Gunakan **`Result<T>`** untuk semua operasi yang bisa gagal (network, IO)
- Semua string user-facing disimpan di **`res/values/strings.xml`** (support i18n)
- Tidak ada **hardcoded API key** dalam kode — seluruhnya dari `local.properties` atau input user
- Setiap ViewModel hanya boleh memiliki **satu UiState sealed class**
- Setiap file harus punya **KDoc** minimal di class/object utamanya
- **No God class:** setiap class hanya memiliki satu tanggung jawab (SRP)
- Repository tidak boleh import apapun dari layer `ui/` atau `feature/`

### Naming Convention

```
Screens          → [Feature]Screen.kt         (DashboardScreen.kt)
ViewModels       → [Feature]ViewModel.kt      (DashboardViewModel.kt)
Repositories     → [Feature]Repository.kt     (TransactionRepository.kt)
Use Cases        → [Action][Domain]UseCase.kt (GetMonthlyInsightUseCase.kt)
UI State         → [Feature]UiState.kt        (DashboardUiState.kt)
Room Entities    → [Name]Entity.kt            (OfflineTransactionEntity.kt)
Room DAOs        → [Name]Dao.kt               (TransactionQueueDao.kt)
DI Modules       → [Scope]Module.kt           (NetworkModule.kt)
Compose Comps    → [Name]Component.kt         (BalanceCardComponent.kt)
```

### File Splitting Rules

Jika sebuah file mendekati 350 baris, pecah menggunakan pola:

```
TransactionRepository.kt         ← interface + data operations
TransactionRepositoryImpl.kt     ← implementation detail
TransactionMapper.kt             ← mapping logic
TransactionValidator.kt          ← validation logic
```

---

## 5. ARCHITECTURE OVERVIEW

Menggunakan **Clean Architecture** dengan 3 layer utama:

```
┌─────────────────────────────────────────────┐
│              PRESENTATION LAYER             │
│   Screen (Compose) ← ViewModel ← UiState   │
├─────────────────────────────────────────────┤
│               DOMAIN LAYER                 │
│         UseCase ← Repository Interface      │
├─────────────────────────────────────────────┤
│                DATA LAYER                  │
│  RepositoryImpl → [SheetsDS | LocalDS | AI] │
└─────────────────────────────────────────────┘
```

**Data flow:** UI Event → ViewModel → UseCase → Repository → DataSource → (mapping) → UiState

**Offline-first flow:**

```
User input transaksi
    ↓
Simpan ke Room (OfflineQueue)
    ↓
WorkManager cek koneksi
    ↓ (ada internet)
Sync ke Google Sheets
    ↓
Hapus dari Room queue
    ↓
Update UI dari Sheets
```

---

## 6. FILE STRUCTURE

```
app/src/main/java/com/cuan/
│
├── core/                                   # Infrastruktur global
│   ├── network/
│   │   ├── RetrofitClient.kt               # OkHttp + Retrofit setup, interceptors
│   │   ├── SheetsApiService.kt             # Google Sheets REST endpoints
│   │   ├── OpenRouterApiService.kt         # OpenRouter REST endpoints
│   │   └── NetworkMonitor.kt              # Observasi status koneksi internet
│   │
│   ├── local/
│   │   ├── AppDataStore.kt                 # DataStore: profil user, sheet URL, settings
│   │   ├── AppDatabase.kt                  # Room database definition
│   │   ├── dao/
│   │   │   ├── TransactionQueueDao.kt      # CRUD offline queue transaksi
│   │   │   └── SavingsGoalDao.kt           # CRUD tujuan tabungan
│   │   └── entity/
│   │       ├── OfflineTransactionEntity.kt # Entitas antrian transaksi offline
│   │       └── SavingsGoalEntity.kt        # Entitas target tabungan
│   │
│   ├── sync/
│   │   ├── SyncWorker.kt                   # WorkManager worker untuk push ke Sheets
│   │   ├── SyncManager.kt                  # Orkestrasi sync, retry logic
│   │   └── SyncScheduler.kt               # Jadwal sync periodic & immediate
│   │
│   ├── di/
│   │   ├── NetworkModule.kt                # Provides Retrofit, OkHttp, ApiServices
│   │   ├── DatabaseModule.kt               # Provides Room DB, DAOs
│   │   ├── DataStoreModule.kt              # Provides DataStore
│   │   └── RepositoryModule.kt            # Binds interface → implementation
│   │
│   └── utils/
│       ├── Extensions.kt                   # Kotlin extension functions umum
│       ├── DateUtils.kt                    # Format tanggal, range bulan, dll
│       ├── CurrencyUtils.kt               # Format Rupiah, parsing nominal
│       ├── ImageUtils.kt                   # Compress, rotate, bitmap helpers
│       └── ResultUtils.kt                 # Result<T> helpers, fold extensions
│
├── data/
│   ├── model/                              # Pure data classes (domain model)
│   │   ├── Transaction.kt
│   │   ├── Category.kt
│   │   ├── SavingsGoal.kt
│   │   ├── MonthlyInsight.kt
│   │   ├── AnomalyAlert.kt
│   │   ├── UserProfile.kt                  # [BARU] Profil lengkap user
│   │   └── SheetsConfig.kt
│   │
│   ├── repository/
│   │   ├── TransactionRepository.kt        # Interface
│   │   ├── TransactionRepositoryImpl.kt    # Impl: Sheets + offline queue
│   │   ├── SheetsRepository.kt             # Interface
│   │   ├── SheetsRepositoryImpl.kt         # Impl: setup, buat tab, summary
│   │   ├── AIRepository.kt                 # Interface
│   │   ├── AIRepositoryImpl.kt             # Impl: OpenRouter calls
│   │   ├── UserRepository.kt               # Interface
│   │   └── UserRepositoryImpl.kt           # Impl: DataStore read/write profil
│   │
│   └── mapper/
│       ├── TransactionMapper.kt            # Entity ↔ Domain ↔ SheetsRow
│       └── SheetsRowMapper.kt             # Raw Sheets values → domain models
│
├── domain/                                 # Use cases (business logic)
│   ├── transaction/
│   │   ├── AddTransactionUseCase.kt
│   │   ├── GetTransactionsUseCase.kt
│   │   ├── DeleteTransactionUseCase.kt
│   │   └── SyncPendingTransactionsUseCase.kt
│   │
│   ├── analytics/
│   │   ├── GetMonthlyInsightUseCase.kt
│   │   ├── GetTopExpensesUseCase.kt
│   │   ├── GetDailyAverageUseCase.kt
│   │   ├── GetMonthlyComparisonUseCase.kt
│   │   ├── GetTransactionPatternUseCase.kt
│   │   └── DetectAnomalyUseCase.kt
│   │
│   ├── ai/
│   │   ├── ParseReceiptUseCase.kt
│   │   ├── ParseFreeTextUseCase.kt
│   │   ├── AutoCategorizeUseCase.kt
│   │   ├── ChatWithFinanceUseCase.kt
│   │   └── GetSavingTipsUseCase.kt
│   │
│   ├── sheets/
│   │   ├── SetupSheetUseCase.kt
│   │   ├── CreateMonthlyTabUseCase.kt
│   │   └── UpdateSummaryTabUseCase.kt
│   │
│   ├── user/                               # [BARU]
│   │   ├── GetUserProfileUseCase.kt
│   │   ├── SaveUserProfileUseCase.kt
│   │   └── IsProfileCompleteUseCase.kt     # Cek apakah semua field wajib terisi
│   │
│   └── goals/
│       ├── AddSavingsGoalUseCase.kt
│       ├── GetSavingsGoalsUseCase.kt
│       └── CalculateSavingsRateUseCase.kt
│
├── feature/                                # Presentation layer per fitur
│   │
│   ├── splash/                             # [BARU]
│   │   └── SplashScreen.kt                # Logo + animasi 1.5 detik, cek DataStore
│   │
│   ├── onboarding/
│   │   ├── OnboardingCarouselScreen.kt     # [BARU] 3-slide carousel tentang fitur app
│   │   ├── OnboardingCarouselViewModel.kt  # [BARU]
│   │   ├── OnboardingNameScreen.kt         # Input nama user (renamed dari OnboardingScreen)
│   │   ├── OnboardingNameViewModel.kt
│   │   ├── SheetsSetupScreen.kt
│   │   ├── SheetsSetupViewModel.kt
│   │   └── components/
│   │       ├── CarouselSlideComponent.kt   # [BARU] Satu slide carousel
│   │       └── CarouselIndicatorComponent.kt # [BARU] Dot indicator
│   │
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── DashboardUiState.kt
│   │   └── components/
│   │       ├── BalanceCardComponent.kt
│   │       ├── QuickStatsComponent.kt
│   │       ├── RecentTransactionComponent.kt
│   │       ├── AnomalyBannerComponent.kt
│   │       └── ProfileIncompleteBannerComponent.kt  # [BARU] Alert lengkapi profil
│   │
│   ├── transaction/
│   │   ├── add/
│   │   │   ├── AddTransactionScreen.kt
│   │   │   ├── AddTransactionViewModel.kt
│   │   │   └── AddTransactionUiState.kt
│   │   │
│   │   ├── scan/
│   │   │   ├── ScanScreen.kt
│   │   │   ├── ScanViewModel.kt
│   │   │   ├── ScanUiState.kt
│   │   │   ├── OcrProcessor.kt
│   │   │   └── ScanResultScreen.kt
│   │   │
│   │   ├── freetext/
│   │   │   ├── FreeTextScreen.kt
│   │   │   ├── FreeTextViewModel.kt
│   │   │   └── FreeTextUiState.kt
│   │   │
│   │   └── list/
│   │       ├── TransactionListScreen.kt
│   │       ├── TransactionListViewModel.kt
│   │       └── components/
│   │           ├── TransactionItemComponent.kt
│   │           └── TransactionFilterComponent.kt
│   │
│   ├── analytics/
│   │   ├── AnalyticsScreen.kt
│   │   ├── AnalyticsViewModel.kt
│   │   ├── AnalyticsUiState.kt
│   │   └── components/
│   │       ├── PieChartComponent.kt
│   │       ├── BarChartComponent.kt
│   │       ├── LineChartComponent.kt
│   │       ├── MonthlyComparisonComponent.kt
│   │       ├── TopExpensesComponent.kt
│   │       ├── DailyAverageComponent.kt
│   │       └── PatternInsightComponent.kt
│   │
│   ├── ai_chat/
│   │   ├── AIChatScreen.kt
│   │   ├── AIChatViewModel.kt
│   │   ├── AIChatUiState.kt
│   │   └── components/
│   │       ├── ChatBubbleComponent.kt
│   │       ├── ChatInputBarComponent.kt
│   │       └── SuggestedQuestionsComponent.kt
│   │
│   ├── goals/
│   │   ├── GoalsScreen.kt
│   │   ├── GoalsViewModel.kt
│   │   ├── GoalsUiState.kt
│   │   └── components/
│   │       ├── GoalCardComponent.kt
│   │       └── AddGoalBottomSheet.kt
│   │
│   ├── profile/                            # [BARU] Preferences / profil lengkap
│   │   ├── ProfileScreen.kt
│   │   ├── ProfileViewModel.kt
│   │   ├── ProfileUiState.kt
│   │   └── components/
│   │       ├── ProfileFieldComponent.kt    # Reusable labeled input field
│   │       └── SheetConnectionStatusComponent.kt
│   │
│   └── settings/
│       ├── SettingsScreen.kt
│       ├── SettingsViewModel.kt
│       └── components/
│           ├── NotificationSettingsComponent.kt
│           └── ExportShareComponent.kt
│
└── ui/
    ├── theme/
    │   ├── Theme.kt
    │   ├── Color.kt                         # Semua token warna (lihat Design System)
    │   └── Type.kt
    ├── navigation/
    │   ├── AppNavGraph.kt
    │   ├── AppRoute.kt
    │   └── BottomNavBarComponent.kt
    └── components/
        ├── PrimaryButtonComponent.kt        # Background: Accent, text: OnAccent
        ├── SecondaryButtonComponent.kt      # Background: Secondary, text: OnSecondary
        ├── LoadingOverlayComponent.kt
        ├── EmptyStateComponent.kt
        ├── ErrorStateComponent.kt
        └── CategoryChipComponent.kt         # Background: SecondaryContainer
```

---

## 7. DATA MODELS

### Transaction.kt

```kotlin
data class Transaction(
    val id: String,                    // UUID
    val amount: Long,                  // Dalam Rupiah (integer, hindari float)
    val type: TransactionType,         // INCOME | EXPENSE
    val category: String,
    val note: String,
    val date: LocalDate,
    val timeMillis: Long,              // Timestamp exact untuk sorting
    val source: TransactionSource,     // MANUAL | SCAN | FREE_TEXT
    val isSynced: Boolean,
    val rawSheetsRowIndex: Int? = null // Untuk update/delete di Sheets
)

enum class TransactionType { INCOME, EXPENSE }
enum class TransactionSource { MANUAL, SCAN, FREE_TEXT }
```

### SheetsConfig.kt

```kotlin
data class SheetsConfig(
    val spreadsheetId: String,        // Extracted dari URL Sheets
    val spreadsheetUrl: String,
    val isConnected: Boolean,
    val lastSyncAt: Long?
)
```

### UserProfile.kt

```kotlin
/**
 * Profil lengkap user — disimpan di DataStore (lokal).
 * Tidak pernah dikirim ke server manapun.
 * [isProfileComplete] dipakai Dashboard untuk tampilkan/sembunyikan banner.
 */
data class UserProfile(
    val name: String,                       // Wajib — diisi saat onboarding
    val occupation: String,                 // Pekerjaan (Karyawan, Freelancer, dll)
    val monthlyIncomeRange: IncomeRange,    // Range penghasilan bulanan
    val monthlyBudget: Long?,               // Anggaran bulanan (opsional, dalam Rupiah)
    val sheetsUrl: String,                  // Link Google Spreadsheet
    val openRouterApiKey: String,           // API key OpenRouter
    val isProfileComplete: Boolean          // true jika occupation + sheetsUrl sudah diisi
)

enum class IncomeRange(val label: String, val rangeDescription: String) {
    BELOW_3M("Di bawah 3 juta", "< Rp 3.000.000"),
    RANGE_3M_5M("3 - 5 juta", "Rp 3.000.000 - 5.000.000"),
    RANGE_5M_10M("5 - 10 juta", "Rp 5.000.000 - 10.000.000"),
    RANGE_10M_20M("10 - 20 juta", "Rp 10.000.000 - 20.000.000"),
    ABOVE_20M("Di atas 20 juta", "> Rp 20.000.000"),
    PREFER_NOT_TO_SAY("Tidak ingin membagikan", "")
}
```

### MonthlyInsight.kt

```kotlin
data class MonthlyInsight(
    val month: YearMonth,
    val totalIncome: Long,
    val totalExpense: Long,
    val netBalance: Long,
    val topCategories: List<CategorySummary>,
    val dailyAverage: Long,
    val projectedMonthEnd: Long,
    val comparedToPrevMonth: Float    // Persentase naik/turun
)
```

### AnomalyAlert.kt

```kotlin
data class AnomalyAlert(
    val category: String,
    val currentMonthAmount: Long,
    val averageAmount: Long,
    val multiplier: Float,            // 3x lipat dari biasanya
    val message: String               // Pesan AI yang sudah di-generate
)
```

### SavingsGoal.kt

```kotlin
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val deadline: LocalDate?,
    val dailySavingsNeeded: Long,     // Dihitung otomatis
    val weeklySavingsNeeded: Long     // Dihitung otomatis
)
```

---

## 8. FEATURE SPECIFICATIONS

### F-00: SPLASH SCREEN

**File:** `feature/splash/SplashScreen.kt`  
**Durasi:** 1.5 detik (tidak dapat dilewati)

**Tampilan:**

- Background: `Background (#FCFBF4)`
- Logo app di tengah layar (SVG vector drawable), warna `Secondary`
- Nama app di bawah logo, `headlineMedium`, warna `OnBackground`
- Tidak ada teks lain, tidak ada loading indicator

**Logic setelah 1.5 detik:**

```
cek DataStore → userName kosong?
  ├─ Ya  → navigate ke OnboardingCarouselScreen (popUpTo Splash, inclusive)
  └─ Tidak → navigate ke Dashboard (popUpTo Splash, inclusive)
```

---

### F-01: ONBOARDING

**Terdiri dari 3 tahap berurutan, tidak bisa dilewati:**

#### Tahap 1 — Carousel Pengenalan Fitur

**File:** `feature/onboarding/OnboardingCarouselScreen.kt`

Carousel 3 slide, swipe horizontal atau tap tombol "Berikutnya".  
Dot indicator di bawah menunjukkan posisi slide aktif (warna aktif: `Secondary`, tidak aktif: `BackgroundVariant`).

| Slide | Judul                    | Deskripsi                                                                               | Ikon (Lucide) |
| ----- | ------------------------ | --------------------------------------------------------------------------------------- | ------------- |
| 1     | Catat Setiap Transaksi   | Catat pemasukan dan pengeluaran dengan cepat, manual atau lewat scan struk              | `PenLine`     |
| 2     | Scan Struk dan QRIS      | Foto struk belanja atau screenshot QRIS, AI kami langsung mengenali nominalnya          | `ScanLine`    |
| 3     | Analitik Keuangan Cerdas | Lihat grafik, deteksi pengeluaran tidak wajar, dan tanya langsung ke AI soal keuanganmu | `BarChart3`   |

**Layout setiap slide:**

- Background: `Background`
- Ikon besar (64.dp) di tengah atas, warna `Secondary`
- Judul: `headlineMedium`, warna `OnBackground`
- Deskripsi: `bodyMedium`, warna `TextSecondary`, max 2 baris, center-aligned
- Tombol "Berikutnya" (slide 1–2): warna `Secondary`, full width
- Tombol "Mulai" (slide 3): warna `Accent`, full width — navigasi ke Tahap 2

---

#### Tahap 2 — Input Nama User

**File:** `feature/onboarding/OnboardingNameScreen.kt`

**Tampilan:**

- Header singkat: "Siapa nama Anda?" — `headlineMedium`
- Subjudul: "Nama Anda hanya tersimpan di perangkat ini" — `bodySmall`, `TextSecondary`
- `OutlinedTextField`: label "Nama lengkap", ikon `UserRound` di trailing
- Validasi: tidak boleh kosong, minimal 2 karakter
- Tombol "Simpan dan Mulai" — `Accent`, disabled jika validasi gagal

**Behavior saat tombol ditekan:**

1. Simpan nama ke DataStore (`USER_NAME`)
2. Set `IS_ONBOARDING_DONE = true`
3. Navigate ke Dashboard (`popUpTo OnboardingCarousel, inclusive = true`)

**Penting:**

- Nama **tidak pernah** dikirim ke Sheets, OpenRouter, atau server manapun
- Nama hanya dipakai untuk greeting di Dashboard dan export ringkasan

---

### F-02: DASHBOARD

**Komponen layar dari atas ke bawah:**

**1. Header / TopAppBar**

- Background: `Secondary`
- Kiri: Greeting dua baris
  - Baris 1: "Halo, {nama}" — `headlineSmall`, `OnSecondary`, Bold
  - Baris 2: Tanggal hari ini — `bodySmall`, `OnSecondary` dengan opacity 80%
- Kanan: Ikon notifikasi `Bell` (warna `OnSecondary`), tap → daftar notifikasi

**2. ProfileIncompleteBanner** _(tampil kondisional)_

- Tampil **hanya jika** `isProfileComplete = false` (occupation atau sheetsUrl belum diisi)
- Background: `SurfaceError (#FDF0ED)`, border kiri 3.dp warna `Accent`
- Ikon `CircleAlert` warna `Accent` di kiri
- Teks: "Lengkapi profil untuk pengalaman terbaik"
- Teks kecil di bawah: "Pekerjaan dan koneksi Sheets belum diisi"
- Chevron `ChevronRight` di kanan → tap seluruh banner → navigate ke ProfileScreen
- Banner **hilang otomatis** saat `isProfileComplete` berubah jadi `true`
- Tidak ada tombol dismiss manual — user harus melengkapi profil

**3. BalanceCard**

- Background: `Secondary`
- Label: "Saldo Bulan Ini" — `labelSmall`, `OnSecondary` opacity 80%
- Nominal: format Rupiah — `headlineLarge`, `OnSecondary`, Bold
- Sub-row: ikon `TrendingUp` + total income | ikon `TrendingDown` + total expense

**4. QuickStats** — dua chip sejajar

- Chip kiri: ikon `TrendingUp` + nominal income — background `SecondaryContainer`
- Chip kanan: ikon `TrendingDown` + nominal expense — background `SurfaceError`, teks `Accent`

**5. AnomalyBanner** _(tampil kondisional, dismissible)_

- Tampil jika `DetectAnomalyUseCase` menemukan anomali
- Background `SurfaceError`, ikon `AlertCircle` warna `Accent`
- Teks pesan AI yang kontekstual
- Ikon `X` di kanan untuk dismiss (dalam satu sesi, tidak persist)

**6. Transaksi Terbaru**

- Label section: "Transaksi Terbaru" — `labelLarge`, ikon `RefreshCw` kecil di kanan (trigger sync manual)
- Maks 5 item, setiap item: ikon kategori + nama catatan + nominal (merah jika expense, hijau jika income) + tanggal
- "Lihat semua" → TransactionListScreen

**7. FAB (Floating Action Button)**

- Warna: `Accent`, ikon: `Plus`
- Tap → expand menjadi 3 mini-FAB ke atas:
  - `PenLine` + label "Manual"
  - `ScanLine` + label "Scan Struk"
  - `MessageSquare` + label "Teks Bebas"

---

### F-03: TAMBAH TRANSAKSI — MANUAL

**File:** `feature/transaction/add/AddTransactionScreen.kt`

**Layout dari atas ke bawah:**

- `TopAppBar` background `Secondary`, judul "Transaksi Baru", ikon `ChevronLeft` warna `OnSecondary`
- Body background `Background`

**Input fields:**

| Field    | Komponen                          | Detail                                                                                                                         |
| -------- | --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| Nominal  | `OutlinedTextField` numerik besar | Font `headlineLarge`, center-aligned, prefix "Rp"                                                                              |
| Tipe     | Toggle dua pilihan                | "Pemasukan" (background `SecondaryContainer`) / "Pengeluaran" (background `SurfaceError`, teks `Accent`)                       |
| Kategori | `LazyRow` chip grid               | Background chip: `SecondaryContainer`, teks: `OnSecondaryContainer`; chip terpilih: background `Secondary`, teks `OnSecondary` |
| Catatan  | `OutlinedTextField`               | Opsional, placeholder "Tambahkan catatan...", ikon `PenLine`                                                                   |
| Tanggal  | `OutlinedTextField` non-editable  | Default hari ini, ikon `CalendarDays`, tap → `DatePickerDialog`                                                                |

**Kategori default:**

- Pengeluaran: Makan, Transport, Belanja, Hiburan, Kesehatan, Tagihan, Lainnya
- Pemasukan: Gaji, Freelance, Bisnis, Investasi, Hadiah, Lainnya
- Kategori aktif berubah otomatis saat toggle Tipe diubah

**Tombol Simpan:**

- Background `Accent`, teks `OnAccent`, full width, ikon `CircleCheck` di kiri
- Disabled (background `BackgroundVariant`) jika nominal kosong atau 0

**Behavior:**

- Tap Simpan → `AddTransactionUseCase` → simpan ke Room queue → trigger sync
- Jika ada internet: sync langsung, snackbar "Transaksi tersimpan" → pop back
- Jika offline: snackbar "Tersimpan. Akan disinkronkan saat online" → pop back

---

### F-04: TAMBAH TRANSAKSI — SCAN STRUK / QRIS

**Alur detail:**

```
User tap "Scan"
    ↓
ScanScreen: pilih Kamera atau Galeri
    ↓
Image diproses ML Kit OCR → extracted text
    ↓
Text dikirim ke OpenRouter (ParseReceiptUseCase)
    ↓
AI return JSON: { amount, type, merchant, category, date, note }
    ↓
ScanResultScreen: tampilkan hasil, user bisa edit sebelum simpan
    ↓
Simpan via AddTransactionUseCase
```

**ScanScreen layout:**

- Background hitam (kamera full screen) atau `Background` (setelah gambar dipilih)
- `TopAppBar` background `Secondary`, judul "Scan Struk", ikon kembali `ChevronLeft`
- Dua tombol besar di tengah (jika belum ada gambar):
  - `Camera` + "Ambil Foto" — background `Secondary`
  - `Image` + "Pilih dari Galeri" — background `BackgroundVariant`, teks `OnBackground`
- Setelah gambar dipilih: preview gambar + tombol `ScanLine` + "Proses Gambar" background `Accent`
- Loading state: `CircularProgressIndicator` warna `Secondary` + teks "AI sedang membaca struk..."

**ScanResultScreen layout:**

- `TopAppBar` background `Secondary`, judul "Konfirmasi Hasil"
- Card background `BackgroundVariant` berisi hasil parsing (bisa diedit):
  - Nominal — `OutlinedTextField`, prefill dari AI
  - Tipe — toggle Pemasukan / Pengeluaran, prefill dari AI
  - Kategori — chip row, chip terpilih sesuai hasil AI
  - Catatan — `OutlinedTextField`, prefill merchant dari AI
  - Tanggal — prefill dari AI atau hari ini
- Banner kuning tipis jika ada field yang tidak terdeteksi: ikon `AlertCircle` + "Beberapa field tidak terdeteksi, silakan lengkapi"
- Tombol "Simpan" background `Accent`, full width

**AI Prompt untuk parsing struk (di AIRepositoryImpl):**

```
Kamu adalah parser struk belanja. Ekstrak informasi dari teks OCR berikut.
Return HANYA JSON valid (tanpa backtick) dengan format:
{
  "amount": <integer dalam Rupiah, tanpa titik/koma>,
  "type": "EXPENSE" atau "INCOME",
  "merchant": "<nama toko/merchant>",
  "category": "<satu dari: Makan|Transport|Belanja|Hiburan|Kesehatan|Tagihan|Gaji|Freelance|Lainnya>",
  "date": "<YYYY-MM-DD atau null jika tidak ada>",
  "note": "<nama item atau deskripsi singkat>"
}
Teks OCR: [OCR_TEXT]
```

**Error handling:**

- OCR tidak menemukan teks → `ErrorStateComponent`: ikon `ScanLine`, pesan "Teks tidak terdeteksi", tombol "Input Manual" background `Secondary`
- AI return JSON tidak valid → fallback ke `AddTransactionScreen` dengan field kosong + snackbar "Gagal membaca otomatis, silakan isi manual"

---

### F-05: TAMBAH TRANSAKSI — TEKS BEBAS

**File:** `feature/transaction/freetext/FreeTextScreen.kt`

**Layout:**

- `TopAppBar` background `Secondary`, judul "Input Teks", ikon `ChevronLeft`
- Area input besar (min 3 baris): placeholder "Contoh: tadi beli kopi 35rb, atau terima gaji 5 juta"
- Ikon `MessageSquare` besar berwarna `SecondaryContainer` sebagai ilustrasi di atas input
- Tombol "Proses" background `Accent`, ikon `Bot`, disabled jika input kosong
- Loading state: `CircularProgressIndicator` + "AI sedang memproses..."

**Konfirmasi hasil:**

- Tampil sebagai `BottomSheet` (bukan halaman baru)
- Isi BottomSheet sama dengan `ScanResultScreen` — semua field bisa diedit
- Tombol "Simpan" background `Accent`, tombol "Edit Lagi" background `BackgroundVariant`

**AI Prompt untuk free text:**

```
Parse transaksi keuangan dari teks berikut. Return HANYA JSON valid:
{
  "amount": <integer Rupiah>,
  "type": "EXPENSE" atau "INCOME",
  "category": "<kategori>",
  "date": "<YYYY-MM-DD, gunakan hari ini jika tidak disebutkan>",
  "note": "<ringkasan singkat>"
}
Hari ini: [TODAY_DATE]. Teks: [USER_TEXT]
```

**Error handling:**

- AI gagal parse (nominal tidak ditemukan) → snackbar "Tidak dapat memahami input, coba lebih spesifik" — input tetap ada, user bisa edit dan coba lagi

---

### F-06: GOOGLE SHEETS INTEGRATION

**Struktur Spreadsheet:**

```
Spreadsheet
├── Ringkasan              ← Summary tab (rumus otomatis)
├── Jan 2025               ← Tab per bulan (dibuat otomatis)
├── Feb 2025
└── ... dst
```

**Header per tab bulan (row 1):**

```
A: ID | B: Tanggal | C: Tipe | D: Kategori | E: Nominal | F: Catatan | G: Sumber | H: Timestamp
```

**Tab Ringkasan — otomatis dihitung:**

- Total Pemasukan bulan ini
- Total Pengeluaran bulan ini
- Saldo Bersih
- Breakdown per kategori (SUMIF)

**Auto-create tab bulan:**

- `CreateMonthlyTabUseCase` dipanggil saat pertama transaksi di bulan baru
- Cek dulu apakah tab sudah ada via Sheets API `spreadsheets.get`

**Sync Logic di `SyncWorker`:**

1. Query Room: semua transaksi dengan `isSynced = false`
2. Append ke tab bulan yang sesuai via `spreadsheets.values.append`
3. Update `isSynced = true` di Room
4. Trigger update tab Ringkasan

---

### F-07: ANALYTICS

**Screen layout:**

- `TopAppBar` background `Secondary`, judul "Analitik"
- Month selector di bawah TopAppBar: `LazyRow` chip per bulan, chip aktif background `Secondary` teks `OnSecondary`, tidak aktif background `BackgroundVariant`
- Konten: `LazyColumn` dengan padding 16.dp, background `Background`
- Setiap section dibungkus `Card` background `BackgroundVariant`, corner radius 12.dp, elevation 0.dp

**7a. Ringkasan Bulanan**

- Tiga metric dalam satu row: Pemasukan (`IncomeGreen`), Pengeluaran (`Accent`), Saldo (hijau jika positif / `Accent` jika negatif)
- Baris perbandingan: teks "vs bulan lalu" + persentase naik (hijau + `TrendingUp`) / turun (`Accent` + `TrendingDown`)

**7b. Pengeluaran per Kategori**

- Vico `PieChart`: slice warna dari palet `Secondary` dengan variasi opacity (30%–100%)
- Legend list di bawah: setiap item berupa row — dot warna + nama kategori + nominal + persentase
- Teks total di tengah pie: "Total" + nominal, `headlineSmall`

**7c. Tren Keuangan (6 Bulan)**

- Vico `LineChart` dual-line: income `IncomeGreen`, expense `Accent`
- X-axis: label bulan singkat (Jan, Feb, ...) warna `TextSecondary`
- Y-axis: format nominal disingkat (1.2jt, 500rb) warna `TextSecondary`
- Legenda: dua chip kecil "Pemasukan" dan "Pengeluaran" dengan dot warna

**7d. Top 5 Pengeluaran Terbesar**

- Setiap item: nomor urut + ikon kategori (Lucide) + nama catatan + nominal `Accent` Bold + tanggal `TextSecondary`
- Background item bergantian: `Background` dan `BackgroundVariant`

**7e. Rata-rata & Proyeksi**

- Dua metric: "Rata-rata/hari" dan "Proyeksi akhir bulan"
- Progress bar `LinearProgressIndicator`: warna `Secondary`, track `BackgroundVariant`
- Label: "Hari ke-X dari Y hari" + proyeksi nominal

**7f. Pola Transaksi**

- Bar chart 7 kolom (Sen–Min): warna bar `Secondary`, bar tertinggi warna `Accent`
- Caption di bawah: "Paling aktif: [hari]"

---

### F-08: AI CHAT

**Konsep:** User bisa "ngobrol" dengan data keuangan mereka sendiri.

**Layout:**

- `TopAppBar` background `Secondary`, judul "Asisten AI", subjudul kecil "Didukung Nemotron" warna `OnSecondary` opacity 70%
- Background layar: `Background`
- `LazyColumn` (chat history) — grows bottom-up
- Input bar di bawah: `TextField` background `BackgroundVariant`, tombol kirim ikon `Send` background `Accent`

**Bubble chat:**

- Pesan user: alignment kanan, background `Secondary`, teks `OnSecondary`, corner radius 16.dp (kiri bawah lebih kecil)
- Pesan AI: alignment kiri, background `BackgroundVariant`, teks `OnBackground`, corner radius 16.dp (kanan bawah lebih kecil)
- AI loading: bubble kiri dengan 3 titik animasi (`LoadingDotsComponent`) background `BackgroundVariant`

**Suggested Questions** — chip row di atas input bar, tampil hanya saat chat kosong:

- Background chip: `SecondaryContainer`, teks `OnSecondaryContainer`
- Chip disembunyikan setelah user mengirim pesan pertama

**Pertanyaan saran:**

- "Berapa total pengeluaran minggu ini?"
- "Kategori apa yang paling besar bulan ini?"
- "Bagaimana tren keuanganku 3 bulan terakhir?"
- "Di mana aku bisa hemat lebih banyak?"

**AI System Prompt:**

```
Kamu adalah asisten keuangan personal yang membantu dan profesional.
Nama pengguna: [USER_NAME]. Pekerjaan: [OCCUPATION].
Data transaksi bulan ini: [TRANSACTION_DATA_JSON]

Jawab pertanyaan pengguna berdasarkan data di atas secara ringkas dan
informatif dalam Bahasa Indonesia. Jika relevan, berikan saran praktis
yang spesifik. Jangan pernah mengarang data yang tidak ada dalam konteks.
Format nominal selalu dalam Rupiah (contoh: Rp 1.500.000).
```

**Catatan implementasi:**

- Kirim `USER_NAME` dan `OCCUPATION` dari DataStore untuk personalisasi jawaban
- Batasi data transaksi yang dikirim: maks 100 transaksi terakhir (cegah token overflow)
- `temperature: 0.7` untuk chat (lebih natural dari parsing)

---

### F-09: DETEKSI ANOMALI

**Logic:**

- Dijalankan oleh `DetectAnomalyUseCase` setiap kali Dashboard dibuka
- Hitung rata-rata pengeluaran per kategori dari 3 bulan terakhir
- Jika bulan berjalan melebihi 2x rata-rata di kategori tertentu → anomali
- Generate pesan AI yang kontekstual (bukan template)

**Contoh output:**

> _"Pengeluaran Makananmu bulan ini sudah Rp 1,2 juta — 3x lebih tinggi dari rata-rata Rp 400rb. Mau kubantu analisis lebih lanjut?"_

---

### F-10: TARGET TABUNGAN

**Data storage:** Room DB lokal — tidak disinkronkan ke Sheets.

**GoalsScreen layout:**

- `TopAppBar` background `Secondary`, judul "Target Tabungan"
- `LazyColumn` list `GoalCard`, background `Background`
- FAB ikon `Plus` background `Accent` → `AddGoalBottomSheet`
- Empty state (tidak ada goal): ikon `Target` 48.dp warna `SecondaryContainer`, teks "Belum ada target tabungan", tombol "Buat Target" background `Secondary`

**GoalCard layout (`Card` background `BackgroundVariant`, radius 12.dp):**

- Row 1: nama goal (`bodyLarge` Bold) + persentase progress (`labelSmall` `TextSecondary`)
- Row 2: `LinearProgressIndicator` — progress warna `Secondary`, track `BackgroundVariant`; jika sudah 100% ganti warna `IncomeGreen`
- Row 3: "Rp [sisa] lagi" (`bodySmall` `TextSecondary`) + deadline jika ada
- Row 4: "Hemat Rp [X]/hari untuk tepat waktu" (`labelSmall` `Accent`)
- Long press → opsi Edit / Hapus (ikon `Pencil` dan `Trash2`)

**AddGoalBottomSheet:**

- Handle bar di tengah atas
- Field: Nama target (`OutlinedTextField`), Nominal target (numerik), Sudah ditabung (numerik, default 0), Deadline (opsional, `DatePicker`)
- Saat nominal target dan deadline diisi → tampilkan preview kalkulasi: "Harus menabung Rp X/hari"
- Tombol "Buat Target" background `Accent`, full width

**Auto-kalkulasi di `CalculateSavingsRateUseCase`:**

```kotlin
val remainingAmount = targetAmount - currentAmount
val daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), deadline)
val dailySavingsNeeded = if (daysUntilDeadline > 0) remainingAmount / daysUntilDeadline else 0
val weeklySavingsNeeded = dailySavingsNeeded * 7
```

---

### F-11: NOTIFIKASI & REMINDER

**Jenis notifikasi:**

| ID   | Jenis                | Waktu                   | Teks Notifikasi                                           |
| ---- | -------------------- | ----------------------- | --------------------------------------------------------- |
| N-01 | Reminder harian      | 20:00 tiap hari         | "Sudah catat pengeluaran hari ini?"                       |
| N-02 | Anomali terdeteksi   | Real-time saat buka app | "Pengeluaran [Kategori] sudah 2x dari biasanya bulan ini" |
| N-03 | Sync berhasil        | Silent                  | Tidak tampil ke user                                      |
| N-04 | Goal hampir tercapai | Real-time               | "Target [Nama Goal] sudah 90 persen tercapai"             |

**Implementasi:**

- N-01: `WorkManager` `PeriodicWorkRequest` interval 24 jam, `setInitialDelay` agar pertama kali tepat jam 20:00
- N-02: Dipanggil dari `DashboardViewModel` setelah `DetectAnomalyUseCase` selesai
- N-04: Dipanggil dari `GoalsViewModel` setiap kali ada update progress goal

**Permission yang dibutuhkan (`AndroidManifest.xml`):**

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<!-- Runtime permission request wajib untuk Android 13+ (API 33+) -->
```

**Notification Channel setup (di `Application.onCreate`):**

```kotlin
val channel = NotificationChannel(
    "CuOne!_main",
    "Cuan Notifikasi",
    NotificationManager.IMPORTANCE_DEFAULT
).apply { description = "Reminder dan alert keuangan" }
```

---

### F-12: SHARE RINGKASAN BULANAN

**Akses:** Settings → "Bagikan Ringkasan"

**Desain kartu yang di-generate (Compose Canvas → Bitmap):**

- Ukuran: 1080 × 1350 px (rasio 4:5, cocok untuk Instagram/WhatsApp)
- Background: `Background (#FCFBF4)`
- Header band 200px: background `Secondary`, teks nama user `headlineMedium` `OnSecondary`, sub-teks bulan/tahun `bodySmall` `OnSecondary`
- Area konten:
  - Dua kotak berdampingan: Pemasukan (teks `IncomeGreen`, ikon `TrendingUp`) dan Pengeluaran (teks `Accent`, ikon `TrendingDown`)
  - Divider tipis warna `BackgroundVariant`
  - "Top 3 Pengeluaran" — list 3 item: nama kategori + bar horizontal proporsional warna `Secondary`
- Footer: teks kecil "Dibuat dengan CuOne" warna `TextSecondary`

**Tombol di Settings:**

- "Bagikan Ringkasan" — background `Secondary`, ikon `Share2` — generate bitmap + buka ShareSheet
- "Buka di Spreadsheet" — background `BackgroundVariant`, ikon `ExternalLink` — `Intent(ACTION_VIEW, Uri.parse(sheetsUrl))`

**Implementasi generate bitmap:**

```kotlin
// Render Composable ke bitmap menggunakan ComposeView + drawToBitmap()
// Library: androidx.compose.ui:ui-graphics (sudah included di Compose BOM)
val bitmap = ComposeView(context).apply {
    setContent { SummaryCardContent(insight) }
}.drawToBitmap()
```

---

### F-13: PROFIL & PREFERENSI

**File:** `feature/profile/ProfileScreen.kt`  
**Akses:** BottomNav (tab Settings) → "Profil Saya" atau tap banner di Dashboard

**Field yang tersedia:**

| Field              | Tipe Input                                | Ikon Lucide | Wajib    | Keterangan                              |
| ------------------ | ----------------------------------------- | ----------- | -------- | --------------------------------------- |
| Nama               | `OutlinedTextField`                       | `UserRound` | Ya       | Prefill dari DataStore, bisa diubah     |
| Pekerjaan          | `OutlinedTextField` + dropdown saran      | `Briefcase` | Ya       | Untuk konteks saran AI                  |
| Range Penghasilan  | Dropdown `IncomeRange`                    | `Banknote`  | Opsional | Untuk proyeksi & saran hemat            |
| Anggaran Bulanan   | `OutlinedTextField` numerik               | `Wallet`    | Opsional | Limit pengeluaran, dipakai di analytics |
| Link Google Sheets | `OutlinedTextField` + ikon `ExternalLink` | `Sheet`     | Ya       | Paste URL spreadsheet                   |
| API Key OpenRouter | `OutlinedTextField` password style        | `Bot`       | Ya       | Tersimpan encrypted di DataStore        |

**Saran dropdown Pekerjaan** (bisa ketik manual juga):
Karyawan Swasta, PNS / ASN, Wirausaha, Freelancer, Mahasiswa, Profesional (Dokter/Pengacara/dll), Ibu Rumah Tangga, Lainnya

**Status koneksi Sheets** (di bawah field Link Sheets):

- Jika terhubung: ikon `CircleCheck` warna `IncomeGreen` + "Terhubung" + tanggal sync terakhir
- Jika belum: ikon `AlertCircle` warna `Accent` + "Belum terhubung"
- Tombol "Uji Koneksi" → trigger `SetupSheetUseCase` → tampil hasil

**Behavior tombol Simpan:**

1. Validasi: nama tidak kosong, pekerjaan tidak kosong, sheetsUrl valid (bisa di-parse ID-nya), apiKey tidak kosong
2. Simpan semua field ke DataStore
3. Set `IS_PROFILE_COMPLETE = true` jika semua field wajib terisi
4. Emit UiEvent.Success → snackbar "Profil berhasil disimpan"
5. Dashboard secara reaktif observe `isProfileComplete` → banner hilang otomatis (Flow-based)

**Layout:**

- `Scaffold` dengan `TopAppBar` background `Secondary`, judul "Profil Saya", ikon kembali `ChevronLeft`
- `LazyColumn` dengan padding 16.dp
- Setiap field dikelompokkan dalam `Card` dengan background `BackgroundVariant`, corner radius 12.dp
- Grouping card:
  - Card 1: "Informasi Pribadi" — Nama, Pekerjaan, Range Penghasilan
  - Card 2: "Keuangan" — Anggaran Bulanan
  - Card 3: "Koneksi" — Link Sheets (+ status), API Key OpenRouter
- Tombol "Simpan" di paling bawah, full width, background `Accent`

---

## 9. API INTEGRATIONS

### 9a. OpenRouter API

**Base URL:** `https://openrouter.ai/api/v1`  
**Endpoint:** `POST /chat/completions`  
**Model:** `nvidia/llama-3.1-nemotron-ultra-253b-v1`  
**Auth:** Bearer token — user input API key mereka sendiri di Settings  
**API Key storage:** DataStore (encrypted), TIDAK di-hardcode

**Request template:**

```kotlin
data class OpenRouterRequest(
    val model: String = "nvidia/llama-3.1-nemotron-ultra-253b-v1",
    val messages: List<ChatMessage>,
    val maxTokens: Int = 1024,
    val temperature: Float = 0.3f   // Rendah untuk parsing, lebih tinggi untuk chat
)
```

**Handling response:**

- Parse `choices[0].message.content`
- Jika mode parsing: ekstrak JSON dengan regex/`substring` jika ada teks tambahan
- Jika mode chat: tampilkan langsung sebagai teks

### 9b. Google Sheets API

**Auth flow:** Google Sign-In → OAuth2 → access token  
**Scopes:** `https://www.googleapis.com/auth/spreadsheets`  
**Base URL:** `https://sheets.googleapis.com/v4`

**Endpoints yang digunakan:**

| Method | Endpoint                                   | Kegunaan                     |
| ------ | ------------------------------------------ | ---------------------------- |
| GET    | `/spreadsheets/{id}`                       | Cek keberadaan tab, metadata |
| POST   | `/spreadsheets/{id}/values/{range}:append` | Tambah baris transaksi       |
| GET    | `/spreadsheets/{id}/values/{range}`        | Baca transaksi               |
| POST   | `/spreadsheets/{id}:batchUpdate`           | Buat tab baru, format header |
| PUT    | `/spreadsheets/{id}/values/{range}`        | Update tab Ringkasan         |

**Extract Spreadsheet ID dari URL:**

```kotlin
fun extractSpreadsheetId(url: String): String? {
    val regex = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
    return regex.find(url)?.groupValues?.get(1)
}
```

---

## 10. NAVIGATION & UI FLOW

```
SplashScreen (1.5 detik)
  ↓ (cek DataStore: userName kosong?)
  ├─ Ya  → OnboardingCarouselScreen
  │         ↓ (slide 1 → 2 → 3 → tap "Mulai")
  │        OnboardingNameScreen
  │         ↓ (input nama + simpan)
  │        Dashboard
  └─ Tidak → Dashboard

Dashboard (BottomNav Tab 1)
  ├─ ProfileIncompleteBanner tap → ProfileScreen
  ├─ FAB expand:
  │   ├─ Manual     → AddTransactionScreen
  │   ├─ Scan       → ScanScreen → ScanResultScreen
  │   └─ Teks Bebas → FreeTextScreen → konfirmasi
  └─ "Lihat semua" → TransactionListScreen

Analytics (BottomNav Tab 2)
  └─ Scrollable page dengan semua chart

AI Chat (BottomNav Tab 3)

Goals (BottomNav Tab 4)
  └─ FAB → AddGoalBottomSheet

Settings (BottomNav Tab 5)
  ├─ "Profil Saya" → ProfileScreen
  │     └─ Isi Nama, Pekerjaan, Range Penghasilan, Anggaran, Sheets, API Key → Simpan
  ├─ Notifikasi
  └─ Share Ringkasan Bulanan
```

**Route constants (AppRoute.kt):**

```kotlin
sealed class AppRoute(val route: String) {
    object Splash              : AppRoute("splash")
    object OnboardingCarousel  : AppRoute("onboarding_carousel")
    object OnboardingName      : AppRoute("onboarding_name")
    object Dashboard           : AppRoute("dashboard")
    object AddTransaction      : AppRoute("add_transaction")
    object Scan                : AppRoute("scan")
    object ScanResult          : AppRoute("scan_result")
    object FreeText            : AppRoute("free_text")
    object TransactionList     : AppRoute("transaction_list")
    object Analytics           : AppRoute("analytics")
    object AIChat              : AppRoute("ai_chat")
    object Goals               : AppRoute("goals")
    object Profile             : AppRoute("profile")
    object Settings            : AppRoute("settings")
}
```

---

## 11. STATE MANAGEMENT

**Pattern:** Setiap ViewModel menggunakan `StateFlow<UiState>` + `SharedFlow<UiEvent>`

```kotlin
// Contoh pattern UiState
sealed class AddTransactionUiState {
    object Idle : AddTransactionUiState()
    object Loading : AddTransactionUiState()
    data class Success(val message: String) : AddTransactionUiState()
    data class Error(val message: String) : AddTransactionUiState()
}

// Contoh pattern di ViewModel
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.Loading
            addTransactionUseCase(transaction)
                .onSuccess { _uiState.value = AddTransactionUiState.Success("Transaksi tersimpan") }
                .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
        }
    }
}
```

**DataStore keys (AppDataStore.kt):**

```kotlin
object DataStoreKeys {
    // Onboarding
    val USER_NAME                = stringPreferencesKey("user_name")
    val IS_ONBOARDING_DONE       = booleanPreferencesKey("is_onboarding_done")

    // Profil lengkap
    val USER_OCCUPATION          = stringPreferencesKey("user_occupation")
    val USER_INCOME_RANGE        = stringPreferencesKey("user_income_range")   // IncomeRange.name
    val USER_MONTHLY_BUDGET      = longPreferencesKey("user_monthly_budget")   // 0 jika tidak diset
    val IS_PROFILE_COMPLETE      = booleanPreferencesKey("is_profile_complete")

    // Koneksi Sheets
    val SHEETS_URL               = stringPreferencesKey("sheets_url")
    val SHEETS_ID                = stringPreferencesKey("sheets_id")
    val IS_SHEETS_CONNECTED      = booleanPreferencesKey("is_sheets_connected")
    val LAST_SYNC_AT             = longPreferencesKey("last_sync_at")

    // AI
    val OPENROUTER_API_KEY       = stringPreferencesKey("openrouter_api_key")

    // Notifikasi & preferensi
    val DAILY_REMINDER_ENABLED   = booleanPreferencesKey("daily_reminder_enabled")
    val DAILY_REMINDER_HOUR      = intPreferencesKey("daily_reminder_hour")    // default 20
}
```

**Notifikasi — teks tanpa emoji, gunakan ikon sistem Android:**

| ID   | Jenis                | Waktu           | Teks Notifikasi                                           |
| ---- | -------------------- | --------------- | --------------------------------------------------------- |
| N-01 | Reminder harian      | 20:00 tiap hari | "Sudah catat pengeluaran hari ini?"                       |
| N-02 | Anomali terdeteksi   | Real-time       | "Pengeluaran [Kategori] sudah 2x dari biasanya bulan ini" |
| N-03 | Sync berhasil        | Silent          | — (tidak tampil ke user)                                  |
| N-04 | Goal hampir tercapai | Real-time       | "Target [Nama Goal] sudah 90 persen tercapai"             |

---

## 12. BUILD.GRADLE DEPENDENCIES

Tambahkan semua dependency berikut ke `app/build.gradle.kts`. Versi mengacu pada BOM atau versi stabil terakhir saat dokumen dibuat.

```kotlin
// app/build.gradle.kts
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}

dependencies {
    // --- Compose BOM ---
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Core AndroidX ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Hilt DI ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Room ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // --- WorkManager ---
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // --- Network ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // --- Google Sign-In & Sheets ---
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:2.4.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
    }

    // --- ML Kit OCR ---
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // --- Image picker / crop ---
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Charts ---
    implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")

    // --- Lucide Icons ---
    implementation("com.github.lucide-icons:lucide-android:0.446.0")

    // --- Animations ---
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
```

**`settings.gradle.kts` — tambahkan JitPack untuk Lucide:**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## 13. UI COMPONENTS CONTRACT

Spesifikasi reusable component di `ui/components/`. Semua komponen harus mengikuti kontrak ini.

### PrimaryButtonComponent

```kotlin
// Digunakan untuk: Simpan, Konfirmasi, Mulai, Proses
// Background: Accent | Teks: OnAccent | Ikon: opsional di sisi kiri
@Composable
fun PrimaryButtonComponent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,          // Lucide icon, ukuran 18.dp
    enabled: Boolean = true,            // Disabled: background BackgroundVariant
    isLoading: Boolean = false          // True: ganti teks dengan CircularProgressIndicator
)
```

### SecondaryButtonComponent

```kotlin
// Digunakan untuk: Batal, Kembali, Opsi alternatif
// Background: Secondary | Teks: OnSecondary
@Composable
fun SecondaryButtonComponent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
)
```

### EmptyStateComponent

```kotlin
// Tampil saat list kosong atau belum ada data
// Ikon besar (48.dp) warna SecondaryContainer, teks TextSecondary, tombol opsional
@Composable
fun EmptyStateComponent(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
)
```

### ErrorStateComponent

```kotlin
// Tampil saat terjadi error network atau parsing
// Ikon AlertCircle warna Accent, tombol "Coba Lagi" SecondaryButton
@Composable
fun ErrorStateComponent(
    message: String,
    onRetry: (() -> Unit)? = null
)
```

### LoadingOverlayComponent

```kotlin
// Full-screen semi-transparent overlay saat proses berlangsung
// CircularProgressIndicator warna Secondary di tengah layar
// Blokir semua interaksi user saat tampil
@Composable
fun LoadingOverlayComponent(
    isVisible: Boolean,
    message: String? = null             // Opsional teks di bawah spinner
)
```

### CategoryChipComponent

```kotlin
// Chip kategori di form transaksi
// Selected: background Secondary, teks OnSecondary
// Unselected: background SecondaryContainer, teks OnSecondaryContainer
@Composable
fun CategoryChipComponent(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
)
```

---

## 14. ANDROIDMANIFEST.XML — PERMISSIONS & KONFIGURASI

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Jaringan -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Kamera & Galeri -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <!-- Untuk Android < 13 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <!-- Notifikasi (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- WorkManager (sudah included, pastikan tidak duplikat) -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".CuOne"     <!-- Hilt Application class -->
        android:label="CuOne"
        android:icon="@mipmap/ic_launcher"
        android:theme="@style/Theme.CuOne"
        android:networkSecurityConfig="@xml/network_security_config">

        <!-- SplashScreen API -->
        <meta-data
            android:name="android.splashscreen.background_color"
            android:value="#FCFBF4" />

        <!-- Google Sign-In OAuth client ID -->
        <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />

        <activity android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

**`res/xml/network_security_config.xml`** — izinkan koneksi ke OpenRouter dan Google APIs:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">openrouter.ai</domain>
        <domain includeSubdomains="true">googleapis.com</domain>
        <domain includeSubdomains="true">sheets.googleapis.com</domain>
    </domain-config>
</network-security-config>
```

---

## 15. ERROR & EMPTY STATES

Setiap screen harus menangani 4 kondisi state secara konsisten.

### Pola State per Screen

```kotlin
sealed class ScreenUiState<out T> {
    object Loading : ScreenUiState<Nothing>()
    data class Success<T>(val data: T) : ScreenUiState<T>()
    data class Error(val message: String, val retryable: Boolean = true) : ScreenUiState<Nothing>()
    object Empty : ScreenUiState<Nothing>()
}
```

### Mapping State → UI

| State     | Komponen                                        | Warna dominan                   |
| --------- | ----------------------------------------------- | ------------------------------- |
| `Loading` | `LoadingOverlayComponent` atau skeleton shimmer | `BackgroundVariant` shimmer     |
| `Success` | Konten normal                                   | Sesuai design system            |
| `Error`   | `ErrorStateComponent`                           | `Accent` untuk ikon error       |
| `Empty`   | `EmptyStateComponent`                           | `SecondaryContainer` untuk ikon |

### Pesan Error Standar per Sumber

| Sumber Error               | Pesan yang Ditampilkan                                         |
| -------------------------- | -------------------------------------------------------------- |
| Tidak ada koneksi internet | "Tidak ada koneksi internet. Periksa jaringan Anda."           |
| Sheets API gagal (401)     | "Sesi Google berakhir. Silakan masuk ulang."                   |
| Sheets API gagal (403)     | "Tidak punya akses ke spreadsheet ini."                        |
| Sheets API gagal (5xx)     | "Layanan Google Sheets sedang bermasalah. Coba lagi nanti."    |
| OpenRouter API gagal (401) | "API Key tidak valid. Periksa di Profil."                      |
| OpenRouter API gagal (429) | "Batas permintaan AI tercapai. Coba lagi dalam beberapa saat." |
| OCR tidak menemukan teks   | "Teks tidak terdeteksi. Coba foto dengan cahaya lebih terang." |
| Parse JSON gagal           | "AI tidak dapat memproses gambar ini. Coba input manual."      |

### Shimmer Loading

Gunakan untuk list transaksi dan analytics saat data sedang di-fetch dari Sheets:

- Komponen: `ShimmerBox` — `Box` dengan animasi `infiniteTransition` dari `BackgroundVariant` ke `Background`
- Tampilkan maks 5 baris shimmer sebagai placeholder

_AGENTS.md v1.2 — C1 Kotlin Project_  
_Changelog v1.2: Fix bug AppRoute code block, expand F-03–F-12 dengan detail layout & warna, tambah Section 12 (build.gradle dependencies), Section 13 (UI Components Contract), Section 14 (AndroidManifest), Section 15 (Error & Empty States)._  
_Dibuat untuk digunakan bersama AI coding agent (Cursor, Claude Code, dll)_ Tambah Design System (warna 60/30/10, Lucide icons, tipografi), F-00 Splash Screen, revisi F-01 Onboarding (carousel 3 slide), revisi F-02 Dashboard (greeting + ProfileIncompleteBanner), tambah F-13 Profil & Preferensi, tambah UserProfile model + IncomeRange enum, update DataStore keys, update navigasi, hapus semua emoji dari UI.\*  
_Dibuat untuk digunakan bersama AI coding agent (Cursor, Claude Code, dll)_
