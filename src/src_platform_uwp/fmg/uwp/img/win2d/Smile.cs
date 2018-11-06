using System;
using System.Numerics;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Brushes;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.uwp.mosaic.win2d;
using static fmg.core.img.SmileModel;
using Color = fmg.common.Color;

namespace fmg.uwp.img.win2d {

   /// <summary> Smile images </summary>
   public static class Smile {

      /// <summary> Smile images. Base view Win2D implementation </summary>
      public abstract class SmileImageView<TImage> : ImageView<TImage, SmileModel>
         where TImage : DependencyObject, ICanvasResourceCreator
      {

         protected readonly ICanvasResourceCreator _rc;

         protected SmileImageView(EFaceType faceType, ICanvasResourceCreator resourceCreator)
            : base(new SmileModel(faceType))
         {
            _rc = resourceCreator;
         }

         static SmileImageView() {
            StaticInitializer.Init();
         }

         protected void Draw(CanvasDrawingSession ds, bool fillBk) {
            // fill background (only transparent color)
            if (fillBk)
               ds.Clear(Colors.Transparent);

            //ds.DrawRectangle(0, 0, (float)Model.Size.Width, (float)Model.Size.Height, Windows.UI.Colors.Red, 1); // test

            DrawBody(ds);
            DrawEyes(ds);
            DrawMouth(ds);
         }

         protected void DrawBody(CanvasDrawingSession ds) {
            var sm = Model;
            var type = sm.FaceType;
            if (type == EFaceType.Eyes_OpenDisabled || type == EFaceType.Eyes_ClosedDisabled)
               return;

            var size = Size;
            double width = size.Width;
            double height = size.Height;

            Color yellowBody   = new Color(0xFF, 0xCC, 0x00);
            Color yellowGlint  = new Color(0xFF, 0xFF, 0x33);
            Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);

            { // рисую затемненный круг
               ds.FillEllipseInRect(0, 0, width, height, yellowBorder);
            }
            var padX = 0.033 * width;
            var padY = 0.033 * height;
            var wInt = width - 2 * padX;
            var hInt = height - 2 * padY;
            var wExt = 1.133 * width;
            var hExt = 1.133 * height;
            using (var ellipseInternal = _rc.CreateEllipseInRect(padX, padY, wInt, hInt)) {
               { // поверх него, внутри - градиентный круг
                  using (var brush = new CanvasLinearGradientBrush(_rc, yellowBody.ToWinColor(), yellowBorder.ToWinColor()) {
                     StartPoint = new Vector2(0, 0),
                     EndPoint = new Vector2((float)width, (float)height),
                  }) {
                     //ds.FillOval(padX, padY, wInt, hInt, brush); // не совпадает с аналогичным _rc.CreateEllipse...
                     ds.FillGeometry(ellipseInternal, brush);
                  }
               }
               { // верхний левый блик
                  using (var ellipseExternal = _rc.CreateEllipseInRect(padX, padY, wExt, hExt)) {
                     using (var intersect = ellipseInternal.IntersectExclude(ellipseExternal)) {
                        ds.FillGeometry(intersect, yellowGlint); // Colors.DarkGray
                     }

                     // test
                     //ds.DrawGeometry(ellipseInternal, Color.Black);
                     //ds.DrawGeometry(ellipseExternal, Color.Black);
                  }
               }
               { // нижний правый блик
                  using (var ellipseExternal = _rc.CreateEllipseInRect(padX + wInt - wExt, padY + hInt - hExt, wExt, hExt)) {
                     using (var intersect = ellipseInternal.IntersectExclude(ellipseExternal)) {
                        ds.FillGeometry(intersect, yellowBorder.Darker(0.4));
                     }

                     // test
                     //ds.DrawGeometry(ellipseInternal, Color.Black);
                     //ds.DrawGeometry(ellipseExternal, Color.Black);
                  }
               }
            }
         }

         protected void DrawEyes(CanvasDrawingSession ds) {
            var sm = Model;
            var type = sm.FaceType;
            var size = Size;
            double width = size.Width;
            double height = size.Height;

            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Round,
               EndCap = CanvasCapStyle.Round
            }) {
               switch (type) {
               case EFaceType.Face_Assistant:
               case EFaceType.Face_SmilingWithSunglasses: {
                     // glasses
                     var strokeWidth = Math.Max(1, 0.03 * ((width + height) / 2.0));
                     var clr = Color.Black;
                     ds.DrawEllipseInRect(0.200 * width, 0.100 * height, 0.290 * width, 0.440 * height, clr, strokeWidth, css);
                     ds.DrawEllipseInRect(0.510 * width, 0.100 * height, 0.290 * width, 0.440 * height, clr, strokeWidth, css);
                     // дужки
                     ds.DrawLine(     0.746  * width, 0.148 * height,      0.885  * width, 0.055 * height, clr, strokeWidth, css);
                     ds.DrawLine((1 - 0.746) * width, 0.148 * height, (1 - 0.885) * width, 0.055 * height, clr, strokeWidth, css);
                     ds.DrawArc(_rc,      0.864          * width, 0.047 * height, 0.100 * width, 0.100 * height,  0, 125, false, false, clr, strokeWidth, css);
                     ds.DrawArc(_rc, (1 - 0.864 - 0.100) * width, 0.047 * height, 0.100 * width, 0.100 * height, 55, 125, false, false, clr, strokeWidth, css);
                  }
                  //break; // ! no break
                  goto case EFaceType.Face_SavouringDeliciousFood;
               case EFaceType.Face_SavouringDeliciousFood:
               case EFaceType.Face_WhiteSmiling:
               case EFaceType.Face_Grinning: {
                     var clr = Color.Black;
                     ds.FillEllipseInRect(0.270 * width, 0.170 * height, 0.150 * width, 0.300 * height, clr);
                     ds.FillEllipseInRect(0.580 * width, 0.170 * height, 0.150 * width, 0.300 * height, clr);
                  }
                  break;
               case EFaceType.Face_Disappointed: {
                     var strokeWidth = Math.Max(1, 0.02 * ((width + height) / 2.0));

                     using (var rcHalfLeft = _rc.CreateRectangle(0, 0, width / 2.0, height)) {
                     using (var rcHalfRght = _rc.CreateRectangle(width / 2.0, 0, width, height)) {

                     // глаз/eye
                     using (var ellipseLeft1 = _rc.CreateEllipseInRect(0.417 * width, 0.050 * height, 0.384 * width, 0.400 * height)) {
                     using (var ellipseRght1 = _rc.CreateEllipseInRect(0.205 * width, 0.050 * height, 0.384 * width, 0.400 * height)) {
                     using (var areaLeft1 = ellipseLeft1.IntersectExclude(rcHalfLeft)) {
                     using (var areaRght1 = ellipseRght1.IntersectExclude(rcHalfRght)) {
                        ds.FillGeometry(areaLeft1, Color.Red);
                        ds.FillGeometry(areaRght1, Color.Red);
                        ds.DrawGeometry(areaLeft1, Color.Black, strokeWidth, css);
                        ds.DrawGeometry(areaRght1, Color.Black, strokeWidth, css);

                        // зрачок/pupil
                        using (var ellipseLeft2 = _rc.CreateEllipseInRect(0.550 * width, 0.200 * height, 0.172 * width, 0.180 * height)) {
                        using (var ellipseRght2 = _rc.CreateEllipseInRect(0.282 * width, 0.200 * height, 0.172 * width, 0.180 * height)) {
                        using (var areaLeft2 = ellipseLeft2.IntersectExclude(rcHalfLeft)) {
                        using (var areaRght2 = ellipseRght2.IntersectExclude(rcHalfRght)) {
                           ds.FillGeometry(areaLeft2, Color.Blue);
                           ds.FillGeometry(areaRght2, Color.Blue);
                           ds.DrawGeometry(areaLeft2, Color.Black, strokeWidth, css);
                           ds.DrawGeometry(areaRght2, Color.Black, strokeWidth, css);
                        }}}}

                        // веко/eyelid
                        using (var ellipseLeft3 = _rc.CreateEllipseInRect(0.441 * width, -0.236 * height, 0.436 * width, 0.560 * height)) {
                        using (var ellipseRght3 = _rc.CreateEllipseInRect(0.128 * width, -0.236 * height, 0.436 * width, 0.560 * height)) {
                        using (var rotatedLeft3 = ellipseLeft3.Rotate(new PointDouble(0.441 * width, -0.236 * height), 30)) {
                        using (var rotatedRght3 = ellipseRght3.Rotate(new PointDouble(0.564 * width, -0.236 * height), -30)) {
                        using (var areaLeft3 = rotatedLeft3.IntersectExclude(rcHalfLeft)) {
                        using (var areaRght3 = rotatedRght3.IntersectExclude(rcHalfRght)) {
                        using (var areaLeft31 = areaLeft1.Intersect(areaLeft3)) {
                        using (var areaRght31 = areaRght1.Intersect(areaRght3)) {
                           ds.FillGeometry(areaLeft31, Color.Lime);
                           ds.FillGeometry(areaRght31, Color.Lime);
                           ds.DrawGeometry(areaLeft31, Color.Black, strokeWidth, css);
                           ds.DrawGeometry(areaRght31, Color.Black, strokeWidth, css);
                        }}}}}}}}
                     }}}}}}

                     // nose
                     using (var nose = _rc.CreateEllipseInRect(0.415 * width, 0.400 * height, 0.170 * width, 0.170 * height)) {
                        ds.FillGeometry(nose, Color.Lime);
                        ds.DrawGeometry(nose, Color.Black, strokeWidth, css);
                     }
                  }
                  break;
               case EFaceType.Eyes_OpenDisabled:
                  EyeOpened(ds, true, true);
                  EyeOpened(ds, false, true);
                  break;
               case EFaceType.Eyes_ClosedDisabled:
                  EyeClosed(ds, true, true);
                  EyeClosed(ds, false, true);
                  break;
               case EFaceType.Face_EyesOpen:
                  EyeOpened(ds, true, false);
                  EyeOpened(ds, false, false);
                  break;
               case EFaceType.Face_WinkingEyeLeft:
                  EyeClosed(ds, true, false);
                  EyeOpened(ds, false, false);
                  break;
               case EFaceType.Face_WinkingEyeRight:
                  EyeOpened(ds, true, false);
                  EyeClosed(ds, false, false);
                  break;
               case EFaceType.Face_EyesClosed:
                  EyeClosed(ds, true, false);
                  EyeClosed(ds, false, false);
                  break;
               default:
                  throw new NotImplementedException();
               }
            }
         }

         protected void DrawMouth(CanvasDrawingSession ds) {
            var sm = Model;
            var type = sm.FaceType;
            switch (type) {
            case EFaceType.Face_Assistant:
            case EFaceType.Eyes_OpenDisabled:
            case EFaceType.Eyes_ClosedDisabled:
            case EFaceType.Face_EyesOpen:
            case EFaceType.Face_WinkingEyeLeft:
            case EFaceType.Face_WinkingEyeRight:
            case EFaceType.Face_EyesClosed:
               return;
            }

            var size = Size;
            double width = size.Width;
            double height = size.Height;

            using (var css = new CanvasStrokeStyle {
               StartCap = CanvasCapStyle.Round,
               EndCap = CanvasCapStyle.Round
            }) {
               var strokeWidth = Math.Max(1, 0.044 * ((width + height) / 2.0));

               switch (type) {
               case EFaceType.Face_SavouringDeliciousFood:
               case EFaceType.Face_SmilingWithSunglasses:
               case EFaceType.Face_WhiteSmiling: {
                     // smile
                     var arcSmile = _rc.BuildArc(0.103 * width, -0.133 * height, 0.795 * width, 1.003 * height, 207, 126, false, false);
                     ds.DrawGeometry(arcSmile, Color.Black, strokeWidth, css);
                     var lip = _rc.CreateEllipseInRect(0.060 * width, 0.475 * height, 0.877 * width, 0.330 * height);
                     ds.FillGeometry(arcSmile.IntersectExclude(lip), Color.Black);

                     // test
                     //ds.DrawGeometry(lip, Color.Lime.ToWinColor(), 1);

                     // dimples - ямочки на щеках
                     ds.DrawArc(_rc, +0.020 * width, 0.420 * height, 0.180 * width, 0.180 * height, 85 + 180, 57, false, false, Color.Black, strokeWidth, css);
                     ds.DrawArc(_rc, +0.800 * width, 0.420 * height, 0.180 * width, 0.180 * height, 38 + 180, 57, false, false, Color.Black, strokeWidth, css);

                     // tongue / язык
                     if (type == EFaceType.Face_SavouringDeliciousFood) {
                        var tongue = _rc.CreateEllipseInRect(0.470 * width, 0.406 * height, 0.281 * width, 0.628 * height).Rotate(
                                             new PointDouble(0.470 * width, 0.406 * height), 40);
                        var ellipseSmile = _rc.CreateEllipseInRect(0.103 * width, -0.133 * height, 0.795 * width, 1.003 * height);
                        ds.FillGeometry(tongue.IntersectExclude(ellipseSmile), Color.Red);
                     }
                  }
                  break;
               case EFaceType.Face_Disappointed: {
                     // smile
                     var arcSmile = _rc.BuildArc(0.025 * width, 0.655 * height, 0.950 * width, 0.950 * height, 50, 80, false, false);
                     ds.DrawGeometry(arcSmile, Color.Black, strokeWidth, css);
                     arcSmile = _rc.CreateEllipseInRect(0.025 * width, 0.655 * height, 0.950 * width, 0.950 * height); // arc as circle

                     // tongue / язык
                     var tongue = _rc.CreateEllipseInRect(0.338 * width, 0.637 * height, 0.325 * width, 0.325 * height).IntersectInclude( // кончик языка
                                  _rc.CreateRectangle    (0.338 * width, 0.594 * height, 0.325 * width, 0.206 * height)); // тело языка
                     var hole = _rc.CreateRectangle(0, 0, width, height).IntersectExclude(arcSmile);
                     tongue = tongue.IntersectExclude(hole);
                     ds.FillGeometry(tongue, Color.Red);
                     ds.DrawGeometry(tongue, Color.Black, strokeWidth, css);
                     ds.DrawGeometry(_rc.CreateRectangle(width / 2.0, 0.637 * height, 0.0001, 0.200 * height).IntersectExclude(hole), Color.Black, strokeWidth, css);

                     // test
                     //ds.DrawGeometry(arcSmile, Color.Black, 1, css);
                     //ds.DrawGeometry(hole, Color.Black, 1, css);
                  }
                  break;
               case EFaceType.Face_Grinning: {
                     var arcSmile = _rc.BuildArc(0.103 * width, -0.133 * height, 0.795 * width, 1.003 * height, 207, 126, false, true);
                     using (var brush = new CanvasLinearGradientBrush(_rc, Color.Gray.ToWinColor(), Color.White.ToWinColor()) {
                        StartPoint = new Vector2(0, 0),
                        EndPoint = new Vector2((float)(width / 2.0), 0),
                     }) {
                      //ds.FillGeometry(_rc.CreateRectangle(0, 0, width, height), brush); // test
                        ds.FillGeometry(arcSmile, brush);
                     }
                     ds.DrawGeometry(arcSmile, Color.Black, strokeWidth, css);
                  }
                  break;
               default:
                  throw new NotImplementedException();
               }
            }
         }

         private void EyeOpened(CanvasDrawingSession ds, bool right, bool disabled) {
            var sm = Model;
            var type = sm.FaceType;
            var size = Size;
            double width = size.Width;
            double height = size.Height;

            Action<PointDouble, Color> draw = (offset, holeColor) => {
               var pupil = right
                     ? _rc.CreateEllipseInRect((offset.X + 0.273) * width, (offset.Y + 0.166) * height, 0.180 * width, 0.324 * height).IntersectInclude(
                       _rc.CreateEllipseInRect((offset.X + 0.320) * width, (offset.Y + 0.124) * height, 0.180 * width, 0.273 * height).Rotate(
                               new PointDouble((offset.X + 0.320) * width, (offset.Y + 0.124) * height),  35)                          ).IntersectInclude(
                       _rc.CreateEllipseInRect((offset.X + 0.163) * width, (offset.Y + 0.313) * height, 0.180 * width, 0.266 * height).Rotate(
                               new PointDouble((offset.X + 0.163) * width, (offset.Y + 0.313) * height), -36))
                     : _rc.CreateEllipseInRect((offset.X + 0.500) * width, (offset.Y + 0.166) * height, 0.180 * width, 0.324 * height).IntersectInclude(
                       _rc.CreateEllipseInRect((offset.X + 0.486) * width, (offset.Y + 0.227) * height, 0.180 * width, 0.273 * height).Rotate(
                               new PointDouble((offset.X + 0.486) * width, (offset.Y + 0.227) * height), -35)                          ).IntersectInclude(
                       _rc.CreateEllipseInRect((offset.X + 0.646) * width, (offset.Y + 0.211) * height, 0.180 * width, 0.266 * height).Rotate(
                               new PointDouble((offset.X + 0.646) * width, (offset.Y + 0.211) * height),  36));
               if (!disabled) {
                  ds.FillGeometry(pupil, Color.Black);
               }
               var hole = right
                     ? _rc.CreateEllipseInRect((offset.X + 0.303 * width), (offset.Y + 0.209) * height, 0.120 * width, 0.160 * height).Rotate(
                               new PointDouble((offset.X + 0.303 * width), (offset.Y + 0.209) * height), 25)
                     : _rc.CreateEllipseInRect((offset.X + 0.610 * width), (offset.Y + 0.209) * height, 0.120 * width, 0.160 * height).Rotate(
                               new PointDouble((offset.X + 0.610 * width), (offset.Y + 0.209) * height), 25);
               if (!disabled) {
                  ds.FillGeometry(hole, holeColor);
               } else {
                  ds.FillGeometry(pupil.IntersectExclude(hole), holeColor);
               }
            };
            if (disabled) {
               draw(new PointDouble(0.034, 0.027), Color.White);
               draw(new PointDouble(), Color.Gray);
            } else {
               draw(new PointDouble(), Color.White);
            }
         }

         private void EyeClosed(CanvasDrawingSession ds, bool right, bool disabled) {
            var size = Size;
            double width = size.Width;
            double height = size.Height;

            Action<PointDouble> eye = offset => {
               if (disabled) {
                  ds.FillGeometry(_rc.CreateEllipseInRect((offset.X + 0.532) * width, (offset.Y + 0.248) * height, 0.313 * width, 0.068 * height).IntersectInclude(
                                  _rc.CreateEllipseInRect((offset.X + 0.655) * width, (offset.Y + 0.246) * height, 0.205 * width, 0.130 * height)), Color.White);
               }
               ds.FillGeometry(_rc.CreateEllipseInRect((offset.X + 0.517) * width, (offset.Y + 0.248) * height, 0.313 * width, 0.034 * height).IntersectInclude(
                               _rc.CreateEllipseInRect((offset.X + 0.640) * width, (offset.Y + 0.246) * height, 0.205 * width, 0.075 * height)), disabled ? Color.Gray : Color.Black);
            };
            eye(right ? new PointDouble(-0.410, 0)
                      : new PointDouble());
         }

         protected override void Disposing() {
            Model.Dispose();
            base.Disposing();
         }

      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Smile image view implementation over <see cref="CanvasBitmap"/> </summary>
      public class CanvasBmp : SmileImageView<CanvasBitmap> {

         public CanvasBmp(EFaceType faceType, ICanvasResourceCreator resourceCreator)
            : base(faceType, resourceCreator)
         { }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var s = Model.Size;
            return new CanvasRenderTarget(_rc, (float)s.Width, (float)s.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               Draw(ds, true);
            }
         }

      }

      /// <summary> Smile image view implementation over <see cref="CanvasImageSource"/> (XAML <see cref="Windows.UI.Xaml.Media.ImageSource"/> compatible) </summary>
      public class CanvasImgSrc : SmileImageView<CanvasImageSource> {

         public CanvasImgSrc(EFaceType faceType, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(faceType, resourceCreator)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var s = Model.Size;
            return new CanvasImageSource(_rc, (float)s.Width, (float)s.Height, dpi);
         }

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(Colors.Transparent)) {
               Draw(ds, false);
            }
         }

      }

      /// <summary> Smile image controller implementation for <see cref="Smile.CanvasBmp"/> </summary>
      public class ControllerBitmap : ImageController<CanvasBitmap, Smile.CanvasBmp, SmileModel> {

         public ControllerBitmap(EFaceType faceType, ICanvasResourceCreator resourceCreator)
            : base(new Smile.CanvasBmp(faceType, resourceCreator)) { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

      /// <summary> Smile image controller implementation for <see cref="Smile.CanvasImgSrc"/> </summary>
      public class ControllerImgSrc : ImageController<CanvasImageSource, Smile.CanvasImgSrc, SmileModel> {

         public ControllerImgSrc(EFaceType faceType, ICanvasResourceCreator resourceCreator)
            : base(new Smile.CanvasImgSrc(faceType, resourceCreator)) { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

   }

}
