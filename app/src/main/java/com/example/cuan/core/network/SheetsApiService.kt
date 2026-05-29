package com.example.cuan.core.network

import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class SheetsAppendRequest(
    val range: String,
    val majorDimension: String = "ROWS",
    val values: List<List<String>>
)

@Serializable
data class SheetsUpdateRequest(
    val range: String,
    val majorDimension: String = "ROWS",
    val values: List<List<String>>
)

@Serializable
data class BatchUpdateRequest(
    val requests: List<SheetRequest>
)

@Serializable
data class SheetRequest(
    val addSheet: AddSheetRequest? = null
)

@Serializable
data class AddSheetRequest(
    val properties: SheetProperties
)

@Serializable
data class SheetProperties(
    val title: String
)

interface SheetsApiService {

    @GET("v4/spreadsheets/{spreadsheetId}")
    suspend fun getSpreadsheet(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String
    ): ResponseBody

    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Body request: RequestBody
    ): ResponseBody

    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getValues(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String
    ): ResponseBody

    @POST("v4/spreadsheets/{spreadsheetId}:batchUpdate")
    suspend fun batchUpdate(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Body request: RequestBody
    ): ResponseBody

    @PUT("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun updateValues(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Body request: RequestBody
    ): ResponseBody
}
