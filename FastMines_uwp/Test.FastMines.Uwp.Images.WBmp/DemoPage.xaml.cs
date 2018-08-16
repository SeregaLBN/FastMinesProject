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
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.uwp.img.wbmp;
using fmg.uwp.mosaic.wbmp;
using fmg.uwp.mosaic.xaml;
using DummyMosaicImageType = System.Object;

namespace Test.FastMines.Uwp.Images.WBmp {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing {
         public TestDrawing() : base("WBmp") { }
      }

      TestDrawing _td;
      Panel _panel;
      static readonly int margin = 10; // panel margin - padding to inner images
      Action _onCloseImages;
      Action[] _onCreateImages; // images factory
      int _nextCreateImagesIndex;

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
      public void TestMosaicXamlCtl()  { TestAppMosaicXamlCtr(()         => new       MosaicXamlController[] { MosaicXamlController .GetTestData() }); }
      public void TestMosaicControl()  { TestAppMosaicControl(()         => new      MosaicImageController[] { MosaicImageController.GetTestData() }); }
      public void TestMosaicsImg()     { TestAppMosaicImage                                                          (MosaicImg     .GetTestData); }
      public void TestLogos()          { TestAppAnimated<          Logo.Controller,           Logo,        LogoModel>(Logo          .GetTestData); }
      public void TestMosaicSkillImg() { TestAppAnimated<MosaicSkillImg.Controller, MosaicSkillImg, MosaicSkillModel>(MosaicSkillImg.GetTestData); }
      public void TestMosaicGroupImg() { TestAppAnimated<MosaicGroupImg.Controller, MosaicGroupImg, MosaicGroupModel>(MosaicGroupImg.GetTestData); }
      public void TestMine()           { TestAppAnimated<          Mine.Controller,           Logo,        LogoModel>(Mine          .GetTestData); }
      public void TestFlag()           { TestAppSimple<            Flag.Controller,           Flag,        FlagModel>(Flag          .GetTestData); }
      public void TestSmile()          { TestAppSimple<           Smile.Controller,          Smile,       SmileModel>(Smile         .GetTestData); }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         _onCreateImages = new Action[] { TestMosaicXamlCtl, TestMosaicControl, TestLogos, TestMine, TestMosaicSkillImg, TestMosaicGroupImg, TestMosaicsImg, TestFlag, TestSmile };

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
         where TImage : class
         where TMosaicImageInner : class
         where TImageController : ImageController<TImage, TImageView, TImageModel>
         where TImageView : IImageView<TImage, TImageModel>
         where TAImageView : IImageView<TImage, TAnimatedModel>
         where TImageModel : IImageModel
         where TAnimatedModel : IAnimatedModel
      {
         _panel.Children.Clear();
         List<TImageController> images = funcGetImages().ToList();
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
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<TImage, TImageController, TImageView, TImageModel> ctr = _td.CellTiling<TImage, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;
            if (createImgControls)
               imgControls = new FrameworkElement[images.Count];

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               if (createImgControls) {
                  if (imgIsControl) {
                     var imgControl = imgObj.Image as FrameworkElement;
                     _panel.Children.Add(imgControl);
                     imgControls[ctr.tableSize.Width * cti.j + cti.i] = imgControl;
                  } else {
                     var imgControl = new Image {
                        Stretch = Stretch.None
                     };
                     imgControl.SetBinding(Image.SourceProperty, new Binding {
                        Source = imgObj,
                        Path = new PropertyPath(nameof(imgObj.Image)),
                        Mode = BindingMode.OneWay
                     });

                     _panel.Children.Add(imgControl);
                     imgControls[ctr.tableSize.Width * cti.j + cti.i] = imgControl;
                  }
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

      }
      #endregion

      void OnNextImages() {
         _onCloseImages?.Invoke();

         Action onCreate = _onCreateImages[_nextCreateImagesIndex];
         if (++_nextCreateImagesIndex >= _onCreateImages.Length)
            _nextCreateImagesIndex = 0;
         onCreate();
      }

   }

}
