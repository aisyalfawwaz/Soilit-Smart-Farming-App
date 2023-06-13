package com.github.user.soilitouraplication.api

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

data class UserResponse(
    val data: List<Campaign>,
)

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class Campaign(
    val id: Int,
    val name: String,
    val description: String,
    val image: String,
    val created_at: String,
) : Parcelable


data class HistoryResponse(
    val message: String?,
    val data: List<History>,
)

data class FaqResponse(
    val message: String?,
    val data: List<FaqClass>,
)

data class FaqClass(
    val answer: String,
    val question: String,
)

data class TemperatureResponse(
    val type: String,
    val value: Int,
    val unit: String,
)


@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val image: String,
    val user_id: String,
    val soil_type: String,
    val soil_moisture: Int,
    val soil_temperature: Int,
    val soil_condition: String,
    val created_at: String,
)
