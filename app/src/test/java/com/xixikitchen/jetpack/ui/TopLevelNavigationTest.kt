package com.xixikitchen.jetpack.ui

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.xixikitchen.jetpack.MainActivity
import com.xixikitchen.jetpack.ui.screens.DiscoverSubTab
import com.xixikitchen.jetpack.widget.AnniversaryWidgetProvider
import com.xixikitchen.jetpack.widget.DiaryWidgetProvider
import com.xixikitchen.jetpack.widget.WidgetConfigActivity
import com.xixikitchen.jetpack.widget.WidgetIntents
import com.xixikitchen.jetpack.widget.WidgetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class TopLevelNavigationTest {
    private lateinit var nav: TestNavHostController
    private lateinit var composeNavigator: ComposeNavigator
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        nav = TestNavHostController(ApplicationProvider.getApplicationContext())
        viewModelStore = ViewModelStore()
        nav.setViewModelStore(viewModelStore)
        composeNavigator = ComposeNavigator()
        nav.navigatorProvider.addNavigator(composeNavigator)
        nav.graph = nav.createGraph(
            startDestination = "kitchen",
            route = MAIN_GRAPH_ROUTE
        ) {
            composable("kitchen") { }
            composable("orders") { }
            composable("discover") { }
            composable("mine") { }
            composable("settings") { }
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
    }

    @Test
    fun rootPolicyClearsToTheStableGraphAndPreservesSavedState() {
        val policy = topLevelNavigationPolicy()

        assertEquals("main_graph", policy.popUpToRoute)
        assertEquals(false, policy.inclusive)
        assertEquals(true, policy.saveState)
        assertEquals(true, policy.launchSingleTop)
        assertEquals(true, policy.restoreState)
    }

    @Test
    fun selectingCurrentRootDoesNotNavigate() {
        assertFalse(shouldNavigateToTopLevel(currentRoute = "orders", targetRoute = "orders"))
    }

    @Test
    fun selectingDifferentRootNavigates() {
        assertEquals(true, shouldNavigateToTopLevel(currentRoute = "orders", targetRoute = "mine"))
    }

    @Test
    fun switchingRootsLeavesOneActiveComposeEntryAndRestoresTheRoute() {
        nav.navigateToTopLevel("orders")
        assertSingleActiveRoot()

        nav.currentBackStackEntry?.savedStateHandle?.set("marker", "orders-state")
        nav.navigateToTopLevel("mine")
        assertSingleActiveRoot()

        nav.navigateToTopLevel("orders")
        assertSingleActiveRoot()
        assertEquals("orders", nav.currentDestination?.route)
        assertEquals("orders-state", nav.currentBackStackEntry?.savedStateHandle?.get<String>("marker"))
    }

    @Test
    fun navigatingFromNestedRouteToDiscoverLandsOnDiscover() {
        nav.navigateToTopLevel("mine")
        nav.navigate("settings")
        assertEquals("settings", nav.currentDestination?.route)

        nav.navigateToTopLevel("discover")
        assertEquals("discover", nav.currentDestination?.route)
        assertSingleActiveRoot()
    }

    @Test
    fun widgetIntentsCarryMatchingDiscoverSubTab() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val annTarget = MainActivity.extractWidgetTarget(
            WidgetIntents.createOpenIntent(context, WidgetType.Anniversary)
        )
        assertNotNull(annTarget)
        assertEquals("discover", annTarget!!.route)
        assertEquals(DiscoverSubTab.Anniversaries, DiscoverSubTab.fromKey(annTarget.subTab))

        val diaryTarget = MainActivity.extractWidgetTarget(
            WidgetIntents.createOpenIntent(context, WidgetType.Diary)
        )
        assertNotNull(diaryTarget)
        assertEquals("discover", diaryTarget!!.route)
        assertEquals(DiscoverSubTab.Diary, DiscoverSubTab.fromKey(diaryTarget.subTab))
    }

    @Test
    fun widgetIntentFallsBackToDataUriWhenExtrasMissing() {
        val annIntent = Intent(WidgetIntents.ACTION_OPEN).apply {
            data = Uri.parse("xixikitchen://widget/anniversary")
        }
        val annTarget = MainActivity.extractWidgetTarget(annIntent)
        assertEquals("discover", annTarget?.route)
        assertEquals("anniversaries", annTarget?.subTab)

        val diaryIntent = Intent(WidgetIntents.ACTION_OPEN).apply {
            data = Uri.parse("xixikitchen://widget/diary")
        }
        val diaryTarget = MainActivity.extractWidgetTarget(diaryIntent)
        assertEquals("discover", diaryTarget?.route)
        assertEquals("diary", diaryTarget?.subTab)
    }

    @Test
    fun widgetConfigResolvesProviderTypeByProviderClassName() {
        assertEquals(
            WidgetType.Anniversary,
            WidgetConfigActivity.widgetTypeOf(AnniversaryWidgetProvider::class.java.name)
        )
        assertEquals(
            WidgetType.Diary,
            WidgetConfigActivity.widgetTypeOf(DiaryWidgetProvider::class.java.name)
        )
        assertNull(WidgetConfigActivity.widgetTypeOf("com.example.UnrelatedReceiver"))
    }

    private fun assertSingleActiveRoot() {
        assertEquals(
            1,
            composeNavigator.backStack.value.count { entry ->
                entry.destination.route in setOf("kitchen", "orders", "discover", "mine")
            }
        )
    }

}
