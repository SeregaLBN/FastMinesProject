package fmg.swing.draw.img;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.core.img.ImageView;
import fmg.core.img.LogoController;
import fmg.core.img.LogoModel;
import fmg.swing.Cast;

/** Main logos image - base Logo image view implementation */
public abstract class Logo<TImage> extends ImageView<TImage, LogoModel> {

   protected Logo() {
      super(new LogoModel());
   }

   static {
      StaticInitilizer.init();
   }

   protected void draw(Graphics2D g) {
      LogoModel lm = this.getModel();
      { // fill background
         g.setComposite(AlphaComposite.Src);
         fmg.common.Color bkClr = lm.getBackgroundColor();
         if (!bkClr.isTransparent()) {
            g.setColor(Cast.toColor(bkClr));
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
      }

      g.setComposite(AlphaComposite.SrcOver);
      List<PointDouble> rays0 = lm.getRays();
      List<PointDouble> inn0  = lm.getInn();
      List<PointDouble> oct0  = lm.getOct();

      Point2D.Double [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double center = new Point2D.Double(getSize().width/2.0, getSize().height/2.0);

      HSV[] Palette = lm.getPalette();
      Color[] palette = Arrays.stream(Palette)
         .map(hsv -> Cast.toColor(hsv.toColor()))
         .toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         if (lm.isUseGradient()) {
            // rectangle gragient
            g.setPaint(new GradientPaint(oct[(i+5)%8], palette[(i+0)%8], oct[i], palette[(i+3)%8]));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
            Composite composite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            Color clr = palette[(i+6)%8];
            clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
            g.setPaint(new GradientPaint(center, clr, inn[(i+6)%8], palette[(i+3)%8]));
            fillPolygon(g, rays[i], oct[i], inn[i]);
            g.setPaint(new GradientPaint(center, clr, inn[(i+2)%8], palette[(i+0)%8]));
            fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
            g.setComposite(composite);
         } else {
            g.setColor(Cast.toColor(lm.getPalette()[i].toColor().darker()));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
         }
      }

      // paint star perimeter
      double zoomAverage = (lm.getZoomX() + lm.getZoomY())/2;
      final double penWidth = Math.max(1, 2 * zoomAverage);
      g.setStroke(new BasicStroke((float)penWidth));
      for (int i=0; i<8; i++) {
         Point2D.Double p1 = rays[(i + 7)%8];
         Point2D.Double p2 = rays[i];
         g.setColor(palette[i].darker());
         g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
      }

      // paint inner gradient triangles
      for (int i=0; i<8; i++) {
         if (lm.isUseGradient())
            g.setPaint(new GradientPaint(
                  inn[i], palette[(i+6)%8],
                  center, ((i&1)==0) ? Color.BLACK : Color.WHITE));
         else
            g.setColor(((i & 1) == 0)
                  ? Cast.toColor(Palette[(i + 6)%8].toColor().brighter())
                  : Cast.toColor(Palette[(i + 6)%8].toColor().darker()));
         fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
      }
   }

   private static void fillPolygon(Graphics2D g, Point2D.Double... p) {
      g.fillPolygon(
         Arrays.stream(p).mapToInt(s -> (int)s.x).toArray(),
         Arrays.stream(p).mapToInt(s -> (int)s.y).toArray(),
         p.length);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Logo image view implementation over {@link javax.swing.Icon} */
   static class Icon extends Logo<javax.swing.Icon> {

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

   /** Logo image view implementation over {@link java.awt.Image} */
   static class Image extends Logo<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** Logo image controller implementation for {@link Icon} */
   public static class ControllerIcon extends LogoController<javax.swing.Icon, Logo.Icon> {
      public ControllerIcon() { super(new Logo.Icon()); }
   }

   /** Logo image controller implementation for {@link Image} */
   public static class ControllerImage extends LogoController<java.awt.Image, Logo.Image> {
      public ControllerImage() { super(new Logo.Image()); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> Arrays.asList(new Logo.ControllerIcon()
                                            , new Logo.ControllerImage()
                                            , new Logo.ControllerIcon()
                                            , new Logo.ControllerImage()
                         ));
   }
   //////////////////////////////////

}
