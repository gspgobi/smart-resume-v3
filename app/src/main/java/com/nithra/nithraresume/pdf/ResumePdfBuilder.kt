package com.nithra.nithraresume.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.utils.BG_COLOR_PEACH
import com.nithra.nithraresume.utils.BULLET_NONE
import com.nithra.nithraresume.utils.SrDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ResumePdfBuilder(private val context: Context) {

    private companion object { const val TAG = "ResumePdfBuilder" }

    private val COLOR_BLUE     = BaseColor(51, 102, 153)
    private val COLOR_GRAY     = BaseColor(192, 192, 192)
    private val COLOR_PEACH_BG = BaseColor(247, 242, 223)

    // ── Public entry point ─────────────────────────────────────────────────────

    fun build(data: ResumePdfData, fileName: String): File {
        try {
            return buildInternal(data, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "PDF generation failed", e)
            throw e
        }
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
        val fmt = data.format.title.uppercase()
        val bgColor = data.profile.backgroundColor

        val pageSize = Rectangle(PageSize.A4)
        if (bgColor.equals(BG_COLOR_PEACH, ignoreCase = true)) {
            pageSize.backgroundColor = COLOR_PEACH_BG
        }

        val document = Document(pageSize)
        document.setMargins(36f, 36f, 30f, 40f)
        document.setMarginMirroringTopBottom(true)

        val writer = PdfWriter.getInstance(document, FileOutputStream(outputFile))
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
        data.sections.forEach { sha -> buildSection(paragraph, sha, data, fonts, fmt, bgColor) }
        if (!paragraph.isEmpty()) document.add(paragraph)

        document.close()
        return outputFile
    }

    // ── Section dispatcher ─────────────────────────────────────────────────────

    private fun buildSection(
        p: Paragraph, sha: SectionHeadAdded, data: ResumePdfData,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        val id = sha.id
        when (sha.headBaseId) {
            1 -> data.sc1ByHeadId[id]?.let { buildSc1(p, it, fonts, fmt) }
            2 -> buildSc2(p, sha.title, data.sc2sByHeadId[id] ?: emptyList(), fonts, fmt, bgColor)
            3 -> buildSc3(p, sha.title, data.sc3sByHeadId[id] ?: emptyList(), fonts, fmt, bgColor)
            4 -> data.sc4ByHeadId[id]?.let {
                    buildSc4(p, sha.title, it, data.sc1ByHeadId.values.firstOrNull(), fonts, fmt, bgColor)
                }
            5 -> data.sc5ByHeadId[id]?.let { buildSc5(p, sha.title, it, fonts, fmt, bgColor) }
            6 -> buildSc6(p, sha.title, data.sc6sByHeadId[id] ?: emptyList(), fonts, fmt, bgColor)
            7 -> buildSc7(p, sha.title, data.sc7sByHeadId[id] ?: emptyList(), fonts, fmt, bgColor)
        }
    }

    // ── SC1 – Contact Info ─────────────────────────────────────────────────────

    private fun buildSc1(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        fmt: String
    ) {
        val photo = if (sc1.userImagePath.isNotEmpty() && sc1.isUserImageEnable)
            loadScaledImage(sc1.userImagePath, 150) else null

        when (fmt) {
            "CLASSIC"              -> sc1Classic(p, sc1, fonts, photo)
            "HARVARD", "GRAYSCALE" -> sc1Centered(p, sc1, fonts)
            else                   -> sc1Functional(p, sc1, fonts, photo)
        }
    }

    private fun sc1Functional(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        photo: Image?
    ) {
        if (photo != null) {
            val table = PdfPTable(floatArrayOf(11f, 2f)).apply { widthPercentage = 100f }
            table.addCell(noBorderCell(Phrase(sc1.name, fonts.nameFont), Element.ALIGN_CENTER, 1))
            photo.scaleAbsolute(50f, 50f)
            val imgCell = PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
                setBorder(Rectangle.NO_BORDER)
                rowspan = 3
            }
            table.addCell(imgCell)
            addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
            p.add(table)
        } else {
            val table = PdfPTable(1).apply { widthPercentage = 100f }
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
            addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
            p.add(table)
        }
    }

    private fun sc1Classic(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        photo: Image?
    ) {
        if (photo != null) {
            val table = PdfPTable(floatArrayOf(2f, 11f)).apply { widthPercentage = 100f }
            photo.scaleAbsolute(50f, 50f)
            val imgCell = PdfPCell(photo).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                setBorder(Rectangle.NO_BORDER)
                rowspan = 4
            }
            table.addCell(imgCell)
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_RIGHT)
            addContactRows(table, sc1, fonts, Element.ALIGN_RIGHT, 1)
            addRuleCell(table, colspan = 2, paddingBottom = 2f, font = fonts.subFont)
            p.add(table)
        } else {
            val table = PdfPTable(1).apply { widthPercentage = 100f }
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_RIGHT)
            addContactRows(table, sc1, fonts, Element.ALIGN_RIGHT, 1)
            addRuleCell(table, colspan = 1, paddingBottom = 2f, font = fonts.subFont)
            p.add(table)
        }
    }

    private fun sc1Centered(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts
    ) {
        val table = PdfPTable(1).apply { widthPercentage = 100f }
        addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
        addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
        p.add(table)
    }

    // ── SC2 – Work Experience ──────────────────────────────────────────────────

    private fun buildSc2(
        p: Paragraph, sectionTitle: String, items: List<SectionChild2>,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        if (fmt == "HARVARD") {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEach { item ->
                    addWorkItemRows(t, item.workRole, item.companyName, item.subtitle,
                        item.workPeriod, item.accomplishments, item.accomplishmentsBulletType, fonts, fmt)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        items.forEach { item ->
            val t = itemTable()
            addWorkItemRows(t, item.workRole, item.companyName, item.subtitle,
                item.workPeriod, item.accomplishments, item.accomplishmentsBulletType, fonts, fmt)
            p.add(t)
        }
    }

    private fun addWorkItemRows(
        table: PdfPTable,
        role: String, company: String, subtitle: String,
        period: String, content: String, bulletType: String,
        fonts: PdfFonts, fmt: String
    ) {
        when (fmt) {
            "FUNCTIONAL", "HARVARD" -> {
                if (company.isNotEmpty()) addBoldCell(table, " $company", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = joinNonEmpty(role, subtitle, ", ")
                table.addCell(noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1))
                table.addCell(noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1))
                addBulletContent(table, content, bulletType, fonts)
            }
            else -> {
                addBoldCell(table, buildClassicItemLine(role, subtitle, company, period), fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                addBulletContent(table, content, bulletType, fonts)
            }
        }
    }

    // ── SC3 – Education ────────────────────────────────────────────────────────

    private fun buildSc3(
        p: Paragraph, sectionTitle: String, items: List<SectionChild3>,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        if (fmt == "HARVARD") {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEach { item ->
                    addEducationItemRows(t, item.studyDegree, item.schoolName, item.subtitle,
                        item.studyPeriod, item.concentrates, item.concentratesBulletType, fonts, fmt)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        items.forEach { item ->
            val t = itemTable()
            addEducationItemRows(t, item.studyDegree, item.schoolName, item.subtitle,
                item.studyPeriod, item.concentrates, item.concentratesBulletType, fonts, fmt)
            p.add(t)
        }
    }

    private fun addEducationItemRows(
        table: PdfPTable,
        degree: String, school: String, subtitle: String,
        period: String, concentrates: String, bulletType: String,
        fonts: PdfFonts, fmt: String
    ) {
        when (fmt) {
            "CLASSIC" -> {
                addBoldCell(table, buildClassicItemLine(degree, subtitle, school, period), fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                addBulletContent(table, concentrates, bulletType, fonts)
            }
            else -> {
                if (school.isNotEmpty()) addBoldCell(table, " $school", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = joinNonEmpty(degree, subtitle, ", ")
                table.addCell(noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1))
                table.addCell(noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1))
                addBulletContent(table, concentrates, bulletType, fonts)
            }
        }
    }

    // ── SC4 – Declaration ──────────────────────────────────────────────────────

    private fun buildSc4(
        p: Paragraph, sectionTitle: String,
        sc4: com.nithra.nithraresume.data.model.SectionChild4,
        sc1: com.nithra.nithraresume.data.model.SectionChild1?,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)

        val table = itemTable()
        addBulletContent(table, sc4.declarationContent, sc4.declarationContentBulletType, fonts)
        if (sc4.date.isNotEmpty()) {
            table.addCell(noBorderCell(Phrase(sc4.date, fonts.subFont), Element.ALIGN_LEFT, 2))
        }
        if (sc4.place.isNotEmpty()) {
            table.addCell(noBorderCell(Phrase(sc4.place, fonts.subFont), Element.ALIGN_LEFT, 2))
        }
        if (sc4.signatureImagePath.isNotEmpty() && sc4.isSignatureImageEnable) {
            loadScaledImage(sc4.signatureImagePath, 200)?.let { img ->
                img.scaleAbsolute(80f, 40f)
                table.addCell(PdfPCell(img).apply {
                    setBorder(Rectangle.NO_BORDER)
                    colspan = 2
                })
            }
        }
        val name = sc1?.name.orEmpty()
        if (name.isNotEmpty()) {
            table.addCell(noBorderCell(Phrase(name, fonts.subFont), Element.ALIGN_LEFT, 2))
        }
        p.add(table)
    }

    // ── SC5 – Paragraph ────────────────────────────────────────────────────────

    private fun buildSc5(
        p: Paragraph, sectionTitle: String,
        sc5: com.nithra.nithraresume.data.model.SectionChild5,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        val table = itemTable()
        addBulletContent(table, sc5.content, sc5.contentBulletType, fonts)
        p.add(table)
    }

    // ── SC6 – Split Text ───────────────────────────────────────────────────────

    private fun buildSc6(
        p: Paragraph, sectionTitle: String, items: List<SectionChild6>,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        fun splitTable() = PdfPTable(floatArrayOf(5f, 10f)).apply { widthPercentage = 100f }
        fun fillSplitTable(t: PdfPTable) = items.forEach { item ->
            val titleCell = noBorderCell(Phrase(item.contentTitle, fonts.subBoldFont), Element.ALIGN_LEFT, 1)
            titleCell.setLeading(2f, 1.2f)
            val detailCell = noBorderCell(Phrase(item.contentDetail, fonts.subFont), Element.ALIGN_JUSTIFIED, 1)
            detailCell.setLeading(2f, 1.2f)
            t.addCell(titleCell); t.addCell(detailCell)
        }

        if (fmt == "HARVARD") {
            buildHarvardSection(p, sectionTitle, fonts, splitTable()) { t -> fillSplitTable(t) }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        if (items.isEmpty()) return
        val table = splitTable()
        fillSplitTable(table)
        p.add(table)
    }

    // ── SC7 – Multiple Item Text ───────────────────────────────────────────────

    private fun buildSc7(
        p: Paragraph, sectionTitle: String, items: List<SectionChild7>,
        fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        if (fmt == "HARVARD") {
            buildHarvardSection(p, sectionTitle, fonts) { t ->
                items.forEach { item ->
                    addBoldCell(t, joinNonEmpty(item.contentTitle, item.contentSubtitle, " — "), fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                    addBulletContent(t, item.contentDetail, item.contentDetailBulletType, fonts)
                }
            }
            return
        }
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        items.forEach { item ->
            val t = itemTable()
            if (item.contentTitle.isNotEmpty()) {
                addBoldCell(t, item.contentTitle, fonts.subBoldFont, Element.ALIGN_LEFT, 2)
            }
            if (item.contentSubtitle.isNotEmpty()) {
                t.addCell(noBorderCell(Phrase(item.contentSubtitle, fonts.subFont), Element.ALIGN_LEFT, 2))
            }
            addBulletContent(t, item.contentDetail, item.contentDetailBulletType, fonts)
            p.add(t)
        }
    }

    // ── SC8 – Cover Letter ─────────────────────────────────────────────────────

    private fun buildCoverLetterTable(
        title: String,
        sc8: com.nithra.nithraresume.data.model.SectionChild8,
        sc1: com.nithra.nithraresume.data.model.SectionChild1?,
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
        title: String,
        sc8: com.nithra.nithraresume.data.model.SectionChild8,
        sc1: com.nithra.nithraresume.data.model.SectionChild1?,
        fonts: PdfFonts
    ): Paragraph {
        val p = Paragraph()

        // Title (e.g. "COVER LETTER") — centered
        if (title.isNotEmpty()) {
            p.add(Paragraph(title, fonts.nameFont).also {
                it.alignment = Element.ALIGN_CENTER
            })
        }

        // 6 empty lines after title (mirrors addEmptyLine(paragraph_cover, 6) in v2)
        repeat(6) { p.add(Paragraph(" ", fonts.subFont)) }

        // SC1 contact block: each field + one spacer line, same as v2 cover_table rows
        sc1?.let { c1 ->
            if (c1.name.isNotEmpty()) {
                p.add(Paragraph(c1.name, fonts.subFont).apply { alignment = Element.ALIGN_LEFT })
            }

            // Address — left-aligned, large spacingAfter mirrors v2's paddingBottom=50
            if (sc8.address.isNotEmpty()) {
                p.add(Paragraph(sc8.address, fonts.subFont).apply { alignment = Element.ALIGN_LEFT })
            }
            if (c1.email.isNotEmpty()) {
                p.add(Paragraph(c1.email, fonts.subFont).apply { alignment = Element.ALIGN_LEFT })
            }
            if (c1.phone.isNotEmpty()) {
                p.add(Paragraph(c1.phone, fonts.subFont).apply { alignment = Element.ALIGN_LEFT })
            }
            p.add(Paragraph(" ", fonts.subFont).apply { spacingAfter = 20f })
        }

        // Date
        if (sc8.date.isNotEmpty()) {
            p.add(Paragraph(sc8.date, fonts.subFont).apply { alignment = Element.ALIGN_LEFT
            spacingAfter = 30f})
        }

        // Content + "\n\n" + SC1 name in one paragraph (mirrors v2's single cell)
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

    private fun buildHarvardSection(
        p: Paragraph,
        sectionTitle: String,
        fonts: PdfFonts,
        contentTable: PdfPTable = itemTable(),
        buildContent: (PdfPTable) -> Unit
    ) {
        buildContent(contentTable)

        val outerTable = PdfPTable(floatArrayOf(3f, 10f)).apply {
            widthPercentage = 100f
            spacingBefore = 6f
        }
        val titleCell = noBorderCell(Phrase(sectionTitle, fonts.headingFont), Element.ALIGN_LEFT, 1)
        titleCell.paddingTop = 4f
        outerTable.addCell(titleCell)

        val contentCell = PdfPCell(contentTable).apply {
            setBorder(Rectangle.NO_BORDER)
            setPadding(0f)
        }
        outerTable.addCell(contentCell)
        p.add(outerTable)
    }

    // ── Section heading ────────────────────────────────────────────────────────

    private fun addSectionHeading(
        p: Paragraph, title: String, fonts: PdfFonts, fmt: String, bgColor: String
    ) {
        when (fmt) {
            "CLASSIC" -> {
                val table = PdfPTable(floatArrayOf(0.5f, 10f)).apply { widthPercentage = 100f }
                val cell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                cell.setLeading(2f, 1.5f)
                table.addCell(cell)
                p.add(table)
            }
            "MODERN" -> {
                val table = PdfPTable(floatArrayOf(10f, 5f)).apply { widthPercentage = 100f }
                val cell = noBorderCell(Phrase(title, fonts.headingModernFont), Element.ALIGN_LEFT, 2)
                cell.setLeading(2f, 1.5f)
                table.addCell(cell)
                table.addCell(ruleTopCell(fonts.headingModernFont, colspan = 2))
                p.add(table)
            }
            "GRAYSCALE" -> {
                val table = PdfPTable(floatArrayOf(0.4f, 10f)).apply {
                    widthPercentage = 100f
                    spacingBefore = 8f
                }
                val grayCell = PdfPCell(Phrase("", fonts.subFont)).apply {
                    backgroundColor = COLOR_GRAY
                    setBorder(Rectangle.NO_BORDER)
                }
                table.addCell(grayCell)
                val nested = PdfPTable(floatArrayOf(0.4f, 10f))
                nested.addCell(PdfPCell(Phrase("", fonts.headingFont)).apply { setBorder(Rectangle.NO_BORDER) })
                nested.addCell(noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 1))
                table.addCell(PdfPCell(nested).apply { setBorder(Rectangle.NO_BORDER) })
                p.add(table)
            }
            "FUNCTIONAL", "SIMPLE" -> {
                val table = PdfPTable(1).apply {
                    widthPercentage = 100f
                    spacingBefore = 8f
                }
                val cell = PdfPCell(Phrase(title, fonts.headingFont)).apply {
                    horizontalAlignment = Element.ALIGN_LEFT
                    setBorder(Rectangle.BOTTOM)
                    borderColorBottom = BaseColor.BLACK
                    borderWidthBottom = 0.5f
                    paddingBottom = 2f
                }
                table.addCell(cell)
                p.add(table)
            }
            else -> { // HARVARD fallback
                val table = PdfPTable(floatArrayOf(10f, 5f)).apply { widthPercentage = 100f }
                val cell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                cell.setLeading(2f, 1.5f)
                table.addCell(cell)
                table.addCell(ruleTopCell(fonts.subFont, colspan = 2))
                p.add(table)
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
            val cell = PdfPCell().apply {
                setBorder(Rectangle.NO_BORDER)
                colspan = 2
                setPadding(0f)
            }
            val para = if (hasBullet) {
                Paragraph("• $line", fonts.subFont).apply {
                    setFirstLineIndent(-12f)
                    indentationLeft = 12f
                }
            } else {
                Paragraph(line, fonts.subFont).apply {
                    alignment = Element.ALIGN_JUSTIFIED
                }
            }
            para.setLeading(2f, 1.4f)
            cell.addElement(para)
            table.addCell(cell)
        }
    }

    private fun addContactRows(
        table: PdfPTable,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts, alignment: Int, colspan: Int
    ) {
        listOf(sc1.address, sc1.phone, sc1.email)
            .filter { it.isNotEmpty() }
            .forEach { value ->
                table.addCell(PdfPCell(Phrase(value, fonts.addressFont)).apply {
                    horizontalAlignment = alignment
                    setBorder(Rectangle.NO_BORDER)
                    this.colspan = colspan
                })
            }
        val genderDob = joinNonEmpty(sc1.gender, sc1.dob, "  |  ")
        if (genderDob.isNotEmpty()) {
            table.addCell(PdfPCell(Phrase(genderDob, fonts.addressFont)).apply {
                horizontalAlignment = alignment
                setBorder(Rectangle.NO_BORDER)
                this.colspan = colspan
            })
        }
    }

    private fun addNameCell(table: PdfPTable, name: String, font: Font, alignment: Int) {
        table.addCell(PdfPCell(Phrase(name, font)).apply {
            horizontalAlignment = alignment
            setBorder(Rectangle.NO_BORDER)
        })
    }

    private fun addRuleCell(table: PdfPTable, colspan: Int, paddingBottom: Float, font: Font) {
        table.addCell(ruleTopCell(font, colspan).apply { this.paddingBottom = paddingBottom })
    }

    private fun ruleTopCell(font: Font, colspan: Int): PdfPCell =
        PdfPCell(Phrase("", font)).apply {
            setBorder(Rectangle.TOP)
            setBorderColorTop(BaseColor.BLACK)
            borderWidthTop = 0.5f
            this.colspan = colspan
        }

    private fun addBoldCell(table: PdfPTable, text: String, font: Font, alignment: Int, colspan: Int) {
        table.addCell(noBorderCell(Phrase(text, font), alignment, colspan))
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
        val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            ?: return@runCatching null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        bitmap.recycle()
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
            headingModernFont = font((fontSize + 1).toFloat(),  Font.BOLD, COLOR_BLUE),
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
