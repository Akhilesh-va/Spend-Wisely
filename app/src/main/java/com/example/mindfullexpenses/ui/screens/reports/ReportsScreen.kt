package com.example.mindfullexpenses.ui.screens.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.ui.theme.MindfullExpensesTheme

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportsContent(
        uiState = uiState,
        onPeriodSelected = viewModel::onPeriodSelected
    )
}

@Composable
private fun ReportsContent(
    uiState: ReportsUiState,
    onPeriodSelected: (ReportsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_in_app),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.reports_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(id = R.string.reports_subheading, uiState.selectedPeriod.toDisplayName()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            PeriodSelector(
                selected = uiState.selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        }
        item {
            OverviewCard(uiState)
        }
        item {
            EngagementCard(uiState)
        }
        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                CategoryBreakdownCard(breakdown = uiState.categoryBreakdown)
            }
        }
        if (uiState.series.isNotEmpty()) {
            items(uiState.series, key = { it.title }) { series ->
                ReportSeriesCard(series)
            }
        }
        if (uiState.highlights.isNotEmpty()) {
            item {
                HighlightsCard(highlights = uiState.highlights)
            }
        }
        if (uiState.series.isEmpty() && uiState.highlights.isEmpty() && uiState.categoryBreakdown.isEmpty()) {
            item { EmptyReportsState() }
        }
    }
}

@Composable
private fun OverviewCard(uiState: ReportsUiState) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.primary
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, shape = MaterialTheme.shapes.extraLarge)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.reports_total_spend, uiState.totalSpendLabel),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.reports_accuracy, uiState.accuracyLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Text(
                text = stringResource(id = R.string.reports_transactions_count, uiState.totalTransactions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EngagementCard(uiState: ReportsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.reports_engagement_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.reports_engagement_manual),
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = uiState.manualShare.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = stringResource(id = R.string.reports_engagement_auto),
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = uiState.autoShare.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryBreakdownCard(breakdown: List<CategoryBreakdownUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.reports_section_category_breakdown),
                style = MaterialTheme.typography.titleMedium
            )
            breakdown.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = item.percentLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = item.amountLabel, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: ReportsPeriod,
    onPeriodSelected: (ReportsPeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow {
        ReportsPeriod.values().forEachIndexed { index, period ->
            val selectedState = period == selected
            SegmentedButton(
                selected = selectedState,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index, ReportsPeriod.entries.size)
            ) {
                Text(
                    text = stringResource(id = period.labelRes),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ReportSeriesCard(series: ReportSeriesUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = series.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = series.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HighlightsCard(highlights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.reports_section_highlights),
                style = MaterialTheme.typography.titleMedium
            )
            highlights.forEach { highlight ->
                Text(
                    text = "• $highlight",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun EmptyReportsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.reports_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportsPreview() {
    MindfullExpensesTheme {
        ReportsContent(
            uiState = ReportsUiState(
                selectedPeriod = ReportsPeriod.Daily,
                accuracyLabel = "94%",
                series = listOf(
                    ReportSeriesUi(
                        title = "Today",
                        subtitle = "₹720 spent across 5 transactions"
                    )
                ),
                highlights = listOf(
                    "You spend 30% more on weekends",
                    "Swiggy orders are up ₹500 vs last week"
                ),
                categoryBreakdown = listOf(
                    CategoryBreakdownUi("Food", 0.45f, "₹450", "45%"),
                    CategoryBreakdownUi("Transport", 0.25f, "₹250", "25%")
                ),
                totalSpendLabel = "₹720",
                manualShare = 0.2f,
                autoShare = 0.7f,
                refundCount = 1,
                totalTransactions = 5
            ),
            onPeriodSelected = {}
        )
    }
}

private fun ReportsPeriod.toDisplayName(): String = when (this) {
    ReportsPeriod.Daily -> "today"
    ReportsPeriod.Weekly -> "this week"
    ReportsPeriod.Monthly -> "this month"
}


