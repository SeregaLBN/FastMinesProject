package fmg.core.img;

import fmg.core.mosaic.IMosaicDrawModel;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
@Deprecated
public interface IMosaicAnimatedModel<TImageInner>
         extends IMosaicDrawModel<TImageInner>, IAnimatedModel
 {

    public enum EMosaicRotateMode {
        /** rotate full matrix (all cells) */
        fullMatrix,
        /** rotate some cells (independently of each other) */
        someCells
    }

    EMosaicRotateMode getRotateMode();
    void setRotateMode(EMosaicRotateMode value);

    /** 0° .. +360° */
    double getRotateAngle();
    void setRotateAngle(double value);

}
