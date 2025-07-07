package com.ritesh.rickmortywiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ritesh.network.KtorClient
import com.ritesh.rickmortywiki.screens.CharacterDetailsScreen
import com.ritesh.rickmortywiki.screens.CharacterEpisodeScreen
import com.ritesh.rickmortywiki.screens.HomeScreen
import com.ritesh.rickmortywiki.ui.theme.RickMortyWikiTheme
import dagger.hilt.android.AndroidEntryPoint
import com.ritesh.rickmortywiki.components.common.SimpleToolbar
import com.ritesh.rickmortywiki.screens.AllEpisodesScreen
import com.ritesh.rickmortywiki.screens.SearchScreen
import javax.inject.Inject

sealed class NavDestination(val title: String, val route: String, val icon: ImageVector) {
    object Home : NavDestination("Home", "home_screen", Icons.Rounded.Home)
    object Episode : NavDestination("Episodes", "episodes", Icons.Rounded.PlayArrow)
    object Search : NavDestination("Search", "search", Icons.Rounded.Search)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var ktorClient: KtorClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val topLevelRoutes = listOf(
                NavDestination.Home, NavDestination.Episode, NavDestination.Search
            )
            var selectedIndex by remember { mutableIntStateOf(0) }
            val currentPageTitle = remember { mutableStateOf("App") }
            val onBackAction by remember(currentPageTitle.value) { // Recalculate on change
                derivedStateOf {
                    if (currentPageTitle.value == "All Characters" || currentPageTitle.value == "All Episodes" || currentPageTitle.value == "Search") {
                        null
                    } else {
                        { navController.popBackStack() } // Return a lambda
                    }
                }
            }


            RickMortyWikiTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)) {
                    NavigationHost(
                        navController = navController,
                        ktorClient = ktorClient,
                        innerPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        currentPageTitle
                    )

                    SimpleToolbar(
                        title = currentPageTitle.value,
                        onBackAction = onBackAction as (() -> Unit)?,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black, // Start color (purple)
                                        Color.Transparent  // End color (teal)
                                    )
                                )
                            )
                            .padding(top = 30.dp)
                    )

                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(0.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.7f), // Start color (purple)
                                        Color.Black  // End color (teal)
                                    )
                                )
                            )
                    ) {
                        topLevelRoutes.forEachIndexed { index, topLevelRoute ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = topLevelRoute.title
                                    )
                                },
                                label = { Text(topLevelRoute.title) },
                                selected = index == selectedIndex,
                                onClick = {
                                    selectedIndex = index
                                    navController.navigate(topLevelRoute.route) {
                                        // Navigate while preserving state
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun NavigationHost(
    navController: NavHostController,
    ktorClient: KtorClient,
    innerPadding: PaddingValues,
    modifier: Modifier,
    currentPageTitle: MutableState<String>,


) {
    NavHost(
        navController = navController,
        startDestination = "home_screen",
        modifier = modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        composable(route="home_screen") {
            currentPageTitle.value = "All Characters"
            HomeScreen(
                onCharacterSelected = { characterId ->
                    navController.navigate("character_details/$characterId")
                },
            )
        }
        composable(
            route = "character_details/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
        ) { backStackEntry ->
            currentPageTitle.value = "Characters Details"
            val characterId: Int = backStackEntry.arguments?.getInt("characterId") ?: -1
            CharacterDetailsScreen(
                characterId = characterId,
                onEpisodeClicked = {
                    navController.navigate("character_episodes/$characterId")
                },
                onBackClicked = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = "character_episodes/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
        ) { backStackEntry ->
            currentPageTitle.value = "Characters Episodes"
            val characterId: Int = backStackEntry.arguments?.getInt("characterId") ?: -1
            CharacterEpisodeScreen(
                characterId = characterId,
                ktorClient = ktorClient,
                onBackClicked = { navController.navigateUp() }
            )
        }
        composable(route = NavDestination.Episode.route) {
            currentPageTitle.value = "All Episodes"
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AllEpisodesScreen()
            }
        }
        composable(route = NavDestination.Search.route) {
            currentPageTitle.value = "Search"
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SearchScreen(
                    onCharacterClicked = {characterId ->
                        navController.navigate("character_details/$characterId")
                    }
                )
            }
        }
    }
}
