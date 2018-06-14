package fmg.swing.draw.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.img.MosaicSkillModel;
import fmg.core.img.WithBurgerMenuView;
import fmg.swing.Cast;
import fmg.swing.StaticInitilizer;

/**
 * MVC: view. Abstract SWING representable {@link fmg.core.types.ESkillLevel} or {@link fmg.core.types.EMosaicGroup} as image
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> {@link MosaicSkillModel} or {@link MosaicGroupModel}
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


   protected void draw(Graphics2D g) {
      TImageModel m = getModel();

      { // fill background
         g.setComposite(AlphaComposite.Src);
         g.setColor(Cast.toColor(m.getBackgroundColor()));
         g.fillRect(0, 0, (int)getSize().width, (int)getSize().height);
      }

      g.setComposite(AlphaComposite.SrcOver);
      double bw = m.getBorderWidth();
      boolean needDrawPerimeterBorder = (!m.getBorderColor().isTransparent() && (bw > 0));
      java.awt.Color borderColor = !needDrawPerimeterBorder ? null : Cast.toColor(m.getBorderColor());
      BasicStroke bs = !needDrawPerimeterBorder ? null : new BasicStroke((float)bw);
      Stream<Pair<Color, Stream<PointDouble>>> stars = getCoords();
      stars.forEach(pair -> {
         Polygon poly = Cast.toPolygon(pair.second.collect(Collectors.toList()));
         if (!pair.first.isTransparent()) {
            g.setColor(Cast.toColor(pair.first));
            g.fillPolygon(poly);
         }

         // draw perimeter border
         if (needDrawPerimeterBorder) {
            g.setColor(borderColor);
            g.setStroke(bs);
            g.drawPolygon(poly);
         }
      });

      // draw burger menu
      getBurgerMenuModel().getCoords()
         .forEach(li -> {
            g.setStroke(new BasicStroke((float)li.penWidht));
            g.setColor(Cast.toColor(li.clr));
            g.drawLine((int)li.from.x, (int)li.from.y, (int)li.to.x, (int)li.to.y);
         });
   }

}
