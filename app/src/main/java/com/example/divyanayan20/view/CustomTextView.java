package com.example.divyanayan20.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomTextView extends View {
    private Paint paint;
    private float textX = 100; // Initial X position
    private float textY = 100; // Initial Y position
    private String text = "Divyanayan";
    private ValueAnimator animator;

    public CustomTextView(Context context) {
        super(context);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize the paint object to customize text appearance
        paint = new Paint();
        paint.setColor(Color.BLACK); // Set the text color
        paint.setTextSize(80f); // Set the text size

        // Create an animator to animate the text position (X-axis)
        animator = ValueAnimator.ofFloat(100f, 600f); // Animate from 100 to 500 (X position)
        animator.setDuration(2000); // Set duration for the animation (2 seconds)
        animator.setRepeatCount(ValueAnimator.INFINITE); // Repeat infinitely
        animator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation after reaching the end

        // Update the X position of the text in each animation frame
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textX = (float) animation.getAnimatedValue(); // Update text X position
                invalidate();  // Request a redraw of the view
            }
        });
        animator.start();  // Start the animation
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the text on the canvas at the current X, Y position
        canvas.drawText(text, textX, textY, paint);
    }
}
