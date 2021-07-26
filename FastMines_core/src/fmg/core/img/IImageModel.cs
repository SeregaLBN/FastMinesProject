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

        public static void CheckValue(this IImageModel self, SizeDouble size, bool isOffset = false) {
#if DEBUG
            if (double.IsInfinity(size.Width))
                throw new ArgumentException("Bad value of Width - is IsInfinity.");
            if (double.IsInfinity(size.Height))
                throw new ArgumentException("Bad value of Height - is IsInfinity.");

            if (double.IsNaN(size.Width))
                throw new ArgumentException("Bad value of Width - is NaN.");
            if (double.IsNaN(size.Height))
                throw new ArgumentException("Bad value of Height - is NaN.");

            if (isOffset)
                return;

            if (size.Width <= 0)
                throw new ArgumentException("Size.width must be positive.");
            if (size.Height <= 0)
                throw new ArgumentException("Size.height must be positive.");
#endif
        }

        public static void CheckValue(this IImageModel self, BoundDouble padding) {
#if DEBUG
            if (double.IsInfinity(padding.Left))
                throw new ArgumentException("Bad value of left - is IsInfinity.");
            if (double.IsInfinity(padding.Right))
                throw new ArgumentException("Bad value of right - is IsInfinity.");
            if (double.IsInfinity(padding.Top))
                throw new ArgumentException("Bad value of top - is IsInfinity.");
            if (double.IsInfinity(padding.Bottom))
                throw new ArgumentException("Bad value of bottom - is IsInfinity.");

            if (double.IsNaN(padding.Left))
                throw new ArgumentException("Bad value of left - is NaN.");
            if (double.IsNaN(padding.Right))
                throw new ArgumentException("Bad value of right - is NaN.");
            if (double.IsNaN(padding.Top))
                throw new ArgumentException("Bad value of top - is NaN.");
            if (double.IsNaN(padding.Bottom))
                throw new ArgumentException("Bad value of bottom - is NaN.");

            var size = self.Size;
            if (padding.LeftAndRight >= size.Width)
                throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (padding.TopAndBottom >= size.Height)
                throw new ArgumentException("Padding size is very large. Should be less than Height.");
#endif
        }

    }

}
