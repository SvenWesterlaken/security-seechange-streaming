package com.example.lukab.seechange_streaming.app.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun closeSoftKeyboard(a: Activity) {
    val imm = a.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(a.currentFocus.windowToken, 0)
}