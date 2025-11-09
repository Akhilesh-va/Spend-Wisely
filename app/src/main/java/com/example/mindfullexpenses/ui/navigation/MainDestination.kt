package com.example.mindfullexpenses.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mindfullexpenses.R

enum class MainDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Dashboard(
        route = "dashboard",
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.AutoMirrored.Rounded.ListAlt,
        unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt    ),
    ManualEntry(
        route = "manual_entry",
        labelRes = R.string.nav_manual_entry,
        selectedIcon = Icons.AutoMirrored.Rounded.NoteAdd,
        unselectedIcon = Icons.AutoMirrored.Outlined.NoteAdd    ),
    Reports(
        route = "reports",
        labelRes = R.string.nav_reports,
        selectedIcon = Icons.Rounded.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    );
}


