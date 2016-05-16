using System;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using FastMines.Presentation.Notyfier;

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
   public abstract class FmDataCommon<T> : NotifyPropertyChanged {
      private static Uri _baseUri = new Uri("ms-appx:///");

      public FmDataCommon(T uniqueId, String title, string relativeUri)
         : this(uniqueId, title, new BitmapImage(new Uri(_baseUri, relativeUri)))
      {}

      public FmDataCommon(T uniqueId, String title, ImageSource image) {
         this._uniqueId = uniqueId;
         this._title = title;
         this._image = image;

         Subtitle = "Subtitle...";
         Description = "Description...";
      }

      private readonly T _uniqueId;
      public virtual T UniqueId {
         get { return this._uniqueId; }
      }

      private string _title = string.Empty;
      public string Title {
         get { return this._title; }
         set { this.SetProperty(ref this._title, value); }
      }

      public string Subtitle { get; set; }
      public string Description { get; set; }

      private ImageSource _image = null;
      public virtual ImageSource Image {
         get { return this._image; }
         set { this.SetProperty(ref this._image, value); }
      }

      public override string ToString() {
         return this.Title;
      }
   }
}