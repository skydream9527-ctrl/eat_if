package com.eatif.app.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.eatif.app.data.local.FoodDatabase
import kotlinx.coroutines.flow.first

class FoodWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val foodName = getRandomFoodName(context)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(16),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🍽️ 今天吃什么？",
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                    Spacer(modifier = GlanceModifier.height(8))
                    Text(
                        text = foodName,
                        style = TextStyle(
                            color = ColorProvider(day = Color(0xFFFF6B35), night = Color(0xFFFF8F5C)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(12))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.glance.layout.Button(
                            text = "换一个",
                            onClick = actionRunCallback<RefreshAction>()
                        )
                    }
                }
            }
        }
    }

    private suspend fun getRandomFoodName(context: Context): String {
        val db = Room.databaseBuilder(
            context,
            FoodDatabase::class.java,
            "food_database"
        ).build()
        val foods = db.foodDao().getAllFoods().first()
        db.close()
        return if (foods.isNotEmpty()) foods.random().name else "暂无食物数据"
    }
}
