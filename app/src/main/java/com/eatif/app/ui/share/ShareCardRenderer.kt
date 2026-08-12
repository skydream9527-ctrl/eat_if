package com.eatif.app.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.ui.graphics.toArgb
import com.eatif.app.ui.theme.OrangePrimary
import java.io.File
import java.io.FileOutputStream

/**
 * 分享卡渲染器 - 使用原生 Android Canvas 绘制 Bitmap
 *
 * 不依赖 Compose 捕获 API，稳定可靠。
 */
object ShareCardRenderer {

    /** 卡片尺寸（按 1080x1920 的 0.5 倍渲染，足够清晰且内存友好） */
    private const val WIDTH = 540
    private const val HEIGHT = 960

    /**
     * 渲染分享卡到 Bitmap
     */
    fun render(
        foodName: String,
        emoji: String,
        scorePercent: Int,
        reason: String,
        gameName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景渐变
        val orangeColor = OrangePrimary.toArgb()
        val orangeDark = (orangeColor and 0xFF000000.toInt()) or ((orangeColor and 0x00FF0000) * 8 / 10 and 0x00FF0000) or ((orangeColor and 0x0000FF00) * 6 / 10 and 0x0000FF00) or ((orangeColor and 0x000000FF) * 5 / 10 and 0x000000FF)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, HEIGHT.toFloat(),
                orangeColor, orangeDark,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), bgPaint)

        // 顶部标题
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("今天吃什么？", WIDTH / 2f, 120f, titlePaint)

        // emoji
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 180f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, WIDTH / 2f, 360f, emojiPaint)

        // 美食名
        val foodNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 90f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        // 自适应：过长则缩小
        var foodNameSize = 90f
        foodNamePaint.textSize = foodNameSize
        while (foodNamePaint.measureText(foodName) > WIDTH - 80 && foodNameSize > 40) {
            foodNameSize -= 5
            foodNamePaint.textSize = foodNameSize
        }
        canvas.drawText(foodName, WIDTH / 2f, 480f, foodNamePaint)

        // 推荐理由
        if (reason.isNotEmpty()) {
            val reasonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xE6FFFFFF.toInt()
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            // 简单换行：超过宽度则截断
            val maxWidth = WIDTH - 120
            val displayReason = if (reasonPaint.measureText(reason) > maxWidth) {
                ellipsize(reason, reasonPaint, maxWidth)
            } else reason
            canvas.drawText(displayReason, WIDTH / 2f, 540f, reasonPaint)
        }

        // 游戏分数卡片
        if (scorePercent >= 0) {
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x33FFFFFF
            }
            val cardLeft = WIDTH / 2f - 180
            val cardTop = 620f
            val cardRight = WIDTH / 2f + 180
            val cardBottom = 740f
            val radius = 24f
            canvas.drawRoundRect(
                cardLeft, cardTop, cardRight, cardBottom,
                radius, radius, cardPaint
            )

            // 游戏名
            val gamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xCCFFFFFF.toInt()
                textSize = 28f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("🎮 $gameName", cardLeft + 30, cardTop + 50, gamePaint)

            // 分数
            val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("得分 $scorePercent%", cardLeft + 30, cardTop + 100, scorePaint)
        }

        // 底部水印
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xB3FFFFFF.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("—— Eat If 决定了我今天的饭", WIDTH / 2f, HEIGHT - 80f, footerPaint)

        return bitmap
    }

    /** 文本省略 */
    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        val ellipsis = "…"
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return text.substring(0, end) + ellipsis
    }
}

/**
 * 分享工具
 */
object ShareUtils {

    /** 分享纯文本 */
    fun shareText(
        context: Context,
        text: String,
        chooserTitle: String = "分享结果"
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    /** 分享图片（+可选文本） */
    fun shareImage(
        context: Context,
        bitmap: Bitmap,
        text: String = "",
        chooserTitle: String = "分享结果"
    ) {
        try {
            val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(cacheDir, "eatif_share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (e: Exception) {
            // 回退到纯文本分享
            shareText(context, text.ifEmpty { "Eat If 推荐美食：分享图片失败，请重试" }, chooserTitle)
        }
    }

    /** 构造分享文案 */
    fun buildShareText(
        foodName: String,
        scorePercent: Int,
        gameName: String,
        reason: String,
        emoji: String
    ): String = buildString {
        appendLine("🎉 今天吃什么？Eat If 帮我决定了！")
        appendLine()
        appendLine("$emoji $foodName")
        if (reason.isNotEmpty()) appendLine("💡 $reason")
        appendLine()
        if (scorePercent >= 0) {
            appendLine("🎮 $gameName 得分：$scorePercent%")
            appendLine()
        }
        appendLine("—— 来玩 Eat If，让游戏决定你的下一顿饭")
    }
}
