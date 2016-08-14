using System;
using Rect = Windows.Foundation.Rect;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw.img.win2d;
using StaticImg                = fmg.core.img.StaticImg<object>;
using LogoCanvasBmp            = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.        CanvasBitmap     >.CanvasBmp;
using LogoCanvasImgSrc         = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsSkillCanvasBmp    = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.        CanvasBitmap     >.CanvasBmp;
using MosaicsSkillCanvasImgSrc = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsGroupCanvasBmp    = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.        CanvasBitmap     >.CanvasBmp;
using MosaicsGroupCanvasImgSrc = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using MosaicsCanvasBmp         = fmg.uwp.draw.img.win2d.MosaicsImg<Microsoft.Graphics.Canvas.        CanvasBitmap     >.CanvasBmp;
using MosaicsCanvasImgSrc      = fmg.uwp.draw.img.win2d.MosaicsImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using SmileCanvasBmp           = fmg.uwp.draw.img.win2d.Smile<Microsoft.Graphics.Canvas.        CanvasBitmap     >.CanvasBmp;
using SmileCanvasImgSrc        = fmg.uwp.draw.img.win2d.Smile<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>.CanvasImgSrc;
using FlagCanvasBmp            = fmg.uwp.draw.img.win2d.Flag<Microsoft.Graphics.Canvas.        CanvasBitmap      >.CanvasBmp;
using FlagCanvasImgSrc         = fmg.uwp.draw.img.win2d.Flag<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource >.CanvasImgSrc;
using MineCanvasBmp            = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;
using MineCanvasImgSrc         = fmg.uwp.draw.img.win2d.Mine.CanvasImgSrc;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Test.FastMines.Uwp.Images.Win2D {

   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class DemoPage : Page {

      private readonly LogoCanvasBmp            Bmp1;
      private readonly MosaicsSkillCanvasBmp    Bmp2;
      private readonly MosaicsGroupCanvasBmp    Bmp3;
      private readonly MosaicsCanvasBmp         Bmp4;
      private readonly SmileCanvasBmp           Bmp5;
      private readonly FlagCanvasBmp            Bmp6;
      private readonly MineCanvasBmp            Bmp7;

      public LogoCanvasImgSrc            Img1 { get; }
      public MosaicsSkillCanvasImgSrc    Img2 { get; }
      public MosaicsGroupCanvasImgSrc    Img3 { get; }
      public MosaicsCanvasImgSrc         Img4 { get; }
      public SmileCanvasImgSrc           Img5 { get; }
      public FlagCanvasImgSrc            Img6 { get; }
      public MineCanvasImgSrc            Img7 { get; }

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      private static int R(int max) => Rnd.Next(max);
      private static bool Bl => (R(2) == 1); // random bool
      private static int Np => (Bl ? -1 : +1); // negative or positive

      private const int Offset = 25;

      public DemoPage() {
         this.InitializeComponent();
         this.Unloaded += (sender, ev) => {
            Bmp1.Dispose();
            Bmp2.Dispose();
            Bmp3.Dispose();
            Bmp4.Dispose();
          //Bmp5.Dispose();
          //Bmp6.Dispose();
            Bmp7.Dispose();
            Img1.Dispose();
            Img2.Dispose();
            Img3.Dispose();
            Img4.Dispose();
          //Img5.Dispose();
          //Img6.Dispose();
            Img7.Dispose();
         };

         var device = CanvasDevice.GetSharedDevice();
         Bmp1 = new LogoCanvasBmp(canvasControl1);
         Img1 = new LogoCanvasImgSrc(device);
         Bmp2 = new MosaicsSkillCanvasBmp   (ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], canvasControl2);
         Img2 = new MosaicsSkillCanvasImgSrc(ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], device);
         Bmp3 = new MosaicsGroupCanvasBmp   (EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], canvasControl3);
         Img3 = new MosaicsGroupCanvasImgSrc(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], device);
         Bmp4 = new MosaicsCanvasBmp        (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), canvasControl4);
         Img4 = new MosaicsCanvasImgSrc     (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), device);
         Bmp5 = new SmileCanvasBmp   (((SmileCanvasBmp   .EType[])Enum.GetValues(typeof(SmileCanvasBmp   .EType)))[R(Enum.GetValues(typeof(SmileCanvasBmp   .EType)).Length)], canvasControl5);
         Img5 = new SmileCanvasImgSrc(((SmileCanvasImgSrc.EType[])Enum.GetValues(typeof(SmileCanvasImgSrc.EType)))[R(Enum.GetValues(typeof(SmileCanvasImgSrc.EType)).Length)], device);
         Bmp6 = new FlagCanvasBmp(canvasControl6);
         Img6 = new FlagCanvasImgSrc(device);
         Bmp7 = new MineCanvasBmp(canvasControl7);
         Img7 = new MineCanvasImgSrc(device);

         ApplyRandom(Bmp1, canvasControl1);
         ApplyRandom(Bmp2, canvasControl2);
         ApplyRandom(Bmp4, canvasControl3);
         ApplyRandom(Bmp3, canvasControl4);
         ApplyRandom(Img1, null);
         ApplyRandom(Img2, null);
         ApplyRandom(Img3, null);
         ApplyRandom(Img4, null);

         this.Loaded += (sender1, args) => {
            const int o = 2 * Offset;
            Action onSize = () => {
               Bmp1.Size = new Size((int)canvasControl1.Size.Width - o,               (int)canvasControl1.Size.Height - o);
               Bmp2.Size = new Size((int)canvasControl2.Size.Width - o,               (int)canvasControl2.Size.Height - o);
               Bmp3.Size = new Size((int)canvasControl3.Size.Width - o,               (int)canvasControl3.Size.Height - o);
               Bmp4.Size = new Size((int)canvasControl4.Size.Width - o,               (int)canvasControl4.Size.Height - o);
               Bmp5.Width =         (int)canvasControl5.Size.Width - o; Bmp5.Height = (int)canvasControl5.Size.Height - o ;
               Bmp6.Width =         (int)canvasControl6.Size.Width - o; Bmp6.Height = (int)canvasControl6.Size.Height - o ;
               Bmp7.Size = new Size((int)canvasControl7.Size.Width - o,               (int)canvasControl7.Size.Height - o);
            };
            onSize();
            this.SizeChanged += (sender2, ev) => onSize();

            //img1Cntrl.SizeChanged += (sender2, ev) => { Img1.Size = new Size((int)ev.NewSize.Width,               (int)ev.NewSize.Height); };
            //img2Cntrl.SizeChanged += (sender2, ev) => { Img2.Size = new Size((int)ev.NewSize.Width,               (int)ev.NewSize.Height); };
            //img3Cntrl.SizeChanged += (sender2, ev) => { Img3.Size = new Size((int)ev.NewSize.Width,               (int)ev.NewSize.Height); };
            //img4Cntrl.SizeChanged += (sender2, ev) => { Img4.Size = new Size((int)ev.NewSize.Width,               (int)ev.NewSize.Height); };
            //img5Cntrl.SizeChanged += (sender2, ev) => { Img5.Width =         (int)ev.NewSize.Width; Img5.Height = (int)ev.NewSize.Height ; };
            //img6Cntrl.SizeChanged += (sender2, ev) => { Img6.Width =         (int)ev.NewSize.Width; Img6.Height = (int)ev.NewSize.Height ; };
            //img7Cntrl.SizeChanged += (sender2, ev) => { Img7.Size = new Size((int)ev.NewSize.Width,               (int)ev.NewSize.Height); };
         };
      }

      private static void ApplyRandom<TImage>(RotatedImg<TImage> img, CanvasControl canvasControl)
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
            var wh = 175 + R(50);
            img.Size = new Size(wh, wh);
         }

         img.Rotate = true;
         img.RotateAngleDelta = (3 + R(5)) * Np;
         img.RedrawInterval = 50;
         img.BorderWidth = Bl ? 1 : 2;

         var plrImg = img as PolarLightsImg<TImage>;
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

      void canvasControl_Draw1(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp1.Image, new Rect(Offset, Offset, Bmp1.Width, Bmp1.Height)); }
      void canvasControl_Draw2(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp2.Image, new Rect(Offset, Offset, Bmp2.Width, Bmp2.Height)); }
      void canvasControl_Draw3(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp3.Image, new Rect(Offset, Offset, Bmp3.Width, Bmp3.Height)); }
      void canvasControl_Draw4(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp4.Image, new Rect(Offset, Offset, Bmp4.Width, Bmp4.Height)); }
      void canvasControl_Draw5(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp5.Image, new Rect(Offset, Offset, Bmp5.Width, Bmp5.Height)); }
      void canvasControl_Draw6(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp6.Image, new Rect(Offset, Offset, Bmp6.Width, Bmp6.Height)); }
      void canvasControl_Draw7(CanvasControl sender, CanvasDrawEventArgs args) { args.DrawingSession.DrawImage(Bmp7.Image, new Rect(Offset, Offset, Bmp7.Width, Bmp7.Height)); }

      public void Animation(bool enable) {
         Bmp1.Rotate =   Bmp1.PolarLights =
         Bmp2.Rotate = //Bmp2.PolarLights =
         Bmp3.Rotate =   Bmp3.PolarLights =
         Bmp4.Rotate = //Bmp4.PolarLights =
       //Bmp5.Rotate =   Bmp5.PolarLights =
       //Bmp6.Rotate =   Bmp6.PolarLights =
         Bmp7.Rotate =   Bmp7.PolarLights =
         Img1.Rotate =   Img1.PolarLights =
         Img2.Rotate = //Img2.PolarLights =
         Img3.Rotate =   Img3.PolarLights =
         Img4.Rotate = //Img4.PolarLights =
       //Img5.Rotate =   Img5.PolarLights =
       //Img6.Rotate =   Img6.PolarLights =
         Img7.Rotate =   Img7.PolarLights =
            enable;
      }

   }

}
