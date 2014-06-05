using System;
using Windows.UI.Core;
using Windows.UI.Xaml.Media;
using FastMines.Common;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.res;
using ua.ksn.fmg.view.win_rt.res.img;

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
      private readonly ua.ksn.geom.Size _sizeField;
      private const int Area = 3000;
      private MosaicsImg _mosaicsImg;

      public FmDataItem(EMosaic eMosaic, FmDataGroup group)
         : base(eMosaic, eMosaic.GetDescription(false), (ImageSource) null) {
         this._group = group;
         this.Subtitle = "Subtitle item " + eMosaic.GetDescription(false);
         this.Description = "Description item ...";
         _sizeField = eMosaic.SizeIcoField(true);
         _sizeField.height += _random.Next() & 3;
         _sizeField.width += _random.Next() & 3;

         AsyncRunner.InvokeLater(async () => { base.Image = await Resources.GetImgMosaicPng(eMosaic, true); }, CoreDispatcherPriority.High);
      }

      private FmDataGroup _group;

      public FmDataGroup Group {
         get { return this._group; }
         set { this.SetProperty(ref this._group, value); }
      }

      public override ImageSource Image {
         get {
            if (_mosaicsImg == null)
               AsyncRunner.InvokeLater(() => {
                  _mosaicsImg = Resources.GetImgMosaic(UniqueId, _sizeField, Area, Resources.DefaultBkColor, new Size(7, 7));
                  base.Image = _mosaicsImg.GetImage(true);
               }, CoreDispatcherPriority.Low);
            return base.Image;
         }
      }
   }
}