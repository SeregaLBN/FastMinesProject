using System;
using Fmg.Common;

namespace Fmg.Core.Img {

    /// <summary> Transforming of foreground color (rotation of foreground color) </summary>
    public class PolarLightFgTransformer : IModelTransformer {

        public void Execute(IAnimatedModel model) {
            if (!(model is AnimatedImageModel am))
                throw new InvalidOperationException("Illegal usage transformer");

            if (!am.PolarLights)
                return;

            double rotateAngleDelta = 360.0 / am.TotalFrames; // 360° / TotalFrames
            if (!am.AnimeDirection)
                rotateAngleDelta = -rotateAngleDelta;

            var hsv = new HSV(am.ForegroundColor);
            hsv.h += rotateAngleDelta;
            am.ForegroundColor = hsv.ToColor();
        }

    }

}
