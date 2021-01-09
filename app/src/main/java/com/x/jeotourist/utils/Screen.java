package com.x.jeotourist.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.widget.FrameLayout;


import com.x.jeotourist.application.AppController;

import java.lang.reflect.Method;

/**
 * Class to provide Screen related all operations and fields
 */
public class Screen {
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
    private static float density;
    private static float scaledDensity;

    public static DisplayMetrics displayMetrics = new DisplayMetrics();

    public static int dp(float dp) {
        if (density == 0f)
            density = AppController.getInstance().getResources().getDisplayMetrics().density;
        displayMetrics = AppController.getInstance().getResources().getDisplayMetrics();
        return (int) (dp * density + .5f);
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    private static int getSize(float size) {
        return (int) (size < 0 ? size : Screen.dp(size));
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height));
    }

    public static FrameLayout.LayoutParams createFrame(int width, int height, int gravity) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height, int gravity, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
        layoutParams.setMargins(Screen.dp(leftMargin), Screen.dp(topMargin), Screen.dp(rightMargin), Screen.dp(bottomMargin));
        return layoutParams;
    }

    public static float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static int sp(float sp) {
        if (scaledDensity == 0f)
            scaledDensity = AppController.getInstance().getResources().getDisplayMetrics().scaledDensity;

        return (int) (sp * scaledDensity + .5f);
    }



    public static int getWidth() {
        return AppController.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeight() {
        return AppController.getInstance().getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight() {

        int result = 0;
        int resourceId = AppController.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = AppController.getInstance().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavbarHeight() {
        if (hasNavigationBar()) {
            int resourceId = AppController.getInstance().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return AppController.getInstance().getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public static boolean hasNavigationBar() {
        Resources resources = AppController.getInstance().getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return (id > 0) && resources.getBoolean(id);
    }

    public static float getDensity() {
        return density;
    }

    public static Drawable createSimpleSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.MULTIPLY));
        }


        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
//                if (Build.VERSION.SDK_INT < 21) {
//                    Drawable drawable = getStateDrawable(this, index);
//                    ColorFilter colorFilter = null;
//                    if (drawable instanceof BitmapDrawable) {
//                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
//                    } else if (drawable instanceof NinePatchDrawable) {
//                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
//                    }
//                    boolean result = super.selectDrawable(index);
//                    if (colorFilter != null) {
//                        drawable.setColorFilter(colorFilter);
//                    }
//                    return result;
//                }
               return super.selectDrawable(index);
           }
      };
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    static Method StateListDrawable_getStateDrawableMethod;

    @SuppressLint("PrivateApi")
    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (StateListDrawable_getStateDrawableMethod == null) {
            try {
                StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
            } catch (Throwable ignore) {

            }
        }
        if (StateListDrawable_getStateDrawableMethod == null) {
            return null;
        }
        try {
            return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, index);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static Drawable createCircleDrawable(int size, int color) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(color);
        return defaultDrawable;
    }

}