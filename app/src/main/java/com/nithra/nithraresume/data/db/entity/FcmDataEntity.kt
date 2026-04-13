package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Push Notifications */
@Entity(tableName = "fcm_data")
data class FcmDataEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fcm_data_id")
    val fcmDataId: Int = 0,

    @ColumnInfo(name = "fcm_message_type")
    val fcmMessageType: String?,

    @ColumnInfo(name = "fcm_notification_type")
    val fcmNotificationType: String?,

    @ColumnInfo(name = "fcm_title")
    val fcmTitle: String?,

    @ColumnInfo(name = "fcm_message")
    val fcmMessage: String?,

    @ColumnInfo(name = "fcm_image_url")
    val fcmImageUrl: String?,

    @ColumnInfo(name = "fcm_timestamp")
    val fcmTimestamp: String?,

    @ColumnInfo(name = "fcm_is_read")
    val fcmIsRead: Int?
)
