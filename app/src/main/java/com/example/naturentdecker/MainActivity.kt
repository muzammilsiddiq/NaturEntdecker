package com.example.naturentdecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naturentdecker.features.tour.detail.TourDetailViewModel
import com.example.naturentdecker.features.tour.list.ToursViewModel
import com.example.naturentdecker.ui.theme.AppTheme
import com.example.naturentdecker.features.tour.detail.TourDetailScreen
import com.example.naturentdecker.features.tour.list.ToursListScreen
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
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val toursUiState by toursViewModel.uiState.collectAsState()
    val tourDetailUiState by tourDetailViewModel.uiState.collectAsState()

    if (isLandscape) {
        var selectedTourId by remember { mutableStateOf<Int?>(null) }

        Row(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                tonalElevation = 1.dp
            ) {
                Column {
                    TopAppBar(title = { Text("NaturEntdecker") })
                    ToursListScreen(
                        uiState = toursUiState,
                        onTourClick = { id ->
                            selectedTourId = id
                            tourDetailViewModel.loadTour(id)
                        },
                        onToggleTop5 = toursViewModel::toggleTop5,
                        onRetry = { toursViewModel.loadTours(toursUiState.showTop5) },
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTourId == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Select a tour to view details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    TourDetailScreen(
                        uiState = tourDetailUiState,
                        onBack = null, // No back button in landscape two-pane
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    } else {
        // Portrait
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "tours") {
            composable("tours") {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("NaturEntdecker") })
                    }
                ) { padding ->
                    ToursListScreen(
                        uiState = toursUiState,
                        onTourClick = { id ->
                            tourDetailViewModel.loadTour(id)
                            navController.navigate("tour/$id")
                        },
                        onToggleTop5 = toursViewModel::toggleTop5,
                        onRetry = { toursViewModel.loadTours(toursUiState.showTop5) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            composable(
                route = "tour/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                LaunchedEffect(id) { tourDetailViewModel.loadTour(id) }

                TourDetailScreen(
                    uiState = tourDetailUiState,
                    onBack = {
                        tourDetailViewModel.clear()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
