package com.xixikitchen.jetpack.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * 把 Compose 卡片渲染成 Bitmap。
 *
 * 之前手绘导出（用 Canvas 重画一遍版式）和屏幕上的真实卡片长得不一样——票根有 5 套模板，
 * 手绘只覆盖其中一种简化版，导出的图就"缺一半"。
 *
 * 这里复用同一份 Composable：把 ComposeView 挂进 Activity 的 contentView 里（不可见、不占位），
 * 量好、画完立刻移除。直接 new 一个没 attach 的 ComposeView 会崩，所以必须真挂上去。
 */
object ComposeCardRenderer {

    private const val MAX_HEIGHT_PX = 12_000

    /** 同步渲染，必须在主线程调用。 */
    fun renderToBitmap(
        context: Context,
        widthPx: Int,
        content: @Composable () -> Unit
    ): Bitmap? {
        val activity = context.findActivity() ?: return null
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return null

        val owner = OffscreenOwner()
        val composeView = ComposeView(activity).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent(content)
        }

        // 用一个 0 尺寸容器承载，既不遮挡界面，也不影响布局
        val container = FrameLayout(activity).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            addView(
                composeView,
                FrameLayout.LayoutParams(widthPx, FrameLayout.LayoutParams.WRAP_CONTENT)
            )
        }
        val params = ViewGroup.LayoutParams(0, 0).also { it.width = 0; it.height = 0 }

        root.addView(container, FrameLayout.LayoutParams(0, 0))
        try {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            container.measure(widthSpec, heightSpec)

            val measured = composeView.measuredHeight.takeIf { it > 0 } ?: container.measuredHeight
            val height = measured.coerceIn(1, MAX_HEIGHT_PX)
            val exactHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, exactHeight)
            composeView.measure(widthSpec, exactHeight)
            container.layout(0, 0, widthPx, height)
            composeView.layout(0, 0, widthPx, height)

            val bitmap = Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888)
            composeView.draw(Canvas(bitmap))
            return bitmap
        } catch (e: Exception) {
            return null
        } finally {
            runCatching { root.removeView(container) }
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            val base = ctx.baseContext
            if (base === ctx) break
            ctx = base
        }
        return ctx as? Activity
    }

    private class OffscreenOwner :
        LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

        private val registry = LifecycleRegistry(this)
        private val savedStateController = SavedStateRegistryController.create(this)

        override val viewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

        init {
            savedStateController.performRestore(null)
            registry.currentState = Lifecycle.State.RESUMED
        }
    }
}
