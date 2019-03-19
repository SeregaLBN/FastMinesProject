using System;
using fmg.core.types;

namespace fmg.core.img {

    /// <summary> Transforming of <see cref="MosaicGroupModel"/> </summary>
    public class MosaicGroupTransformer : IModelTransformer {

        public void Execute(IAnimatedModel model) {
            if (!(model is MosaicGroupModel m))
                throw new InvalidOperationException("Illegal usage transformer");

            if (MosaicGroupModel.varMosaicGroupAsValueOthers1 && (m.MosaicGroup == EMosaicGroup.eOthers)) {
                bool castling = false;
                double rotateAngleDelta = 360.0 / m.TotalFrames; // 360° / TotalFrames
                if (!m.AnimeDirection)
                    rotateAngleDelta = -rotateAngleDelta;
                double incrementSpeedAngle = m.IncrementSpeedAngle + 3 * rotateAngleDelta;
                if (incrementSpeedAngle >= 360) {
                    incrementSpeedAngle -= 360;
                    castling = true;
                } else {
                    if (incrementSpeedAngle < 0) {
                        incrementSpeedAngle += 360;
                        castling = true;
                    }
                }
                m.IncrementSpeedAngle = incrementSpeedAngle;
                if (castling) {
                    m.NmIndex1 = (m.NmIndex1 + 1) % m.NmArray.Length;
                    m.NmIndex2 = (m.NmIndex2 + 1) % m.NmArray.Length;
                }
            }
        }

    }

}
