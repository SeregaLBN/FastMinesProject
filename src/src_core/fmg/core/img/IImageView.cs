using System;
using System.ComponentModel;
using fmg.common.geom;

namespace fmg.core.img {

   ///<summary>
   /// Image MVC: view (displayed view)
   ///
   /// @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
   /// @param <TImageModel> model data for display
   /// </summary>
   public interface IImageView<TImage, TImageModel> : INotifyPropertyChanged, IDisposable
      where TImage : class
      where TImageModel : IImageModel
   {

      ///</summary> model data for display </summary>
      TImageModel Model { get; }

      ///</summary> image size in pixels </summary>
      SizeDouble Size { get; set; }

      ///</summary> plaform specific view/image/picture or other display context/canvas/window/panel </summary>
      TImage Image { get; }

      ///</summary> Mark the need to redraw the picture.
      /// Performs a call to the inner draw method (synchronously or asynchronously or implicitly, depending on the implementation) </summary>
      void Invalidate();

   }

}
