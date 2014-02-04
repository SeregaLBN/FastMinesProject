using System;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using Windows.ApplicationModel.Resources.Core;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.res.img;

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
   public class FmDataItem : FmDataCommon {
      public FmDataItem(EMosaic eMosaic, FmDataGroup group)
         : base(eMosaic.getIndex(), eMosaic.getDescription(false), "res/Mosaic/32x32/" + eMosaic.getDescription(true) + ".png") {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.getDescription(false);
         this.Description = "Description item ...";
      }

      private FmDataGroup _group;
      public FmDataGroup Group {
         get { return this._group; }
         set { this.SetProperty(ref this._group, value); }
      }

      public override ImageSource Image {
         get {
            if (this._image == null)
               try {
                  this._image = new MosaicsImg(EMosaicEx.fromIndex(this.UniqueId), false).Image;
               } catch (Exception ex) {
                  System.Diagnostics.Debug.Assert(true, ex.Message);
                  int maxX  = 1024, maxY = 1024;
                  var image = BitmapFactory.New(maxX, maxY);

                  using (var ctx = image.GetBitmapContext()) {
                     int[] points = new int[] { 10, 10, 10, maxY, maxX, maxY, maxX, 10 };
                     var clr = 0xFF << 24;//unchecked((int)0xFF000000);
                     image.FillPolygon(points, Windows.UI.Color.FromArgb(0xFF, 0xFF, 0, 0));
                     //image.DrawRectangle(10, 10, maxX, maxY, clr);
                     clr |= 0xFFFFFF;
                     image.DrawLine(10, 10, 200, 200, clr);
                     int wbmp = image.PixelWidth, hbmp = image.PixelHeight;
                     WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, 10, 10, maxY, clr);
                     WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, maxY, maxY, maxY, clr);
                     WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, maxX, maxY, maxX, 10, clr);
                     WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, maxX, 10, 10, 10, clr);

                     this._image = image;
                  }
               }
            return base.Image;
         }
         set {
            base.Image = value;
         }
      }
   }
}