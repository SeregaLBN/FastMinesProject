using System;
using System.Numerics;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Brushes;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic.win2d {

   public static class CanvasExt {

      public static CanvasGeometry BuildLines(this ICanvasResourceCreator resourceCreator, params PointDouble[] p) {
         using (var builder = new CanvasPathBuilder(resourceCreator)) {
            builder.BeginFigure((float)p[0].X, (float)p[0].Y);
            for (var i = 1; i < p.Length; ++i) {
               builder.AddLine((float)p[i].X, (float)p[i].Y);
            }
            builder.EndFigure(CanvasFigureLoop.Closed);
            return CanvasGeometry.CreatePath(builder);
         }
      }

      public static CanvasGeometry BuildLines(this ICanvasResourceCreator resourceCreator, RegionDouble region) {
         using (var builder = new CanvasPathBuilder(resourceCreator)) {
            builder.BeginFigure((float)region.GetPoint(0).X, (float)region.GetPoint(0).Y);
            for (var i = 1; i < region.CountPoints; ++i) {
               builder.AddLine((float)region.GetPoint(i).X, (float)region.GetPoint(i).Y);
            }
            builder.EndFigure(CanvasFigureLoop.Closed);
            return CanvasGeometry.CreatePath(builder);
         }
      }

      public static CanvasLinearGradientBrush CreateGradientPaintBrush(this ICanvasResourceCreator resourceCreator, PointDouble pt1, Color color1, PointDouble pt2, Color color2) {
         return new CanvasLinearGradientBrush(resourceCreator, color1.ToWinColor(), color2.ToWinColor()) {
            StartPoint = pt1.ToWinPoint().ToVector2(),
            EndPoint = pt2.ToWinPoint().ToVector2()
         };
      }

      public static CanvasGeometry BuildArc(this ICanvasResourceCreator resourceCreator, float x, float y, float width, float height, double startAngle, double arcAngle, bool clockwise) {
         using (var builder = new CanvasPathBuilder(resourceCreator)) {
            Vector2[] arcPoints = new Vector2[2] {
                     new Vector2(x, y),
                     new Vector2(x+width, y+height)
               };
            var centerPoint = (arcPoints[0] + arcPoints[1]) / 2;
            var ellipseRadius = (arcPoints[1] - arcPoints[0]) / 2;
            ellipseRadius.X = Math.Abs(ellipseRadius.X);
            ellipseRadius.Y = Math.Abs(ellipseRadius.Y);
            if (!clockwise)
               startAngle = 360 - startAngle - arcAngle;
            var startAngleR = (float)startAngle.ToRadian();
            var sweepAngleR = (float)arcAngle.ToRadian();
            var startPoint = centerPoint + Vector2.Transform(Vector2.UnitX, Matrix3x2.CreateRotation(startAngleR)) * ellipseRadius;

            builder.BeginFigure(startPoint);
            builder.AddArc(centerPoint, ellipseRadius.X, ellipseRadius.Y, startAngleR, sweepAngleR);
            builder.EndFigure(CanvasFigureLoop.Open);

            return CanvasGeometry.CreatePath(builder);
         }
      }

   }

}
