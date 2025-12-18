package com.example.clearday.network.model

import com.google.gson.annotations.SerializedName

data class PollenForecastResponse(
    @SerializedName("regionCode")
    val regionCode: String,
    
    @SerializedName("dailyInfo")
    val dailyInfo: List<DailyInfo>?,
    
    @SerializedName("nextPageToken")
    val nextPageToken: String?
)

data class DailyInfo(
    @SerializedName("date")
    val date: DateInfo,
    
    @SerializedName("pollenTypeInfo")
    val pollenTypeInfo: List<PollenTypeInfo>?,
    
    @SerializedName("plantInfo")
    val plantInfo: List<PlantInfo>?
)

data class DateInfo(
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("month")
    val month: Int,
    
    @SerializedName("day")
    val day: Int
)

data class PollenTypeInfo(
    @SerializedName("code")
    val code: String, // e.g., "GRASS", "TREE", "WEED"
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("indexInfo")
    val indexInfo: IndexInfo?,
    
    @SerializedName("healthRecommendations")
    val healthRecommendations: List<String>?,
    
    @SerializedName("inSeason")
    val inSeason: Boolean?
)

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

data class IndexInfo(
    @SerializedName("code")
    val code: String, // e.g., "UPI" (Universal Pollen Index)
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("value")
    val value: Int, // 0-5 scale
    
    @SerializedName("category")
    val category: String?, // e.g., "NONE", "LOW", "MODERATE", "HIGH", "VERY_HIGH"
    
    @SerializedName("indexDescription")
    val indexDescription: String?,
    
    @SerializedName("color")
    val color: ColorInfo?
)

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
