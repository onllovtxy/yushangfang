package com.xixikitchen.jetpack.ui

import androidx.lifecycle.ViewModelStore
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun nestedTransitionPolicyMatchesBothSupportedRoutePairs() {
        assertEquals(true, isNestedForwardTransition("orders", "orderDetail/{id}"))
        assertEquals(true, isNestedForwardTransition("mine", "admin"))
        assertEquals(false, isNestedForwardTransition("orders", "mine"))
        assertEquals(false, isNestedForwardTransition("orderDetail/{id}", "orders"))
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

    private fun assertSingleActiveRoot() {
        assertEquals(
            1,
            composeNavigator.backStack.value.count { entry ->
                entry.destination.route in setOf("kitchen", "orders", "discover", "mine")
            }
        )
    }

}
