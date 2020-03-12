package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

fun View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom) {
    val params = this.layoutParams as? ViewGroup.MarginLayoutParams
    params?.run {
        setMargins(left, top, right, bottom)
        this@setMarginOptionally.layoutParams = params
    }
}

fun View.setPaddingOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom){
    this.setPadding(left, top, right, bottom)
}