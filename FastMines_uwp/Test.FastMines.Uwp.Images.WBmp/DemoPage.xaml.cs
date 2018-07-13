using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
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
      //public void TestLogos() {
      //   TestAppW(() => new Logo[] {
      //      new Logo(),
      //      new Logo(),
      //      new Logo(),
      //      new Logo()
      //   });
      //}
      //public void TestMosaicsSkillImg() {
      //   TestAppW(() => (new MosaicsSkillImg[] { new MosaicsSkillImg(null), new MosaicsSkillImg(null) })
      //         .Concat(ESkillLevelEx.GetValues()
      //                              .Select(e => new MosaicsSkillImg[] { new MosaicsSkillImg(e), new MosaicsSkillImg(e) })
      //                              .SelectMany(m => m)));
      //}
      //public void TestMosaicsGroupImg() {
      //   TestAppW(() => (new MosaicsGroupImg[] { new MosaicsGroupImg(null), new MosaicsGroupImg(null) })
      //         .Concat(EMosaicGroupEx.GetValues()
      //                               .Select(e => new MosaicsGroupImg[] { new MosaicsGroupImg(e), new MosaicsGroupImg(e) })
      //                               .SelectMany(m => m)));
      //}
      //public void TestMosaicsImg() {
      //   var rnd = ThreadLocalRandom.Current;
      //   TestAppW(() =>
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
      public void TestFlag()  { TestAppW<Flag.Controller, Flag, FlagModel>(() => new Flag.Controller[]  { new Flag.Controller() }); }
      //public void TestMine()  { TestAppW(() => new Mine[]  { new Mine() }); }
      //public void TestSmile() { TestAppW(() => new Smile[] { new Smile() }); }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         _onCreateImages = new Action[] { /*TestLogos, TestMosaicsSkillImg, TestMosaicsGroupImg, TestMosaicsImg,*/ TestFlag/*, TestMine, TestSmile*/ };

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
      void TestAppW<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<WriteableBitmap, TImageView, TImageModel>
         where TImageView : IImageView<WriteableBitmap, TImageModel>
         where TImageModel : IImageModel 
      {
         TestApp<TImageController, TImageView, TImageModel>(funcGetImages);
      }

      void TestApp<TImageController, TImageView, TImageModel>(Func<IEnumerable<TImageController>> funcGetImages)
         where TImageController : ImageController<WriteableBitmap, TImageView, TImageModel>
         where TImageView : IImageView<WriteableBitmap, TImageModel>
         where TImageModel : IImageModel 
      {
         _panel.Children.Clear();
         List<TImageController> images = funcGetImages().ToList();
         ApplicationView.GetForCurrentView().Title = _td.GetTitle<WriteableBitmap, TImageController, TImageView, TImageModel>(images);

         bool testTransparent = _td.Bl;
         images.ForEach(img => _td.ApplyRandom<WriteableBitmap, TImageView, TImageModel>(img, testTransparent));

         Image[,] imgControls;

         {
            double sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 100;
            double sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 100;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<WriteableBitmap, TImageView, TImageModel> ctr = _td.CellTiling<WriteableBitmap, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;
            imgControls = new Image[ctr.tableSize.Width, ctr.tableSize.Height];

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               Image imgCntrl = new Image {
                  Margin = new Thickness {
                     Left = offset.X,
                     Top = offset.Y
                  },
                  Stretch = Stretch.None
               };

               imgObj.Model.Size = imgSize;

               imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                  Source = imgObj,
                  Path = new PropertyPath(nameof(imgObj.Image)),
                  Mode = BindingMode.OneWay
               });

               _panel.Children.Add(imgCntrl);
               imgControls[cti.i, cti.j] = imgCntrl;
            }
         }

         SizeChangedEventHandler sceh = (s, ev) => {
            double sizeW = ev.NewSize.Width;
            double sizeH = ev.NewSize.Height;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<WriteableBitmap, TImageView, TImageModel> ctr = _td.CellTiling<WriteableBitmap, TImageController, TImageView, TImageModel>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               ATestDrawing.CellTilingInfo cti = callback(imgObj);
               PointDouble offset = cti.imageOffset;

               imgObj.Model.Size = imgSize;

               imgControls[cti.i, cti.j].Margin = new Thickness {
                  Left = offset.X,
                  Top = offset.Y
               };
            }
         };
         _panel.SizeChanged += sceh;

         _onCloseImages = () => {
            _panel.SizeChanged -= sceh;
            images.Select(x => x as IDisposable)
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

   }

}
