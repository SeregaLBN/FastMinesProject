using System;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
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
               DrawBody();
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

      protected abstract void DrawBody();

      protected void DrawBody(CanvasDrawingSession ds, bool fillBk) {
         var w = _width / 1000.0f;
         var h = _height / 1000.0f;

         ds.DrawRectangle(0, 0, Width, Height, Windows.UI.Colors.Red, 1); // test

         // fill background (only transparent color)
         if (fillBk) {
            //ds.FillRectangle(5, 5, Width - 10, Height - 10, Color.Transparent.ToWinColor());
         }

         // тело смайла
         ds.FillEllipse(Width / 2f, Height / 2f, Width / 2f - 5 * w, Height / 2f - 5 * h, new Color(0xFFFFE600).ToWinColor());

         // глаза
         var clr = new Color(0xFF000000).ToWinColor();
         ds.FillEllipse((330 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr);
         ds.FillEllipse((570 + 98 / 2f) * w, (150 + 296 / 2f) * h, 98 / 2f * w, 296 / 2f * h, clr);

         // smile
         using (var css = new CanvasStrokeStyle {
            StartCap = CanvasCapStyle.Round,
            EndCap = CanvasCapStyle.Round
         }) {
            using (var g = _rc.BuildArc(103 * w, -133 * h, 795 * w, 1003 * h, 207, 126, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
            }

            // ямочки на щеках
            using (var g = _rc.BuildArc(90 * w, 580 * h, 180 * w, 180 * h, 90, 45, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
            }
            using (var g = _rc.BuildArc(730 * w, 580 * h, 180 * w, 180 * h, 45, 45, false)) {
               ds.DrawGeometry(g, clr, Math.Max(1, 14 * (w + h) / 2), css);
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

         protected override void DrawBody() {
            using (var ds = ((CanvasRenderTarget)Image).CreateDrawingSession()) {
               DrawBody(ds, true);
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

         protected override void DrawBody() {
            using (var ds = Image.CreateDrawingSession(Color.Transparent.ToWinColor())) {
               DrawBody(ds, false);
            }
         }

      }

   }

}
