package com.krikki.vocabularytrainer.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * This class resolves a known issue where you could not pan scrollable layout when keyboard is opened and activity
 * is set to fullscreen. That way you could not select items that are at the bottom.
 * To use it simply add an instance to your activity and call {@link #onResume()}, {@link #onPause()}
 * and {@link #onDestroy()} from activity's methods.
 *
 * This class was written by grennis and is available on GitHub at
 * https://gist.github.com/2e3cd5f7a9238c59861015ce0a7c5584.git
 */
public class SoftInputAdjustAssist {
    private View rootView;
    private ViewGroup contentContainer;
    private ViewTreeObserver viewTreeObserver;
    private ViewTreeObserver.OnGlobalLayoutListener listener = () -> possiblyResizeChildOfContent();
    private Rect contentAreaOfWindowBounds = new Rect();
    private FrameLayout.LayoutParams rootViewLayout;
    private int usableHeightPrevious = 0;

    public SoftInputAdjustAssist(Activity activity) {
        contentContainer = (ViewGroup) activity.findViewById(android.R.id.content);
        rootView = contentContainer.getChildAt(0);
        rootViewLayout = (FrameLayout.LayoutParams) rootView.getLayoutParams();
    }

    public void onPause() {
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(listener);
        }
    }

    public void onResume() {
        if (viewTreeObserver == null || !viewTreeObserver.isAlive()) {
            viewTreeObserver = rootView.getViewTreeObserver();
        }

        viewTreeObserver.addOnGlobalLayoutListener(listener);
    }

    public void onDestroy() {
        rootView = null;
        contentContainer = null;
        viewTreeObserver = null;
    }

    private void possiblyResizeChildOfContent() {
        contentContainer.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds);
        int usableHeightNow = contentAreaOfWindowBounds.height();

        if (usableHeightNow != usableHeightPrevious) {
            rootViewLayout.height = usableHeightNow;
            rootView.layout(contentAreaOfWindowBounds.left, contentAreaOfWindowBounds.top, contentAreaOfWindowBounds.right, contentAreaOfWindowBounds.bottom);
            rootView.requestLayout();

            usableHeightPrevious = usableHeightNow;
        }
    }
}