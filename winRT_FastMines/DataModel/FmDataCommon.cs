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
   public abstract class FmDataCommon : FastMines.Common.BindableBase {
      private static Uri _baseUri = new Uri("ms-appx:///");

      public FmDataCommon(int uniqueId, String title, String imagePath) {
         this._uniqueId = uniqueId;
         this._title = title;
         this._imagePath = imagePath;

         Subtitle = "Subtitle...";
         Description = "Description...";
      }

      private int _uniqueId = 0;
      public int UniqueId {
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
               this._image = new BitmapImage(new Uri(FmDataCommon._baseUri, this._imagePath));
            return this._image;
         }

         set {
            this._imagePath = null;
            this.SetProperty(ref this._image, value);
         }
      }

      public void SetImage(String path) {
         this._image = null;
         this._imagePath = path;
         this.OnPropertyChanged("Image");
      }

      public override string ToString() {
         return this.Title;
      }
   }
}