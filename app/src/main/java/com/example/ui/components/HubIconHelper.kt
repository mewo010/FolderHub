package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object HubIconHelper {

    val availableIcons = listOf(
        "folder" to Icons.Default.Folder,
        "media" to Icons.Default.PlayArrow,
        "gaming" to Icons.Default.SportsEsports,
        "shortcuts" to Icons.Default.Bookmark,
        "work" to Icons.Default.Work,
        "code" to Icons.Default.Code,
        "music" to Icons.Default.MusicNote,
        "star" to Icons.Default.Star,
        "web" to Icons.Default.Language,
        "docs" to Icons.Default.Description
    )

    val availableColors = listOf(
        "#4F46E5", // Indigo
        "#7C3AED", // Violet
        "#EC4899", // Pink
        "#EF4444", // Red
        "#F59E0B", // Amber
        "#10B981", // Emerald
        "#06B6D4", // Cyan
        "#3B82F6"  // Blue
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.find { it.first == key }?.second ?: Icons.Default.Folder
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF4F46E5)): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorLong = if (clean.length == 6) {
                ("FF$clean").toLong(16)
            } else {
                clean.toLong(16)
            }
            Color(colorLong)
        } catch (e: Exception) {
            defaultColor
        }
    }
}
