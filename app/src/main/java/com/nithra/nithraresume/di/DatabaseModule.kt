package com.nithra.nithraresume.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nithra.nithraresume.data.db.MIGRATION_1_2
import com.nithra.nithraresume.data.db.SmartResumeDatabase
import com.nithra.nithraresume.data.db.migrationRan1to2
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.nithra.nithraresume.data.db.dao.FcmDataDao
import com.nithra.nithraresume.data.db.dao.ResumeFormatBaseDao
import com.nithra.nithraresume.data.db.dao.SectionChildListDao
import com.nithra.nithraresume.data.db.dao.SectionChildSingleDao
import com.nithra.nithraresume.data.db.dao.SectionHeadAddedDao
import com.nithra.nithraresume.data.db.dao.SectionHeadBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadGroupBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadSampleDataDao
import com.nithra.nithraresume.data.db.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        prefsManager: PrefsManager,
        analyticsManager: AnalyticsManager
    ): SmartResumeDatabase =
        Room.databaseBuilder(
            context,
            SmartResumeDatabase::class.java,
            SmartResumeDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(SmartResumeDatabase.seedCallback)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    moduleScope.launch {
                        prefsManager.setV3IsPerfectNewSrv3User(true)
                        prefsManager.setV3AppInstalledDuringSrv3DbVersion(SmartResumeDatabase.DATABASE_VERSION)
                    }
                }
                override fun onOpen(db: SupportSQLiteDatabase) {
                    if (migrationRan1to2) {
                        migrationRan1to2 = false
                        analyticsManager.logDbMigrate1to2Finished()
                    }
                }
            })
            .build()

    @Provides
    fun provideResumeFormatBaseDao(db: SmartResumeDatabase): ResumeFormatBaseDao =
        db.resumeFormatBaseDao()

    @Provides
    fun provideSectionHeadGroupBaseDao(db: SmartResumeDatabase): SectionHeadGroupBaseDao =
        db.sectionHeadGroupBaseDao()

    @Provides
    fun provideSectionHeadBaseDao(db: SmartResumeDatabase): SectionHeadBaseDao =
        db.sectionHeadBaseDao()

    @Provides
    fun provideSectionHeadSampleDataDao(db: SmartResumeDatabase): SectionHeadSampleDataDao =
        db.sectionHeadSampleDataDao()

    @Provides
    fun provideUserProfileDao(db: SmartResumeDatabase): UserProfileDao =
        db.userProfileDao()

    @Provides
    fun provideSectionHeadAddedDao(db: SmartResumeDatabase): SectionHeadAddedDao =
        db.sectionHeadAddedDao()

    @Provides
    fun provideSectionChildSingleDao(db: SmartResumeDatabase): SectionChildSingleDao =
        db.sectionChildSingleDao()

    @Provides
    fun provideSectionChildListDao(db: SmartResumeDatabase): SectionChildListDao =
        db.sectionChildListDao()

    @Provides
    fun provideFcmDataDao(db: SmartResumeDatabase): FcmDataDao =
        db.fcmDataDao()
}
