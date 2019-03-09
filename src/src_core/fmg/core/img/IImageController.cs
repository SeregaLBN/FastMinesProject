using System;
using System.ComponentModel;
using fmg.common.geom;

namespace fmg.core.img {

    /// <summary> Image MVC: controller </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageView">image view</typeparam>
    /// <typeparam name="TImageModel">image model</typeparam>
    public interface IImageController<out TImage, out TImageView, out TImageModel> : INotifyPropertyChanged, IDisposable
        where TImage      : class
        where TImageView  : IImageView<TImage, TImageModel>
        where TImageModel : IImageModel
    {

        TImageModel Model { get; }
        TImage      Image { get; }
        SizeDouble  Size  { get; }

    }

}
