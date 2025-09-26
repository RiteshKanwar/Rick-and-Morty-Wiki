package com.ritesh.rickmortywiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.ritesh.rickmortywiki.screens.AllEpisodesScreen
import com.ritesh.rickmortywiki.screens.SearchScreen
import javax.inject.Inject

sealed class NavDestination(val title: String, val route: String, val icon: ImageVector) {
    object Home : NavDestination("Home", "home_screen", Icons.Rounded.Home)
    object Episode : NavDestination("Episodes", "episodes", Icons.Rounded.PlayArrow)
    object Search : NavDestination("Search", "search", Icons.Rounded.Search)
}

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var ktorClient: KtorClient

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val topLevelRoutes = listOf(
                NavDestination.Home, NavDestination.Episode, NavDestination.Search
            )
            var selectedIndex by remember { mutableIntStateOf(0) }
            val currentPageTitle = remember { mutableStateOf("App") }
            var isDarkTheme by remember { mutableStateOf(false) }


            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { isDarkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { isDarkTheme }
                    )
                )
            }

            val onBackAction by remember(currentPageTitle.value) {
                derivedStateOf {
                    if (currentPageTitle.value == "All Characters" ||
                        currentPageTitle.value == "All Episodes" ||
                        currentPageTitle.value == "Search") {
                        null
                    } else {
                        { navController.popBackStack() }
                    }
                }
            }

            RickMortyWikiTheme(darkTheme = isDarkTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    NavigationHost(
                        navController = navController,
                        ktorClient = ktorClient,
                        innerPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        currentPageTitle = currentPageTitle
                    )

                    TopAppBar(
                        title = {
                            Text(
                                text = currentPageTitle.value,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        },
                        navigationIcon = {
                            onBackAction?.let { backAction ->
                                IconButton(onClick = { backAction() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        actions = {
                            // Dark mode toggle with icons
                            IconButton(
                                onClick = { isDarkTheme = !isDarkTheme },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) {
                                        Icons.Filled.LightMode
                                    } else {
                                        Icons.Filled.DarkMode
                                    },
                                    contentDescription = if (isDarkTheme) {
                                        "Switch to light mode"
                                    } else {
                                        "Switch to dark mode"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )

                    // Custom Pill Navigation Bar
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 10.dp, vertical = 15.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 12.dp,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            topLevelRoutes.forEachIndexed { index, topLevelRoute ->
                                val isSelected = index == selectedIndex

                                Surface(
                                    onClick = {
                                        selectedIndex = index
                                        navController.navigate(topLevelRoute.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                    else
                                        Color.Transparent
                                ) {
                                    // Use AnimatedContent for smoother transitions
                                    AnimatedContent(
                                        targetState = isSelected,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(300)) togetherWith
                                                    fadeOut(animationSpec = tween(300))
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 18.dp
                                        )
                                    ) { selected ->
                                        if (selected) {
                                            // Selected state with icon and text
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = topLevelRoute.icon,
                                                    contentDescription = topLevelRoute.title,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = when (topLevelRoute.title) {
                                                        "Episodes" -> "Episodes"
                                                        else -> topLevelRoute.title
                                                    },
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        } else {
                                            // Unselected state with only icon
                                            Icon(
                                                imageVector = topLevelRoute.icon,
                                                contentDescription = topLevelRoute.title,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
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
