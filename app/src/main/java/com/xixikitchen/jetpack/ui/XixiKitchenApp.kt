package com.xixikitchen.jetpack.ui

import androidx.compose.foundation.BorderStroke
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassMeshBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.AppTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.ThemeStore
import com.xixikitchen.jetpack.ui.designsystem.theme.resolveTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.orderStatusColors
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.motion.AppDuration
import com.xixikitchen.jetpack.ui.designsystem.motion.AppEase
import com.xixikitchen.jetpack.ui.designsystem.motion.LocalReducedMotion
import com.xixikitchen.jetpack.ui.designsystem.motion.SpringSettle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.xixikitchen.jetpack.data.Dish
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import androidx.compose.runtime.snapshotFlow

// Redesigned Kitchen Theme Palette

@Composable
fun XixiKitchenApp(vm: KitchenViewModel) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 主题状态放在 GlassTheme 之外 —— 它就是 GlassTheme 的入参。
    // ThemeStore 同步读,所以第一帧就是用户选的那套,不会闪一下默认配色。
    val context = LocalContext.current
    val themeStore = remember(context) { ThemeStore(context) }
    var themeId by remember { mutableStateOf(themeStore.loadSelection()) }
    var customThemes by remember { mutableStateOf(themeStore.loadCustoms()) }
    val resolvedTheme = remember(themeId, customThemes) { resolveTheme(themeId, customThemes) }

    // showSnackbar 会一直挂起到这条消息结束。用 scope.launch 才能让它活过
    // consumeMessage() 引起的 key 变化;但必须先顶掉上一条,否则连点几下之后
    // 消息会排成长队一条条慢慢播完。
    LaunchedEffect(state.message) {
        state.message?.let {
            vm.consumeMessage()
            snack.currentSnackbarData?.dismiss()
            scope.launch { snack.showSnackbar(it) }
        }
    }

    val errorMsg = state.errorDialogMessage
    if (errorMsg != null) {
        ErrorGlassDialog(
            message = errorMsg,
            onDismiss = vm::consumeErrorDialog
        )
    }

    GlassTheme(tokens = resolvedTheme.tokens) {
        Box(Modifier.fillMaxSize()) {
            if (!state.isLoggedIn) {
                LoginScreen(
                    loading = state.loading,
                    onLogin = vm::login,
                    onRegister = vm::register,
                    onResetPassword = vm::resetPassword
                )
            } else {
                val nav = rememberNavController()
                val backStack by nav.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route ?: "kitchen"
                val showBottomBar = currentRoute in listOf("kitchen", "orders", "discover", "mine")
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snack,
                            modifier = Modifier.padding(bottom = if (showBottomBar) 96.dp else 0.dp)
                        ) { data ->
                            CustomSnackbar(data.visuals.message)
                        }
                    },
                    containerColor = Color.Transparent
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        NavHost(
                            navController = nav,
                            startDestination = "kitchen",
                            route = MAIN_GRAPH_ROUTE,
                            // 导航切换不播放任何转场动画（应要求）
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
                            composable("kitchen") {
                                TopLevelDestination(nav, "kitchen") {
                                    KitchenScreen(state, vm)
                                }
                            }
                            composable("orders") {
                                TopLevelDestination(
                                    nav,
                                    "orders",
                                    backdropBlur = if (currentRoute == "orderDetail/{id}") 28.dp else 0.dp
                                ) {
                                    OrdersScreen(state, vm, nav)
                                }
                            }
                            composable("discover") {
                                TopLevelDestination(nav, "discover") {
                                    DiscoverScreen(state, vm)
                                }
                            }
                            composable("mine") {
                                TopLevelDestination(
                                    nav,
                                    "mine",
                                    backdropBlur = if (currentRoute == "admin") 28.dp else 0.dp
                                ) {
                                    MineScreen(state, vm, nav)
                                }
                            }
                            composable("admin") {
                                NestedPageSurface {
                                    AdminScreen(state, vm, nav)
                                }
                            }
                            composable("settings") {
                                NestedPageSurface {
                                    SettingsScreen(
                                        current = resolvedTheme,
                                        onOpenThemePicker = { nav.navigate("settings/theme") },
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable("settings/theme") {
                                NestedPageSurface {
                                    ThemePickerScreen(
                                        currentId = resolvedTheme.id,
                                        customs = customThemes,
                                        onSelectTheme = { picked ->
                                            themeId = picked
                                            themeStore.saveSelection(picked)
                                        },
                                        onSaveCustom = { saved ->
                                            customThemes = customThemes.filterNot { it.id == saved.id } + saved
                                            themeStore.saveCustoms(customThemes)
                                        },
                                        onDeleteCustom = { removedId ->
                                            customThemes = customThemes.filterNot { it.id == removedId }
                                            themeStore.saveCustoms(customThemes)
                                            // 删掉的正好是当前正在用的那套,回落到默认,
                                            // 否则已选 id 会变成悬空值
                                            if (themeId == removedId) {
                                                themeId = AppTheme.DEFAULT.id
                                                themeStore.saveSelection(themeId)
                                            }
                                        },
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable("orderDetail/{id}") { entry ->
                                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                                LaunchedEffect(id) { vm.loadOrderDetail(id) }
                                NestedPageSurface {
                                    OrderDetailScreen(state, vm, nav)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal const val MAIN_GRAPH_ROUTE = "main_graph"

@Composable
private fun TopLevelDestination(
    nav: NavHostController,
    route: String,
    backdropBlur: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (backdropBlur > 0.dp) Modifier.blur(radius = backdropBlur) else Modifier)
    ) {
        GlassMeshBackground(tokens)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { MainBottomBar(nav, route) },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}

/**
 * 二级页面(订单详情/管理页)的整屏背景:半透明磨砂 mesh,
 * 叠加在一级页的高斯模糊之上,形成磨砂玻璃效果,不会镂空。
 */
@Composable
private fun NestedPageSurface(content: @Composable () -> Unit) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x1AFFFFFF))
    ) {
        GlassMeshBackground(tokens, Modifier.alpha(0.62f))
        content()
    }
}

internal data class TopLevelNavigationPolicy(
    val popUpToRoute: String,
    val inclusive: Boolean,
    val saveState: Boolean,
    val launchSingleTop: Boolean,
    val restoreState: Boolean
)

internal fun topLevelNavigationPolicy(): TopLevelNavigationPolicy =
    TopLevelNavigationPolicy(
        popUpToRoute = MAIN_GRAPH_ROUTE,
        inclusive = false,
        saveState = true,
        launchSingleTop = true,
        restoreState = true
    )

internal fun shouldNavigateToTopLevel(currentRoute: String?, targetRoute: String): Boolean =
    currentRoute != targetRoute

internal fun NavHostController.navigateToTopLevel(route: String) {
    if (!shouldNavigateToTopLevel(currentDestination?.route, route)) return

    val policy = topLevelNavigationPolicy()
    navigate(route) {
        popUpTo(policy.popUpToRoute) {
            inclusive = policy.inclusive
            saveState = policy.saveState
        }
        launchSingleTop = policy.launchSingleTop
        restoreState = policy.restoreState
    }
}

@Composable
private fun MainBottomBar(
    nav: NavHostController,
    selectedRoute: String
) {
    val tokens = LocalGlassTokens.current
    val items = listOf(
        Triple("kitchen", "点单", Icons.Default.RestaurantMenu),
        Triple("orders", "订单", Icons.AutoMirrored.Filled.ReceiptLong),
        Triple("discover", "发现", Icons.Default.Explore),
        Triple("mine", "我的", Icons.Default.AccountCircle)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // edge-to-edge 下 Scaffold 的 contentWindowInsets 被清零了,
            // 手势导航条不会自己让位 —— 悬浮胶囊会被压在系统条底下。
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassConvex(24.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            items.forEach { (route, label, icon) ->
                val active = selectedRoute == route
                // tintColor / textColor 原本是两个一模一样的 animateColorAsState,
                // 合并成一个,省掉一次多余的动画驱动。
                val contentColor by animateColorAsState(
                    targetValue = if (active) GlassAccent.primary else tokens.textSecondary,
                    animationSpec = tween(AppDuration.quick, easing = AppEase.standard),
                    label = "navItemColor"
                )
                val activeBgColor by animateColorAsState(
                    targetValue = if (active) GlassAccent.washStrong else Color.Transparent,
                    animationSpec = tween(AppDuration.quick, easing = AppEase.standard),
                    label = "activeBgColor"
                )
                // 切 tab 是被动状态变化,不是手势甩出来的 —— 不该过冲。
                // 原来 MediumBouncy + StiffnessLow 会先冲过 1.15 再弹回来,显得松垮。
                val iconScale by animateFloatAsState(
                    targetValue = if (active) 1.08f else 1.0f,
                    animationSpec = SpringSettle,
                    label = "iconScale"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .noRipplePressable(pressedScale = 0.94f) { nav.navigateToTopLevel(route) },
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
                            tint = contentColor,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        label,
                        color = contentColor,
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
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onResetPassword: (String, String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticlesBackground()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .glassConvex(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcave(20.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("账号", color = tokens.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = GlassAccent.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassAccent.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = GlassAccent.primary,
                            cursorColor = GlassAccent.primary
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcave(20.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    GlassPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = GlassAccent.primary) },
                        shape = RoundedCornerShape(20.dp),
                        defaultColors = true
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showRegisterDialog = true },
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("注册", color = GlassAccent.primary, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = { showResetDialog = true },
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("忘记密码", color = GlassAccent.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        // 顺序很重要:graphicsLayer 必须排在 glassConvex 之前,
                        // 这样缩放包住的是整块玻璃面。反过来写就变成"玻璃不动、字在缩"。
                        // glassConvex 内部已经 clip 过,不需要再 clip 一次。
                        .noRipplePressable(enabled = !loading, pressedScale = 0.975f) {
                            onLogin(username.trim(), password)
                        }
                        .glassConvex(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = GlassAccent.primary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            text = if (loading) "登录中..." else "登 录",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
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

    if (showRegisterDialog) {
        RegisterDialog(
            initialUsername = username,
            loading = loading,
            onDismiss = { showRegisterDialog = false },
            onConfirm = { registerUsername, registerPassword ->
                onRegister(registerUsername, registerPassword)
                showRegisterDialog = false
            }
        )
    }
}

/** 睁眼图标（密文可见时显示），Lucide "eye"，描边风格 */
private val EyeOpenIcon: ImageVector = ImageVector.Builder(
    name = "EyeOpen",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0
        moveTo(2.062f, 12.348f)
        arcToRelative(1f, 1f, 0f, false, true, 0f, -0.696f)
        arcToRelative(10.75f, 10.75f, 0f, false, true, 19.876f, 0f)
        arcToRelative(1f, 1f, 0f, false, true, 0f, 0.696f)
        arcToRelative(10.75f, 10.75f, 0f, false, true, -19.876f, 0f)
        close()
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // <circle cx="12" cy="12" r="3"/>
        moveTo(12f, 9f)
        arcToRelative(3f, 3f, 0f, false, true, 0f, 6f)
        arcToRelative(3f, 3f, 0f, false, true, 0f, -6f)
        close()
    }
}.build()

/** 闭眼图标（密文隐藏时显示），Lucide "eye-closed"，睫毛下垂描边风格 */
private val EyeClosedIcon: ImageVector = ImageVector.Builder(
    name = "EyeClosed",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // M2 8a10.645 10.645 0 0 0 20 0
        moveTo(2f, 8f)
        arcToRelative(10.645f, 10.645f, 0f, false, false, 20f, 0f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // m15 18-.722-3.25
        moveTo(15f, 18f)
        lineToRelative(-0.722f, -3.25f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // m20 15-1.726-2.05
        moveTo(20f, 15f)
        lineToRelative(-1.726f, -2.05f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // m4 15 1.726-2.05
        moveTo(4f, 15f)
        lineToRelative(1.726f, -2.05f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // m9 18 .722-3.25
        moveTo(9f, 18f)
        lineToRelative(0.722f, -3.25f)
    }
}.build()

@Composable
private fun GlassPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    defaultColors: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    // 看得见(明文)显示睁眼, 看不见(密文)显示睫毛闭眼
                    imageVector = if (visible) EyeOpenIcon else EyeClosedIcon,
                    contentDescription = if (visible) "隐藏密码" else "显示密码",
                    tint = GlassAccent.primary
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        shape = shape,
        colors = if (defaultColors) {
            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlassAccent.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = GlassAccent.primary,
                cursorColor = GlassAccent.primary
            )
        } else {
            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlassAccent.primary,
                focusedLabelColor = GlassAccent.primary,
                cursorColor = GlassAccent.primary
            )
        }
    )
}

@Composable
private fun ErrorGlassDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        // 少了这两行,AlertDialog 会用默认的不透明 surface 色盖在玻璃层上面,
        // 圆角也会是默认的 28dp —— 跟 glassConvexOverlay(24dp) 对不齐,
        // 玻璃效果整个白做。
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        text = {
            Text(
                message,
                color = tokens.textPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("知道了", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun RegisterDialog(
    initialUsername: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = { Text("注册账号", fontWeight = FontWeight.Bold, color = tokens.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary,
                        cursorColor = GlassAccent.primary
                    )
                )
                GlassPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "密码（至少6位）"
                )
                GlassPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "确认密码",
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it, color = GlassAccent.error) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = username.trim()
                    if (u.length < 2) {
                        passwordError = "用户名至少2个字符"
                        return@Button
                    }
                    if (password.length < 6) {
                        passwordError = "密码至少6位"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        passwordError = "两次输入的密码不一致"
                        return@Button
                    }
                    passwordError = null
                    onConfirm(u, password)
                },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("注册", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("取消", color = tokens.textSecondary)
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
    val tokens = LocalGlassTokens.current
    var resetKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialUsername) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
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
                GlassPasswordField(
                    value = resetKey,
                    onValueChange = { resetKey = it },
                    label = "重置密钥"
                )
                GlassPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "新密码"
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
                Text("确认重置", color = GlassAccent.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitchenScreen(state: KitchenUiState, vm: KitchenViewModel) {
    val tokens = LocalGlassTokens.current
    val reducedMotion = LocalReducedMotion.current
    var showCart by remember { mutableStateOf(false) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    var showOrder by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.refreshHome() }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            // 购物车条:
            //  · 进场沿底部向上"长出来",退场沿原路缩回去 —— 出入同一条路径。
            //  · 退场比进场快(140 vs 220ms)。用户做决定时可以慢,系统响应时永远要快。
            //  · 加一层 0.96 → 1 的缩放,让玻璃面像是真的"材质化"了,而不只是淡入。
            //  · 关掉动画时只留一次短交叉淡化:去掉位移和缩放,保留能帮助理解的变化。
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(Modifier.height(8.dp))

                // 输入框的文本状态必须是**稳定**的,不能写成 remember(state.searchKeyword)。
                // 下面 LaunchedEffect(Unit) 里的 snapshotFlow 只在第一次组合时建立,
                // 它捕获的是当时那个 MutableState 实例。而 remember 的 key 一变就会换成
                // 新实例 —— 写入进了新对象,flow 还在盯旧的,于是搜完第一次就彻底失灵。
                var searchInput by remember { mutableStateOf(state.searchKeyword) }
                val searchFocus = remember { FocusRequester() }

                @OptIn(FlowPreview::class)
                LaunchedEffect(Unit) {
                    snapshotFlow { searchInput }
                        .drop(1)                  // 跳过初始值,进页面别先搜一次
                        .debounce(300)            // 等打字停下来再发请求
                        .distinctUntilChanged()   // 删掉又打回同一个词,不重复请求
                        .collect { vm.search(it) }
                }

                // 切分类时后端会把搜索清空,输入框也得跟着清 ——
                // 否则会出现"显示着分类的菜、搜索框里却还留着上一次的字"的错位。
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
                        // 不放 placeholder:空着就空着
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
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
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
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                            item {
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
                }
                }
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
                // 原来是"点击之后补一个 80ms 的缩放脉冲":反馈发生在手指抬起之后,
                // 而且 0.82 + MediumBouncy/LowStiffness 又大又软,像橡皮。
                // 现在缩放直接绑在真实按压状态上 —— pointer-down 立刻响应,松手立刻回位。
                // 触摸区从 38dp 放大到 44dp:视觉不变,手指不必瞄得那么准。
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
private fun CartDialog(cart: List<CartLine>, onDismiss: () -> Unit, onChange: (Long, Int) -> Unit, onClear: () -> Unit) {
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
private fun SubmitOrderDialog(state: KitchenUiState, onDismiss: () -> Unit, onSubmit: (String, Long?) -> Unit) {
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
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
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val tokens = LocalGlassTokens.current
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
                    items(state.orders) { order ->
                        OrderCard(
                            order = order,
                            state = state,
                            onOpen = { nav.navigate("orderDetail/${order.id}") },
                            onAction = { vm.orderAction(order.id, it) }
                        )
                    }
                    if (state.orders.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
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
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .noRipplePressable(pressedScale = 0.96f, onClick = onClick)
            .then(
                if (active) Modifier.glassConvex(16.dp)
                else Modifier.glassConcave(16.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) GlassAccent.primary else tokens.textSecondary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun OrderCard(order: Order, state: KitchenUiState, onOpen: () -> Unit, onAction: (String) -> Unit) {
    val tokens = LocalGlassTokens.current
    val statusColors = orderStatusColors(order.status)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 整张卡片都是热区,所以缩放要更克制 —— 0.97 放在全宽卡片上会像在弹跳。
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
                text = "${order.createdAt ?: ""}",
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
                            val imageUrl = realImageUrl(item.dishImage)
                            if (!imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = item.dishName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Transparent),
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

            val fromUser = state.recipients.firstOrNull { it.id == order.fromUserId } ?: (if (state.user?.id == order.fromUserId) state.user else null)
            val toUser = state.recipients.firstOrNull { it.id == order.toUserId } ?: (if (state.user?.id == order.toUserId) state.user else null)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassAccent.wash, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserMiniAvatar(fromUser)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = fromUser?.nickname ?: "未知成员",
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

                UserMiniAvatar(toUser)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = toUser?.nickname ?: "未知接单人",
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
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary)
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
private fun OrderDetailScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val tokens = LocalGlassTokens.current
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
            // 详情还在拉的这段时间不转圈,就让它空着(顶栏的返回键一直在)。
            Box(Modifier.fillMaxSize().padding(padding))
        } else {
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
                            // 订单列表用玻璃面,详情页却用 Material Card + 2dp 阴影 + 米色描边 ——
                            // 同一件事物长了两副面孔。统一成玻璃。
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
                            Text("下单时间: ${order.createdAt ?: ""}", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)

                            Spacer(Modifier.height(14.dp))
                            
                            val fromUser = state.recipients.firstOrNull { it.id == order.fromUserId } ?: (if (state.user?.id == order.fromUserId) state.user else null)
                            val toUser = state.recipients.firstOrNull { it.id == order.toUserId } ?: (if (state.user?.id == order.toUserId) state.user else null)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GlassAccent.wash, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(fromUser)
                                    Spacer(Modifier.height(4.dp))
                                    Text(fromUser?.nickname ?: "未知成员", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                                    Text("下单人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GlassAccent.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    UserMiniAvatar(toUser)
                                    Spacer(Modifier.height(4.dp))
                                    Text(toUser?.nickname ?: "未知接单人", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
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
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary),
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverScreen(state: KitchenUiState, vm: KitchenViewModel) {
    val tokens = LocalGlassTokens.current
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshAnnouncements() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
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
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)) {
                        Text(
                            text = "公告栏",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.textPrimary
                        )
                    }
                }
                if (state.announcements.isNotEmpty()) {
                    items(state.announcements) { ann ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassConvex(20.dp)
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .glassConcave(12.dp)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "公告",
                                            color = GlassAccent.primary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        text = ann.updatedAt?.take(10) ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.textSecondary
                                    )
                                }
                                Text(
                                    text = ann.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = tokens.textPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MineScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val tokens = LocalGlassTokens.current
    var editProfile by remember { mutableStateOf(false) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)
    LaunchedEffect(Unit) { vm.refreshStats() }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // 编辑入口只有长按,不放任何视觉提示。
                            .noRippleCombinedPressable(
                                pressedScale = 0.985f,
                                onLongClick = { editProfile = true },
                                onClick = {}
                            )
                            .glassConvex(24.dp)
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .glassConcave(32.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .glassConcave(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar Placeholder",
                                        tint = GlassAccent.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = state.user?.nickname ?: "美味食客",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = tokens.textPrimary
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (state.isAdmin) {
                            ListAction("管理后台", Icons.Default.AdminPanelSettings) { nav.navigate("admin") }
                        }
                        ListAction("设置", Icons.Default.Settings) { nav.navigate("settings") }
                        ListAction("退出登录", Icons.Default.Person) { vm.logout() }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
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
}

@Composable
fun ListAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String? = null,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.985f, onClick = onClick)
            .glassConvex(20.dp)
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
                    .background(GlassAccent.wash, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlassAccent.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = tokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tokens.textSecondary,
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
    val tokens = LocalGlassTokens.current
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
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                "修改个人资料",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 圆形头像 + 选择器。
                // 编辑遮罩改成"只在按下的那一刻出现":之前是一层常驻的 15% 黑罩 + 铅笔,
                // 一直盖在用户自己的照片上,看起来像这个头像被禁用了。
                val avatarInteraction = remember { MutableInteractionSource() }
                val avatarPressed by avatarInteraction.collectIsPressedAsState()
                val editScrimAlpha by animateFloatAsState(
                    targetValue = if (avatarPressed && !uploading) 1f else 0f,
                    animationSpec = SpringSettle,
                    label = "avatarScrim"
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .pressScale(avatarInteraction, enabled = !uploading, pressedScale = 0.96f)
                        .clip(CircleShape)
                        .background(GlassAccent.wash)
                        .border(BorderStroke(2.dp, GlassAccent.stroke), CircleShape)
                        .clickable(
                            interactionSource = avatarInteraction,
                            indication = null,
                            enabled = !uploading
                        ) {
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
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // 按下时才浮现的"更换图片"提示
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = editScrimAlpha }
                            .background(Color.Black.copy(alpha = 0.28f)),
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
                                color = GlassAccent.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (uploading) "正在上传头像..." else "点击头像直接上传",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uploading) GlassAccent.primary else tokens.textSecondary
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
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
                onClick = { onSave(nickname, avatar) },
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

private fun realImageUrl(url: String?): String? {
    return BackendConfig.resolveUrl(url)
}

/**
 * 菜品缩略图。
 *
 * 之前只在 url 为空时才回退到 emoji 头像 —— 图片 404 或断网时会留一块空白,
 * 用户看到的就是"界面坏了"。现在加载失败也回退。
 * 顺带把三个地方重复的 if/else 收成一处。
 */
@Composable
private fun DishThumbnail(
    dishId: Long,
    dishName: String?,
    imageUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    emojiSize: androidx.compose.ui.unit.TextUnit,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
    val model = realImageUrl(imageUrl)
    var failed by remember(model) { mutableStateOf(false) }
    if (model.isNullOrBlank() || failed) {
        FallbackFoodAvatar(
            dishId = dishId,
            dishName = dishName,
            size = size,
            emojiSize = emojiSize,
            cornerRadius = cornerRadius
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = dishName,
            onError = { failed = true },
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(GlassAccent.wash),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
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
    // 原来这里算了 bgColor / borderColor 两个变量,但一个都没用上
    // (背景其实是 glassConvex 画的)。删掉,文字色统一走 token。
    val icon = if (isSuccess) "✨" else "⚠️"
    val textColor = if (isSuccess) GlassAccent.primaryDark else GlassAccent.errorDeep

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .glassConvex(18.dp)
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
                .background(GlassAccent.wash),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = GlassAccent.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun FloatingParticlesBackground() {
    // 装饰性的无限循环动画必须尊重系统的"移除动画"设置。
    // Compose 的 animate*AsState / Animatable 会自动跟随,但 rememberInfiniteTransition
    // 走的是 withFrameNanos,完全不看这个开关 —— 只能在这里手动关掉。
    if (LocalReducedMotion.current) return

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

                // 原来每个水果都在缓缓自转一整圈(5.5~6.9 秒一轮 ≈ 0.15Hz)。
                // 接近 0.2Hz 的缓慢循环运动正是最容易引起前庭不适的那一类,
                // 视觉上读起来也像"转盘"。改成 ±9° 的轻微摇摆 —— 是"飘",不是"转"。
                val sway by transition.animateFloat(
                    initialValue = -9f,
                    targetValue = 9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.rotationDuration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sway"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = item.xFraction * width
                            translationY = item.yFraction * height + offsetY
                            scaleX = scale
                            scaleY = scale
                            rotationZ = sway
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
