package com.nithra.nithraresume.ui.navigation

/**
 * Single source of truth for every navigation destination.
 *
 * - Objects  → no arguments (flat route string)
 * - Classes  → have arguments (route template + createRoute helper)
 */
sealed class Screen(val route: String) {

    // ── No-argument screens ───────────────────────────────────────────────────
    data object Main          : Screen("main")
    data object UserProfiles  : Screen("user_profiles")
    data object SampleResumes : Screen("sample_resumes")
    data object Notifications : Screen("notifications")
    data object AppSettings   : Screen("app_settings")

    // ── Screens that receive profileId ────────────────────────────────────────

    data object SectionHead : Screen("section_head/{profileId}") {
        fun createRoute(profileId: Int) = "section_head/$profileId"
    }

    data object ResumeFormat : Screen("resume_format/{profileId}") {
        fun createRoute(profileId: Int) = "resume_format/$profileId"
    }

    data object GenerateResume : Screen("generate_resume/{profileId}") {
        fun createRoute(profileId: Int) = "generate_resume/$profileId"
    }

    data object ViewShare : Screen("view_share/{profileId}") {
        fun createRoute(profileId: Int) = "view_share/$profileId"
    }

    // ── Screens that receive sectionHeadAddedId ───────────────────────────────

    data object SectionChild1 : Screen("section_child_1/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_1/$id"
    }

    data object SectionChild2 : Screen("section_child_2/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_2/$id"
    }

    data object SectionChild3 : Screen("section_child_3/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_3/$id"
    }

    data object SectionChild4 : Screen("section_child_4/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_4/$id"
    }

    data object SectionChild5 : Screen("section_child_5/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_5/$id"
    }

    data object SectionChild6 : Screen("section_child_6/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_6/$id"
    }

    data object SectionChild7 : Screen("section_child_7/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_7/$id"
    }

    data object SectionChild8 : Screen("section_child_8/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_8/$id"
    }

    // ── Sub-edit screens (sectionHeadAddedId + itemId; itemId = -1 → new) ────

    data object SectionChild2Sub : Screen("section_child_2_sub/{sectionHeadAddedId}/{itemId}") {
        fun createRoute(sectionHeadAddedId: Int, itemId: Int = -1) =
            "section_child_2_sub/$sectionHeadAddedId/$itemId"
    }

    data object SectionChild3Sub : Screen("section_child_3_sub/{sectionHeadAddedId}/{itemId}") {
        fun createRoute(sectionHeadAddedId: Int, itemId: Int = -1) =
            "section_child_3_sub/$sectionHeadAddedId/$itemId"
    }

    data object SectionChild4Signature : Screen("section_child_4_signature/{sectionHeadAddedId}") {
        fun createRoute(id: Int) = "section_child_4_signature/$id"
    }

    data object SectionChild6Sub : Screen("section_child_6_sub/{sectionHeadAddedId}/{itemId}") {
        fun createRoute(sectionHeadAddedId: Int, itemId: Int = -1) =
            "section_child_6_sub/$sectionHeadAddedId/$itemId"
    }

    data object SectionChild7Sub : Screen("section_child_7_sub/{sectionHeadAddedId}/{itemId}") {
        fun createRoute(sectionHeadAddedId: Int, itemId: Int = -1) =
            "section_child_7_sub/$sectionHeadAddedId/$itemId"
    }

    // ── Reorder sections / add-ons ────────────────────────────────────────────

    data object ReorderSections : Screen("reorder_sections/{profileId}/{groupId}") {
        fun createRoute(profileId: Int, groupId: Int) = "reorder_sections/$profileId/$groupId"
    }

    data object ReorderChild : Screen("reorder_child/{sectionHeadAddedId}/{childType}") {
        fun createRoute(sectionHeadAddedId: Int, childType: Int) =
            "reorder_child/$sectionHeadAddedId/$childType"
    }

    // ── Notification detail ───────────────────────────────────────────────────

    data object NotificationDetail : Screen("notification_detail/{notificationId}") {
        fun createRoute(notificationId: Int) = "notification_detail/$notificationId"
    }
}
