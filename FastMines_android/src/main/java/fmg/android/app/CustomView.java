package fmg.android.app;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import fmg.android.draw.img.Logo;
import fmg.core.img.LogoController;

public class CustomView extends View {

   private Logo.ControllerBitmap logo;

   public CustomView(Context context) {
      super(context);
      init(null, 0);
   }

   public CustomView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(attrs, 0);
   }

   public CustomView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(attrs, defStyle);
   }

   private void init(AttributeSet attrs, int defStyle) {
      logo = new Logo.ControllerBitmap();
      logo.getModel().setSize(300);
      logo.getModel().setAnimated(true);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      Random rand = new Random();
      canvas.drawRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
      canvas.drawBitmap(logo.getImage(), 0,0, null);
   }

   public void changeColor() {
      invalidate(); // redraws the view calling onDraw()
   }

}
