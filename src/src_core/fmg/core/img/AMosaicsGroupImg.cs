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

         if (MosaicGroup != EMosaicGroup.eOthers)
            return FigureHelper.GetRegularPolygonCoords(vertices, sq / 2, center, RotateAngle);

         //return FigureHelper.GetRegularStarCoords(4, sq / 2, sq / 5, center, RotateAngle);
         //return FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, RotateAngle);
         //return FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0);
         var m = _n_to_m_array[(_n_to_m_index+1) % _n_to_m_array.Length];
         var n = (_incrementSpeedAngle >= 180) ? m : _n_to_m_array[_n_to_m_index];
         return FigureHelper.GetFlowingToTheRightPolygonCoords(n, m, sq / 2, center, _incrementSpeedAngle, 0);//.RotateBySide(2, center, 0);
      }

      protected Tuple<IEnumerable<PointDouble>, IEnumerable<PointDouble>> GetDoubleCoords() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var vertices = 3 + MosaicGroup.Ordinal(); // vertices count
         var center = new PointDouble(Width / 2.0, Height / 2.0);
         return new Tuple<IEnumerable<PointDouble>, IEnumerable<PointDouble>>(
            FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0),
            FigureHelper.GetFlowingToTheRightPolygonCoords(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0));
      }

      private readonly int[] _n_to_m_array = { 3, 4, 6 }; //  triangle -> quadrangle -> hexagon -> anew triangle -> ...
      private int _n_to_m_index = 0;
      private double _incrementSpeedAngle;

      protected override void OnTimer() {
         if (Rotate) {
            bool castling = false;
            var incrementSpeedAngle = _incrementSpeedAngle + 3*RotateAngleDelta;
            if (incrementSpeedAngle >= 360) {
               incrementSpeedAngle -= 360;
               castling = true;
            } else {
               if (incrementSpeedAngle < 0) {
                  incrementSpeedAngle += 360;
                  castling = true;
               }
            }
            _incrementSpeedAngle = incrementSpeedAngle;
            if (castling)
               _n_to_m_index = ++_n_to_m_index % _n_to_m_array.Length;
         }
         base.OnTimer();
      }

   }

}
