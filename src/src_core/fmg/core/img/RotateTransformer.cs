using System;

namespace Fmg.Core.Img {

    /// <summary> Transforming of rotate angle </summary>
    public class RotateTransformer : IModelTransformer {

        public void Execute(IAnimatedModel model) {
            if (!(model is AnimatedImageModel am))
                throw new InvalidOperationException("Illegal usage transformer");

            double rotateAngleDelta = 360.0 / am.TotalFrames; // 360° / TotalFrames
            if (!am.AnimeDirection)
                rotateAngleDelta = -rotateAngleDelta;

          //am.RotateAngle = am.CurrentFrame * rotateAngleDelta; // не враховує початкове значення кута
            am.RotateAngle += rotateAngleDelta;
        }

    }

}
