package fmg.jfx.draw.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.core.img.AMosaicsSkillImg;
import fmg.core.types.ESkillLevel;
import fmg.jfx.Cast;
import fmg.jfx.utils.ImgUtils;
import javafx.scene.canvas.GraphicsContext;

/**
 * Representable {@link fmg.core.types.ESkillLevel} as image
 * <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas})
 **/
public abstract class MosaicsSkillImg<TImage> extends AMosaicsSkillImg<TImage> {

   static {
      StaticInitilizer.init();
   }

   /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
   protected MosaicsSkillImg(ESkillLevel skill) { super(skill); }

   protected void drawBody(GraphicsContext g) {
      { // fill background
         fmg.common.Color bkClr = getBackgroundColor();
         if (!bkClr.isOpaque())
            g.clearRect(0, 0, getSize().width, getSize().height);
         if (!bkClr.isTransparent()) {
            g.setFill(Cast.toColor(bkClr));
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
      }

      g.setFill(Cast.toColor(getBackgroundColor()));
      g.fillRect(0, 0, getSize().width, getSize().height);

      int bw = getBorderWidth();
      boolean needDrawPerimeterBorder = (!getBorderColor().isTransparent() && (bw > 0));
      javafx.scene.paint.Color borderColor = Cast.toColor(getBorderColor());
      if (needDrawPerimeterBorder)
         g.setLineWidth(bw);
      Stream<Pair<Color, Stream<PointDouble>>> stars = getCoords();
      stars.forEach(pair -> {
         List<PointDouble> poly = pair.second.collect(Collectors.toList());
         double[] polyX = Cast.toPolygon(poly, true);
         double[] polyY = Cast.toPolygon(poly, false);
         if (!pair.first.isTransparent()) {
            g.setFill(Cast.toColor(pair.first));
            g.fillPolygon(polyX, polyY, polyX.length);
         }

         // draw perimeter border
         if (needDrawPerimeterBorder) {
            g.setStroke(borderColor);
            g.strokePolygon(polyX, polyY, polyX.length);
         }
      });

      getCoordsBurgerMenu()
         .forEach(li -> {
            g.setLineWidth(li.penWidht);
            g.setStroke(Cast.toColor(li.clr));
            g.strokeLine(li.from.x, li.from.y, li.to.x, li.to.y);
         });
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Canvas extends MosaicsSkillImg<javafx.scene.canvas.Canvas> {

      /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
      public Canvas(ESkillLevel skill) { super(skill); }

      @Override
      protected javafx.scene.canvas.Canvas createImage() {
         Size size = getSize();
         return new javafx.scene.canvas.Canvas(size.width, size.height);
      }

      @Override
      protected void drawBody() { drawBody(getImage().getGraphicsContext2D()); }

   }

   public static class Image extends MosaicsSkillImg<javafx.scene.image.Image> {

      private javafx.scene.canvas.Canvas canvas;

      /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
      public Image(ESkillLevel skill) { super(skill); }

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
      TestDrawing.testApp(() ->
         Stream.concat(Stream.of((ESkillLevel)null),
                       Stream.of(ESkillLevel.values()))
               .map(e -> new Pair<>(new MosaicsSkillImg.Canvas(e),
                                    new MosaicsSkillImg.Image(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
