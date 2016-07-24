using System;
using System.Collections.Generic;
using fmg.core.types;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> Abstract representable <see cref="EMosaicGroup"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class AMosaicsGroupImg<TImage> : PolarLightsImg<TImage>
      where TImage : class
   {
      protected AMosaicsGroupImg(EMosaicGroup group) {
         _mosaicGroup = group;
      }

      private EMosaicGroup _mosaicGroup;
      public EMosaicGroup MosaicGroup {
         get { return _mosaicGroup; }
         set { SetProperty(ref _mosaicGroup, value); }
      }

      protected IEnumerable<PointDouble> GetCoords() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var vertices = 3 + MosaicGroup.Ordinal(); // vertices count
         var center = new PointDouble(Width / 2.0, Height / 2.0);
         return (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(vertices, sq/2, center, RotateAngle)
          //: FigureHelper.GetRegularStarCoords(4, sq / 2, sq / 5, center, RotateAngle);
          //: FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, RotateAngle);
            : FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0);
      }

   }

}
