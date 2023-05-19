package com.example.vegas.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vegas.AppFunction
import com.example.vegas.R
import com.example.vegas.viewModel

@Composable
fun SettingsScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
    ) {
        val theme = listOf(stringResource(R.string.light), stringResource(R.string.dark))

        val (selectedThemeOption, onThemeOptionSelected) = remember { mutableStateOf(viewModel.currentTheme) }
        val checkedStateExitFromApp = remember { mutableStateOf(viewModel.askToExitFromApp) }
        val checkedStateDeleteSelectedtem = remember { mutableStateOf(viewModel.askToDeleteSelectedItem) }

        val checkedStateSaveTracking = remember { mutableStateOf(viewModel.askToSaveTracking) }
        val checkedStateWakeLock = remember { mutableStateOf(viewModel.wakeLock) }
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(viewModel.currentMinDistance) }
        val (selectedMapOption, onMapOptionSelected) = remember { mutableStateOf(viewModel.currentMap) }

        Column(
            modifier = Modifier.padding(0.dp, 16.dp),
        ) {

            androidx.compose.material3.Text(
                text = "${stringResource(R.string.theme)}:",
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Row(
                Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(56.dp), verticalAlignment = Alignment.CenterVertically) {

                for(theme_index in 0..theme.lastIndex) {
                    androidx.compose.material3.RadioButton(
                        selected = (theme_index == selectedThemeOption),
                        onClick = {
                            onThemeOptionSelected(theme_index)
                            viewModel.currentTheme = theme_index
                            AppFunction.putPreferences.run()
                        },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(),

                    )

                    ClickableText(
                        text = AnnotatedString(theme[theme_index]),
                        style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.primary,
                        ),
                        onClick = {
                            onThemeOptionSelected(theme_index)
                            viewModel.currentTheme = theme_index
                            AppFunction.putPreferences.run()
                        }
                    )
                }

            }


            Row {
                androidx.compose.material3.Checkbox(
                    checked = checkedStateExitFromApp.value,
                    onCheckedChange = {
                        checkedStateExitFromApp.value = it
                        viewModel.askToExitFromApp = it
                        AppFunction.putPreferences.run()
                    },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(),
                    modifier = Modifier.padding(10.dp),
                )
                ClickableText(
                    text = AnnotatedString(stringResource(R.string.ask_confirmation_to_exit_from_app)),
                    modifier = Modifier.padding(10.dp, 20.dp),
                    style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                    onClick = { offset ->
                        checkedStateExitFromApp.value = !checkedStateExitFromApp.value;
                        viewModel.askToExitFromApp = !viewModel.askToExitFromApp
                        AppFunction.putPreferences.run()
                    }
                )
            }

            Row {
                androidx.compose.material3.Checkbox(
                    checked = checkedStateDeleteSelectedtem.value,
                    onCheckedChange = {
                        checkedStateDeleteSelectedtem.value = it
                        viewModel.askToDeleteSelectedItem = it
                        AppFunction.putPreferences.run()

                    },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(),
                    modifier = Modifier.padding(10.dp)
                )
                ClickableText(
                    text = AnnotatedString(stringResource(R.string.ask_confirmation_to_delete_selected_item)),
                    modifier = Modifier.padding(10.dp, 20.dp),
                    style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                    onClick = { offset ->
                        checkedStateDeleteSelectedtem.value = !checkedStateDeleteSelectedtem.value;
                        viewModel.askToDeleteSelectedItem = !viewModel.askToDeleteSelectedItem
                        AppFunction.putPreferences.run()
                    }
                )

            }

            Row {
                androidx.compose.material3.Checkbox(
                    checked = checkedStateSaveTracking.value,
                    onCheckedChange = {
                        checkedStateSaveTracking.value = it
                        viewModel.askToSaveTracking = it
                        AppFunction.putPreferences.run()

                    },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(),
                    modifier = Modifier.padding(10.dp)
                )
                ClickableText(
                    text = AnnotatedString(stringResource(R.string.ask_confirmation_to_save_tracking)),
                    modifier = Modifier.padding(10.dp, 20.dp),
                    style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                    onClick = { offset ->
                        checkedStateSaveTracking.value = !checkedStateSaveTracking.value;
                        viewModel.askToSaveTracking = !viewModel.askToSaveTracking
                        AppFunction.putPreferences.run()
                    }
                )

            }

            Row{
                androidx.compose.material3.Checkbox(
                    checked = checkedStateWakeLock.value,
                    onCheckedChange = {
                        checkedStateWakeLock.value = it
                        viewModel.wakeLock = it
                        AppFunction.putPreferences.run()
                    },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(),
                    modifier = Modifier.padding(10.dp),
                )
                ClickableText(
                    text = AnnotatedString(stringResource(R.string.wake_lock)),
                    modifier = Modifier.padding(vertical = 18.dp),
                    style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                    onClick = {
                        checkedStateWakeLock.value = !checkedStateWakeLock.value;
                        viewModel.wakeLock = !viewModel.wakeLock
                        AppFunction.putPreferences.run()
                    }
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            androidx.compose.material3.Text(
                text = "${stringResource(R.string.min_distance)}:",
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Row(
                Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                viewModel.minDistance.forEach { distance ->
                    androidx.compose.material3.RadioButton(
                        selected = (distance == selectedOption),
                        onClick = {
                            onOptionSelected(distance)
                            viewModel.currentMinDistance = distance
                            AppFunction.updateDistance.run()
                            AppFunction.putPreferences.run()
                        },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(),
                    )

                    ClickableText(
                        text = AnnotatedString("${distance}${stringResource(R.string.m)}"),
                        style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                        onClick = {
                            onOptionSelected(distance)
                            viewModel.currentMinDistance = distance
                            AppFunction.updateDistance.run()
                            AppFunction.putPreferences.run()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            androidx.compose.material3.Text(
                text = "${stringResource(R.string.map)}:",
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Row(
                Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                viewModel.maps.forEach { map ->
                    androidx.compose.material3.RadioButton(
                        selected = (map == selectedMapOption),
                        onClick = {
                            onMapOptionSelected(map)
                            viewModel.currentMap = map
                            AppFunction.putPreferences.run()
                        },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(),
                    )

                    ClickableText(
                        text = AnnotatedString(map),
                        style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp),
                        onClick = {
                            onMapOptionSelected(map)
                            viewModel.currentMap = map
                            AppFunction.putPreferences.run()
                        }
                    )
                }
            }


        }
    }
}