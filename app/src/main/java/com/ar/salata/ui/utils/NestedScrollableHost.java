package com.ar.salata.ui.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class NestedScrollableHost extends FrameLayout {

    private int touchSlop;
    private float initialX;
    private float initialY;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private ViewPager2 getParentViewPager() {
        View v = (View) getParent();
        while (v != null && !(v instanceof ViewPager2)) {
            if (!(v.getParent() instanceof View)) {
                break;
            }
            v = (View) v.getParent();
        }
        return (v instanceof ViewPager2) ? (ViewPager2) v : null;
    }

    private View getChild() {
        return getChildCount() > 0 ? getChildAt(0) : null;
    }

    private boolean canChildScroll(int orientation, float delta) {
        int direction = (int) -Math.signum(delta);
        View child = getChild();
        if (orientation == 0) {
            return child != null && child.canScrollHorizontally(direction);
        } else if (orientation == 1) {
            return child != null && child.canScrollVertically(direction);
        } else {
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        handleInterceptTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    private void handleInterceptTouchEvent(MotionEvent e) {
        ViewPager2 parentViewPager = getParentViewPager();
        if (parentViewPager == null) return;

        int orientation = parentViewPager.getOrientation();

        if (!canChildScroll(orientation, -1f) && !canChildScroll(orientation, 1f)) {
            return;
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = e.getX();
            initialY = e.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - initialX;
            float dy = e.getY() - initialY;
            boolean isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL;

            float scaledDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1f);
            float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1f : 0.5f);

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    if (canChildScroll(orientation, isVpHorizontal ? dx : dy)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
            }
        }
    }
}
