package br.com.gestahub.ui.maternitybag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Enum para os tipos de restauração
private enum class RestoreType {
    RESTORE_MISSING,
    RESET_ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaternityBagScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: MaternityBagViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRestoreDialog by remember { mutableStateOf(false) }

    if (showRestoreDialog) {
        RestoreDialog(
            onDismiss = { showRestoreDialog = false },
            onConfirm = { restoreType ->
                if (restoreType == RestoreType.RESTORE_MISSING) {
                    viewModel.restoreMissingDefaults()
                } else {
                    viewModel.resetToDefaults()
                }
                showRestoreDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mala Maternidade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showRestoreDialog = true }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar Padrão")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.listData != null -> {
                    MaternityBagContent(
                        listData = uiState.listData!!,
                        checkedItems = uiState.checkedItems,
                        isDarkTheme = isDarkTheme,
                        onToggleItem = viewModel::toggleItem,
                        onAddItem = viewModel::addItem,
                        onRemoveItem = viewModel::removeItem
                    )
                }
            }
        }
    }
}

@Composable
fun MaternityBagContent(
    listData: MaternityBagList,
    checkedItems: List<String>,
    isDarkTheme: Boolean,
    onToggleItem: (String) -> Unit,
    onAddItem: (categoryId: String, label: String) -> Unit,
    onRemoveItem: (categoryId: String, itemId: String) -> Unit
) {
    val categories = mapOf(
        "mom" to listData.mom,
        "baby" to listData.baby,
        "companion" to listData.companion,
        "docs" to listData.docs
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories.entries.toList(), key = { it.key }) { (categoryId, category) ->
            CategoryCard(
                categoryId = categoryId,
                category = category,
                checkedItems = checkedItems,
                isDarkTheme = isDarkTheme,
                onToggleItem = onToggleItem,
                onAddItem = onAddItem,
                onRemoveItem = onRemoveItem
            )
        }
    }
}

@Composable
fun CategoryCard(
    categoryId: String,
    category: MaternityBagCategory,
    checkedItems: List<String>,
    isDarkTheme: Boolean,
    onToggleItem: (String) -> Unit,
    onAddItem: (categoryId: String, label: String) -> Unit,
    onRemoveItem: (categoryId: String, itemId: String) -> Unit
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Adicionar Item") },
            text = {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Nome do item") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    onAddItem(categoryId, newItemText)
                    newItemText = ""
                    showAddItemDialog = false
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showAddItemDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Item")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            category.items.forEach { item ->
                ChecklistItem(
                    item = item,
                    isChecked = checkedItems.contains(item.id),
                    onToggle = { onToggleItem(item.id) },
                    onRemove = { onRemoveItem(categoryId, item.id) }
                )
            }
        }
    }
}

@Composable
fun ChecklistItem(
    item: MaternityBagItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.scale(1.2f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remover Item", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RestoreDialog(
    onDismiss: () -> Unit,
    onConfirm: (RestoreType) -> Unit
) {
    var selectedOption by remember { mutableStateOf(RestoreType.RESTORE_MISSING) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Lista Padrão") },
        text = {
            Column {
                Text("Escolha como você quer restaurar a lista:")
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = RestoreType.RESTORE_MISSING }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedOption == RestoreType.RESTORE_MISSING,
                            onClick = { selectedOption = RestoreType.RESTORE_MISSING }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Apenas restaurar itens padrão que foram removidos.", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = RestoreType.RESET_ALL }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedOption == RestoreType.RESET_ALL,
                            onClick = { selectedOption = RestoreType.RESET_ALL }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Limpar tudo e recomeçar com a lista padrão (itens personalizados serão apagados).", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedOption) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}