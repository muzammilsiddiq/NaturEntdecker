package com.example.naturentdecker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naturentdecker.features.tour.detail.TourDetailScreen
import com.example.naturentdecker.features.tour.detail.TourDetailViewModel
import com.example.naturentdecker.features.tour.list.ToursListScreen
import com.example.naturentdecker.features.tour.list.ToursViewModel
import com.example.naturentdecker.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val toursViewModel: ToursViewModel by viewModels()
    private val tourDetailViewModel: TourDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                NaturEntdeckerApp(toursViewModel, tourDetailViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaturEntdeckerApp(
    toursViewModel: ToursViewModel,
    tourDetailViewModel: TourDetailViewModel,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val toursUiState by toursViewModel.uiState.collectAsStateWithLifecycle()
    val tourDetailUiState by tourDetailViewModel.uiState.collectAsStateWithLifecycle()

    if (isLandscape) {
        var selectedTourId by remember { mutableStateOf<Int?>(null) }

        BackHandler(enabled = selectedTourId != null) {
            selectedTourId = null
            tourDetailViewModel.clear()
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
                            tourDetailViewModel.loadTour(id)
                        },
                        onToggleTop5 = toursViewModel::toggleTop5,
                        onRefresh = toursViewModel::refresh,
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "NaturEntdecker",
                            modifier = Modifier.size(180.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Select a tour to view details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    TourDetailScreen(
                        uiState = tourDetailUiState,
                        onBack = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    } else {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "tours") {
            composable("tours") {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name))  },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    },
                ) { padding ->
                    ToursListScreen(
                        uiState = toursUiState,
                        onTourClick = { id ->
                            tourDetailViewModel.loadTour(id)
                            navController.navigate("tour/$id")
                        },
                        onToggleTop5 = toursViewModel::toggleTop5,
                        onRefresh = toursViewModel::refresh,
                        modifier = Modifier.padding(padding),
                    )
                }
            }

            composable(
                route = "tour/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                LaunchedEffect(id) { tourDetailViewModel.loadTour(id) }
                TourDetailScreen(
                    uiState = tourDetailUiState,
                    onBack = {
                        tourDetailViewModel.clear()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
