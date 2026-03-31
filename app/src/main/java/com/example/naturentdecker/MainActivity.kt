package com.example.naturentdecker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naturentdecker.features.tour.detail.TourDetailScreen
import com.example.naturentdecker.features.tour.detail.TourDetailViewModel
import com.example.naturentdecker.features.tour.list.TourTab
import com.example.naturentdecker.features.tour.list.ToursListScreen
import com.example.naturentdecker.features.tour.list.ToursUiState
import com.example.naturentdecker.features.tour.list.ToursViewModel
import com.example.naturentdecker.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                NaturEntdeckerHome()
            }
        }
    }
}

@Composable
fun NaturEntdeckerHome() {
    val toursViewModel: ToursViewModel = hiltViewModel()
    val toursUiState by toursViewModel.uiState.collectAsStateWithLifecycle()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            toursUiState = toursUiState,
            onTabSelected = toursViewModel::onTabSelected,
            onRefresh = toursViewModel::refresh,
        )
    } else {
        PortraitLayout(
            toursUiState = toursUiState,
            onTabSelected = toursViewModel::onTabSelected,
            onRefresh = toursViewModel::refresh,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
    toursUiState: ToursUiState,
    onTabSelected: (TourTab) -> Unit,
    onRefresh: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tours") {
        composable("tours") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                },
            ) { padding ->
                ToursListScreen(
                    uiState = toursUiState,
                    onTourClick = { id -> navController.navigate("tour/$id") },
                    onTabSelected = onTabSelected,
                    onRefresh = onRefresh,
                    modifier = Modifier.padding(padding),
                )
            }
        }

        composable(
            route = "tour/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { backStackEntry ->
            val viewModel: TourDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            LaunchedEffect(id) { viewModel.loadTour(id) }

            TourDetailScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeLayout(
    toursUiState: ToursUiState,
    onTabSelected: (TourTab) -> Unit,
    onRefresh: () -> Unit,
) {
    var selectedTourId by remember { mutableStateOf<Int?>(null) }
    val detailViewModel: TourDetailViewModel = hiltViewModel()
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedTourId != null) {
        selectedTourId = null
        detailViewModel.clear()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            tonalElevation = 1.dp,
        ) {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )

                ToursListScreen(
                    uiState = toursUiState,
                    onTourClick = { id ->
                        selectedTourId = id
                        detailViewModel.loadTour(id)
                    },
                    onTabSelected = onTabSelected,
                    onRefresh = onRefresh,
                )
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedTourId == null) {
                EmptyDetailPlaceholder()
            } else {
                TourDetailScreen(
                    uiState = detailUiState,
                    onBack = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun EmptyDetailPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(180.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.select_a_tour_to_see_details),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}