using System;

namespace Fmg.Core.Img {

    /// <summary> Representable <see cref="Fmg.Core.Types.EMosaic"/> as animated image </summary>
    public class MosaicRotateTransformer<TImage> : IModelTransformer
        where TImage : class
    {
        public void Execute(IAnimatedModel model) {
            if (!(model is MosaicAnimatedModel<TImage> mam))
                throw new InvalidOperationException("Illegal usage transformer");

            double rotateAngleDelta = 360.0 / mam.TotalFrames; // 360° / TotalFrames
            //if (!mam.AnimeDirection)
            //    rotateAngleDelta = -rotateAngleDelta;
            double rotateAngle = mam.CurrentFrame * rotateAngleDelta;
            mam.RotateAngle = rotateAngle;

            switch (mam.RotateMode) {
            case EMosaicRotateMode.fullMatrix:
                mam.RotateMatrix();
                break;
            case EMosaicRotateMode.someCells:
                mam.UpdateAnglesOffsets(rotateAngleDelta);
                mam.RotateCells();
                break;
            }
        }

    }

}
