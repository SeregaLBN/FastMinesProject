using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Core.Types.Draw;

namespace Fmg.Core.Mosaic {

    /// <summary> MVC: draw model of mosaic field. </summary>
    /// <typeparam name="TImageInner">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    public interface IMosaicDrawModel<out TImageInner> : IImageModel, IMosaic
        where TImageInner : class
    {
        bool AutoFit { get; set; }

        /// <summary> get mosaic size in pixels </summary>
        SizeDouble MosaicSize { get; }

        /// <summary> Offset to mosaic.
        /// Определяется Padding'ом  и, дополнительно, смещением к мозаике (т.к. мозаика равномерно вписана в InnerSize) </summary>
        SizeDouble MosaicOffset { get; set; }

        TImageInner ImgMine { get; /*set;*/ }

        TImageInner ImgFlag { get; /*set;*/ }

        ColorText ColorText { get; set; }

        PenBorder PenBorder { get; set; }

        BackgroundFill BkFill { get; set; }

        FontInfo FontInfo { get; set; }

        Color BackgroundColor { get; set; }

        TImageInner ImgBckgrnd { get; /*set;*/ }
    }

}
