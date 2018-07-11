package fmg.jfx.draw.img;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.core.img.ImageView;
import fmg.core.img.LogoController;
import fmg.core.img.LogoModel;
import fmg.jfx.utils.Cast;
import fmg.jfx.utils.StaticInitializer;

/** Main logos image - base Logo image view implementation */
public abstract class Logo<TImage> extends ImageView<TImage, LogoModel> {

   protected Logo() {
      super(new LogoModel());
   }

   static {
      StaticInitializer.init();
   }

   protected void draw(GraphicsContext g) {
      LogoModel lm = this.getModel();

      { // fill background
         fmg.common.Color bkClr = lm.getBackgroundColor();
         if (!bkClr.isOpaque())
            g.clearRect(0, 0, getSize().width, getSize().height);
         if (!bkClr.isTransparent()) {
            g.setFill(Cast.toColor(bkClr));
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
      }

      List<PointDouble> rays0 = lm.getRays();
      List<PointDouble> inn0  = lm.getInn();
      List<PointDouble> oct0  = lm.getOct();

      Point2D [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D[size]);
      Point2D center = new Point2D(getSize().width/2.0, getSize().height/2.0);

      HSV[] hsvPalette = lm.getPalette();
      Color[] palette = Arrays.stream(hsvPalette)
         .map(hsv -> Cast.toColor(hsv.toColor()))
         .toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         if (!lm.isUseGradient()) {
            // linear gragient
            g.setFill(Cast.toColor(hsvPalette[i].toColor().darker()));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
         } else {
            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
            // over linear gragients

            setGradientFill(g, rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8]);
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

            Point2D p1 = oct[i];
            Point2D p2 = oct[(i+5)%8];
            Point2D p = new Point2D((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

            Color clr;// = new Color(1,1,1,0); //  Cast.toColor(fmg.common.Color.Transparent);
            if (true) {
               HSV c1 = hsvPalette[(i+1)%8];
               HSV c2 = hsvPalette[(i+6)%8];
               double diff = c1.h - c2.h;
               HSV cP = new HSV(c1.toColor());
               cP.h += diff/2; // цвет в точке p (пересечений линий...)
               cP.a = 0;
               clr = Cast.toColor(cP.toColor());
            }

            setGradientFill(g, oct[i], palette[(i+3)%8], p, clr);
            fillPolygon(g, rays[i], oct[i], inn[i]);

            setGradientFill(g, oct[(i+5)%8], palette[(i+0)%8], p, clr);
            fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
         }
      }

      // paint star perimeter
      double zoomAverage = (lm.getZoomX() + lm.getZoomY())/2;
      final double penWidth = lm.getBorderWidth() * zoomAverage;
      g.setLineWidth(penWidth);
      for (int i=0; i<8; i++) {
         Point2D p1 = rays[(i + 7)%8];
         Point2D p2 = rays[i];
         g.setStroke(palette[i].darker());
         g.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
      }

      // paint inner gradient triangles
      for (int i=0; i<8; i++) {
         if (lm.isUseGradient()) {
            Point2D p1 = inn[(i+0)%8];
            Point2D p2 = inn[(i+3)%8];
            Point2D p = new Point2D((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // center line of p1-p2
            setGradientFill(g,
                  p, palette[(i+6)%8],
                  center, ((i & 1) == 1) ? Color.BLACK : Color.WHITE);
         } else {
            g.setFill(((i & 1) == 1)
                  ? Cast.toColor(hsvPalette[(i + 6)%8].toColor().brighter())
                  : Cast.toColor(hsvPalette[(i + 6)%8].toColor().darker()));
         }
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

   /** Logo image view implementation over {@link javafx.scene.canvas.Canvas} */
   public static class Canvas extends Logo<javafx.scene.canvas.Canvas> {

      private CanvasJfx canvas = new CanvasJfx(this);

      @Override
      protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

      @Override
      protected void drawBody() { draw(canvas.getGraphics()); }

   }

   /** Logo image view implementation over {@link javafx.scene.image.Image} */
   public static class Image extends Logo<javafx.scene.image.Image> {

      private ImageJfx img = new ImageJfx(this);

      @Override
      protected javafx.scene.image.Image createImage() {
         img.createCanvas();
         return null; // img.createImage(); // fake empty image
      }

      @Override
      protected void drawBody() {
         draw(img.getGraphics());
         setImage(img.createImage()); // real image
      }

   }

   /** Logo image controller implementation for {@link Canvas} */
   public static class ControllerCanvas extends LogoController<javafx.scene.canvas.Canvas, Logo.Canvas> {

      public ControllerCanvas() {
         super(new Logo.Canvas());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** Logo image controller implementation for {@link Image} */
   public static class ControllerImage extends LogoController<javafx.scene.image.Image, Logo.Image> {

      public ControllerImage() {
         super(new Logo.Image());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> Arrays.asList(/*new Logo.ControllerCanvas()
                                            , new Logo.ControllerImage()
                                            , new Logo.ControllerCanvas()
                                            , */new Logo.ControllerImage()));
   }
   //////////////////////////////////

}
