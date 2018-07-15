using System;
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
using fmg.core.types;
using fmg.uwp.draw.img.wbmp;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.wbmp;

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
         public bool Animated { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public long AnimatePeriod { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public int TotalFrames { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public int CurrentFrame { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public SizeDouble Size { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
#pragma warning disable CS0067 // warning CS0067: The event is never used
         public event PropertyChangedEventHandler PropertyChanged; // TODO unusable
#pragma warning restore CS0067
         public void Dispose() { throw new NotImplementedException(); }
      }
      class DummyView : IImageView<WriteableBitmap, DummyModel> {
         public DummyModel Model => throw new NotImplementedException();
         public SizeDouble Size { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }
         public WriteableBitmap Image => throw new NotImplementedException();
#pragma warning disable CS0067 // warning CS0067: The event is never used
         public event PropertyChangedEventHandler PropertyChanged; // TODO unusable
#pragma warning restore CS0067
         public void Dispose() { throw new NotImplementedException(); }
         public void Invalidate() { throw new NotImplementedException(); }
      }
      public void TestLogos() {
         TestApp<Logo.Controller, Logo, Logo, LogoModel, LogoModel>(() => new Logo.Controller[] {
            new Logo.Controller(),
            new Logo.Controller(),
            new Logo.Controller(),
            new Logo.Controller()
         });
      }
      //public void TestMosaicsSkillImg() {
      //   TestApp(() => (new MosaicsSkillImg[] { new MosaicsSkillImg(null), new MosaicsSkillImg(null) })
      //         .Concat(ESkillLevelEx.GetValues()
      //                              .Select(e => new MosaicsSkillImg[] { new MosaicsSkillImg(e), new MosaicsSkillImg(e) })
      //                              .SelectMany(m => m)));
      //}
      //public void TestMosaicsGroupImg() {
      //   TestApp(() => (new MosaicsGroupImg[] { new MosaicsGroupImg(null), new MosaicsGroupImg(null) })
      //         .Concat(EMosaicGroupEx.GetValues()
      //                               .Select(e => new MosaicsGroupImg[] { new MosaicsGroupImg(e), new MosaicsGroupImg(e) })
      //                               .SelectMany(m => m)));
      //}
      //public void TestMosaicsImg() {
      //   var rnd = ThreadLocalRandom.Current;
      //   TestApp(() =>
      //      EMosaicEx.GetValues().Select(e => new MosaicsImg() {
      //         MosaicType = e,
      //         SizeField = new Matrisize(2 + rnd.Next(2), 2 + rnd.Next(2))
      //      })
      //      //new List<MosaicsImg>() { new MosaicsImg() {
      //      //   MosaicType = EMosaic.eMosaicSquare1,
      //      //   SizeField = new Matrisize(3 + rnd.Next(4), 4 + rnd.Next(3))
      //      //} }
      //   );
      //}
      public void TestFlag()  { TestApp<Flag.Controller, Flag, DummyView, FlagModel, DummyModel>(() => new Flag.Controller[]  { new Flag.Controller() }); }
      public void TestMine()  { TestApp<Mine.Controller, Logo, Logo, LogoModel, LogoModel>(() => new Mine.Controller[]  { new Mine.Controller() }); }
      public void TestSmile() { TestApp<Smile.Controller, Smile, DummyView, SmileModel, DummyModel>(() => new Smile.Controller[] { new Smile.Controller(SmileModel.EFaceType.Face_WhiteSmiling) }); }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         _onCreateImages = new Action[] { TestLogos, TestMine, /*TestMosaicsSkillImg, TestMosaicsGroupImg, TestMosaicsImg,*/ TestFlag, TestSmile };

         InitializeComponent();

         _page.Content = _panel = new Canvas();

         SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = AppViewBackButtonVisibility.Visible;
         SystemNavigationManager.GetForCurrentView().BackRequested += (s, ev) => { OnNewImages(); ev.Handled = true; };
         if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
            HardwareButtons.BackPressed += (s, ev) => { OnNewImages(); ev.Handled = true; };
         }
         _page.Loaded   += (s, ev) => OnNewImages();
         _page.Unloaded += (s, ev) => _onCloseImages();
      }

      #region main part
      void TestApp<TImageController, TImageView, TAImageView, TImageModel, TAnimatedModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<WriteableBitmap, TImageView, TImageModel>
         where TImageView : IImageView<WriteableBitmap, TImageModel>
         where TAImageView : IImageView<WriteableBitmap, TAnimatedModel>
         where TImageModel : IImageModel
         where TAnimatedModel : IAnimatedModel
      {
         _panel.Children.Clear();
         List<TImageController> images = funcGetImages().ToList();
         ApplicationView.GetForCurrentView().Title = _td.GetTitle<WriteableBitmap, TImageController, TImageView, TImageModel>(images);

         bool testTransparent = _td.Bl;
         images.ForEach(img => _td.ApplySettings<WriteableBitmap, TImageView, TAImageView, TImageModel, TAnimatedModel>(img, testTransparent));

         Image[,] imgControls;

         {
            double sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 100;
            double sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 100;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<WriteableBitmap, TImageController, TImageView, TImageModel> ctr = _td.CellTiling<WriteableBitmap, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;
            imgControls = new Image[ctr.tableSize.Width, ctr.tableSize.Height];

            var callback = ctr.itemCallback;
            foreach (var img in images) {
               ATestDrawing.CellTilingInfo cti = callback(img);
               PointDouble offset = cti.imageOffset;

               var imgControl = new Image {
                  Margin = new Thickness {
                     Left = offset.X,
                     Top = offset.Y
                  },
                  Stretch = Stretch.None
               };

               img.Model.Size = imgSize;

               imgControl.SetBinding(Image.SourceProperty, new Binding {
                  Source = img,
                  Path = new PropertyPath(nameof(img.Image)),
                  Mode = BindingMode.OneWay
               });

               _panel.Children.Add(imgControl);
               imgControls[cti.i, cti.j] = imgControl;
            }
         }

         void onCellTilingHandler() {
            double sizeW = _panel.ActualWidth;
            double sizeH = _panel.ActualHeight;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<WriteableBitmap, TImageController, TImageView, TImageModel> ctr = _td.CellTiling<WriteableBitmap, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               imgObj.Model.Size = imgSize;

               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;
               imgControls[cti.i, cti.j].Margin = new Thickness {
                  Left = offset.X,
                  Top = offset.Y
               };
            }
         }

         void onMousePressed() {
            testTransparent = _td.Bl;
            images.ForEach(img => {
               _td.ApplySettings<WriteableBitmap, TImageView, TAImageView, TImageModel, TAnimatedModel>(img, testTransparent);
            });
            onCellTilingHandler();
         }

         void onSizeChanged(object s, SizeChangedEventArgs ev) {
            onCellTilingHandler();
         };
         void onTapped(object sender, TappedRoutedEventArgs ev) {
            onMousePressed();
         };
         _panel.SizeChanged += onSizeChanged;
         _panel.Tapped      += onTapped;

         _onCloseImages = () => {
            _panel.SizeChanged -= onSizeChanged;
            _panel.Tapped      -= onTapped;
            images.ForEach(img => img.Dispose());
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

   }

}
