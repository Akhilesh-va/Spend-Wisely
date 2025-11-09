package com.example.mindfullexpenses.ui.screens.dashboard

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import com.example.mindfullexpenses.ui.theme.MindfullExpensesTheme
import kotlinx.coroutines.flow.collect

@Composable
fun DashboardScreen(
    onRequestEnableAutoTracking: () -> Unit = {},
    onRequestAdjustBudget: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshNotificationAccess()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DashboardEvent.RequestEnableAutoTracking -> {
                    Log.i("DashboardScreen", "Event: enable auto-tracking")
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                        Toast.makeText(
                            context,
                            context.getString(R.string.enable_auto_tracking),
                            Toast.LENGTH_SHORT
                        ).show()
                        onRequestEnableAutoTracking()
                    }.onFailure {
                        Toast.makeText(
                            context,
                            context.getString(R.string.dashboard_enable_auto_tracking_failed, it.message ?: "Unknown"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                DashboardEvent.BudgetUpdated -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.dashboard_budget_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    onRequestAdjustBudget()
                }

                is DashboardEvent.BudgetUpdateFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.dashboard_budget_update_failed, event.reason),
                        Toast.LENGTH_LONG
                    ).show()
                }

                DashboardEvent.ManualExpenseDeleted -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.dashboard_manual_delete_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is DashboardEvent.ManualExpenseDeleteFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.dashboard_manual_delete_failed, event.reason),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    DashboardContent(
        uiState = uiState,
        onEnableAutoTracking = viewModel::onEnableAutoTrackingClicked,
        onAdjustBudget = viewModel::onAdjustBudgetClicked,
        onManualExpenseDelete = viewModel::onDeleteManualExpenseRequested,
        onBudgetInputChange = viewModel::onBudgetInputChanged,
        onBudgetDialogDismiss = viewModel::onBudgetDialogDismissed,
        onBudgetDialogSave = viewModel::onBudgetSaveConfirmed
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onEnableAutoTracking: () -> Unit,
    onAdjustBudget: () -> Unit,
    onManualExpenseDelete: (Long) -> Unit,
    onBudgetInputChange: (String) -> Unit,
    onBudgetDialogDismiss: () -> Unit,
    onBudgetDialogSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.showBudgetDialog) {
        BudgetDialog(
            amountInput = uiState.budgetInput,
            isSaving = uiState.isUpdatingBudget,
            errorMessage = uiState.budgetInputError,
            onInputChange = onBudgetInputChange,
            onDismiss = onBudgetDialogDismiss,
            onSave = onBudgetDialogSave
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        .size(42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(8.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(id = R.string.dashboard_greeting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!uiState.hasNotificationAccess) {
            item {
                AutoTrackingStatusCard(
                    isTracking = uiState.isTrackingEnabled,
                    trackedCount = uiState.trackedCountToday,
                    onEnableAutoTracking = onEnableAutoTracking
                )
            }
        }
        item {
            BudgetProgressCard(
                progress = uiState.budgetProgress,
                onAdjustBudget = onAdjustBudget
            )
        }
        uiState.manualEntryHintRes?.let { resId ->
            item {
                ManualEntryHintCard(resId)
            }
        }
        item {
            Text(
                text = stringResource(id = R.string.today_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        if (uiState.recentExpenses.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            items(uiState.recentExpenses, key = { it.id }) { expense ->
                ExpenseTimelineRow(
                    item = expense,
                    showDelete = expense.source == ExpenseSourceUi.Manual,
                    onDelete = { onManualExpenseDelete(expense.id) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun AutoTrackingStatusCard(
    isTracking: Boolean,
    trackedCount: Int,
    onEnableAutoTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (isTracking) Icons.Rounded.CheckCircle else Icons.Rounded.PauseCircle
    val title = if (isTracking) {
        stringResource(R.string.auto_tracking_active)
    } else {
        stringResource(R.string.auto_tracking_inactive)
    }
    val chipLabel = stringResource(R.string.expenses_tracked_today, trackedCount)
    val background = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.primary
        )
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .background(background, shape = MaterialTheme.shapes.extraLarge),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = chipLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
            AnimatedVisibility(visible = !isTracking) {
                AssistChip(
                    onClick = onEnableAutoTracking,
                    label = {
                        Text(text = stringResource(id = R.string.enable_auto_tracking))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressCard(
    progress: BudgetProgressUi,
    onAdjustBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(96.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = progress.spentLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = progress.remainingLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.daily_budget_progress, progress.spentLabel, progress.limitLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.remaining_budget_today, progress.remainingLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = onAdjustBudget,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.ManageAccounts,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.dashboard_adjust_budget)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ManualEntryHintCard(@StringRes hintRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoGraph,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = hintRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BudgetDialog(
    amountInput: String,
    isSaving: Boolean,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = stringResource(id = R.string.dashboard_budget_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(text = stringResource(id = R.string.dashboard_budget_cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.dashboard_budget_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onInputChange,
                    label = { Text(text = stringResource(id = R.string.dashboard_budget_hint)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun ExpenseTimelineRow(
    item: ExpenseTimelineItemUi,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    val (icon, tint) = when (item.source) {
        ExpenseSourceUi.AutoTracked -> Icons.Rounded.TaskAlt to MaterialTheme.colorScheme.primary
        ExpenseSourceUi.Manual -> Icons.AutoMirrored.Rounded.NoteAdd to MaterialTheme.colorScheme.tertiary
        ExpenseSourceUi.Refund -> Icons.Rounded.Cached to MaterialTheme.colorScheme.secondary
    }
    ListItem(
        headlineContent = {
            Text(
                text = item.amountLabel,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = "${item.merchant} • ${item.time}",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        },
        overlineContent = {
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = if (showDelete) {
            {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(id = R.string.dashboard_manual_delete)
                    )
                }
            }
        } else null
    )
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.empty_state_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.empty_state_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Immutable
data class DashboardUiState(
    val isTrackingEnabled: Boolean = true,
    val trackedCountToday: Int = 0,
    val budgetProgress: BudgetProgressUi = BudgetProgressUi(),
    @StringRes val manualEntryHintRes: Int? = null,
    val recentExpenses: List<ExpenseTimelineItemUi> = emptyList(),
    val budgetLimitMinor: Long = ExpenseRepository.DEFAULT_DAILY_LIMIT_MINOR,
    val showBudgetDialog: Boolean = false,
    val budgetInput: String = "",
    val budgetInputError: String? = null,
    val isUpdatingBudget: Boolean = false,
    val hasNotificationAccess: Boolean = false
)

@Immutable
data class BudgetProgressUi(
    val spentLabel: String = "₹0",
    val limitLabel: String = "₹0",
    val remainingLabel: String = "₹0",
    val progress: Float = 0f
)

@Immutable
data class ExpenseTimelineItemUi(
    val id: Long,
    val merchant: String,
    val amountLabel: String,
    val category: String,
    val time: String,
    val source: ExpenseSourceUi
)

enum class ExpenseSourceUi {
    AutoTracked,
    Manual,
    Refund
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    MindfullExpensesTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isTrackingEnabled = true,
                trackedCountToday = 4,
                budgetProgress = BudgetProgressUi(
                    spentLabel = "₹720",
                    limitLabel = "₹1,000",
                    remainingLabel = "₹280",
                    progress = 0.72f
                ),
                manualEntryHintRes = R.string.manual_entry_prompt,
                recentExpenses = sampleExpenses(),
                showBudgetDialog = true,
                budgetInput = "1000.00"
            ),
            onEnableAutoTracking = {},
            onAdjustBudget = {},
            onManualExpenseDelete = {},
            onBudgetInputChange = {},
            onBudgetDialogDismiss = {},
            onBudgetDialogSave = {}
        )
    }
}

private fun sampleExpenses() = listOf(
    ExpenseTimelineItemUi(
        id = 1,
        merchant = "Starbucks",
        amountLabel = "₹260",
        category = "Food & Dining",
        time = "09:12 AM",
        source = ExpenseSourceUi.AutoTracked
    ),
    ExpenseTimelineItemUi(
        id = 2,
        merchant = "Delhi Metro",
        amountLabel = "₹60",
        category = "Transport",
        time = "08:45 AM",
        source = ExpenseSourceUi.Manual
    )
)


