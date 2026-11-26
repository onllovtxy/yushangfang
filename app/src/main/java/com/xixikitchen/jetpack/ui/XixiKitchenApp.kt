package com.xixikitchen.jetpack.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xixikitchen.jetpack.ui.components.CustomSnackbar
import com.xixikitchen.jetpack.ui.components.ErrorGlassDialog
import com.xixikitchen.jetpack.ui.designsystem.motion.AppDuration
import com.xixikitchen.jetpack.ui.designsystem.motion.AppEase
import com.xixikitchen.jetpack.ui.designsystem.motion.SpringSettle
import com.xixikitchen.jetpack.ui.designsystem.theme.AppTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassMeshBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.ThemeStore
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.resolveTheme
import com.xixikitchen.jetpack.ui.screens.DiscoverScreen
import com.xixikitchen.jetpack.ui.screens.KitchenScreen
import com.xixikitchen.jetpack.ui.screens.LoginScreen
import com.xixikitchen.jetpack.ui.screens.MineScreen
import com.xixikitchen.jetpack.ui.screens.OrderDetailScreen
import com.xixikitchen.jetpack.ui.screens.OrdersScreen
import kotlinx.coroutines.launch

@Composable
fun XixiKitchenApp(
    vm: KitchenViewModel,
    discoverVm: DiscoverViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val discoverState by discoverVm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val themeStore = remember(context) { ThemeStore(context) }
    var themeId by remember { mutableStateOf(themeStore.loadSelection()) }
    var customThemes by remember { mutableStateOf(themeStore.loadCustoms()) }
    val resolvedTheme = remember(themeId, customThemes) { resolveTheme(themeId, customThemes) }

    LaunchedEffect(state.message) {
        state.message?.let {
            vm.consumeMessage()
            snack.currentSnackbarData?.dismiss()
            scope.launch { snack.showSnackbar(it) }
        }
    }

    LaunchedEffect(discoverState.message) {
        discoverState.message?.let {
            discoverVm.consumeMessage()
            snack.currentSnackbarData?.dismiss()
            scope.launch { snack.showSnackbar(it) }
        }
    }

    val errorMsg = state.errorDialogMessage ?: discoverState.errorDialogMessage
    if (errorMsg != null) {
        ErrorGlassDialog(
            message = errorMsg,
            onDismiss = {
                if (state.errorDialogMessage != null) vm.consumeErrorDialog()
                if (discoverState.errorDialogMessage != null) discoverVm.consumeErrorDialog()
            }
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
                                TopLevelDestination(
                                    nav,
                                    "discover"
                                ) {
                                    DiscoverScreen(
                                        state = discoverState,
                                        vm = discoverVm,
                                        isSuperAdmin = state.isSuperAdmin,
                                        currentSpace = state.coupleSpace,
                                        allSpaces = state.allCoupleSpaces,
                                        onSelectSpace = vm::selectCoupleSpace
                                    )
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
    backdropBlur: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (backdropBlur > 0.dp) Modifier.blur(radius = backdropBlur) else Modifier)
    ) {
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
