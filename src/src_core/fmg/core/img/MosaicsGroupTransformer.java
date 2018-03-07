package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/** Transforming of {@link MosaicsGroupModel} */
public class MosaicsGroupTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, AnimatedImageModel model) {
      if (!(model instanceof MosaicsGroupModel))
         throw new RuntimeException("Illegal usage transformer");

      MosaicsGroupModel m = (MosaicsGroupModel)model;

      if (MosaicsGroupModel.varMosaicGroupAsValueOthers1 && (m.getMosaicGroup() == EMosaicGroup.eOthers)) {
         boolean castling = false;
         double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
         if (!m.getAnimeDirection())
            rotateAngleDelta = -rotateAngleDelta;
         double incrementSpeedAngle = m.getIncrementSpeedAngle() + 3*rotateAngleDelta;
         if (incrementSpeedAngle >= 360) {
            incrementSpeedAngle -= 360;
            castling = true;
         } else {
            if (incrementSpeedAngle < 0) {
               incrementSpeedAngle += 360;
               castling = true;
            }
         }
         m.setIncrementSpeedAngle(incrementSpeedAngle);
         if (castling) {
            m.setNmIndex1((m.getNmIndex1()+1) % m.getNmArray().length);
            m.setNmIndex2((m.getNmIndex2()+1) % m.getNmArray().length);
         }
      }
   }

}
