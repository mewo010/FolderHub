package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HubEntity
import com.example.data.model.HubItemEntity
import com.example.data.model.HubItemType
import com.example.ui.components.AddItemSheet
import com.example.ui.components.CreateHubDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.HubIconHelper
import com.example.ui.components.HubItemCard
import com.example.ui.components.HubTabBar
import com.example.ui.components.PlayStorePromptDialog
import com.example.ui.viewmodel.HubFilter
import com.example.ui.viewmodel.HubLauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubLauncherScreen(
    viewModel: HubLauncherViewModel,
    modifier: Modifier = Modifier
) {
    val hubsWithItems by viewModel.hubsWithItems.collectAsStateWithLifecycle()
    val selectedHubId by viewModel.selectedHubId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()

    val showCreateHubDialog by viewModel.showCreateHubDialog.collectAsStateWithLifecycle()
    val hubToEdit by viewModel.hubToEdit.collectAsStateWithLifecycle()
    val hubToDelete by viewModel.hubToDelete.collectAsStateWithLifecycle()
    val itemToDelete by viewModel.itemToDelete.collectAsStateWithLifecycle()
    val activeHubForAddItem by viewModel.activeHubForAddItem.collectAsStateWithLifecycle()
    val playStorePrompt by viewModel.playStorePrompt.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    // Identify active hub entity (if one is selected)
    val activeHub = remember(selectedHubId, hubsWithItems) {
        if (selectedHubId != null) {
            hubsWithItems.find { it.hub.id == selectedHubId }?.hub
        } else null
    }

    val activeAccentColor = remember(activeHub) {
        if (activeHub != null) HubIconHelper.parseColor(activeHub.colorHex)
        else Color(0xFF4F46E5)
    }

    // Filter items based on selected hub, filter chips, and search query
    val displayedItems = remember(hubsWithItems, selectedHubId, selectedFilter, searchQuery) {
        val baseItems = if (selectedHubId != null) {
            hubsWithItems.find { it.hub.id == selectedHubId }?.items ?: emptyList()
        } else {
            hubsWithItems.flatMap { it.items }
        }

        baseItems.filter { item ->
            // Filter by type
            val typeMatches = when (selectedFilter) {
                HubFilter.ALL -> true
                HubFilter.APPS -> item.type == HubItemType.APP
                HubFilter.LINKS -> item.type == HubItemType.WEB_LINK
                HubFilter.FILES -> item.type == HubItemType.LOCAL_FILE
            }

            // Filter by search query
            val searchMatches = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.secondaryInfo.contains(searchQuery, ignoreCase = true) ||
                item.dataUri.contains(searchQuery, ignoreCase = true)
            }

            typeMatches && searchMatches
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search apps, links, files...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("top_search_input"),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (searchQuery.isNotEmpty()) {
                                            viewModel.setSearchQuery("")
                                        } else {
                                            isSearchExpanded = false
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Close search")
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(activeAccentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (activeHub != null) HubIconHelper.getIcon(activeHub.iconKey) else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = activeAccentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeHub?.name ?: "Hub Launcher",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (activeHub != null) "Folder Hub" else "Apps & Content Organizer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier.testTag("open_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        IconButton(
                            onClick = { viewModel.showCreateHubDialog(true) },
                            modifier = Modifier.testTag("create_hub_top_button")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Hub")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val target = activeHub ?: hubsWithItems.firstOrNull()?.hub
                    if (target != null) {
                        viewModel.openAddItemSheet(target)
                    } else {
                        viewModel.showCreateHubDialog(true)
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(if (activeHub != null) "Add to ${activeHub.name}" else "Add Item")
                },
                containerColor = activeAccentColor,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_item_fab")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Hub Tab Selector
            HubTabBar(
                hubs = hubsWithItems,
                selectedHubId = selectedHubId,
                onSelectHub = { viewModel.selectHub(it) },
                onAddHubClick = { viewModel.showCreateHubDialog(true) },
                onEditHub = { hub -> viewModel.showCreateHubDialog(true, hub) },
                onDeleteHub = { hub -> viewModel.confirmDeleteHub(hub) }
            )

            // Filter Chips (All, Apps, Links, Files)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == HubFilter.ALL,
                    onClick = { viewModel.setFilter(HubFilter.ALL) },
                    label = { Text("All (${displayedItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeAccentColor.copy(alpha = 0.15f),
                        selectedLabelColor = activeAccentColor
                    ),
                    modifier = Modifier.testTag("filter_all")
                )

                FilterChip(
                    selected = selectedFilter == HubFilter.APPS,
                    onClick = { viewModel.setFilter(HubFilter.APPS) },
                    label = { Text("Apps") },
                    leadingIcon = { Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeAccentColor.copy(alpha = 0.15f),
                        selectedLabelColor = activeAccentColor
                    ),
                    modifier = Modifier.testTag("filter_apps")
                )

                FilterChip(
                    selected = selectedFilter == HubFilter.LINKS,
                    onClick = { viewModel.setFilter(HubFilter.LINKS) },
                    label = { Text("Links") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeAccentColor.copy(alpha = 0.15f),
                        selectedLabelColor = activeAccentColor
                    ),
                    modifier = Modifier.testTag("filter_links")
                )

                FilterChip(
                    selected = selectedFilter == HubFilter.FILES,
                    onClick = { viewModel.setFilter(HubFilter.FILES) },
                    label = { Text("Files") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeAccentColor.copy(alpha = 0.15f),
                        selectedLabelColor = activeAccentColor
                    ),
                    modifier = Modifier.testTag("filter_files")
                )
            }

            // Main Content: Grid of items or empty state
            if (displayedItems.isEmpty()) {
                EmptyStateView(
                    activeHub = activeHub,
                    searchQuery = searchQuery,
                    onAddClick = {
                        val target = activeHub ?: hubsWithItems.firstOrNull()?.hub
                        if (target != null) {
                            viewModel.openAddItemSheet(target)
                        } else {
                            viewModel.showCreateHubDialog(true)
                        }
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("hub_items_grid")
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        HubItemCard(
                            item = item,
                            accentColor = activeAccentColor,
                            onLaunch = { viewModel.onLaunchItem(item) },
                            onDelete = { viewModel.confirmDeleteItem(item) },
                            onOpenPlayStore = if (item.type == HubItemType.APP) {
                                {
                                    viewModel.dismissPlayStorePrompt()
                                    com.example.util.LauncherHelper.openPlayStore(
                                        viewModel.getApplication(),
                                        item.dataUri
                                    )
                                }
                            } else null,
                            modifier = Modifier.height(145.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog: Create or Edit Hub
    if (showCreateHubDialog) {
        CreateHubDialog(
            initialHub = hubToEdit,
            onDismiss = { viewModel.showCreateHubDialog(false) },
            onSave = { name, iconKey, colorHex ->
                viewModel.saveHub(name, iconKey, colorHex)
            }
        )
    }

    // BottomSheet: Add Item to Hub
    activeHubForAddItem?.let { hub ->
        AddItemSheet(
            targetHub = hub,
            installedApps = installedApps,
            isLoadingApps = isLoadingApps,
            onDismiss = { viewModel.closeAddItemSheet() },
            onAddApp = { app -> viewModel.addAppItem(hub.id, app) },
            onAddWebLink = { title, url -> viewModel.addWebLinkItem(hub.id, title, url) },
            onAddFile = { uri -> viewModel.addFileItem(hub.id, uri) }
        )
    }

    // Dialog: Play Store Prompt when app is not installed
    playStorePrompt?.let { prompt ->
        PlayStorePromptDialog(
            prompt = prompt,
            onDismiss = { viewModel.dismissPlayStorePrompt() },
            onOpenPlayStore = { viewModel.confirmOpenPlayStore() }
        )
    }

    // Dialog: Confirm Delete Hub
    hubToDelete?.let { hub ->
        DeleteConfirmDialog(
            title = "Delete Hub \"${hub.name}\"?",
            message = "This will remove the hub folder and all shortcuts inside it. Installed apps on your device will NOT be uninstalled.",
            onDismiss = { viewModel.confirmDeleteHub(null) },
            onConfirm = { viewModel.deleteHub(hub.id) }
        )
    }

    // Dialog: Confirm Delete Item
    itemToDelete?.let { item ->
        DeleteConfirmDialog(
            title = "Remove \"${item.title}\"?",
            message = "Remove this shortcut from the hub?",
            onDismiss = { viewModel.confirmDeleteItem(null) },
            onConfirm = { viewModel.deleteItem(item.id) }
        )
    }
}

@Composable
private fun EmptyStateView(
    activeHub: HubEntity?,
    searchQuery: String,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isNotEmpty()) "No shortcuts found" else if (activeHub != null) "Folder is empty" else "No items in hubs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "No items match \"$searchQuery\""
                } else if (activeHub != null) {
                    "Organize \"${activeHub.name}\" by adding apps, web links, or media files."
                } else {
                    "Create folders and organize your apps, games, links, and documents."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Shortcut")
                }
            }
        }
    }
}
