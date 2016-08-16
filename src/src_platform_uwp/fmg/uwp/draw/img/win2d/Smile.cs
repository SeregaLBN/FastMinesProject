using System;
using System.Numerics;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Microsoft.Graphics.Canvas.Brushes;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic.win2d;

namespace fmg.uwp.draw.img.win2d {

   /// <summary> Smile image </summary>
   public abstract class Smile<TImage>
      where TImage : DependencyObject, ICanvasResourceCreator
   {

      /// <summary> http://unicode-table.com/blocks/emoticons/
      ///           http://unicode-table.com/blocks/miscellaneous-symbols-and-pictographs/
      /// </summary>
      public enum EType {
         /// <summary> :) ☺ -  White Smiling Face (Незакрашенное улыбающееся лицо) U+263A </summary>
         Face_WhiteSmiling,

         /// <summary> :( 😞 - Disappointed Face (Разочарованное лицо) U+1F61E </summary>
         Face_Disappointed,

         /// <summary> 😀 - Grinning Face (Ухмыляющееся лицо) U+1F600 </summary>
         Face_Grinning,

         /// <summary> 😎 - Smiling Face with Sunglasses (Улыбающееся лицо в солнечных очках) U+1F60E </summary>
         Face_SmilingWithSunglasses,

         /// <summary> 😋 - Face Savouring Delicious Food (Лицо, смакующее деликатес) U+1F60B </summary>
         Face_SavouringDeliciousFood,


         /// <summary> like as Professor: 🎓 - Graduation Cap (Выпускная шапочка) U+1F393 </summary>
         Face_Assistant,

         /// <summary> 👀 - Eyes (Глаза) U+1F440 </summary>
         Eyes_OpenDisabled,

         Eyes_ClosedDisabled,

         Face_EyesOpen,

         Face_WinkingEyeLeft,
         Face_WinkingEyeRight,

         Face_EyesClosed
      }

      protected readonly ICanvasResourceCreator _rc;
      private TImage _img;
      private EType _type;
      private int _width = 100;
      private int _height = 100;

      protected Smile(ICanvasResourceCreator resourceCreator, EType type) {
         _rc = resourceCreator;
         _type = type;
      }

      public TImage Image {
         get {
            if (_img == null) {
               _img = CreateImage();
               Draw();
            }
            return _img;
         }
      }

      public int Width {
         get { return _width; }
         set { _width = value; _img = null; }
      }
      public int Height {
         get { return _height; }
         set { _height = value; _img = null; }
      }
      public EType Type {
         get { return _type; }
         set { _type = value; _img = null; }
      }

      protected abstract TImage CreateImage();

      protected abstract void Draw();

      protected void Draw(CanvasDrawingSession ds, bool fillBk) {
         ds.DrawRectangle(0, 0, Width, Height, Windows.UI.Colors.Red, 1); // test

         // fill background (only transparent color)
         if (fillBk) {
            //ds.FillRectangle(5, 5, Width - 10, Height - 10, Color.Transparent.ToWinColor());
         }

         DrawBody(ds);
         DrawEyes(ds);
         DrawMouth(ds);
      }

      protected void DrawBody(CanvasDrawingSession ds) {
         if (_type == EType.Eyes_OpenDisabled || _type == EType.Eyes_ClosedDisabled)
            return;

         Color yellowBody   = new Color(0xFF, 0xCC, 0x00);
         Color yellowGlint  = new Color(0xFF, 0xFF, 0x33);
         Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);

         { // рисую затемненный круг
            ds.FillEllipseInRect(0, 0, _width, _height, yellowBorder);
         }
         var padX = 0.033 * _width;
         var padY = 0.033 * _height;
         var wInt = _width - 2 * padX;
         var hInt = _height - 2 * padY;
         var wExt = 1.133 * _width;
         var hExt = 1.133 * _height;
         using (var ellipseInternal = _rc.CreateEllipseInRect(padX, padY, wInt, hInt)) {
            { // поверх него, внутри - градиентный круг
               using (var brush = new CanvasLinearGradientBrush(_rc, yellowBody.ToWinColor(), yellowBorder.ToWinColor()) {
                  StartPoint = new Vector2(0, 0),
                  EndPoint = new Vector2(_width, _height),
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
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            switch (_type) {
            case EType.Face_Assistant:
            case EType.Face_SmilingWithSunglasses: {
                  // glasses
                  var strokeWidth = Math.Max(1, 0.03 * ((_width + _height) / 2.0));
                  var clr = Color.Black;
                  ds.DrawEllipseInRect(0.200 * _width, 0.100 * _height, 0.290 * _width, 0.440 * _height, clr, strokeWidth, css);
                  ds.DrawEllipseInRect(0.510 * _width, 0.100 * _height, 0.290 * _width, 0.440 * _height, clr, strokeWidth, css);
                  // дужки
                  ds.DrawLine(0.746 * _width, 0.148 * _height, 0.885 * _width, 0.055 * _height, clr, strokeWidth, css);
                  ds.DrawArc(_rc, 0.864 * _width, 0.047 * _height, 0.100 * _width, 0.100 * _height, 0, 125, false, clr, strokeWidth, css);
                  ds.DrawLine((1 - 0.746) * _width, 0.148 * _height, (1 - 0.885) * _width, 0.055 * _height, clr, strokeWidth, css);
                  ds.DrawArc(_rc, (1 - 0.864 - 0.100) * _width, 0.047 * _height, 0.100 * _width, 0.100 * _height, 55, 125, false, clr, strokeWidth, css);
               }
               //break; // ! no break
               goto case EType.Face_SavouringDeliciousFood;
            case EType.Face_SavouringDeliciousFood:
            case EType.Face_WhiteSmiling:
            case EType.Face_Grinning: {
                  var clr = Color.Black;
                  ds.FillEllipseInRect(0.270 * _width, 0.170 * _height, 0.150 * _width, 0.300 * _height, clr);
                  ds.FillEllipseInRect(0.580 * _width, 0.170 * _height, 0.150 * _width, 0.300 * _height, clr);
               }
               break;
            case EType.Face_Disappointed: {
                  var strokeWidth = Math.Max(1, 0.02 * ((_width + _height) / 2.0));

                  using (var rcHalfLeft = _rc.CreateRectangle(0, 0, _width / 2.0, _height)) {
                  using (var rcHalfRght = _rc.CreateRectangle(_width / 2.0, 0, _width, _height)) {

                  // глаз/eye
                  using (var ellipseLeft1 = _rc.CreateEllipseInRect(0.417 * _width, 0.050 * _height, 0.384 * _width, 0.400 * _height)) {
                  using (var ellipseRght1 = _rc.CreateEllipseInRect(0.205 * _width, 0.050 * _height, 0.384 * _width, 0.400 * _height)) {
                  using (var areaLeft1 = ellipseLeft1.IntersectExclude(rcHalfLeft)) {
                  using (var areaRght1 = ellipseRght1.IntersectExclude(rcHalfRght)) {
                     ds.FillGeometry(areaLeft1, Color.Red);
                     ds.FillGeometry(areaRght1, Color.Red);
                     ds.DrawGeometry(areaLeft1, Color.Black, strokeWidth, css);
                     ds.DrawGeometry(areaRght1, Color.Black, strokeWidth, css);

                     // зрачок/pupil
                     using (var ellipseLeft2 = _rc.CreateEllipseInRect(0.550 * _width, 0.200 * _height, 0.172 * _width, 0.180 * _height)) {
                     using (var ellipseRght2 = _rc.CreateEllipseInRect(0.282 * _width, 0.200 * _height, 0.172 * _width, 0.180 * _height)) {
                     using (var areaLeft2 = ellipseLeft2.IntersectExclude(rcHalfLeft)) {
                     using (var areaRght2 = ellipseRght2.IntersectExclude(rcHalfRght)) {
                        ds.FillGeometry(areaLeft2, Color.Blue);
                        ds.FillGeometry(areaRght2, Color.Blue);
                        ds.DrawGeometry(areaLeft2, Color.Black, strokeWidth, css);
                        ds.DrawGeometry(areaRght2, Color.Black, strokeWidth, css);
                     }}}}

                     // веко/eyelid
                     using (var ellipseLeft3 = _rc.CreateEllipseInRect(0.441 * _width, -0.236 * _height, 0.436 * _width, 0.560 * _height)) {
                     using (var ellipseRght3 = _rc.CreateEllipseInRect(0.128 * _width, -0.236 * _height, 0.436 * _width, 0.560 * _height)) {
                     using (var rotatedLeft3 = ellipseLeft3.Rotate(new common.geom.PointDouble(0.441 * _width, -0.236 * _height), 30)) {
                     using (var rotatedRght3 = ellipseRght3.Rotate(new common.geom.PointDouble(0.564 * _width, -0.236 * _height), -30)) {
                     using (var areaLeft3 = rotatedLeft3.IntersectExclude(rcHalfLeft)) {
                     using (var areaRght3 = rotatedRght3.IntersectExclude(rcHalfRght)) {
                     using (var areaLeft31 = areaLeft1.Intersect(areaLeft3)) {
                     using (var areaRght31 = areaRght1.Intersect(areaRght3)) {
                        ds.FillGeometry(areaLeft31, Color.Green);
                        ds.FillGeometry(areaRght31, Color.Green);
                        ds.DrawGeometry(areaLeft31, Color.Black, strokeWidth, css);
                        ds.DrawGeometry(areaRght31, Color.Black, strokeWidth, css);
                     }}}}}}}}
                  }}}}}}

                  // nose
                  using (var nose = _rc.CreateEllipseInRect(0.415 * _width, 0.400 * _height, 0.170 * _width, 0.170 * _height)) {
                     ds.FillGeometry(nose, Color.Green);
                     ds.DrawGeometry(nose, Color.Black, strokeWidth, css);
                  }
               }
               break;
            case EType.Eyes_OpenDisabled:
               //eyeOpened(g, true, true);
               //eyeOpened(g, false, true);
               break;
            case EType.Eyes_ClosedDisabled:
               //eyeClosed(g, true, true);
               //eyeClosed(g, false, true);
               break;
            case EType.Face_EyesOpen:
               //eyeOpened(g, true, false);
               //eyeOpened(g, false, false);
               break;
            case EType.Face_WinkingEyeLeft:
               //eyeClosed(g, true, false);
               //eyeOpened(g, false, false);
               break;
            case EType.Face_WinkingEyeRight:
               //eyeOpened(g, true, false);
               //eyeClosed(g, false, false);
               break;
            case EType.Face_EyesClosed:
               //eyeClosed(g, true, false);
               //eyeClosed(g, false, false);
               break;
            default:
               throw new NotImplementedException();
            }
         }
      }

      protected void DrawMouth(CanvasDrawingSession ds) {
         var w = _width / 1000.0f;
         var h = _height / 1000.0f;

         // smile
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            var clr = Color.Black;
            using (var g = _rc.BuildArc(103 * w, -133 * h, 795 * w, 1003 * h, 207, 126, false)) {
               ds.DrawGeometry(g, clr.ToWinColor(), Math.Max(1, 14 * (w + h) / 2), css);
            }

            // ямочки на щеках
            using (var g = _rc.BuildArc(90 * w, 580 * h, 180 * w, 180 * h, 90, 45, false)) {
               ds.DrawGeometry(g, clr.ToWinColor(), Math.Max(1, 14 * (w + h) / 2), css);
            }
            using (var g = _rc.BuildArc(730 * w, 580 * h, 180 * w, 180 * h, 45, 45, false)) {
               ds.DrawGeometry(g, clr.ToWinColor(), Math.Max(1, 14 * (w + h) / 2), css);
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> Smile image
      /// <br/>
      /// CanvasBitmap impl
      /// </summary>
      public class CanvasBmp : Smile<CanvasBitmap> {

         public CanvasBmp(EType type, ICanvasResourceCreator resourceCreator)
            : base(resourceCreator, type)
         { }

         protected override CanvasBitmap CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasRenderTarget(_rc, Width, Height, dpi);
         }

         protected override void Draw() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               Draw(ds, true);
            }
         }

      }

      /// <summary> Smile image
      /// <br/>
      /// CanvasImageSource impl (XAML ImageSource compatible)
      /// </summary>
      public class CanvasImgSrc : Smile<CanvasImageSource> {

         public CanvasImgSrc(EType type, ICanvasResourceCreator resourceCreator /* = CanvasDevice.GetSharedDevice() */)
            : base(resourceCreator, type)
         { }

         protected override CanvasImageSource CreateImage() {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            return new CanvasImageSource(_rc, Width, Height, dpi);
         }

         protected override void Draw() {
            using (var ds = Image.CreateDrawingSession(Color.Transparent.ToWinColor())) {
               Draw(ds, false);
            }
         }

      }

   }

}
