package com.nithra.nithraresume.di

/**
 * All five repositories use @Singleton + @Inject constructor, so Hilt
 * provides them automatically without explicit @Provides bindings.
 *
 * Repositories and their automatic Hilt wiring:
 *   - ResumeFormatRepository  → inject ResumeFormatBaseDao
 *   - UserProfileRepository   → inject UserProfileDao
 *   - SectionHeadRepository   → inject SectionHeadGroupBaseDao, SectionHeadBaseDao,
 *                                        SectionHeadSampleDataDao, SectionHeadAddedDao
 *   - SectionChildRepository  → inject SectionChildSingleDao, SectionChildListDao
 *   - FcmRepository           → inject FcmDataDao
 *
 * DAOs are provided by DatabaseModule. No explicit bindings needed here.
 * This file is retained as a placeholder in case interface abstractions
 * are introduced in a later step.
 */
