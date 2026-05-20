package com.nithra.nithraresume.data.model

enum class ResumeFormatType(val id: Int) {
    FUNCTIONAL(1),
    HARVARD(2),
    CLASSIC(3),
    MODERN(4),
    SIMPLE(5),
    GRAYSCALE(6);

    companion object {
        fun fromId(id: Int): ResumeFormatType = entries.find { it.id == id } ?: FUNCTIONAL
    }
}

fun ResumeFormat.toFormatType(): ResumeFormatType = ResumeFormatType.fromId(id)
