using System;
using fmg.core.types;

namespace fmg.core.img {

   /// <summary> Transforming of {@link MosaicGroupModel} </summary>
   public class MosaicGroupTransformer : IModelTransformer {

      public void Execute(int currentFrame, int totalFrames, IImageModel model) {
         MosaicGroupModel m = model as MosaicGroupModel;
         if (m == null)
            throw new Exception("Illegal usage transformer");

         if (MosaicGroupModel.varMosaicGroupAsValueOthers1 && (m.MosaicGroup == EMosaicGroup.eOthers)) {
            bool castling = false;
            double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
            if (!m.AnimeDirection)
               rotateAngleDelta = -rotateAngleDelta;
            double incrementSpeedAngle = m.IncrementSpeedAngle + 3*rotateAngleDelta;
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
               m.NmIndex1 = (m.getNmIndex1()+1) % m.getNmArray().Length;
               m.NmIndex2 = (m.getNmIndex2()+1) % m.getNmArray().Length;
            }
         }
      }

   }

}
