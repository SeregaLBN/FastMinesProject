using System;
using System.Collections.Generic;
using fmg.core.types;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class MosaicsGroupImg<TImage> : PolarLightsImg<EMosaicGroup, TImage>
      where TImage : class
   {
      protected MosaicsGroupImg(EMosaicGroup group) : base(group) {}

      public EMosaicGroup MosaicGroup => Entity;

      protected IEnumerable<PointDouble> GetCoords() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var vertices = 3 + MosaicGroup.Ordinal(); // vertices count
         var center = new PointDouble(Width / 2.0, Height / 2.0);
         return (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(vertices, sq/2, center, RotateAngle)
            : FigureHelper.GetRegularStarCoords(4, sq/2, sq/5, center, RotateAngle);
      }

   }

}
