package com.nithra.nithraresume.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "smart_resume_prefs")

@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Key definitions ───────────────────────────────────────────────────────

    private object Key {
        // Notifications
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("v1_notification_on_off_check_box")

        // FCM
        val FCM_TOKEN_SENT_TO_SERVER = booleanPreferencesKey("v2_fcm_instance_token_sent_to_server")
        val FCM_TOKEN_ID = stringPreferencesKey("v2_fcm_instance_token_id")

        // Resume generation counters
        val RESUME_GENERATED_COUNT = intPreferencesKey("v2_resume_generated_count")
        val RATE_US_DONE           = booleanPreferencesKey("v1_rate_us")

        // App versioning / first-launch
        val CURRENT_APP_VERSION_CODE = intPreferencesKey("v2_current_app_version_code")
        val APP_INSTALLED_DURING_SRV2_DB_VERSION = intPreferencesKey("v2_app_installed_during_srv2_db_version")
        val IS_PERFECT_NEW_SRV2_USER = booleanPreferencesKey("v2_is_perfect_new_srv2_user")
        val IS_PERFECT_NEW_SRV3_USER = booleanPreferencesKey("v3_is_perfect_new_srv3_user")
    }

    // ── Safe data flow (recovers from corrupted preferences file) ────────────

    private val safeData: Flow<Preferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    // ── Notifications ─────────────────────────────────────────────────────────

    val notificationsEnabled: Flow<Boolean> = safeData
        .map { it[Key.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Key.NOTIFICATIONS_ENABLED] = enabled }
    }

    // ── FCM ───────────────────────────────────────────────────────────────────

    val fcmTokenSentToServer: Flow<Boolean> = safeData
        .map { it[Key.FCM_TOKEN_SENT_TO_SERVER] ?: false }

    suspend fun setFcmTokenSentToServer(sent: Boolean) {
        context.dataStore.edit { it[Key.FCM_TOKEN_SENT_TO_SERVER] = sent }
    }

    val fcmTokenId: Flow<String> = safeData
        .map { it[Key.FCM_TOKEN_ID] ?: "" }

    suspend fun setFcmTokenId(token: String) {
        context.dataStore.edit { it[Key.FCM_TOKEN_ID] = token }
    }

    // ── Resume generation ─────────────────────────────────────────────────────

    val resumeGeneratedCount: Flow<Int> = safeData
        .map { it[Key.RESUME_GENERATED_COUNT] ?: 0 }

    suspend fun incrementResumeGeneratedCount() {
        context.dataStore.edit { prefs ->
            prefs[Key.RESUME_GENERATED_COUNT] = (prefs[Key.RESUME_GENERATED_COUNT] ?: 0) + 1
        }
    }

    val rateUsDone: Flow<Boolean> = safeData
        .map { it[Key.RATE_US_DONE] ?: false }

    suspend fun setRateUsDone() {
        context.dataStore.edit { it[Key.RATE_US_DONE] = true }
    }

    // ── App version tracking ──────────────────────────────────────────────────

    val currentAppVersionCode: Flow<Int> = safeData
        .map { it[Key.CURRENT_APP_VERSION_CODE] ?: 0 }

    suspend fun setCurrentAppVersionCode(versionCode: Int) {
        context.dataStore.edit { it[Key.CURRENT_APP_VERSION_CODE] = versionCode }
    }

    val appInstalledDuringSrv2DbVersion: Flow<Int> = safeData
        .map { it[Key.APP_INSTALLED_DURING_SRV2_DB_VERSION] ?: 0 }

    suspend fun setAppInstalledDuringSrv2DbVersion(dbVersion: Int) {
        context.dataStore.edit { it[Key.APP_INSTALLED_DURING_SRV2_DB_VERSION] = dbVersion }
    }

    val isPerfectNewSrv2User: Flow<Boolean> = safeData
        .map { it[Key.IS_PERFECT_NEW_SRV2_USER] ?: false }

    suspend fun setIsPerfectNewSrv2User(isNew: Boolean) {
        context.dataStore.edit { it[Key.IS_PERFECT_NEW_SRV2_USER] = isNew }
    }

    val isPerfectNewSrv3User: Flow<Boolean> = safeData
        .map { it[Key.IS_PERFECT_NEW_SRV3_USER] ?: false }

    suspend fun setIsPerfectNewSrv3User(isNew: Boolean) {
        context.dataStore.edit { it[Key.IS_PERFECT_NEW_SRV3_USER] = isNew }
    }
}
