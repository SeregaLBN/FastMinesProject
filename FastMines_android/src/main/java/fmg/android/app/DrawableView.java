package fmg.android.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.function.Consumer;

/** Default android {@link View} where can define onDraw method dynamically via consumer {@link DrawableView#drawMethod} */
public class DrawableView extends View {

    public Consumer<Canvas> drawMethod;

    public DrawableView(Context context) {
        super(context);
    }

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawMethod != null)
            drawMethod.accept(canvas);
    }

}
