package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinCollection
import com.eatif.app.ui.settings.SkinSettingsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkinResolver @Inject constructor() {
    fun getActiveSkin(gameId: String): Skin {
        val skins = SkinRegistry.getSkinsForGame(gameId)
        val activeSkinId = SkinSettingsManager.getActiveSkinId(gameId)

        if (activeSkinId != null) {
            val skin = skins.find { it.id == activeSkinId }
            if (skin != null && SkinSettingsManager.isSkinUnlocked(skin.id)) {
                return skin
            }
        }

        return skins.find { it.isDefault } ?: skins.firstOrNull() ?: SkinRegistry.all.first()
    }

    fun getSkinCollection(gameId: String): List<SkinCollection> {
        return SkinRegistry.getSkinsForGame(gameId).map { skin ->
            SkinCollection(
                skinId = skin.id,
                gameId = skin.gameId,
                isUnlocked = SkinSettingsManager.isSkinUnlocked(skin.id),
                isActive = SkinSettingsManager.getActiveSkinId(gameId) == skin.id && SkinSettingsManager.isSkinUnlocked(skin.id)
            )
        }
    }

    fun setActiveSkin(gameId: String, skinId: String): Boolean {
        val skin = SkinRegistry.getById(skinId) ?: return false
        if (!SkinSettingsManager.isSkinUnlocked(skinId)) return false
        SkinSettingsManager.setActiveSkinId(gameId, skinId)
        return true
    }

    fun unlockSkin(skinId: String) {
        SkinSettingsManager.unlockSkin(skinId)
    }

    fun initializeDefaults() {
        SkinRegistry.all.filter { it.isDefault }.forEach { skin ->
            if (!SkinSettingsManager.isSkinUnlocked(skin.id)) {
                unlockSkin(skin.id)
            }
        }
    }
}
