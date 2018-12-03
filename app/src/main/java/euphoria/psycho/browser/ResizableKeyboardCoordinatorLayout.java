

package euphoria.psycho.browser;

import android.content.Context;
import android.util.AttributeSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * A CoordinatorLayout implementation that resizes dynamically based on whether a keyboard is visible or not.
 */
public class ResizableKeyboardCoordinatorLayout extends CoordinatorLayout {
    private final ResizableKeyboardViewDelegate delegate;

    public ResizableKeyboardCoordinatorLayout(Context context) {
        this(context, null);
    }

    public ResizableKeyboardCoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizableKeyboardCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        delegate = new ResizableKeyboardViewDelegate(this, attrs);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        delegate.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        delegate.onDetachedFromWindow();
    }
}
