package com.xixikitchen.jetpack.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.Announcement
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.Category
import com.xixikitchen.jetpack.data.Dish
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.data.UserEditorPayload
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import kotlinx.coroutines.launch

private enum class AdminTab(val title: String, val caption: String) {
    Category("分类", "菜单结构"),
    Dish("菜品", "商品资料"),
    Announcement("公告", "内容发布"),
    User("用户", "账号权限"),
    Order("订单", "交易记录")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val tokens = LocalGlassTokens.current
    var tab by remember { mutableStateOf(AdminTab.Category) }
    val isSuperAdmin = state.user?.role == "super_admin"
    val currentUserId = state.user?.id
    var categoryEditor by remember { mutableStateOf<Category?>(null) }
    var showNewCategory by remember { mutableStateOf(false) }
    var dishEditor by remember { mutableStateOf<Dish?>(null) }
    var showNewDish by remember { mutableStateOf(false) }
    var announcementEditor by remember { mutableStateOf<Announcement?>(null) }
    var showNewAnnouncement by remember { mutableStateOf(false) }
    var userEditor by remember { mutableStateOf<User?>(null) }
    var showNewUser by remember { mutableStateOf(false) }
    var partnerEditor by remember { mutableStateOf<User?>(null) }
    val visibleTabs = if (isSuperAdmin) {
        AdminTab.entries
    } else {
        AdminTab.entries.filter { it != AdminTab.User }
    }

    LaunchedEffect(Unit) {
        vm.refreshHome()
        vm.refreshAnnouncements()
        vm.loadAdminData()
    }

    val moduleCount = when (tab) {
        AdminTab.Category -> state.categories.size
        AdminTab.Dish -> state.dishes.size
        AdminTab.Announcement -> state.announcements.size
        AdminTab.User -> state.adminUsers.size
        AdminTab.Order -> state.adminOrders.size
    }
    val primaryAction = {
        when (tab) {
            AdminTab.Category -> showNewCategory = true
            AdminTab.Dish -> showNewDish = true
            AdminTab.Announcement -> showNewAnnouncement = true
            AdminTab.User -> showNewUser = true
            AdminTab.Order -> vm.loadAdminData()
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            AdminConsoleHeader(
                tab = tab,
                count = moduleCount,
                isSuperAdmin = isSuperAdmin,
                onBack = { nav.popBackStack() },
                onPrimaryAction = primaryAction
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(top = 14.dp, bottom = 12.dp)
            ) {
                items(visibleTabs) { item ->
                    val itemCount = when (item) {
                        AdminTab.Category -> state.categories.size
                        AdminTab.Dish -> state.dishes.size
                        AdminTab.Announcement -> state.announcements.size
                        AdminTab.User -> state.adminUsers.size
                        AdminTab.Order -> state.adminOrders.size
                    }
                    AdminModuleCard(
                        tab = item,
                        count = itemCount,
                        selected = tab == item,
                        onClick = { tab = item }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .glassConvex(28.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(start = 14.dp, top = 16.dp, end = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${tab.title}管理",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .glassConvex(14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { primaryAction() }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tab == AdminTab.Order) "刷新" else "新增",
                                color = GlassAccent.primary,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    AdminTab.Category -> CategoryAdminList(state.categories, onEdit = { categoryEditor = it }, onDelete = vm::deleteCategory)
                    AdminTab.Dish -> DishAdminList(
                        categories = state.categories,
                        activeCategoryId = state.activeCategoryId,
                        onSelectCategory = vm::selectCategory,
                        items = state.dishes,
                        onEdit = { dishEditor = it },
                        onDelete = vm::deleteDish
                    )
                    AdminTab.Announcement -> AnnouncementAdminList(state.announcements, onEdit = { announcementEditor = it }, onDelete = vm::deleteAnnouncement)
                    AdminTab.User -> UserAdminList(
                        users = state.adminUsers,
                        currentUserId = currentUserId,
                        isSuperAdmin = isSuperAdmin,
                        onEdit = { userEditor = it },
                        onDelete = vm::deleteUser,
                        onToggleRole = vm::toggleRole,
                        onPartner = { partnerEditor = it }
                    )
                    AdminTab.Order -> OrderAdminList(state.adminOrders, onDelete = vm::deleteAdminOrder)
                }
                    }
                }
            }
        }
    }

    if (showNewCategory || categoryEditor != null) {
        CategoryEditorDialog(categoryEditor, onDismiss = { showNewCategory = false; categoryEditor = null }, onSave = {
            vm.saveCategory(it)
            showNewCategory = false
            categoryEditor = null
        })
    }
    if (showNewDish || dishEditor != null) {
        DishEditorDialog(dishEditor, state.categories, vm, onDismiss = { showNewDish = false; dishEditor = null }, onSave = {
            vm.saveDish(it)
            showNewDish = false
            dishEditor = null
        })
    }
    if (showNewAnnouncement || announcementEditor != null) {
        AnnouncementEditorDialog(announcementEditor, onDismiss = { showNewAnnouncement = false; announcementEditor = null }, onSave = { id, content ->
            vm.saveAnnouncement(id, content)
            showNewAnnouncement = false
            announcementEditor = null
        })
    }
    if (showNewUser || userEditor != null) {
        UserEditorDialog(userEditor, vm, canManageRoles = isSuperAdmin, onDismiss = { showNewUser = false; userEditor = null }, onSave = { id, payload ->
            vm.saveUser(id, payload)
            showNewUser = false
            userEditor = null
        })
    }
    partnerEditor?.let { user ->
        PartnerDialog(user, state.adminUsers, onDismiss = { partnerEditor = null }, onSave = { partnerId ->
            vm.updatePartner(user.id, partnerId)
            partnerEditor = null
        })
    }
}

@Composable
private fun AdminConsoleHeader(
    tab: AdminTab,
    count: Int,
    isSuperAdmin: Boolean,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassConvex(24.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = GlassAccent.primary)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "后台管理",
                    color = tokens.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            Box(
                modifier = Modifier
                    .glassConcave(14.dp)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSuperAdmin) "超级管理员" else "管理员",
                    color = GlassAccent.primary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun AdminModuleCard(
    tab: AdminTab,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(16.dp)
    val textColor by animateColorAsState(
        targetValue = if (selected) GlassAccent.primary else tokens.textPrimary,
        label = "tabTextColor"
    )
    Box(
        modifier = Modifier
            .then(
                if (selected) Modifier.glassConvex(16.dp) else Modifier.glassConcave(16.dp)
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tab.title,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (selected) GlassAccent.primary.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.05f),
                        CircleShape
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    color = if (selected) GlassAccent.primary else tokens.textSecondary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun AdminEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize())
}

@Composable
private fun CategoryAdminList(items: List<Category>, onEdit: (Category) -> Unit, onDelete: (Long) -> Unit) {
    if (items.isEmpty()) {
        AdminEmptyState("暂无分类数据")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                AdminCard(
                    title = item.name,
                    subtitle = "图标: ${item.icon ?: "无"}  ·  排序: ${item.sortOrder ?: 0}",
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DishAdminList(
    categories: List<Category>,
    activeCategoryId: Long?,
    onSelectCategory: (Long) -> Unit,
    items: List<Dish>,
    onEdit: (Dish) -> Unit,
    onDelete: (Long) -> Unit
) {
    val tokens = LocalGlassTokens.current
    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val active = category.id == activeCategoryId
                    val chipTextColor by animateColorAsState(
                        targetValue = if (active) GlassAccent.primary else tokens.textPrimary,
                        label = "chipTextColor"
                    )
                    Box(
                        modifier = Modifier
                            .then(
                                if (active) Modifier.glassConvex(14.dp) else Modifier.glassConcave(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCategory(category.id) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            color = chipTextColor,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium)
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AdminEmptyState("该分类下暂无菜品数据")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(items) { item ->
                    AdminCard(
                        title = item.name,
                        subtitle = "评分: ${item.rating ?: 5.0}  ·  月售: ${item.monthlySales ?: 0}",
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(item.id) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun AnnouncementAdminList(items: List<Announcement>, onEdit: (Announcement) -> Unit, onDelete: (Long) -> Unit) {
    if (items.isEmpty()) {
        AdminEmptyState("暂无公告数据")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                AdminCard(
                    title = "公告 #${item.id}",
                    subtitle = item.content,
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun UserAdminList(
    users: List<User>,
    currentUserId: Long?,
    isSuperAdmin: Boolean,
    onEdit: (User) -> Unit,
    onDelete: (Long) -> Unit,
    onToggleRole: (User) -> Unit,
    onPartner: (User) -> Unit
) {
    if (users.isEmpty()) {
        AdminEmptyState("暂无用户数据")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(users) { user ->
                val isNewlyAddedUser = user.id > 2L && user.nickname != "熙熙" && user.nickname != "哥哥"
                val tokens = LocalGlassTokens.current
                var showDeleteConfirm by remember { mutableStateOf(false) }
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (isNewlyAddedUser && value == SwipeToDismissBoxValue.EndToStart) {
                            showDeleteConfirm = true
                            false
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = isNewlyAddedUser,
                    backgroundContent = {
                        val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart || dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .glassConvex(20.dp)
                                .background(
                                    if (isSwiping) Color(0xFFFFEBEE).copy(alpha = 0.85f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "左滑删除",
                                    color = Color(0xFFE53935),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConvex(20.dp)
                            .then(
                                if (isNewlyAddedUser) {
                                    Modifier.combinedClickable(
                                        onClick = {},
                                        onLongClick = { onEdit(user) }
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .padding(16.dp)
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                user.nickname ?: "未命名用户",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "账号: ${user.username ?: "无"}  ·  角色: ${user.role}  ·  接单人: ${users.firstOrNull { it.id == user.partnerId }?.nickname ?: "未设置"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        modifier = Modifier.glassConvexOverlay(24.dp),
                        containerColor = Color.Transparent,
                        title = {
                            Text(
                                "确认删除",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = GlassAccent.primaryDark
                            )
                        },
                        text = {
                            Text(
                                "确定要删除用户 \"${user.nickname}\" 吗？此操作无法撤销。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.textPrimary
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDelete(user.id)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                            ) {
                                Text("确认删除", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("取消", color = tokens.textSecondary)
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AdminActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) Color(0xFFE53935).copy(alpha = 0.9f) else GlassAccent.primary
    val shape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = Modifier
            .height(34.dp)
            .glassConcave(12.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OrderAdminList(items: List<Order>, onDelete: (Long) -> Unit) {
    if (items.isEmpty()) {
        AdminEmptyState("暂无订单数据")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                val statusText = when (item.status) {
                    0 -> "待接单"
                    1 -> "已接单"
                    2 -> "已完成"
                    3 -> "已拒绝"
                    else -> "未知"
                }
                AdminCard(
                    title = "订单号: ${item.orderNo}",
                    subtitle = "状态: $statusText  ·  创建时间: ${item.createdAt ?: ""}",
                    onEdit = null,
                    onDelete = { onDelete(item.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AdminCard(title: String, subtitle: String, onEdit: (() -> Unit)?, onDelete: () -> Unit) {
    val tokens = LocalGlassTokens.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart || dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .glassConvex(20.dp)
                    .background(
                        if (isSwiping) Color(0xFFFFEBEE).copy(alpha = 0.85f) else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "左滑删除",
                        color = Color(0xFFE53935),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassConvex(20.dp)
                .then(
                    if (onEdit != null) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = onEdit
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            modifier = Modifier.glassConvexOverlay(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    "确认删除",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Text(
                    "确定要删除 \"$title\" 吗？此操作无法撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("确认删除", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = tokens.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun CategoryEditorDialog(category: Category?, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    val tokens = LocalGlassTokens.current
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }
    var sort by remember { mutableStateOf((category?.sortOrder ?: 0).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (category == null) "新增分类" else "编辑分类",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分类名称") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("图标 / Emoji") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                OutlinedTextField(
                    value = sort,
                    onValueChange = { sort = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("排序权重") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Category(category?.id ?: 0L, name, icon, sort.toIntOrNull() ?: 0)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
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
private fun DishEditorDialog(
    dish: Dish?,
    categories: List<Category>,
    vm: KitchenViewModel,
    onDismiss: () -> Unit,
    onSave: (Dish) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var name by remember { mutableStateOf(dish?.name ?: "") }
    var image by remember { mutableStateOf(dish?.imageUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf((dish?.sortOrder ?: 0).toString()) }
    var categoryId by remember { mutableStateOf(dish?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (dish == null) "新增菜品" else "编辑菜品",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("菜品名称") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                Text(
                    "所属分类",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = categoryId == category.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) GlassAccent.primary else Color(0xFFF5F0EB),
                                    RoundedCornerShape(14.dp)
                                )
                                .noRippleClick { categoryId = category.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.name,
                                color = if (isSelected) Color.White else tokens.textPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                DirectImageUploadField(
                    value = image,
                    label = "菜品图片",
                    emptyText = "点击选择菜品图片",
                    uploading = uploading,
                    filePrefix = "dish",
                    vm = vm,
                    onUploadingChange = { uploading = it },
                    onValueChange = { image = it }
                )
                OutlinedTextField(
                    value = sort,
                    onValueChange = { sort = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("排序权重") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Dish(id = dish?.id ?: 0L, categoryId = categoryId, name = name, imageUrl = image, sortOrder = sort.toIntOrNull() ?: 0)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                enabled = !uploading
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uploading) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun DirectImageUploadField(
    value: String,
    label: String,
    emptyText: String,
    uploading: Boolean,
    filePrefix: String,
    vm: KitchenViewModel,
    onUploadingChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    circular: Boolean = false
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        onUploadingChange(true)
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val processed = readAndCompressImage(context, uri)
                if (processed == null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onUploadingChange(false)
                    }
                    return@launch
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    vm.uploadFile(
                        bytes = processed.bytes,
                        fileName = "${filePrefix}_${System.currentTimeMillis()}.${processed.extension}",
                        onSuccess = { url ->
                            onValueChange(url)
                            onUploadingChange(false)
                        },
                        onFailure = {
                            onUploadingChange(false)
                        }
                    )
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onUploadingChange(false)
                }
            }
        }
    }
    val shape = if (circular) CircleShape else RoundedCornerShape(18.dp)
    val imageUrl = adminRealImageUrl(value)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (circular) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = tokens.textSecondary
        )
        Box(
            modifier = Modifier
                .then(if (circular) Modifier.size(92.dp) else Modifier.fillMaxWidth().height(132.dp))
                .clip(shape)
                .background(Color(0xFFFFF2F4))
                .border(BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.35f)), shape)
                .noRippleClick(enabled = !uploading) { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "更换图片",
                        tint = Color.White,
                        modifier = Modifier.size(if (circular) 24.dp else 28.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = label,
                        tint = GlassAccent.primary,
                        modifier = Modifier.size(if (circular) 26.dp else 32.dp)
                    )
                    Text(
                        emptyText,
                        color = GlassAccent.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (uploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.78f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GlassAccent.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Text(
            text = if (uploading) "正在上传..." else "选择后自动上传，无需手动填写地址",
            style = MaterialTheme.typography.bodySmall,
            color = if (uploading) GlassAccent.primary else tokens.textSecondary
        )
    }
}

@Composable
private fun AnnouncementEditorDialog(announcement: Announcement?, onDismiss: () -> Unit, onSave: (Long?, String) -> Unit) {
    val tokens = LocalGlassTokens.current
    var content by remember { mutableStateOf(announcement?.content ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (announcement == null) "新增公告" else "编辑公告",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("公告内容") },
                shape = RoundedCornerShape(16.dp),
                minLines = 4,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassAccent.primary,
                    focusedLabelColor = GlassAccent.primary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(announcement?.id, content) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
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
private fun UserEditorDialog(
    user: User?,
    vm: KitchenViewModel,
    canManageRoles: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long?, UserEditorPayload) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var nickname by remember { mutableStateOf(user?.nickname ?: "") }
    var avatar by remember { mutableStateOf(user?.avatarUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "user") }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (user == null) "新增用户" else "编辑用户",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称/登录名") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                DirectImageUploadField(
                    value = avatar,
                    label = "用户头像",
                    emptyText = "点击选择头像",
                    uploading = uploading,
                    filePrefix = "avatar",
                    circular = true,
                    vm = vm,
                    onUploadingChange = { uploading = it },
                    onValueChange = { avatar = it }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (user == null) "密码" else "新密码（空表示不修改）") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isUser = role == "user"
                    val isAdmin = role == "admin"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isUser) GlassAccent.primary else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                            .noRippleClick { role = "user" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("普通用户", color = if (isUser) Color.White else tokens.textPrimary, fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isAdmin) GlassAccent.primary else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                            .noRippleClick { role = "admin" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("管理员", color = if (isAdmin) Color.White else tokens.textPrimary, fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(user?.id, UserEditorPayload(nickname, avatar, password.takeIf { it.isNotBlank() }, role, user?.partnerId)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                enabled = !uploading
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uploading) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun PartnerDialog(user: User, users: List<User>, onDismiss: () -> Unit, onSave: (Long?) -> Unit) {
    val tokens = LocalGlassTokens.current
    var partnerId by remember { mutableStateOf(user.partnerId) }
    val candidates = users.filter { it.id != user.id }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                "设置接单人",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "为 [${user.nickname ?: "用户"}] 设置关联接单人:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isNone = partnerId == null
                        Box(
                            modifier = Modifier
                                .background(if (isNone) GlassAccent.primary else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                                .noRippleClick { partnerId = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未设置", color = if (isNone) Color.White else tokens.textPrimary, fontWeight = if (isNone) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    items(candidates) { candidate ->
                        val isSel = partnerId == candidate.id
                        Box(
                            modifier = Modifier
                                .background(if (isSel) GlassAccent.primary else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                                .noRippleClick { partnerId = candidate.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(candidate.nickname ?: "成员", color = if (isSel) Color.White else tokens.textPrimary, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(partnerId) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

private fun adminRealImageUrl(url: String?): String? {
    return BackendConfig.resolveUrl(url)
}
