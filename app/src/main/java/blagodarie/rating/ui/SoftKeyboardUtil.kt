package blagodarie.rating.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun showSoftKeyboard(
        context: Context,
        focusView: View
) {
    if (focusView.requestFocus()) {
        val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun hideSoftKeyboard(
        context: Context,
        view: View
) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun hideSoftKeyboard(
        activity: Activity
) {
    var view: View? = activity.currentFocus
    if (view == null) {
        view = View(activity)
    }
    val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
