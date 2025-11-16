package ar.edu.utn.frba.ExpendinatorApp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import ar.edu.utn.frba.expendinator.MainActivity
import ar.edu.utn.frba.ExpendinatorApp.R

/**
 * Widget principal de Expendinator para la pantalla de inicio
 * Muestra resumen de gastos y acceso rápido al escaneo
 */
class ExpendinatorWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Actualizar cada instancia del widget
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Widget agregado por primera vez
    }

    override fun onDisabled(context: Context) {
        // Último widget removido
    }

    companion object {
        private const val ACTION_SCAN = "ar.edu.utn.frba.expendinator.ACTION_SCAN"
        private const val ACTION_OPEN_APP = "ar.edu.utn.frba.expendinator.ACTION_OPEN_APP"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Obtener datos del repositorio (simulado por ahora)
            val widgetData = getWidgetData(context)

            // Crear RemoteViews del layout del widget
            val views = RemoteViews(context.packageName, R.layout.widget_expendinator)

            // Configurar textos
            views.setTextViewText(R.id.widget_total_today, "$ ${widgetData.totalToday}")
            views.setTextViewText(R.id.widget_total_week, "$ ${widgetData.totalWeek}")


            // Últimos gastos
            views.setTextViewText(R.id.widget_expense_1, widgetData.lastExpenses.getOrNull(0) ?: "")
            views.setTextViewText(R.id.widget_expense_2, widgetData.lastExpenses.getOrNull(1) ?: "")
            views.setTextViewText(R.id.widget_expense_3, widgetData.lastExpenses.getOrNull(2) ?: "")

            // Intent para abrir la cámara (escanear)

            val scanIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_SCAN
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("openScanner", true)
            }
            val scanPendingIntent = PendingIntent.getActivity(
                context,
                100,
                scanIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_scan_button, scanPendingIntent)

            // Intent para abrir la app
            val openIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_APP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                200,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openPendingIntent)

            // Actualizar el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getWidgetData(context: Context): WidgetData {
            // Leer datos guardados por el ViewModel
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)

            val totalToday = prefs.getString("total_today", "0.00") ?: "0.00"
            val totalWeek = prefs.getString("total_week", "0.00") ?: "0.00"
            val expense1 = prefs.getString("expense_1", "") ?: ""
            val expense2 = prefs.getString("expense_2", "") ?: ""
            val expense3 = prefs.getString("expense_3", "") ?: ""

            return WidgetData(
                totalToday = totalToday,
                totalWeek = totalWeek,
                topCategory = "", // No lo usamos por ahora
                lastExpenses = listOf(expense1, expense2, expense3).filter { it.isNotEmpty() }
            )
        }
    }
}

/**
 * Clase de datos para la información del widget
 */
data class WidgetData(
    val totalToday: String,
    val totalWeek: String,
    val topCategory: String,
    val lastExpenses: List<String>
)