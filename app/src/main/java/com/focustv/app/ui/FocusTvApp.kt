package com.focustv.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.screens.*

enum class Screen { Splash, Home, Sources, Live, Movies, Series, Favorites, Profiles, Settings, Details }

@Composable
fun FocusTvApp(vm: FocusTvViewModel = viewModel()) {
    FocusTheme {
        val state by vm.state.collectAsState()
        val context = LocalContext.current
        var screen by remember { mutableStateOf(Screen.Splash) }
        var selected by remember { mutableStateOf<MediaContent?>(null) }

        LaunchedEffect(state.loading) {
            if (!state.loading && screen == Screen.Splash) screen = Screen.Home
        }

        Box(Modifier.fillMaxSize().background(DeepBg)) {
            Crossfade(targetState = screen, label = "screen") { current ->
                when (current) {
                    Screen.Splash -> SplashScreen()
                    Screen.Home -> HomeScreen(
                        state = state,
                        onOpen = { section ->
                            screen = when (section) {
                                "live" -> Screen.Live
                                "movies" -> Screen.Movies
                                "series" -> Screen.Series
                                "favorites" -> Screen.Favorites
                                "sources" -> Screen.Sources
                                "profiles" -> Screen.Profiles
                                "settings" -> Screen.Settings
                                else -> Screen.Home
                            }
                        },
                        onItem = { selected = it; screen = Screen.Details }
                    )
                    Screen.Sources -> SourcesScreen(
                        state = state,
                        onBack = { screen = Screen.Home },
                        onSave = vm::saveSource,
                        onDelete = vm::deleteSource,
                        onRefresh = vm::refresh
                    )
                    Screen.Live -> LiveScreen(
                        items = state.live,
                        onBack = { screen = Screen.Home },
                        onPlay = { vm.markWatched(it); PlayerLauncher.open(context, it) },
                        onDetails = { selected = it; screen = Screen.Details }
                    )
                    Screen.Movies -> VodScreen(
                        title = "Films",
                        items = state.movies,
                        onBack = { screen = Screen.Home },
                        onItem = { selected = it; screen = Screen.Details }
                    )
                    Screen.Series -> VodScreen(
                        title = "Séries",
                        items = state.series,
                        onBack = { screen = Screen.Home },
                        onItem = { selected = it; screen = Screen.Details }
                    )
                    Screen.Favorites -> VodScreen(
                        title = "Favoris",
                        items = (state.live + state.movies + state.series).filter { state.favorites.contains(it.id) },
                        onBack = { screen = Screen.Home },
                        onItem = { selected = it; screen = Screen.Details }
                    )
                    Screen.Profiles -> ProfilesScreen(state, onBack = { screen = Screen.Home })
                    Screen.Settings -> SettingsScreen(state, onBack = { screen = Screen.Home })
                    Screen.Details -> DetailsScreen(
                        item = selected,
                        isFavorite = selected?.id?.let { state.favorites.contains(it) } == true,
                        onBack = { screen = Screen.Home },
                        onPlay = {
                            selected?.let {
                                vm.markWatched(it)
                                PlayerLauncher.open(context, it)
                            }
                        },
                        onFavorite = { selected?.let(vm::toggleFavorite) }
                    )
                }
            }
        }
    }
}
