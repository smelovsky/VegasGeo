package com.example.vegas.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension
import com.example.vegas.*
import com.example.vegas.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TracksScreen(showUp: (Boolean, ()->Unit) -> Unit, showDown: (Boolean, ()->Unit) -> Unit) {

    val lazyListState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showAlertDialog = remember { mutableStateOf(false) }

    var top_zone = 0.0f
    var midle_zone = 1.0f

    val allItems = viewModel.trackListBase.size.toFloat()
    if (allItems > 0) {
        val visiibleItems = lazyListState.layoutInfo.visibleItemsInfo.size.toFloat()
        if (lazyListState.layoutInfo.visibleItemsInfo.size > 0) {
            top_zone = lazyListState.layoutInfo.visibleItemsInfo[0].index.toFloat() / allItems
        }
        midle_zone = visiibleItems/allItems + top_zone
    }

    if (showAlertDialog.value) {
        AlertDialog(
            onDismissRequest = {  },
            title = {
            Row() {
                androidx.compose.material3.Icon(
                    painterResource(R.drawable.vegas_08),
                    contentDescription = "AlertDialog",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(stringResource(R.string.app_name), fontSize = 22.sp)
            }

                    },
            text = { Text(stringResource(R.string.would_you_like_to_delete_selected_track), fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.trackDeleteSelectedItem()
                        showAlertDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAlertDialog.value = false }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Row() {
        LazyColumn (
            modifier = Modifier
                //.background(color = if (viewModel.currentTheme == 0)  MaterialTheme.colorScheme.background else Color.White)
                .fillMaxHeight()
                .fillMaxWidth(0.99f),
            state = lazyListState,
        ) {
            itemsIndexed(
                items = viewModel.trackListBase,
                key = { _, trackBaseItem -> trackBaseItem.id })
            { index, trackBaseItem ->

                Log.d("zzz", "LazyColumn: ${index}")
                TracksEditBlock(
                    index = index,
                    item = trackBaseItem,
                    { showAlertDialog.value = true },
                )

            }
        }

        Column() {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxHeight(top_zone)
                    .fillMaxWidth(),
            ) {

            }
            Box(
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxHeight(midle_zone)
                    .fillMaxWidth(),
            ) {

            }
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxHeight()
                    .fillMaxWidth(),
            ) {

            }
        }

    }

    if (viewModel.trackListBase.size > 0) {

        val lastIndex = lazyListState.layoutInfo.visibleItemsInfo.size - 1

        if (lastIndex != -1) {

            val keyBottomItem = lazyListState.layoutInfo.visibleItemsInfo[lastIndex].key
            val keyTopItem = lazyListState.layoutInfo.visibleItemsInfo[0].key
            val firstItemKey = viewModel.trackListBase[0].id
            val lastItemKey = viewModel.trackListBase[viewModel.trackListBase.lastIndex].id

            val isFirstItemFullyVisible = TracksIsItemFullyVisible(lazyListState, 0)
            val isLastItemFullyVisible = TracksIsItemFullyVisible(lazyListState, lastIndex)

            if (viewModel.trackListBase.size >= lazyListState.layoutInfo.visibleItemsInfo.size) {

                showDown(keyBottomItem != lastItemKey || isLastItemFullyVisible == false,
                    {
                        coroutineScope.launch {
                            lazyListState.scrollToItem(viewModel.trackListBase.size - 1)
                        }
                    })
                showUp(keyTopItem != firstItemKey || isFirstItemFullyVisible == false,
                    {
                        coroutineScope.launch {
                            lazyListState.scrollToItem(0)
                        }
                    })

            }
        }
    }
}

fun TracksIsItemFullyVisible(lazyListState: LazyListState, editTagItemIndex: Int): Boolean {
    with(lazyListState.layoutInfo) {
        val editingTagItemVisibleInfo = visibleItemsInfo.find { it.index == editTagItemIndex }
        return if (editingTagItemVisibleInfo == null) {
            false
        } else {
            viewportEndOffset - editingTagItemVisibleInfo.offset >= editingTagItemVisibleInfo.size

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable

fun TracksEditBlock(
    index: Int,
    item: TrackBaseItem,
    showAlertDialog: ()-> Unit,
) {
    val id = item.id
    val details = viewModel.trackListDetails[index]

    val name = details.name
    var isEditMode = details.isEditMode
    val isSelected = details.isSelected
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .combinedClickable(
                onLongClick = {
                    if (!viewModel.trackIsEditMode()) {
                        viewModel.trackSelectItemAndSetEditMode(index)
                    }
                },
                onDoubleClick = {
                    if (!viewModel.trackIsEditMode()) {
                        viewModel.trackSelectItemAndSetEditMode(index)
                    }
                },
                onClick = {
                    if (!viewModel.trackIsEditMode()) {
                        viewModel.trackSelectItem(index)
                    }
                },

                )

    ) {
        val focusRequester = remember { FocusRequester() }

        val (startIconRef, titleRef, deleteIconRef, endIconRef, topDividerRef, bottomDividerRef) = createRefs()

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
                painter = painterResource(R.drawable.baseline_label_24),
                contentDescription = "Label",
                modifier = Modifier
                    .constrainAs(startIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
            )

            var textFieldValueOld: String by remember {
                mutableStateOf(name)
            }

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
                        start.linkTo(startIconRef.end )
                        end.linkTo(deleteIconRef.start)
                    },
                value = textFieldValue,
                singleLine = true,
                textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        Log.d("zzz", "<${textFieldValue.text}>")
                        viewModel.trackRenameItem(index, textFieldValue.text)
                    }
                ),
                onValueChange = { newValue -> textFieldValue = newValue },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                ),
            )

            IconButton(
                onClick = { showAlertDialog() },
                modifier = Modifier
                    .constrainAs(deleteIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(endIconRef.start, margin = 4.dp)
                    }
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete",
                )
            }

            IconButton(
                onClick = { viewModel.trackRenameItem(index, textFieldValueOld)
                },
                modifier = Modifier
                    .constrainAs(endIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 4.dp)
                    }
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.ic_cancel),
                    contentDescription = "Cancel",
                )
            }

        }
        else {
            if (isSelected) {
                IconButton(
                    onClick = {
                        viewModel.showSavedMarker = false
                        viewModel.showSavedTrack = true

                        viewModel.marker_index = -1
                        viewModel.track_index = viewModel.trackGetSelectedItem()

                        AppFunction.map.run()
                    },
                    modifier = Modifier
                        .constrainAs(startIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.baseline_map_24),
                        contentDescription = "Map",
                    )
                }
            } else {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.baseline_label_24),
                    contentDescription = "Label",
                    modifier = Modifier
                        .constrainAs(startIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 16.dp)
                        }
                )
            }


            androidx.compose.material3.Text(
                text = name,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(titleRef) {
                        width = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(startIconRef.end, margin = if (isSelected) 8.dp else 16.dp )
                        end.linkTo(endIconRef.start, )
                    }
            )

            if (isSelected) {
                IconButton(
                    onClick = { viewModel.trackChangeEditMode(index)
                    },
                    modifier = Modifier
                        .constrainAs(endIconRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, )
                        }
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                    )
                }
            }

        }
    }
}


@Composable
fun MainActivity.BottomBarTracks(isShowUp: Boolean,
                                 isShowDown: Boolean,
                                 up: () -> Unit,
                                 down: () -> Unit,
) {

    val alertDialog = android.app.AlertDialog.Builder(this)

    ConstraintLayout(

        modifier = Modifier.fillMaxWidth() ) {

        val (upIcon, mapButton, downIcon ) = createRefs()

        androidx.compose.material3.IconButton(
            onClick = { up() },
            enabled = isShowUp,
            modifier = Modifier
                .clip(CircleShape)
                .constrainAs(upIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 4.dp)
                }

        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = "Up",
            )
        }

        androidx.compose.material3.Button(
            onClick = {

                alertDialog.apply {
                    setIcon(R.drawable.vegas_08)
                    setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                    setMessage(getApplicationContext().getResources().getString(R.string.would_you_like_to_delete_all_items))
                    setPositiveButton(getApplicationContext().getResources().getString(R.string.yes))
                    {  _, _ -> viewModel.trackDeleteAllItems() }
                    setNegativeButton(getApplicationContext().getResources().getString(R.string.no))
                    { _, _ -> }

                }.create().show()

            },
            enabled = viewModel.trackListBase.size > 0,
            modifier = Modifier
                .constrainAs(mapButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(upIcon.end, margin = 4.dp)
                    end.linkTo(downIcon.start, margin = 4.dp)
                }
        ) {
            androidx.compose.material3.Text(text = stringResource(R.string.delete_all))
        }



        androidx.compose.material3.IconButton(
            onClick = { down() },
            enabled = isShowDown,
            modifier = Modifier
                .constrainAs(downIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 4.dp)
                }

        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = "Down",
            )
        }
    }

}

