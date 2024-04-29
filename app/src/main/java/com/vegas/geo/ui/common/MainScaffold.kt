package com.vegas.geo.ui.common

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.Man
import androidx.compose.material.icons.outlined.Scanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vegas.geo.R
import com.vegas.geo.mainViewModel
import kotlinx.coroutines.launch

data class NavigationItem(
    var id: Int,
    val isSelected: Boolean,
    var label: String,
    val iconFilled: ImageVector,
    val iconsOutlined: ImageVector,
    val navigateTo: (() -> Unit)?
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    @StringRes titleRes: Int,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
    navigateToHome: (() -> Unit)? = null,
    navigateToSettings: (() -> Unit)? = null,
    navigateToScanner: (() -> Unit)? = null,
    navigateToLocation: (() -> Unit)? = null,
    navigateToTracks: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = {
            MainTopAppBar(
                titleRes = titleRes,
                trackingEnabled = trackingEnabled,
            )
        },
        bottomBar = {
            MainBottomAppBar(
                navigateToHome = navigateToHome,
                navigateToSettings = navigateToSettings,
                navigateToScanner = navigateToScanner,
                navigateToLocation = navigateToLocation,
                navigateToTracks = navigateToTracks,
            )
        },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    @StringRes titleRes: Int,
    trackingEnabled: Boolean,
) {

    val activity = (LocalContext.current as? Activity)
    val scope = rememberCoroutineScope()

    androidx.compose.material.TopAppBar(
        backgroundColor = androidx.compose.material.MaterialTheme.colors.background,
        contentColor = androidx.compose.material.MaterialTheme.colors.primary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(id = titleRes))
                if (trackingEnabled) {
                    Spacer(Modifier.width(5.dp))
                    Text(text = "(${stringResource(R.string.tracking)})", color = Color.Red)
                }
            }
        },
        elevation = 0.dp,
        actions = {
            androidx.compose.material.IconButton(onClick = {
                mainViewModel.exitFromApp.value = true
            }) {
                androidx.compose.material.Icon(
                    Icons.Outlined.ExitToApp,
                    contentDescription = "Exit",
                )
            }
        }
    )
}


@Composable
fun MainBottomAppBar(
    navigateToHome: (() -> Unit)? = null,
    navigateToSettings: (() -> Unit)? = null,
    navigateToScanner: (() -> Unit)? = null,
    navigateToLocation: (() -> Unit)? = null,
    navigateToTracks: (() -> Unit)? = null,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var settingsList = listOf<NavigationItem>(
        NavigationItem(0, navigateToHome == null, stringResource(R.string.home_screen_title), Icons.Filled.Home, Icons.Outlined.Home, navigateToHome),
        NavigationItem(1, navigateToSettings == null, stringResource(R.string.settings_screen_title), Icons.Filled.Settings, Icons.Outlined.Settings, navigateToSettings),
        NavigationItem(2, navigateToSettings == null, stringResource(R.string.scanner_screen_title), Icons.Filled.Scanner, Icons.Outlined.Scanner, navigateToScanner),
        NavigationItem(3, navigateToLocation == null, stringResource(R.string.location_screen_title), Icons.Filled.LocationSearching, Icons.Outlined.LocationSearching, navigateToLocation),
        NavigationItem(4, navigateToTracks == null, stringResource(R.string.tracks_screen_title), Icons.Filled.Man, Icons.Outlined.Man, navigateToTracks),
    )

    LaunchedEffect(Unit) {

        var isItemVisible: Boolean = false

        with(lazyListState.layoutInfo) {
            val itemVisibleInfo = visibleItemsInfo.find { it.index == mainViewModel.current_tab_index }
            isItemVisible = if (itemVisibleInfo == null) {
                false
            } else {
                viewportEndOffset - itemVisibleInfo.offset >= itemVisibleInfo.size
            }
        }

        if (!isItemVisible) {
            coroutineScope.launch {
                lazyListState.scrollToItem(mainViewModel.current_tab_index)
            }
        }

    }
    /*
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {

        } else if (event == Lifecycle.Event.ON_PAUSE) {

        } else if (event == Lifecycle.Event.ON_CREATE) {
            coroutineScope.launch {
                lazyListState.scrollToItem(mainViewModel.current_tab_index)
            }

        }
    }

     */

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        elevation = 0.dp,
    ) {
        LazyRow (
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
        ) {
            itemsIndexed(
                items = settingsList,
                key = { _, settingsItem -> settingsItem.id })
            { index, item ->

                val icon = if (item.isSelected) item.iconFilled else item.iconsOutlined

                BottomNavigationItem(
                    selected = true,
                    onClick = {
                        if (item.navigateTo != null) {
                            item.navigateTo.invoke()
                            mainViewModel.current_tab_index = index
                        }
                    },
                    label = {
                        Text(item.label)
                    },
                    icon = {
                        Icon(icon, contentDescription = null)
                    },
                )
            }
        }
    }

}
