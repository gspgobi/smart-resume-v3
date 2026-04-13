package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild1Entity

/** Contact Information */
data class SectionChild1(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val name: String,
    val address: String,
    val email: String,
    val phone: String,
    val gender: String,
    val dob: String,
    val dobDateFormat: String,
    val nationality: String,
    val userImagePath: String,
    val isUserImageEnable: Boolean
)

fun SectionChild1Entity.toModel() = SectionChild1(
    id = sectionChild1Id,
    sectionHeadAddedId = sectionHeadAddedId,
    name = sc1Name.orEmpty(),
    address = sc1Address.orEmpty(),
    email = sc1Email.orEmpty(),
    phone = sc1Phone.orEmpty(),
    gender = sc1Gender.orEmpty(),
    dob = sc1Dob.orEmpty(),
    dobDateFormat = sc1DobDateFormat.orEmpty(),
    nationality = sc1Nationality.orEmpty(),
    userImagePath = sc1UserImagePath.orEmpty(),
    isUserImageEnable = (sc1IsUserImageEnable ?: 0) == 1
)

fun SectionChild1.toEntity() = SectionChild1Entity(
    sectionChild1Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc1Name = name,
    sc1Address = address,
    sc1Email = email,
    sc1Phone = phone,
    sc1Gender = gender,
    sc1Dob = dob,
    sc1DobDateFormat = dobDateFormat,
    sc1Nationality = nationality,
    sc1UserImagePath = userImagePath,
    sc1IsUserImageEnable = if (isUserImageEnable) 1 else 0
)
