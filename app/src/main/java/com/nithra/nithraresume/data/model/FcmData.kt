package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.FcmDataEntity

data class FcmData(
    val id: Int = 0,
    val messageType: String,
    val notificationType: String,
    val title: String,
    val message: String,
    val imageUrl: String,
    val timestamp: String,
    val isRead: Boolean
)

fun FcmDataEntity.toModel() = FcmData(
    id = fcmDataId,
    messageType = fcmMessageType.orEmpty(),
    notificationType = fcmNotificationType.orEmpty(),
    title = fcmTitle.orEmpty(),
    message = fcmMessage.orEmpty(),
    imageUrl = fcmImageUrl.orEmpty(),
    timestamp = fcmTimestamp.orEmpty(),
    isRead = (fcmIsRead ?: 0) == 1
)

fun FcmData.toEntity() = FcmDataEntity(
    fcmDataId = id,
    fcmMessageType = messageType,
    fcmNotificationType = notificationType,
    fcmTitle = title,
    fcmMessage = message,
    fcmImageUrl = imageUrl,
    fcmTimestamp = timestamp,
    fcmIsRead = if (isRead) 1 else 0
)
