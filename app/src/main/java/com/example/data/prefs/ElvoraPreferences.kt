package com.example.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.elvoraDataStore by preferencesDataStore(name = "elvora_prefs")

enum class ThemePreference { SYSTEM, LIGHT, DARK }

data class UserSettingsState(
    val currentUserEmail: String = "",
    val onboardingCompleted: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.DARK,
    val musicEnabled: Boolean = false,
    val musicVolume: Float = 0.35f,
    val reducedMotion: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val feedbackPromptDismissed: Boolean = false
)

class ElvoraPreferences(private val context: Context) {
    private object Keys {
        val USER_EMAIL = stringPreferencesKey("current_user_email")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_preference")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val FEEDBACK_DISMISSED = booleanPreferencesKey("feedback_prompt_dismissed")
    }

    val settingsFlow: Flow<UserSettingsState> = context.elvoraDataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemePreference.DARK.name
        val theme = runCatching { ThemePreference.valueOf(themeStr) }.getOrDefault(ThemePreference.DARK)
        UserSettingsState(
            currentUserEmail = prefs[Keys.USER_EMAIL] ?: "",
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false,
            themePreference = theme,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: false,
            musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.35f,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            feedbackPromptDismissed = prefs[Keys.FEEDBACK_DISMISSED] ?: false
        )
    }

    suspend fun setCurrentUserEmail(email: String) {
        context.elvoraDataStore.edit { it[Keys.USER_EMAIL] = email }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.elvoraDataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.elvoraDataStore.edit { it[Keys.THEME_MODE] = theme.name }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.elvoraDataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.elvoraDataStore.edit { it[Keys.MUSIC_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.elvoraDataStore.edit { it[Keys.REDUCED_MOTION] = reduced }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.elvoraDataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun setFeedbackPromptDismissed(dismissed: Boolean) {
        context.elvoraDataStore.edit { it[Keys.FEEDBACK_DISMISSED] = dismissed }
    }
}
