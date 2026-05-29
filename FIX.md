# CuOne Build Error Fixes — Panduan Perbaikan Lengkap

> Dokumen ini mencakup **semua error kompilasi** yang tersisa setelah fix Hilt version, Kotlin version, dan identifier hyphen.

---

## Ringkasan Error

| # | Kategori | Jumlah Error | File Terdampak |
|---|----------|:---:|---|
| 1 | [Typo di Theme.kt](#1-typo-di-themekt) | 2 | 1 |
| 2 | [Missing Import DataStore](#2-missing-import-datastore) | ~25 | 1 |
| 3 | [Unresolved Material Icons](#3-unresolved-material-icons) | ~80+ | 10 |
| 4 | [DateUtils API Error](#4-dateutils-api-error) | 1 | 1 |
| 5 | [ResultUtils Extension Conflict](#5-resultutils-extension-conflict) | 2 | 1 |
| 6 | [Type Mismatch & Missing Imports](#6-type-mismatch--missing-imports) | 4 | 3 |

---

## 1. Typo di Theme.kt

> [!CAUTION]
> Variabel salah tulis — kurang huruf pada kedua identifier.

### File: [Theme.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/ui/theme/Theme.kt)

#### Error 1 — Line 14: `CuOneolorScheme` (kurang huruf `C`)

```diff
- private val CuOneolorScheme = lightColorScheme(
+ private val CuOneColorScheme = lightColorScheme(
```

#### Error 2 — Line 38: Harus ikut berubah

```diff
-     val colorScheme = CuOneolorScheme
+     val colorScheme = CuOneColorScheme
```

#### Error 3 — Line 51: `CuOneypography` (kurang huruf `T`)

```diff
-         typography = CuOneypography,
+         typography = CuOneTypography,
```

> [!NOTE]
> Ini terjadi karena replacement tool sebelumnya memotong huruf saat menghapus hyphen dari `Cu-One`. `Cu-OneColorScheme` → harusnya `CuOneColorScheme` tapi jadi `CuOneolorScheme`.

---

## 2. Missing Import DataStore

### File: [AppDataStore.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/core/local/AppDataStore.kt)

#### Error A — Line 45: `intPreferencesKey` unresolved

**Root Cause:** Import `intPreferencesKey` tidak ada dalam daftar import.

```diff
  import androidx.datastore.preferences.core.booleanPreferencesKey
+ import androidx.datastore.preferences.core.intPreferencesKey
  import androidx.datastore.preferences.core.longPreferencesKey
  import androidx.datastore.preferences.core.stringPreferencesKey
```

#### Error B — Line 64, 68, 78, dst: `edit` unresolved

**Root Cause:** Extension function `edit` memerlukan import tambahan.

```diff
  import androidx.datastore.preferences.preferencesDataStore
+ import androidx.datastore.preferences.core.edit
  import dagger.hilt.android.qualifiers.ApplicationContext
```

---

## 3. Unresolved Material Icons

> [!IMPORTANT]
> Ini adalah error **terbanyak** (~80 error). Project saat ini **tidak memiliki dependency `material-icons-extended`** — hanya menggunakan `material3` yang menyertakan set ikon terbatas (Home, Search, Add, Close, Settings, ArrowBack, dll).

### Root Cause

Banyak file menggunakan ikon dari `Icons.Default.*` dan `Icons.AutoMirrored.Filled.*` yang hanya ada di `material-icons-extended`:

- `TrendingUp`, `TrendingDown`, `ChevronRight`, `Message`, `QrCodeScanner`
- `BarChart`, `ChatBubble`, `TrackChanges`, `Warning`, `Receipt`
- `CameraAlt`, `Image`, `Bell`, `Share2`, `ExternalLink`
- `Bot`, `Briefcase`, `Wallet`, `Banknote`, `Sheet`, `User`
- `Target`, `CalendarToday`, `Fastfood`, `HealthAndSafety`, `School`
- `MoreHoriz`, `Restaurant`, `DirectionsCar`, `ShoppingCart`, `Movie`
- `LocalHospital`, `LocalActivity`, `Error`, `Help`

### Solusi: Tambahkan dependency `material-icons-extended`

#### [libs.versions.toml](file:///c:/project-uta/cuan/gradle/libs.versions.toml) — Tambah library baru

```diff
  [libraries]
  ...
  androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
+ androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
  androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
```

#### [app/build.gradle.kts](file:///c:/project-uta/cuan/app/build.gradle.kts) — Tambah implementation

```diff
  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
+ implementation(libs.androidx.compose.material.icons.extended)
```

> [!WARNING]
> `material-icons-extended` cukup besar (~36 MB). Pastikan untuk mengaktifkan **R8/ProGuard** di release build agar unused icons di-strip. Di `build.gradle.kts` release:
> ```kotlin
> release {
>     isMinifyEnabled = true  // <-- ubah dari false ke true
> }
> ```

### File-file yang terdampak dan icon yang diperlukan

| File | Icons yang digunakan |
|------|---------------------|
| [DashboardScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/dashboard/DashboardScreen.kt) | `ChevronRight`, `Message`, `QrCodeScanner`, `TrendingDown`, `TrendingUp`, `Warning` |
| [AnalyticsScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/analytics/AnalyticsScreen.kt) | `TrendingDown`, `TrendingUp` |
| [BottomNavBarComponent.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/ui/navigation/BottomNavBarComponent.kt) | `ChatBubble`, `BarChart`, `TrackChanges` |
| [SettingsScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/settings/SettingsScreen.kt) | `Help`, `Bell`, `ChevronRight`, `ExternalLink`, `Share2` |
| [ProfileScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/profile/ProfileScreen.kt) | `Banknote`, `Bot`, `Briefcase`, `Error`, `ExternalLink`, `Sheet`, `User`, `Wallet` |
| [AddTransactionScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/add/AddTransactionScreen.kt) | `CalendarToday`, `Fastfood`, `HealthAndSafety`, `LocalActivity`, `MoreHoriz`, `Receipt`, `School`, `TrendingDown`, `TrendingUp` |
| [FreeTextScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/freetext/FreeTextScreen.kt) | `Bot`, `Message`, `TrendingDown`, `TrendingUp`, `Restaurant`, `DirectionsCar`, `ShoppingCart`, `Movie`, `LocalHospital`, `Receipt`, `MoreHoriz` |
| [ScanResultScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/scan/ScanResultScreen.kt) | `TrendingDown`, `TrendingUp`, `Restaurant`, `DirectionsCar`, `ShoppingCart`, `Movie`, `LocalHospital`, `Receipt`, `MoreHoriz` |
| [ScanScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/scan/ScanScreen.kt) | `CameraAlt`, `Image` |
| [TransactionListScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/list/TransactionListScreen.kt) | `TrendingDown`, `TrendingUp`, `Receipt` |
| [States.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/ui/components/States.kt) | `Warning` |
| [AIChatScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/ai_chat/AIChatScreen.kt) | — (tidak ada icon issue) |

> [!NOTE]
> **Setelah menambahkan dependency**, beberapa ikon yang tidak ada di Material Icons standard tetap perlu diganti nama. Berikut mapping icon yang **TIDAK ADA** di `material-icons-extended` (karena ini bukan Material Icons, melainkan Lucide Icons sesuai AGENTS.md):

| Icon di kode | ❌ Tidak ada di Material Icons | ✅ Ganti dengan |
|---|---|---|
| `Bot` | Tidak ada | `Icons.Default.SmartToy` |
| `Banknote` | Tidak ada | `Icons.Default.AttachMoney` |
| `Briefcase` | Tidak ada | `Icons.Default.Work` |
| `Sheet` | Tidak ada | `Icons.Default.TableChart` |
| `User` | Tidak ada | `Icons.Default.Person` |
| `Wallet` | Tidak ada | `Icons.Default.AccountBalanceWallet` |
| `Share2` | Tidak ada | `Icons.Default.Share` |
| `Bell` | Tidak ada | `Icons.Default.Notifications` |
| `ExternalLink` | Tidak ada | `Icons.Default.OpenInNew` |
| `Help` | Tidak ada | `Icons.Default.HelpOutline` |
| `Target` | Tidak ada | `Icons.Default.TrackChanges` |
| `CalendarToday` | **Ada** ✅ | — (keep as is) |
| `Image` | **Ada** ✅ (di `Icons.Default.Image`) | Cek apakah import benar |

### Khusus: BottomNavBarComponent.kt — Icon Fixes

```diff
- import androidx.compose.material.icons.automirrored.filled.ChatBubble
- import androidx.compose.material.icons.filled.BarChart
- import androidx.compose.material.icons.filled.TrackChanges
+ import androidx.compose.material.icons.automirrored.filled.Chat
+ import androidx.compose.material.icons.filled.BarChart
+ import androidx.compose.material.icons.filled.TrackChanges
```

Atau alternatif untuk `ChatBubble`:
```diff
- Icons.AutoMirrored.Filled.ChatBubble
+ Icons.AutoMirrored.Filled.Chat
```

### Khusus: States.kt — Line 95

```diff
- imageVector = androidx.compose.material.icons.Icons.Default.Warning,
+ imageVector = Icons.Default.Warning,
```

Dan tambahkan import:
```diff
+ import androidx.compose.material.icons.Icons
+ import androidx.compose.material.icons.filled.Warning
```

### Khusus: TransactionListScreen.kt — Line 154: Missing `Box` import

```diff
  import androidx.compose.foundation.layout.Column
+ import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.layout.Row
```

---

## 4. DateUtils API Error

### File: [DateUtils.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/core/utils/DateUtils.kt) — Line 15

**Error:** `Too many arguments for DateTimeFormatter.ofLocalizedDate(FormatStyle)`

**Root Cause:** `DateTimeFormatter.ofLocalizedDate()` hanya menerima 1 argumen (`FormatStyle`), bukan 2. Locale harus di-set terpisah via `.withLocale()`.

```diff
- private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM, Locale("id", "ID"))
+ private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("id", "ID"))
```

---

## 5. ResultUtils Extension Conflict

### File: [ResultUtils.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/core/utils/ResultUtils.kt) — Line 35-36

**Error:** `Argument type mismatch: actual type is 'Function0<...>', but 'T' was expected`

**Root Cause:** Kotlin stdlib **sudah punya** `Result.getOrDefault(defaultValue: T)`. Extension function ini konflik/bentrok dengan yang built-in. Di line 36, kode memanggil `getOrDefault { default }` — meneruskan lambda ke fungsi yang mengharapkan nilai `T`.

**Solusi — Rename atau hapus:**

```diff
- fun <T> Result<T>.getOrDefault(default: T): T {
-     return getOrDefault { default }
- }
+ fun <T> Result<T>.getOrElse(default: T): T {
+     return getOrNull() ?: default
+ }
```

> [!IMPORTANT]
> Setelah rename, cari semua pemanggilan `getOrDefault` di codebase dan ganti ke `getOrElse`. Atau hapus saja fungsi ini karena stdlib sudah menyediakan `Result.getOrDefault()`.

Juga untuk `onSuccess` dan `onFailure` di line 10-23: Kotlin stdlib sudah memiliki `Result.onSuccess` dan `Result.onFailure`. Extension ini akan membuat conflict. **Rekomenasi: hapus seluruh file** karena semua fungsinya sudah tersedia di stdlib Kotlin 2.2.

---

## 6. Type Mismatch & Missing Imports

### A. FreeTextViewModel.kt — Line 120, 122

### File: [FreeTextViewModel.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/transaction/freetext/FreeTextViewModel.kt)

**Error:** `Assignment type mismatch: actual type is 'Long', but 'String' was expected`

**Root Cause:** `value` dideklarasikan sebagai `String` (dari `match.groupValues[1]`), tapi kemudian di-reassign dengan hasil `Long` multiplication.

```diff
  // Line 118-123: Handle shorthand
  if (text.contains("jt") || text.contains("juta")) {
-     value = (value.toLongOrNull() ?: 0) * 1_000_000
+     return ((value.toLongOrNull() ?: 0) * 1_000_000).toString()
  } else if (text.contains("rb") || text.contains("ribu")) {
-     value = (value.toLongOrNull() ?: 0) * 1_000
+     return ((value.toLongOrNull() ?: 0) * 1_000).toString()
  }
```

### B. AIChatScreen.kt — Line 183: `widthIn` unresolved

### File: [AIChatScreen.kt](file:///c:/project-uta/cuan/app/src/main/java/com/example/cuan/feature/ai_chat/AIChatScreen.kt)

**Root Cause:** Missing import for `widthIn` modifier.

```diff
  import androidx.compose.foundation.layout.width
+ import androidx.compose.foundation.layout.widthIn
  import androidx.compose.foundation.lazy.LazyColumn
```

---

## Checklist Perbaikan

Urutan eksekusi yang direkomendasikan:

- [ ] **1. Fix typo Theme.kt** — `CuOneolorScheme` → `CuOneColorScheme`, `CuOneypography` → `CuOneTypography`
- [ ] **2. Fix AppDataStore.kt** — Tambah import `intPreferencesKey` dan `edit`
- [ ] **3. Fix DateUtils.kt** — `.withLocale()` terpisah
- [ ] **4. Tambah `material-icons-extended` dependency** — `libs.versions.toml` + `build.gradle.kts`
- [ ] **5. Fix/rename ikon yang tidak ada** di Material Icons (lihat mapping table di atas)
- [ ] **6. Fix TransactionListScreen.kt** — Tambah import `Box`
- [ ] **7. Fix AIChatScreen.kt** — Tambah import `widthIn`
- [ ] **8. Fix ResultUtils.kt** — Hapus atau rename extension yang conflict dengan stdlib
- [ ] **9. Fix FreeTextViewModel.kt** — Type mismatch `Long` → `String`
- [ ] **10. Fix States.kt** — Ganti fully-qualified `Warning` icon reference
- [ ] **11. Fix BottomNavBarComponent.kt** — Ganti `ChatBubble` → `Chat`
- [ ] **12. Enable ProGuard** — `isMinifyEnabled = true` di release build
