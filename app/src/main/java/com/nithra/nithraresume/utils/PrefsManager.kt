package com.nithra.nithraresume.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore
import com.nithra.nithraresume.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "smart_resume_prefs",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "com.nithra.nithraresume_preferences"
            ) { sharedPrefs, currentPrefs ->
                currentPrefs.toMutablePreferences().apply {
                    for ((key, value) in sharedPrefs.getAll()) {
                        when (value) {
                            is Boolean -> this[booleanPreferencesKey(key)] = value
                            is Int     -> this[intPreferencesKey(key)]     = value
                            is String  -> this[stringPreferencesKey(key)]  = value
                            is Float   -> this[floatPreferencesKey(key)]   = value
                            is Long    -> this[longPreferencesKey(key)]    = value
                        }
                    }
                }.toPreferences()
            }
        )
    }
)

@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Key definitions ───────────────────────────────────────────────────────

    private object Key {
        // Notifications
        val V1_NOTIFICATIONS_ENABLED = booleanPreferencesKey("v1_notification_on_off_check_box")

        // FCM
        val V2_FCM_TOKEN_SENT_TO_SERVER = booleanPreferencesKey("v2_fcm_instance_token_sent_to_server")
        val V2_FCM_TOKEN_ID = stringPreferencesKey("v2_fcm_instance_token_id")

        // Resume generation counters
        val V2_RESUME_GENERATED_COUNT = intPreferencesKey("v2_resume_generated_count")
        val V1_RATE_US_DONE           = booleanPreferencesKey("v1_rate_us")

        // App versioning / first-launch
        val V2_CURRENT_APP_VERSION_CODE = intPreferencesKey("v2_current_app_version_code")
        val V2_APP_INSTALLED_DURING_SRV2_DB_VERSION = intPreferencesKey("v2_app_installed_during_srv2_db_version")
        val V3_APP_INSTALLED_DURING_SRV3_DB_VERSION = intPreferencesKey("v3_app_installed_during_srv3_db_version")
        val V2_IS_PERFECT_NEW_SRV2_USER = booleanPreferencesKey("v2_is_perfect_new_srv2_user")
        val V3_IS_PERFECT_NEW_SRV3_USER = booleanPreferencesKey("v3_is_perfect_new_srv3_user")
        val V3_ALL_V2_FILES_MIGRATED_TO_V3_FILES_STRUCTURE = booleanPreferencesKey("v3_all_v2_files_migrated_to_v3_files_structure")
    }

    // ── Safe data flow (recovers from corrupted preferences file) ────────────

    private val safeData: Flow<Preferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    // ── Notifications ─────────────────────────────────────────────────────────

    val v1NotificationsEnabled: Flow<Boolean> = safeData
        .map { it[Key.V1_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setV1NotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Key.V1_NOTIFICATIONS_ENABLED] = enabled }
    }

    // ── FCM ───────────────────────────────────────────────────────────────────

    val v2FcmTokenSentToServer: Flow<Boolean> = safeData
        .map { it[Key.V2_FCM_TOKEN_SENT_TO_SERVER] ?: false }

    suspend fun setV2FcmTokenSentToServer(sent: Boolean) {
        context.dataStore.edit { it[Key.V2_FCM_TOKEN_SENT_TO_SERVER] = sent }
    }

    val v2FcmTokenId: Flow<String> = safeData
        .map { it[Key.V2_FCM_TOKEN_ID] ?: "" }

    suspend fun setV2FcmTokenId(token: String) {
        context.dataStore.edit { it[Key.V2_FCM_TOKEN_ID] = token }
    }

    // ── Resume generation ─────────────────────────────────────────────────────

    val v2ResumeGeneratedCount: Flow<Int> = safeData
        .map { it[Key.V2_RESUME_GENERATED_COUNT] ?: 0 }

    suspend fun incrementV2ResumeGeneratedCount() {
        context.dataStore.edit { prefs ->
            prefs[Key.V2_RESUME_GENERATED_COUNT] = (prefs[Key.V2_RESUME_GENERATED_COUNT] ?: 0) + 1
        }
    }

    val v1RateUsDone: Flow<Boolean> = safeData
        .map { it[Key.V1_RATE_US_DONE] ?: false }

    suspend fun setV1RateUsDone() {
        context.dataStore.edit { it[Key.V1_RATE_US_DONE] = true }
    }

    // ── App version tracking ──────────────────────────────────────────────────

    val v2CurrentAppVersionCode: Flow<Int> = safeData
        .map { it[Key.V2_CURRENT_APP_VERSION_CODE] ?: 0 }

    suspend fun setV2CurrentAppVersionCode(versionCode: Int) {
        context.dataStore.edit { it[Key.V2_CURRENT_APP_VERSION_CODE] = versionCode }
    }

    val v2AppInstalledDuringSrv2DbVersion: Flow<Int> = safeData
        .map { it[Key.V2_APP_INSTALLED_DURING_SRV2_DB_VERSION] ?: 0 }

    suspend fun setV2AppInstalledDuringSrv2DbVersion(dbVersion: Int) {
        context.dataStore.edit { it[Key.V2_APP_INSTALLED_DURING_SRV2_DB_VERSION] = dbVersion }
    }

    val v3AppInstalledDuringSrv3DbVersion: Flow<Int> = safeData
        .map { it[Key.V3_APP_INSTALLED_DURING_SRV3_DB_VERSION] ?: 0 }

    suspend fun setV3AppInstalledDuringSrv3DbVersion(dbVersion: Int) {
        context.dataStore.edit { it[Key.V3_APP_INSTALLED_DURING_SRV3_DB_VERSION] = dbVersion }
    }

    val v2IsPerfectNewSrv2User: Flow<Boolean> = safeData
        .map { it[Key.V2_IS_PERFECT_NEW_SRV2_USER] ?: false }

    suspend fun setV2IsPerfectNewSrv2User(isNew: Boolean) {
        context.dataStore.edit { it[Key.V2_IS_PERFECT_NEW_SRV2_USER] = isNew }
    }

    val v3IsPerfectNewSrv3User: Flow<Boolean> = safeData
        .map { it[Key.V3_IS_PERFECT_NEW_SRV3_USER] ?: false }

    suspend fun setV3IsPerfectNewSrv3User(isNew: Boolean) {
        context.dataStore.edit { it[Key.V3_IS_PERFECT_NEW_SRV3_USER] = isNew }
    }

    val v3AllV2FilesMigratedToV3FilesStructure: Flow<Boolean> = safeData
        .map { it[Key.V3_ALL_V2_FILES_MIGRATED_TO_V3_FILES_STRUCTURE] ?: false }

    suspend fun setV3AllV2FilesMigratedToV3FilesStructure() {
        context.dataStore.edit { it[Key.V3_ALL_V2_FILES_MIGRATED_TO_V3_FILES_STRUCTURE] = true }
    }

    suspend fun dumpPrefs() {
        if (BuildConfig.DEBUG.not()) return
        context.dataStore.data.first().asMap().forEach { (key, value) ->
            Log.d("PrefsDebug", "${key.name} = $value")
        }
    }
}
