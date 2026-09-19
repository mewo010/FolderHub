package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.HubEntity
import com.example.data.model.HubItemEntity
import com.example.data.model.HubItemType
import com.example.data.model.HubWithItems
import com.example.data.model.InstalledAppInfo
import com.example.data.repository.HubRepository
import com.example.util.LauncherHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HubFilter {
    ALL,
    APPS,
    LINKS,
    FILES
}

data class PlayStorePrompt(
    val title: String,
    val packageName: String
)

class HubLauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HubRepository
    val hubsWithItems: StateFlow<List<HubWithItems>>

    private val _selectedHubId = MutableStateFlow<Long?>(null)
    val selectedHubId: StateFlow<Long?> = _selectedHubId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HubFilter.ALL)
    val selectedFilter: StateFlow<HubFilter> = _selectedFilter.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _playStorePrompt = MutableStateFlow<PlayStorePrompt?>(null)
    val playStorePrompt: StateFlow<PlayStorePrompt?> = _playStorePrompt.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Dialog & Sheet States
    private val _showCreateHubDialog = MutableStateFlow(false)
    val showCreateHubDialog: StateFlow<Boolean> = _showCreateHubDialog.asStateFlow()

    private val _hubToEdit = MutableStateFlow<HubEntity?>(null)
    val hubToEdit: StateFlow<HubEntity?> = _hubToEdit.asStateFlow()

    private val _hubToDelete = MutableStateFlow<HubEntity?>(null)
    val hubToDelete: StateFlow<HubEntity?> = _hubToDelete.asStateFlow()

    private val _itemToDelete = MutableStateFlow<HubItemEntity?>(null)
    val itemToDelete: StateFlow<HubItemEntity?> = _itemToDelete.asStateFlow()

    private val _activeHubForAddItem = MutableStateFlow<HubEntity?>(null)
    val activeHubForAddItem: StateFlow<HubEntity?> = _activeHubForAddItem.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = HubRepository(db.hubDao(), db.hubItemDao())
        hubsWithItems = repository.allHubsWithItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        loadInstalledAppsAndSeed()
    }

    private fun loadInstalledAppsAndSeed() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = LauncherHelper.loadInstalledApps(getApplication())
            _installedApps.value = apps
            _isLoadingApps.value = false

            // Seed default hubs if database is currently empty
            repository.seedDefaultsIfEmpty(apps)
        }
    }

    fun selectHub(hubId: Long?) {
        _selectedHubId.value = hubId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: HubFilter) {
        _selectedFilter.value = filter
    }

    fun showCreateHubDialog(show: Boolean, editHub: HubEntity? = null) {
        _hubToEdit.value = editHub
        _showCreateHubDialog.value = show
    }

    fun confirmDeleteHub(hub: HubEntity?) {
        _hubToDelete.value = hub
    }

    fun confirmDeleteItem(item: HubItemEntity?) {
        _itemToDelete.value = item
    }

    fun openAddItemSheet(hub: HubEntity?) {
        _activeHubForAddItem.value = hub
    }

    fun closeAddItemSheet() {
        _activeHubForAddItem.value = null
    }

    fun saveHub(name: String, iconKey: String, colorHex: String) {
        viewModelScope.launch {
            val editing = _hubToEdit.value
            if (editing != null) {
                repository.updateHub(
                    editing.copy(
                        name = name.trim(),
                        iconKey = iconKey,
                        colorHex = colorHex
                    )
                )
                _snackbarMessage.value = "Updated Hub \"$name\""
            } else {
                val newId = repository.createHub(
                    name = name.trim(),
                    iconKey = iconKey,
                    colorHex = colorHex
                )
                _selectedHubId.value = newId
                _snackbarMessage.value = "Created Hub \"$name\""
            }
            _showCreateHubDialog.value = false
            _hubToEdit.value = null
        }
    }

    fun deleteHub(hubId: Long) {
        viewModelScope.launch {
            repository.deleteHub(hubId)
            if (_selectedHubId.value == hubId) {
                _selectedHubId.value = null
            }
            _hubToDelete.value = null
            _snackbarMessage.value = "Hub deleted"
        }
    }

    fun addAppItem(hubId: Long, app: InstalledAppInfo) {
        viewModelScope.launch {
            repository.addItem(
                hubId = hubId,
                title = app.label,
                type = HubItemType.APP,
                dataUri = app.packageName,
                secondaryInfo = app.packageName
            )
            _activeHubForAddItem.value = null
            _snackbarMessage.value = "Added ${app.label}"
        }
    }

    fun addWebLinkItem(hubId: Long, title: String, url: String) {
        viewModelScope.launch {
            var cleanUrl = url.trim()
            if (!cleanUrl.startsWith("http://", ignoreCase = true) &&
                !cleanUrl.startsWith("https://", ignoreCase = true)) {
                cleanUrl = "https://$cleanUrl"
            }
            val domain = try {
                val uri = Uri.parse(cleanUrl)
                uri.host?.removePrefix("www.") ?: cleanUrl
            } catch (e: Exception) {
                cleanUrl
            }
            val finalTitle = title.trim().ifEmpty { domain }

            repository.addItem(
                hubId = hubId,
                title = finalTitle,
                type = HubItemType.WEB_LINK,
                dataUri = cleanUrl,
                secondaryInfo = domain
            )
            _activeHubForAddItem.value = null
            _snackbarMessage.value = "Added link: $finalTitle"
        }
    }

    fun addFileItem(hubId: Long, uri: Uri) {
        viewModelScope.launch {
            val (displayName, secondaryInfo) = LauncherHelper.queryFileDetails(getApplication(), uri)
            repository.addItem(
                hubId = hubId,
                title = displayName,
                type = HubItemType.LOCAL_FILE,
                dataUri = uri.toString(),
                secondaryInfo = secondaryInfo
            )
            _activeHubForAddItem.value = null
            _snackbarMessage.value = "Added file: $displayName"
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            _itemToDelete.value = null
            _snackbarMessage.value = "Item removed"
        }
    }

    /**
     * Smart launch item:
     * - If APP: check if installed. If installed, launch. If not, trigger Play Store prompt.
     * - If WEB_LINK: launch browser.
     * - If LOCAL_FILE: open document with persistable URI permission.
     */
    fun onLaunchItem(item: HubItemEntity) {
        val context = getApplication<Application>()
        when (item.type) {
            HubItemType.APP -> {
                val isInstalled = LauncherHelper.isAppInstalled(context, item.dataUri)
                if (isInstalled) {
                    val launched = LauncherHelper.launchApp(context, item.dataUri)
                    if (!launched) {
                        _snackbarMessage.value = "Unable to launch ${item.title}"
                    }
                } else {
                    // Smart detection: Package not found! Prompt user to open Google Play Store
                    _playStorePrompt.value = PlayStorePrompt(
                        title = item.title,
                        packageName = item.dataUri
                    )
                }
            }
            HubItemType.WEB_LINK -> {
                val result = LauncherHelper.openWebLink(context, item.dataUri)
                if (result.isFailure) {
                    _snackbarMessage.value = "Could not open URL: ${item.dataUri}"
                }
            }
            HubItemType.LOCAL_FILE -> {
                val result = LauncherHelper.openDocument(context, item.dataUri, null)
                if (result.isFailure) {
                    _snackbarMessage.value = "Could not open file: ${result.exceptionOrNull()?.localizedMessage ?: "File missing"}"
                }
            }
        }
    }

    fun dismissPlayStorePrompt() {
        _playStorePrompt.value = null
    }

    fun confirmOpenPlayStore() {
        val prompt = _playStorePrompt.value ?: return
        LauncherHelper.openPlayStore(getApplication(), prompt.packageName)
        _playStorePrompt.value = null
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
