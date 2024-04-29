package com.vegas.geo.ui.screens.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vegas.geo.R
import com.vegas.geo.mainViewModel
import com.vegas.geo.ui.common.MainScaffold


@Composable
fun SettingsRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val allowedNavigateToHome = remember() { navigateToHome }
    val allowedNavigateToScanner = remember() { navigateToScanner }
    val allowedNavigateToLocation = remember() { navigateToLocation }
    val allowedNavigateToTracks = remember() { navigateToTracks }

    SettingsScreen(
        modifier = modifier,
        navigateBack = navigateBack,
        navigateToHome = { allowedNavigateToHome() },
        navigateToScanner = { allowedNavigateToScanner() },
        navigateToLocation = { allowedNavigateToLocation() },
        navigateToTracks = { allowedNavigateToTracks() },
        trackingEnabled = trackingEnabled,
    )

}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
) {

    BackHandler(onBack = navigateBack)

    MainScaffold(
        titleRes = R.string.settings_screen_title,
        navigateToHome = navigateToHome,
        navigateToScanner = navigateToScanner,
        navigateToLocation = navigateToLocation,
        navigateToTracks = navigateToTracks,
        trackingEnabled = trackingEnabled,
    ) { innerPadding ->

        val scrollState = rememberScrollState()
        val settingsViewState = mainViewModel.settingsViewState.collectAsState()
        val focusManager = LocalFocusManager.current

        val phoneNumber = remember{ mutableStateOf(mainViewModel.getPhoneNumber()) }
        val contactName = remember{ mutableStateOf(mainViewModel.getContactName()) }
        val myStatus = remember{mutableStateOf(mainViewModel.getMyStatus())}

        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Transparent),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.dark_mode))
                    Spacer(Modifier.weight(1f))

                    SelectableItem(
                        selected = settingsViewState.value.darkModeAutoSelected,
                        onClick = { mainViewModel.onDarkModeAutoClicked() },
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                horizontal = 4.dp
                            ), text = stringResource(R.string.auto)
                        )
                    }
                    SelectableItem(
                        selected = settingsViewState.value.darkModeLightSelected,
                        onClick = { mainViewModel.onDarkModeLightClicked() },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LightMode,
                            contentDescription = ""
                        )
                    }
                    SelectableItem(
                        selected = settingsViewState.value.darkModeDarkSelected,
                        onClick = { mainViewModel.onDarkModeDarkClicked() },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = ""
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.ask_confirmation_to_exit_from_app))

                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = settingsViewState.value.askToExitFromApp,
                        onCheckedChange = { mainViewModel.onExitFromAppChecked(it) },
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.keep_screen_on))

                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = settingsViewState.value.keepScreenOn,
                        onCheckedChange = { mainViewModel.onKeepScreenOn(it) },
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.location_enabled))

                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = settingsViewState.value.locationEnabled,
                        onCheckedChange = { mainViewModel.onLocationEnabled(it) },
                    )
                }


                Spacer(Modifier.height(10.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),

                    label = { androidx.compose.material.Text(stringResource(R.string.phone_number)) },
                    value = phoneNumber.value,
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {

                            val contact = mainViewModel.getContactNameByPhoneNumber(phoneNumber.value)
                            mainViewModel.onContactNameChanged(contact)
                            contactName.value = contact

                            focusManager.clearFocus(true)
                        },
                    ),
                    onValueChange = {
                            newValue -> mainViewModel.onPhoneNumberChanged(newValue)
                        phoneNumber.value = newValue
                    },
                )

                if (contactName.value.isEmpty()) {
                    Text(text = stringResource(R.string.contact_not_found))
                } else {
                    Text(text = contactName.value)
                }


                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.alarm))

                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = settingsViewState.value.alarm,
                        onCheckedChange = { mainViewModel.onAlarmChecked(it) },
                    )
                }


                Spacer(Modifier.height(10.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),

                    label = { Text(text = "My status:")},
                    value = myStatus.value,
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(true) },
                    ),
                    onValueChange = {
                        newValue -> mainViewModel.onMyStatusChanged(newValue)
                        myStatus.value = newValue
                    },
                )

            }
        }

    }

}

@Composable
private fun SelectableItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(percent = 50)
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(shape)
            .clickable { onClick() },
        shape = shape,
        color = if (selected) MaterialTheme.colors.secondary.copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

