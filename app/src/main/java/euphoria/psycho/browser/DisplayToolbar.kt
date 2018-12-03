package euphoria.psycho.browser

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.app_layout.view.*

class DisplayToolbar(context: Context, attributeSet: AttributeSet) : AppBarLayout(context, attributeSet),
    AppBarLayout.OnOffsetChangedListener {
    private val collapsedProgressTranslationY: Float by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        )
    }

    init {
        addOnOffsetChangedListener(this)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange
        val isCollapsed = Math.abs(verticalOffset) == totalScrollRange


        progress.translationY = if (isCollapsed) collapsedProgressTranslationY else 0f
        if (verticalOffset == 0 || isCollapsed) {


            toolbarContent.alpha = 1f
            return
        }


        var alpha = -1 * (100f / (totalScrollRange * 0.5f) * verticalOffset / 100)

        alpha = Math.max(0f, alpha)
        alpha = Math.min(1f, alpha)

        alpha = 1 - alpha
        toolbarContent.alpha = alpha
    }

}