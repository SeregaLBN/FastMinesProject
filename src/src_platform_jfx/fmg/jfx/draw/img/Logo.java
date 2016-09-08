package fmg.jfx.draw.img;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.core.img.ALogo;
import fmg.jfx.Cast;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Main logos image */
public abstract class Logo<TImage> extends ALogo<TImage> {

   static {
      StaticRotateImgConsts.init();
   }

   protected void drawBody(GraphicsContext g) {
      { // fill background
         fmg.common.Color bkClr = getBackgroundColor();
         //if (bkClr.getA() != fmg.common.Color.Transparent.getA())
         {
            g.setFill(Cast.toColor(bkClr));
            g.fillRect(0, 0, getWidth(), getHeight());
         }
      }

      List<PointDouble> rays0 = new ArrayList<>();
      List<PointDouble> inn0 = new ArrayList<>();
      List<PointDouble> oct0 = new ArrayList<>();
      getCoords(rays0, inn0, oct0);

      Point2D [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D center = new Point2D(getWidth()/2.0, getHeight()/2.0);

      Color [] palette = Arrays.stream(Palette)
         .map(hsv -> Cast.toColor(hsv.toColor()))
         .toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
//         if (isUseGradient()) {
//            // rectangle gragient
//            g.setPaint(new GradientPaint(oct[(i+5)%8], palette[(i+0)%8], oct[i], palette[(i+3)%8]));
//            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
//
//            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
//            Composite composite = g.getComposite();
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
//            Color clr = palette[(i+6)%8];
//            clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
//            g.setPaint(new GradientPaint(center, clr, inn[(i+6)%8], palette[(i+3)%8]));
//            fillPolygon(g, rays[i], oct[i], inn[i]);
//            g.setPaint(new GradientPaint(center, clr, inn[(i+2)%8], palette[(i+0)%8]));
//            fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
//            g.setComposite(composite);
//         } else
         {
            g.setFill(Cast.toColor(Palette[i].toColor().darker()));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
         }
      }

      // paint star perimeter
      double zoomAverage = (getZoomX() + getZoomY())/2;
      final double penWidth = Math.max(1, 2 * zoomAverage);
      g.setLineWidth(penWidth);
      for (int i=0; i<8; i++) {
         Point2D p1 = rays[(i + 7)%8];
         Point2D p2 = rays[i];
         g.setStroke(palette[i].darker());
         g.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
      }

      // paint inner gradient triangles
      for (int i=0; i<8; i++) {
//         if (isUseGradient())
//            g.setPaint(new GradientPaint(
//                  inn[i], palette[(i+6)%8],
//                  center, ((i&1)==0) ? Color.BLACK : Color.WHITE));
//         else
            g.setFill(((i & 1) == 0)
                  ? Cast.toColor(Palette[(i + 6)%8].toColor().brighter())
                  : Cast.toColor(Palette[(i + 6)%8].toColor().darker()));
         fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
      }
   }

   private static void fillPolygon(GraphicsContext g, Point2D... p) {
      g.fillPolygon(
         Arrays.stream(p).mapToDouble(s -> s.getX()).toArray(),
         Arrays.stream(p).mapToDouble(s -> s.getY()).toArray(),
         p.length);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Canvas extends Logo<javafx.scene.canvas.Canvas> {

      @Override
      protected javafx.scene.canvas.Canvas createImage() {
         Size size = getSize();
         return new javafx.scene.canvas.Canvas(size.width, size.height);
      }

      @Override
      protected void drawBody() { drawBody(getImage().getGraphicsContext2D()); }

      public static Image castToImage(javafx.scene.canvas.Canvas self) {
         SnapshotParameters params = new SnapshotParameters();
         params.setFill(Color.TRANSPARENT);
         WritableImage image = self.snapshot(params, null);
         return image;
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(p -> Arrays.asList(new Logo.Canvas()
                                           , new Logo.Canvas()
                                           , new Logo.Canvas()
                                           , new Logo.Canvas()
                         ));
   }
   //////////////////////////////////

}
