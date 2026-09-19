package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LauncherHelper {

    suspend fun loadInstalledApps(context: Context): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val apps = mutableListOf<InstalledAppInfo>()

            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                // Skip our own launcher from the app picker to avoid recursive confusion
                if (pkgName == context.packageName) continue

                val appName = resolveInfo.loadLabel(pm).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val icon = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }

                apps.add(
                    InstalledAppInfo(
                        label = appName,
                        packageName = pkgName,
                        activityName = resolveInfo.activityInfo.name,
                        isSystemApp = isSystem,
                        icon = icon
                    )
                )
            }

            apps.sortedBy { it.label.lowercase() }
        }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            pm.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
    }

    fun getInstalledAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        val marketUri = Uri.parse("market://details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (e: ActivityNotFoundException) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun openWebLink(context: Context, rawUrl: String): Result<Unit> {
        val cleanedUrl = when {
            rawUrl.startsWith("http://", ignoreCase = true) ||
            rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl
            else -> "https://$rawUrl"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openDocument(context: Context, uriString: String, mimeType: String?): Result<Unit> {
        return try {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType?.takeIf { it.isNotBlank() } ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun queryFileDetails(context: Context, uri: Uri): Pair<String, String> {
        var name = "Document"
        var sizeStr = ""

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: "Document"
                    }
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        val bytes = cursor.getLong(sizeIndex)
                        sizeStr = formatFileSize(bytes)
                    }
                }
            }
        } catch (e: Exception) {
            // fallback to last path segment
            name = uri.lastPathSegment ?: "Document"
        }

        val type = context.contentResolver.getType(uri) ?: ""
        val subtitle = buildString {
            if (sizeStr.isNotBlank()) append(sizeStr)
            if (type.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(type.substringAfterLast('/'))
            }
        }

        return Pair(name, subtitle)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format("%.1f %s", value, units[digitGroups])
    }
}
