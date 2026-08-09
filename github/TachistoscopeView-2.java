package ro.tachistoscop.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class TachistoscopeView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bodyRect = new RectF();
    private final RectF windowRect = new RectF();
    private final RectF leverTrackRect = new RectF();
    private final RectF leverKnobRect = new RectF();
    private final RectF hitRect = new RectF();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String stimulus = "";
    private Typeface stimulusTypeface = Typeface.SERIF;
    private float stimulusSizeSp = 42f;

    private float pull = 0f;
    private float shutterOpen = 0f;
    private float dragStartY;
    private float dragStartPull;
    private boolean dragging = false;
    private boolean exposing = false;

    private LinearGradient bodyGradient;
    private LinearGradient shutterGradient;
    private LinearGradient leverGradient;

    public TachistoscopeView(Context context) {
        super(context);
        init();
    }

    public TachistoscopeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFocusable(true);
        setClickable(true);
        setBackgroundColor(Color.rgb(231, 215, 183));
        textPaint.setColor(Color.rgb(31, 26, 22));
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setStimulus(String value) {
        stimulus = value == null ? "" : value;
        invalidate();
    }

    public void setStimulusStyle(Typeface typeface, float sizeSp) {
        stimulusTypeface = typeface == null ? Typeface.SERIF : typeface;
        stimulusSizeSp = Math.max(22f, Math.min(72f, sizeSp));
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bodyGradient = new LinearGradient(0, 0, w, h,
                new int[]{Color.rgb(104, 61, 34), Color.rgb(128, 79, 43), Color.rgb(82, 47, 28)},
                null, Shader.TileMode.CLAMP);
        shutterGradient = new LinearGradient(0, 0, 0, h,
                new int[]{Color.rgb(118, 71, 40), Color.rgb(88, 50, 29), Color.rgb(125, 76, 42)},
                null, Shader.TileMode.CLAMP);
        leverGradient = new LinearGradient(0, 0, w, 0,
                new int[]{Color.rgb(67, 39, 23), Color.rgb(145, 92, 54), Color.rgb(80, 46, 26)},
                null, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float pad = dp(18);
        float top = dp(28);
        float bottom = h - dp(42);
        bodyRect.set(pad, top, w - pad, bottom);

        paint.setShader(bodyGradient);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(bodyRect, dp(22), dp(22), paint);
        paint.setShader(null);

        drawWoodGrain(canvas, bodyRect, 0.10f);

        float innerLeft = bodyRect.left + dp(30);
        float innerRight = bodyRect.right - dp(86);
        float innerTop = bodyRect.top + dp(74);
        float innerBottom = bodyRect.bottom - dp(82);
        windowRect.set(innerLeft, innerTop, innerRight, innerBottom);

        paint.setColor(Color.rgb(52, 31, 20));
        canvas.drawRoundRect(expand(windowRect, dp(8)), dp(10), dp(10), paint);

        paint.setColor(Color.rgb(246, 238, 218));
        canvas.drawRoundRect(windowRect, dp(5), dp(5), paint);

        paint.setColor(Color.rgb(68, 44, 30));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        paint.setTextSize(sp(18));
        canvas.drawText("TAHISTOSCOP", bodyRect.centerX() - dp(26), bodyRect.top + dp(45), paint);

        drawStimulus(canvas);
        drawShutter(canvas);
        drawLever(canvas);
        drawInstructions(canvas);
    }

    private void drawStimulus(Canvas canvas) {
        canvas.save();
        canvas.clipRect(windowRect);

        textPaint.setColor(Color.rgb(29, 27, 23));
        textPaint.setTypeface(stimulusTypeface);
        textPaint.setTextSize(sp(stimulusSizeSp));

        String value = stimulus == null ? "" : stimulus.trim();
        if (value.isEmpty()) value = "Adaugă conținut din Setări";

        int maxWidth = Math.max(1, (int) (windowRect.width() * 0.86f));
        StaticLayout layout = StaticLayout.Builder
                .obtain(value, 0, value.length(), textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .setLineSpacing(0f, 1.0f)
                .build();

        float x = windowRect.centerX() - maxWidth / 2f;
        float y = windowRect.centerY() - layout.getHeight() / 2f;
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawShutter(Canvas canvas) {
        canvas.save();
        canvas.clipRect(expand(windowRect, dp(1)));

        float travel = windowRect.height() + dp(4);
        float offsetY = -shutterOpen * travel;
        RectF shutter = new RectF(
                windowRect.left - dp(1),
                windowRect.top - dp(1) + offsetY,
                windowRect.right + dp(1),
                windowRect.bottom + dp(1) + offsetY
        );

        paint.setShader(shutterGradient);
        canvas.drawRect(shutter, paint);
        paint.setShader(null);

        drawWoodGrain(canvas, shutter, 0.16f);

        paint.setColor(Color.argb(120, 31, 18, 10));
        paint.setStrokeWidth(dp(2));
        canvas.drawLine(shutter.left, shutter.bottom - dp(7), shutter.right, shutter.bottom - dp(7), paint);
        canvas.restore();
    }

    private void drawLever(Canvas canvas) {
        float trackLeft = bodyRect.right - dp(58);
        float trackRight = bodyRect.right - dp(28);
        float trackTop = bodyRect.top + dp(90);
        float trackBottom = bodyRect.bottom - dp(88);
        leverTrackRect.set(trackLeft, trackTop, trackRight, trackBottom);

        paint.setColor(Color.rgb(47, 29, 18));
        canvas.drawRoundRect(leverTrackRect, dp(12), dp(12), paint);

        float knobHeight = dp(74);
        float knobTravel = Math.max(0f, leverTrackRect.height() - knobHeight - dp(8));
        float knobTop = leverTrackRect.top + dp(4) + pull * knobTravel;
        leverKnobRect.set(
                leverTrackRect.left - dp(9),
                knobTop,
                leverTrackRect.right + dp(9),
                knobTop + knobHeight
        );

        paint.setShader(leverGradient);
        canvas.drawRoundRect(leverKnobRect, dp(12), dp(12), paint);
        paint.setShader(null);
        drawWoodGrain(canvas, leverKnobRect, 0.20f);

        paint.setColor(Color.rgb(214, 176, 114));
        canvas.drawCircle(leverKnobRect.centerX(), leverKnobRect.centerY(), dp(5), paint);
        paint.setColor(Color.rgb(52, 31, 20));
        canvas.drawCircle(leverKnobRect.centerX(), leverKnobRect.centerY(), dp(2), paint);

        hitRect.set(
                leverTrackRect.left - dp(28),
                leverTrackRect.top - dp(26),
                leverTrackRect.right + dp(28),
                leverTrackRect.bottom + dp(26)
        );

        paint.setColor(Color.argb(190, 244, 225, 191));
        paint.setTextSize(sp(11));
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("TRAGE", leverTrackRect.centerX(), leverTrackRect.bottom + dp(28), paint);
    }

    private void drawInstructions(Canvas canvas) {
        String message;
        if (exposing) {
            message = "";
        } else if (dragging) {
            int percent = Math.round(pull * 100f);
            message = "Tensiune " + percent + "%  •  eliberează când ești gata";
        } else {
            message = "Trage mânerul în jos • textul rămâne ascuns până la eliberare";
        }

        paint.setColor(Color.rgb(63, 42, 29));
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(sp(12));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(message, bodyRect.centerX() - dp(18), bodyRect.bottom - dp(28), paint);
    }

    private void drawWoodGrain(Canvas canvas, RectF rect, float opacity) {
        int alpha = Math.max(10, Math.min(80, (int) (255 * opacity)));
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.argb(alpha, 44, 24, 13));

        int lines = 17;
        for (int i = 0; i < lines; i++) {
            float y = rect.top + (i + 1) * rect.height() / (lines + 1f);
            float wobble = (float) Math.sin(i * 1.7) * dp(3);
            canvas.drawLine(rect.left + dp(5), y, rect.right - dp(5), y + wobble, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (exposing) return true;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (hitRect.contains(event.getX(), event.getY())) {
                    dragging = true;
                    dragStartY = event.getY();
                    dragStartPull = pull;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                    return true;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    float usable = Math.max(dp(120), leverTrackRect.height() - dp(80));
                    float delta = (event.getY() - dragStartY) / usable;
                    pull = clamp(dragStartPull + delta, 0f, 1f);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    float armedPull = pull;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    springLeverBack();
                    if (armedPull >= 0.22f) {
                        triggerExposure(armedPull);
                    } else {
                        invalidate();
                    }
                    performClick();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                springLeverBack();
                return true;

            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void triggerExposure(float tension) {
        if (stimulus == null || stimulus.trim().isEmpty() || exposing) return;

        exposing = true;
        long computedMs = (long) (300 - 250 * Math.sqrt(clamp(tension, 0.22f, 1f)));
        final long visibleMs = Math.max(40, Math.min(150, computedMs));

        ValueAnimator open = ValueAnimator.ofFloat(shutterOpen, 1f);
        open.setDuration(55);
        open.setInterpolator(new DecelerateInterpolator());
        open.addUpdateListener(animation -> {
            shutterOpen = (float) animation.getAnimatedValue();
            invalidate();
        });
        open.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handler.postDelayed(TachistoscopeView.this::closeShutter, visibleMs);
            }
        });
        open.start();
    }

    private void closeShutter() {
        ValueAnimator close = ValueAnimator.ofFloat(shutterOpen, 0f);
        close.setDuration(65);
        close.setInterpolator(new DecelerateInterpolator());
        close.addUpdateListener(animation -> {
            shutterOpen = (float) animation.getAnimatedValue();
            invalidate();
        });
        close.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                exposing = false;
                shutterOpen = 0f;
                invalidate();
            }
        });
        close.start();
    }

    private void springLeverBack() {
        ValueAnimator reset = ValueAnimator.ofFloat(pull, 0f);
        reset.setDuration(150);
        reset.setInterpolator(new DecelerateInterpolator());
        reset.addUpdateListener(animation -> {
            pull = (float) animation.getAnimatedValue();
            invalidate();
        });
        reset.start();
    }

    private RectF expand(RectF src, float amount) {
        return new RectF(src.left - amount, src.top - amount, src.right + amount, src.bottom + amount);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
