package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.forBackground {
            when(type) {
                Element.BlockCode.Type.SINGLE -> {
                    rect = RectF(
                        0f,
                        top + padding,
                        canvas.width.toFloat(),
                        bottom - padding
                    )
                    canvas.drawRoundRect(
                        rect,
                        cornerRadius,
                        cornerRadius,
                        paint
                    )
                }

                Element.BlockCode.Type.START -> {
                    path.reset()
                    rect = RectF(
                        0f,
                        top + padding,
                        canvas.width.toFloat(),
                        bottom.toFloat()
                    )
                    path.addRoundRect(
                        rect,
                        floatArrayOf(
                            cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f
                        ),
                        Path.Direction.CW
                    )
                    canvas.drawPath(path, paint)
                }

                Element.BlockCode.Type.MIDDLE -> {
                    rect = RectF(
                            0f,
                            top.toFloat(),
                            canvas.width.toFloat(),
                            bottom.toFloat()
                    )
                    canvas.drawRect(
                            rect,
                            paint
                    )
                }

                Element.BlockCode.Type.END -> {
                    path.reset()
                    rect = RectF(
                            0f,
                            top.toFloat(),
                            canvas.width.toFloat(),
                            bottom - padding
                    )
                    path.addRoundRect(
                        rect,
                        floatArrayOf(
                            0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius
                        ),
                        Path.Direction.CW
                    )
                    canvas.drawPath(path, paint)
                }
            }
        }

        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }

    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if(fm != null){
            val offset = padding * 2
            when(type){
                Element.BlockCode.Type.SINGLE ->{
                    fm.ascent = (paint.ascent() - offset).toInt()
                    fm.descent = (paint.descent() + offset).toInt()
                }

                Element.BlockCode.Type.START ->{
                    fm.ascent = (paint.ascent() - offset).toInt()
                    fm.descent = (paint.descent()).toInt()
                }

                Element.BlockCode.Type.MIDDLE ->{
                    fm.ascent = (paint.ascent()).toInt()
                    fm.descent = (paint.descent()).toInt()
                }

                Element.BlockCode.Type.END ->{
                    fm.ascent = (paint.ascent()).toInt()
                    fm.descent = (paint.descent() + offset).toInt()
                }
            }

        }
        return 0
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style

        color = bgColor
        style = Paint.Style.FILL

        block()

        color = oldColor
        style = oldStyle
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldFont = typeface
        val oldColor = color

        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f

        block()

        textSize = oldSize
        typeface = oldFont
        color = oldColor
    }
}
