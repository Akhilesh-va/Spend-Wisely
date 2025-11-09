package com.example.mindfullexpenses.ui.screens.manual

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.ui.theme.MindfullExpensesTheme

@Composable
fun ManualEntryScreen(
    viewModel: ManualEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ManualEntryContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onMerchantChanged = viewModel::onMerchantChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::onSaveExpense
    )
}

@Composable
private fun ManualEntryContent(
    uiState: ManualEntryUiState,
    onAmountChanged: (String) -> Unit,
    onMerchantChanged: (String) -> Unit,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
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
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_in_app),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.manual_entry_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = stringResource(id = R.string.manual_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    AmountField(value = uiState.amount, onAmountChanged = onAmountChanged)
                    CategorySection(uiState = uiState, onCategorySelected = onCategorySelected)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.merchant,
                        onValueChange = onMerchantChanged,
                        label = { Text(text = stringResource(id = R.string.manual_merchant_label)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        value = uiState.notes,
                        onValueChange = onNotesChanged,
                        label = { Text(text = stringResource(id = R.string.manual_notes_label)) },
                        maxLines = 4
                    )
                    SaveButton(uiState = uiState, onSave = onSave)
                }
            }
        }
        if (uiState.suggestedMerchants.isNotEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.manual_recent_merchants),
                            style = MaterialTheme.typography.titleSmall
                        )
                        uiState.suggestedMerchants.forEach { merchant ->
                            TextButton(onClick = { onMerchantChanged(merchant) }) {
                                Text(text = merchant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountField(value: String, onAmountChanged: (String) -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            Color.Transparent
        )
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.manual_amount_label),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onAmountChanged,
                placeholder = { Text(text = "0.00") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
private fun CategorySection(
    uiState: ManualEntryUiState,
    onCategorySelected: (ExpenseCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(id = R.string.manual_category_label),
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            uiState.availableCategories.forEach { category ->
                val isSelected = category == uiState.selectedCategory
                AssistChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    uiState: ManualEntryUiState,
    onSave: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.isSaveEnabled && !uiState.isSaving,
        onClick = onSave,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (uiState.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = stringResource(id = R.string.manual_save_cta))
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualEntryPreview() {
    MindfullExpensesTheme {
        ManualEntryContent(
            uiState = ManualEntryUiState(
                amount = "250",
                merchant = "Local Cafe",
                selectedCategory = ExpenseCategory.FOOD_AND_DINING,
                notes = "Breakfast with friends",
                isSaveEnabled = true,
                suggestedMerchants = listOf("Domino's", "Zomato", "Starbucks"),
                feedback = ManualEntryFeedback.Success("Expense saved")
            ),
            onAmountChanged = {},
            onMerchantChanged = {},
            onCategorySelected = {},
            onNotesChanged = {},
            onSave = {}
        )
    }
}


