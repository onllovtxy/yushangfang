package com.xixikitchen.jetpack.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.CartLine
import com.xixikitchen.jetpack.data.Category
import com.xixikitchen.jetpack.data.Dish
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Redesigned Kitchen Theme Palette
private val CoralPink = Color(0xFFFF5E7E)
private val PeachSunset = Color(0xFFFF9E79)
private val CoralDark = Color(0xFFD63B5D)
private val PrimaryLight = Color(0xFFFFECEF)
private val OatmealPage = Color(0xFFFAF6F0)
private val WarmCream = Color(0xFFFFFDFB)
private val TextDark = Color(0xFF3C3333)
private val TextMuted = Color(0xFF8C7E7E)

@Composable
fun XixiKitchenApp(vm: KitchenViewModel) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        state.message?.let {
            scope.launch { snack.showSnackbar(it) }
            vm.consumeMessage()
        }
    }

    val kitchenColorScheme = lightColorScheme(
        primary = CoralPink,
        onPrimary = Color.White,
        secondary = PeachSunset,
        onSecondary = Color.White,
        background = OatmealPage,
        surface = Color.White,
        onSurface = TextDark,
        surfaceVariant = WarmCream,
        onSurfaceVariant = TextMuted
    )

    MaterialTheme(colorScheme = kitchenColorScheme) {
        Surface(Modifier.fillMaxSize(), color = OatmealPage) {
            if (!state.isLoggedIn) {
                LoginScreen(
                    loading = state.loading,
                    backendBaseUrl = state.backendBaseUrl,
                    onLogin = vm::login,
                    onResetPassword = vm::resetPassword,
                    onSaveBackendBaseUrl = vm::saveBackendBaseUrl
                )
            } else {
                val nav = rememberNavController()
                val backStack by nav.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route ?: "kitchen"
                val showBottomBar = currentRoute in listOf("kitchen", "orders", "discover", "mine")
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(snack) { data ->
                            CustomSnackbar(data.visuals.message)
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            MainBottomBar(nav)
                        }
                    },
                    containerColor = OatmealPage
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        NavHost(navController = nav, startDestination = "kitchen") {
                            composable("kitchen") { KitchenScreen(state, vm) }
                            composable("orders") { OrdersScreen(state, vm, nav) }
                            composable("discover") { DiscoverScreen(state, vm) }
                            composable("mine") { MineScreen(state, vm, nav) }
                            composable("admin") { AdminScreen(state, vm, nav) }
                            composable("orderDetail/{id}") { entry ->
                                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                                LaunchedEffect(id) { vm.loadOrderDetail(id) }
                                OrderDetailScreen(state, vm, nav)
                            }
                        }
                        if (state.loading && state.categories.isNotEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = CoralPink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainBottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "kitchen"
    val items = listOf(
        Triple("kitchen", "点单", Icons.Default.Home),
        Triple("orders", "订单", Icons.AutoMirrored.Filled.ListAlt),
        Triple("discover", "发现", Icons.Default.Star),
        Triple("mine", "我的", Icons.Default.Person)
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            items.forEach { (route, label, icon) ->
                val active = current == route
                val tintColor by animateColorAsState(
                    targetValue = if (active) CoralPink else TextMuted,
                    label = "tintColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (active) CoralPink else TextMuted,
                    label = "textColor"
                )
                val activeBgColor by animateColorAsState(
                    targetValue = if (active) Color(0xFFFFEEF2) else Color.Transparent,
                    label = "activeBgColor"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (active) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconScale"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .noRippleClick {
                            if (current != route) {
                                nav.navigate(route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(48.dp)
                            .scale(iconScale)
                            .background(
                                activeBgColor,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = tintColor,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        label,
                        color = textColor,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberPullRefreshState(globalLoading: Boolean): Pair<Boolean, () -> Unit> {
    var pullRefreshing by remember { mutableStateOf(false) }
    var pullRefreshStarted by remember { mutableStateOf(false) }
    LaunchedEffect(globalLoading, pullRefreshing) {
        if (pullRefreshing && globalLoading) {
            pullRefreshStarted = true
        }
        if (pullRefreshing && pullRefreshStarted && !globalLoading) {
            pullRefreshing = false
            pullRefreshStarted = false
        }
    }
    return pullRefreshing to {
        if (!pullRefreshing) {
            pullRefreshing = true
            pullRefreshStarted = false
        }
    }
}

@Composable
private fun LoginScreen(
    loading: Boolean,
    backendBaseUrl: String,
    onLogin: (String, String) -> Unit,
    onResetPassword: (String, String, String) -> Unit,
    onSaveBackendBaseUrl: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBackendDialog by remember { mutableStateOf(false) }
    var iconTapCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF5F5), OatmealPage)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticlesBackground()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .noRippleClick {
                            iconTapCount += 1
                            if (iconTapCount >= 10) {
                                iconTapCount = 0
                                showBackendDialog = true
                            }
                        }
                        .background(
                            Brush.linearGradient(colors = listOf(PeachSunset, CoralPink)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FoodBank,
                        contentDescription = "Kitchen Icon",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = CoralPink) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPink,
                        unfocusedBorderColor = Color(0xFFF0E5D8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFFDFD),
                        focusedLabelColor = CoralPink,
                        cursorColor = CoralPink
                    )
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("密码") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = CoralPink) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPink,
                        unfocusedBorderColor = Color(0xFFF0E5D8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFFDFD),
                        focusedLabelColor = CoralPink,
                        cursorColor = CoralPink
                    )
                )
                TextButton(
                    onClick = { showResetDialog = true },
                    enabled = !loading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("重置密码", color = CoralPink)
                }
                Spacer(Modifier.height(28.dp))

                Button(
                    enabled = !loading,
                    onClick = { onLogin(username.trim(), password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (loading) {
                                    Brush.linearGradient(colors = listOf(Color.LightGray, Color.Gray))
                                } else {
                                    Brush.linearGradient(colors = listOf(PeachSunset, CoralPink))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (loading) "登录中..." else "登录",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            initialUsername = username,
            loading = loading,
            onDismiss = { showResetDialog = false },
            onConfirm = { resetKey, resetUsername, newPassword ->
                onResetPassword(resetKey, resetUsername, newPassword)
                showResetDialog = false
            }
        )
    }

    if (showBackendDialog) {
        BackendAddressDialog(
            initialBaseUrl = backendBaseUrl,
            loading = loading,
            onDismiss = { showBackendDialog = false },
            onConfirm = { baseUrl ->
                onSaveBackendBaseUrl(baseUrl)
                showBackendDialog = false
            }
        )
    }
}

@Composable
private fun BackendAddressDialog(
    initialBaseUrl: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var baseUrl by remember(initialBaseUrl) { mutableStateOf(initialBaseUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("后端地址", fontWeight = FontWeight.Bold, color = TextDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPink,
                        focusedLabelColor = CoralPink,
                        cursorColor = CoralPink
                    )
                )
                Text(
                    "模拟器本机默认用 ${BackendConfig.DEFAULT_BASE_URL}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(baseUrl) },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("取消", color = TextMuted)
            }
        }
    )
}

@Composable
private fun ResetPasswordDialog(
    initialUsername: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var resetKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialUsername) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = resetKey,
                    onValueChange = { resetKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("重置密钥") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("新密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading &&
                    resetKey.isNotBlank() &&
                    username.isNotBlank() &&
                    newPassword.length >= 6,
                onClick = { onConfirm(resetKey, username, newPassword) }
            ) {
                Text("确认重置", color = CoralPink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextMuted)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitchenScreen(state: KitchenUiState, vm: KitchenViewModel) {
    var showCart by remember { mutableStateOf(false) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    var showOrder by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refreshHome() }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            val cartScale by animateFloatAsState(
                targetValue = if (state.cartCount > 0) 1.08f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "cartScale"
            )
            AnimatedVisibility(
                visible = state.cartCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
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
                                .clip(CircleShape)
                                .background(PrimaryLight)
                                .clickable { showCart = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "购物车",
                                tint = CoralPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCart = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "已选菜品",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "${state.cartCount} 份",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { showOrder = true },
                            enabled = state.cartCount > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralPink,
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
        containerColor = OatmealPage
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.searchKeyword,
                onValueChange = vm::search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索你想吃的菜品...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CoralPink) },
                trailingIcon = {
                    if (state.searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { vm.search("") }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "清空",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPink,
                    focusedLabelColor = CoralPink,
                    unfocusedBorderColor = Color(0xFFF0E5D8),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            val latestAnn = state.announcements.firstOrNull()
            if (latestAnn != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F6)),
                    border = BorderStroke(1.dp, Color(0xFFFFE3E6)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📢 公告: ${latestAnn.content}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = CoralDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxSize()
                            .background(Color(0xFFF7F3EE))
                    ) {
                        items(state.categories) { category ->
                            val active = category.id == state.activeCategoryId
                            val catBgColor by animateColorAsState(
                                targetValue = if (active) Color.White else Color.Transparent,
                                label = "catBgColor"
                            )
                            val catTextColor by animateColorAsState(
                                targetValue = if (active) CoralPink else TextDark,
                                label = "catTextColor"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            bottomStart = 16.dp
                                        )
                                    )
                                    .background(catBgColor)
                                    .clickable { vm.selectCategory(category.id) }
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (active) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(20.dp)
                                            .background(CoralPink, RoundedCornerShape(2.dp))
                                            .align(Alignment.CenterStart)
                                    )
                                }
                                Text(
                                    text = category.name,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = catTextColor,
                                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    val displayedDishes = if (state.searchKeyword.isNotEmpty()) state.searchResults else state.dishes
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.searchKeyword.isNotEmpty() && displayedDishes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("没有找到匹配的菜品哦~", color = TextMuted)
                                }
                            }
                        } else {
                            items(displayedDishes) { dish ->
                                DishCard(dish, onAdd = { vm.addToCart(dish) })
                            }
                        }
                        item {
                            CuteBackyardGardenDecor(
                                modifier = Modifier.padding(vertical = 16.dp),
                                height = if (displayedDishes.isEmpty()) 420.dp else 200.dp
                            )
                        }
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
            }
            }

            SweetInteractMascot(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            )
        }
    }

    if (showCart) CartDialog(state.cart, onDismiss = { showCart = false }, onChange = vm::changeCartQuantity, onClear = vm::clearCart)
    if (showOrder) SubmitOrderDialog(state, onDismiss = { showOrder = false }, onSubmit = { remark, userId ->
        showOrder = false
        vm.submitOrder(remark, userId)
    })
}

@Composable
private fun DishCard(dish: Dish, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val model = realImageUrl(dish.imageUrl)
            if (!model.isNullOrBlank()) {
                AsyncImage(
                    model = model,
                    contentDescription = dish.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF5F5)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                FallbackFoodAvatar(
                    dishId = dish.id,
                    dishName = dish.name,
                    size = 80.dp,
                    emojiSize = 38.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB03A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${dish.rating ?: 5.0}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "月售 ${dish.monthlySales ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            var isPressed by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.82f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "buttonScale"
            )
            val scope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors = listOf(PeachSunset, CoralPink))
                    )
                    .clickable {
                        scope.launch {
                            isPressed = true
                            kotlinx.coroutines.delay(80)
                            isPressed = false
                        }
                        onAdd()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CartDialog(cart: List<CartLine>, onDismiss: () -> Unit, onChange: (Long, Int) -> Unit, onClear: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🛒 爱的采购清单",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = CoralDark
                )
                if (cart.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        }
                    ) {
                        Text("清空", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        text = {
            if (cart.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("🧸 肚子咕咕叫啦，快去挑几道美味吧~", color = TextMuted)
                }
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
                                .background(Color(0xFFFFFBF9), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = line.dish.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(BorderStroke(1.dp, Color(0xFFF5EFE6)), CircleShape)
                                    .clickable { onChange(line.dish.id, -1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "减少",
                                    tint = CoralPink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${line.quantity}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextDark,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(CoralPink)
                                    .clickable { onChange(line.dish.id, 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "增加",
                                    tint = Color.White,
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
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("完成", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SubmitOrderDialog(state: KitchenUiState, onDismiss: () -> Unit, onSubmit: (String, Long?) -> Unit) {
    var remark by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(state.recipients.firstOrNull()?.id) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                "🍳 召唤大厨接单",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "🍳 指定掌勺大厨",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    items(state.recipients) { user ->
                        val isSelected = selected == user.id
                        val borderCol = if (isSelected) CoralPink else Color(0xFFF0E5D8)
                        val bgCol = if (isSelected) Color(0xFFFFECEF) else Color.White
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(bgCol)
                                .border(BorderStroke(1.5.dp, borderCol), RoundedCornerShape(18.dp))
                                .clickable { selected = user.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val avatarUrl = user.avatarUrl
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = realImageUrl(avatarUrl),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF0F2)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF0F2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CoralPink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = user.nickname ?: "成员",
                                color = if (isSelected) CoralPink else TextDark,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("口味备注/捎句话") },
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 3,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPink,
                        unfocusedBorderColor = Color(0xFFF0E5D8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFFDFD),
                        focusedLabelColor = CoralPink
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(remark, selected) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
            ) {
                Text("提交下单", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextMuted)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshOrders() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = OatmealPage
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
                    StatusFilterChip(state.orderStatus == null, { vm.refreshOrders(null) }, "全部 🍽️", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 0, { vm.refreshOrders(0) }, "等喂食 ⏰", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 1, { vm.refreshOrders(1) }, "做饭中 🍳", Modifier.weight(1f))
                    StatusFilterChip(state.orderStatus == 2, { vm.refreshOrders(2) }, "已享用 😋", Modifier.weight(1f))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.orders.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                DecorativeEmptyState(
                                    message = "还没有点单订单，快去召唤大厨吧~",
                                    icon = Icons.Default.Favorite
                                )
                            }
                        }
                    } else {
                        items(state.orders) { order ->
                            OrderCard(
                                order = order,
                                state = state,
                                onOpen = { nav.navigate("orderDetail/${order.id}") },
                                onAction = { vm.orderAction(order.id, it) }
                            )
                        }
                        item {
                            CuteBackyardGardenDecor(modifier = Modifier.padding(vertical = 16.dp))
                        }
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }

            SweetInteractMascot(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    active: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (active) CoralPink else Color.White,
                RoundedCornerShape(16.dp)
            )
            .border(
                BorderStroke(1.dp, if (active) Color.Transparent else Color(0xFFEFE6DD)),
                RoundedCornerShape(16.dp)
            )
            .noRippleClick { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color.White else TextDark,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun OrderCard(order: Order, state: KitchenUiState, onOpen: () -> Unit, onAction: (String) -> Unit) {
    val (statusBg, statusFg) = when (order.status) {
        0 -> Color(0xFFFFECE0) to Color(0xFFFF8A43)  // Pending (Orange)
        1 -> Color(0xFFE3F2FD) to Color(0xFF1E88E5)  // Accepted/InProgress (Blue)
        2 -> Color(0xFFE8F5E9) to Color(0xFF43A047)  // Completed (Green)
        3 -> Color(0xFFFFEBEE) to Color(0xFFE53935)  // Rejected (Red)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF5EFE6))
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
                    color = TextDark
                )
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText(order.status),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusFg
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "${order.createdAt ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(Modifier.height(12.dp))

            if (order.items.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFDFB), RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color(0xFFFAF2EB)), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        order.items.take(4).forEach { item ->
                            val imageUrl = realImageUrl(item.dishImage)
                            if (!imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = item.dishName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF5F5)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                FallbackFoodAvatar(
                                    dishId = item.dishId,
                                    dishName = item.dishName,
                                    size = 44.dp,
                                    emojiSize = 22.sp,
                                    cornerRadius = 10.dp
                                )
                            }
                        }
                        if (order.items.size > 4) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF0F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+${order.items.size - 4}",
                                    color = CoralPink,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Text(
                        text = "共 ${order.items.sumOf { it.quantity }} 份",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val fromUser = state.recipients.firstOrNull { it.id == order.fromUserId } ?: (if (state.user?.id == order.fromUserId) state.user else null)
            val toUser = state.recipients.firstOrNull { it.id == order.toUserId } ?: (if (state.user?.id == order.toUserId) state.user else null)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAF6F0).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserMiniAvatar(fromUser)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = fromUser?.nickname ?: "未知成员",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
                
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CoralPink.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(8.dp))

                UserMiniAvatar(toUser)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = toUser?.nickname ?: "未知接单人",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
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
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                        ) {
                            Text("拒绝")
                        }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = { onAction("accept") },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPink)
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
private fun OrderDetailScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val order = state.selectedOrder
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OatmealPage)
            )
        },
        containerColor = OatmealPage
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CoralPink)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    val (statusBg, statusFg) = when (order.status) {
                        0 -> Color(0xFFFFECE0) to Color(0xFFFF8A43)
                        1 -> Color(0xFFE3F2FD) to Color(0xFF1E88E5)
                        2 -> Color(0xFFE8F5E9) to Color(0xFF43A047)
                        3 -> Color(0xFFFFEBEE) to Color(0xFFE53935)
                        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(20.dp))
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
                                    color = TextDark
                                )
                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        statusText(order.status),
                                        color = statusFg,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("订单编号: ${order.orderNo}", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                            Spacer(Modifier.height(4.dp))
                            Text("下单时间: ${order.createdAt ?: ""}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)

                            Spacer(Modifier.height(14.dp))
                            
                            val fromUser = state.recipients.firstOrNull { it.id == order.fromUserId } ?: (if (state.user?.id == order.fromUserId) state.user else null)
                            val toUser = state.recipients.firstOrNull { it.id == order.toUserId } ?: (if (state.user?.id == order.toUserId) state.user else null)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAF6F0).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(fromUser)
                                    Spacer(Modifier.height(4.dp))
                                    Text(fromUser?.nickname ?: "未知成员", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextDark)
                                    Text("下单人", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = CoralPink.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(toUser)
                                    Spacer(Modifier.height(4.dp))
                                    Text(toUser?.nickname ?: "未知接单人", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextDark)
                                    Text("烹饪人", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }

                            if (!order.remark.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF8F5), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFFFEADF)), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "备注: ${order.remark}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFD35400)
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
                        color = TextDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                items(order.items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val model = realImageUrl(item.dishImage)
                                if (!model.isNullOrBlank()) {
                                    AsyncImage(
                                        model = model,
                                        contentDescription = item.dishName,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFFF5F5)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    FallbackFoodAvatar(
                                        dishId = item.dishId,
                                        dishName = item.dishName,
                                        size = 50.dp,
                                        emojiSize = 24.sp,
                                        cornerRadius = 10.dp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = item.dishName ?: "未知菜品",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextDark
                                    )
                                }
                                Text(
                                    text = "x${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CoralPink
                                )
                            }

                            if (order.status == 2) {
                                Spacer(Modifier.height(14.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFDFCFB), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "评价这道菜",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextMuted
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        (1..5).forEach { score ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(BorderStroke(1.dp, Color(0xFFF2E6DB)), CircleShape)
                                                    .clickable { vm.rateDish(order.id, item.dishId, score) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "$score",
                                                    color = CoralPink,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Accept / Reject / Complete Action Buttons directly in OrderDetailScreen
                if (order.toUserId == (state.user?.id ?: -1) && (order.status == 0 || order.status == 1)) {
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
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("拒绝接单")
                                }
                                Button(
                                    onClick = { vm.orderAction(order.id, "accept") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPink),
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverScreen(state: KitchenUiState, vm: KitchenViewModel) {
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshAnnouncements() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = OatmealPage
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                startPullRefresh()
                vm.refreshAnnouncements()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                        Text(
                            text = "✨ 爱的留言板",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "甜蜜公告板",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                if (state.announcements.isNotEmpty()) {
                    items(state.announcements) { ann ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFFFF2F4), Color.White)
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "📢",
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "公告消息",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = CoralDark
                                        )
                                    }
                                    Text(
                                        text = ann.updatedAt?.take(10) ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextMuted
                                    )
                                }
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = ann.content,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }
                    item {
                        CuteBackyardGardenDecor(modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            SweetInteractMascot(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MineScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    var editProfile by remember { mutableStateOf(false) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshStats() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = OatmealPage
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                startPullRefresh()
                vm.refreshStats()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFF7F5), Color.White)
                                )
                            )
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatarUrl = state.user?.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = realImageUrl(avatarUrl),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(2.dp, CoralPink), CircleShape)
                                    .background(Color(0xFFFFECEF)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(2.dp, CoralPink), CircleShape)
                                    .background(Color(0xFFFFECEF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar Placeholder",
                                    tint = CoralPink,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.user?.nickname ?: "美味食客",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = TextDark,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(Modifier.width(6.dp))
                                IconButton(
                                    onClick = { editProfile = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "编辑资料",
                                        tint = CoralPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (state.isAdmin) Color(0xFFFFF0F2) else Color(0xFFF5F0EB),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (state.isAdmin) "👑 厨房大厨" else "🍽️ 首席试吃员",
                                    color = if (state.isAdmin) CoralPink else TextMuted,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF5EFE6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "👩‍❤️‍👨 甜蜜厨房统计",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Stat("✨ 累计点单", state.stats.totalOrders)
                            Stat("📝 本月被投喂", state.stats.monthOrders)
                            Stat("🍳 本月掌勺", state.stats.monthReceived)
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.isAdmin) {
                        ListAction("管理后台", Icons.Default.AdminPanelSettings) { nav.navigate("admin") }
                    }
                    ListAction("退出登录", Icons.Default.Person) { vm.logout() }
                }
            }

            item {
                val transition = rememberInfiniteTransition(label = "mineSway")
                val swayAngle by transition.animateFloat(
                    initialValue = -8f,
                    targetValue = 8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mineSwayAngle"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(60.dp)
                            .graphicsLayer {
                                rotationZ = swayAngle
                            }
                    ) {
                        Text(text = "🐱", fontSize = 24.sp)
                        Text(text = "🌸", fontSize = 16.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🐾 厨房小管家为您服务 🐾",
                            color = TextMuted.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "❤ 今天也是甜蜜蜜的一天呢 ❤",
                            color = CoralPink.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(60.dp)
                            .graphicsLayer {
                                rotationZ = -swayAngle
                            }
                    ) {
                        Text(text = "🐶", fontSize = 24.sp)
                        Text(text = "🌻", fontSize = 16.sp)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        SweetInteractMascot(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        )
    }
}

    if (editProfile) {
        ProfileDialog(
            state = state,
            vm = vm,
            onDismiss = { editProfile = false },
            onSave = { nickname, avatar ->
                editProfile = false
                vm.updateProfile(nickname, avatar)
            }
        )
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF7F8))
            .border(BorderStroke(1.dp, Color(0xFFFFECEF)), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "$value",
            color = CoralPink,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun ListAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF5EFE6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFF0F2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CoralPink,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileDialog(
    state: KitchenUiState,
    vm: KitchenViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nickname by remember { mutableStateOf(state.user?.nickname ?: "") }
    var avatar by remember { mutableStateOf(state.user?.avatarUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            scope.launch(Dispatchers.IO) {
                try {
                    val processed = readAndCompressImage(context, uri)
                    if (processed != null) {
                        withContext(Dispatchers.Main) {
                            vm.uploadFile(
                                bytes = processed.bytes,
                                fileName = "avatar_${System.currentTimeMillis()}.${processed.extension}",
                                onSuccess = { url ->
                                    uploading = false
                                    avatar = url
                                },
                                onFailure = {
                                    uploading = false
                                }
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            uploading = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        uploading = false
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                "修改个人资料",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = CoralDark
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Avatar with edit overlay and picker launcher
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFECEF))
                        .border(BorderStroke(2.dp, CoralPink), CircleShape)
                        .clickable(enabled = !uploading) {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val realUrl = realImageUrl(avatar)
                    if (!realUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = realUrl,
                            contentDescription = "Avatar Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            tint = CoralPink,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Edit Badge overlay at bottom of avatar circle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Loading overlay if uploading
                    if (uploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
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
                    text = if (uploading) "正在上传头像..." else "点击头像直接上传",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uploading) CoralPink else TextMuted
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
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
                onClick = { onSave(nickname, avatar) },
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

private fun realImageUrl(url: String?): String? {
    return BackendConfig.resolveUrl(url)
}

private fun statusText(status: Int): String = when (status) {
    0 -> "待接单"
    1 -> "已接单"
    2 -> "已完成"
    3 -> "已拒绝"
    else -> "未知"
}

@Composable
private fun CustomSnackbar(message: String) {
    val isSuccess = !message.contains("失败") && !message.contains("错误") && !message.contains("不匹配") && !message.contains("无")
    val bgColor = if (isSuccess) Color(0xFFFFF0F2) else Color(0xFFFFF2F2)
    val borderColor = if (isSuccess) CoralPink else Color(0xFFFFD2D2)
    val icon = if (isSuccess) "✨" else "⚠️"
    val textColor = if (isSuccess) CoralDark else Color(0xFFD32F2F)

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UserMiniAvatar(user: User?) {
    val model = realImageUrl(user?.avatarUrl)
    if (!model.isNullOrBlank()) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF0F2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CoralPink,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun FloatingParticlesBackground() {
    val transition = rememberInfiniteTransition(label = "particles")
    val items = remember {
        listOf(
            ParticleData("🍓", 0.15f, 0.2f, 3000, 6000),
            ParticleData("🥑", 0.78f, 0.12f, 3200, 6500),
            ParticleData("🍕", 0.45f, 0.38f, 3400, 5800),
            ParticleData("🍩", 0.85f, 0.62f, 2800, 6200),
            ParticleData("🍰", 0.22f, 0.72f, 3600, 6000),
            ParticleData("🥤", 0.65f, 0.82f, 3100, 6700),
            ParticleData("🍒", 0.08f, 0.48f, 3300, 6900),
            ParticleData("🍭", 0.90f, 0.28f, 3500, 5500)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = this.constraints.maxWidth.toFloat()
        val height = this.constraints.maxHeight.toFloat()

        for (item in items) {
            androidx.compose.runtime.key(item.emoji) {
                val offsetY by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = -90f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )

                val scale by transition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.duration, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                val rotation by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.rotationDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = item.xFraction * width
                            translationY = item.yFraction * height + offsetY
                            scaleX = scale
                            scaleY = scale
                            rotationZ = rotation
                            alpha = 0.12f
                        }
                ) {
                    Text(text = item.emoji, fontSize = 28.sp)
                }
            }
        }
    }
}

private data class ParticleData(
    val emoji: String,
    val xFraction: Float,
    val yFraction: Float,
    val duration: Int,
    val rotationDuration: Int
)

@Composable
private fun MenuShimmerPlaceholder() {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(Color(0xFFF7F3EE)),
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

@Composable
private fun DecorativeEmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val transition = rememberInfiniteTransition(label = "emptyPulse")
    val scale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFFF0F2)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = floatY
                        }
                        .size(90.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF0F2), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFFF5E7E), // CoralPink
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color(0xFF3C3333), // TextDark
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "在温暖的小屋里静候佳音~",
                    color = Color(0xFF8C7E7E).copy(alpha = 0.7f), // TextMuted
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        CuteBackyardGardenDecor(
            modifier = Modifier.padding(top = 12.dp),
            height = 360.dp
        )
    }
}

private val SWEET_QUOTES = listOf(
    "大厨辛苦啦！给宝贝比个大大的心 ❤️",
    "今天想吃你做的可乐鸡翅啦~ 🍽️",
    "每一道菜，都是我爱你的证明呀~ 🥰",
    "被你喂饱的感觉，真的超级超级幸福！✨",
    "宝贝，今天也要按时好好吃饭哦！🍓",
    "叮咚！你的今日份甜度已超标~ 🥤",
    "下一顿饭，换我掌勺来投喂你吧！🍳",
    "不管吃什么，和你在一起就最美味啦 🐾",
    "柴米油盐酱醋茶，每天都有你在身边 ❤"
)

@Composable
private fun CuteKitchenSteamDecor(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "steam")

    val progress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam1"
    )

    val progress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam2"
    )

    val progress3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam3"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Shelf at the bottom
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(6.dp)
                .background(Color(0xFFE6D2C0), RoundedCornerShape(3.dp))
                .align(Alignment.BottomCenter)
        )

        // Peeking bunny behind the pot
        Box(
            modifier = Modifier
                .padding(bottom = 6.dp, end = 50.dp)
                .size(44.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Bunny ears
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .width(8.dp)
                    .height(20.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .padding(start = 26.dp)
                    .width(8.dp)
                    .height(20.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
            )
            // Bunny head
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxSize()
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFF7ECEF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Bunny face (two dots and blush)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(3.dp).background(TextDark, CircleShape))
                    Box(modifier = Modifier.size(3.dp).background(TextDark, CircleShape))
                }
            }
        }

        // Cooking Pot Body
        Box(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .width(76.dp)
                .height(48.dp)
                .background(
                    CoralPink,
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 4.dp, topEnd = 4.dp)
                )
                .align(Alignment.BottomCenter)
        ) {
            // Pot handle left
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-6).dp)
                    .size(12.dp)
                    .border(2.dp, CoralPink, CircleShape)
            )
            // Pot handle right
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 6.dp)
                    .size(12.dp)
                    .border(2.dp, CoralPink, CircleShape)
            )
        }

        // Pot Lid
        Box(
            modifier = Modifier
                .padding(bottom = 54.dp)
                .width(82.dp)
                .height(8.dp)
                .background(PeachSunset, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .align(Alignment.BottomCenter)
        ) {
            // Lid knob
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
                    .width(14.dp)
                    .height(6.dp)
                    .background(PeachSunset, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }

        // Animated steam/emoji particles
        val steamItems = listOf(
            Triple("❤️", progress1, -12),
            Triple("✨", progress2, 16),
            Triple("💨", progress3, 0)
        )

        steamItems.forEach { (emoji, progress, xOffset) ->
            if (progress > 0f && progress < 1f) {
                val yPosVal = -60f * progress - 56f
                val xSwayVal = (kotlin.math.sin(progress.toDouble() * Math.PI * 2.0).toFloat() * 12f) + xOffset
                val alphaVal = if (progress < 0.2f) {
                    progress / 0.2f
                } else if (progress > 0.7f) {
                    (1f - progress) / 0.3f
                } else {
                    1f
                }

                Text(
                    text = emoji,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = yPosVal * density
                            translationX = xSwayVal * density
                            alpha = alphaVal
                            scaleX = 0.8f + progress * 0.4f
                            scaleY = 0.8f + progress * 0.4f
                        }
                )
            }
        }
    }
}

@Composable
private fun SweetInteractMascot(modifier: Modifier = Modifier) {
    var showBubble by remember { mutableStateOf(false) }
    var bubbleText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var clickTrigger by remember { mutableStateOf(0) }
    var isClicked by remember { mutableStateOf(false) }
    var bubbleJob by remember { mutableStateOf<Job?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { if (isClicked) isClicked = false },
        label = "mascotScale"
    )

    // Gentle float idle animation
    val transition = rememberInfiniteTransition(label = "mascotFloat")
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotFloatY"
    )

    val heartProgress = remember { Animatable(0f) }
    LaunchedEffect(clickTrigger) {
        if (clickTrigger > 0) {
            isClicked = true
            heartProgress.snapTo(0f)
            heartProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = FastOutLinearInEasing)
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Speech Bubble
        AnimatedVisibility(
            visible = showBubble,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 10 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 10 }),
            modifier = Modifier
                .padding(bottom = 60.dp, end = 4.dp)
                .align(Alignment.BottomEnd)
        ) {
            Surface(
                color = Color(0xFFFFF2F4),
                border = BorderStroke(1.dp, Color(0xFFFFD6DC)),
                shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                modifier = Modifier
                    .width(180.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
            ) {
                Text(
                    text = bubbleText,
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Particle hearts container
        Box(
            modifier = Modifier
                .padding(bottom = 26.dp, end = 26.dp)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            val progress = heartProgress.value
            if (progress > 0f && progress < 1f) {
                val angles = listOf(-60f, -40f, -20f, 0f, 20f, 40f, 60f)
                val distances = listOf(60f, 80f, 70f, 90f, 75f, 85f, 65f)
                val emojis = listOf("❤️", "💖", "✨", "💕", "❤️", "🌸", "✨")

                angles.forEachIndexed { index, angle ->
                    val distVal = distances[index] * progress
                    val angleRad = Math.toRadians(angle.toDouble())
                    val xVal = distVal * kotlin.math.sin(angleRad).toFloat()
                    val yVal = -distVal * kotlin.math.cos(angleRad).toFloat()
                    val alphaVal = 1f - progress

                    Text(
                        text = emojis[index],
                        fontSize = 14.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = xVal * density
                                translationY = yVal * density
                                alpha = alphaVal
                                scaleX = 1f - progress * 0.4f
                                scaleY = 1f - progress * 0.4f
                            }
                    )
                }
            }
        }

        // Mascot Main Circle
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = floatY
                    scaleX = scale
                    scaleY = scale
                }
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFECEF), Color(0xFFFFD6DC))
                    ),
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .clickable {
                    clickTrigger += 1
                    bubbleText = SWEET_QUOTES.random()
                    showBubble = true
                    bubbleJob?.cancel()
                    bubbleJob = scope.launch {
                        delay(4000)
                        showBubble = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐱",
                fontSize = 26.sp
            )
        }
    }
}

@Composable
private fun DraggableCuteElement(
    key: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("cute_decor_prefs", android.content.Context.MODE_PRIVATE) }
    val keyX = "decor_${key}_x"
    val keyY = "decor_${key}_y"

    var offsetX by remember { mutableStateOf(prefs.getFloat(keyX, 0f)) }
    var offsetY by remember { mutableStateOf(prefs.getFloat(keyY, 0f)) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(key) {
                detectDragGestures(
                    onDragEnd = {
                        prefs.edit().putFloat(keyX, offsetX).putFloat(keyY, offsetY).apply()
                    },
                    onDragCancel = {
                        prefs.edit().putFloat(keyX, offsetX).putFloat(keyY, offsetY).apply()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        content()
    }
}

@Composable
private fun CuteBackyardGardenDecor(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 200.dp
) {
    // Empty per user request
}
