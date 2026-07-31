package com.aura.ai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aura.ai.presentation.explore.ExploreScreen
import com.aura.ai.presentation.home.HomeScreen
import com.aura.ai.presentation.library.LibraryScreen
import com.aura.ai.presentation.chat.ChatScreen
import com.aura.ai.presentation.search.SearchScreen
import com.aura.ai.presentation.settings.SettingsScreen
import com.aura.ai.presentation.voice.VoiceScreen

@Composable
fun AuraNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in TopLevelDestination.entries.map { it.route }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = { fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(260)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(240)) },
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenChat = { navController.navigate(Routes.chat(it)) },
                    onNewChat = { navController.navigate(Routes.chat("new")) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) }
                )
            }
            composable(Routes.EXPLORE) {
                ExploreScreen(onRunPrompt = { navController.navigate(Routes.chat("new?prompt=${it}")) })
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(onOpenChat = { navController.navigate(Routes.chat(it)) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenChat = { navController.navigate(Routes.chat(it)) }
                )
            }
            composable(Routes.VOICE) {
                VoiceScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.CHAT}/{${Routes.CHAT_ARG}}",
                arguments = listOf(navArgument(Routes.CHAT_ARG) { type = NavType.StringType })
            ) {
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVoice = { navController.navigate(Routes.VOICE) }
                )
            }
        }

        if (showBottomBar) {
            FloatingNavBar(
                currentRoute = currentRoute,
                onNavigate = { dest ->
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
            )
        }
    }
}
