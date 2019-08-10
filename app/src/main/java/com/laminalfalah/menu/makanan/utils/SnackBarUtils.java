package com.laminalfalah.menu.makanan.utils;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.snackbar.Snackbar;
import com.laminalfalah.menu.makanan.App;
import com.laminalfalah.menu.makanan.R;

public class SnackBarUtils {

    private static final String TAG = SnackBarUtils.class.getSimpleName();

    private Snackbar snackbar;
    private final Context mContext;
    private View view = null;
    private ViewGroup viewGroup;
    private final SnackbarHelper snackbarHelper;

    public SnackBarUtils(Context mContext) {
        this.mContext = mContext;
        snackbarHelper = new SnackbarHelper();
    }

    private View getRootViewCustom() {
        if (mContext instanceof Activity) {
            viewGroup = ((Activity) mContext).findViewById(android.R.id.content);
        } else {
            viewGroup = App.mActivity.findViewById(android.R.id.content);
        }

        if (viewGroup != null) {
            if (viewGroup.getChildAt(0) instanceof DrawerLayout) {
                view = viewGroup.findViewById(R.id.coordinator);
            } else {
                view = viewGroup.getChildAt(0);
            }
        }
        if (view == null) {
            view = App.mActivity.getWindow().getDecorView().getRootView();
        }
        return view;
    }

    private void addInterceptLayoutToViewGroup(Snackbar snackbar) {
        View contentView = App.mActivity.findViewById(android.R.id.content);
        ViewGroup vg;

        if (viewGroup.getChildAt(0) instanceof DrawerLayout) {
            vg = (ViewGroup) ((ViewGroup) view).getChildAt(0);
        } else {
            vg = (ViewGroup) ((ViewGroup) contentView).getChildAt(0);
        }

        if (!(vg.getChildAt(0) instanceof InterceptTouchEventLayout)) {
            InterceptTouchEventLayout interceptLayout = new InterceptTouchEventLayout(App.mActivity.getApplicationContext());
            interceptLayout.setSnackbar(snackbar);
            if (viewGroup.getChildAt(0) instanceof DrawerLayout) {
                for (int i = 2; i < vg.getChildCount(); i++) {
                    View view = vg.getChildAt(i);
                    vg.removeView(view);
                    interceptLayout.addView(view);
                }
            } else {
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View view = vg.getChildAt(i);
                    vg.removeView(view);
                    interceptLayout.addView(view);
                }
            }
            vg.addView(interceptLayout, 0);
        } else {
            InterceptTouchEventLayout interceptLayout = (InterceptTouchEventLayout) vg.getChildAt(0);
            interceptLayout.setSnackbar(snackbar);
        }
    }

    public void snackbarShort(String message) {
        snackbar = Snackbar.make(getRootViewCustom(), message, Snackbar.LENGTH_SHORT);
        snackbarHelper.configSnackbar(App.mActivity.getApplicationContext(), snackbar);
        snackbar.show();
    }

    public void snackBarLong(String message) {
        snackbar = Snackbar.make(getRootViewCustom(), message, Snackbar.LENGTH_LONG);
        snackbarHelper.configSnackbar(App.mActivity.getApplicationContext(), snackbar);
        snackbar.show();
    }

    public void snackBarInfinite(String message) {
        snackbar = Snackbar.make(getRootViewCustom(), message, Snackbar.LENGTH_INDEFINITE);
        snackbarHelper.configSnackbar(App.mActivity.getApplicationContext(), snackbar);
        snackbar.setDuration(60*1000);
        snackbar.show();
        addInterceptLayoutToViewGroup(snackbar);
    }

    public void snackBarInfinite(String message, String action) {
        snackbar = Snackbar.make(getRootViewCustom(), message, Snackbar.LENGTH_INDEFINITE);
        snackbarHelper.configSnackbar(App.mActivity.getApplicationContext(), snackbar);
        snackbar.setDuration(60*1000);
        snackbar.setAction(action, v -> snackbar.dismiss());
        snackbar.show();
        addInterceptLayoutToViewGroup(snackbar);
    }

    class InterceptTouchEventLayout extends FrameLayout {

        Snackbar mSnackbar;

        public InterceptTouchEventLayout(Context context) {
            super(context);
            setLayoutParams(new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.MATCH_PARENT));
        }

        public void setSnackbar(Snackbar snackbar) {
            mSnackbar = snackbar;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                if (mSnackbar != null && mSnackbar.isShown()) {
                    mSnackbar.dismiss();
                }
            }
            return super.onInterceptTouchEvent(ev);
        }
    }

    private class SnackbarHelper {

        private void configSnackbar(Context context, Snackbar snack) {
            addMargins(snack);
            setRoundBordersBg(context, snack);
            snack.getView().setPadding(2,2,2,2);
            ViewCompat.setElevation(snack.getView(), 10f);
        }

        private void addMargins(Snackbar snack) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snack.getView().getLayoutParams();
            params.setMargins(24, 0, 24, 24);
            snack.getView().setLayoutParams(params);
        }

        private void setRoundBordersBg(Context context, Snackbar snackbar) {
            snackbar.getView().setBackground(context.getDrawable(R.drawable.background_snackbar));
        }
    }
}
