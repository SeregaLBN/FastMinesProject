using System;
using Windows.UI.Xaml.Media;
using fmg.uwp.res.img;

namespace FastMines.Presentation
{
   public class MenuItem : NotifyPropertyChanged
   {
      private MosaicsGroupImg _mosaicGroupImage;
      private string _icon;
      private string _title;
      private Type _pageType;


      public MosaicsGroupImg MosaicGroupImage
      {
         get { return this._mosaicGroupImage; }
         set
         {
            if (Set(ref this._mosaicGroupImage, value)) {
               _mosaicGroupImage.PropertyChanged += (sender, ev) => {
                  var pn = ev.PropertyName;
                  if (pn == "Image") {
                     OnPropertyChanged(pn);
                  }
               };
            }
         }
      }

      public ImageSource Image => MosaicGroupImage?.Image;

      public string Icon
      {
         get { return this._icon; }
         set { Set(ref this._icon, value); }
      }

      public string Title
      {
         get { return this._title; }
         set { Set(ref this._title, value); }
      }

      public Type PageType
      {
         get { return this._pageType; }
         set { Set(ref this._pageType, value); }
      }
   }
}
