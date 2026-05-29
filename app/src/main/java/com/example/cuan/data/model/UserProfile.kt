package com.example.cuan.data.model

/**
 * User profile - stored locally in DataStore, never sent to any server
 */
data class UserProfile(
    val name: String = "",
    val occupation: String = "",
    val monthlyIncomeRange: IncomeRange? = null,
    val monthlyBudget: Long? = null,
    val sheetsUrl: String = "",
    val openRouterApiKey: String = "",
    val isProfileComplete: Boolean = false
)

/**
 * Income range options for user profile
 */
enum class IncomeRange(val label: String, val rangeDescription: String) {
    BELOW_3M("Di bawah 3 juta", "< Rp 3.000.000"),
    RANGE_3M_5M("3 - 5 juta", "Rp 3.000.000 - 5.000.000"),
    RANGE_5M_10M("5 - 10 juta", "Rp 5.000.000 - 10.000.000"),
    RANGE_10M_20M("10 - 20 juta", "Rp 10.000.000 - 20.000.000"),
    ABOVE_20M("Di atas 20 juta", "> Rp 20.000.000"),
    PREFER_NOT_TO_SAY("Tidak ingin membagikan", "")
}

/**
 * Occupation suggestions for user profile
 */
object OccupationSuggestions {
    val suggestions = listOf(
        "Karyawan Swasta",
        "PNS / ASN",
        "Wirausaha",
        "Freelancer",
        "Mahasiswa",
        "Profesional (Dokter/Pengacara/dll)",
        "Ibu Rumah Tangga",
        "Lainnya"
    )
}