package com.example.rmcfrontend.compose.screens.terms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.compose.viewmodel.TermsItem
import com.example.rmcfrontend.compose.viewmodel.TermsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    vm: TermsViewModel,
    onBack: (() -> Unit)? = null
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    var showCreate by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<TermsItem?>(null) }
    var deleteItem by remember { mutableStateOf<TermsItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreate = true },
                        modifier = Modifier.testTag("terms_add")
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            state.errorMessage?.let { msg ->
                AssistChip(
                    onClick = { },
                    label = { Text(msg) }
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.items.isEmpty()) {
                EmptyTerms(onAdd = { showCreate = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items, key = { it.id }) { item ->
                        TermsCard(
                            item = item,
                            onSetActive = { vm.setActive(item.id) },
                            onEdit = { editItem = item },
                            onDelete = { deleteItem = item }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showCreate) {
        TermsEditDialog(
            title = "Create Terms",
            initialTitle = "",
            initialContent = "",
            onDismiss = { showCreate = false },
            onSave = { t, c ->
                vm.create(t, c)
                showCreate = false
            }
        )
    }

    editItem?.let { item ->
        TermsEditDialog(
            title = "Edit Terms",
            initialTitle = item.title,
            initialContent = item.content,
            onDismiss = { editItem = null },
            onSave = { t, c ->
                vm.update(item.id, t, c)
                editItem = null
            }
        )
    }

    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete Terms") },
            text = { Text("Are you sure you want to delete '${item.title.ifBlank { "(Untitled)" }}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.delete(item.id)
                        deleteItem = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EmptyTerms(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "No Terms & Conditions yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Create your first Terms text. You can have only one active Terms per user.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onAdd,
                modifier = Modifier.testTag("terms_empty_create")
            ) { Text("Create Terms") }
        }
    }
}

@Composable
private fun TermsCard(
    item: TermsItem,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {

            // Title + ACTIVE pill (never wraps)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    item.title.ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (item.isActive) {
                    Spacer(Modifier.width(10.dp))
                    ActivePill()
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                item.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Updated: ${item.modifiedAt ?: item.createdAt ?: "-"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onSetActive,
                    enabled = !item.isActive
                ) {
                    Icon(
                        Icons.Outlined.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (item.isActive) "Active" else "Set Active",
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun ActivePill() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                Icons.Outlined.Verified,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "ACTIVE",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun TermsEditDialog(
    title: String,
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var t by remember { mutableStateOf(initialTitle) }
    var c by remember { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terms_title_field")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = c,
                    onValueChange = { c = it },
                    label = { Text("Content") },
                    minLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terms_content_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(t, c) },
                enabled = t.isNotBlank() && c.isNotBlank(),
                modifier = Modifier.testTag("terms_save_button")
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("terms_cancel_button")
            ) { Text("Cancel") }
        }
    )
}
