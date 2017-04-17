package fmg.jfx.draw.img;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.core.img.ALogo;
import fmg.jfx.Cast;
import fmg.jfx.utils.ImgUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/** Main logos image */
public abstract class Logo<TImage> extends ALogo<TImage> {

   static {
      StaticRotateImgConsts.init();
   }

   protected void drawBody(GraphicsContext g) {
      { // fill background
         fmg.common.Color bkClr = getBackgroundColor();
         if (!bkClr.isOpaque()) { // fix not supporting in jFx the mode equals java.awt.AlphaComposite.SRC
            g.setFill(Color.WHITE);
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
         if (!bkClr.isTransparent()) {
            g.setFill(Cast.toColor(bkClr));
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
      }

      List<PointDouble> rays0 = new ArrayList<>();
      List<PointDouble> inn0 = new ArrayList<>();
      List<PointDouble> oct0 = new ArrayList<>();
      getCoords(rays0, inn0, oct0);

      Point2D [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D center = new Point2D(getSize().width/2.0, getSize().height/2.0);

      Color [] palette = Arrays.stream(Palette)
         .map(hsv -> Cast.toColor(hsv.toColor()))
         .toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         if (isUseGradient()) {
            // rectangle gragient
            setGradientFill(g, oct[(i+5)%8], palette[(i+0)%8], oct[i], palette[(i+3)%8]);
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
//            BlendMode bm = g.getGlobalBlendMode();
//            g.setGlobalBlendMode(BlendMode.SRC_OVER);
            Color clr = palette[(i+6)%8];
            clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
            setGradientFill(g, center, clr, inn[(i+6)%8], palette[(i+3)%8]);
            fillPolygon(g, rays[i], oct[i], inn[i]);
            setGradientFill(g, center, clr, inn[(i+2)%8], palette[(i+0)%8]);
            fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
//            g.setGlobalBlendMode(bm);
         } else {
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
         if (isUseGradient())
            setGradientFill(g,
                  inn[i], palette[(i+6)%8],
                  center, ((i&1)==0) ? Color.BLACK : Color.WHITE);
         else
            g.setFill(((i & 1) == 0)
                  ? Cast.toColor(Palette[(i + 6)%8].toColor().brighter())
                  : Cast.toColor(Palette[(i + 6)%8].toColor().darker()));
         fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
      }
   }

   private static void setGradientFill(GraphicsContext g, Point2D start, Color startClr, Point2D end, Color endClr) {
      g.setFill(new LinearGradient(start.getX(), start.getY(),
                                   end  .getX(), end  .getY(),
                                   false,
                                   CycleMethod.NO_CYCLE,
                                   new Stop[] {
                                      new Stop(0, startClr),
                                      new Stop(1, endClr)
                                   }));
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

   }

   public static class Image extends Logo<javafx.scene.image.Image> {

      private javafx.scene.canvas.Canvas canvas;

      @Override
      protected javafx.scene.image.Image createImage() {
         Size size = getSize();
         canvas = new javafx.scene.canvas.Canvas(size.width, size.height);
         return ImgUtils.toImage(canvas);
      }

      @Override
      protected void drawEnd() {
         super.drawEnd();
         setImage(ImgUtils.toImage(canvas));
      }

      @Override
      protected void drawBody() { drawBody(canvas.getGraphicsContext2D()); }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> Arrays.asList(new Logo.Canvas()
                                            , new Logo.Image()
                                            , new Logo.Canvas()
                                            , new Logo.Image()
                         ));
   }
   //////////////////////////////////

}
