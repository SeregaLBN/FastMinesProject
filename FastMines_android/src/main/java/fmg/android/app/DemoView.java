package fmg.android.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import fmg.android.draw.img.Logo;
import fmg.common.Color;
import fmg.core.img.IImageController;

public class DemoView extends View {

   private Color bkColor;
   private Logo.ControllerBitmap logo;

   public DemoView(Context context) {
      super(context);
      init(null, 0);
   }

   public DemoView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(attrs, 0);
   }

   public DemoView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(attrs, defStyle);
   }

   private void init(AttributeSet attrs, int defStyle) {
      bkColor = Color.RandomColor();
      logo = new Logo.ControllerBitmap();
      logo.getModel().setSize(300);
      logo.getModel().setAnimated(true);
      logo.useRotateTransforming(true);
      logo.usePolarLightFgTransforming(true);
      logo.addListener(ev -> {
         if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE))
            invalidate();
      });
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.drawRGB(bkColor.getR(), bkColor.getG(), bkColor.getB());
      canvas.drawBitmap(logo.getImage(), 0,0, null);
   }

   public void onNextImages() {
      bkColor = Color.RandomColor();
      invalidate();
   }

}
