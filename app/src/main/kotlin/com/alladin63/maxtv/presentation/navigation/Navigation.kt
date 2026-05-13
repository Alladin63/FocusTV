package com.alladin63.maxtv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alladin63.maxtv.presentation.screens.details.MovieDetailsScreen
import com.alladin63.maxtv.presentation.screens.details.SerieDetailsScreen
import com.alladin63.maxtv.presentation.screens.epg.EpgScreen
import com.alladin63.maxtv.presentation.screens.home.HomeScreen
import com.alladin63.maxtv.presentation.screens.player.PlayerScreen
import com.alladin63.maxtv.presentation.screens.playlist.PlaylistScreen
import com.alladin63.maxtv.presentation.screens.search.SearchScreen
import com.alladin63.maxtv.presentation.screens.settings.SettingsScreen
import com.alladin63.maxtv.presentation.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Playlist : Screen("playlist")
    object Home : Screen("home")
    object Epg : Screen("epg")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object MovieDetails : Screen("movie/{movieId}") {
        fun createRoute(movieId: Long) = "movie/$movieId"
    }
    object SerieDetails : Screen("serie/{serieId}") {
        fun createRoute(serieId: Long) = "serie/$serieId"
    }
    object Player : Screen("player?url={url}&title={title}&type={type}&contentId={contentId}") {
        fun createRoute(url: String, title: String, type: String = "VOD", contentId: Long = 0L) =
            "player?url=${url}&title=${title}&type=${type}&contentId=${contentId}"
    }
}

@Composable
fun MaxTvNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPlaylist = {
                    navController.navigate(Screen.Playlist.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Playlist.route) {
            PlaylistScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Playlist.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetails.createRoute(movieId))
                },
                onSerieClick = { serieId ->
                    navController.navigate(Screen.SerieDetails.createRoute(serieId))
                },
                onChannelClick = { url, title ->
                    navController.navigate(Screen.Player.createRoute(url, title, "LIVE"))
                },
                onNavigateToEpg = {
                    navController.navigate(Screen.Epg.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Epg.route) {
            EpgScreen(
                onChannelClick = { url, title ->
                    navController.navigate(Screen.Player.createRoute(url, title, "LIVE"))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetails.createRoute(movieId))
                },
                onSerieClick = { serieId ->
                    navController.navigate(Screen.SerieDetails.createRoute(serieId))
                },
                onChannelClick = { url, title ->
                    navController.navigate(Screen.Player.createRoute(url, title, "LIVE"))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L
            MovieDetailsScreen(
                movieId = movieId,
                onPlay = { url, title ->
                    navController.navigate(Screen.Player.createRoute(url, title, "VOD", movieId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SerieDetails.route,
            arguments = listOf(navArgument("serieId") { type = NavType.LongType })
        ) { backStackEntry ->
            val serieId = backStackEntry.arguments?.getLong("serieId") ?: 0L
            SerieDetailsScreen(
                serieId = serieId,
                onPlayEpisode = { url, title, episodeId ->
                    navController.navigate(Screen.Player.createRoute(url, title, "EPISODE", episodeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType; defaultValue = "VOD" },
                navArgument("contentId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "VOD"
            val contentId = backStackEntry.arguments?.getLong("contentId") ?: 0L
            PlayerScreen(
                url = url,
                title = title,
                type = type,
                contentId = contentId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
