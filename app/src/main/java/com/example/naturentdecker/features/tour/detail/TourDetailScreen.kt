package com.example.naturentdecker.features.tour.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.naturentdecker.R
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.model.formattedPrice
import com.example.naturentdecker.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    modifier: Modifier = Modifier,
    uiState: TourDetailUiState,
    onBack: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.tour?.title
                                ?: stringResource(R.string.tour_detail_default_title),
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            stringResource(R.string.tour_detail_error_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            uiState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.tour != null -> {
                TourDetailContent(
                    tour = uiState.tour,
                    contactPhone = uiState.contactPhone,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
fun TourDetailContent(
    modifier: Modifier = Modifier,
    tour: Tour,
    contactPhone: String? = null,
) {
    val context = LocalContext.current

    val priceLabel = stringResource(R.string.stat_label_price)
    val datesLabel = stringResource(R.string.stat_label_dates)

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {

        if (!tour.thumb.isNullOrEmpty()) {
            AsyncImage(
                model = tour.thumb,
                contentDescription = tour.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = tour.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            val statsItems = buildList {
                add(Pair(Icons.Default.Euro, priceLabel to tour.formattedPrice))
                val dateRange = DateUtils.formatTourDateRange(tour.startDate, tour.endDate)
                add(Pair(Icons.Default.CalendarMonth, datesLabel to dateRange))

            }

            if (statsItems.isNotEmpty()) {
                val dateRange = DateUtils.formatTourDateRange(tour.startDate, tour.endDate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Euro,
                        label = priceLabel,
                        value = tour.formattedPrice,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.CalendarMonth,
                        label = datesLabel,
                        value = dateRange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            tour.shortDescription?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tour_detail_about_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            Card(
                onClick = {
                    if (!contactPhone.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$contactPhone")
                        }
                        context.startActivity(intent)
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.booking_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (!contactPhone.isNullOrBlank()) contactPhone
                            else stringResource(R.string.booking_card_fallback_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}