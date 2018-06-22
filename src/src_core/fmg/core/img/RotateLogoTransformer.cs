using System;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> Transforming of logo rays </summary>
   public class RotateLogoTransformer : IModelTransformer {

      public void Execute(int currentFrame, int totalFrames, IImageModel model) {
         LogoModel lm = model as LogoModel;
         if (lm == null)
            throw new Exception("Illegal usage transformer");

         lm.Rays.Clear();
         lm.Inn.Clear();
         lm.Oct.Clear();

         SizeDouble size = lm.Size;
         PointDouble center = new PointDouble(size.Width/2.0, size.Height/2.0);
         double ra = lm.RotateAngle;
         FigureHelper.RotateCollection(lm.Rays, ra, center);
         FigureHelper.RotateCollection(lm.Inn , ra, center);
         FigureHelper.RotateCollection(lm.Oct , ra, center);
      }

   }

}
