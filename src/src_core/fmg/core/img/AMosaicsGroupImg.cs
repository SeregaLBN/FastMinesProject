using System;
using System.Linq;
using System.Collections.Generic;
using fmg.core.types;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.common;

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
         var vertices = 3 + MosaicGroup.Ordinal(); // verticles count
         var center = new PointDouble(Width / 2.0, Height / 2.0);

         if (MosaicGroup != EMosaicGroup.eOthers)
            return FigureHelper.GetRegularPolygonCoords(vertices, sq / 2, center, RotateAngle);

         //return FigureHelper.GetRegularStarCoords(4, sq / 2, sq / 5, center, RotateAngle);
         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices + 1, sq / 2, center, RotateAngle, RotateAngle);
         //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices + 1, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, 0);
         var m = _nmArray[(_nmIndex1+1) % _nmArray.Length];
         var n = (_incrementSpeedAngle >= 180) ? m : _nmArray[_nmIndex1];
         return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(n, m, sq / 2, center, _incrementSpeedAngle, 0);//.RotateBySide(2, center, 0);
      }

      protected Tuple<IEnumerable<PointDouble>, IEnumerable<PointDouble>> GetDoubleCoords() {
         double sq = Math.Min( // size inner square
            Width - Padding.LeftAndRight,
            Height - Padding.TopAndBottom);
         var center = new PointDouble(Width / 2.0, Height / 2.0);


         var isa = _incrementSpeedAngle;
         var m1 = _nmArray[(_nmIndex1 + 1) % _nmArray.Length];
         var n1 = (isa >= 180) ? m1 : _nmArray[_nmIndex1];
         var m2 = _nmArray[(_nmIndex2 + 1) % _nmArray.Length];
         var n2 = (isa >= 180) ? m2 : _nmArray[_nmIndex2];
         var ra = RotateAngle;

         var sideNum = 2;
         var sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

         // высчитываю координаты двух фигур.
         // с одинаковым размером одной из граней.
         var res1 = FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(n1, m1, sizeSide, sideNum, center, isa, 0)
                  .RotateBySide(sideNum, center, ra)
                  .ToList();
         var res2 = FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(n2, m2, sizeSide, sideNum, center, isa, 0)
                  .RotateBySide(sideNum, center, ra+180) // +180° - разворачиваю вторую фигуру, чтобы не пересекалась с первой фигурой
                  .ToList();

         // и склеиваю грани:
         //  * нахожу середины граней
         var p11 = res1[sideNum - 1]; var p12 = res1[sideNum];
         var p21 = res2[sideNum - 1]; var p22 = res2[sideNum];
         var centerPoint1 = new PointDouble((p11.X + p12.X) / 2, (p11.Y + p12.Y) / 2);
         var centerPoint2 = new PointDouble((p21.X + p22.X) / 2, (p21.Y + p22.Y) / 2);

         //  * и совмещаю их по центру изображения
         var offsetToCenter1 = new PointDouble(center.X - centerPoint1.X, center.Y - centerPoint1.Y);
         var offsetToCenter2 = new PointDouble(center.X - centerPoint2.X, center.Y - centerPoint2.Y);
         return new Tuple<IEnumerable<PointDouble>, IEnumerable<PointDouble>>(
               res1.Move(offsetToCenter1),
               res2.Move(offsetToCenter2)
            );
      }

      private readonly int[] _nmArray = { 3, 4, 6 }; //  triangle -> quadrangle -> hexagon -> anew triangle -> ...
      private int _nmIndex1 = 0, _nmIndex2 = 1;
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
            if (castling) {
               _nmIndex1 = ++_nmIndex1 % _nmArray.Length;
               _nmIndex2 = ++_nmIndex2 % _nmArray.Length;
            }
         }
         base.OnTimer();
      }

   }

}
