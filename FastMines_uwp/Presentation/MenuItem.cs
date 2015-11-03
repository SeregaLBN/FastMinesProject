using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.uwp.res.img;
using FastMines.Presentation.Notyfier;

namespace FastMines.Presentation
{
   public class MenuItem : NotifyPropertyChanged
   {
      private MosaicsGroupImg _mosaicGroupImage;
      private string _title;
      private Type _pageType;

      public MosaicsGroupImg MosaicGroupImage
      {
         get { return this._mosaicGroupImage; }
         set
         {
            var old = this._mosaicGroupImage;
            if (SetProperty(ref this._mosaicGroupImage, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnMosaicGroupImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnMosaicGroupImagePropertyChanged;
               }
            }
         }
      }

      public BitmapSource Image => MosaicGroupImage?.Image;

      public string Title
      {
         get { return this._title; }
         set { SetProperty(ref this._title, value); }
      }

      public Type PageType
      {
         get { return this._pageType; }
         set { SetProperty(ref this._pageType, value); }
      }

      private void OnMosaicGroupImagePropertyChanged(object sender, PropertyChangedEventArgs ev)
      {
         var pn = ev.PropertyName;
         if (pn == "Image")
         {
            OnPropertyChanged(this, ev);
         }
      }

   }
}
