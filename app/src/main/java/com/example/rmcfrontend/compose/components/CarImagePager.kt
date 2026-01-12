package com.example.rmcfrontend.compose.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

sealed interface CarImageItem {
    data class Remote(val url: String) : CarImageItem
    data class Local(val uri: Uri) : CarImageItem
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarImagePager(
    items: List<CarImageItem>,
    placeholderResId: Int,
    modifier: Modifier = Modifier,
    showIndicators: Boolean = true,
    allowDeleteLocal: Boolean = false,
    onDeleteLocal: ((Uri) -> Unit)? = null
) {
    val safeItems: List<CarImageItem> =
        if (items.isEmpty()) listOf(CarImageItem.Remote(url = "")) else items

    val pagerState = rememberPagerState(pageCount = { safeItems.size })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = safeItems.size > 1
        ) { page ->
            val item = safeItems[page]
            when (item) {
                is CarImageItem.Remote -> {
                    val model: Any = if (item.url.isBlank()) placeholderResId else item.url
                    AsyncImage(
                        model = model,
                        placeholder = painterResource(placeholderResId),
                        error = painterResource(placeholderResId),
                        contentDescription = "Car image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                is CarImageItem.Local -> {
                    AsyncImage(
                        model = item.uri,
                        placeholder = painterResource(placeholderResId),
                        error = painterResource(placeholderResId),
                        contentDescription = "Car image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (allowDeleteLocal) {
            val current = safeItems.getOrNull(pagerState.currentPage)
            if (current is CarImageItem.Local && onDeleteLocal != null) {
                IconButton(
                    onClick = { onDeleteLocal(current.uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove image")
                }
            }
        }

        if (showIndicators && safeItems.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in safeItems.indices) {
                    val isSelected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                    )
                    if (i != safeItems.lastIndex) Spacer(modifier = Modifier.size(5.dp))
                }
            }
        }
    }
}
