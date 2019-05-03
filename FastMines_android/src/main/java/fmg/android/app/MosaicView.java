package fmg.android.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.function.Consumer;

public class MosaicView extends View {

    public Consumer<Canvas> drawMethod;

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawMethod != null)
            drawMethod.accept(canvas);
    }

}
