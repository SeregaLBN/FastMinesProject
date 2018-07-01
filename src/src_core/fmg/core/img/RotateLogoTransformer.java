package fmg.core.img;

import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;

/** Transforming of logo rays */
public class RotateLogoTransformer implements IModelTransformer {

   @Override
   public void execute(IAnimatedModel model) {
      if (!(model instanceof LogoModel))
         throw new RuntimeException("Illegal usage transformer");

      LogoModel lm = (LogoModel)model;

      lm.getRays().clear();
      lm.getInn().clear();
      lm.getOct().clear();

      SizeDouble size = lm.getSize();
      PointDouble center = new PointDouble(size.width/2.0, size.height/2.0);
      double ra = lm.getRotateAngle();
      FigureHelper.rotateCollection(lm.getRays(), ra, center);
      FigureHelper.rotateCollection(lm.getInn() , ra, center);
      FigureHelper.rotateCollection(lm.getOct() , ra, center);
   }

}
