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
using fmg.uwp.utils.win2d;
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
            ds.FillOval(0, 0, _width, _height, yellowBorder);
         }
         { // поверх него, внутри - градиентный круг
            double padX = _width / 30.0; // offset
            double padY = _height / 30.0; // offset
            using (var brush = new CanvasLinearGradientBrush(_rc, yellowBody.ToWinColor(), yellowBorder.ToWinColor()) {
               StartPoint = new Vector2(0, 0),
               EndPoint = new Vector2(_width, _height),
            }) {
               ds.FillOval(padX, padY, _width - padX * 2, _height - padY * 2, brush);
            }
         }
         { // верхний левый блик
            double padX = _width / 30.0; // offset
            double padY = _height / 30.0; // offset
            double w = _width - padX * 2;
            double h = _height - padY * 2;
            using (var ellipse1 = _rc.CreateEllipse(padX, padY, w, h)) {
               w = 1.13 * _width;
               h = 1.13 * _height;
               using (var ellipse2 = _rc.CreateEllipse(padX, padY, w, h)) {
                  using (var intersect = ellipse1.IntersectExclude(ellipse2)) {
                     ds.FillGeometry(intersect, yellowGlint.ToWinColor()); // Colors.DarkGray
                  }

                  // test
                  //ds.DrawGeometry(ellipse1, Color.Black.ToWinColor());
                  //ds.DrawGeometry(ellipse2, Color.Black.ToWinColor());
               }
            }
         }
         { // нижний правый блик
            double padX = _width / 30.0; // offset
            double padY = _height / 30.0; // offset
            double w1 = _width - padX * 2;
            double h1 = _height - padY * 2;
            using (var ellipse1 = _rc.CreateEllipse(padX, padY, w1, h1)) {
               double w2 = 1.13 * _width;
               double h2 = 1.13 * _height;
               using (var ellipse2 = _rc.CreateEllipse(padX + w1 - w2, padY + h1 - h2, w2, h2)) {
                  using (var intersect = ellipse1.IntersectExclude(ellipse2)) {
                     ds.FillGeometry(intersect, yellowBorder.Darker().ToWinColor());
                  }

                  // test
                  //ds.DrawGeometry(ellipse1, Color.Black.ToWinColor());
                  //ds.DrawGeometry(ellipse2, Color.Black.ToWinColor());
               }
            }
         }
      }

      protected void DrawEyes(CanvasDrawingSession ds) {
         var w = _width / 1000.0f;
         var h = _height / 1000.0f;

         // глаза
         var clr = Color.Black;
         ds.FillEllipse((330 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr.ToWinColor());
         ds.FillEllipse((570 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr.ToWinColor());
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
