package com.eatif.app

import android.app.Application
import com.eatif.app.data.session.SessionManager
import com.eatif.app.games.initGameRegistry
import com.eatif.app.ui.onboarding.OnboardingManager
import com.eatif.app.ui.settings.AchievementSettingsManager
import com.eatif.app.ui.settings.GameSettingsManager
import com.eatif.app.ui.settings.SkinSettingsManager
import com.eatif.app.ui.sounds.SoundManager
import com.eatif.app.ui.theme.ThemeManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EatIfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(this)
        GameSettingsManager.init(this)
        AchievementSettingsManager.init(this)
        SkinSettingsManager.init(this)
        SoundManager.init(this)
        OnboardingManager.init(this)
        SessionManager.init(this)
        initGameRegistry()
        SoundManager.setEnabled(GameSettingsManager.isSoundEnabled)
    }
}
