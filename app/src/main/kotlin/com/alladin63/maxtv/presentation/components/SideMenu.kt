package com.alladin63.maxtv.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alladin63.maxtv.presentation.screens.home.HomeSection
import com.alladin63.maxtv.presentation.theme.*

data class MenuItemData(
    val section: HomeSection,
    val icon: ImageVector,
    val label: String
)

val menuItems = listOf(
    MenuItemData(HomeSection.MOVIES, Icons.Default.Movie, "Films"),
    MenuItemData(HomeSection.SERIES, Icons.Default.PlayArrow, "Séries"),
    MenuItemData(HomeSection.CHANNELS, Icons.Default.LiveTv, "Chaînes"),
    MenuItemData(HomeSection.EPG, Icons.Default.CalendarToday, "Guide TV"),
    MenuItemData(HomeSection.FAVORITES, Icons.Default.Favorite, "Favoris"),
    MenuItemData(HomeSection.HISTORY, Icons.Default.History, "Historique"),
    MenuItemData(HomeSection.SEARCH, Icons.Default.Search, "Recherche"),
    MenuItemData(HomeSection.SETTINGS, Icons.Default.Settings, "Paramètres"),
)

@Composable
fun SideMenu(
    selectedSection: HomeSection,
    onSectionSelected: (HomeSection) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .onFocusChanged { isExpanded = it.hasFocus || it.isFocused }
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isExpanded) 200.dp else 64.dp)
                    .background(MaxSurface)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Logo compact
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isExpanded) "MaX TV" else "M",
                        color = MaxBlue,
                        fontSize = if (isExpanded) 18.sp else 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                menuItems.forEach { item ->
                    MenuItemRow(
                        item = item,
                        isSelected = selectedSection == item.section,
                        isExpanded = isExpanded,
                        onClick = { onSectionSelected(item.section) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItemData,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .background(
                color = when {
                    isSelected -> MaxBlue.copy(alpha = 0.2f)
                    isFocused -> MaxSurfaceVariant
                    else -> androidx.compose.ui.graphics.Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 1.dp,
                    color = MaxBlue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) MaxBlue else if (isFocused) MaxOnBackground else MaxOnSurface,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = item.label,
                color = if (isSelected) MaxBlue else if (isFocused) MaxOnBackground else MaxOnSurface,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}
