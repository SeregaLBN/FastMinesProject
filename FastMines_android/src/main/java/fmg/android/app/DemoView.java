package fmg.android.app;

import java.util.function.Consumer;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class DemoView extends View {

   public Consumer<Canvas> _onDraw;

   public DemoView(Context context) {
      super(context);
   }

   public DemoView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public DemoView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      if (_onDraw != null)
         _onDraw.accept(canvas);
   }

}
