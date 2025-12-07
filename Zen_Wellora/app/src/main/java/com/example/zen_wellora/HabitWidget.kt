package com.example.zen_wellora

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

enum class WidgetSize {
    SMALL, MEDIUM, LARGE
}

class HabitWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Clean up when widget is deleted
        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (appWidgetId in appWidgetIds) {
            editor.remove("widget_${appWidgetId}_percentage")
            editor.remove("widget_${appWidgetId}_completed")
            editor.remove("widget_${appWidgetId}_total")
            editor.remove("widget_${appWidgetId}_streak")
            editor.remove("widget_${appWidgetId}_timestamp")
        }
        editor.apply()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        // Handle widget refresh
        when (intent?.action) {
            "UPDATE_WIDGET" -> {
                context?.let {
                    val appWidgetManager = AppWidgetManager.getInstance(it)
                    val thisWidget = ComponentName(it, HabitWidget::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                    onUpdate(it, appWidgetManager, appWidgetIds)
                }
            }
            "REFRESH_WIDGET" -> {
                context?.let {
                    refreshAllWidgets(it)
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                context?.let {
                    val appWidgetManager = AppWidgetManager.getInstance(it)
                    val thisWidget = ComponentName(it, HabitWidget::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                    onUpdate(it, appWidgetManager, appWidgetIds)
                }
            }
        }
    }

    companion object {
        private const val TAG = "HabitWidget"
        
        fun refreshAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, HabitWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                
                android.util.Log.d(TAG, "All widgets refreshed successfully")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error refreshing widgets: ${e.message}")
            }
        }
        
        private fun getWidgetSize(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int): WidgetSize {
            return try {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                
                when {
                    minWidth <= 110 && minHeight <= 110 -> WidgetSize.SMALL
                    minWidth >= 200 || minHeight >= 200 -> WidgetSize.LARGE
                    else -> WidgetSize.MEDIUM
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error determining widget size: ${e.message}")
                WidgetSize.MEDIUM
            }
        }
        
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                android.util.Log.d(TAG, "Updating widget $appWidgetId")
                
                // Get real habit data from HabitManager
                val habitManager = HabitManager(context)
                habitManager.loadHabitsFromStorage()
                
                val (completed, total) = habitManager.getTodayProgress()
                val percentage = habitManager.getTodayCompletionPercentage()
                val bestStreak = habitManager.getBestStreak()

                android.util.Log.d(TAG, "Widget data: $completed/$total ($percentage%), streak: $bestStreak")

                // Determine widget size and layout
                val widgetSize = getWidgetSize(context, appWidgetManager, appWidgetId)
                val layoutRes = when (widgetSize) {
                    WidgetSize.SMALL -> R.layout.widget_layout_small
                    WidgetSize.LARGE -> R.layout.widget_layout_large
                    else -> R.layout.widget_layout
                }
                
                // Construct the RemoteViews object
                val views = RemoteViews(context.packageName, layoutRes)

                // Update widget views with real data
                views.setTextViewText(R.id.widget_title, "Habit Progress")
                views.setTextViewText(R.id.widget_percentage, "$percentage%")
                views.setTextViewText(R.id.widget_completed, completed.toString())
                views.setTextViewText(R.id.widget_total, total.toString())
                views.setTextViewText(R.id.widget_streak, bestStreak.toString())
                views.setTextViewText(R.id.widget_time, "now")

                // Update progress circle rotation
                val progressValue = if (total > 0) (completed.toFloat() / total.toFloat()) * 360f else 0f
                views.setFloat(R.id.widget_progress_fill, "setRotation", progressValue - 90f)

                // Set motivational message based on completion
                val message = when {
                    total == 0 -> "Add your first habit! 🌱"
                    completed == total -> "Perfect! 🎉"
                    completed >= total * 0.7 -> "Great job! 🌟"
                    completed >= total * 0.5 -> "Keep going! 🌿"
                    else -> "You've got this! 💪"
                }
                views.setTextViewText(R.id.widget_message, message)

                // Add click intent to open the habit tracker
                val intent = Intent(context, DailyHabitTracker::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId, // Use widget ID to make each widget unique
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_layout_root, pendingIntent)

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
                android.util.Log.d(TAG, "Widget $appWidgetId updated successfully")
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error updating widget $appWidgetId: ${e.message}", e)
                
                // Fallback to default values if there's an error
                try {
                    val fallbackLayout = when (getWidgetSize(context, appWidgetManager, appWidgetId)) {
                        WidgetSize.SMALL -> R.layout.widget_layout_small
                        WidgetSize.LARGE -> R.layout.widget_layout_large
                        else -> R.layout.widget_layout
                    }
                    val views = RemoteViews(context.packageName, fallbackLayout)
                    views.setTextViewText(R.id.widget_title, "Habit Progress")
                    views.setTextViewText(R.id.widget_percentage, "0%")
                    views.setTextViewText(R.id.widget_completed, "0")
                    views.setTextViewText(R.id.widget_total, "0")
                    views.setTextViewText(R.id.widget_streak, "0")
                    views.setTextViewText(R.id.widget_time, "now")
                    views.setTextViewText(R.id.widget_message, "Add your first habit! 🌱")
                    
                    // Reset progress circle
                    views.setFloat(R.id.widget_progress_fill, "setRotation", -90f)
                    
                    val intent = Intent(context, DailyHabitTracker::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_layout_root, pendingIntent)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    
                    android.util.Log.d(TAG, "Widget $appWidgetId fallback applied")
                } catch (fallbackError: Exception) {
                    android.util.Log.e(TAG, "Critical error in widget fallback: ${fallbackError.message}", fallbackError)
                }
            }
        }
    }
}