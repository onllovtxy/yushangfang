package com.xixikitchen.jetpack.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.OrderCheckIn
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import com.xixikitchen.jetpack.ui.KitchenUiState
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.components.ConfirmDeleteDialog
import com.xixikitchen.jetpack.ui.components.DishThumbnail
import com.xixikitchen.jetpack.ui.components.PhotoLightboxDialog
import com.xixikitchen.jetpack.ui.components.StatusFilterChip
import com.xixikitchen.jetpack.ui.components.UserMiniAvatar
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.components.statusText
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.orderStatusColors
import com.xixikitchen.jetpack.ui.noRippleCombinedPressable
import com.xixikitchen.jetpack.ui.noRipplePressable

private data class ResolvedOrderUsers(
    val fromName: String,
    val fromAvatar: String?,
    val toName: String,
    val toAvatar: String?
)

private fun resolveOrderUsers(order: Order, state: KitchenUiState): ResolvedOrderUsers {
    val fromFallback = state.recipients.firstOrNull { it.id == order.fromUserId }
        ?: if (state.user?.id == order.fromUserId) state.user else null
    val toFallback = state.recipients.firstOrNull { it.id == order.toUserId }
        ?: if (state.user?.id == order.toUserId) state.user else null

    return ResolvedOrderUsers(
        fromName = order.fromUser?.nickname ?: fromFallback?.nickname ?: "未知成员",
        fromAvatar = order.fromUser?.avatarUrl ?: fromFallback?.avatarUrl,
        toName = order.toUser?.nickname ?: toFallback?.nickname ?: "未知接单人",
        toAvatar = order.toUser?.avatarUrl ?: toFallback?.avatarUrl
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshOrders() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                startPullRefresh()
                vm.refreshOrders()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    StatusFilterChip(state.orderStatus == null, { vm.refreshOrders(null) }, "全部", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 0, { vm.refreshOrders(0) }, "等喂食", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 1, { vm.refreshOrders(1) }, "做饭中", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 2, { vm.refreshOrders(2) }, "已享用", Modifier.weight(1f))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            state = state,
                            onOpen = { nav.navigate("orderDetail/${order.id}") },
                            onAction = { vm.orderAction(order.id, it) }
                        )
                    }
                    if (state.orders.isNotEmpty()) {
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    state: KitchenUiState,
    onOpen: () -> Unit,
    onAction: (String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val statusColors = orderStatusColors(order.status)
    val users = resolveOrderUsers(order, state)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.985f, onClick = onOpen)
            .glassConvex(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "订单 #${order.orderNo.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tokens.textPrimary
                )
                Box(
                    modifier = Modifier
                        .background(statusColors.wash, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText(order.status),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColors.on
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = order.createdAt.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary
            )

            Spacer(Modifier.height(12.dp))

            if (order.items.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcave(12.dp)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        order.items.take(4).forEach { item ->
                            DishThumbnail(
                                dishId = item.dishId,
                                dishName = item.dishName,
                                imageUrl = item.dishImage,
                                size = 44.dp,
                                emojiSize = 22.sp,
                                cornerRadius = 10.dp
                            )
                        }
                        if (order.items.size > 4) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GlassAccent.wash),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+${order.items.size - 4}",
                                    color = GlassAccent.primary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Text(
                        text = "共 ${order.items.sumOf { it.quantity }} 份",
                        color = tokens.textSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassAccent.wash, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserMiniAvatar(users.fromAvatar)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = users.fromName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary
                )

                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = GlassAccent.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(8.dp))

                UserMiniAvatar(users.toAvatar)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = users.toName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary
                )
            }

            val currentUserId = state.user?.id ?: -1
            if (order.toUserId == currentUserId && (order.status == 0 || order.status == 1)) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.status == 0) {
                        OutlinedButton(
                            onClick = { onAction("reject") },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary)
                        ) {
                            Text("拒绝")
                        }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = { onAction("accept") },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                        ) {
                            Text("接单")
                        }
                    } else if (order.status == 1) {
                        Button(
                            onClick = { onAction("complete") },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Text("完成烹饪", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    state: KitchenUiState,
    discoverState: DiscoverUiState,
    vm: KitchenViewModel,
    discoverVm: DiscoverViewModel,
    nav: NavHostController
) {
    val tokens = LocalGlassTokens.current
    val order = state.selectedOrder
    var showCreateCheckIn by remember { mutableStateOf(false) }
    var editingCheckIn by remember { mutableStateOf<OrderCheckIn?>(null) }
    var actionTargetCheckIn by remember { mutableStateOf<OrderCheckIn?>(null) }
    var deleteTargetCheckIn by remember { mutableStateOf<OrderCheckIn?>(null) }
    var lightboxCheckIn by remember { mutableStateOf<OrderCheckIn?>(null) }

    LaunchedEffect(order?.id, order?.status) {
        if (order != null && order.status == 2) {
            discoverVm.loadOrderCheckIns(order.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = GlassAccent.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding))
        } else {
            val users = resolveOrderUsers(order, state)
            val checkIns = discoverState.orderCheckIns[order.id].orEmpty()
            val currentUserId = state.user?.id ?: -1L
            val isParticipant = order.fromUserId == currentUserId || order.toUserId == currentUserId

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    val statusColors = orderStatusColors(order.status)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConvex(20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "订单信息",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.textPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .background(statusColors.wash, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        statusText(order.status),
                                        color = statusColors.on,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("订单编号: ${order.orderNo}", style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("下单时间: ${order.createdAt.orEmpty()}", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
                            if (!order.acceptedAt.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("接单时间: ${order.acceptedAt}", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
                            }
                            if (!order.completedAt.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("出锅时间: ${order.completedAt}", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GlassAccent.wash, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(users.fromAvatar)
                                    Spacer(Modifier.height(4.dp))
                                    Text(users.fromName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                                    Text("下单人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GlassAccent.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(users.toAvatar)
                                    Spacer(Modifier.height(4.dp))
                                    Text(users.toName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                                    Text("烹饪人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                                }
                            }

                            if (!order.remark.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GlassAccent.wash, RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, GlassAccent.edge), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "备注: ${order.remark}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = GlassAccent.primaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "菜品清单",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textPrimary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                items(order.items) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConvex(20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DishThumbnail(
                                    dishId = item.dishId,
                                    dishName = item.dishName,
                                    imageUrl = item.dishImage,
                                    size = 50.dp,
                                    emojiSize = 24.sp,
                                    cornerRadius = 10.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = item.dishName ?: "未知菜品",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.textPrimary
                                    )
                                }
                                Text(
                                    text = "x${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassAccent.primary
                                )
                            }
                        }
                    }
                }

                if (order.toUserId == currentUserId && (order.status == 0 || order.status == 1)) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (order.status == 0) {
                                OutlinedButton(
                                    onClick = { vm.orderAction(order.id, "reject") },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.LightGray),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("拒绝接单")
                                }
                                Button(
                                    onClick = { vm.orderAction(order.id, "accept") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("接受订单", fontWeight = FontWeight.Bold)
                                }
                            } else if (order.status == 1) {
                                Button(
                                    onClick = { vm.orderAction(order.id, "complete") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("完成烹饪", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (order.status == 2) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 8.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (checkIns.isEmpty()) "成品打卡" else "成品打卡 (${checkIns.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary
                            )
                        }
                    }

                    items(checkIns, key = { it.id }) { checkIn ->
                        val canManage = discoverState.isAdmin || checkIn.userId == currentUserId
                        OrderCheckInCard(
                            checkIn = checkIn,
                            canManage = canManage,
                            onImageClick = { lightboxCheckIn = checkIn },
                            onLongClick = { if (canManage) actionTargetCheckIn = checkIn }
                        )
                    }

                    if (isParticipant) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRipplePressable(pressedScale = 0.975f) { showCreateCheckIn = true }
                                    .glassConvex(20.dp)
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (checkIns.isEmpty()) "📸 上传成品打卡" else "📸 再晒一张",
                                    color = GlassAccent.primary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (showCreateCheckIn && order != null) {
        CheckInEditorDialog(
            initial = null,
            onUpload = discoverVm::uploadImage,
            onDismiss = { showCreateCheckIn = false },
            onSave = { imageUrl, note ->
                showCreateCheckIn = false
                discoverVm.addCheckIn(order.id, imageUrl, note)
            }
        )
    }

    editingCheckIn?.let { target ->
        CheckInEditorDialog(
            initial = target,
            onUpload = discoverVm::uploadImage,
            onDismiss = { editingCheckIn = null },
            onSave = { imageUrl, note ->
                editingCheckIn = null
                discoverVm.updateCheckIn(target, imageUrl, note)
            }
        )
    }

    actionTargetCheckIn?.let { target ->
        val actionTokens = LocalGlassTokens.current
        AlertDialog(
            onDismissRequest = { actionTargetCheckIn = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    "成品打卡",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Text(
                    "想要编辑或删除这张成品打卡吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = actionTokens.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingCheckIn = target
                        actionTargetCheckIn = null
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                ) {
                    Text("编辑", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { actionTargetCheckIn = null }) {
                        Text("取消", color = actionTokens.textSecondary)
                    }
                    TextButton(
                        onClick = {
                            deleteTargetCheckIn = target
                            actionTargetCheckIn = null
                        }
                    ) {
                        Text("删除", color = GlassAccent.error)
                    }
                }
            }
        )
    }

    deleteTargetCheckIn?.let { target ->
        ConfirmDeleteDialog(
            message = "确定要删除这张成品打卡吗？此操作无法撤销。",
            onConfirm = {
                deleteTargetCheckIn = null
                discoverVm.deleteCheckIn(target)
            },
            onDismiss = { deleteTargetCheckIn = null }
        )
    }

    lightboxCheckIn?.let { target ->
        PhotoLightboxDialog(
            imageUrl = target.imageUrl,
            caption = target.note.takeIf { it.isNotBlank() },
            onDismiss = { lightboxCheckIn = null }
        )
    }
}

@Composable
private fun OrderCheckInCard(
    checkIn: OrderCheckIn,
    canManage: Boolean,
    onImageClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleCombinedPressable(
                pressedScale = 0.985f,
                onLongClick = if (canManage) onLongClick else null,
                onClick = onImageClick
            )
            .glassConvex(20.dp)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AsyncImage(
                model = realImageUrl(checkIn.imageUrl),
                contentDescription = "成品照",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassAccent.wash),
                contentScale = ContentScale.Crop
            )
            if (checkIn.note.isNotBlank()) {
                Text(
                    text = checkIn.note,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = tokens.textPrimary,
                    lineHeight = 20.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserMiniAvatar(checkIn.authorAvatar, size = 22.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = checkIn.authorName ?: "成员",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(GlassAccent.wash, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (checkIn.roleTag == "chef") "掌勺打卡" else "食客打卡",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassAccent.primary
                        )
                    }
                }
                Text(
                    text = checkIn.createdAt?.take(16).orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }
        }
    }
}
