package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/** Transforming of {@link MosaicGroupModel} */
public class MosaicGroupTransformer implements IModelTransformer {

   @Override
   public void execute(IAnimatedModel model) {
      if (!(model instanceof MosaicGroupModel))
         throw new RuntimeException("Illegal usage transformer");

      MosaicGroupModel m = (MosaicGroupModel)model;

      if (MosaicGroupModel.varMosaicGroupAsValueOthers1 && (m.getMosaicGroup() == EMosaicGroup.eOthers)) {
         boolean castling = false;
         double rotateAngleDelta = 360.0 / m.getTotalFrames(); // 360° / TotalFrames
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
