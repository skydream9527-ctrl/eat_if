package com.eatif.app.domain.model

enum class TimeSlot(val label: String, val emoji: String, val hours: IntRange) {
    MORNING("早餐", "🌅", 6..10),
    LUNCH("午餐", "☀️", 11..14),
    DINNER("晚餐", "🌆", 17..21),
    LATE_NIGHT("夜宵", "🌙", 22..5);

    companion object {
        fun current(): TimeSlot {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return entries.find { hour in it.hours } ?: LATE_NIGHT
        }
    }
}
