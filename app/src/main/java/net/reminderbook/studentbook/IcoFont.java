package net.reminderbook.studentbook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class IcoFont extends TextView {


    @SuppressLint("NewApi")
    public IcoFont(Context context) {
        super(context);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "icofont.ttf");
        this.setTypeface(face);
    }

    public IcoFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "icofont.ttf");
        this.setTypeface(face);
    }

    public IcoFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "icofont.ttf");
        this.setTypeface(face);
    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }

}