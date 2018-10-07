using System;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

    /// <summary> Transforming of logo rays </summary>
    public class RotateLogoTransformer : IModelTransformer {

        public void Execute(IAnimatedModel model) {
            if (!(model is LogoModel lm))
                throw new Exception("Illegal usage transformer");

            lm.Rays.Clear();
            lm.Inn.Clear();
            lm.Oct.Clear();

            SizeDouble size = lm.Size;
            var center = new PointDouble(size.Width / 2.0, size.Height / 2.0);
            double ra = lm.RotateAngle;
            FigureHelper.RotateList(lm.Rays, ra, center);
            FigureHelper.RotateList(lm.Inn, ra, center);
            FigureHelper.RotateList(lm.Oct, ra, center);
        }

    }

}
