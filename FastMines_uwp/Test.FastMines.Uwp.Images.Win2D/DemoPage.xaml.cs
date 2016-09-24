using System;
using System.Linq;
using System.Collections.Generic;
using Rect = Windows.Foundation.Rect;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.ViewManagement;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.data.controller.types;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.img.win2d;
using fmg.uwp.draw.mosaic.win2d;
using StaticCanvasBmp       = fmg.core.img.StaticImg<Microsoft.Graphics.Canvas.        CanvasBitmap     >;
using StaticCanvasImg       = fmg.core.img.StaticImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>;
using LogoCanvasBmp         = fmg.uwp.draw.img.win2d.Logo.CanvasBmp;
using LogoCanvasImg         = fmg.uwp.draw.img.win2d.Logo.CanvasImgSrc;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;
using MosaicsSkillCanvasImg = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasImgSrc;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;
using MosaicsGroupCanvasImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasImgSrc;
using MosaicsCanvasBmp      = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;
using MosaicsCanvasImg      = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasImgSrc;
using SmileCanvasBmp        = fmg.uwp.draw.img.win2d.Smile.CanvasBmp;
using SmileCanvasImg        = fmg.uwp.draw.img.win2d.Smile.CanvasImgSrc;
using FlagCanvasBmp         = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using FlagCanvasImg         = fmg.uwp.draw.img.win2d.Flag.CanvasImgSrc;
using MineCanvasBmp         = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;
using MineCanvasImg         = fmg.uwp.draw.img.win2d.Mine.CanvasImgSrc;

namespace Test.FastMines.Uwp.Images.Win2D {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing { }

      TestDrawing _td;
      Panel _panel;
      static readonly int margin = 10; // panel margin - padding to inner images
      Action _onCloseImages;
      Action[] _onCreateImages; // images factory
      int _nextCreateImagesIndex;
      bool _enableAnimation;

      #region images Fabrica
      public void TestLogos1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(rnd => new LogoCanvasBmp[] {
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator)
         });
      }
      public void TestLogos2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(rnd => new LogoCanvasImg[] {
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator)
         });
      }
      public void TestMosaicsSkillImg1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(rnd => (new MosaicsSkillCanvasBmp[] { new MosaicsSkillCanvasBmp(null, resourceCreator), new MosaicsSkillCanvasBmp(null, resourceCreator) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillCanvasBmp[] { new MosaicsSkillCanvasBmp(e, resourceCreator), new MosaicsSkillCanvasBmp(e, resourceCreator) })
                                    .SelectMany(m => m)));
      }
      public void TestMosaicsSkillImg2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(rnd => (new MosaicsSkillCanvasImg[] { new MosaicsSkillCanvasImg(null, resourceCreator), new MosaicsSkillCanvasImg(null, resourceCreator) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillCanvasImg[] { new MosaicsSkillCanvasImg(e, resourceCreator), new MosaicsSkillCanvasImg(e, resourceCreator) })
                                    .SelectMany(m => m)));
      }
      public void TestMosaicsGroupImg1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(rnd => (new MosaicsGroupCanvasBmp[] { new MosaicsGroupCanvasBmp(null, resourceCreator), new MosaicsGroupCanvasBmp(null, resourceCreator) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupCanvasBmp[] { new MosaicsGroupCanvasBmp(e, resourceCreator), new MosaicsGroupCanvasBmp(e, resourceCreator) })
                                     .SelectMany(m => m)));
      }
      public void TestMosaicsGroupImg2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(rnd => (new MosaicsGroupCanvasImg[] { new MosaicsGroupCanvasImg(null, resourceCreator), new MosaicsGroupCanvasImg(null, resourceCreator) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupCanvasImg[] { new MosaicsGroupCanvasImg(e, resourceCreator), new MosaicsGroupCanvasImg(e, resourceCreator) })
                                     .SelectMany(m => m)));
      }
      public void TestMosaicsImg1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(rnd =>
            EMosaicEx.GetValues().Select(e => new MosaicsCanvasBmp(e, new Matrisize(3 + _td.R(4), 4 + _td.R(3)), resourceCreator))
         );
      }
      public void TestMosaicsImg2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(rnd =>
            EMosaicEx.GetValues().Select(e => new MosaicsCanvasImg(e, new Matrisize(3 + _td.R(4), 4 + _td.R(3)), resourceCreator))
         );
      }
      public void TestFlag1 (ICanvasResourceCreator resourceCreator) { TestAppCanvasBmp(rnd => new FlagCanvasBmp [] { new FlagCanvasBmp(resourceCreator) }); }
      public void TestFlag2 (ICanvasResourceCreator resourceCreator) { TestAppCanvasImg(rnd => new FlagCanvasImg [] { new FlagCanvasImg(resourceCreator) }); }
      public void TestMine1 (ICanvasResourceCreator resourceCreator) { TestAppCanvasBmp(rnd => new MineCanvasBmp [] { new MineCanvasBmp(resourceCreator) }); }
      public void TestMine2 (ICanvasResourceCreator resourceCreator) { TestAppCanvasImg(rnd => new MineCanvasImg [] { new MineCanvasImg(resourceCreator) }); }
      public void TestSmile1(ICanvasResourceCreator resourceCreator) {
         var vals = (Smile.EType[])Enum.GetValues(typeof(Smile.EType));
         TestAppCanvasBmp(rnd =>
            vals.Select(e => new SmileCanvasBmp(e, resourceCreator))
         );
      }
      public void TestSmile2(ICanvasResourceCreator resourceCreator) {
         var vals = (Smile.EType[])Enum.GetValues(typeof(Smile.EType));
         TestAppCanvasImg(rnd =>
            vals.Select(e => new SmileCanvasImg(e, resourceCreator))
         );
      }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         var device = CanvasDevice.GetSharedDevice();
         _onCreateImages = new Action[] {
            () => TestLogos1          (device),
            () => TestLogos2          (device),
            () => TestMosaicsSkillImg1(device),
            () => TestMosaicsSkillImg2(device),
            () => TestMosaicsGroupImg1(device),
            () => TestMosaicsGroupImg2(device),
            () => TestMosaicsImg1     (device),
            () => TestMosaicsImg2     (device),
            () => TestFlag1           (device),
            () => TestFlag2           (device),
            () => TestMine1           (device),
            () => TestMine2           (device),
            () => TestSmile1          (device),
            () => TestSmile2          (device)
         };

         InitializeComponent();

         _page.Content = _panel = new Canvas();

         SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = AppViewBackButtonVisibility.Visible;
         SystemNavigationManager.GetForCurrentView().BackRequested += (s, ev) => OnNewImages();
         if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
            HardwareButtons.BackPressed += (s, ev) => OnNewImages();
         }
         _page.Loaded   += (s, ev) => OnNewImages();
         _page.Unloaded += (s, ev) => _onCloseImages();
      }

      #region main part
      void TestAppCanvasBmp<TImageEx>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
      {
         TestApp<TImageEx, PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>, CanvasBitmap>(funcGetImages);
      }
      void TestAppCanvasImg<TImageEx>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
      {
         TestApp<TImageEx, PaintableWin2D, CanvasImageSource, PaintUwpContext<CanvasBitmap>, CanvasBitmap>(funcGetImages);
      }

      void TestApp<TImageEx, TPaintable, TImage, TPaintContext, TImageInner>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
         where TPaintable : IPaintable
         where TImage : DependencyObject, ICanvasResourceCreator
         where TImageInner : class
         where TPaintContext : PaintContext<TImageInner>
      {
         _panel.Children.Clear();
         List<TImageEx> images = funcGetImages(_td.GetRandom).ToList();
         ApplicationView.GetForCurrentView().Title = _td.GetTitle(images);

         bool testTransparent = _td.Bl;
         images.Select(x => x as StaticImg<TImage>)
            .Where(x => x != null)
            .ToList()
            .ForEach(img => _td.ApplyRandom<TPaintable, TImage, TPaintContext, TImageInner>(img, testTransparent));

         FrameworkElement[,] imgControls;

         {
            double sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 100;
            double sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 100;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<TImageEx> ctr = _td.CellTiling(rc, images, testTransparent);
            Size imgSize = ctr.imageSize;
            imgControls = new FrameworkElement[ctr.tableSize.Width, ctr.tableSize.Height];

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               FrameworkElement imgCntrl = null;
               #region CanvasImageSource
               if (typeof(TImage) == typeof(CanvasImageSource)) {
                  imgCntrl = new Image {
                     Margin = new Thickness {
                        Left = offset.X,
                        Top = offset.Y
                     },
                     Stretch = Stretch.None
                  };

                  if (imgObj is StaticCanvasImg) {
                     StaticCanvasImg simg = imgObj as StaticCanvasImg;
                     simg.Size = imgSize;

                     imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(StaticCanvasImg.Image)),
                        Mode = BindingMode.OneWay
                     });
                  } else
                  if (imgObj is FlagCanvasImg) {
                     imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                        Source = imgObj,
                        Path = new PropertyPath(nameof(FlagCanvasImg.Image)),
                        Mode = BindingMode.OneWay
                     });
                  } else
                  if (imgObj is SmileCanvasImg) {
                     imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                        Source = imgObj,
                        Path = new PropertyPath(nameof(SmileCanvasImg.Image)),
                        Mode = BindingMode.OneWay
                     });
                  } else {
                     throw new Exception("Unsupported image type");
                  }

               } else
               #endregion
               #region CanvasBitmap
               if (typeof(TImage) == typeof(CanvasBitmap)) {
                  var cnvsCtrl= new CanvasControl {
                     Margin = new Thickness {
                        Left = offset.X,
                        Top = offset.Y
                     },
                     //ClearColor = ColorExt.RandomColor(_td.GetRandom).Brighter().ToWinColor(),
                  };
                  imgCntrl = cnvsCtrl;

                  if (imgObj is StaticCanvasBmp) {
                     StaticCanvasBmp simg = imgObj as StaticCanvasBmp;
                     simg.Size = imgSize;
                     simg.PropertyChanged += (s, ev) => {
                        if (ev.PropertyName == nameof(simg.Image))
                           cnvsCtrl.Invalidate();
                     };
                     cnvsCtrl.SetBinding(CanvasControl.WidthProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(StaticCanvasBmp.Width)),
                        Mode = BindingMode.OneWay
                     });
                     cnvsCtrl.SetBinding(CanvasControl.HeightProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(StaticCanvasBmp.Height)),
                        Mode = BindingMode.OneWay
                     });
                  } else
                  if (imgObj is FlagCanvasBmp) {
                     FlagCanvasBmp fimg = imgObj as FlagCanvasBmp;
                     fimg.Width = imgSize.Width;
                     fimg.Height = imgSize.Height;
                     cnvsCtrl.SetBinding(CanvasControl.WidthProperty, new Binding {
                        Source = fimg,
                        Path = new PropertyPath(nameof(FlagCanvasBmp.Width)),
                        Mode = BindingMode.OneWay
                     });
                     cnvsCtrl.SetBinding(CanvasControl.HeightProperty, new Binding {
                        Source = fimg,
                        Path = new PropertyPath(nameof(FlagCanvasBmp.Height)),
                        Mode = BindingMode.OneWay
                     });
                  } else
                  if (imgObj is SmileCanvasBmp) {
                     SmileCanvasBmp simg = imgObj as SmileCanvasBmp;
                     simg.Width = imgSize.Width;
                     simg.Height = imgSize.Height;
                     cnvsCtrl.SetBinding(CanvasControl.WidthProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(SmileCanvasBmp.Width)),
                        Mode = BindingMode.OneWay
                     });
                     cnvsCtrl.SetBinding(CanvasControl.HeightProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(SmileCanvasBmp.Height)),
                        Mode = BindingMode.OneWay
                     });
                  } else {
                     throw new Exception("Unsupported image type");
                  }

                  cnvsCtrl.Draw += (s, ev) => {

                     CanvasBitmap cnvsBmp;
                     if (imgObj is StaticCanvasBmp) {
                        cnvsBmp = (imgObj as StaticCanvasBmp).Image;
                     } else
                     if (imgObj is FlagCanvasBmp) {
                        cnvsBmp = (imgObj as FlagCanvasBmp).Image;
                     } else
                     if (imgObj is SmileCanvasBmp) {
                        cnvsBmp = (imgObj as SmileCanvasBmp).Image;
                     } else {
                        throw new Exception("Unsupported image type");
                     }

                     ev.DrawingSession.DrawImage(cnvsBmp, new Rect(0, 0, cnvsCtrl.Width, cnvsCtrl.Height));
                  };
               } else
               #endregion
               {
                  throw new Exception("Unsupported image type");
               }

               _panel.Children.Add(imgCntrl);
               imgControls[cti.i, cti.j] = imgCntrl;
            }
         }

         SizeChangedEventHandler sceh = (s, ev) => {
            double sizeW = ev.NewSize.Width;
            double sizeH = ev.NewSize.Height;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<TImageEx> ctr = _td.CellTiling(rc, images, testTransparent);
            Size imgSize = ctr.imageSize;

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               if (imgObj is StaticImg<TImage>) {
                  StaticImg<TImage> simg = imgObj as StaticImg<TImage>;
                  simg.Size = imgSize;
               } else
               if (imgObj is Flag.CommonImpl<TImage>) {
                  Flag.CommonImpl<TImage> fimg = imgObj as Flag.CommonImpl<TImage>;
                  fimg.Width = imgSize.Width;
                  fimg.Height = imgSize.Height;
               } else
               if (imgObj is Smile.CommonImpl<TImage>) {
                  Smile.CommonImpl<TImage> simg = imgObj as Smile.CommonImpl<TImage>;
                  simg.Width = imgSize.Width;
                  simg.Height = imgSize.Height;
               } else {
                  throw new Exception("Unsupported image type");
               }

               imgControls[cti.i, cti.j].Margin = new Thickness {
                  Left = offset.X,
                  Top = offset.Y
               };
            }
         };
         _panel.SizeChanged += sceh;

         _onCloseImages = () => {
            _panel.SizeChanged -= sceh;
            images.Select(x => x as StaticImg<TImage>)
               .Where(x => x != null)
               .ToList()
               .ForEach(img => img.Dispose());
            images.Clear();
            images = null;
            imgControls = null;
         };
      }
      #endregion


      void OnNewImages() {
         _onCloseImages?.Invoke();

         Action onCreate = _onCreateImages[_nextCreateImagesIndex];
         if (++_nextCreateImagesIndex >= _onCreateImages.Length)
            _nextCreateImagesIndex = 0;
         onCreate();
      }

      public void Animation(bool enable) {
      }

/*
      private readonly LogoCanvasBmp            Bmp1;
      private readonly MosaicsSkillCanvasBmp    Bmp2;
      private readonly MosaicsGroupCanvasBmp    Bmp3;
      private readonly MosaicsCanvasBmp         Bmp4;
      private readonly SmileCanvasBmp           Bmp5;
      private readonly FlagCanvasBmp            Bmp6;
      private readonly MineCanvasBmp            Bmp7;

      public LogoCanvasImgSrc            Img1 { get; }
      public MosaicsSkillCanvasImg    Img2 { get; }
      public MosaicsGroupCanvasImg    Img3 { get; }
      public MosaicsCanvasImg         Img4 { get; }
      public SmileCanvasImg           Img5 { get; }
      public FlagCanvasImg            Img6 { get; }
      public MineCanvasImg            Img7 { get; }

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
         Bmp2 = new MosaicsSkillCanvasBmp   (Bl ? (ESkillLevel?)null : ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], canvasControl2);
         Img2 = new MosaicsSkillCanvasImg(Bl ? (ESkillLevel?)null : ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], device);
         Bmp3 = new MosaicsGroupCanvasBmp   (EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], canvasControl3);
         Img3 = new MosaicsGroupCanvasImg(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], device);
         Bmp4 = new MosaicsCanvasBmp        (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), canvasControl4);
         Img4 = new MosaicsCanvasImg     (EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), device);
         Bmp5 = new SmileCanvasBmp   (SmileCanvasBmp   .EType.Face_WhiteSmiling, canvasControl5);
         Img5 = new SmileCanvasImg(SmileCanvasImg.EType.Face_WhiteSmiling, device);
         Bmp6 = new FlagCanvasBmp(canvasControl6);
         Img6 = new FlagCanvasImg(device);
         Bmp7 = new MineCanvasBmp(canvasControl7);
         Img7 = new MineCanvasImg(device);

         ApplyRandom(Bmp1, canvasControl1);
         ApplyRandom(Bmp2, canvasControl2);
         ApplyRandom(Bmp3, canvasControl3);
         ApplyRandom(Bmp4, canvasControl4);
       //ApplyRandom(Bmp5, canvasControl5);
       //ApplyRandom(Bmp6, canvasControl6);
         ApplyRandom(Bmp7, canvasControl7);
         ApplyRandom(Img1, null);
         ApplyRandom(Img2, null);
         ApplyRandom(Img3, null);
         ApplyRandom(Img4, null);
       //ApplyRandom(Img5, null);
       //ApplyRandom(Img6, null);
         ApplyRandom(Img7, null);

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

         NextImg5();
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
         loopImg5 =
       //Img5.Rotate =   Img5.PolarLights =
       //Img6.Rotate =   Img6.PolarLights =
         Img7.Rotate =   Img7.PolarLights =
            enable;
      }

      private static Smile<TImage>.EType NextSmileType<TImage>(Smile<TImage>.EType smileType)
         where TImage : DependencyObject, ICanvasResourceCreator
      {
         var vals = (Smile<TImage>.EType[])Enum.GetValues(typeof(Smile<TImage>.EType));
         var pos = Array.IndexOf(vals, smileType);
         return (pos < (vals.Length - 1)) ? vals[pos + 1] : vals[0];
      }

      private void canvasControl_Tapped5(object sender, Windows.UI.Xaml.Input.TappedRoutedEventArgs e) {
         Bmp5.Type = NextSmileType(Bmp5.Type);
         canvasControl5.Invalidate();
      }

      private bool loopImg5 = true;
      private void NextImg5() {
         TaskExec.DelayedStart(
               TimeSpan.FromSeconds(1),
               () => {
                  Img5.Type = NextSmileType(Img5.Type);
                  AsyncRunner.InvokeFromUiLater(() => { if (loopImg5) img5Cntrl.Source = Img5.Image; } );
                  NextImg5();
               });
      }
*/
   }

}
