using System;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.draw.mosaics;
using ua.ksn.fmg.view.win_rt.res;
using ua.ksn.fmg.view.win_rt.res.img;
using System.Threading.Tasks;
using ua.ksn;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {

   /// <summary>
   /// Generic item data model.
   /// </summary>
   public class FmDataItem : FmDataCommon<EMosaic> {
      public FmDataItem(EMosaic eMosaic, FmDataGroup group)
         : base(eMosaic, eMosaic.GetDescription(false), "res/Mosaic/32x32/" + eMosaic.GetDescription(true) + ".png") {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.GetDescription(false);
         this.Description = "Description item ...";
      }

      private static WriteableBitmap _failedImage;
      private FmDataGroup _group;

      public FmDataGroup Group {
         get { return this._group; }
         set { this.SetProperty(ref this._group, value); }
      }

      public static async Task<WriteableBitmap> CreateImage(EMosaic eMosaic) {
         try {
            return await new MosaicsImg(eMosaic, true).GetImage();
            //return await Resources.GetImgMosaic(eMosaic, true);
         }
         catch (Exception ex) {
            System.Diagnostics.Debug.Assert(true, ex.Message);
            return GetFailedImage();
         }
      }

      // TODO переделать...
      private static WriteableBitmap GetFailedImage() {
         if (null == _failedImage) {
            int maxX = 1024, maxY = 1024;
            var image = BitmapFactory.New(maxX, maxY);

            using (var ctx = image.GetBitmapContext()) {
               int[] points = new int[] {10, 10, 10, maxY, maxX, maxY, maxX, 10};
               var clr = 0xFF << 24; //unchecked((int)0xFF000000);
               image.FillPolygon(points,
                  Windows.ApplicationModel.DesignMode.DesignModeEnabled
                     ? Color.GREEN.ToWinColor()
                     : Color.RED.ToWinColor());
               //image.DrawRectangle(10, 10, maxX, maxY, clr);
               clr |= 0xFFFFFF;
               image.DrawLine(10, 10, 200, 200, clr);
               int wbmp = image.PixelWidth, hbmp = image.PixelHeight;
               WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, 10, 10, maxY, clr);
               WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, maxY, maxY, maxY, clr);
               WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, maxX, maxY, maxX, 10, clr);
               WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, maxX, 10, 10, 10, clr);

               _failedImage = image;
            }
         }
         return _failedImage;
      }

      public override ImageSource Image {
         get {
            if (Windows.ApplicationModel.DesignMode.DesignModeEnabled)
               try {
                  _image = CreateImage(UniqueId).Result;
               }
               catch {
                  //_image = GetFailedImage();
               }
            return base.Image;
         }
         set {
            base.Image = value;
         }
      }
   }
}