package com.nithra.nithraresume.pdf

import android.content.Context
import android.graphics.Color
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
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
import java.io.File
import java.io.FileOutputStream

/**
 * Builds a PDF resume using iTextPDF 5.4.0.
 * Call [build] on Dispatchers.IO.
 */
class ResumePdfBuilder(private val context: Context) {

    // ── Font aliases ───────────────────────────────────────────────────────────

    private val BASE_FONT_ALIAS = "sr_base_font"

    // Color constants (matching V2)
    private val COLOR_BLUE   = BaseColor(51, 102, 153)
    private val COLOR_GREEN  = BaseColor(0, 90, 4)
    private val COLOR_GRAY   = BaseColor(192, 192, 192)
    private val COLOR_ORANGE = BaseColor(230, 115, 0)
    private val COLOR_PEACH_BG = BaseColor(247, 242, 223)
    private val COLOR_WHITE  = BaseColor(255, 255, 255)

    // ── Public entry point ─────────────────────────────────────────────────────

    /**
     * @param data       All resume data pre-loaded from the DB.
     * @param fileName   Output filename without .pdf extension.
     * @return           The generated PDF [File].
     */
    fun build(data: ResumePdfData, fileName: String): File {
        val outputDir = File(context.getExternalFilesDir(null), SrDir.GENERATED_RESUME)
        outputDir.mkdirs()
        val outputFile = File(outputDir, "$fileName.pdf")

        val fontFile = extractFont(data.profile.fontStyle)
        FontFactory.register(fontFile.absolutePath, BASE_FONT_ALIAS)

        val sz = data.profile.fontSize
        val fonts = buildFonts(sz)
        val fmt = data.format.title.uppercase()
        val bgColor = data.profile.backgroundColor

        val pageSize = Rectangle(PageSize.A4)
        if (bgColor.equals(BG_COLOR_PEACH, ignoreCase = true)) {
            pageSize.backgroundColor = COLOR_PEACH_BG
        }

        val document = Document(pageSize)
        document.setMargins(0.5f, 0.5f, 30f, 40f)
        document.setMarginMirroringTopBottom(true)

        val paragraph = Paragraph()
        val coverParagraph = Paragraph()

        PdfWriter.getInstance(document, FileOutputStream(outputFile))
        document.open()

        // ── Cover letter (addon sc8) ──────────────────────────────────────────
        data.addons.forEach { sha ->
            if (sha.headBaseId == 8) {
                val sc8 = data.sc8ByHeadId[sha.id]
                if (sc8 != null) buildCoverLetter(coverParagraph, sha, sc8, fonts, fmt, bgColor)
            }
        }
        if (!coverParagraph.isEmpty()) {
            document.add(coverParagraph)
            document.newPage()
        }

        // ── Main sections ──────────────────────────────────────────────────────
        data.sections.forEach { sha ->
            buildSection(paragraph, sha, data, fonts, fmt, bgColor)
        }
        if (!paragraph.isEmpty()) document.add(paragraph)

        document.close()
        return outputFile
    }

    // ── Section dispatcher ─────────────────────────────────────────────────────

    private fun buildSection(
        p: Paragraph,
        sha: SectionHeadAdded,
        data: ResumePdfData,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        val shaId = sha.id
        when (sha.headBaseId) {
            1 -> data.sc1ByHeadId[shaId]?.let { buildSc1(p, sha.title, it, fonts, fmt) }
            2 -> buildSc2(p, sha.title, data.sc2sByHeadId[shaId] ?: emptyList(), fonts, fmt, bgColor)
            3 -> buildSc3(p, sha.title, data.sc3sByHeadId[shaId] ?: emptyList(), fonts, fmt, bgColor)
            4 -> data.sc4ByHeadId[shaId]?.let { buildSc4(p, sha.title, it, fonts, fmt) }
            5 -> data.sc5ByHeadId[shaId]?.let { buildSc5(p, sha.title, it, fonts, fmt, bgColor) }
            6 -> buildSc6(p, sha.title, data.sc6sByHeadId[shaId] ?: emptyList(), fonts, fmt, bgColor)
            7 -> buildSc7(p, sha.title, data.sc7sByHeadId[shaId] ?: emptyList(), fonts, fmt, bgColor)
        }
    }

    // ── SC1 – Contact Info ─────────────────────────────────────────────────────

    private fun buildSc1(
        p: Paragraph,
        title: String,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        fmt: String
    ) {
        val hasPhoto = sc1.userImagePath.isNotEmpty() && sc1.isUserImageEnable

        when {
            fmt == "CLASSIC" -> sc1Classic(p, sc1, fonts, hasPhoto)
            fmt == "HARVARD" -> sc1Harvard(p, sc1, fonts)
            fmt == "MODERN"  -> sc1Modern(p, sc1, fonts, hasPhoto)
            fmt == "GRAYSCALE" -> sc1Grayscale(p, sc1, fonts)
            else             -> sc1Functional(p, sc1, fonts, hasPhoto) // FUNCTIONAL, SIMPLE
        }
    }

    private fun sc1Functional(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        hasPhoto: Boolean
    ) {
        if (hasPhoto) {
            val colWidths = floatArrayOf(11f, 2f)
            val table = PdfPTable(colWidths)
            var cell = PdfPCell(Phrase(sc1.name, fonts.nameFont))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.setBorder(Color.WHITE); cell.colspan = 1; cell.rowspan = 1
            table.addCell(cell)
            runCatching {
                val img = Image.getInstance(sc1.userImagePath)
                img.scaleAbsolute(50f, 50f)
                cell = PdfPCell(img)
                cell.horizontalAlignment = Element.ALIGN_RIGHT
                cell.setBorder(Color.WHITE); cell.rowspan = 3
                table.addCell(cell)
            }
            addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
            table.horizontalAlignment = Element.ALIGN_MIDDLE
            p.add(table)
        } else {
            val table = PdfPTable(1)
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
            addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
            table.horizontalAlignment = Element.ALIGN_MIDDLE
            p.add(table)
        }
    }

    private fun sc1Classic(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        hasPhoto: Boolean
    ) {
        if (hasPhoto) {
            val colWidths = floatArrayOf(2f, 11f)
            val table = PdfPTable(colWidths)
            runCatching {
                val img = Image.getInstance(sc1.userImagePath)
                img.scaleAbsolute(50f, 50f)
                val cell = PdfPCell(img)
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.setBorder(Color.WHITE); cell.rowspan = 4
                table.addCell(cell)
            }
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_RIGHT)
            addContactRows(table, sc1, fonts, Element.ALIGN_RIGHT, 1)
            addRuleCell(table, colspan = 2, paddingBottom = 2f)
            table.horizontalAlignment = Element.ALIGN_MIDDLE
            p.add(table)
        } else {
            val table = PdfPTable(1)
            addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_RIGHT)
            addContactRows(table, sc1, fonts, Element.ALIGN_RIGHT, 1)
            addRuleCell(table, colspan = 2, paddingBottom = 2f)
            table.horizontalAlignment = Element.ALIGN_MIDDLE
            p.add(table)
        }
    }

    private fun sc1Modern(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        hasPhoto: Boolean
    ) {
        // Modern uses same layout as Functional
        sc1Functional(p, sc1, fonts, hasPhoto)
    }

    private fun sc1Harvard(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts
    ) {
        val table = PdfPTable(1)
        addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
        addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
        table.horizontalAlignment = Element.ALIGN_MIDDLE
        p.add(table)
    }

    private fun sc1Grayscale(
        p: Paragraph,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts
    ) {
        val table = PdfPTable(1)
        addNameCell(table, sc1.name, fonts.nameFont, Element.ALIGN_CENTER)
        addContactRows(table, sc1, fonts, Element.ALIGN_CENTER, 1)
        table.horizontalAlignment = Element.ALIGN_MIDDLE
        p.add(table)
    }

    // ── SC2 – Work Experience ──────────────────────────────────────────────────

    private fun buildSc2(
        p: Paragraph,
        sectionTitle: String,
        items: List<SectionChild2>,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        if (items.isEmpty()) {
            addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
            return
        }
        var headerAdded = false
        items.forEach { item ->
            val isFirst = !headerAdded
            if (isFirst) { addSectionHeading(p, sectionTitle, fonts, fmt, bgColor); headerAdded = true }
            addWorkItem(p, item.workRole, item.companyName, item.subtitle,
                item.workPeriod, item.accomplishments, item.accomplishmentsBulletType, fonts, fmt)
        }
    }

    private fun addWorkItem(
        p: Paragraph,
        role: String, company: String, subtitle: String,
        period: String, content: String, bulletType: String,
        fonts: PdfFonts, fmt: String
    ) {
        val table = PdfPTable(floatArrayOf(10f, 5f))
        when {
            fmt == "FUNCTIONAL" -> {
                // Company bold, role+subtitle left, period right
                if (company.isNotEmpty()) addBoldCell(table, " $company", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = buildInlineText(role, subtitle, ", ")
                val cell1 = noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1)
                val cell2 = noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1)
                table.addCell(cell1); table.addCell(cell2)
                addBulletContent(table, content, bulletType, fonts)
            }
            fmt == "CLASSIC" -> {
                // "role, subtitle, company -- period" all bold on one line
                val line = buildClassicItemLine(role, subtitle, company, period)
                addBoldCell(table, line, fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                addBulletContent(table, content, bulletType, fonts)
            }
            fmt == "HARVARD" -> {
                // Already in two-column heading; for Harvard items, same as Functional
                if (company.isNotEmpty()) addBoldCell(table, " $company", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = buildInlineText(role, subtitle, ", ")
                val cell1 = noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1)
                val cell2 = noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1)
                table.addCell(cell1); table.addCell(cell2)
                addBulletContent(table, content, bulletType, fonts)
            }
            else -> {
                // MODERN, SIMPLE, GRAYSCALE: Informal style
                val line = buildClassicItemLine(role, subtitle, company, period)
                addBoldCell(table, line, fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                addBulletContent(table, content, bulletType, fonts)
            }
        }
        p.add(table)
    }

    // ── SC3 – Education ────────────────────────────────────────────────────────

    private fun buildSc3(
        p: Paragraph,
        sectionTitle: String,
        items: List<SectionChild3>,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        if (items.isEmpty()) {
            addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
            return
        }
        var headerAdded = false
        items.forEach { item ->
            if (!headerAdded) { addSectionHeading(p, sectionTitle, fonts, fmt, bgColor); headerAdded = true }
            addEducationItem(p, item.studyDegree, item.schoolName, item.subtitle,
                item.studyPeriod, item.concentrates, item.concentratesBulletType, fonts, fmt)
        }
    }

    private fun addEducationItem(
        p: Paragraph,
        degree: String, school: String, subtitle: String,
        period: String, concentrates: String, bulletType: String,
        fonts: PdfFonts, fmt: String
    ) {
        val table = PdfPTable(floatArrayOf(10f, 5f))
        when {
            fmt == "CLASSIC" -> {
                val line = buildClassicItemLine(degree, subtitle, school, period)
                addBoldCell(table, line, fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                addBulletContent(table, concentrates, bulletType, fonts)
            }
            fmt == "HARVARD" -> {
                if (degree.isNotEmpty()) addBoldCell(table, " $degree", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = buildInlineText(school, subtitle, ", ")
                val cell1 = noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1)
                val cell2 = noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1)
                table.addCell(cell1); table.addCell(cell2)
                addBulletContent(table, concentrates, bulletType, fonts)
            }
            else -> {
                // FUNCTIONAL, MODERN, SIMPLE, GRAYSCALE
                if (degree.isNotEmpty()) addBoldCell(table, " $degree", fonts.subBoldFont, Element.ALIGN_LEFT, 2)
                val left = buildInlineText(school, subtitle, ", ")
                val cell1 = noBorderCell(Phrase(" $left", fonts.subFont), Element.ALIGN_LEFT, 1)
                val cell2 = noBorderCell(Phrase(period, fonts.subFont), Element.ALIGN_RIGHT, 1)
                table.addCell(cell1); table.addCell(cell2)
                addBulletContent(table, concentrates, bulletType, fonts)
            }
        }
        p.add(table)
    }

    // ── SC4 – Declaration ──────────────────────────────────────────────────────

    private fun buildSc4(
        p: Paragraph,
        sectionTitle: String,
        sc4: com.nithra.nithraresume.data.model.SectionChild4,
        fonts: PdfFonts,
        fmt: String
    ) {
        val headTable = PdfPTable(floatArrayOf(10f, 5f))
        val headCell = noBorderCell(Phrase(sectionTitle, fonts.headingFont), Element.ALIGN_LEFT, 2)
        headTable.addCell(headCell)
        val ruleCell = PdfPCell(Phrase("", fonts.subFont))
        ruleCell.setBorderColorTop(com.itextpdf.text.BaseColor.BLACK)
        ruleCell.colspan = 2; ruleCell.setBorder(0)
        ruleCell.borderWidthTop = 0.5f
        headTable.addCell(ruleCell)
        p.add(headTable)

        val table = PdfPTable(floatArrayOf(10f, 5f))
        addBulletContent(table, sc4.declarationContent, sc4.declarationContentBulletType, fonts)

        if (sc4.date.isNotEmpty() || sc4.place.isNotEmpty()) {
            val datePlace = buildInlineText(sc4.place, sc4.date, ", ")
            val dpCell = noBorderCell(Phrase(datePlace, fonts.subFont), Element.ALIGN_LEFT, 2)
            table.addCell(dpCell)
        }

        if (sc4.signatureImagePath.isNotEmpty() && sc4.isSignatureImageEnable) {
            runCatching {
                val img = Image.getInstance(sc4.signatureImagePath)
                img.scaleAbsolute(80f, 40f)
                val imgCell = PdfPCell(img)
                imgCell.setBorder(Color.WHITE); imgCell.colspan = 2
                table.addCell(imgCell)
            }
        }
        p.add(table)
    }

    // ── SC5 – Paragraph ────────────────────────────────────────────────────────

    private fun buildSc5(
        p: Paragraph,
        sectionTitle: String,
        sc5: com.nithra.nithraresume.data.model.SectionChild5,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        val table = PdfPTable(floatArrayOf(0.5f, 10f))
        addBulletContent(table, sc5.content, sc5.contentBulletType, fonts)
        p.add(table)
    }

    // ── SC6 – Split Text ───────────────────────────────────────────────────────

    private fun buildSc6(
        p: Paragraph,
        sectionTitle: String,
        items: List<SectionChild6>,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
        if (items.isEmpty()) return

        val table = PdfPTable(floatArrayOf(5f, 10f))
        items.forEach { item ->
            val titleCell = noBorderCell(Phrase(item.contentTitle, fonts.subBoldFont), Element.ALIGN_LEFT, 1)
            titleCell.setLeading(2f, 1.2f)
            val detailCell = noBorderCell(Phrase(item.contentDetail, fonts.subFont), Element.ALIGN_JUSTIFIED, 1)
            detailCell.setLeading(2f, 1.2f)
            table.addCell(titleCell)
            table.addCell(detailCell)
        }
        p.add(table)
    }

    // ── SC7 – Multiple Item Text ───────────────────────────────────────────────

    private fun buildSc7(
        p: Paragraph,
        sectionTitle: String,
        items: List<SectionChild7>,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        if (items.isEmpty()) {
            addSectionHeading(p, sectionTitle, fonts, fmt, bgColor)
            return
        }
        var headerAdded = false
        items.forEach { item ->
            if (!headerAdded) { addSectionHeading(p, sectionTitle, fonts, fmt, bgColor); headerAdded = true }
            val table = PdfPTable(floatArrayOf(10f, 5f))
            val titleLine = buildInlineText(item.contentTitle, item.contentSubtitle, " — ")
            addBoldCell(table, titleLine, fonts.subBoldFont, Element.ALIGN_LEFT, 2)
            addBulletContent(table, item.contentDetail, item.contentDetailBulletType, fonts)
            p.add(table)
        }
    }

    // ── SC8 – Cover Letter ─────────────────────────────────────────────────────

    private fun buildCoverLetter(
        p: Paragraph,
        sha: SectionHeadAdded,
        sc8: com.nithra.nithraresume.data.model.SectionChild8,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        if (sc8.date.isNotEmpty()) {
            val table = PdfPTable(1)
            val cell = noBorderCell(Phrase(sc8.date, fonts.subFont), Element.ALIGN_RIGHT, 1)
            table.addCell(cell)
            p.add(table)
        }
        if (sc8.address.isNotEmpty()) {
            val table = PdfPTable(1)
            sc8.address.split("\n").forEach { line ->
                table.addCell(noBorderCell(Phrase(line, fonts.subFont), Element.ALIGN_LEFT, 1))
            }
            p.add(table)
        }
        if (sc8.content.isNotEmpty()) {
            val table = PdfPTable(1)
            sc8.content.split("\n").forEach { line ->
                val cell = noBorderCell(Phrase(line, fonts.subFont), Element.ALIGN_JUSTIFIED, 1)
                cell.setLeading(2f, 1.3f)
                table.addCell(cell)
            }
            p.add(table)
        }
    }

    // ── Section heading ────────────────────────────────────────────────────────

    private fun addSectionHeading(
        p: Paragraph,
        title: String,
        fonts: PdfFonts,
        fmt: String,
        bgColor: String
    ) {
        when (fmt) {
            "FUNCTIONAL", "SIMPLE" -> {
                val table = PdfPTable(floatArrayOf(10f, 5f))
                val titleCell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                titleCell.setLeading(2f, 1.5f)
                table.addCell(titleCell)
                val ruleCell = PdfPCell(Phrase("", fonts.subFont))
                ruleCell.setBorderColorTop(com.itextpdf.text.BaseColor.BLACK)
                ruleCell.colspan = 2; ruleCell.setBorder(0); ruleCell.borderWidthTop = 0.5f
                table.addCell(ruleCell)
                table.horizontalAlignment = Element.ALIGN_MIDDLE
                p.add(table)
            }
            "CLASSIC" -> {
                val table = PdfPTable(floatArrayOf(0.5f, 10f))
                val titleCell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 2)
                titleCell.setLeading(2f, 1.5f)
                table.addCell(titleCell)
                table.horizontalAlignment = Element.ALIGN_MIDDLE
                p.add(table)
            }
            "MODERN" -> {
                val headingFont = FontFactory.getFont(BASE_FONT_ALIAS, "", true,
                    (fonts.subFont.size + 1).toFloat(), Font.BOLD, COLOR_BLUE, BaseFont.EMBEDDED)
                val table = PdfPTable(floatArrayOf(10f, 5f))
                val titleCell = noBorderCell(Phrase(title, headingFont), Element.ALIGN_LEFT, 2)
                titleCell.setLeading(2f, 1.5f)
                table.addCell(titleCell)
                val ruleCell = PdfPCell(Phrase("", headingFont))
                ruleCell.setBorderColorTop(com.itextpdf.text.BaseColor.BLACK)
                ruleCell.colspan = 2; ruleCell.setBorder(0); ruleCell.borderWidthTop = 0.5f
                table.addCell(ruleCell)
                table.horizontalAlignment = Element.ALIGN_MIDDLE
                p.add(table)
            }
            "HARVARD" -> {
                val table = PdfPTable(floatArrayOf(3f, 10f))
                val titleCell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 1)
                titleCell.paddingTop = 10f
                table.addCell(titleCell)
                // Content will be nested; add empty right cell for now
                val emptyCell = noBorderCell(Phrase("", fonts.subFont), Element.ALIGN_LEFT, 1)
                table.addCell(emptyCell)
                table.defaultCell.setBorder(0)
                p.add(table)
            }
            "GRAYSCALE" -> {
                val table = PdfPTable(floatArrayOf(0.4f, 10f))
                addEmptyLine(p)
                val grayCell = PdfPCell(Phrase("", fonts.subFont))
                grayCell.backgroundColor = COLOR_GRAY
                grayCell.setBorder(Rectangle.NO_BORDER)
                table.addCell(grayCell)
                val nested = PdfPTable(floatArrayOf(0.4f, 10f))
                val emptyNested = PdfPCell(Phrase("", fonts.headingFont))
                emptyNested.setBorder(Rectangle.NO_BORDER)
                nested.addCell(emptyNested)
                val titleCell = noBorderCell(Phrase(title, fonts.headingFont), Element.ALIGN_LEFT, 1)
                nested.addCell(titleCell)
                table.defaultCell.setBorder(0)
                table.addCell(nested)
                p.add(table)
            }
        }
    }

    // ── Low-level helpers ──────────────────────────────────────────────────────

    private fun addBulletContent(
        table: PdfPTable,
        content: String,
        bulletType: String,
        fonts: PdfFonts
    ) {
        if (content.isEmpty()) return
        val hasBullet = !bulletType.equals(BULLET_NONE, ignoreCase = true)
        content.split("\n").forEach { line ->
            if (line.isBlank()) return@forEach
            if (hasBullet) {
                val bulletCell = PdfPCell(Phrase("    •"))
                bulletCell.horizontalAlignment = Element.ALIGN_CENTER
                bulletCell.setBorder(Color.WHITE)
                bulletCell.setLeading(0f, 1.5f)
                bulletCell.colspan = 1
                table.addCell(bulletCell)
                val contentCell = noBorderCell(Phrase(line, fonts.subFont), Element.ALIGN_JUSTIFIED, 1)
                contentCell.setLeading(1f, 1.5f)
                table.addCell(contentCell)
            } else {
                val cell = noBorderCell(Phrase(line, fonts.subFont), Element.ALIGN_JUSTIFIED, 2)
                cell.setLeading(2f, 1.5f)
                table.addCell(cell)
                val empty = noBorderCell(Phrase("", fonts.subFont), Element.ALIGN_LEFT, 2)
                table.addCell(empty)
            }
        }
    }

    private fun addContactRows(
        table: PdfPTable,
        sc1: com.nithra.nithraresume.data.model.SectionChild1,
        fonts: PdfFonts,
        alignment: Int,
        colspan: Int
    ) {
        listOf(sc1.address, sc1.phone, sc1.email).forEach { value ->
            val cell = PdfPCell(Phrase(value, fonts.addressFont))
            cell.horizontalAlignment = alignment
            cell.setBorder(Color.WHITE)
            cell.colspan = colspan
            table.addCell(cell)
        }
        val genderDob = buildInlineText(sc1.gender, sc1.dob, "  |  ")
        if (genderDob.isNotEmpty()) {
            val cell = PdfPCell(Phrase(genderDob, fonts.addressFont))
            cell.horizontalAlignment = alignment
            cell.setBorder(Color.WHITE)
            cell.colspan = colspan
            table.addCell(cell)
        }
    }

    private fun addNameCell(
        table: PdfPTable,
        name: String,
        font: Font,
        alignment: Int
    ) {
        val cell = PdfPCell(Phrase(name, font))
        cell.horizontalAlignment = alignment
        cell.setBorder(Color.WHITE)
        table.addCell(cell)
    }

    private fun addRuleCell(table: PdfPTable, colspan: Int, paddingBottom: Float) {
        val cell = PdfPCell(Phrase("", FontFactory.getFont(BASE_FONT_ALIAS, 1f)))
        cell.setBorderColorTop(com.itextpdf.text.BaseColor.BLACK)
        cell.colspan = colspan; cell.setBorder(0)
        cell.borderWidthTop = 0.5f; cell.paddingBottom = paddingBottom
        table.addCell(cell)
    }

    private fun addBoldCell(
        table: PdfPTable,
        text: String,
        font: Font,
        alignment: Int,
        colspan: Int
    ) {
        val cell = noBorderCell(Phrase(text, font), alignment, colspan)
        table.addCell(cell)
    }

    private fun noBorderCell(phrase: Phrase, alignment: Int, colspan: Int): PdfPCell {
        val cell = PdfPCell(phrase)
        cell.horizontalAlignment = alignment
        cell.setBorder(Color.WHITE)
        cell.colspan = colspan
        return cell
    }

    private fun addEmptyLine(p: Paragraph) {
        p.add(Paragraph(" "))
    }

    private fun buildInlineText(a: String, b: String, separator: String): String {
        return when {
            a.isNotEmpty() && b.isNotEmpty() -> "$a$separator$b"
            a.isNotEmpty() -> a
            b.isNotEmpty() -> b
            else -> ""
        }
    }

    private fun buildClassicItemLine(
        role: String, subtitle: String, company: String, period: String
    ): String {
        val sb = StringBuilder()
        if (role.isNotEmpty()) sb.append(role)
        if (subtitle.isNotEmpty()) sb.append(", $subtitle")
        if (company.isNotEmpty()) sb.append(", $company")
        if (period.isNotEmpty()) sb.append(" --  $period")
        return sb.toString()
    }

    // ── Font helpers ───────────────────────────────────────────────────────────

    private data class PdfFonts(
        val nameFont: Font,
        val headingFont: Font,
        val subFont: Font,
        val subBoldFont: Font,
        val addressFont: Font
    )

    private fun buildFonts(fontSize: Int): PdfFonts {
        fun font(size: Float, style: Int, color: BaseColor? = null) =
            FontFactory.getFont(BASE_FONT_ALIAS, "", true, size, style, color, BaseFont.EMBEDDED)

        return PdfFonts(
            nameFont     = font((fontSize + 12).toFloat(), Font.BOLD),
            headingFont  = font((fontSize + 1).toFloat(), Font.BOLD),
            subFont      = font(fontSize.toFloat(), Font.NORMAL),
            subBoldFont  = font(fontSize.toFloat(), Font.BOLD),
            addressFont  = font((fontSize - 2).coerceAtLeast(6).toFloat(), Font.ITALIC)
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
