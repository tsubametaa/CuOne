package com.example.cuan.data.repository

import com.example.cuan.data.model.Transaction

interface SheetsRepository {
    suspend fun verifySpreadsheet(spreadsheetId: String, accessToken: String): Result<Boolean>
    suspend fun createMonthlyTab(spreadsheetId: String, monthYear: String, accessToken: String): Result<Boolean>
    suspend fun syncTransactions(spreadsheetId: String, transactions: List<Transaction>, accessToken: String): Result<Boolean>
    suspend fun updateSummaryTab(spreadsheetId: String, accessToken: String): Result<Boolean>
}
