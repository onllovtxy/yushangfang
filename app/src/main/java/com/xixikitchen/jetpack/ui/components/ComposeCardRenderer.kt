package com.xixikitchen.jetpack.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
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
 * 把 Compose 卡片原样渲染成 Bitmap。
 *
 * 之前手绘导出（用 Canvas 重画一遍版式）和屏幕上的真实卡片长得不一样：票根有 5 套模板，
 * 手绘只覆盖其中一种简化版，导出的图就"缺一半"。这里改为复用同一份 Composable 做离屏
 * 渲染，导出结果和屏幕所见一致。
 */
object ComposeCardRenderer {

    private const val MAX_HEIGHT_PX = 12_000

    /**
     * 同步渲染，必须在主线程调用。
     *
     * Compose 的合成依赖测量，这里直接对 ComposeView 做 measure/layout 再 draw。
     * 关键点是先把 lifecycle 抬到 RESUMED：ComposeView 在 onAttachedToWindow 之后才会
     * 启动组合，未 attach 时要手动把 owner 状态设好，否则 draw 出来是空白。
     */
    fun renderToBitmap(
        context: Context,
        widthPx: Int,
        content: @Composable () -> Unit
    ): Bitmap {
        val owner = OffscreenOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent(content)
        }

        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        // 量两次：第一次拿自然高度，第二次按确定高度再量一次，避免 wrap_content 下测量不完整
        composeView.measure(widthSpec, heightSpec)
        val height = composeView.measuredHeight.coerceIn(1, MAX_HEIGHT_PX)
        val exactHeightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        composeView.measure(widthSpec, exactHeightSpec)
        composeView.layout(0, 0, widthPx, height)

        val bitmap = Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888)
        composeView.draw(Canvas(bitmap))
        return bitmap
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
