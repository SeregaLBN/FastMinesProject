using System;
using Windows.ApplicationModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media;
using FastMines.Common;
using fmg.common.geom;
using fmg.core.types;
using fmg.winrt.res;
using fmg.winrt.res.img;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {

   /// <summary> Generic item data model. </summary>
   public class FmDataItem : FmDataCommon<EMosaic> {
      private static readonly Random _random = new Random();
      private readonly Size _sizeField;
      private const int Area = 3000;
      private MosaicsImg _mosaicsImg;

      public FmDataItem(EMosaic eMosaic, FmDataGroup group)
         : base(eMosaic, eMosaic.GetDescription(false), Resources.GetImgMosaicPngSync(eMosaic, true)) {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.GetDescription(false);
         this.Description = "Description item ...";
         _sizeField = eMosaic.SizeIcoField(true);
         _sizeField.height += _random.Next() & 3;
         _sizeField.width += _random.Next() & 3;
      }

      private FmDataGroup _group;

      public FmDataGroup Group { get { return this._group; } set { this.SetProperty(ref this._group, value); } }

      private static MosaicsImg GetMosaicsImage(EMosaic eMosaic, Size sizeField) {
         return Resources.GetImgMosaic(eMosaic, sizeField, Area, Resources.DefaultBkColor, new Size(7, 7));
      }

      public override ImageSource Image {
         get {
            if (_mosaicsImg == null) {
               Action<bool> func = drawAsync => {
                  _mosaicsImg = GetMosaicsImage(UniqueId, _sizeField);
                  base.Image = _mosaicsImg.GetImage(drawAsync);
               };
               if (DesignMode.DesignModeEnabled)
                  func(false);
               else
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
                  AsyncRunner.InvokeLater(() => func(true), CoreDispatcherPriority.Low);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            }
            return base.Image;
         }
      }
   }
}