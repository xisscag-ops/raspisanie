package ru.ngieu.raspisanie.ui.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.data.ActorKind
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.PaperPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import ru.ngieu.raspisanie.ui.theme.TerminalPalette
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ScheduleShareRenderer {
    private const val IMAGE_WIDTH = 1080
    private const val OUTER_PAD = 40f
    private const val TIMELINE_X = 56f
    private const val TIME_COL_X = 82f
    private const val CARD_X = 192f
    private const val CARD_WIDTH = IMAGE_WIDTH - OUTER_PAD - CARD_X
    private const val CARD_PAD_X = 24f
    private const val CARD_PAD_Y = 20f

    private data class ThemeColors(
        val isNeon: Boolean,
        val isGlass: Boolean,
        val isAero: Boolean,
        val isTerminal: Boolean,
        val isPaper: Boolean,
        val isDark: Boolean,
        val bgBase: Int,
        val cardTop: Int,
        val cardBottom: Int,
        val cardBorder: Int,
        val changeCardTop: Int,
        val changeCardBottom: Int,
        val changeBorder: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val divider: Int,
        val timelineLine: Int,
        val timelineDot: Int,
        val todayAccent: Int,
        val pillBg: Int,
        val pillText: Int,
    )

    private data class BadgeSpec(
        val text: String,
        val bgColor: Int,
        val textColor: Int,
    )

    private data class MeasuredLessonBlock(
        val badges: List<BadgeSpec>,
        val badgeHeight: Float,
        val subjectLayout: StaticLayout,
        val metaLayout: StaticLayout?,
        val totalHeight: Float,
    )

    private data class MeasuredSlot(
        val slot: LessonSlot,
        val blocks: List<MeasuredLessonBlock>,
        val cardHeight: Float,
    )

    suspend fun renderAndShareDay(
        context: Context,
        snapshot: ShareDaySnapshot,
        todayDate: LocalDate,
        themeMode: ThemeModeSetting,
        updatedAtEpochMillis: Long?,
    ) {
        val pngFile = withContext(Dispatchers.Default) {
            renderToCachePng(
                context = context,
                snapshot = snapshot,
                todayDate = todayDate,
                themeMode = themeMode,
                updatedAtEpochMillis = updatedAtEpochMillis,
            )
        }
        launchShareSheet(context, pngFile, snapshot)
    }

    private fun renderToCachePng(
        context: Context,
        snapshot: ShareDaySnapshot,
        todayDate: LocalDate,
        themeMode: ThemeModeSetting,
        updatedAtEpochMillis: Long?,
    ): File {
        val colors = resolveThemeColors(context, themeMode)

        val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 40f
            typeface = Typeface.create(if (colors.isPaper) Typeface.SERIF else Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val dayCountPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val timeStartPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val timeEndPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subjectPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 28f
            typeface = Typeface.create(if (colors.isPaper) Typeface.SERIF else Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val countPillText = ScheduleShareBuilder.formatPairsCount(snapshot.pairsCount)
        val countPillW = badgeTextPaint.measureText(countPillText) + 28f
        val headerTitleMaxW = ((IMAGE_WIDTH - OUTER_PAD * 2) - 56f - countPillW - 18f).toInt().coerceAtLeast(360)
        val headerTitleLayout = StaticLayout.Builder
            .obtain(snapshot.actorTitle, 0, snapshot.actorTitle.length, headerTitlePaint, headerTitleMaxW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.0f)
            .setIncludePad(false)
            .build()

        val headerCardHeight = (28f + headerTitleLayout.height + 12f + 34f + 26f).coerceAtLeast(144f)

        val innerCardWidth = (CARD_WIDTH - CARD_PAD_X * 2).toInt().coerceAtLeast(200)
        val measuredSlots = snapshot.slots.map { slot ->
            val isMultiSubgroup = slot.lessons.size > 1
            val blocks = slot.lessons.map { lesson ->
                measureLessonBlock(
                    lesson = lesson,
                    isMultiSubgroup = isMultiSubgroup,
                    actorKind = snapshot.actorKind,
                    colors = colors,
                    innerWidth = innerCardWidth,
                    badgeTextPaint = badgeTextPaint,
                    subjectPaint = subjectPaint,
                    metaPaint = metaPaint,
                )
            }
            val blocksSum = blocks.sumOf { it.totalHeight.toDouble() }.toFloat()
            val dividersHeight = if (blocks.size > 1) (blocks.size - 1) * 26f else 0f
            val cardHeight = (CARD_PAD_Y * 2 + blocksSum + dividersHeight).coerceAtLeast(108f)
            MeasuredSlot(slot = slot, blocks = blocks, cardHeight = cardHeight)
        }

        val bodyHeight = if (measuredSlots.isEmpty()) {
            132f
        } else {
            measuredSlots.sumOf { (it.cardHeight + 14f).toDouble() }.toFloat() - 14f
        }

        val topSpace = 44f
        val betweenHeaderAndBody = 26f
        val footerHeight = 68f
        val bottomPad = 34f
        val totalHeight = (topSpace + headerCardHeight + betweenHeaderAndBody + bodyHeight + footerHeight + bottomPad)
            .toInt()
            .coerceAtMost(10_000)

        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(context, canvas, IMAGE_WIDTH.toFloat(), totalHeight.toFloat(), colors)

        var y = topSpace

        // Верхняя карточка дня
        drawHeaderCard(
            canvas = canvas,
            y = y,
            height = headerCardHeight,
            snapshot = snapshot,
            isToday = snapshot.date == todayDate,
            titleLayout = headerTitleLayout,
            countPillText = countPillText,
            colors = colors,
            subPaint = headerSubPaint,
            badgePaint = badgeTextPaint,
        )
        y += headerCardHeight + betweenHeaderAndBody

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.timelineLine
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.divider
            strokeWidth = 1.6f
        }

        if (measuredSlots.isEmpty()) {
            val emptyRect = RectF(OUTER_PAD, y, IMAGE_WIDTH - OUTER_PAD, y + bodyHeight)
            drawGlassBox(canvas, emptyRect, 24f, colors, isChange = false, fillPaint = fillPaint, strokePaint = strokePaint)
            val emptyLabel = "В этот день пар нет"
            val labelW = dayCountPaint.measureText(emptyLabel)
            canvas.drawText(
                emptyLabel,
                (IMAGE_WIDTH - labelW) / 2f,
                y + bodyHeight / 2f + 9f,
                dayCountPaint,
            )
            y += bodyHeight
        } else {
            measuredSlots.forEachIndexed { slotIndex, measuredSlot ->
                val slotTop = y
                val slotCenterY = slotTop + measuredSlot.cardHeight / 2f
                val isFirst = slotIndex == 0
                val isLast = slotIndex == measuredSlots.lastIndex

                if (!isFirst) {
                    canvas.drawLine(TIMELINE_X, slotTop - 14f, TIMELINE_X, slotCenterY, linePaint)
                }
                if (!isLast) {
                    canvas.drawLine(TIMELINE_X, slotCenterY, TIMELINE_X, slotTop + measuredSlot.cardHeight, linePaint)
                }

                val hasAnyChange = measuredSlot.slot.lessons.any { it.isChange }
                val hasAnyCancelled = measuredSlot.slot.lessons.any { it.isCancelled }
                val isStandardDark = !colors.isNeon && !colors.isGlass && !colors.isAero && !colors.isTerminal && !colors.isPaper && colors.isDark
                val dotColor = when {
                    hasAnyCancelled -> when {
                        isStandardDark -> ScheduleAccentColors.cancelBadgeDark.toArgb()
                        colors.isDark -> ScheduleAccentColors.cancelBorderNeon.toArgb()
                        else -> ScheduleAccentColors.cancelBorderLight.toArgb()
                    }
                    hasAnyChange -> if (isStandardDark) ScheduleAccentColors.changeBadgeDark.toArgb() else Color.parseColor("#FBBF24")
                    else -> colors.timelineDot
                }

                fillPaint.shader = null
                if (!colors.isPaper) {
                    fillPaint.color = Color.argb(72, Color.red(dotColor), Color.green(dotColor), Color.blue(dotColor))
                    canvas.drawCircle(TIMELINE_X, slotCenterY, 13f, fillPaint)
                }
                fillPaint.color = dotColor
                canvas.drawCircle(TIMELINE_X, slotCenterY, 8.5f, fillPaint)
                fillPaint.color = if (colors.isPaper) PaperPalette.surfaceElevated.toArgb() else Color.WHITE
                canvas.drawCircle(TIMELINE_X, slotCenterY, 3.8f, fillPaint)

                val timeParts = measuredSlot.slot.timeRangeText.split("–").map { it.trim() }
                if (timeParts.size == 2) {
                    canvas.drawText(timeParts[0], TIME_COL_X, slotCenterY - 6f, timeStartPaint)
                    canvas.drawText(timeParts[1], TIME_COL_X, slotCenterY + 24f, timeEndPaint)
                } else {
                    canvas.drawText(measuredSlot.slot.timeRangeText, TIME_COL_X, slotCenterY + 8f, timeEndPaint)
                }

                val cardRect = RectF(CARD_X, slotTop, IMAGE_WIDTH - OUTER_PAD, slotTop + measuredSlot.cardHeight)
                drawGlassBox(
                    canvas = canvas,
                    rect = cardRect,
                    radius = 26f,
                    colors = colors,
                    isChange = hasAnyChange,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                )

                var innerY = slotTop + CARD_PAD_Y
                val contentLeft = CARD_X + CARD_PAD_X
                measuredSlot.blocks.forEachIndexed { blockIdx, block ->
                    if (blockIdx > 0) {
                        val divY = innerY + 12f
                        canvas.drawLine(
                            contentLeft,
                            divY,
                            IMAGE_WIDTH - OUTER_PAD - CARD_PAD_X,
                            divY,
                            dividerPaint,
                        )
                        innerY += 26f
                    }

                    var badgeX = contentLeft
                    var badgeY = innerY
                    val maxRight = IMAGE_WIDTH - OUTER_PAD - CARD_PAD_X
                    val pillH = 32f
                    for (badge in block.badges) {
                        val pillW = badgeTextPaint.measureText(badge.text) + 24f
                        if (badgeX + pillW > maxRight && badgeX > contentLeft) {
                            badgeX = contentLeft
                            badgeY += pillH + 8f
                        }
                        drawPill(
                            canvas = canvas,
                            left = badgeX,
                            top = badgeY,
                            text = badge.text,
                            bgColor = badge.bgColor,
                            textColor = badge.textColor,
                            textPaint = badgeTextPaint,
                        )
                        badgeX += pillW + 10f
                    }

                    val textTop = innerY + block.badgeHeight + 10f
                    canvas.save()
                    canvas.translate(contentLeft, textTop)
                    block.subjectLayout.draw(canvas)
                    canvas.restore()

                    if (block.metaLayout != null) {
                        val metaTop = textTop + block.subjectLayout.height + 8f
                        canvas.save()
                        canvas.translate(contentLeft, metaTop)
                        block.metaLayout.draw(canvas)
                        canvas.restore()
                    }

                    innerY += block.totalHeight
                }

                y += measuredSlot.cardHeight + 14f
            }
            y -= 14f
        }

        // Футер
        val timestampMillis = updatedAtEpochMillis ?: System.currentTimeMillis()
        val timeText = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))
        val footerText = "НГИЭУ • ${snapshot.actorBadgeName} • Данные от $timeText (МСК)"
        val footerW = footerPaint.measureText(footerText)
        canvas.drawText(footerText, (IMAGE_WIDTH - footerW) / 2f, (y + 42f).coerceAtMost(totalHeight - 24f), footerPaint)

        val sharesDir = File(context.cacheDir, "shares").apply { mkdirs() }
        sharesDir.listFiles()?.forEach { it.delete() }
        val safeName = snapshot.actorBadgeName.replace(Regex("""[^\p{L}\p{N}_\-]+"""), "_")
        val file = File(sharesDir, "raspisanie-${safeName}-${snapshot.date}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    private fun launchShareSheet(
        context: Context,
        pngFile: File,
        snapshot: ShareDaySnapshot,
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pngFile,
        )
        val label = "Расписание ${snapshot.actorBadgeName} на ${snapshot.dayTitle}"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, label)
            clipData = ClipData.newRawUri(label, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Поделиться расписанием").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun measureLessonBlock(
        lesson: Lesson,
        isMultiSubgroup: Boolean,
        actorKind: ActorKind,
        colors: ThemeColors,
        innerWidth: Int,
        badgeTextPaint: TextPaint,
        subjectPaint: TextPaint,
        metaPaint: TextPaint,
    ): MeasuredLessonBlock {
        val badges = buildList {
            val subNum = ScheduleEngine.extractSubgroupNumber(lesson)
            if (isMultiSubgroup || subNum != null) {
                val subLabel = subNum?.let { "$it подгруппа" } ?: "Подгруппа"
                add(
                    BadgeSpec(
                        text = subLabel,
                        bgColor = when {
                            colors.isNeon -> Color.argb(72, 139, 92, 246)
                            colors.isGlass -> Color.argb(97, 149, 149, 149)
                            colors.isAero -> Color.argb(130, 29, 110, 166)
                            colors.isTerminal -> Color.argb(215, 20, 68, 26)
                            colors.isPaper -> PaperPalette.secondaryPillBg.toArgb()
                            else -> colors.pillBg
                        },
                        textColor = when {
                            colors.isNeon -> Color.parseColor("#E9D5FF")
                            colors.isGlass -> Color.parseColor("#CDCDCD")
                            colors.isAero -> Color.parseColor("#EAF7FF")
                            colors.isTerminal -> Color.parseColor("#96FF91")
                            colors.isPaper -> PaperPalette.textPrimary.toArgb()
                            else -> colors.pillText
                        },
                    ),
                )
            }

            val typeColor = ScheduleAccentColors.lessonTypeColor(lesson.lessonType, colors.isDark).toArgb()
            add(
                BadgeSpec(
                    text = lesson.lessonType.label,
                    bgColor = when {
                        colors.isGlass -> Color.argb(133, 191, 191, 191)
                        colors.isAero -> Color.argb(235, 131, 196, 233)
                        colors.isTerminal -> Color.argb(215, 14, 52, 18)
                        colors.isPaper -> PaperPalette.activeFill.toArgb()
                        colors.isNeon -> Color.parseColor("#28325C")
                        colors.isDark -> Color.parseColor("#283244")
                        else -> Color.parseColor("#E9EEF5")
                    },
                    textColor = when {
                        colors.isGlass -> Color.parseColor("#F8F8F8")
                        colors.isAero -> Color.parseColor("#07375D")
                        colors.isTerminal -> Color.parseColor("#96FF91")
                        colors.isPaper -> PaperPalette.activeText.toArgb()
                        colors.isNeon -> Color.parseColor("#E2E8F0")
                        colors.isDark -> Color.parseColor("#E4E7EC")
                        else -> typeColor
                    },
                ),
            )

            if (lesson.isChange) {
                val isStandardDark = !colors.isNeon && !colors.isGlass && !colors.isAero && !colors.isTerminal && !colors.isPaper && colors.isDark
                add(
                    BadgeSpec(
                        text = "Замена",
                        bgColor = if (isStandardDark) {
                            ScheduleAccentColors.changeBadgeBgDark.toArgb()
                        } else {
                            Color.argb(56, 251, 191, 36)
                        },
                        textColor = when {
                            isStandardDark -> ScheduleAccentColors.changeBadgeDark.toArgb()
                            colors.isDark -> Color.parseColor("#FBBF24")
                            else -> Color.parseColor("#B45309")
                        },
                    ),
                )
            }

            if (lesson.isCancelled) {
                val isStandardDark = !colors.isNeon && !colors.isGlass && !colors.isAero && !colors.isTerminal && !colors.isPaper && colors.isDark
                add(
                    BadgeSpec(
                        text = "Отмена",
                        bgColor = when {
                            isStandardDark -> ScheduleAccentColors.cancelBadgeBgDark.toArgb()
                            colors.isDark -> ScheduleAccentColors.cancelBadgeBgNeon.toArgb()
                            else -> ScheduleAccentColors.cancelBadgeBgLight.toArgb()
                        },
                        textColor = when {
                            isStandardDark -> ScheduleAccentColors.cancelBadgeDark.toArgb()
                            colors.isDark -> ScheduleAccentColors.cancelBadgeNeon.toArgb()
                            else -> ScheduleAccentColors.cancelBadgeLight.toArgb()
                        },
                    ),
                )
            }
        }

        var badgeX = 0f
        var rows = 1
        val pillH = 32f
        for (badge in badges) {
            val pillW = badgeTextPaint.measureText(badge.text) + 24f
            if (badgeX + pillW > innerWidth && badgeX > 0f) {
                badgeX = 0f
                rows++
            }
            badgeX += pillW + 10f
        }
        val badgeHeight = rows * pillH + (rows - 1) * 8f

        val subjectText = if (isMultiSubgroup || ScheduleEngine.extractSubgroupNumber(lesson) != null) {
            ScheduleEngine.cleanSubgroupPrefix(lesson.subject)
        } else {
            lesson.subject
        }
        val subjectLayout = StaticLayout.Builder
            .obtain(subjectText, 0, subjectText.length, subjectPaint, innerWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.0f)
            .setIncludePad(false)
            .build()

        val metaParts = buildList {
            if (lesson.room.isNotBlank()) add("ауд. ${lesson.room}")
            if (actorKind == ActorKind.TEACHER && lesson.groups.isNotEmpty()) {
                add("гр. ${lesson.groups.joinToString(", ")}")
            } else if (lesson.teachers.isNotEmpty()) {
                add(lesson.teachers.joinToString(", "))
            }
            val note = lesson.note.trim()
            if (note.isNotEmpty() && !note.equals(lesson.lessonType.label, ignoreCase = true)) {
                add(note)
            }
        }
        val metaText = metaParts.joinToString("  •  ")
        val metaLayout = if (metaText.isNotBlank()) {
            StaticLayout.Builder
                .obtain(metaText, 0, metaText.length, metaPaint, innerWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.0f)
                .setIncludePad(false)
                .build()
        } else {
            null
        }

        val totalHeight = badgeHeight + 10f + subjectLayout.height +
            (if (metaLayout != null) 8f + metaLayout.height else 0f)

        return MeasuredLessonBlock(
            badges = badges,
            badgeHeight = badgeHeight,
            subjectLayout = subjectLayout,
            metaLayout = metaLayout,
            totalHeight = totalHeight,
        )
    }

    private fun drawBackground(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        colors: ThemeColors,
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.bgBase
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        if (colors.isGlass || colors.isAero) {
            val wallRes = if (colors.isAero) R.drawable.bg_aero_wallpaper else R.drawable.bg_glass_waves
            val wall = BitmapFactory.decodeResource(context.resources, wallRes)
            if (wall != null) {
                val scale = maxOf(width / wall.width.toFloat(), height / wall.height.toFloat())
                val drawnW = wall.width * scale
                val drawnH = wall.height * scale
                val left = (width - drawnW) / 2f
                val top = (height - drawnH) / 2f
                canvas.drawBitmap(
                    wall,
                    Rect(0, 0, wall.width, wall.height),
                    RectF(left, top, left + drawnW, top + drawnH),
                    Paint(Paint.FILTER_BITMAP_FLAG),
                )
                wall.recycle()
            }
            return
        }

        if (colors.isTerminal) {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * 0.5f,
                    height * 0.34f,
                    width * 0.92f,
                    intArrayOf(Color.parseColor("#0C2711"), Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawRect(0f, 0f, width, height, glowPaint)

            val scanPaint = Paint().apply {
                color = Color.argb(28, 0, 0, 0)
                strokeWidth = 2f
            }
            var sy = 0f
            while (sy < height) {
                canvas.drawLine(0f, sy, width, sy, scanPaint)
                sy += 5f
            }
            return
        }

        if (!colors.isNeon) return

        val topVioletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.12f,
                260f,
                width * 0.78f,
                intArrayOf(Color.argb(66, 147, 51, 234), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, topVioletPaint)

        val rightCyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.92f,
                height * 0.55f,
                width * 0.72f,
                intArrayOf(Color.argb(56, 14, 165, 233), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, rightCyanPaint)
    }

    private fun drawHeaderCard(
        canvas: Canvas,
        y: Float,
        height: Float,
        snapshot: ShareDaySnapshot,
        isToday: Boolean,
        titleLayout: StaticLayout,
        countPillText: String,
        colors: ThemeColors,
        subPaint: TextPaint,
        badgePaint: TextPaint,
    ) {
        val rect = RectF(OUTER_PAD, y, IMAGE_WIDTH - OUTER_PAD, y + height)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                when {
                    colors.isNeon -> intArrayOf(Color.parseColor("#C7242D58"), Color.parseColor("#BA141B38"))
                    colors.isGlass -> intArrayOf(Color.parseColor("#8C737373"), Color.parseColor("#782C2C2C"))
                    colors.isAero -> intArrayOf(Color.parseColor("#DF338AC0"), Color.parseColor("#E307558D"))
                    colors.isTerminal -> intArrayOf(Color.parseColor("#DF0E2E12"), Color.parseColor("#EB061608"))
                    else -> intArrayOf(colors.cardTop, colors.cardBottom)
                },
                null,
                Shader.TileMode.CLAMP,
            )
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = colors.cardBorder
        }
        canvas.drawRoundRect(rect, 30f, 30f, fillPaint)
        canvas.drawRoundRect(rect, 30f, 30f, strokePaint)

        canvas.save()
        canvas.translate(rect.left + 28f, rect.top + 28f)
        titleLayout.draw(canvas)
        canvas.restore()

        val subtitleText = "${snapshot.dayTitle} • ${snapshot.weekFilter.title} неделя"
        val subBaseline = rect.top + 28f + titleLayout.height + 34f
        canvas.drawText(subtitleText, rect.left + 28f, subBaseline, subPaint)

        if (isToday) {
            val subW = subPaint.measureText(subtitleText)
            drawPill(
                canvas = canvas,
                left = rect.left + 28f + subW + 14f,
                top = subBaseline - 24f,
                text = "Сегодня",
                bgColor = when {
                    colors.isGlass -> Color.argb(133, 191, 191, 191)
                    colors.isAero -> Color.argb(235, 131, 196, 233)
                    colors.isTerminal -> Color.argb(225, 22, 72, 27)
                    colors.isPaper -> PaperPalette.activeFill.toArgb()
                    else -> Color.argb(56, 56, 189, 248)
                },
                textColor = colors.todayAccent,
                textPaint = badgePaint,
            )
        }

        val pillW = badgePaint.measureText(countPillText) + 28f
        drawPill(
            canvas = canvas,
            left = rect.right - 26f - pillW,
            top = rect.top + 28f,
            text = countPillText,
            bgColor = colors.pillBg,
            textColor = colors.pillText,
            textPaint = badgePaint,
        )
    }

    private fun drawGlassBox(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        colors: ThemeColors,
        isChange: Boolean,
        fillPaint: Paint,
        strokePaint: Paint,
    ) {
        fillPaint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.left,
            rect.bottom,
            if (isChange) colors.changeCardTop else colors.cardTop,
            if (isChange) colors.changeCardBottom else colors.cardBottom,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(rect, radius, radius, fillPaint)

        strokePaint.color = if (isChange) colors.changeBorder else colors.cardBorder
        strokePaint.strokeWidth = if (isChange) 2.2f else 1.5f
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    private fun drawPill(
        canvas: Canvas,
        left: Float,
        top: Float,
        text: String,
        bgColor: Int,
        textColor: Int,
        textPaint: TextPaint,
    ) {
        val textW = textPaint.measureText(text)
        val rect = RectF(left, top, left + textW + 24f, top + 32f)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(rect, 11f, 11f, bgPaint)
        textPaint.color = textColor
        canvas.drawText(text, left + 12f, top + 22.5f, textPaint)
    }

    private fun resolveThemeColors(context: Context, themeMode: ThemeModeSetting): ThemeColors {
        val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val isNeon = themeMode == ThemeModeSetting.NEON
        val isGlass = themeMode == ThemeModeSetting.GLASS
        val isAero = themeMode == ThemeModeSetting.AERO
        val isTerminal = themeMode == ThemeModeSetting.TERMINAL
        val isPaper = themeMode == ThemeModeSetting.PAPER
        val isDark = when (themeMode) {
            ThemeModeSetting.NEON,
            ThemeModeSetting.GLASS,
            ThemeModeSetting.AERO,
            ThemeModeSetting.TERMINAL,
            ThemeModeSetting.DARK,
            -> true
            ThemeModeSetting.PAPER,
            ThemeModeSetting.LIGHT,
            -> false
            ThemeModeSetting.SYSTEM -> systemDark
        }

        return when {
            isNeon -> ThemeColors(
                isNeon = true,
                isGlass = false,
                isAero = false,
                isTerminal = false,
                isPaper = false,
                isDark = true,
                bgBase = NeonPalette.backgroundBase.toArgb(),
                cardTop = Color.parseColor("#B51D254B"),
                cardBottom = Color.parseColor("#A6131935"),
                cardBorder = Color.argb(68, 165, 180, 252),
                changeCardTop = Color.parseColor("#BD3D260A"),
                changeCardBottom = Color.parseColor("#B3271907"),
                changeBorder = Color.parseColor("#FBBF24"),
                textPrimary = NeonPalette.textPrimary.toArgb(),
                textSecondary = NeonPalette.textSecondary.toArgb(),
                divider = Color.argb(46, 255, 255, 255),
                timelineLine = Color.argb(180, 124, 58, 237),
                timelineDot = NeonPalette.timelinePurple.toArgb(),
                todayAccent = Color.parseColor("#38BDF8"),
                pillBg = Color.argb(86, 147, 51, 234),
                pillText = Color.parseColor("#E9D5FF"),
            )
            isGlass -> ThemeColors(
                isNeon = false,
                isGlass = true,
                isAero = false,
                isTerminal = false,
                isPaper = false,
                isDark = true,
                bgBase = GlassPalette.backgroundBase.toArgb(),
                cardTop = Color.parseColor("#858A8A8A"),
                cardBottom = Color.parseColor("#6E272727"),
                cardBorder = Color.argb(135, 244, 244, 244),
                changeCardTop = Color.parseColor("#A64E3818"),
                changeCardBottom = Color.parseColor("#94291D0C"),
                changeBorder = Color.parseColor("#FBBF24"),
                textPrimary = GlassPalette.textPrimary.toArgb(),
                textSecondary = GlassPalette.textSecondary.toArgb(),
                divider = Color.argb(56, 244, 244, 244),
                timelineLine = Color.argb(179, 157, 157, 157),
                timelineDot = GlassPalette.timelineDot.toArgb(),
                todayAccent = Color.parseColor("#FAFAFA"),
                pillBg = Color.argb(133, 191, 191, 191),
                pillText = Color.parseColor("#F8F8F8"),
            )
            isAero -> ThemeColors(
                isNeon = false,
                isGlass = false,
                isAero = true,
                isTerminal = false,
                isPaper = false,
                isDark = true,
                bgBase = AeroPalette.backgroundBase.toArgb(),
                cardTop = AeroPalette.cardFillTop.toArgb(),
                cardBottom = AeroPalette.cardFillBottom.toArgb(),
                cardBorder = AeroPalette.borderTop.toArgb(),
                changeCardTop = AeroPalette.changeLessonTop.toArgb(),
                changeCardBottom = AeroPalette.changeLessonBottom.toArgb(),
                changeBorder = Color.parseColor("#FBBF24"),
                textPrimary = AeroPalette.textPrimary.toArgb(),
                textSecondary = AeroPalette.textSecondary.toArgb(),
                divider = Color.argb(82, 188, 239, 254),
                timelineLine = Color.argb(230, 208, 240, 255),
                timelineDot = AeroPalette.timelineDot.toArgb(),
                todayAccent = Color.parseColor("#07375D"),
                pillBg = Color.argb(235, 131, 196, 233),
                pillText = Color.parseColor("#07375D"),
            )
            isTerminal -> ThemeColors(
                isNeon = false,
                isGlass = false,
                isAero = false,
                isTerminal = true,
                isPaper = false,
                isDark = true,
                bgBase = TerminalPalette.backgroundBase.toArgb(),
                cardTop = TerminalPalette.cardFillTop.toArgb(),
                cardBottom = TerminalPalette.cardFillBottom.toArgb(),
                cardBorder = TerminalPalette.borderTop.toArgb(),
                changeCardTop = TerminalPalette.changeLessonTop.toArgb(),
                changeCardBottom = TerminalPalette.changeLessonBottom.toArgb(),
                changeBorder = Color.parseColor("#FBBF24"),
                textPrimary = TerminalPalette.textPrimary.toArgb(),
                textSecondary = TerminalPalette.textSecondary.toArgb(),
                divider = TerminalPalette.divider.toArgb(),
                timelineLine = TerminalPalette.timelineLine.toArgb(),
                timelineDot = TerminalPalette.timelineDot.toArgb(),
                todayAccent = TerminalPalette.textPrimary.toArgb(),
                pillBg = Color.argb(225, 22, 72, 27),
                pillText = TerminalPalette.textPrimary.toArgb(),
            )
            isPaper -> ThemeColors(
                isNeon = false,
                isGlass = false,
                isAero = false,
                isTerminal = false,
                isPaper = true,
                isDark = false,
                bgBase = PaperPalette.backgroundBase.toArgb(),
                cardTop = PaperPalette.surfaceElevated.toArgb(),
                cardBottom = PaperPalette.surfaceElevated.toArgb(),
                cardBorder = PaperPalette.borderInk.toArgb(),
                changeCardTop = PaperPalette.changeLessonTop.toArgb(),
                changeCardBottom = PaperPalette.changeLessonBottom.toArgb(),
                changeBorder = Color.parseColor("#B45309"),
                textPrimary = PaperPalette.textPrimary.toArgb(),
                textSecondary = PaperPalette.textSecondary.toArgb(),
                divider = PaperPalette.divider.toArgb(),
                timelineLine = PaperPalette.timelineLine.toArgb(),
                timelineDot = PaperPalette.timelineDot.toArgb(),
                todayAccent = PaperPalette.activeText.toArgb(),
                pillBg = PaperPalette.activeFill.toArgb(),
                pillText = PaperPalette.activeText.toArgb(),
            )
            isDark -> ThemeColors(
                isNeon = false,
                isGlass = false,
                isAero = false,
                isTerminal = false,
                isPaper = false,
                isDark = true,
                bgBase = Color.parseColor("#0D111A"),
                cardTop = Color.parseColor("#151B26"),
                cardBottom = Color.parseColor("#151B26"),
                cardBorder = Color.parseColor("#242D3D"),
                changeCardTop = Color.parseColor("#151B26"),
                changeCardBottom = Color.parseColor("#151B26"),
                changeBorder = ScheduleAccentColors.changeBorderDark.toArgb(),
                textPrimary = Color.parseColor("#F2F4F7"),
                textSecondary = Color.parseColor("#98A2B3"),
                divider = Color.parseColor("#242D3D"),
                timelineLine = Color.argb(120, 148, 163, 184),
                timelineDot = Color.parseColor("#84ADFF"),
                todayAccent = Color.parseColor("#84ADFF"),
                pillBg = Color.parseColor("#0043A8"),
                pillText = Color.parseColor("#D9E6FF"),
            )
            else -> ThemeColors(
                isNeon = false,
                isGlass = false,
                isAero = false,
                isTerminal = false,
                isPaper = false,
                isDark = false,
                bgBase = Color.parseColor("#F6F8FC"),
                cardTop = Color.parseColor("#FFFFFF"),
                cardBottom = Color.parseColor("#FFFFFF"),
                cardBorder = Color.parseColor("#E4E7EC"),
                changeCardTop = Color.parseColor("#FFF4E5"),
                changeCardBottom = Color.parseColor("#FFF4E5"),
                changeBorder = Color.parseColor("#F79009"),
                textPrimary = Color.parseColor("#101828"),
                textSecondary = Color.parseColor("#475467"),
                divider = Color.parseColor("#E4E7EC"),
                timelineLine = Color.argb(120, 148, 163, 184),
                timelineDot = Color.parseColor("#155EEF"),
                todayAccent = Color.parseColor("#155EEF"),
                pillBg = Color.parseColor("#D9E6FF"),
                pillText = Color.parseColor("#001B4E"),
            )
        }
    }
}
