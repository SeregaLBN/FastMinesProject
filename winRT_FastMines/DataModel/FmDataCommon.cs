using System;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {
   /// <summary>
   /// Base class for <see cref="FmDataItem"/> and <see cref="FmDataGroup"/> that defines properties common to both.
   /// </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class FmDataCommon<T> : FastMines.Common.BindableBase {
      private static Uri _baseUri = new Uri("ms-appx:///");

      public FmDataCommon(T uniqueId, String title, String imagePath) {
         this._uniqueId = uniqueId;
         this._title = title;
         this._imagePath = imagePath;

         Subtitle = "Subtitle...";
         Description = "Description...";
      }

      private T _uniqueId;
      public virtual T UniqueId {
         get { return this._uniqueId; }
         set { this.SetProperty(ref this._uniqueId, value); }
      }

      private string _title = string.Empty;
      public string Title {
         get { return this._title; }
         set { this.SetProperty(ref this._title, value); }
      }

      public string Subtitle { get; set; }
      public string Description { get; set; }

      protected ImageSource _image = null;
      private String _imagePath = null;
      public virtual ImageSource Image {
         get {
            if (this._image == null && this._imagePath != null)
               this._image = new BitmapImage(new Uri(FmDataCommon<T>._baseUri, this._imagePath));
            return this._image;
         }

         //set {
         //   this._imagePath = null;
         //   this.SetProperty(ref this._image, value);
         //}
      }

      public string ImagePath {
         get { return this._imagePath; }
         set {
            this._image = null;
            this.SetProperty(ref this._imagePath, value);
         }
      }

      public override string ToString() {
         return this.Title;
      }
   }
}