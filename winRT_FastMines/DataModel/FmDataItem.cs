using System;
using Windows.UI.Core;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.draw.mosaics;
using ua.ksn.fmg.view.win_rt.res;
using ua.ksn.fmg.view.win_rt.res.img;
using System.Threading.Tasks;
using ua.ksn;
using FastMines.Common;

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
         : base(eMosaic, eMosaic.GetDescription(false), (ImageSource)null) {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.GetDescription(false);
         this.Description = "Description item ...";
      }

      private FmDataGroup _group;

      public FmDataGroup Group {
         get { return this._group; }
         set { this.SetProperty(ref this._group, value); }
      }

      public override ImageSource Image {
         get { return base.Image ?? (base.Image = Resources.GetImgMosaic(UniqueId, true, bmp => base.Image = bmp)); }
      }
   }
}