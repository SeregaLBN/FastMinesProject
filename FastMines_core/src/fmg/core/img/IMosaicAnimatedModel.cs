using Fmg.Core.Mosaic;
using Fmg.Core.Types;

namespace Fmg.Core.Img {

    public enum EMosaicRotateMode {
        /// <summary> rotate full matrix (all cells) </summary>
        fullMatrix,
        /// <summary> rotate some cells (independently of each other) </summary>
        someCells
    }

    /// <summary> Representable <see cref="EMosaic"/> as animated image </summary>
    public interface IMosaicAnimatedModel<out TImageInner>
                   : IMosaicDrawModel<TImageInner>, IAnimatedModel
        where TImageInner : class
    {

        EMosaicRotateMode RotateMode { get; set; }

        /// <summary> 0° .. +360° </summary>
        double RotateAngle { get; set; }
    }

}
