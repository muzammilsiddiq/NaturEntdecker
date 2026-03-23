package com.example.naturentdecker.features.tour.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import com.example.naturentdecker.R
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.ui.components.ErrorState
import com.example.naturentdecker.ui.components.ShimmerTourList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToursListScreen(
    uiState: ToursUiState,
    onTourClick: (Int) -> Unit,
    onToggleTop5: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = if (uiState.showTop5) 1 else 0) {
            Tab(
                selected = !uiState.showTop5,
                onClick = { if (uiState.showTop5) onToggleTop5() },
                text = { Text(stringResource(R.string.tab_all_tours)) }
            )
            Tab(
                selected = uiState.showTop5,
                onClick = { if (!uiState.showTop5) onToggleTop5() },
                text = { Text(stringResource(R.string.tab_top_5)) }
            )
        }

        when {
            uiState.isLoading && uiState.tours.isEmpty() -> {
                ShimmerTourList()
            }

            uiState.error != null && uiState.tours.isEmpty() -> {
                ErrorState(
                    message = uiState.error,
                    onRetry = onRefresh,
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(uiState.tours, key = { it.id }) { tour ->
                            TourCard(tour = tour, onClick = { onTourClick(tour.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TourCard(tour: Tour, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            if (!tour.thumb.isNullOrEmpty()) {
                AsyncImage(
                    model = tour.thumb,
                    contentDescription = tour.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = tour.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(6.dp))

                tour.price.let {
                    val price = it.toDoubleOrNull()?.let { p -> "€%.2f".format(p) } ?: "€$it"

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
