/*
 * Designed and developed by 2017 Roman Kazov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.kazov.colorpickerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerView extends View {

    private final float wheelWidthRatio = 0.8f;
    private final float wheelPaddingRatio = 0.1f;

    private int viewWidth;
    private int viewHeight;
    private int wheelSize;
    private int centerX;
    private int centerY;

    private final Paint colorWheelPaint;
    private final Paint colorWheelPointerPaint;
    private final RectF colorWheelPointerCords;

    private final Paint valueSliderPaint;
    private final Paint valuePointerPaint;
    private final RectF valueSliderRect;
    private final Path valueSliderPath;


    private Bitmap colorWheelBitmap;
    private int colorWheelRadius;

    /**
     * Selected color
     */
    private float[] hsvColor = new float[]{0f, 0f, 1f};

    /**
     * List of harmonized color
     */
    private final ArrayList<Float[]> colorsList;

    /**
     * Current harmony type
     */
    private HarmonyTypes harmonyType;

    /**
     * Touchable variable
     */
    private boolean isTouchable = true;

    /**
     * On color selected callback
     */
    private ColorSelectedInterface colorListener;

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerView(Context context) {
        super(context);
    }

    {
        colorsList = new ArrayList<>(6);
        harmonyType = HarmonyTypes.NONE;

        colorWheelPaint = new Paint();
        colorWheelPaint.setAntiAlias(true);
        colorWheelPaint.setDither(true);

        colorWheelPointerPaint = new Paint();
        colorWheelPointerPaint.setStyle(Style.STROKE);
        colorWheelPointerPaint.setStrokeWidth(2f);
        colorWheelPointerPaint.setARGB(128, 0, 0, 0);
        colorWheelPointerCords = new RectF();

        valueSliderPaint = new Paint();
        valueSliderPaint.setAntiAlias(true);
        valueSliderPaint.setDither(true);

        valueSliderPath = new Path();
        valueSliderRect = new RectF();

        valuePointerPaint = new Paint();
        valuePointerPaint.setStyle(Style.STROKE);
        valuePointerPaint.setStrokeWidth(2f);
    }

    /**
     * Current color setter
     *
     * @param color Android int color
     */
    public void setColor(int color) {
        Color.colorToHSV(color, hsvColor);
        harmonize();
        invalidate();
        callbackColors();
    }

    /**
     * Current color getter
     *
     * @return Android int color
     */
    public int getColor() {
        return Color.HSVToColor(hsvColor);
    }

    /**
     * Setter for callback
     *
     * @param colorListener implementation of ColorSelectedInterface
     */
    public void setColorListener(ColorSelectedInterface colorListener) {
        this.colorListener = colorListener;
    }

    /**
     * Setter for touchability
     *
     * @param touchable is ColorPicker touchable
     */
    public void setTouchable(boolean touchable) {
        this.isTouchable = touchable;
    }

    /**
     * Getter for touchability
     *
     * @return is ColorPicker touchable
     */
    public boolean isTouchable() {
        return this.isTouchable;
    }

    /**
     * Sets current harmony type
     *
     * @param harmonyType ColorPickerView.HarmonyTypes Enums can be NONE, COMPLEMENTARY,
     *                    SPLIT_COMPLEMENTARY, ANALOGOUS, ANALOGOUS_ACCENT, TRIADIC, SQUARE,
     *                    TETRADIC_PLUS, TETRADIC_MINUS, CLASH, FIVE_TONE, SIX_TONE
     */
    public void setHarmonyType(HarmonyTypes harmonyType) {
        this.harmonyType = harmonyType;
        setColor(getColor());
    }

    /**
     * Getter for current harmony type
     *
     * @return ColorPickerView.HarmonyTypes Enums can be NONE, COMPLEMENTARY,
     * SPLIT_COMPLEMENTARY, ANALOGOUS, ANALOGOUS_ACCENT, TRIADIC, SQUARE,
     * TETRADIC_PLUS, TETRADIC_MINUS, CLASH, FIVE_TONE, SIX_TONE
     */
    public HarmonyTypes getHarmonyType() {
        return this.harmonyType;
    }

    /**
     * Getter for harmonized colors list
     *
     * @return list of harmonized colors
     */
    public List<Integer> getColorsList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (Float[] hsv : colorsList) {
            list.add(Color.HSVToColor(new float[]{hsv[0], hsv[1], hsv[2]}));
        }
        return list;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widthSize, (int) (widthSize * wheelWidthRatio));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        viewWidth = width;
        viewHeight = height;
        wheelSize = height;
        centerX = wheelSize / 2;
        centerY = wheelSize / 2;
        colorWheelRadius = wheelSize / 2;
        colorWheelBitmap = createColorWheelBitmap();
        valueSliderRect.set((wheelWidthRatio + wheelPaddingRatio) * viewWidth, 0, viewWidth, viewHeight);
        valueSliderPath.addRect(valueSliderRect, Path.Direction.CCW);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);

        float[] tmpHsv = new float[]{hsvColor[0], hsvColor[1], 1f};
        LinearGradient linearGradient = new LinearGradient(viewWidth, viewHeight, viewWidth, 0, new int[]{Color.BLACK, Color.HSVToColor(tmpHsv)}, null, TileMode.REPEAT);
        valueSliderPaint.setShader(linearGradient);
        canvas.drawPath(valueSliderPath, valueSliderPaint);

        drawWheelPointer(canvas);
        drawValuePointer(canvas);
    }

    private Bitmap createColorWheelBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(colorWheelRadius * 2, colorWheelRadius * 2, Config.ARGB_8888);

        int[] colors = new int[13];
        float[] hsv = new float[]{0f, 1f, 1f};
        for (int i = 0; i < colors.length; i++) {
            hsv[0] = (i * 30) % 360;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[12] = colors[0];

        SweepGradient sweepGradient = new SweepGradient(colorWheelRadius, colorWheelRadius, colors, null);
        RadialGradient radialGradient = new RadialGradient(colorWheelRadius, colorWheelRadius, colorWheelRadius, 0xFFFFFFFF, 0x00FFFFFF, TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);
        colorWheelPaint.setShader(composeShader);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(colorWheelRadius, colorWheelRadius, colorWheelRadius, colorWheelPaint);

        return bitmap;
    }

    private void drawWheelPointer(Canvas canvas) {
        for (Float[] color : colorsList) {
            float hueAngle = (float) Math.toRadians(color[0]);
            int colorPointX = (int) (Math.cos(hueAngle) * color[1] * colorWheelRadius) + centerX;
            int colorPointY = (int) (Math.sin(hueAngle) * color[1] * colorWheelRadius) + centerY;

            float pointerRadius = 0.075f * colorWheelRadius;
            int pointerX = (int) (colorPointX - pointerRadius / 2);
            int pointerY = (int) (colorPointY - pointerRadius / 2);

            colorWheelPointerCords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
            canvas.drawOval(colorWheelPointerCords, colorWheelPointerPaint);
        }
    }

    private void drawValuePointer(Canvas canvas) {
        valuePointerPaint.setColor(Color.HSVToColor(new float[]{0f, 0f, 1f - hsvColor[2]}));
        valuePointerPaint.setStrokeWidth(6f);
        float value = viewHeight - (hsvColor[2] * viewHeight);
        canvas.drawLine((wheelWidthRatio + wheelPaddingRatio) * viewWidth, value, viewWidth, value, valuePointerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouchable)
            return false;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:


                int x = (int) event.getX();
                int y = (int) event.getY();
                int dx = x - wheelSize / 2;
                int dy = y - viewHeight / 2;
                double d = Math.sqrt(dx * dx + dy * dy);

                if (d <= colorWheelRadius) {
                    Log.d(null, "onTouchEvent - Wheel");
                    hsvColor[0] = (float) (Math.toDegrees(Math.atan2(dy, dx)) + 360) % 360;
                    hsvColor[1] = Math.max(0f, Math.min(1f, (float) (d / colorWheelRadius)));


                } else if (x >= (wheelWidthRatio + wheelPaddingRatio) * viewWidth) {

                    Log.d(null, "onTouchEvent - Value");
                    if (y <= 0)
                        hsvColor[2] = 1f;
                    else if (y >= viewHeight)
                        hsvColor[2] = 0f;
                    else
                        hsvColor[2] = 1f - ((float) y / (float) viewHeight);

                } else {
                    return false;
                }

                harmonize();
                invalidate();

                callbackColors();

                return true;
        }
        return super.onTouchEvent(event);
    }

    private void harmonize() {
        colorsList.clear();
        switch (harmonyType) {
            case NONE:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                break;
            case COMPLEMENTARY:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 180) % 360, hsvColor[1], hsvColor[2]});
                break;
            case SPLIT_COMPLEMENTARY:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 150) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 210) % 360, hsvColor[1], hsvColor[2]});
                break;
            case ANALOGOUS:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 330) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 30) % 360, hsvColor[1], hsvColor[2]});
                break;
            case ANALOGOUS_ACCENT:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 330) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 30) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 180) % 360, hsvColor[1], hsvColor[2]});
                break;
            case TRIADIC:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 120) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 240) % 360, hsvColor[1], hsvColor[2]});
                break;
            case SQUARE:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 90) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 180) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 270) % 360, hsvColor[1], hsvColor[2]});
                break;
            case TETRADIC_PLUS:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 60) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 180) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 240) % 360, hsvColor[1], hsvColor[2]});
                break;
            case TETRADIC_MINUS:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 120) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 180) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 300) % 360, hsvColor[1], hsvColor[2]});
                break;
            case CLASH:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 90) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 270) % 360, hsvColor[1], hsvColor[2]});
                break;
            case FIVE_TONE:
                colorsList.add(new Float[]{(hsvColor[0]) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 60) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 120) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 240) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 300) % 360, hsvColor[1], hsvColor[2]});
                break;
            case SIX_TONE:
                colorsList.add(new Float[]{(hsvColor[0] + 30) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 90) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 120) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 240) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 270) % 360, hsvColor[1], hsvColor[2]});
                colorsList.add(new Float[]{(hsvColor[0] + 330) % 360, hsvColor[1], hsvColor[2]});
                break;
        }
    }

    private void callbackColors() {
        if (this.colorListener != null)
            colorListener.colorSelected(getColorsList());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(null, "onSaveInstanceState");
        Bundle state = new Bundle();
        state.putFloatArray("color", hsvColor);
        state.putSerializable("type", harmonyType);
        state.putParcelable("super", super.onSaveInstanceState());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d(null, "onRestoreInstanceState");
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            hsvColor = bundle.getFloatArray("color");
            harmonyType = (HarmonyTypes) bundle.getSerializable("type");
            harmonize();
            super.onRestoreInstanceState(bundle.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public  interface ColorSelectedInterface {

        /**
         * Callback method with list of colors
         *
         * @param colorsList List of Android Color int
         */
        void colorSelected(List<Integer> colorsList);
    }

    public enum HarmonyTypes {
        NONE, COMPLEMENTARY, SPLIT_COMPLEMENTARY, ANALOGOUS, ANALOGOUS_ACCENT, TRIADIC, SQUARE, TETRADIC_PLUS, TETRADIC_MINUS, CLASH, FIVE_TONE, SIX_TONE
    }
}