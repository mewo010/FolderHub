package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.HubItemEntity
import com.example.data.model.HubItemType
import com.example.util.LauncherHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HubItemCard(
    item: HubItemEntity,
    accentColor: Color,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    onOpenPlayStore: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    // Load native app icon if it's an installed app
    val appIconBitmap = remember(item.dataUri, item.type) {
        if (item.type == HubItemType.APP) {
            try {
                val drawable = LauncherHelper.getInstalledAppIcon(context, item.dataUri)
                drawable?.toBitmap(96, 96, Bitmap.Config.ARGB_8888)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Card(
        modifier = modifier
            .testTag("item_card_${item.id}")
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = { menuExpanded = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Type badge and More options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeBadge(type = item.type)

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("item_menu_btn_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Item options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Launch") },
                                onClick = {
                                    menuExpanded = false
                                    onLaunch()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            )

                            if (item.type == HubItemType.APP) {
                                DropdownMenuItem(
                                    text = { Text("Play Store Page") },
                                    onClick = {
                                        menuExpanded = false
                                        onOpenPlayStore?.invoke() ?: LauncherHelper.openPlayStore(context, item.dataUri)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Shop,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Remove from Hub", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Center: Icon representing app, web link, or file
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = accentColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        appIconBitmap != null -> {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        item.type == HubItemType.APP -> {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = item.title,
                                tint = accentColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        item.type == HubItemType.WEB_LINK -> {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = item.title,
                                tint = accentColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        item.type == HubItemType.LOCAL_FILE -> {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = item.title,
                                tint = accentColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom: Title and secondary text
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.secondaryInfo.isNotBlank()) {
                        Text(
                            text = item.secondaryInfo,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: HubItemType) {
    val (label, bg, fg) = when (type) {
        HubItemType.APP -> Triple("APP", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        HubItemType.WEB_LINK -> Triple("LINK", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        HubItemType.LOCAL_FILE -> Triple("FILE", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            color = fg
        )
    }
}
