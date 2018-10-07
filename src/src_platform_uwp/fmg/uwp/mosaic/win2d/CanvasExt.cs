using System;
using System.Linq;
using System.Numerics;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Microsoft.Graphics.Canvas.Brushes;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

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

        public static CanvasGeometry BuildLines(this ICanvasResourceCreator resourceCreator, RegionDouble region, SizeDouble? offset = null) {
            var x = offset?.Width;
            var y = offset?.Height;
            using (var builder = new CanvasPathBuilder(resourceCreator)) {
                builder.BeginFigure((float)(region.GetPoint(0).X + x), (float)(region.GetPoint(0).Y + y));
                for (var i = 1; i < region.CountPoints; ++i) {
                    builder.AddLine((float)(region.GetPoint(i).X + x), (float)(region.GetPoint(i).Y + y));
                }
                builder.EndFigure(CanvasFigureLoop.Closed);
                return CanvasGeometry.CreatePath(builder);
            }
        }

        public static CanvasLinearGradientBrush CreateGradientPaintBrush(this ICanvasResourceCreator resourceCreator, PointDouble pt1, Color color1, PointDouble pt2, Color color2) {
            return CreateGradientPaintBrush(resourceCreator, pt1, color1.ToWinColor(), pt2, color2.ToWinColor());
        }

        public static CanvasLinearGradientBrush CreateGradientPaintBrush(this ICanvasResourceCreator resourceCreator, PointDouble pt1, Windows.UI.Color color1, PointDouble pt2, Windows.UI.Color color2) {
            return new CanvasLinearGradientBrush(resourceCreator, color1, color2) {
                StartPoint = pt1.ToWinPoint().ToVector2(),
                EndPoint = pt2.ToWinPoint().ToVector2()
            };
        }

        public static CanvasGeometry CreatePolygon(this ICanvasResourceCreator resourceCreator, RegionDouble region, SizeDouble? offset = null) {
            var x = offset?.Width;
            var y = offset?.Height;
            return CanvasGeometry.CreatePolygon(resourceCreator, region.Points.Select(p => new Vector2((float)(p.X + x), (float)(p.Y + y))).ToArray());
        }

        public static CanvasGeometry BuildArc(this ICanvasResourceCreator resourceCreator, double x, double y, double width, double height, double startAngle, double arcAngle, bool clockwise, bool loopClosed) {
            using (var builder = new CanvasPathBuilder(resourceCreator)) {
                Vector2[] arcPoints = new Vector2[2] {
                        new Vector2((float)x, (float)y),
                        new Vector2((float)(x+width), (float)(y+height))
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
                builder.EndFigure(loopClosed ? CanvasFigureLoop.Closed : CanvasFigureLoop.Open);

                return CanvasGeometry.CreatePath(builder);
            }
        }

        /// <summary>
        /// Constructs and initializes an Ellipse {CanvasGeometry} from the specified coordinates
        /// </summary>
        /// <param name="resourceCreator"></param>
        /// <param name="x">the X coordinate of the upper-left corner of the framing rectangle</param>
        /// <param name="y">the Y coordinate of the upper-left corner of the framing rectangle</param>
        /// <param name="w">the width of the framing rectangle</param>
        /// <param name="h">the height of the framing rectangle</param>
        /// <returns></returns>
        public static CanvasGeometry CreateEllipseInRect(this ICanvasResourceCreator resourceCreator, double x, double y, double w, double h) {
            return CanvasGeometry.CreateEllipse(resourceCreator, (float)(x + w / 2), (float)(y + h / 2), (float)(w / 2), (float)(h / 2));
        }

        public static CanvasGeometry CreateRectangle(this ICanvasResourceCreator resourceCreator, double x, double y, double w, double h) {
            return CanvasGeometry.CreateRectangle(resourceCreator, (float)x, (float)y, (float)w, (float)h);
        }



        /// <summary>
        /// Fills an oval bounded by the specified rectangle with the specified color
        /// </summary>
        /// <param name="ds"></param>
        /// <param name="x">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="y">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="width">width the width of the oval to be filled</param>
        /// <param name="height">height the height of the oval to be filled</param>
        /// <param name="clr">Fills the interior of a ellipse with the specified color</param>
        public static void FillEllipseInRect(this CanvasDrawingSession ds, double x, double y, double width, double height, Color clr) {
            ds.FillEllipse((float)(x + width / 2), (float)(y + height / 2), (float)(width / 2), (float)(height / 2), clr.ToWinColor());
        }

        /// <summary>
        /// Fills an oval bounded by the specified rectangle with the specified brush
        /// </summary>
        /// <param name="ds"></param>
        /// <param name="x">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="y">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="width">width the width of the oval to be filled</param>
        /// <param name="height">height the height of the oval to be filled</param>
        /// <param name="brush">Fills the interior of a ellipse with the specified brush</param>
        public static void FillEllipseInRect(this CanvasDrawingSession ds, double x, double y, double width, double height, ICanvasBrush brush) {
            ds.FillEllipse((float)(x + width / 2), (float)(y + height / 2), (float)(width / 2), (float)(height / 2), brush);
        }

        /// <summary>
        /// Draw an oval bounded by the specified rectangle with the specified color and stroke
        /// </summary>
        /// <param name="ds"></param>
        /// <param name="x">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="y">coordinate of the upper left corner of the oval to be filled</param>
        /// <param name="width">width the width of the oval to be filled</param>
        /// <param name="height">height the height of the oval to be filled</param>
        /// <param name="color"></param>
        /// <param name="strokeWidth"></param>
        /// <param name="strokeStyle"></param>
        public static void DrawEllipseInRect(this CanvasDrawingSession ds, double x, double y, double width, double height, Color color, double strokeWidth, CanvasStrokeStyle strokeStyle) {
            ds.DrawEllipse((float)(x + width / 2), (float)(y + height / 2), (float)(width / 2), (float)(height / 2), color.ToWinColor(), (float)strokeWidth, strokeStyle);
        }

        public static CanvasGeometry Intersect(this CanvasGeometry shape1, CanvasGeometry shape2) {
            return shape1.CombineWith(shape2, Matrix3x2.CreateTranslation(0, 0), CanvasGeometryCombine.Intersect);
        }
        public static CanvasGeometry IntersectExclude(this CanvasGeometry shape1, CanvasGeometry shape2) {
            return shape1.CombineWith(shape2, Matrix3x2.CreateTranslation(0, 0), CanvasGeometryCombine.Exclude);
        }
        public static CanvasGeometry IntersectInclude(this CanvasGeometry shape1, CanvasGeometry shape2) {
            return shape1.CombineWith(shape2, Matrix3x2.CreateTranslation(0, 0), CanvasGeometryCombine.Union);
        }

        public static void DrawLine(this CanvasDrawingSession ds, double x0, double y0, double x1, double y1, Color color, double strokeWidth, CanvasStrokeStyle strokeStyle) {
            ds.DrawLine((float)x0, (float)y0, (float)x1, (float)y1, color.ToWinColor(), (float)strokeWidth, strokeStyle);
        }

        public static void DrawArc(this CanvasDrawingSession ds, ICanvasResourceCreator resourceCreator,
                                   double x, double y, double w, double h, double start, double extent, bool clockwise, bool loopClosed,
                                   Color color, double strokeWidth, CanvasStrokeStyle strokeStyle) {
            using (var arc = resourceCreator.BuildArc(x, y, w, h, start, extent, clockwise, loopClosed)) {
                ds.DrawGeometry(arc, Color.Black, strokeWidth, strokeStyle);
            }
        }

        public static void FillGeometry(this CanvasDrawingSession ds, CanvasGeometry geometry, Color color) {
            ds.FillGeometry(geometry, color.ToWinColor());
        }

        public static void DrawGeometry(this CanvasDrawingSession ds, CanvasGeometry geometry, Color color, double strokeWidth, CanvasStrokeStyle strokeStyle) {
            ds.DrawGeometry(geometry, color.ToWinColor(), (float)strokeWidth, strokeStyle);
        }

        public static CanvasGeometry Rotate(this CanvasGeometry shape, PointDouble rotatePoint, double angle) {
            Matrix3x2 placeNearOrigin     = Matrix3x2.CreateTranslation(-(float)rotatePoint.X, -(float)rotatePoint.Y);
            Matrix3x2 undoPlaceNearOrigin = Matrix3x2.CreateTranslation( (float)rotatePoint.X,  (float)rotatePoint.Y);
            Matrix3x2 rotate = Matrix3x2.CreateRotation((float)angle.ToRadian());
            return shape.Transform(placeNearOrigin * rotate * undoPlaceNearOrigin);
        }

    }

}
