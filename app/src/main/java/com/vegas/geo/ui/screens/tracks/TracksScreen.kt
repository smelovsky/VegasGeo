package com.vegas.geo.ui.screens.tracks

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.vegas.geo.AppFunction
import com.vegas.geo.MapMode
import com.vegas.geo.R
import com.vegas.geo.data.database.model.TrackEntity
import com.vegas.geo.mainViewModel
import com.vegas.geo.ui.common.MainScaffold

@Composable
fun TracksRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    modifier: Modifier = Modifier,
    trackingEnabled: Boolean,
) {

    val allowedNavigateToHome = remember() { navigateToHome }
    val allowedNavigateToSettings = remember() { navigateToSettings }
    val allowedNavigateToScanner = remember() { navigateToScanner }
    val allowedNavigateToLocation = remember() { navigateToLocation }

    TracksScreen(
        navigateBack = navigateBack,
        navigateToHome = { allowedNavigateToHome() },
        navigateToSettings = { allowedNavigateToSettings() },
        navigateToScanner = { allowedNavigateToScanner() },
        navigateToLocation = { allowedNavigateToLocation() },
        modifier = modifier,
        trackingEnabled = trackingEnabled,
    )


}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TracksScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    modifier: Modifier = Modifier,
    trackingEnabled: Boolean,
) {
    BackHandler(onBack = navigateBack)

    LaunchedEffect(Unit) {
        mainViewModel.getTracks()
    }

    MainScaffold(
        titleRes = R.string.tracks_screen_title,
        modifier = modifier,
        navigateToHome = navigateToHome,
        navigateToSettings = navigateToSettings,
        navigateToScanner = navigateToScanner,
        navigateToLocation = navigateToLocation,
        trackingEnabled = trackingEnabled,
    ) { innerPadding ->

        val lazyListState: LazyListState = rememberLazyListState()
        val showAlertDialog = remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Transparent),
        ) {
            if (showAlertDialog.value) {
                AlertDialog(
                    onDismissRequest = {  },
                    title = {
                        Row() {
                            Icon(
                                painterResource(R.drawable.vegas_02),
                                contentDescription = "AlertDialog",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            androidx.compose.material.Text(stringResource(R.string.app_name), fontSize = 22.sp)
                        }

                    },
                    text = { androidx.compose.material.Text(stringResource(R.string.would_you_like_to_delete_selected_track), fontSize = 16.sp) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mainViewModel.trackDeleteSelectedItems()
                                showAlertDialog.value = false
                            }
                        ) {
                            androidx.compose.material.Text(stringResource(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAlertDialog.value = false }
                        ) {
                            androidx.compose.material.Text(stringResource(R.string.no))
                        }
                    }
                )
            }

            LazyColumn (
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.99f),
                state = lazyListState,
            ) {
                itemsIndexed(
                    items = mainViewModel.trackListEntity,
                    key = { _, trackItem -> trackItem.trackId })
                { index, trackItem ->

                    TracksEditBlock(
                        index = index,
                        item = trackItem,
                        { showAlertDialog.value = true },
                    )

                }
            }

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable
fun TracksEditBlock(
    index: Int,
    item: TrackEntity,
    showAlertDialog: ()-> Unit,
) {
    val details = mainViewModel.trackListEntity[index]

    val name = details.trackName
    var isEditMode = details.isEditMode
    val isSelected = details.isSelected

    Log.d("ddd", details.toString())

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .combinedClickable(
                onLongClick = {
                    mainViewModel.trackSelectItemAndSetEditMode(details.trackId)
                },
                onDoubleClick = {
                    mainViewModel.trackSelectItemAndSetEditMode(details.trackId)
                },
                onClick = {
                    if (details.isSelected) {
                        mainViewModel.trackUnselectAllItems()
                    } else {
                        mainViewModel.trackSelectItem(trackId = details.trackId, isSelected = true)
                    }

                },

                )

    ) {
        val focusRequester = remember { FocusRequester() }

        val (labelIconRef,
            mapIconRef,
            titleRef,
            deleteIconRef,
            editIconRef,
            cancelIconRef,
            topDividerRef,
            bottomDividerRef) = createRefs()

        val dividerColor: Color = if (isEditMode) {
            androidx.compose.material3.MaterialTheme.colorScheme.secondary
        } else {
            if (isSelected) {
                androidx.compose.material3.MaterialTheme.colorScheme.secondary
            } else {
                Color.Transparent
            }
        }

        val dividerThickness = 1.5.dp
        Divider(
            color = dividerColor,
            thickness = dividerThickness,
            modifier = Modifier
                .constrainAs(topDividerRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        Divider(
            color = dividerColor,
            thickness = dividerThickness,
            modifier = Modifier
                .constrainAs(bottomDividerRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        if (isEditMode) {
            androidx.compose.material3.Icon(
                painter = painterResource(R.drawable.ic_label),
                contentDescription = "Label",
                modifier = Modifier
                    .constrainAs(labelIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
            )


            var textFieldValue: TextFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = name,
                        selection = TextRange(name.length),
                        composition = TextRange(0, name.length)
                    )
                )
            }

            DisposableEffect(key1 = Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .constrainAs(titleRef) {
                        width = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(labelIconRef.end)
                        end.linkTo(cancelIconRef.start)
                    },
                value = textFieldValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        mainViewModel.trackRenameItem(details.trackId, textFieldValue.text)
                    }
                ),
                onValueChange = { newValue -> textFieldValue = newValue },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.primary,
                ),
            )

            IconButton(
                onClick = {
                    mainViewModel.trackSelectItem(trackId = details.trackId, isSelected = false)
                },
                modifier = Modifier
                    .constrainAs(cancelIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 4.dp)
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cancel),
                    contentDescription = "Cancel",
                )
            }

        }
        else {
            if (isSelected) {
                IconButton(
                    onClick = {
                        mainViewModel.mapMode = MapMode.SAVED_TRACK
                        mainViewModel.trackingEanabled = false
                        AppFunction.map.run()
                    },
                    modifier = Modifier
                        .constrainAs(mapIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_map_24),
                        contentDescription = "Map",
                    )
                }

                androidx.compose.material3.Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .constrainAs(titleRef) {
                            width = Dimension.fillToConstraints
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(mapIconRef.end, margin = if (isSelected) 8.dp else 16.dp )
                            end.linkTo(deleteIconRef.start, )
                        }
                )

                IconButton(
                    onClick = { showAlertDialog() },
                    modifier = Modifier
                        .constrainAs(deleteIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(editIconRef.start, margin = 4.dp)
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete",
                    )
                }

                IconButton(
                    onClick = { mainViewModel.trackChangeEditMode(details.trackId, !isEditMode)
                    },
                    modifier = Modifier
                        .constrainAs(editIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, )
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                    )
                }

            } else {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.baseline_label_24),
                    contentDescription = "Label",
                    modifier = Modifier
                        .constrainAs(labelIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 16.dp)
                        }
                )
                androidx.compose.material3.Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .constrainAs(titleRef) {
                            width = Dimension.fillToConstraints
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(labelIconRef.end, margin = if (isSelected) 8.dp else 16.dp )
                            end.linkTo(parent.end, )
                        }
                )
            }

        }
    }
}