package com.xixikitchen.jetpack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.CartLine
import com.xixikitchen.jetpack.data.Dish
import com.xixikitchen.jetpack.ui.KitchenUiState
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.components.DishThumbnail
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.designsystem.motion.AppDuration
import com.xixikitchen.jetpack.ui.designsystem.motion.AppEase
import com.xixikitchen.jetpack.ui.designsystem.motion.LocalReducedMotion
import com.xixikitchen.jetpack.ui.designsystem.motion.SpringSettle
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.ui.shimmerEffect
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(state: KitchenUiState, vm: KitchenViewModel) {
    val tokens = LocalGlassTokens.current
    val reducedMotion = LocalReducedMotion.current
    var showCart by remember { mutableStateOf(false) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    var showOrder by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refreshHome() }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            AnimatedVisibility(
                visible = state.cartCount > 0,
                enter = if (reducedMotion) {
                    fadeIn(tween(150))
                } else {
                    fadeIn(tween(AppDuration.standard, easing = AppEase.out)) +
                        slideInVertically(
                            animationSpec = tween(AppDuration.standard, easing = AppEase.out),
                            initialOffsetY = { it }
                        ) +
                        scaleIn(
                            animationSpec = tween(AppDuration.standard, easing = AppEase.out),
                            initialScale = 0.96f,
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        )
                },
                exit = if (reducedMotion) {
                    fadeOut(tween(120))
                } else {
                    fadeOut(tween(AppDuration.exit, easing = AppEase.out)) +
                        slideOutVertically(
                            animationSpec = tween(AppDuration.exit, easing = AppEase.out),
                            targetOffsetY = { it }
                        ) +
                        scaleOut(
                            animationSpec = tween(AppDuration.exit, easing = AppEase.out),
                            targetScale = 0.96f,
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        )
                }
            ) {
                val cartScale by animateFloatAsState(
                    targetValue = if (state.cartCount > 0) 1.08f else 1.0f,
                    animationSpec = SpringSettle,
                    label = "cartScale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .glassConvex(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(cartScale)
                                .noRipplePressable(pressedScale = 0.9f) { showCart = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "购物车",
                                tint = GlassAccent.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .noRipplePressable(pressedScale = 0.97f) { showCart = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "已选菜品",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = "${state.cartCount} 份",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { showOrder = true },
                            enabled = state.cartCount > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GlassAccent.primary,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("立即下单", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.isSuperAdmin) {
                    SuperAdminSpaceBanner(
                        isSuperAdmin = true,
                        currentSpace = state.coupleSpace,
                        spaces = state.allCoupleSpaces,
                        onSelectSpace = vm::selectCoupleSpace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))

                var searchInput by remember { mutableStateOf(state.searchKeyword) }
                val searchFocus = remember { FocusRequester() }

                @OptIn(FlowPreview::class)
                LaunchedEffect(Unit) {
                    snapshotFlow { searchInput }
                        .drop(1)
                        .debounce(300)
                        .distinctUntilChanged()
                        .collect { vm.search(it) }
                }

                LaunchedEffect(state.activeCategoryId) {
                    if (searchInput.isNotEmpty()) searchInput = ""
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .glassConcave(22.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocus),
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .noRipplePressable(pressedScale = 0.88f) {
                                        searchFocus.requestFocus()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索",
                                    tint = GlassAccent.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = GlassAccent.primary
                        )
                    )
                }

                if (state.categories.isEmpty() && state.loading) {
                    MenuShimmerPlaceholder()
                } else {
                    PullToRefreshBox(
                        isRefreshing = isPullRefreshing,
                        onRefresh = {
                            startPullRefresh()
                            vm.refreshHome()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .width(96.dp)
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(state.categories) { category ->
                                    val active = category.id == state.activeCategoryId
                                    val catTextColor by animateColorAsState(
                                        targetValue = if (active) GlassAccent.primary else tokens.textSecondary,
                                        label = "catTextColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .noRipplePressable(pressedScale = 0.97f) { vm.selectCategory(category.id) }
                                            .then(
                                                if (active) Modifier.glassConvex(16.dp)
                                                else Modifier.glassConcave(16.dp)
                                            )
                                            .padding(vertical = 14.dp, horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category.name,
                                            color = catTextColor,
                                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            val displayedDishes = if (state.searchKeyword.isNotEmpty()) state.searchResults else state.dishes
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                if (state.searchKeyword.isNotEmpty() && displayedDishes.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("没有找到匹配的菜品哦~", color = tokens.textSecondary)
                                        }
                                    }
                                } else {
                                    items(displayedDishes) { dish ->
                                        DishCard(dish, onAdd = { vm.addToCart(dish) })
                                    }
                                }
                                item { Spacer(Modifier.height(24.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCart) {
        CartDialog(
            cart = state.cart,
            onDismiss = { showCart = false },
            onChange = vm::changeCartQuantity,
            onClear = vm::clearCart
        )
    }
    if (showOrder) {
        SubmitOrderDialog(
            state = state,
            onDismiss = { showOrder = false },
            onSubmit = { remark, userId ->
                showOrder = false
                vm.submitOrder(remark, userId)
            }
        )
    }
}

@Composable
private fun DishCard(dish: Dish, onAdd: () -> Unit) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassConvex(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DishThumbnail(
                dishId = dish.id,
                dishName = dish.name,
                imageUrl = dish.imageUrl,
                size = 80.dp,
                emojiSize = 38.sp,
                cornerRadius = 18.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tokens.textPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .noRipplePressable(pressedScale = 0.88f, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .glassConvex(19.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        tint = GlassAccent.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartDialog(
    cart: List<CartLine>,
    onDismiss: () -> Unit,
    onChange: (Long, Int) -> Unit,
    onClear: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "购物车",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
                if (cart.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        }
                    ) {
                        Text("清空", color = tokens.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        text = {
            if (cart.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(150.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cart) { line ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassConcaveOverlay(14.dp)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = line.dish.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = tokens.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .noRipplePressable(pressedScale = 0.9f) { onChange(line.dish.id, -1) }
                                    .glassConvexOverlay(17.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "减少",
                                    tint = GlassAccent.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${line.quantity}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .noRipplePressable(pressedScale = 0.9f) { onChange(line.dish.id, 1) }
                                    .glassConvexOverlay(17.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "增加",
                                    tint = GlassAccent.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("完成", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SubmitOrderDialog(
    state: KitchenUiState,
    onDismiss: () -> Unit,
    onSubmit: (String, Long?) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var remark by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(state.recipients.firstOrNull()?.id) }
    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                "🍳 召唤大厨接单",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "🍳 指定掌勺大厨",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary
                )
                if (state.recipients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConcaveOverlay(16.dp)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (state.isSuperAdmin) {
                                "当前情侣空间暂无可接单成员，请先到管理后台为该空间添加或配对成员。"
                            } else {
                                "你当前处于单人空间，请先去「我的」页面输入对方配对码绑定另一半后再下单。"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassAccent.errorDeep
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(state.recipients) { user ->
                            val isSelected = selected == user.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .noRipplePressable(pressedScale = 0.95f) { selected = user.id }
                                    .then(
                                        if (isSelected) Modifier.glassConvexOverlay(18.dp)
                                        else Modifier.glassConcaveOverlay(18.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                val avatarUrl = user.avatarUrl
                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = realImageUrl(avatarUrl),
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(GlassAccent.primary.copy(alpha = 0.12f)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(GlassAccent.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = GlassAccent.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = user.nickname ?: "成员",
                                    color = if (isSelected) GlassAccent.primary else tokens.textPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcaveOverlay(18.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("口味备注/捎句话", color = tokens.textSecondary) },
                        shape = RoundedCornerShape(18.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = GlassAccent.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(remark, selected) },
                enabled = selected != null && state.recipients.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("提交下单", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun MenuShimmerPlaceholder() {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            repeat(6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(BorderStroke(1.dp, Color(0xFFF5EFE6)), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmerEffect()
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .shimmerEffect()
                            )
                            Spacer(Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        }
    }
}
