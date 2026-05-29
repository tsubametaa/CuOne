package com.example.cuan.data.model

/**
 * Google Sheets configuration
 */
data class SheetsConfig(
    val spreadsheetId: String,
    val spreadsheetUrl: String,
    val isConnected: Boolean,
    val lastSyncAt: Long?
)

/**
 * Extract spreadsheet ID from URL
 * URL format: https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit...
 */
fun extractSpreadsheetId(url: String): String? {
    val regex = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
    return regex.find(url)?.groupValues?.get(1)
}