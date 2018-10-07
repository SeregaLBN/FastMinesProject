using System;
using System.ComponentModel;
using fmg.common.geom;

namespace fmg.core.img {

    /// <summary> Image MVC: model.
    /// Model of image data/properties/characteristics </summary>
    public interface IImageModel : INotifyPropertyChanged, IDisposable {

        /// <summary> width and height of the displayed part in pixels </summary>
        SizeDouble Size { get; set; }

    }

}
