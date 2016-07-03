using System;
using Windows.UI.Xaml;
using Rect = Windows.Foundation.Rect;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw.img.win2d;
using StaticImg                = fmg.core.img.StaticImg<object, object>;
using LogoCanvasBmp            = fmg.uwp.draw.img.win2d.Logo           <Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using LogoCanvasImgSrc         = fmg.uwp.draw.img.win2d.Logo           <Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsSkillCanvasBmp    = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using MosaicsSkillCanvasImgSrc = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsGroupCanvasBmp    = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using MosaicsGroupCanvasImgSrc = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsImgCanvasBmp      = fmg.uwp.draw.img.win2d.MosaicsImg     <Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using MosaicsImgCanvasImgSrc   = fmg.uwp.draw.img.win2d.MosaicsImg     <Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Test.FastMines.Uwp.Images.Win2D {

   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class DemoPage : Page {

      private readonly LogoCanvasBmp         _logo;
      private readonly MosaicsSkillCanvasBmp _msi;
      private readonly MosaicsGroupCanvasBmp _mgi;
      private readonly MosaicsImgCanvasBmp   _mi;

      public LogoCanvasImgSrc         DemoImg1 { get; }
      public MosaicsSkillCanvasImgSrc DemoImg2 { get; }
      public MosaicsGroupCanvasImgSrc DemoImg3 { get; }
      public MosaicsImgCanvasImgSrc   DemoImg4 { get; }

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      private static int R(int max) => Rnd.Next(max);
      private static bool Bl => (R(2) == 1); // random bool
      private static int Np => (Bl ? -1 : +1); // negative or positive

      private const int Offset = 25;

      public DemoPage() {
         this.InitializeComponent();
         this.Unloaded += (sender, ev) => {
            _logo.Dispose();
            _msi.Dispose();
            _mgi.Dispose();
            _mi.Dispose();
            DemoImg1.Dispose();
            DemoImg2.Dispose();
            DemoImg3.Dispose();
            DemoImg4.Dispose();
         };

         var device = CanvasDevice.GetSharedDevice();
         _logo    = new LogoCanvasBmp(canvasControl1);
         DemoImg1 = new LogoCanvasImgSrc(device);
         _msi     = new MosaicsSkillCanvasBmp   (ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], canvasControl2);
         DemoImg2 = new MosaicsSkillCanvasImgSrc(ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], device);
         _mgi     = new MosaicsGroupCanvasBmp   (EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], canvasControl4);
         DemoImg3 = new MosaicsGroupCanvasImgSrc(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], device);
         _mi      = new MosaicsImgCanvasBmp     (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), canvasControl3);
         DemoImg4 = new MosaicsImgCanvasImgSrc  (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), device);

         ApplyRandom(_logo, canvasControl1);
         ApplyRandom(_msi , canvasControl2);
         ApplyRandom(_mi  , canvasControl3);
         ApplyRandom(_mgi , canvasControl4);
         ApplyRandom(DemoImg1, null);
         ApplyRandom(DemoImg2, null);
         ApplyRandom(DemoImg3, null);
         ApplyRandom(DemoImg4, null);

         this.Loaded += (sender1, args) => {
            const int o = 2 * Offset;
            Action onSize = () => {
               _logo.Size = new Size((int)canvasControl1.Size.Width - o, (int)canvasControl1.Size.Height - o);
               _msi .Size = new Size((int)canvasControl2.Size.Width - o, (int)canvasControl2.Size.Height - o);
               _mi  .Size = new Size((int)canvasControl3.Size.Width - o, (int)canvasControl3.Size.Height - o);
               _mgi .Size = new Size((int)canvasControl4.Size.Width - o, (int)canvasControl4.Size.Height - o);
            };
            onSize();
            this.SizeChanged += (sender2, ev) => onSize();
         };
      }

      private static void ApplyRandom<T, TImage>(RotatedImg<T, TImage> img, CanvasControl canvasControl)
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         if (canvasControl != null) {
            // TImage is CanvasBitmap
            img.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == nameof(StaticImg.Image))
                  canvasControl.Invalidate();
            };
         } else {
            // TImage is CanvasImageSource
            img.SizeInt = 175 + R(50);
         }

         img.Rotate = true;
         img.RotateAngleDelta = (3 + R(5)) * Np;
         img.RedrawInterval = 50;
         img.BorderWidth = Bl ? 1 : 2;

         var plrImg = img as PolarLightsImg<T, TImage>;
         if (plrImg != null) {
            plrImg.PolarLights = true;
         }

         var logoImg = img as Logo<TImage>;
         if (logoImg != null) {
            var vals = (Logo<TImage>.ERotateMode[])Enum.GetValues(typeof(LogoCanvasBmp.ERotateMode));
            logoImg.RotateMode = vals[R(vals.Length)];
            logoImg.UseGradient = Bl;
         }

         var mosaicsImg = img as MosaicsImg<TImage>;
         if (mosaicsImg != null) {
            var vals = (MosaicsImg<TImage>.ERotateMode[])Enum.GetValues(typeof(MosaicsImg<TImage>.ERotateMode));
            mosaicsImg.RotateMode = vals[R(vals.Length)];
         }

         if (Bl) {
            // test transparent
            var bkClr = new HSV(ColorExt.RandomColor(Rnd)) { a = (byte)(50 + R(10)) };
            img.PropertyChanged += (o, ev) => {
               if (ev.PropertyName == nameof(StaticImg.RotateAngle)) {
                  bkClr.h = img.RotateAngle;
                  img.BackgroundColor = bkClr.ToColor();
               }
            };
         } else {
            img.BackgroundColor = ColorExt.RandomColor(Rnd).Brighter();
         }
      }

      void canvasControl_Draw1(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_logo.Image, new Rect(Offset, Offset, _logo.Width, _logo.Height));
      }
      void canvasControl_Draw2(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_msi.Image, new Rect(Offset, Offset, _msi.Width, _msi.Height));
      }
      void canvasControl_Draw3(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mi.Image, new Rect(Offset, Offset, _mi.Width, _mi.Height));
      }
      void canvasControl_Draw4(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mgi.Image, new Rect(Offset, Offset, _mgi.Width, _mgi.Height));
      }

   }

}
