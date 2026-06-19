package com.example.mediaplayerprep.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mediaplayerprep.data.InMemoryVideoCatalog
import com.example.mediaplayerprep.player.PlayerControllerFactory
import com.example.mediaplayerprep.ui.feed.FeedRoot
import com.example.mediaplayerprep.ui.home.HomeRoot
import com.example.mediaplayerprep.ui.player.PlayerRoot
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class PlayerRoute(val videoId: String)

@Serializable
data object FeedRoute

@Composable
fun MediaPrepApp(navController: NavHostController = rememberNavController()) {
    val catalog = InMemoryVideoCatalog()
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeRoot(
                catalog = catalog,
                onOpenVideo = { navController.navigate(PlayerRoute(it)) },
                onOpenFeed = { navController.navigate(FeedRoute) }
            )
        }
        composable<PlayerRoute> { entry ->
            val route = entry.toRoute<PlayerRoute>()
            PlayerRoot(
                videoId = route.videoId,
                catalog = catalog,
                controllerFactory = PlayerControllerFactory(androidx.compose.ui.platform.LocalContext.current),
                onBack = navController::popBackStack
            )
        }
        composable<FeedRoute> {
            FeedRoot(
                catalog = catalog,
                controllerFactory = PlayerControllerFactory(androidx.compose.ui.platform.LocalContext.current),
                onBack = navController::popBackStack
            )
        }
    }
}
