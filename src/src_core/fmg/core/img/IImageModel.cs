using System;
using System.ComponentModel;
using Fmg.Common.Geom;

namespace Fmg.Core.Img {

    /// <summary> Image MVC: model.
    /// Model of image data/properties/characteristics </summary>
    public interface IImageModel : INotifyPropertyChanged, IDisposable {

        /// <summary> width and height of the displayed part in pixels </summary>
        SizeDouble Size { get; set; }

        /// <summary> inner padding </summary>
        BoundDouble Padding { get; set; }

    }

    public static class IImageModelExt {

        /// <summary> proportionally adjust when resizing </summary>
        public static BoundDouble RecalcPadding(this IImageModel self, BoundDouble paddingOld, SizeDouble sizeNew, SizeDouble sizeOld) {
            return new BoundDouble(paddingOld.Left   * sizeNew.Width  / sizeOld.Width,
                                   paddingOld.Top    * sizeNew.Height / sizeOld.Height,
                                   paddingOld.Right  * sizeNew.Width  / sizeOld.Width,
                                   paddingOld.Bottom * sizeNew.Height / sizeOld.Height);
        }

        public static void CheckSize(this IImageModel self, SizeDouble size) {
            if (size.Width <= 0)
                throw new ArgumentException("Size.width must be positive.");
            if (size.Height <= 0)
                throw new ArgumentException("Size.height must be positive.");
        }

        public static void CheckPadding(this IImageModel self, BoundDouble padding) {
            var size = self.Size;
            if (padding.LeftAndRight >= size.Width)
                throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (padding.TopAndBottom >= size.Height)
                throw new ArgumentException("Padding size is very large. Should be less than Height.");
        }

    }

}
