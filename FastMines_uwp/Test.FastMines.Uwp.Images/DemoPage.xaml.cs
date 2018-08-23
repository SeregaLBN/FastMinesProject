using System;
using System.Reflection;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
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
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.uwp.utils;
using fmg.uwp.img.win2d;
using fmg.uwp.img.wbmp;
using fmg.uwp.mosaic.win2d;
using fmg.uwp.mosaic.wbmp;
using fmg.uwp.mosaic.xaml;
#if false
using StaticCanvasBmp = fmg.core.img.ImageModel<Microsoft.Graphics.Canvas.CanvasBitmap>;
using StaticCanvasImg = fmg.core.img.ImageModel<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>;
#endif
using LogoWBmp = fmg.uwp.img.wbmp.Logo;
using LogoW2D  = fmg.uwp.img.win2d.Logo;
using LogoCtrlCanvasBmp = fmg.uwp.img.win2d.Logo.ControllerBitmap;
using LogoCtrlCanvasImg = fmg.uwp.img.win2d.Logo.ControllerImgSrc;
using fmg.common.Converters;
#if false
using MosaicsSkillCanvasBmp = fmg.uwp.img.win2d.MosaicsSkillImg.CanvasBmp;
using MosaicsSkillCanvasImg = fmg.uwp.img.win2d.MosaicsSkillImg.CanvasImgSrc;
using MosaicsGroupCanvasBmp = fmg.uwp.img.win2d.MosaicsGroupImg.CanvasBmp;
using MosaicsGroupCanvasImg = fmg.uwp.img.win2d.MosaicsGroupImg.CanvasImgSrc;
using MosaicsCanvasBmp = fmg.uwp.img.win2d.MosaicsImg.CanvasBmp;
using MosaicsCanvasImg = fmg.uwp.img.win2d.MosaicsImg.CanvasImgSrc;
using SmileCanvasBmp = fmg.uwp.img.win2d.Smile.CanvasBmp;
using SmileCanvasImg = fmg.uwp.img.win2d.Smile.CanvasImgSrc;
using FlagCanvasBmp = fmg.uwp.img.win2d.Flag.CanvasBmp;
using FlagCanvasImg = fmg.uwp.img.win2d.Flag.CanvasImgSrc;
using MineCanvasBmp = fmg.uwp.img.win2d.Mine.CanvasBmp;
using MineCanvasImg = fmg.uwp.img.win2d.Mine.CanvasImgSrc;
#endif
using DummyMosaicImageType = System.Object;

namespace Test.FastMines.Uwp.Images {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing {
         public TestDrawing() : base("UWP") { }
      }

      TestDrawing _td;
      Panel _panel;
      static readonly int margin = 10; // panel margin - padding to inner images
      Action _onCloseImages;
      Action[] _onCreateImages; // images factory
      int _nextCreateImagesIndex;
      Action<bool> _onActivated;

      #region images Fabrica
      class DummyModel : IAnimatedModel {
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
      class DummyView<TImage> : IImageView<TImage, DummyModel>
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
      public void TestLogos1(ICanvasResourceCreator resourceCreator) { TestAppAnimatedCanvasBmp<LogoCtrlCanvasBmp, LogoW2D.CanvasBmp, LogoModel>(() => LogoW2D.GetTestData1(resourceCreator)); }
      public void TestLogos2(ICanvasResourceCreator resourceCreator) { TestAppAnimatedCanvasImg<LogoCtrlCanvasImg, LogoW2D.CanvasImgSrc, LogoModel>(() => LogoW2D.GetTestData2(resourceCreator)); }
#if false
      public void TestMosaicsSkillImg1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(() => (new MosaicsSkillCanvasBmp[] { new MosaicsSkillCanvasBmp(null, resourceCreator), new MosaicsSkillCanvasBmp(null, resourceCreator) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillCanvasBmp[] { new MosaicsSkillCanvasBmp(e, resourceCreator), new MosaicsSkillCanvasBmp(e, resourceCreator) })
                                    .SelectMany(m => m)));
      }
      public void TestMosaicsSkillImg2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(() => (new MosaicsSkillCanvasImg[] { new MosaicsSkillCanvasImg(null, resourceCreator), new MosaicsSkillCanvasImg(null, resourceCreator) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillCanvasImg[] { new MosaicsSkillCanvasImg(e, resourceCreator), new MosaicsSkillCanvasImg(e, resourceCreator) })
                                    .SelectMany(m => m)));
      }
      public void TestMosaicsGroupImg1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(() => (new MosaicsGroupCanvasBmp[] { new MosaicsGroupCanvasBmp(null, resourceCreator), new MosaicsGroupCanvasBmp(null, resourceCreator) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupCanvasBmp[] { new MosaicsGroupCanvasBmp(e, resourceCreator), new MosaicsGroupCanvasBmp(e, resourceCreator) })
                                     .SelectMany(m => m)));
      }
      public void TestMosaicsGroupImg2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(() => (new MosaicsGroupCanvasImg[] { new MosaicsGroupCanvasImg(null, resourceCreator), new MosaicsGroupCanvasImg(null, resourceCreator) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupCanvasImg[] { new MosaicsGroupCanvasImg(e, resourceCreator), new MosaicsGroupCanvasImg(e, resourceCreator) })
                                     .SelectMany(m => m)));
      }
      public void TestMosaicsImg1(ICanvasResourceCreator resourceCreator) {
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
      public void TestMosaicsImg2(ICanvasResourceCreator resourceCreator) {
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
      public void TestFlag1 (ICanvasResourceCreator resourceCreator) { TestAppCanvasBmp(() => new FlagCanvasBmp [] { new FlagCanvasBmp(resourceCreator) }); }
      public void TestFlag2 (ICanvasResourceCreator resourceCreator) { TestAppCanvasImg(() => new FlagCanvasImg [] { new FlagCanvasImg(resourceCreator) }); }
      public void TestMine1 (ICanvasResourceCreator resourceCreator) { TestAppCanvasBmp(() => new MineCanvasBmp [] { new MineCanvasBmp(resourceCreator) }); }
      public void TestMine2 (ICanvasResourceCreator resourceCreator) { TestAppCanvasImg(() => new MineCanvasImg [] { new MineCanvasImg(resourceCreator) }); }
      public void TestSmile1(ICanvasResourceCreator resourceCreator) {
         var vals = (Smile.EType[])Enum.GetValues(typeof(Smile.EType));
         TestAppCanvasBmp(() =>
            vals.Select(e => new SmileCanvasBmp(e, resourceCreator))
         );
      }
      public void TestSmile2(ICanvasResourceCreator resourceCreator) {
         var vals = (Smile.EType[])Enum.GetValues(typeof(Smile.EType));
         TestAppCanvasImg(() =>
            vals.Select(e => new SmileCanvasImg(e, resourceCreator))
         );
      }
#endif
      public void TestMosaicXamlCtl()  { TestAppMosaicXamlCtr(()         => new       MosaicXamlController[] { MosaicXamlController .GetTestData() }); }
      public void TestMosaicControl()  { TestAppMosaicControl(()         => new      MosaicImageController[] { MosaicImageController.GetTestData() }); }
      public void TestMosaicsImg()     { TestAppMosaicImage                                                          (MosaicImg     .GetTestData); }
      public void TestLogos()          { TestAppAnimated<      LogoWBmp.Controller,       LogoWBmp,        LogoModel>(LogoWBmp      .GetTestData); }
      public void TestMosaicSkillImg() { TestAppAnimated<MosaicSkillImg.Controller, MosaicSkillImg, MosaicSkillModel>(MosaicSkillImg.GetTestData); }
      public void TestMosaicGroupImg() { TestAppAnimated<MosaicGroupImg.Controller, MosaicGroupImg, MosaicGroupModel>(MosaicGroupImg.GetTestData); }
      public void TestMine()           { TestAppAnimated<          Mine.Controller,       LogoWBmp,        LogoModel>(Mine          .GetTestData); }
      public void TestFlag()           { TestAppSimple<            Flag.Controller,           Flag,        FlagModel>(Flag          .GetTestData); }
      public void TestSmile()          { TestAppSimple<           Smile.Controller,          Smile,       SmileModel>(Smile         .GetTestData); }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         var device = CanvasDevice.GetSharedDevice();
         _onCreateImages = new Action[] {
            () => TestLogos1          (device),
            () => TestLogos2          (device),
#if false
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
#endif
            TestMosaicXamlCtl,
            TestMosaicControl, TestLogos, TestMine, TestMosaicSkillImg, TestMosaicGroupImg, TestMosaicsImg, TestFlag, TestSmile };

         InitializeComponent();

         _page.Content = _panel = new Canvas();

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
      void TestAppAnimatedCanvasBmp<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<CanvasBitmap, TImageView, TImageModel>
         where TImageView       : IImageView<CanvasBitmap, TImageModel>
         where TImageModel      : IAnimatedModel {
         TestApp<CanvasBitmap, DummyMosaicImageType, TImageController, TImageView, TImageView, TImageModel, TImageModel>(funcGetImages);
      }
      void TestAppAnimatedCanvasImg<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<CanvasImageSource, TImageView, TImageModel>
         where TImageView       : IImageView<CanvasImageSource, TImageModel>
         where TImageModel      : IAnimatedModel {
         TestApp<CanvasImageSource, DummyMosaicImageType, TImageController, TImageView, TImageView, TImageModel, TImageModel>(funcGetImages);
      }

      void TestAppSimple<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<WriteableBitmap, TImageView, TImageModel>
         where TImageView       : IImageView<WriteableBitmap, TImageModel>
         where TImageModel      : IImageModel
      {
         TestApp<WriteableBitmap, DummyMosaicImageType, TImageController, TImageView, DummyView<WriteableBitmap>, TImageModel, DummyModel>(funcGetImages);
      }

      void TestAppAnimated<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<WriteableBitmap, TImageView, TImageModel>
         where TImageView       : IImageView<WriteableBitmap, TImageModel>
         where TImageModel      : IAnimatedModel
      {
         TestApp<WriteableBitmap, DummyMosaicImageType, TImageController, TImageView, TImageView, TImageModel, TImageModel>(funcGetImages);
      }

      void TestAppMosaicImage(Func<IEnumerable<MosaicImg.Controller>> funcGetImages) {
         TestApp<WriteableBitmap, Nothing, MosaicImg.Controller, MosaicImg, MosaicImg, MosaicAnimatedModel<Nothing>, MosaicAnimatedModel<Nothing>>(funcGetImages);
      }

      void TestAppMosaicControl(Func<IEnumerable<MosaicImageController>> funcGetImages) {
         TestApp<Image, WriteableBitmap, MosaicImageController, MosaicImageView, DummyView<Image>, MosaicDrawModel<WriteableBitmap>, DummyModel>(funcGetImages);
      }

      void TestAppMosaicXamlCtr(Func<IEnumerable<MosaicXamlController>> funcGetImages) {
         TestApp<Panel, ImageSource, MosaicXamlController, MosaicXamlView, DummyView<Panel>, MosaicDrawModel<ImageSource>, DummyModel>(funcGetImages);
      }
      #endregion wrappers

      void TestApp<TImage, TMosaicImageInner, TImageController, TImageView, TAImageView, TImageModel, TAnimatedModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImage            : class
         where TMosaicImageInner : class
         where TImageController  : ImageController<TImage, TImageView, TImageModel>
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

                        cnvsCtrl.Draw += (s, ev) => {
                           ev.DrawingSession.DrawImage(imgObj.Image as CanvasBitmap, new Windows.Foundation.Rect(0, 0, cnvsCtrl.Width, cnvsCtrl.Height));
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
            images.Select(x => x as RotatedImg<TImage>)
               .Where(x => x != null)
               .Where(x => typeof(TImage) == typeof(CanvasImageSource))
               .ToList()
               .ForEach(img => {
                  img.Rotate = enable;
                  if (img is PolarLightsImg<TImage>)
                     (img as PolarLightsImg<TImage>).PolarLights = enable;
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
