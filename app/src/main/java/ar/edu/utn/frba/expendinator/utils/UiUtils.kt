package ar.edu.utn.frba.expendinator.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.LinearLayout

private fun showCustomToast(
    context: Context,
    message: String,
    backgroundColor: Int,
    iconRes: Int
) {
    val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(32, 20, 32, 20)
        background = GradientDrawable().apply {
            cornerRadius = 32f
            setColor(backgroundColor)
        }
    }

    val icon = ImageView(context).apply {
        setImageResource(iconRes)
        setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val size = (20 * context.resources.displayMetrics.density).toInt()
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            rightMargin = (12 * context.resources.displayMetrics.density).toInt()
        }
    }

    val textView = TextView(context).apply {
        text = message
        setTextColor(Color.WHITE)
        textSize = 14f
    }

    container.addView(icon)
    container.addView(textView)

    Toast(context).apply {
        duration = Toast.LENGTH_SHORT
        view = container
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
    }.show()
}

fun showSuccessToast(context: Context, message: String) {
    showCustomToast(
        context = context,
        message = message,
        backgroundColor = 0xFF4CAF50.toInt(),
        iconRes = android.R.drawable.ic_dialog_info // icono porque Toast no soporta Icons
    )
}

fun showErrorToast(context: Context, message: String) {
    showCustomToast(
        context = context,
        message = message,
        backgroundColor = 0xFFF44336.toInt(),
        iconRes = android.R.drawable.ic_dialog_alert // icono porque Toast no soporta Icons
    )
}
