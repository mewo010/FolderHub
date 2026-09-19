package com.example.data.repository

import com.example.data.dao.HubDao
import com.example.data.dao.HubItemDao
import com.example.data.model.HubEntity
import com.example.data.model.HubItemEntity
import com.example.data.model.HubItemType
import com.example.data.model.HubWithItems
import com.example.data.model.InstalledAppInfo
import kotlinx.coroutines.flow.Flow

class HubRepository(
    private val hubDao: HubDao,
    private val hubItemDao: HubItemDao
) {
    val allHubsWithItems: Flow<List<HubWithItems>> = hubDao.getAllHubsWithItems()
    val allHubs: Flow<List<HubEntity>> = hubDao.getAllHubs()

    suspend fun createHub(name: String, iconKey: String = "folder", colorHex: String = "#4F46E5"): Long {
        return hubDao.insertHub(
            HubEntity(
                name = name,
                iconKey = iconKey,
                colorHex = colorHex
            )
        )
    }

    suspend fun updateHub(hub: HubEntity) {
        hubDao.updateHub(hub)
    }

    suspend fun deleteHub(hubId: Long) {
        hubDao.deleteHubById(hubId)
    }

    suspend fun addItem(
        hubId: Long,
        title: String,
        type: HubItemType,
        dataUri: String,
        secondaryInfo: String = "",
        iconIdentifier: String = ""
    ): Long {
        return hubItemDao.insertItem(
            HubItemEntity(
                hubId = hubId,
                title = title,
                type = type,
                dataUri = dataUri,
                secondaryInfo = secondaryInfo,
                iconIdentifier = iconIdentifier
            )
        )
    }

    suspend fun deleteItem(itemId: Long) {
        hubItemDao.deleteItemById(itemId)
    }

    suspend fun seedDefaultsIfEmpty(installedApps: List<InstalledAppInfo>) {
        if (hubDao.getHubCount() > 0) return

        // Seed Starter Hub 1: Media & Watching
        val mediaHubId = hubDao.insertHub(
            HubEntity(
                name = "Media & Watching",
                iconKey = "media",
                colorHex = "#7C3AED",
                displayOrder = 0
            )
        )

        // Seed Starter Hub 2: Gaming Hub
        val gamingHubId = hubDao.insertHub(
            HubEntity(
                name = "Gaming Hub",
                iconKey = "gaming",
                colorHex = "#F59E0B",
                displayOrder = 1
            )
        )

        // Seed Starter Hub 3: Shortcuts & Tools
        val shortcutsHubId = hubDao.insertHub(
            HubEntity(
                name = "Shortcuts & Tools",
                iconKey = "shortcuts",
                colorHex = "#059669",
                displayOrder = 2
            )
        )

        // Pre-populate items
        val starterItems = mutableListOf<HubItemEntity>()

        // 1. Media Hub items
        starterItems.add(
            HubItemEntity(
                hubId = mediaHubId,
                title = "YouTube",
                type = HubItemType.WEB_LINK,
                dataUri = "https://www.youtube.com",
                secondaryInfo = "youtube.com"
            )
        )
        starterItems.add(
            HubItemEntity(
                hubId = mediaHubId,
                title = "Twitch",
                type = HubItemType.WEB_LINK,
                dataUri = "https://www.twitch.tv",
                secondaryInfo = "twitch.tv"
            )
        )
        starterItems.add(
            HubItemEntity(
                hubId = mediaHubId,
                title = "Netflix",
                type = HubItemType.WEB_LINK,
                dataUri = "https://www.netflix.com",
                secondaryInfo = "netflix.com"
            )
        )

        // 2. Gaming Hub items
        starterItems.add(
            HubItemEntity(
                hubId = gamingHubId,
                title = "Discord",
                type = HubItemType.WEB_LINK,
                dataUri = "https://discord.com/app",
                secondaryInfo = "discord.com"
            )
        )
        starterItems.add(
            HubItemEntity(
                hubId = gamingHubId,
                title = "Steam Community",
                type = HubItemType.WEB_LINK,
                dataUri = "https://steamcommunity.com",
                secondaryInfo = "steamcommunity.com"
            )
        )
        starterItems.add(
            HubItemEntity(
                hubId = gamingHubId,
                title = "IGN News",
                type = HubItemType.WEB_LINK,
                dataUri = "https://www.ign.com",
                secondaryInfo = "ign.com"
            )
        )

        // 3. Shortcuts & Tools items
        starterItems.add(
            HubItemEntity(
                hubId = shortcutsHubId,
                title = "GitHub",
                type = HubItemType.WEB_LINK,
                dataUri = "https://github.com",
                secondaryInfo = "github.com"
            )
        )
        starterItems.add(
            HubItemEntity(
                hubId = shortcutsHubId,
                title = "Wikipedia",
                type = HubItemType.WEB_LINK,
                dataUri = "https://www.wikipedia.org",
                secondaryInfo = "wikipedia.org"
            )
        )

        // If installed apps exist, associate some common ones into the hubs
        installedApps.forEach { app ->
            val pkgLower = app.packageName.lowercase()
            val labelLower = app.label.lowercase()
            if (pkgLower.contains("youtube") || labelLower.contains("youtube") ||
                labelLower.contains("camera") || labelLower.contains("gallery") || labelLower.contains("photos")) {
                starterItems.add(
                    HubItemEntity(
                        hubId = mediaHubId,
                        title = app.label,
                        type = HubItemType.APP,
                        dataUri = app.packageName,
                        secondaryInfo = app.packageName
                    )
                )
            } else if (labelLower.contains("settings") || labelLower.contains("clock") || labelLower.contains("calculator") ||
                labelLower.contains("files") || labelLower.contains("browser") || labelLower.contains("chrome")) {
                starterItems.add(
                    HubItemEntity(
                        hubId = shortcutsHubId,
                        title = app.label,
                        type = HubItemType.APP,
                        dataUri = app.packageName,
                        secondaryInfo = app.packageName
                    )
                )
            }
        }

        hubItemDao.insertItems(starterItems)
    }
}
