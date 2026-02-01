package com.example.clearday.network.model

import com.google.gson.annotations.SerializedName

/**
 * Root object for the Google Pollen API forecast response.
 */
data class PollenForecastResponse(
    @SerializedName("regionCode")
    val regionCode: String,

    @SerializedName("dailyInfo")
    val dailyInfo: List<DailyInfo>?,

    @SerializedName("nextPageToken")
    val nextPageToken: String?
)

/**
 * Contains pollen information for a specific calendar day.
 */
data class DailyInfo(
    @SerializedName("date")
    val date: DateInfo,

    @SerializedName("pollenTypeInfo")
    val pollenTypeInfo: List<PollenTypeInfo>?,

    @SerializedName("plantInfo")
    val plantInfo: List<PlantInfo>?
)

/**
 * Represents the date components for the forecast info.
 */
data class DateInfo(
    @SerializedName("year")
    val year: Int,

    @SerializedName("month")
    val month: Int,

    @SerializedName("day")
    val day: Int
)

/**
 * Details regarding specific pollen categories like Grass, Tree, or Weed.
 */
data class PollenTypeInfo(
    @SerializedName("code")
    val code: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("indexInfo")
    val indexInfo: IndexInfo?,

    @SerializedName("healthRecommendations")
    val healthRecommendations: List<String>?,

    @SerializedName("inSeason")
    val inSeason: Boolean?
)

/**
 * Specific plant data indicating if it is currently in season and its intensity index.
 */
data class PlantInfo(
    @SerializedName("code")
    val code: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("indexInfo")
    val indexInfo: IndexInfo?,

    @SerializedName("inSeason")
    val inSeason: Boolean?
)

/**
 * Standardized index information (e.g., UPI) describing the intensity of pollen levels.
 */
data class IndexInfo(
    @SerializedName("code")
    val code: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("value")
    val value: Int,

    @SerializedName("category")
    val category: String?,

    @SerializedName("indexDescription")
    val indexDescription: String?,

    @SerializedName("color")
    val color: ColorInfo?
)

/**
 * Color representation in RGBA format as provided by the API for UI mapping.
 */
data class ColorInfo(
    @SerializedName("red")
    val red: Float,

    @SerializedName("green")
    val green: Float,

    @SerializedName("blue")
    val blue: Float,

    @SerializedName("alpha")
    val alpha: Float?
)