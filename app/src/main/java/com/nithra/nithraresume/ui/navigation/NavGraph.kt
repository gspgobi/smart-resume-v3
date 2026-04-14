package com.nithra.nithraresume.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nithra.nithraresume.ui.format.ResumeFormatScreen
import com.nithra.nithraresume.ui.generate.GenerateResumeScreen
import com.nithra.nithraresume.ui.main.MainScreen
import com.nithra.nithraresume.ui.notification.NotificationDetailScreen
import com.nithra.nithraresume.ui.notification.NotificationListScreen
import com.nithra.nithraresume.ui.profile.UserProfileScreen
import com.nithra.nithraresume.ui.sample.SampleResumesScreen
import com.nithra.nithraresume.ui.section.child.SectionChild1Screen
import com.nithra.nithraresume.ui.section.child.SectionChild2Screen
import com.nithra.nithraresume.ui.section.child.SectionChild2SubScreen
import com.nithra.nithraresume.ui.section.child.SectionChild3Screen
import com.nithra.nithraresume.ui.section.child.SectionChild3SubScreen
import com.nithra.nithraresume.ui.section.child.SectionChild4Screen
import com.nithra.nithraresume.ui.section.child.SectionChild4SignatureScreen
import com.nithra.nithraresume.ui.section.child.SectionChild5Screen
import com.nithra.nithraresume.ui.section.child.SectionChild6Screen
import com.nithra.nithraresume.ui.section.child.SectionChild6SubScreen
import com.nithra.nithraresume.ui.section.child.SectionChild7Screen
import com.nithra.nithraresume.ui.section.child.SectionChild7SubScreen
import com.nithra.nithraresume.ui.section.child.SectionChild8Screen
import com.nithra.nithraresume.ui.section.head.SectionHeadScreen
import com.nithra.nithraresume.ui.settings.AppSettingsScreen
import com.nithra.nithraresume.ui.viewshare.ViewShareScreen

@Composable
fun SmartResumeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── No-argument screens ───────────────────────────────────────────────

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.UserProfiles.route) {
            UserProfileScreen(navController = navController)
        }

        composable(Screen.SampleResumes.route) {
            SampleResumesScreen(navController = navController)
        }

        composable(Screen.Notifications.route) {
            NotificationListScreen(navController = navController)
        }

        composable(Screen.AppSettings.route) {
            AppSettingsScreen(navController = navController)
        }

        // ── Screens that receive profileId ────────────────────────────────────

        composable(
            route = Screen.SectionHead.route,
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) {
            SectionHeadScreen(navController = navController)
        }

        composable(
            route = Screen.ResumeFormat.route,
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) {
            ResumeFormatScreen(navController = navController)
        }

        composable(
            route = Screen.GenerateResume.route,
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) {
            GenerateResumeScreen(navController = navController)
        }

        composable(
            route = Screen.ViewShare.route,
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) {
            ViewShareScreen(navController = navController)
        }

        // ── Screens that receive sectionHeadAddedId ───────────────────────────

        composable(
            route = Screen.SectionChild1.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild1Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild2.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild2Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild3.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild3Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild4.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild4Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild5.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild5Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild6.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild6Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild7.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild7Screen(navController = navController)
        }

        composable(
            route = Screen.SectionChild8.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild8Screen(navController = navController)
        }

        // ── Sub-edit screens (sectionHeadAddedId + itemId) ────────────────────

        composable(
            route = Screen.SectionChild2Sub.route,
            arguments = listOf(
                navArgument("sectionHeadAddedId") { type = NavType.IntType },
                navArgument("itemId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            SectionChild2SubScreen(navController = navController)
        }

        composable(
            route = Screen.SectionChild3Sub.route,
            arguments = listOf(
                navArgument("sectionHeadAddedId") { type = NavType.IntType },
                navArgument("itemId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            SectionChild3SubScreen(navController = navController)
        }

        composable(
            route = Screen.SectionChild4Signature.route,
            arguments = listOf(navArgument("sectionHeadAddedId") { type = NavType.IntType })
        ) {
            SectionChild4SignatureScreen(navController = navController)
        }

        composable(
            route = Screen.SectionChild6Sub.route,
            arguments = listOf(
                navArgument("sectionHeadAddedId") { type = NavType.IntType },
                navArgument("itemId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            SectionChild6SubScreen(navController = navController)
        }

        composable(
            route = Screen.SectionChild7Sub.route,
            arguments = listOf(
                navArgument("sectionHeadAddedId") { type = NavType.IntType },
                navArgument("itemId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            SectionChild7SubScreen(navController = navController)
        }

        // ── Notification detail ───────────────────────────────────────────────

        composable(
            route = Screen.NotificationDetail.route,
            arguments = listOf(navArgument("notificationId") { type = NavType.IntType })
        ) {
            NotificationDetailScreen(navController = navController)
        }
    }
}
