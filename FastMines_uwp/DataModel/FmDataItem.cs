using System;
using Windows.ApplicationModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media;
using FastMines.Common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.res;
using fmg.uwp.res.img;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {

   /// <summary> Generic item data model. </summary>
   public class FmDataItem : FmDataCommon<EMosaic> {
      private static readonly Random _random = new Random(Guid.NewGuid().GetHashCode());
      private readonly Matrisize _sizeField;
      private const int Area = 3000;
      private MosaicsImg _mosaicsImg;

      public FmDataItem(EMosaic eMosaic, FmDataGroup group)
         : base(eMosaic, eMosaic.GetDescription(false), Resources.GetImgMosaicPngSync(eMosaic, true)) {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.GetDescription(false);
         this.Description = "Description item ...";
         _sizeField = eMosaic.SizeIcoField(true);
         _sizeField.n += _random.Next() & 3;
         _sizeField.m += _random.Next() & 3;
      }

      private FmDataGroup _group;

      public FmDataGroup Group { get { return this._group; } set { this.SetProperty(ref this._group, value); } }

      private static MosaicsImg GetMosaicsImage(EMosaic eMosaic, Matrisize sizeField) {
         return Resources.GetImgMosaic(eMosaic, sizeField, Area, Resources.DefaultBkColor, new Bound(7, 7, 7, 7));
      }

      public override ImageSource Image {
         get {
            if (_mosaicsImg == null) {
               Action<bool> func = drawAsync => {
                  _mosaicsImg = GetMosaicsImage(UniqueId, _sizeField);
                  base.Image = drawAsync ? _mosaicsImg.ImageAsync : _mosaicsImg.Image;
               };
               if (DesignMode.DesignModeEnabled)
                  func(false);
               else
                  AsyncRunner.InvokeFromUiLater(() => func(true), CoreDispatcherPriority.Low);
            }
            return base.Image;
         }
      }
   }
}