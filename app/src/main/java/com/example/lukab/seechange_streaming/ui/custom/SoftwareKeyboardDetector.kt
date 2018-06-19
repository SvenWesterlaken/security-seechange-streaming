package com.example.lukab.seechange_streaming.ui.custom

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.opengl.ETC1.getHeight
import android.view.inputmethod.InputMethodManager

fun closeSoftKeyboard(a: Activity) {
    val imm = a.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(a.currentFocus.windowToken, 0)
}

class SoftwareKeyboardDetector : ViewTreeObserver.OnGlobalLayoutListener{
    var softKeyBoardIsVisible: Boolean = false
    private var contentView: View

    constructor(contentView: View) {
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this)
        this.contentView = contentView
    }

    override fun onGlobalLayout() {
        val r = Rect()
        contentView.getWindowVisibleDisplayFrame(r)
        val screenHeight = contentView.rootView.height

        // r.bottom is the position above soft keypad or device button.
        // if keypad is shown, the r.bottom is smaller than that before.
        val keypadHeight = screenHeight - r.bottom

        // 0.15 ratio is perhaps enough to determine keypad height.
        this.softKeyBoardIsVisible = keypadHeight > screenHeight * 0.15
    }

}