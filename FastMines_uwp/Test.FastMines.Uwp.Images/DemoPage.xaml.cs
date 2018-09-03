using System;
using System.Linq;
using System.Reflection;
using System.Collections.Generic;
using System.ComponentModel;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.ViewManagement;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using fmg.common;
using fmg.common.geom;
using fmg.common.Converters;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.uwp.utils;
using fmg.uwp.img.win2d;
using fmg.uwp.img.wbmp;
using fmg.uwp.mosaic.win2d;
using fmg.uwp.mosaic.wbmp;
using fmg.uwp.mosaic.xaml;
using Win2dMosaicSkillImg = fmg.uwp.img.win2d.MosaicSkillImg;
using Win2dMosaicGroupImg = fmg.uwp.img.win2d.MosaicGroupImg;
using Win2dLogo           = fmg.uwp.img.win2d.Logo;
using Win2dMine           = fmg.uwp.img.win2d.Mine;
using Win2dSmile          = fmg.uwp.img.win2d.Smile;
using Win2dFlag           = fmg.uwp.img.win2d.Flag;
#if false
using MosaicsCanvasBmp = fmg.uwp.img.win2d.MosaicsImg.CanvasBmp;
using MosaicsCanvasImg = fmg.uwp.img.win2d.MosaicsImg.CanvasImgSrc;
#endif
using WBmpMosaicImageController = fmg.uwp.mosaic.wbmp.MosaicImageController;
using WBmpMosaicImageView       = fmg.uwp.mosaic.wbmp.MosaicImageView;
using WBmpMosaicImg             = fmg.uwp.img.wbmp.MosaicImg;
using WBmpMosaicSkillImg        = fmg.uwp.img.wbmp.MosaicSkillImg;
using WBmpMosaicGroupImg        = fmg.uwp.img.wbmp.MosaicGroupImg;
using WBmpLogo                  = fmg.uwp.img.wbmp.Logo;
using WBmpMine                  = fmg.uwp.img.wbmp.Mine;
using WBmpFlag                  = fmg.uwp.img.wbmp.Flag;
using WBmpSmile                 = fmg.uwp.img.wbmp.Smile;
using DummyMosaicImageType = System.Object;

namespace Test.FastMines.Uwp.Images {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing {
         public TestDrawing() : base("UWP") { }
      }

      private TestDrawing _td;
      private Panel _panel;
      private static readonly int margin = 10; // panel margin - padding to inner images
      private Action _onCloseImages;
      private Action[] _onCreateImages; // images factory
      private int _nextCreateImagesIndex;
      private Action<bool> _onActivated;

      #region images Fabrica
      private class DummyModel : IAnimatedModel {
         public bool Animated      { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public long AnimatePeriod { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public int TotalFrames    { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public int CurrentFrame   { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public SizeDouble Size    { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
#pragma warning disable CS0067 // warning CS0067: The event is never used
         public event PropertyChangedEventHandler PropertyChanged; // TODO unusable
#pragma warning restore CS0067
         public void Dispose() { throw new NotImplementedException(); }
      }
      private class DummyView<TImage> : IImageView<TImage, DummyModel>
         where TImage : class
      {
         public DummyModel Model      => throw new NotImplementedException();
         public SizeDouble Size { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public TImage Image          => throw new NotImplementedException();
#pragma warning disable CS0067 // warning CS0067: The event is never used
         public event PropertyChangedEventHandler PropertyChanged; // TODO unusable
#pragma warning restore CS0067
         public void Dispose()    { throw new NotImplementedException(); }
         public void Invalidate() { throw new NotImplementedException(); }
      }
      private void TestWin2dLogo1(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasBitmap, Win2dLogo.ControllerBitmap, Win2dLogo.CanvasBmp, LogoModel>(() =>
            new Win2dLogo.ControllerBitmap[] { new Win2dLogo.ControllerBitmap(resourceCreator)
                                             , new Win2dLogo.ControllerBitmap(resourceCreator)
                                             , new Win2dLogo.ControllerBitmap(resourceCreator)
                                             , new Win2dLogo.ControllerBitmap(resourceCreator)}
         );
      }
      private void TestWin2dLogo2(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasImageSource, Win2dLogo.ControllerImgSrc, Win2dLogo.CanvasImgSrc, LogoModel>(() =>
            new Win2dLogo.ControllerImgSrc[] { new Win2dLogo.ControllerImgSrc(resourceCreator)
                                             , new Win2dLogo.ControllerImgSrc(resourceCreator)
                                             , new Win2dLogo.ControllerImgSrc(resourceCreator)
                                             , new Win2dLogo.ControllerImgSrc(resourceCreator)}
         );
      }
      private void TestWin2dMine1(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasBitmap, Win2dMine.ControllerBitmap, Win2dLogo.CanvasBmp, LogoModel>(() =>
            new Win2dMine.ControllerBitmap[] { new Win2dMine.ControllerBitmap(resourceCreator)
                                             , new Win2dMine.ControllerBitmap(resourceCreator)
                                           //, new Win2dMine.ControllerBitmap(resourceCreator)
                                             , new Win2dMine.ControllerBitmap(resourceCreator)}
         );
      }
      private void TestWin2dMine2(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasImageSource, Win2dMine.ControllerImgSrc, Win2dLogo.CanvasImgSrc, LogoModel>(() =>
            new Win2dMine.ControllerImgSrc[] { new Win2dMine.ControllerImgSrc(resourceCreator)
                                             , new Win2dMine.ControllerImgSrc(resourceCreator)
                                           //, new Win2dMine.ControllerImgSrc(resourceCreator)
                                             , new Win2dMine.ControllerImgSrc(resourceCreator)}
         );
      }
      public void TestWin2dMosaicSkillImg1(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasBitmap, Win2dMosaicSkillImg.ControllerBitmap, Win2dMosaicSkillImg.CanvasBmp, MosaicSkillModel>(() =>
            (new Win2dMosaicSkillImg.ControllerBitmap[] {
                  new Win2dMosaicSkillImg.ControllerBitmap(null, resourceCreator),
                  new Win2dMosaicSkillImg.ControllerBitmap(null, resourceCreator) })
            .Concat(ESkillLevelEx.GetValues()
                                 .Select(e => new Win2dMosaicSkillImg.ControllerBitmap[] {
                                    new Win2dMosaicSkillImg.ControllerBitmap(e, resourceCreator),
                                    new Win2dMosaicSkillImg.ControllerBitmap(e, resourceCreator) })
                                 .SelectMany(m => m)));
      }
      public void TestWin2dMosaicSkillImg2(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasImageSource, Win2dMosaicSkillImg.ControllerImgSrc, Win2dMosaicSkillImg.CanvasImgSrc, MosaicSkillModel>(() =>
            (new Win2dMosaicSkillImg.ControllerImgSrc[] {
                  new Win2dMosaicSkillImg.ControllerImgSrc(null, resourceCreator),
                  new Win2dMosaicSkillImg.ControllerImgSrc(null, resourceCreator) })
            .Concat(ESkillLevelEx.GetValues()
                                 .Select(e => new Win2dMosaicSkillImg.ControllerImgSrc[] {
                                    new Win2dMosaicSkillImg.ControllerImgSrc(e, resourceCreator),
                                    new Win2dMosaicSkillImg.ControllerImgSrc(e, resourceCreator) })
                                 .SelectMany(m => m)));
      }
      public void TestWin2dMosaicGroupImg1(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasBitmap, Win2dMosaicGroupImg.ControllerBitmap, Win2dMosaicGroupImg.CanvasBmp, MosaicGroupModel>(() =>
            (new Win2dMosaicGroupImg.ControllerBitmap[] {
                  new Win2dMosaicGroupImg.ControllerBitmap(null, resourceCreator),
                  new Win2dMosaicGroupImg.ControllerBitmap(null, resourceCreator) })
            .Concat(EMosaicGroupEx.GetValues()
                                 .Select(e => new Win2dMosaicGroupImg.ControllerBitmap[] {
                                    new Win2dMosaicGroupImg.ControllerBitmap(e, resourceCreator),
                                    new Win2dMosaicGroupImg.ControllerBitmap(e, resourceCreator) })
                                 .SelectMany(m => m)));
      }
      public void TestWin2dMosaicGroupImg2(ICanvasResourceCreator resourceCreator) {
         TestAppAnimated<CanvasImageSource, Win2dMosaicGroupImg.ControllerImgSrc, Win2dMosaicGroupImg.CanvasImgSrc, MosaicGroupModel>(() =>
            (new Win2dMosaicGroupImg.ControllerImgSrc[] {
                  new Win2dMosaicGroupImg.ControllerImgSrc(null, resourceCreator),
                  new Win2dMosaicGroupImg.ControllerImgSrc(null, resourceCreator) })
            .Concat(EMosaicGroupEx.GetValues()
                                 .Select(e => new Win2dMosaicGroupImg.ControllerImgSrc[] {
                                    new Win2dMosaicGroupImg.ControllerImgSrc(e, resourceCreator),
                                    new Win2dMosaicGroupImg.ControllerImgSrc(e, resourceCreator) })
                                 .SelectMany(m => m)));
      }
#if false
      public void TestWin2dMosaicsImg1(ICanvasResourceCreator resourceCreator) {
         var rnd = ThreadLocalRandom.Current;
         TestAppCanvasBmp(() =>
            // test all
            EMosaicEx.GetValues().Select(e => new MosaicsCanvasBmp(resourceCreator) {
               MosaicType = e,
               SizeField = new Matrisize(3 + rnd.Next(4), 4 + rnd.Next(3))
            })
            
            //// test single
            //new List<MosaicsCanvasBmp>() { new MosaicsCanvasBmp(resourceCreator) {
            //   MosaicType = EMosaic.eMosaicPentagonT24,
            //   SizeField = new Matrisize(3, 7)
            //} }
         );
      }
      public void TestWin2dMosaicsImg2(ICanvasResourceCreator resourceCreator) {
         var rnd = ThreadLocalRandom.Current;
         TestAppCanvasImg(() =>
            // test all
            EMosaicEx.GetValues().Select(e => new MosaicsCanvasImg(resourceCreator) {
               MosaicType = e,
               SizeField = new Matrisize(3 + rnd.Next(4), 4 + rnd.Next(3))
            })

            //// test single
            //new List<MosaicsCanvasImg>() { new MosaicsCanvasImg(resourceCreator) {
            //   MosaicType = EMosaic.eMosaicPentagonT24,
            //   SizeField = new Matrisize(3, 7)
            //} }
         );
      }
#endif
      public void TestWin2dFlag1(ICanvasResourceCreator resourceCreator) { TestAppSimple<CanvasBitmap     , Win2dFlag.ControllerBitmap, Win2dFlag.CanvasBmp   , FlagModel>(() => new Win2dFlag.ControllerBitmap[] { new Win2dFlag.ControllerBitmap(resourceCreator) }); }
      public void TestWin2dFlag2(ICanvasResourceCreator resourceCreator) { TestAppSimple<CanvasImageSource, Win2dFlag.ControllerImgSrc, Win2dFlag.CanvasImgSrc, FlagModel>(() => new Win2dFlag.ControllerImgSrc[] { new Win2dFlag.ControllerImgSrc(resourceCreator) }); }
      public void TestWin2dSmile1(ICanvasResourceCreator resourceCreator) {
         var vals = (SmileModel.EFaceType[])Enum.GetValues(typeof(SmileModel.EFaceType));
         TestAppSimple<CanvasBitmap, Win2dSmile.ControllerBitmap, Win2dSmile.CanvasBmp, SmileModel>(() =>
            vals.Select(e => new Win2dSmile.ControllerBitmap(e, resourceCreator))
         );
      }
      public void TestWin2dSmile2(ICanvasResourceCreator resourceCreator) {
         var vals = (SmileModel.EFaceType[])Enum.GetValues(typeof(SmileModel.EFaceType));
         TestAppSimple<CanvasImageSource, Win2dSmile.ControllerImgSrc, Win2dSmile.CanvasImgSrc, SmileModel>(() =>
            vals.Select(e => new Win2dSmile.ControllerImgSrc(e, resourceCreator))
         );
      }

      private static TMosaicController TuneMosaicGameController<TImage, TImageInner, TMosaicController, TMosaicView, TMosaicModel>(TMosaicController mosaicController)
         where TImage      : class
         where TImageInner : class
         where TMosaicController : MosaicController<TImage, TImageInner, TMosaicView, TMosaicModel>
         where TMosaicView       : IMosaicView<TImage, TImageInner, TMosaicModel>
         where TMosaicModel      : MosaicDrawModel<TImageInner> 
      {
         if (ThreadLocalRandom.Current.Next(2) == 1) {
            // unmodified controller test
         } else {
            EMosaic mosaicType = EMosaic.eMosaicTrSq1;
            ESkillLevel skill = ESkillLevel.eBeginner;

            mosaicController.Area = 500;
            mosaicController.MosaicType = mosaicType;
            mosaicController.SizeField = skill.GetDefaultSize();
            mosaicController.MinesCount = skill.GetNumberMines(mosaicType);
            mosaicController.GameNew();
         }
         return mosaicController;
      }

      private void TestXamlMosaicControl()  {
         MosaicXamlView._DEBUG_DRAW_FLOW = true;
         TestAppMosaicXamlCtr(() => new MosaicXamlController[] {
            TuneMosaicGameController<Panel, ImageSource, MosaicXamlController, MosaicXamlView, MosaicDrawModel<ImageSource>>(new MosaicXamlController())
         });
      }
      private void TestWBmpMosaicControl() {
         WBmpMosaicImageView._DEBUG_DRAW_FLOW = true;
         TestAppMosaicWBmpControl(() => new WBmpMosaicImageController[] {
            TuneMosaicGameController<Image, WriteableBitmap, WBmpMosaicImageController, WBmpMosaicImageView, MosaicDrawModel<WriteableBitmap>>(new WBmpMosaicImageController())
         });
      }
      private void TestWBmpMosaicsImg() {
         TestAppMosaicWBmpImage(() =>
             //new List<MosaicWBmpImg.Controller>() { new MosaicWBmpImg.Controller() { MosaicType = EMosaic.eMosaicSquare1 } }
               EMosaicEx.GetValues().Select(e => new WBmpMosaicImg.Controller() { MosaicType = e })
         );
      }
      private void TestWBmpLogo() {
         TestAppAnimated<WriteableBitmap, WBmpLogo.Controller, WBmpLogo, LogoModel>(() =>
            new WBmpLogo.Controller[] {
               new WBmpLogo.Controller(),
               new WBmpLogo.Controller(),
               new WBmpLogo.Controller(),
               new WBmpLogo.Controller()}
         );
      }
      private void TestWBmpMosaicSkillImg() {
         TestAppAnimated<WriteableBitmap, WBmpMosaicSkillImg.Controller, WBmpMosaicSkillImg, MosaicSkillModel>(() =>
            (new WBmpMosaicSkillImg.Controller[] { new WBmpMosaicSkillImg.Controller(null) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new WBmpMosaicSkillImg.Controller[] { new WBmpMosaicSkillImg.Controller(e) })
                                    .SelectMany(m => m)));
      }
      private void TestWBmpMosaicGroupImg() {
         TestAppAnimated<WriteableBitmap, WBmpMosaicGroupImg.Controller, WBmpMosaicGroupImg, MosaicGroupModel>(() =>
            (new WBmpMosaicGroupImg.Controller[] { new WBmpMosaicGroupImg.Controller(null) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new WBmpMosaicGroupImg.Controller[] { new WBmpMosaicGroupImg.Controller(e) })
                                     .SelectMany(m => m))
         );
      }
      private void TestWBmpMine() {
         TestAppAnimated<WriteableBitmap, WBmpMine.Controller, WBmpLogo, LogoModel>(() =>
            new WBmpMine.Controller[] { new WBmpMine.Controller() }
         );
      }
      private void TestWBmpFlag() {
         TestAppSimple<WriteableBitmap, WBmpFlag.Controller, WBmpFlag, FlagModel>(() =>
            new WBmpFlag.Controller[] { new WBmpFlag.Controller() }
         );
      }
      private void TestWBmpSmile() {
         TestAppSimple<WriteableBitmap, WBmpSmile.Controller, WBmpSmile, SmileModel>(() =>
            new WBmpSmile.Controller[] { new WBmpSmile.Controller(SmileModel.EFaceType.Face_WhiteSmiling) }
         );
      }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         var device = CanvasDevice.GetSharedDevice();
         _onCreateImages = new Action[] {
            () => TestWin2dMosaicSkillImg1(device),
            () => TestWin2dMosaicSkillImg2(device),
            () => TestWin2dMosaicGroupImg1(device),
            () => TestWin2dMosaicGroupImg2(device),
            () => TestWin2dLogo1          (device),
            () => TestWin2dLogo2          (device),
            () => TestWin2dMine1          (device),
            () => TestWin2dMine2          (device),
            () => TestWin2dSmile1         (device),
            () => TestWin2dSmile2         (device),
#if false
            () => TestMosaicsImg1     (device),
            () => TestMosaicsImg2     (device),
#endif
            () => TestWin2dFlag1      (device),
            () => TestWin2dFlag2      (device),
            TestXamlMosaicControl,
            TestWBmpMosaicControl,
            TestWBmpLogo,
            TestWBmpMine,
            TestWBmpMosaicSkillImg,
            TestWBmpMosaicGroupImg,
            TestWBmpMosaicsImg,
            TestWBmpFlag,
            TestWBmpSmile
         };

         InitializeComponent();

         _page.Content = _panel = new Canvas();
         _panel.Background = new SolidColorBrush(Colors.Transparent); // lifehack: otherwise the click does not handled on the empty place

         SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = AppViewBackButtonVisibility.Visible;
         SystemNavigationManager.GetForCurrentView().BackRequested += (s, ev) => { OnNextImages(); ev.Handled = true; };
         if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
            HardwareButtons.BackPressed += (s, ev) => { OnNextImages(); ev.Handled = true; };
         }
         _page.Loaded   += (s, ev) => OnNextImages();
         _page.Unloaded += (s, ev) => _onCloseImages();
      }

      #region main part
      #region wrappers
      void TestAppSimple<TImage, TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImage           : class
         where TImageController : IImageController<TImage, TImageView, TImageModel>
         where TImageView       : IImageView<TImage, TImageModel>
         where TImageModel      : IImageModel
      {
         TestApp<TImage, DummyMosaicImageType, TImageController, TImageView, DummyView<TImage>, TImageModel, DummyModel>(funcGetImages);
      }

      void TestAppAnimated<TImage, TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImage           : class
         where TImageController : IImageController<TImage, TImageView, TImageModel>
         where TImageView       : IImageView<TImage, TImageModel>
         where TImageModel      : IAnimatedModel
      {
         TestApp<TImage, DummyMosaicImageType, TImageController, TImageView, TImageView, TImageModel, TImageModel>(funcGetImages);
      }

      void TestAppMosaicWBmpImage(Func<IEnumerable<WBmpMosaicImg.Controller>> funcGetImages) {
         TestApp<WriteableBitmap, Nothing, WBmpMosaicImg.Controller, WBmpMosaicImg, WBmpMosaicImg, MosaicAnimatedModel<Nothing>, MosaicAnimatedModel<Nothing>>(funcGetImages);
      }

      void TestAppMosaicWBmpControl(Func<IEnumerable<WBmpMosaicImageController>> funcGetImages) {
         TestApp<Image, WriteableBitmap, WBmpMosaicImageController, WBmpMosaicImageView, DummyView<Image>, MosaicDrawModel<WriteableBitmap>, DummyModel>(funcGetImages);
      }

      void TestAppMosaicXamlCtr(Func<IEnumerable<MosaicXamlController>> funcGetImages) {
         TestApp<Panel, ImageSource, MosaicXamlController, MosaicXamlView, DummyView<Panel>, MosaicDrawModel<ImageSource>, DummyModel>(funcGetImages);
      }
      #endregion wrappers

      void TestApp<TImage, TMosaicImageInner, TImageController, TImageView, TAImageView, TImageModel, TAnimatedModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImage            : class
         where TMosaicImageInner : class
         where TImageController  : IImageController<TImage, TImageView, TImageModel>
         where TImageView        : IImageView<TImage, TImageModel>
         where TAImageView       : IImageView<TImage, TAnimatedModel>
         where TImageModel       : IImageModel
         where TAnimatedModel    : IAnimatedModel
      {
         _panel.Children.Clear();
         var images = funcGetImages().ToList();
         ApplicationView.GetForCurrentView().Title = _td.GetTitle<TImage, TImageController, TImageView, TImageModel>(images);

         FrameworkElement[] imgControls = null;
         bool testTransparent = false;
         bool imgIsControl = typeof(FrameworkElement).GetTypeInfo().IsAssignableFrom(typeof(TImage).GetTypeInfo());

         void onCellTilingHandler(bool applySettings, bool createImgControls, bool resized) {
            resized = resized || createImgControls || applySettings;

            if (applySettings) {
               testTransparent = _td.Bl;
               images.ForEach(img => _td.ApplySettings<TImage, TMosaicImageInner, TImageView, TAImageView, TImageModel, TAnimatedModel>(img, testTransparent));
            }

            double sizeW = _panel.ActualWidth;
            double sizeH = _panel.ActualHeight;
            var rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<TImage, TImageController, TImageView, TImageModel> ctr = _td.CellTiling<TImage, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;
            if (createImgControls)
               imgControls = new FrameworkElement[images.Count];

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               if (createImgControls) {
                  FrameworkElement imgControl;
                  if (imgIsControl) {
                     imgControl = imgObj.Image as FrameworkElement;
               #region lifehack for XAML
                     if (typeof(TImageController) == typeof(MosaicXamlController)) {
                        imgControl.Visibility = Visibility.Collapsed;
                        AsyncRunner.InvokeFromUiLater(() => { imgControl.Visibility = Visibility.Visible; });
                     }
               #endregion
                  } else {
               #region CanvasImageSource or WriteableBitmap
                     if (typeof(TImage) == typeof(CanvasImageSource) ||
                         typeof(TImage) == typeof(WriteableBitmap))
                     {
                        imgControl = new Image {
                           Stretch = Stretch.None
                        };
                        imgControl.SetBinding(Image.SourceProperty, new Binding {
                           Source = imgObj,
                           Path = new PropertyPath(nameof(imgObj.Image)),
                           Mode = BindingMode.OneWay
                        });
                     } else
               #endregion
               #region CanvasBitmap
                     if (typeof(TImage) == typeof(CanvasBitmap)) {
                        var cnvsCtrl = new CanvasControl {
                           //ClearColor = ColorExt.RandomColor(_td.GetRandom).Brighter().ToWinColor(),
                        };
                        imgControl = cnvsCtrl;

                        imgObj.PropertyChanged += (s, ev) => {
                           if (ev.PropertyName == nameof(imgObj.Image))
                              cnvsCtrl.Invalidate();
                        };
                        cnvsCtrl.SetBinding(FrameworkElement.WidthProperty, new Binding {
                           Source = imgObj,
                           Path = new PropertyPath(nameof(imgObj.Size)),
                           Converter = new SizeToWidthConverter(),
                           Mode = BindingMode.OneWay
                        });
                        cnvsCtrl.SetBinding(FrameworkElement.HeightProperty, new Binding {
                           Source = imgObj,
                           Path = new PropertyPath(nameof(imgObj.Size)),
                           Converter = new SizeToHeightConverter(),
                           Mode = BindingMode.OneWay
                        });

                        void onDraw(CanvasControl s, CanvasDrawEventArgs ev) {
                           //if (imgObj.Disposed)
                           //   return;
                           ev.DrawingSession.DrawImage(imgObj.Image as CanvasBitmap, new Windows.Foundation.Rect(0, 0, cnvsCtrl.Width, cnvsCtrl.Height));
                        };
                        cnvsCtrl.Draw += onDraw;
                        cnvsCtrl.Unloaded += (s, ev) => {
                           cnvsCtrl.Draw -= onDraw;
                        };
                     } else
               #endregion
                     {
                        throw new Exception("Unsupported image type");
                     }
                  }
                  _panel.Children.Add(imgControl);
                  imgControls[ctr.tableSize.Width * cti.j + cti.i] = imgControl;
               }

               if (resized) {
                  imgObj.Model.Size = imgSize;
                  imgControls[ctr.tableSize.Width * cti.j + cti.i].Margin = new Thickness {
                     Left = offset.X,
                     Top = offset.Y
                  };
               }
            }
         }

         onCellTilingHandler(true, true, true);

         void onSizeChanged(object s, SizeChangedEventArgs ev) {
            onCellTilingHandler(false, false, true);
         };
         void onPointerPressed(object sender, PointerRoutedEventArgs ev) {
            //onCellTilingHandler(true, false, false);
         };
         void onTapped(object sender, TappedRoutedEventArgs ev) {
            onCellTilingHandler(true, false, false);
         };
         _panel.SizeChanged += onSizeChanged;
         if (imgIsControl)
            _panel.PointerPressed += onPointerPressed;
         else
            _panel.Tapped         += onTapped;

         _onCloseImages = () => {
            _panel.SizeChanged -= onSizeChanged;
            if (imgIsControl)
               _panel.PointerPressed -= onPointerPressed;
            else
               _panel.Tapped         -= onTapped;
            images.ForEach(img => img.Dispose());
            images.Clear();
            images = null;
            imgControls = null;
         };

   #if false
         _onActivated = enable => {
            images.ForEach(img => {
               var am = (img.Model as IAnimatedModel);
               if (am != null)
                  am.Animated = enable;
            });
         };
   #endif
      }
      #endregion

      void OnNextImages() {
         _onCloseImages?.Invoke();

         Action onCreate = _onCreateImages[_nextCreateImagesIndex];
         if (++_nextCreateImagesIndex >= _onCreateImages.Length)
            _nextCreateImagesIndex = 0;
         onCreate();
      }

      public void Animation(bool enable) {
         _onActivated?.Invoke(enable);
      }

   }

}
