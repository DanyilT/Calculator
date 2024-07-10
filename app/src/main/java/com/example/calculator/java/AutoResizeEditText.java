package com.example.calculator.java;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatEditText;

public class AutoResizeEditText extends AppCompatEditText {

    private float mMinTextSize;
    private float mMaxTextSize;

    public AutoResizeEditText(Context context) {
        super(context);
        init();
    }

    public AutoResizeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoResizeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMinTextSize = 12;
        mMaxTextSize = 100;
    }

    public void setMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
    }

    public void setMaxTextSize(float maxTextSize) {
        mMaxTextSize = maxTextSize;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        resizeText();
    }

    private void resizeText() {
        float textSize = mMaxTextSize;
        Rect bounds = new Rect();

        do {
            textSize--;
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            getPaint().getTextBounds(getText().toString(), 0, getText().length(), bounds);
        } while ((bounds.width() > getWidth() - getPaddingLeft() - getPaddingRight()) && textSize > mMinTextSize);
    }
}
