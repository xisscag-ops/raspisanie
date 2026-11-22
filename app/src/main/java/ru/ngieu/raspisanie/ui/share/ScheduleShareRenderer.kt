package ru.ngieu.raspisanie.ui.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
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
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
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

    private data class MeasuredDay(
        val section: ShareDaySection,
        val slots: List<MeasuredSlot>,
        val headerHeight: Float,
        val bodyHeight: Float,
    )

    suspend fun renderAndShareWeek(
        context: Context,
        snapshot: ShareWeekSnapshot,
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
        snapshot: ShareWeekSnapshot,
        todayDate: LocalDate,
        themeMode: ThemeModeSetting,
        updatedAtEpochMillis: Long?,
    ): File {
        val colors = resolveThemeColors(context, themeMode)

        val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val dayTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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

        val innerCardWidth = (CARD_WIDTH - CARD_PAD_X * 2).toInt().coerceAtLeast(200)
        val measuredDays = snapshot.days.map { section ->
            val measuredSlots = section.slots.map { slot ->
                val isMultiSubgroup = slot.lessons.size > 1
                val blocks = slot.lessons.map { lesson ->
                    measureLessonBlock(
                        lesson = lesson,
                        isMultiSubgroup = isMultiSubgroup,
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

            val dayBodyHeight = if (measuredSlots.isEmpty()) {
                74f
            } else {
                measuredSlots.sumOf { (it.cardHeight + 14f).toDouble() }.toFloat() - 14f
            }

            MeasuredDay(
                section = section,
                slots = measuredSlots,
                headerHeight = 46f,
                bodyHeight = dayBodyHeight,
            )
        }

        val headerCardHeight = 148f
        val topSpace = 44f
        val betweenHeaderAndDays = 28f
        val betweenDays = 26f
        val footerHeight = 64f
        val bottomPad = 36f

        val daysTotalHeight = measuredDays.sumOf { (it.headerHeight + 10f + it.bodyHeight + betweenDays).toDouble() }.toFloat()
        val rawTotalHeight = (topSpace + headerCardHeight + betweenHeaderAndDays + daysTotalHeight + footerHeight + bottomPad).toInt()
        val totalHeight = rawTotalHeight.coerceAtMost(14_000)

        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas, IMAGE_WIDTH.toFloat(), totalHeight.toFloat(), colors)

        var y = topSpace

        // Верхняя карточка недели
        drawHeaderCard(
            canvas = canvas,
            y = y,
            height = headerCardHeight,
            snapshot = snapshot,
            colors = colors,
            titlePaint = headerTitlePaint,
            subPaint = headerSubPaint,
            badgePaint = badgeTextPaint,
        )
        y += headerCardHeight + betweenHeaderAndDays

        // Дни недели Пн–Сб
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

        for (day in measuredDays) {
            if (y + day.headerHeight + day.bodyHeight > totalHeight - bottomPad) break

            val isToday = day.section.date == todayDate
            dayTitlePaint.color = if (isToday) colors.todayAccent else colors.textPrimary

            val titleBaseline = y + 32f
            canvas.drawText(day.section.dayTitle, OUTER_PAD, titleBaseline, dayTitlePaint)

            val countText = ScheduleShareBuilder.formatPairsCount(day.section.slots.size)
            val countWidth = dayCountPaint.measureText(countText)
            canvas.drawText(countText, IMAGE_WIDTH - OUTER_PAD - countWidth, titleBaseline, dayCountPaint)

            if (isToday) {
                val titleW = dayTitlePaint.measureText(day.section.dayTitle)
                drawPill(
                    canvas = canvas,
                    left = OUTER_PAD + titleW + 14f,
                    top = y + 7f,
                    text = "Сегодня",
                    bgColor = Color.argb(56, 56, 189, 248),
                    textColor = colors.todayAccent,
                    textPaint = badgeTextPaint,
                )
            }

            y += day.headerHeight + 10f

            if (day.slots.isEmpty()) {
                val emptyRect = RectF(OUTER_PAD, y, IMAGE_WIDTH - OUTER_PAD, y + day.bodyHeight)
                drawGlassBox(canvas, emptyRect, 22f, colors, isChange = false, fillPaint = fillPaint, strokePaint = strokePaint)
                canvas.drawText(
                    "Занятий нет",
                    OUTER_PAD + 24f,
                    y + day.bodyHeight / 2f + 9f,
                    dayCountPaint,
                )
                y += day.bodyHeight + betweenDays
                continue
            }

            day.slots.forEachIndexed { slotIndex, measuredSlot ->
                val slotTop = y
                val slotCenterY = slotTop + measuredSlot.cardHeight / 2f
                val isFirst = slotIndex == 0
                val isLast = slotIndex == day.slots.lastIndex

                // Вертикальная ось таймлайна
                if (!isFirst) {
                    canvas.drawLine(TIMELINE_X, slotTop - 14f, TIMELINE_X, slotCenterY, linePaint)
                }
                if (!isLast) {
                    canvas.drawLine(TIMELINE_X, slotCenterY, TIMELINE_X, slotTop + measuredSlot.cardHeight, linePaint)
                }

                val hasAnyChange = measuredSlot.slot.lessons.any { it.isChange }
                val dotColor = if (hasAnyChange) Color.parseColor("#FBBF24") else colors.timelineDot

                fillPaint.shader = null
                fillPaint.color = Color.argb(72, Color.red(dotColor), Color.green(dotColor), Color.blue(dotColor))
                canvas.drawCircle(TIMELINE_X, slotCenterY, 13f, fillPaint)
                fillPaint.color = dotColor
                canvas.drawCircle(TIMELINE_X, slotCenterY, 8.5f, fillPaint)
                fillPaint.color = Color.WHITE
                canvas.drawCircle(TIMELINE_X, slotCenterY, 3.8f, fillPaint)

                // Время пары
                val timeParts = measuredSlot.slot.timeRangeText.split("–").map { it.trim() }
                if (timeParts.size == 2) {
                    canvas.drawText(timeParts[0], TIME_COL_X, slotCenterY - 6f, timeStartPaint)
                    canvas.drawText(timeParts[1], TIME_COL_X, slotCenterY + 24f, timeEndPaint)
                } else {
                    canvas.drawText(measuredSlot.slot.timeRangeText, TIME_COL_X, slotCenterY + 8f, timeEndPaint)
                }

                // Стеклянная карточка пары
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

                    // Бейджи
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

            y += betweenDays - 14f
        }

        // Футер
        val timestampMillis = updatedAtEpochMillis ?: System.currentTimeMillis()
        val timeText = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))
        val footerText = "НГИЭУ • Группа ${snapshot.groupName} • Данные от $timeText (МСК)"
        val footerW = footerPaint.measureText(footerText)
        canvas.drawText(footerText, (IMAGE_WIDTH - footerW) / 2f, (y + 30f).coerceAtMost(totalHeight - 24f), footerPaint)

        val sharesDir = File(context.cacheDir, "shares").apply { mkdirs() }
        sharesDir.listFiles()?.forEach { it.delete() }
        val file = File(sharesDir, "raspisanie-${snapshot.groupName}-${snapshot.selectedMonday}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    private fun launchShareSheet(
        context: Context,
        pngFile: File,
        snapshot: ShareWeekSnapshot,
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pngFile,
        )
        val label = "Расписание ${snapshot.groupName} (${snapshot.weekFilter.title.lowercase()} неделя, ${snapshot.weekRangeLabel})"
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
                        bgColor = if (colors.isNeon) Color.argb(72, 139, 92, 246) else colors.pillBg,
                        textColor = if (colors.isNeon) Color.parseColor("#E9D5FF") else colors.pillText,
                    ),
                )
            }

            val typeColor = ScheduleAccentColors.lessonTypeColor(lesson.lessonType, colors.isDark).toArgb()
            add(
                BadgeSpec(
                    text = lesson.lessonType.label,
                    bgColor = Color.argb(48, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor)),
                    textColor = typeColor,
                ),
            )

            if (lesson.isChange) {
                add(
                    BadgeSpec(
                        text = "Замена",
                        bgColor = Color.argb(56, 251, 191, 36),
                        textColor = if (colors.isDark) Color.parseColor("#FBBF24") else Color.parseColor("#B45309"),
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

        val subjectText = if (isMultiSubgroup) {
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
            if (lesson.teachers.isNotEmpty()) add(lesson.teachers.joinToString(", "))
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
        canvas: Canvas,
        width: Float,
        height: Float,
        colors: ThemeColors,
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.bgBase
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        if (!colors.isNeon) return

        // Мягкие неоновые отсветы сверху и снизу (без тяжёлых размытий)
        val topVioletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.12f,
                280f,
                width * 0.78f,
                intArrayOf(Color.argb(66, 147, 51, 234), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height.coerceAtMost(1200f), topVioletPaint)

        val midCyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.92f,
                height * 0.45f,
                width * 0.72f,
                intArrayOf(Color.argb(56, 14, 165, 233), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width, height, midCyanPaint)

        val bottomPurplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.2f,
                height - 320f,
                width * 0.72f,
                intArrayOf(Color.argb(56, 124, 58, 237), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, (height - 1200f).coerceAtLeast(0f), width, height, bottomPurplePaint)
    }

    private fun drawHeaderCard(
        canvas: Canvas,
        y: Float,
        height: Float,
        snapshot: ShareWeekSnapshot,
        colors: ThemeColors,
        titlePaint: TextPaint,
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
                if (colors.isNeon) intArrayOf(Color.parseColor("#C7242D58"), Color.parseColor("#BA141B38"))
                else intArrayOf(colors.cardTop, colors.cardBottom),
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

        canvas.drawText("Группа ${snapshot.groupName}", rect.left + 28f, rect.top + 66f, titlePaint)
        canvas.drawText(
            "${snapshot.weekFilter.title} неделя • ${snapshot.weekRangeLabel}",
            rect.left + 28f,
            rect.top + 110f,
            subPaint,
        )

        val totalPill = "Всего: ${ScheduleShareBuilder.formatPairsCount(snapshot.totalPairsCount)}"
        val pillW = badgePaint.measureText(totalPill) + 28f
        drawPill(
            canvas = canvas,
            left = rect.right - 26f - pillW,
            top = rect.top + 32f,
            text = totalPill,
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
        val isDark = when (themeMode) {
            ThemeModeSetting.NEON, ThemeModeSetting.DARK -> true
            ThemeModeSetting.LIGHT -> false
            ThemeModeSetting.SYSTEM -> systemDark
        }

        return when {
            isNeon -> ThemeColors(
                isNeon = true,
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
            isDark -> ThemeColors(
                isNeon = false,
                isDark = true,
                bgBase = Color.parseColor("#0D111A"),
                cardTop = Color.parseColor("#151B26"),
                cardBottom = Color.parseColor("#151B26"),
                cardBorder = Color.parseColor("#242D3D"),
                changeCardTop = Color.parseColor("#38220A"),
                changeCardBottom = Color.parseColor("#38220A"),
                changeBorder = Color.parseColor("#FBBF24"),
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
