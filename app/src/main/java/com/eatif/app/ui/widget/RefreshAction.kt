package com.eatif.app.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.Preferences

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentCount = prefs[androidx.glance.state.Preferences.Key<Int>("refresh_count")] ?: 0
            prefs.toMutablePreferences().apply {
                this[androidx.glance.state.Preferences.Key<Int>("refresh_count")] = currentCount + 1
            }
        }
        FoodWidget().update(context, glanceId)
    }
}
