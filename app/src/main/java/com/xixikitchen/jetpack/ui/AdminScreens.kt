package com.xixikitchen.jetpack.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch

// Colors matching XixiKitchenApp palette
private val CoralPink = Color(0xFFFF5E7E)
private val PeachSunset = Color(0xFFFF9E79)
private val CoralDark = Color(0xFFD63B5D)
private val OatmealPage = Color(0xFFFAF6F0)
private val TextDark = Color(0xFF3C3333)
private val TextMuted = Color(0xFF8C7E7E)

private enum class AdminTab(val title: String) {
    Category("分类"),
    Dish("菜品"),
    Announcement("公告"),
    User("用户"),
    Order("订单")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = CoralPink
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            when (tab) {
                                AdminTab.Category -> showNewCategory = true
                                AdminTab.Dish -> showNewDish = true
                                AdminTab.Announcement -> showNewAnnouncement = true
                                AdminTab.User -> showNewUser = true
                                AdminTab.Order -> vm.loadAdminData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (tab == AdminTab.Order) "刷新" else "新增",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OatmealPage)
            )
        },
        containerColor = OatmealPage
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            // Styled Tab Bar (Distributed full row)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                visibleTabs.forEach { item ->
                    val active = tab == item
                    val tabBgColor by animateColorAsState(
                        targetValue = if (active) CoralPink else Color.White,
                        label = "tabBgColor"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (active) Color.White else TextDark,
                        label = "tabTextColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(tabBgColor, RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(1.dp, if (active) Color.Transparent else Color(0xFFEFE6DD)),
                                RoundedCornerShape(12.dp)
                            )
                            .noRippleClick { tab = item }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.title,
                            color = tabTextColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            
            // Tab Contents
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
private fun AdminEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge
        )
    }
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
    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val active = category.id == activeCategoryId
                    val chipBgColor by animateColorAsState(
                        targetValue = if (active) CoralPink else Color.White,
                        label = "chipBgColor"
                    )
                    val chipTextColor by animateColorAsState(
                        targetValue = if (active) Color.White else TextDark,
                        label = "chipTextColor"
                    )
                    Box(
                        modifier = Modifier
                            .background(chipBgColor, RoundedCornerShape(16.dp))
                            .border(
                                BorderStroke(1.dp, if (active) Color.Transparent else Color(0xFFEFE6DD)),
                                RoundedCornerShape(16.dp)
                            )
                            .noRippleClick { onSelectCategory(category.id) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            color = chipTextColor,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
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
                val canEdit = isSuperAdmin || user.id == currentUserId
                val canManagePartner = isSuperAdmin || user.id == currentUserId
                val canChangeRole = isSuperAdmin && user.role != "super_admin"
                val canDelete = isSuperAdmin && user.role != "super_admin" && user.id != currentUserId
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            user.nickname ?: "未命名用户",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "账号: ${user.username ?: "无"}  ·  角色: ${user.role}  ·  接单人: ${users.firstOrNull { it.id == user.partnerId }?.nickname ?: "未设置"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(end = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                AdminActionPill(
                                    text = "编辑",
                                    container = Color(0xFFFFF2F4),
                                    content = CoralPink,
                                    onClick = { onEdit(user) }
                                )
                            }
                            item {
                                AdminActionPill(
                                    text = "接单人",
                                    container = Color(0xFFF3EFE9),
                                    content = TextDark,
                                    onClick = { onPartner(user) }
                                )
                            }
                            item {
                                AdminActionPill(
                                    text = if (user.role == "admin") "设普通" else "设管理",
                                    container = Color(0xFFEAF5FF),
                                    content = Color(0xFF1E88E5),
                                    minWidth = 74.dp,
                                    onClick = { onToggleRole(user) }
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFFFEBEE), CircleShape)
                                        .noRippleClick { onDelete(user.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, "删除", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AdminActionPill(
    text: String,
    container: Color,
    content: Color,
    minWidth: androidx.compose.ui.unit.Dp = 68.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(minWidth)
            .background(container, RoundedCornerShape(14.dp))
            .noRippleClick { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = content,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
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

@Composable
private fun AdminCard(title: String, subtitle: String, onEdit: (() -> Unit)?, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.background(Color(0xFFFFF0F2), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, "编辑", tint = CoralPink, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, "删除", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(category: Category?, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }
    var sort by remember { mutableStateOf((category?.sortOrder ?: 0).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                if (category == null) "新增分类" else "编辑分类",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Category(category?.id ?: 0L, name, icon, sort.toIntOrNull() ?: 0)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextMuted)
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
    var name by remember { mutableStateOf(dish?.name ?: "") }
    var image by remember { mutableStateOf(dish?.imageUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf((dish?.sortOrder ?: 0).toString()) }
    var categoryId by remember { mutableStateOf(dish?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                if (dish == null) "新增菜品" else "编辑菜品",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
                    )
                )
                Text(
                    "所属分类",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
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
                                    if (isSelected) CoralPink else Color(0xFFF5F0EB),
                                    RoundedCornerShape(14.dp)
                                )
                                .noRippleClick { categoryId = category.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.name,
                                color = if (isSelected) Color.White else TextDark,
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Dish(id = dish?.id ?: 0L, categoryId = categoryId, name = name, imageUrl = image, sortOrder = sort.toIntOrNull() ?: 0)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
                enabled = !uploading
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uploading) {
                Text("取消", color = TextMuted)
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
            color = TextMuted
        )
        Box(
            modifier = Modifier
                .then(if (circular) Modifier.size(92.dp) else Modifier.fillMaxWidth().height(132.dp))
                .clip(shape)
                .background(Color(0xFFFFF2F4))
                .border(BorderStroke(1.dp, CoralPink.copy(alpha = 0.35f)), shape)
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
                        tint = CoralPink,
                        modifier = Modifier.size(if (circular) 26.dp else 32.dp)
                    )
                    Text(
                        emptyText,
                        color = CoralPink,
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
                        color = CoralPink,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Text(
            text = if (uploading) "正在上传..." else "选择后自动上传，无需手动填写地址",
            style = MaterialTheme.typography.bodySmall,
            color = if (uploading) CoralPink else TextMuted
        )
    }
}

@Composable
private fun AnnouncementEditorDialog(announcement: Announcement?, onDismiss: () -> Unit, onSave: (Long?, String) -> Unit) {
    var content by remember { mutableStateOf(announcement?.content ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                if (announcement == null) "新增公告" else "编辑公告",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
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
                    focusedBorderColor = CoralPink,
                    focusedLabelColor = CoralPink
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(announcement?.id, content) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextMuted)
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
    var nickname by remember { mutableStateOf(user?.nickname ?: "") }
    var avatar by remember { mutableStateOf(user?.avatarUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "user") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                if (user == null) "新增用户" else "编辑用户",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
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
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink
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
                            .background(if (isUser) CoralPink else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                            .noRippleClick { role = "user" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("普通用户", color = if (isUser) Color.White else TextDark, fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isAdmin) CoralPink else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                            .noRippleClick { role = "admin" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("管理员", color = if (isAdmin) Color.White else TextDark, fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(user?.id, UserEditorPayload(nickname, avatar, password.takeIf { it.isNotBlank() }, role, user?.partnerId)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
                enabled = !uploading
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uploading) {
                Text("取消", color = TextMuted)
            }
        }
    )
}

@Composable
private fun PartnerDialog(user: User, users: List<User>, onDismiss: () -> Unit, onSave: (Long?) -> Unit) {
    var partnerId by remember { mutableStateOf(user.partnerId) }
    val candidates = users.filter { it.id != user.id }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                "设置接单人",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "为 [${user.nickname ?: "用户"}] 设置关联接单人:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isNone = partnerId == null
                        Box(
                            modifier = Modifier
                                .background(if (isNone) CoralPink else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                                .noRippleClick { partnerId = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未设置", color = if (isNone) Color.White else TextDark, fontWeight = if (isNone) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    items(candidates) { candidate ->
                        val isSel = partnerId == candidate.id
                        Box(
                            modifier = Modifier
                                .background(if (isSel) CoralPink else Color(0xFFF5F0EB), RoundedCornerShape(14.dp))
                                .noRippleClick { partnerId = candidate.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(candidate.nickname ?: "成员", color = if (isSel) Color.White else TextDark, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(partnerId) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextMuted)
            }
        }
    )
}

private fun adminRealImageUrl(url: String?): String? {
    return BackendConfig.resolveUrl(url)
}
