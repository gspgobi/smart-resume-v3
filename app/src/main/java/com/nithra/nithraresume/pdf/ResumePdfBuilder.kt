package com.nithra.nithraresume.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.nithra.nithraresume.data.model.ResumeFormatType
import com.nithra.nithraresume.data.model.SectionChild1
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionChild5
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.model.SectionChild8
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.toFormatType
import com.nithra.nithraresume.utils.BG_COLOR_PEACH
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.SrDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ResumePdfBuilder(private val context: Context) {

    private companion object { const val TAG = "ResumePdfBuilder" }

    private val colorBlue    = BaseColor(51, 102, 153)
    private val colorGray    = BaseColor(192, 192, 192)
    private val colorPeachBg = BaseColor(247, 242, 223)

    // ── Public entry point ─────────────────────────────────────────────────────

    fun build(data: ResumePdfData, fileName: String): File = try {
        buildInternal(data, fileName)
    } catch (e: Exception) {
        Log.e(TAG, "PDF generation failed", e)
        throw e
    }

    private fun buildInternal(data: ResumePdfData, fileName: String): File {
        val outputDir = File(context.getExternalFilesDir(null), SrDir.GENERATED_RESUME)
        outputDir.mkdirs()
        val outputFile = File(outputDir, "$fileName.pdf")

        val fontFile = extractFont(data.profile.fontStyle)
        val baseFont = runCatching {
            BaseFont.createFont(fontFile.absolutePath, BaseFont.WINANSI, BaseFont.EMBEDDED)
        }.getOrElse {
            BaseFont.createFont(fontFile.absolutePath, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
        }
        val fonts = buildFonts(baseFont, data.profile.fontSize)
        val fmt = data.format.toFormatType()
        val bgColor = data.profile.backgroundColor

        val pageSize = Rectangle(PageSize.A4)
        if (bgColor.equals(BG_COLOR_PEACH, ignoreCase = true)) {
            pageSize.backgroundColor = colorPeachBg
        }

        val document = Document(pageSize)
        document.setMargins(36f, 36f, 36f, 36f)
        document.setMarginMirroringTopBottom(true)

        FileOutputStream(outputFile).use { fos ->
            val writer = PdfWriter.getInstance(document, fos)
            writer.setPdfVersion(PdfWriter.VERSION_1_5)
            writer.setFullCompression()
            document.open()

            val sc1ForCoverLetter = data.sc1ByHeadId.values.firstOrNull()
            data.addons.forEach { sha ->
                if (sha.headBaseId == 8) {
                    data.sc8ByHeadId[sha.id]?.let {
                        document.add(buildCoverLetterTable(sha.title, it, sc1ForCoverLetter, fonts))
                        document.newPage()
                    }
                }
            }

            val paragraph = Paragraph()
            data.sections.forEach { sha -> buildSection(paragraph, sha, data, fonts, fmt) }
            if (!paragraph.isEmpty()) document.add(paragraph)

            document.close()
        }
        return outputFile
    }

    // ── Section dispatcher ─────────────────────────────────────────────────────

    private fun buildSection(
        p: Paragraph, sha: SectionHeadAdded, data: ResumePdfData,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        val id = sha.id
        when (sha.headBaseId) {
            1 -> data.sc1ByHeadId[id]?.let { buildSc1(p, it, fonts, fmt) }
            2 -> buildSc2(p, sha.title, data.sc2sByHeadId[id] ?: emptyList(), fonts, fmt)
            3 -> buildSc3(p, sha.title, data.sc3sByHeadId[id] ?: emptyList(), fonts, fmt)
            4 -> data.sc4ByHeadId[id]?.let {
                    buildSc4(p, sha.title, it, data.sc1ByHeadId.values.firstOrNull(), fonts, fmt)
                }
            5 -> data.sc5ByHeadId[id]?.let { buildSc5(p, sha.title, it, fonts, fmt) }
            6 -> buildSc6(p, sha.title, data.sc6sByHeadId[id] ?: emptyList(), fonts, fmt)
            7 -> buildSc7(p, sha.title, data.sc7sByHeadId[id] ?: emptyList(), fonts, fmt)
        }
    }

    // ── SC1 – Contact Info ─────────────────────────────────────────────────────

    private fun buildSc1(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, fmt: ResumeFormatType) {
        val photo = if (sc1.userImagePath.isNotEmpty() && sc1.isUserImageEnable)
            loadScaledImage(sc1.userImagePath, 150) else null
        when (fmt) {
            ResumeFormatType.CLASSIC   -> sc1Classic(p, sc1, fonts, photo)
            ResumeFormatType.HARVARD   -> sc1Harvard(p, sc1, fonts)
            ResumeFormatType.GRAYSCALE -> sc1Grayscale(p, sc1, fonts, photo)
            ResumeFormatType.MODERN    -> sc1Modern(p, sc1, fonts, photo)
            ResumeFormatType.SIMPLE    -> sc1Simple(p, sc1, fonts, photo)
            else                       -> sc1Functional(p, sc1, fonts, photo)
        }
    }

    private fun sc1Functional(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, photo: Image?) {
        val contactLines = buildFunctionalContactLines(sc1)
        val rowCount = 1 + contactLines.size   // name row + N contact lines

        if (photo != null) {
            photo.scaleAbsolute(60f, 60f)
            val table = PdfPTable(floatArrayOf(11f, 2f)).apply {
                widthPercentage = 100f
                spacingAfter   = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            table.addCell(PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                verticalAlignment   = Element.ALIGN_MIDDLE
                setBorder(Rectangle.NO_BORDER)
                rowspan = rowCount
            })
            contactLines.forEach { line ->
                table.addCell(PdfPCell(Phrase(line, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_CENTER
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        } else {
            val table = PdfPTable(1).apply {
                widthPercentage = 100f
                spacingAfter   = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            contactLines.forEach { line ->
                table.addCell(PdfPCell(Phrase(line, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_CENTER
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        }
    }

    private fun buildFunctionalContactLines(sc1: SectionChild1): List<String> {
        val lines = mutableListOf<String>()
        // Phone + email share one line; address gets its own line below
        val inline = listOf(sc1.phone, sc1.email).filter { it.isNotEmpty() }
        if (inline.isNotEmpty()) lines.add(inline.joinToString("  |  "))
        if (sc1.address.isNotEmpty()) lines.add(sc1.address.replace("\n", ""))
        return lines
    }

    private fun sc1Classic(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, photo: Image?) {
        val contactValues = listOf(sc1.address, sc1.phone, sc1.email).filter { it.isNotEmpty() }

        if (photo != null) {
            photo.scaleAbsolute(60f, 60f)
            val table = PdfPTable(floatArrayOf(2f, 11f)).apply { widthPercentage = 100f }
            table.addCell(PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                verticalAlignment   = Element.ALIGN_MIDDLE
                setBorder(Rectangle.NO_BORDER)
                rowspan = 1 + contactValues.size  // name + contact rows; rule uses colspan=2 below
            })
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            contactValues.forEach { value ->
                table.addCell(PdfPCell(Phrase(value, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_RIGHT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        } else {
            val table = PdfPTable(1).apply { widthPercentage = 100f }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            contactValues.forEach { value ->
                table.addCell(PdfPCell(Phrase(value, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_RIGHT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        }
    }

    private fun sc1Harvard(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts) {
        val table = PdfPTable(1).apply { widthPercentage = 100f }
        table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            setBorder(Rectangle.NO_BORDER)
            paddingBottom = 3f
        })
        buildFunctionalContactLines(sc1).forEach { line ->
            table.addCell(PdfPCell(Phrase(line, fonts.subFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                setBorder(Rectangle.NO_BORDER)
                paddingTop = 1f
            })
        }
        p.add(table)
    }

    private fun sc1Modern(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, photo: Image?) {
        val inlineParts = listOf(sc1.phone, sc1.email).filter { it.isNotEmpty() }
        val rowCount = 1 + (if (inlineParts.isNotEmpty()) 1 else 0) + (if (sc1.address.isNotEmpty()) 1 else 0)

        if (photo != null) {
            photo.scaleAbsolute(60f, 60f)
            val table = PdfPTable(floatArrayOf(11f, 2f)).apply {
                widthPercentage = 100f
                spacingAfter    = 6f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            table.addCell(PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                verticalAlignment   = Element.ALIGN_MIDDLE
                setBorder(Rectangle.NO_BORDER)
                rowspan = rowCount
            })
            if (inlineParts.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(inlineParts.joinToString("  |  "), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            if (sc1.address.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(sc1.address.replace("\n", ""), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        } else {
            val table = PdfPTable(1).apply {
                widthPercentage = 100f
                spacingAfter    = 6f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            if (inlineParts.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(inlineParts.joinToString("  |  "), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            if (sc1.address.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(sc1.address.replace("\n", ""), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        }
    }

    private fun sc1Simple(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, photo: Image?) {
        val contactParts = listOf(sc1.phone, sc1.email, sc1.address).filter { it.isNotEmpty() }
        val rowCount = 1 + if (contactParts.isNotEmpty()) 1 else 0

        if (photo != null) {
            photo.scaleAbsolute(60f, 60f)
            val table = PdfPTable(floatArrayOf(11f, 2f)).apply {
                widthPercentage = 100f
                spacingAfter    = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            table.addCell(PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                verticalAlignment   = Element.ALIGN_MIDDLE
                setBorder(Rectangle.NO_BORDER)
                rowspan = rowCount
            })
            if (contactParts.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(contactParts.joinToString("  |  "), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        } else {
            val table = PdfPTable(1).apply {
                widthPercentage = 100f
                spacingAfter    = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            if (contactParts.isNotEmpty()) {
                table.addCell(PdfPCell(Phrase(contactParts.joinToString("  |  "), fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        }
    }

    private fun sc1Grayscale(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts, photo: Image?) {
        val contactLines = buildFunctionalContactLines(sc1)

        if (photo != null) {
            photo.scaleAbsolute(60f, 60f)
            val table = PdfPTable(floatArrayOf(11f, 2f)).apply {
                widthPercentage = 100f
                spacingAfter    = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            table.addCell(PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                verticalAlignment   = Element.ALIGN_MIDDLE
                setBorder(Rectangle.NO_BORDER)
                rowspan = 1 + contactLines.size
            })
            contactLines.forEach { line ->
                table.addCell(PdfPCell(Phrase(line, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_CENTER
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        } else {
            val table = PdfPTable(1).apply {
                widthPercentage = 100f
                spacingAfter    = 4f
            }
            table.addCell(PdfPCell(Phrase(sc1.name, fonts.nameFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                setBorder(Rectangle.NO_BORDER)
                paddingBottom = 2f
            })
            contactLines.forEach { line ->
                table.addCell(PdfPCell(Phrase(line, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_CENTER
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop = 1f
                })
            }
            p.add(table)
        }
    }

    private fun sc1Centered(p: Paragraph, sc1: SectionChild1, fonts: PdfFonts) {
        val table = PdfPTable(1).apply { widthPercentage = 100f }
        addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
        addContactRows(table, sc1, fonts, Element.ALIGN_CENTER)
        p.add(table)
    }

    // ── SC2 – Work Experience ──────────────────────────────────────────────────

    private fun buildSc2(
        p: Paragraph, sectionTitle: String, items: List<SectionChild2>,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEachIndexed { index, item ->
                    if (index > 0) addHarvardItemSpacer(t)
                    addWorkItemRows(t, item.workRole, item.companyName, item.subtitle,
                        item.workPeriod, item.accomplishments, item.accomplishmentsBulletType, fonts, fmt)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt)
        items.forEachIndexed { index, item ->
            val t = itemTable().also {
                it.spacingBefore = 4f
                it.spacingAfter  = 4f
            }
            addWorkItemRows(t, item.workRole, item.companyName, item.subtitle,
                item.workPeriod, item.accomplishments, item.accomplishmentsBulletType, fonts, fmt)
            p.add(t)
        }
    }

    private fun addWorkItemRows(
        table: PdfPTable,
        role: String, company: String, subtitle: String,
        period: String, content: String, bulletType: String,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        when (fmt) {
            ResumeFormatType.HARVARD -> {
                val titleLine = buildClassicItemLine(role, subtitle, company, period)
                addBoldCell(table, titleLine, fonts.subBoldFont)
                addBulletContent(table, content, bulletType, fonts)
            }
            ResumeFormatType.FUNCTIONAL -> {
                if (company.isNotEmpty()) addBoldCell(table, company, fonts.subBoldFont)
                val left = joinNonEmpty(role, subtitle, ", ")
                table.addCell(PdfPCell(Phrase(left, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 2f
                })
                table.addCell(PdfPCell(Phrase(period, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_RIGHT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 2f
                })
                addBulletContent(table, content, bulletType, fonts)
            }
            ResumeFormatType.CLASSIC, ResumeFormatType.MODERN,
            ResumeFormatType.SIMPLE, ResumeFormatType.GRAYSCALE -> {
                if (company.isNotEmpty() || period.isNotEmpty()) {
                    table.addCell(PdfPCell(Phrase(company, fonts.subBoldFont)).apply {
                        horizontalAlignment = Element.ALIGN_LEFT
                        setBorder(Rectangle.NO_BORDER)
                        paddingTop    = 1f
                        paddingBottom = 1f
                    })
                    table.addCell(PdfPCell(Phrase(period, fonts.subFont)).apply {
                        horizontalAlignment = Element.ALIGN_RIGHT
                        setBorder(Rectangle.NO_BORDER)
                        paddingTop    = 1f
                        paddingBottom = 1f
                    })
                }
                val roleSubtitle = joinNonEmpty(role, subtitle, ", ")
                if (roleSubtitle.isNotEmpty()) {
                    table.addCell(noBorderCell(Phrase(roleSubtitle, fonts.subFont), Element.ALIGN_LEFT, 2))
                }
                addBulletContent(table, content, bulletType, fonts)
            }
            else -> {
                addBoldCell(table, buildClassicItemLine(role, subtitle, company, period), fonts.subBoldFont)
                addBulletContent(table, content, bulletType, fonts)
            }
        }
    }

    // ── SC3 – Education ────────────────────────────────────────────────────────

    private fun buildSc3(
        p: Paragraph, sectionTitle: String, items: List<SectionChild3>,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEachIndexed { index, item ->
                    if (index > 0) addHarvardItemSpacer(t)
                    addEducationItemRows(t, item.studyDegree, item.schoolName, item.subtitle,
                        item.studyPeriod, item.concentrates, item.concentratesBulletType, fonts, fmt)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt)
        items.forEachIndexed { index, item ->
            val t = itemTable().also {
                it.spacingBefore = 4f
                it.spacingAfter  = 4f
            }
            addEducationItemRows(t, item.studyDegree, item.schoolName, item.subtitle,
                item.studyPeriod, item.concentrates, item.concentratesBulletType, fonts, fmt)
            p.add(t)
        }
    }

    private fun addEducationItemRows(
        table: PdfPTable,
        degree: String, school: String, subtitle: String,
        period: String, concentrates: String, bulletType: String,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        when (fmt) {
            ResumeFormatType.CLASSIC, ResumeFormatType.MODERN,
            ResumeFormatType.SIMPLE, ResumeFormatType.GRAYSCALE -> {
                if (school.isNotEmpty() || period.isNotEmpty()) {
                    table.addCell(PdfPCell(Phrase(school, fonts.subBoldFont)).apply {
                        horizontalAlignment = Element.ALIGN_LEFT
                        setBorder(Rectangle.NO_BORDER)
                        paddingTop    = 1f
                        paddingBottom = 1f
                    })
                    table.addCell(PdfPCell(Phrase(period, fonts.subFont)).apply {
                        horizontalAlignment = Element.ALIGN_RIGHT
                        setBorder(Rectangle.NO_BORDER)
                        paddingTop    = 1f
                        paddingBottom = 1f
                    })
                }
                val degreeSubtitle = joinNonEmpty(degree, subtitle, ", ")
                if (degreeSubtitle.isNotEmpty()) {
                    table.addCell(noBorderCell(Phrase(degreeSubtitle, fonts.subFont), Element.ALIGN_LEFT, 2))
                }
                addBulletContent(table, concentrates, bulletType, fonts)
            }
            ResumeFormatType.HARVARD -> {
                val titleLine = buildHarvardEduLine(school, subtitle, degree, period)
                addBoldCell(table, titleLine, fonts.subBoldFont)
                addBulletContent(table, concentrates, bulletType, fonts)
            }
            else -> {
                if (school.isNotEmpty()) addBoldCell(table, school, fonts.subBoldFont)
                val left = joinNonEmpty(degree, subtitle, ", ")
                table.addCell(PdfPCell(Phrase(left, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 2f
                })
                table.addCell(PdfPCell(Phrase(period, fonts.subFont)).apply {
                    horizontalAlignment = Element.ALIGN_RIGHT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 2f
                })
                addBulletContent(table, concentrates, bulletType, fonts)
            }
        }
    }

    // ── SC4 – Declaration ──────────────────────────────────────────────────────

    private fun buildSc4(
        p: Paragraph, sectionTitle: String,
        sc4: SectionChild4,
        sc1: SectionChild1?,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        val sigImg = if (sc4.signatureImagePath.isNotEmpty() && sc4.isSignatureImageEnable)
            loadScaledImage(sc4.signatureImagePath, 200) else null
        sigImg?.scaleAbsolute(80f, 40f)
        val name = sc1?.name?.takeIf { it.isNotEmpty() }

        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                addBulletContent(t, sc4.declarationContent, sc4.declarationContentBulletType, fonts)
            }
            val footerTable = itemTable()
            footerTable.addCell(PdfPCell().apply {
                setBorder(Rectangle.NO_BORDER)
                colspan     = 2
                fixedHeight = 8f
            })
            footerTable.addCell(PdfPCell().apply {
                setBorder(Rectangle.NO_BORDER)
                paddingTop = 2f
                addElement(Paragraph(sc4.date, fonts.subFont))
                addElement(Paragraph(sc4.place, fonts.subFont).apply { spacingBefore = 1f })
            })
            footerTable.addCell(PdfPCell().apply {
                setBorder(Rectangle.NO_BORDER)
                paddingTop = 2f
                if (sigImg != null) {
                    sigImg.alignment = Element.ALIGN_CENTER
                    addElement(sigImg)
                } else {
                    addElement(Paragraph(" ", fonts.subFont).apply {
                        alignment = Element.ALIGN_CENTER
                        spacingAfter = 32f
                    })
                }
                addElement(Paragraph(name ?: "", fonts.subFont).apply {
                    alignment = Element.ALIGN_CENTER
                    spacingBefore = 2f
                })
            })
            p.add(footerTable)
            return
        }

        addSectionHeading(p, sectionTitle, fonts, fmt)
        val table = itemTable()
        addBulletContent(table, sc4.declarationContent, sc4.declarationContentBulletType, fonts)
        table.addCell(PdfPCell().apply {
            setBorder(Rectangle.NO_BORDER)
            colspan     = 2
            fixedHeight = 8f
        })
        table.addCell(PdfPCell().apply {
            setBorder(Rectangle.NO_BORDER)
            paddingTop = 2f
            addElement(Paragraph(sc4.date, fonts.subFont))
            addElement(Paragraph(sc4.place, fonts.subFont).apply { spacingBefore = 1f })
        })
        table.addCell(PdfPCell().apply {
            setBorder(Rectangle.NO_BORDER)
            paddingTop = 2f
            if (sigImg != null) {
                sigImg.alignment = Element.ALIGN_CENTER
                addElement(sigImg)
            } else {
                addElement(Paragraph(" ", fonts.subFont).apply {
                    alignment = Element.ALIGN_CENTER
                    spacingAfter = 32f
                })
            }
            addElement(Paragraph(name ?: "", fonts.subFont).apply {
                alignment = Element.ALIGN_CENTER
                spacingBefore = 2f
            })
        })
        p.add(table)
    }

    // ── SC5 – Paragraph ────────────────────────────────────────────────────────

    private fun buildSc5(
        p: Paragraph, sectionTitle: String,
        sc5: SectionChild5,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                addBulletContent(t, sc5.content, sc5.contentBulletType, fonts)
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt)
        val table = itemTable()
        addBulletContent(table, sc5.content, sc5.contentBulletType, fonts)
        p.add(table)
    }

    // ── SC6 – Split Text ───────────────────────────────────────────────────────

    private fun buildSc6(
        p: Paragraph, sectionTitle: String, items: List<SectionChild6>,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        fun splitTable() = PdfPTable(floatArrayOf(5f, 10f)).apply { widthPercentage = 100f }
        fun fillSplitTable(t: PdfPTable) = items.forEach { item ->
            val titleCell = noBorderCell(Phrase(item.contentTitle, fonts.subBoldFont), Element.ALIGN_LEFT, 1)
            titleCell.setLeading(2f, 1.2f)
            val detailCell = noBorderCell(Phrase(item.contentDetail, fonts.subFont), Element.ALIGN_JUSTIFIED, 1)
            detailCell.setLeading(2f, 1.2f)
            t.addCell(titleCell); t.addCell(detailCell)
        }

        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts, splitTable()) { t -> fillSplitTable(t) }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt)
        if (items.isEmpty()) return
        val table = splitTable()
        fillSplitTable(table)
        p.add(table)
    }

    // ── SC7 – Multiple Item Text ───────────────────────────────────────────────

    private fun buildSc7(
        p: Paragraph, sectionTitle: String, items: List<SectionChild7>,
        fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        if (fmt == ResumeFormatType.HARVARD) {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEachIndexed { index, item ->
                    if (index > 0) addHarvardItemSpacer(t)
                    if (item.contentTitle.isNotEmpty()) addBoldCell(t, item.contentTitle, fonts.subBoldFont)
                    if (item.contentSubtitle.isNotEmpty()) t.addCell(noBorderCell(Phrase(item.contentSubtitle, fonts.subFont), Element.ALIGN_LEFT, 2))
                    addBulletContent(t, item.contentDetail, item.contentDetailBulletType, fonts)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt)
        items.forEachIndexed { index, item ->
            val t = itemTable().also {
                it.spacingBefore = 4f
                it.spacingAfter  = 4f
            }
            if (item.contentTitle.isNotEmpty())    addBoldCell(t, item.contentTitle, fonts.subBoldFont)
            if (item.contentSubtitle.isNotEmpty()) t.addCell(noBorderCell(Phrase(item.contentSubtitle, fonts.subFont), Element.ALIGN_LEFT, 2))
            addBulletContent(t, item.contentDetail, item.contentDetailBulletType, fonts)
            p.add(t)
        }
    }

    // ── SC8 – Cover Letter ─────────────────────────────────────────────────────

    private fun buildCoverLetterTable(
        title: String,
        sc8: SectionChild8,
        sc1: SectionChild1?,
        fonts: PdfFonts
    ): PdfPTable {
        val table = PdfPTable(1).apply { widthPercentage = 100f }
        table.addCell(PdfPCell().apply {
            addElement(makeCoverLetterParagraph(title, sc8, sc1, fonts))
            setBorder(Rectangle.NO_BORDER)
            setPadding(0f)
        })
        return table
    }

    private fun makeCoverLetterParagraph(
        title: String, sc8: SectionChild8, sc1: SectionChild1?, fonts: PdfFonts
    ): Paragraph {
        fun leftPara(text: String) = Paragraph(text, fonts.subFont).apply { alignment = Element.ALIGN_LEFT }

        val p = Paragraph()

        if (title.isNotEmpty()) {
            p.add(Paragraph(title, fonts.nameFont).apply { alignment = Element.ALIGN_CENTER })
        }
        repeat(6) { p.add(Paragraph(" ", fonts.subFont)) }

        sc1?.let { c1 ->
            if (c1.name.isNotEmpty())     p.add(leftPara(c1.name))
            if (c1.email.isNotEmpty())    p.add(leftPara(c1.email))
            if (c1.phone.isNotEmpty())    p.add(leftPara(c1.phone))
            if (sc8.address.isNotEmpty()) p.add(leftPara(sc8.address))
            p.add(Paragraph(" ", fonts.subFont).apply { spacingAfter = 20f })
        }

        if (sc8.date.isNotEmpty()) {
            p.add(leftPara(sc8.date).apply { spacingAfter = 30f })
        }

        if (sc8.content.isNotEmpty()) {
            val sc1Name = sc1?.name?.takeIf { it.isNotEmpty() } ?: ""
            val text = if (sc1Name.isNotEmpty()) "${sc8.content}\n\n\n$sc1Name" else sc8.content
            p.add(Paragraph(text, fonts.subFont).apply {
                alignment = Element.ALIGN_LEFT
                setLeading(2f, 1.3f)
            })
        }

        return p
    }

    // ── Harvard layout helper ──────────────────────────────────────────────────

    private fun addHarvardItemSpacer(table: PdfPTable) {
        table.addCell(PdfPCell().apply {
            setBorder(Rectangle.NO_BORDER)
            colspan     = 2
            fixedHeight = 8f
        })
    }

    private fun buildHarvardSection(
        p: Paragraph,
        sectionTitle: String,
        fonts: PdfFonts,
        contentTable: PdfPTable = itemTable(),
        buildContent: (PdfPTable) -> Unit
    ) {
        buildContent(contentTable)

        val titleCell = PdfPCell(Phrase(sectionTitle, fonts.headingFont)).apply {
            horizontalAlignment = Element.ALIGN_LEFT
            verticalAlignment   = Element.ALIGN_TOP
            setBorder(Rectangle.NO_BORDER)
            paddingTop    = 2f
            paddingRight  = 8f
            paddingBottom = 0f
        }
        val contentCell = PdfPCell(contentTable).apply {
            setBorder(Rectangle.NO_BORDER)
            setPadding(0f)
        }
        p.add(PdfPTable(floatArrayOf(3f, 10f)).apply {
            widthPercentage = 100f
            spacingBefore   = 10f
            addCell(titleCell)
            addCell(contentCell)
        })
    }

    // ── Section heading ────────────────────────────────────────────────────────

    private fun addSectionHeading(
        p: Paragraph, title: String, fonts: PdfFonts, fmt: ResumeFormatType
    ) {
        when (fmt) {
            ResumeFormatType.CLASSIC -> {
                val cell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.BOTTOM)
                    borderColorBottom = BaseColor.BLACK
                    borderWidthBottom = 0.5f
                    paddingTop        = 2f
                    paddingBottom     = 4f
                }
                p.add(PdfPTable(1).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(cell)
                })
            }
            ResumeFormatType.MODERN -> {
                val cell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                    .also {
                        it.setLeading(2f, 1.5f)
                        it.paddingTop    = 2f
                        it.paddingBottom = 2f
                    }
                p.add(PdfPTable(floatArrayOf(10f, 5f)).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(cell)
                    addCell(ruleTopCell(fonts.headingFont, colspan = 2))
                })
            }
            ResumeFormatType.GRAYSCALE -> {
                val grayCell = PdfPCell(Phrase("", fonts.subFont)).apply {
                    backgroundColor = colorGray
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 4f
                }
                val titleCell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingLeft   = 6f
                    paddingTop    = 2f
                    paddingBottom = 4f
                }
                p.add(PdfPTable(floatArrayOf(0.4f, 10f)).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(grayCell)
                    addCell(titleCell)
                })
            }
            ResumeFormatType.FUNCTIONAL -> {
                val cell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.BOTTOM)
                    borderColorBottom = BaseColor.BLACK
                    borderWidthBottom = 0.5f
                    paddingTop    = 2f
                    paddingBottom = 4f
                }
                p.add(PdfPTable(1).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(cell)
                })
            }
            ResumeFormatType.SIMPLE -> {
                val cell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.NO_BORDER)
                    paddingTop    = 2f
                    paddingBottom = 4f
                }
                p.add(PdfPTable(1).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(cell)
                })
            }
            ResumeFormatType.HARVARD -> {
                val cell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.BOTTOM)
                    borderColorBottom = BaseColor.BLACK
                    borderWidthBottom = 0.5f
                    paddingTop        = 2f
                    paddingBottom     = 4f
                }
                p.add(PdfPTable(1).apply {
                    widthPercentage = 100f
                    spacingBefore   = 10f
                    spacingAfter    = 2f
                    addCell(cell)
                })
            }
            else -> {
                val cell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                    .also { it.setLeading(2f, 1.5f) }
                p.add(PdfPTable(floatArrayOf(10f, 5f)).apply {
                    widthPercentage = 100f
                    addCell(cell)
                    addCell(ruleTopCell(fonts.subFont, colspan = 2))
                })
            }
        }
    }

    // ── Low-level helpers ──────────────────────────────────────────────────────

    private fun itemTable(): PdfPTable =
        PdfPTable(floatArrayOf(10f, 5f)).apply { widthPercentage = 100f }

    private fun addBulletContent(
        table: PdfPTable, content: String, bulletType: String, fonts: PdfFonts
    ) {
        if (content.isEmpty()) return
        val hasBullet = !bulletType.equals(BULLET_NONE, ignoreCase = true)
        content.split("\n").forEach { line ->
            if (line.isBlank()) return@forEach
            val para = if (hasBullet) {
                Paragraph("• $line", fonts.subFont).apply {
                    setFirstLineIndent(-12f)
                    indentationLeft = 12f
                }
            } else {
                Paragraph(line, fonts.subFont).apply { alignment = Element.ALIGN_JUSTIFIED }
            }
            para.setLeading(2f, 1.4f)
            table.addCell(PdfPCell().apply {
                setBorder(Rectangle.NO_BORDER)
                colspan      = 2
                paddingTop   = 1f
                paddingLeft  = 0f
                paddingBottom = 1f
                paddingRight = 0f
                addElement(para)
            })
        }
    }

    private fun addContactRows(table: PdfPTable, sc1: SectionChild1, fonts: PdfFonts, alignment: Int) {
        listOf(sc1.address, sc1.phone, sc1.email).filter { it.isNotEmpty() }.forEach { value ->
            table.addCell(PdfPCell(Phrase(value, fonts.addressFont)).apply {
                horizontalAlignment = alignment
                setBorder(Rectangle.NO_BORDER)
            })
        }
    }

    private fun addNameCell(table: PdfPTable, name: String, font: Font, alignment: Int) {
        table.addCell(PdfPCell(Phrase(name, font)).apply {
            horizontalAlignment = alignment
            setBorder(Rectangle.NO_BORDER)
        })
    }

    private fun addRuleCell(table: PdfPTable, colspan: Int, font: Font) {
        table.addCell(ruleTopCell(font, colspan).apply { paddingBottom = 2f })
    }

    private fun ruleTopCell(font: Font, colspan: Int): PdfPCell =
        PdfPCell(Phrase("", font)).apply {
            setBorder(Rectangle.TOP)
            setBorderColorTop(BaseColor.BLACK)
            borderWidthTop = 0.5f
            this.colspan = colspan
        }

    private fun addBoldCell(table: PdfPTable, text: String, font: Font) {
        table.addCell(noBorderCell(Phrase(text, font), Element.ALIGN_LEFT, 2))
    }

    private fun noBorderCell(phrase: Phrase, alignment: Int, colspan: Int): PdfPCell =
        PdfPCell(phrase).apply {
            horizontalAlignment = alignment
            setBorder(Rectangle.NO_BORDER)
            this.colspan = colspan
        }

    private fun joinNonEmpty(a: String, b: String, separator: String): String = when {
        a.isNotEmpty() && b.isNotEmpty() -> "$a$separator$b"
        a.isNotEmpty() -> a
        else -> b
    }

    private fun buildHarvardEduLine(
        school: String, subtitle: String, degree: String, period: String
    ): String = buildString {
        if (school.isNotEmpty()) append(school)
        if (subtitle.isNotEmpty()) append(", $subtitle")
        if (degree.isNotEmpty()) append(" -- $degree")
        if (period.isNotEmpty()) append(", $period")
    }

    private fun buildClassicItemLine(
        role: String, subtitle: String, company: String, period: String
    ): String = buildString {
        if (role.isNotEmpty()) append(role)
        if (subtitle.isNotEmpty()) append(", $subtitle")
        if (company.isNotEmpty()) append(", $company")
        if (period.isNotEmpty()) append(" --  $period")
    }

    // ── Image helpers ──────────────────────────────────────────────────────────

    private fun loadScaledImage(path: String, maxDimPx: Int = 200): Image? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val sampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx)
        val decoded = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            ?: return@runCatching null
        // Composite onto white before JPEG encoding. If the source has an alpha channel
        // (e.g. transparent-background PNG), Android's encoder can emit a 4-channel JPEG
        // which iText interprets as CMYK, causing full color inversion in the PDF.
        val rgb = Bitmap.createBitmap(decoded.width, decoded.height, Bitmap.Config.ARGB_8888)
        Canvas(rgb).apply { drawColor(Color.WHITE); drawBitmap(decoded, 0f, 0f, null) }
        decoded.recycle()
        val out = ByteArrayOutputStream()
        rgb.compress(Bitmap.CompressFormat.JPEG, 85, out)
        rgb.recycle()
        Image.getInstance(out.toByteArray())
    }.getOrNull()

    private fun computeSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var size = 1
        while (width / size > maxDim || height / size > maxDim) size *= 2
        return size
    }

    // ── Font helpers ───────────────────────────────────────────────────────────

    private data class PdfFonts(
        val nameFont: Font,
        val headingFont: Font,
        val headingModernFont: Font,
        val subFont: Font,
        val subBoldFont: Font,
        val addressFont: Font
    )

    private fun buildFonts(baseFont: BaseFont, fontSize: Int): PdfFonts {
        fun font(size: Float, style: Int, color: BaseColor? = null) =
            if (color != null) Font(baseFont, size, style, color) else Font(baseFont, size, style)
        return PdfFonts(
            nameFont          = font((fontSize + 12).toFloat(), Font.BOLD),
            headingFont       = font((fontSize + 1).toFloat(),  Font.BOLD),
            headingModernFont = font((fontSize + 1).toFloat(),  Font.BOLD, colorBlue),
            subFont           = font(fontSize.toFloat(),         Font.NORMAL),
            subBoldFont       = font(fontSize.toFloat(),         Font.BOLD),
            addressFont       = font((fontSize - 2).coerceAtLeast(6).toFloat(), Font.ITALIC)
        )
    }

    private fun extractFont(fontFileName: String): File {
        val fontsDir = File(context.cacheDir, "sr_fonts")
        fontsDir.mkdirs()
        val fontFile = File(fontsDir, fontFileName)
        if (!fontFile.exists()) {
            context.assets.open("fonts/$fontFileName").use { input ->
                fontFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return fontFile
    }
}
