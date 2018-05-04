package fmg.swing.draw.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

import fmg.core.img.FlagModel;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;

public abstract class Flag<TImage> extends ImageView<TImage, FlagModel> {

   public Flag() {
      super(new FlagModel());
   }

   static {
      StaticInitilizer.init();
   }

   protected void draw(Graphics2D g) {
      double zoomX = getSize().width  / 100.0;
      double zoomY = getSize().height / 100.0;
      // perimeter figure points
      Point2D.Double[] p = new Point2D.Double[] {
            new Point2D.Double(13.5 *zoomX, 90*zoomY),
            new Point2D.Double(17.44*zoomX, 51*zoomY),
            new Point2D.Double(21   *zoomX, 16*zoomY),
            new Point2D.Double(85   *zoomX, 15*zoomY),
            new Point2D.Double(81.45*zoomX, 50*zoomY)};

      double penWidth = 7 * (zoomX + zoomY) / 2;
      BasicStroke penLine = new BasicStroke((float)penWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
      g.setStroke(penLine);
      g.setColor(Color.BLACK);
    //g.drawLine((int)p[0].x, (int)p[0].y, (int)p[2].x, (int)p[2].y);
      g.drawLine((int)p[0].x, (int)p[0].y, (int)p[1].x, (int)p[1].y);

      BasicStroke penCurve = new BasicStroke((float)penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
      g.setStroke(penCurve);
      g.setColor(Color.RED);
      CubicCurve2D curve = new CubicCurve2D.Double(
            p[2].x, p[2].y,
            95*zoomX, 0*zoomY,
            19.3*zoomX, 32*zoomY,
            p[3].x, p[3].y);
       g.draw(curve);
//       if (false) {
//         curve = new CubicCurve2D.Double(
//               p[1].x, p[1].y,
//               55.5*zoomX, 15*zoomY,
//               45*zoomX, 62.5*zoomY,
//               p[3].x, p[3].y);
//          g.draw(curve);
//       } else
       {
          curve = new CubicCurve2D.Double(
               p[3].x, p[3].y,
               77.8*zoomX, 32.89*zoomY,
               88.05*zoomX, 22.73*zoomY,
               p[4].x, p[4].y);
          g.draw(curve);
          curve = new CubicCurve2D.Double(
               p[4].x, p[4].y,
               15.83*zoomX, 67*zoomY,
               91.45*zoomX, 35*zoomY,
               p[1].x, p[1].y);
          g.draw(curve);
       }
       g.setStroke(penLine);
       g.drawLine((int)p[1].x, (int)p[1].y, (int)p[2].x, (int)p[2].y);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Flag image view implementation over {@link javax.swing.Icon} */
   static class Icon extends Flag<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      @Override
      protected javax.swing.Icon createImage() { return ico.create(); }

      @Override
      protected void drawBody() { draw(ico.getGraphics()); }

      @Override
      public void close() {
         ico.close();
         super.close();
         ico = null;
      }

   }

   /** Flag image view implementation over {@link java.awt.Image} */
   static class Image extends Flag<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** Flag image controller implementation for {@link Icon} */
   public static class ControllerIcon extends ImageController<javax.swing.Icon, Flag.Icon, FlagModel> {
      public ControllerIcon() { super(new Flag.Icon()); }
   }

   /** Flag image controller implementation for {@link Image} */
   public static class ControllerImage extends ImageController<java.awt.Image, Flag.Image, FlagModel> {
      public ControllerImage() { super(new Flag.Image()); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> {
            return Arrays.asList(new Flag.ControllerIcon(),
                                 new Flag.ControllerImage());
         }
      );
   }
   //////////////////////////////////

}
