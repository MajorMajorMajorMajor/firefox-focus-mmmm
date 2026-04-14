/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.topsites

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.support.ktx.kotlin.getRepresentativeCharacter
import org.mozilla.focus.R
import org.mozilla.focus.ui.menu.CustomDropdownMenu
import org.mozilla.focus.ui.menu.MenuItem
import org.mozilla.focus.ui.theme.focusColors

private const val MIN_ITEM_WIDTH_DP = 80

@Composable
fun TopSites(
    topSites: List<TopSite>,
    onTopSiteClicked: (TopSite) -> Unit,
    onRemoveTopSiteClicked: (TopSite) -> Unit,
    onRenameTopSiteClicked: (TopSite) -> Unit,
    onReorderTopSites: (List<TopSite>) -> Unit,
) {
    val orderedSites = remember { mutableStateListOf(*topSites.toTypedArray()) }

    LaunchedEffect(topSites) {
        val newByUrl = topSites.associateBy { it.url }
        val oldUrls = orderedSites.map { it.url }.toSet()
        // Update existing entries so title/id changes (rename, reorder) are reflected.
        orderedSites.indices.forEach { i ->
            newByUrl[orderedSites[i].url]?.let { orderedSites[i] = it }
        }
        orderedSites.removeAll { it.url !in newByUrl }
        topSites.filter { it.url !in oldUrls }.forEach { orderedSites.add(it) }
    }

    val haptic = LocalHapticFeedback.current
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var menuExpandedFor by remember { mutableStateOf<Int?>(null) }
    val itemBounds = remember { mutableMapOf<Int, Rect>() }
    var columnRootOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier.padding(horizontal = 10.dp)) {
    val columns = (maxWidth.value / MIN_ITEM_WIDTH_DP).toInt().coerceAtLeast(2)

    Column(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                columnRootOffset = coords.boundsInRoot().topLeft
            }
            .pointerInput(Unit) {
                var hasDragged = false
                var totalDragDistance = 0f
                var hitIndex: Int? = null
                detectDragGesturesAfterLongPress(
                    onDragStart = { pressOffset ->
                        val rootPos = columnRootOffset + pressOffset
                        hitIndex = itemBounds.entries
                            .firstOrNull { (_, b) -> b.contains(rootPos) }
                            ?.key
                        hasDragged = false
                        totalDragDistance = 0f
                        if (hitIndex != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggingIndex = hitIndex
                            dragOffset = Offset.Zero
                        }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val dragIdx = draggingIndex ?: return@detectDragGesturesAfterLongPress
                        totalDragDistance += amount.getDistance()
                        if (totalDragDistance > 24f) hasDragged = true
                        dragOffset += amount
                        val bounds = itemBounds[dragIdx]
                            ?: return@detectDragGesturesAfterLongPress
                        val center = Offset(
                            bounds.center.x + dragOffset.x,
                            bounds.center.y + dragOffset.y,
                        )
                        val hoverIdx = itemBounds.entries
                            .firstOrNull { (i, b) -> i != dragIdx && b.contains(center) }
                            ?.key
                        if (hoverIdx != null) {
                            val temp = orderedSites[dragIdx]
                            orderedSites[dragIdx] = orderedSites[hoverIdx]
                            orderedSites[hoverIdx] = temp
                            draggingIndex = hoverIdx
                            dragOffset = Offset.Zero
                        }
                    },
                    onDragEnd = {
                        if (!hasDragged) menuExpandedFor = hitIndex
                        else onReorderTopSites(orderedSites.toList())
                        draggingIndex = null
                        dragOffset = Offset.Zero
                        hasDragged = false
                        totalDragDistance = 0f
                        hitIndex = null
                    },
                    onDragCancel = {
                        draggingIndex = null
                        dragOffset = Offset.Zero
                        hasDragged = false
                        totalDragDistance = 0f
                        hitIndex = null
                    },
                )
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        orderedSites.indices.chunked(columns).forEach { rowIndices ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowIndices.forEach { index ->
                    val topSite = orderedSites[index]
                    val isBeingDragged = draggingIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                itemBounds[index] = coords.boundsInRoot()
                            }
                            .graphicsLayer {
                                if (isBeingDragged) {
                                    translationX = dragOffset.x
                                    translationY = dragOffset.y
                                    scaleX = 1.08f
                                    scaleY = 1.08f
                                }
                                alpha = if (isBeingDragged) 0.85f else 1f
                            }
                            .zIndex(if (isBeingDragged) 1f else 0f),
                        contentAlignment = Alignment.Center,
                    ) {
                        TopSiteItem(
                            topSite = topSite,
                            menuItems = listOfNotNull(
                                MenuItem(
                                    title = stringResource(R.string.rename_top_site_item),
                                    onClick = { onRenameTopSiteClicked(topSite) },
                                ),
                                MenuItem(
                                    title = stringResource(R.string.remove_top_site),
                                    onClick = { onRemoveTopSiteClicked(topSite) },
                                ),
                            ),
                            isMenuExpanded = menuExpandedFor == index,
                            onMenuDismiss = { menuExpandedFor = null },
                            onTopSiteClick = { item -> onTopSiteClicked(item) },
                        )
                    }
                }
                repeat(columns - rowIndices.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
    } // BoxWithConstraints
}

@Composable
private fun TopSiteItem(
    topSite: TopSite,
    menuItems: List<MenuItem>,
    isMenuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onTopSiteClick: (TopSite) -> Unit = {},
) {
    Box {
        Column(
            modifier = Modifier
                .pointerInput(topSite.url) {
                    detectTapGestures(
                        onTap = { onTopSiteClick(topSite) },
                    )
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopSiteFaviconCard(topSite = topSite)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = topSite.title ?: topSite.url,
                color = focusColors.topSiteTitle,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            CustomDropdownMenu(
                menuItems = menuItems,
                isExpanded = isMenuExpanded,
                onDismissClicked = onMenuDismiss,
            )
        }
    }
}

@Composable
private fun TopSiteFaviconCard(topSite: TopSite) {
    Card(
        modifier = Modifier.size(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = focusColors.topSiteBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(4.dp),
                color = focusColors.surface,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (topSite.title.isNullOrEmpty()) {
                            topSite.url.getRepresentativeCharacter()
                        } else {
                            topSite.title?.get(0).toString()
                        },
                        color = focusColors.topSiteFaviconText,
                        fontSize = 20.sp,
                    )
                }
            }
        }
    }
}
