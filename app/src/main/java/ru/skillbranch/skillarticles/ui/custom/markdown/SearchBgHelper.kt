package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Layout
import android.text.Spanned
import androidx.core.graphics.ColorUtils
import androidx.core.text.getSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan

class SearchBgHelper(
        context: Context,
        private val focusListener: (Int)->Unit
) {
    private val padding: Int = context.dpToIntPx(4)
    private val radius: Float = context.dpToPx(8)
    private val borderWidth: Int = context.dpToIntPx(1)
    private val secondaryColor: Int = context.attrValue(R.attr.colorSecondary)

    private val alphaColor: Int = ColorUtils.setAlphaComponent(secondaryColor, 160)
    val drawable: Drawable by lazy{
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = FloatArray(8).apply { fill(radius) }
            color = ColorStateList.valueOf(alphaColor)
            setStroke(borderWidth, secondaryColor)
        }
    }

    val drawableLeft: Drawable by lazy{
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(
                    radius, radius,
                    0f, 0f,
                    0f, 0f,
                    radius, radius
            )
            color = ColorStateList.valueOf(alphaColor)
            setStroke(borderWidth, secondaryColor)
        }
    }

    val drawableMiddle: Drawable by lazy{
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(alphaColor)
            setStroke(borderWidth, secondaryColor)
        }
    }

    val drawableRight: Drawable by lazy{
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(
                    0f, 0f,
                    radius, radius,
                    radius, radius,
                    0f, 0f
            )
            color = ColorStateList.valueOf(alphaColor)
            setStroke(borderWidth, secondaryColor)
        }
    }

    private lateinit var spans: Array<out SearchSpan>

    private var spanStart = 0
    private var spanEnd = 0
    private var startLine = 0
    private var endLine = 0
    private var startOffset = 0
    private var endOffset = 0

    private lateinit var render: SearchBgRender
    private val singleLineRender: SearchBgRender by lazy {
        SingleLineRender(padding, drawable)
    }

    private val multiLineRender: SearchBgRender by lazy {
        MultiLineRender(
                padding,
                drawableLeft,
                drawableMiddle,
                drawableRight
        )
    }

    fun draw(canvas: Canvas, text: Spanned, layout: Layout){
        spans = text.getSpans()
        spans.forEach {
            spanStart = text.getSpanStart(it)
            spanEnd = text.getSpanEnd(it)
            startLine = layout.getLineForOffset(spanStart)
            endLine = layout.getLineForOffset(spanEnd)

            startOffset = layout.getPrimaryHorizontal(spanStart).toInt()
            endOffset = layout.getPrimaryHorizontal(spanEnd).toInt()

            render = if(startLine == endLine) singleLineRender else multiLineRender
            render.draw(canvas, layout, startLine, endLine, startOffset, endOffset)
        }
    }
}

abstract class SearchBgRender(
        val padding: Int
){
    abstract fun draw(
            canvas: Canvas,
            layout: Layout,
            startLine:Int,
            endLine:Int,
            startOffset:Int,
            endOffset:Int,
            topExtraPadding:Int = 0,
            bottomExtraPadding:Int = 0
    )

    fun getLineTop(layout: Layout, line: Int):Int{
        return layout.getLineTop(line)
    }

    fun getLineBottom(layout: Layout, line: Int):Int{
        return layout.getLineBottom(line)
    }
}

class SingleLineRender(
        padding: Int,
        val drawable: Drawable
): SearchBgRender(padding){
    private var lineTop: Int = 0
    private var lineBottom: Int = 0

    override fun draw(
            canvas: Canvas,
            layout: Layout,
            startLine:Int,
            endLine:Int,
            startOffset:Int,
            endOffset:Int,
            topExtraPadding:Int,
            bottomExtraPadding:Int
    ){
        lineTop = getLineTop(layout, startLine)
        lineBottom = getLineBottom(layout, startLine)
        drawable.setBounds(startOffset, lineTop, endOffset, lineBottom)
        drawable.draw(canvas)
    }
}

class MultiLineRender(
        padding: Int,
        val drawableLeft: Drawable,
        val drawableMiddle: Drawable,
        val drawableRight: Drawable
): SearchBgRender(padding){
    private var lineTop: Int = 0
    private var lineBottom: Int = 0

    override fun draw(
            canvas: Canvas,
            layout: Layout,
            startLine:Int,
            endLine:Int,
            startOffset:Int,
            endOffset:Int,
            topExtraPadding:Int,
            bottomExtraPadding:Int
    ){
        lineTop = getLineTop(layout, startLine)
        lineBottom = getLineBottom(layout, startLine)
        drawableLeft.setBounds(startOffset, lineTop, endOffset, lineBottom)
        drawableLeft.draw(canvas)
    }
}