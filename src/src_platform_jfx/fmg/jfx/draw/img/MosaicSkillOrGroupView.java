package fmg.jfx.draw.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.canvas.GraphicsContext;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.WithBurgerMenuView;
import fmg.jfx.Cast;

/**
 * MVC: view. Abstract JFX representable {@link fmg.core.types.ESkillLevel} or {@link fmg.core.types.EMosaicGroup} as image
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> {@link MosaicsSkillModel} or {@link MosaicsGroupModel}
 */
abstract class MosaicSkillOrGroupView<TImage, TImageModel extends AnimatedImageModel> extends WithBurgerMenuView<TImage, TImageModel> {

   static {
      StaticInitilizer.init();
   }

   protected MosaicSkillOrGroupView(TImageModel imageModel) {
      super(imageModel);
   }

   /** get paint information of drawing basic image model */
   protected abstract Stream<Pair<Color, Stream<PointDouble>>> getCoords();


   protected void draw(GraphicsContext g) {
      TImageModel m = getModel();

      { // fill background
         fmg.common.Color bkClr = m.getBackgroundColor();
         if (!bkClr.isOpaque())
            g.clearRect(0, 0, getSize().width, getSize().height);
         if (!bkClr.isTransparent()) {
            g.setFill(Cast.toColor(bkClr));
            g.fillRect(0, 0, getSize().width, getSize().height);
         }
      }

      double bw = m.getBorderWidth();
      boolean needDrawPerimeterBorder = (!m.getBorderColor().isTransparent() && (bw > 0));
      javafx.scene.paint.Color borderColor = !needDrawPerimeterBorder ? null : Cast.toColor(m.getBorderColor());
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

      getBurgerMenuModel().getCoords()
         .forEach(li -> {
            g.setLineWidth(li.penWidht);
            g.setStroke(Cast.toColor(li.clr));
            g.strokeLine(li.from.x, li.from.y, li.to.x, li.to.y);
         });
   }

}
