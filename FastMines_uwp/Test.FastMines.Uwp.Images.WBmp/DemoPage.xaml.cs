using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.data.controller.types;
using fmg.uwp.draw.img.wbmp;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.wbmp;

namespace Test.FastMines.Uwp.Images.WBmp {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing { }

      TestDrawing _td;
      Panel _panel;
      static readonly int margin = 10; // panel margin - padding to inner images
      Action _onCloseImages;
      Action[] _onCreateImages; // images factory
      int _nextCreateImagesIndex;

      #region images Fabrica
      public void TestLogos() {
         TestAppW(rnd => new Logo[] {
            new Logo(),
            new Logo(),
            new Logo(),
            new Logo()
         });
      }
      public void TestMosaicsSkillImg() {
         TestAppW(rnd => (new MosaicsSkillImg[] { new MosaicsSkillImg(null), new MosaicsSkillImg(null) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillImg[] { new MosaicsSkillImg(e), new MosaicsSkillImg(e) })
                                    .SelectMany(m => m)));
      }
      public void TestMosaicsGroupImg() {
         TestAppW(rnd => (new MosaicsGroupImg[] { new MosaicsGroupImg(null), new MosaicsGroupImg(null) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupImg[] { new MosaicsGroupImg(e), new MosaicsGroupImg(e) })
                                     .SelectMany(m => m)));
      }
      public void TestMosaicsImg() {
         TestAppW(rnd =>
            EMosaicEx.GetValues().Select(e => new MosaicsImg(e, new Matrisize(3 + _td.R(4), 4 + _td.R(3))))
         );
      }
      public void TestFlag()  { TestAppW(rnd => new Flag[]  { new Flag() }); }
      public void TestMine()  { TestAppW(rnd => new Mine[]  { new Mine() }); }
      public void TestSmile() { TestAppW(rnd => new Smile[] { new Smile() }); }
      #endregion


      public DemoPage() {
         _td = new TestDrawing();

         _onCreateImages = new Action[] { TestLogos, TestMosaicsSkillImg, TestMosaicsGroupImg, TestMosaicsImg, TestFlag, TestMine, TestSmile };

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
      void TestAppW<TImageEx>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class {
         TestApp<TImageEx, PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>, WriteableBitmap>(funcGetImages);
      }

      void TestApp<TImageEx, TPaintable, TImage, TPaintContext, TImageInner>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
         where TPaintable : IPaintable
         where TImage : class
         where TImageInner : class
         where TPaintContext : PaintContext<TImageInner>
      {
         _panel.Children.Clear();
         List<TImageEx> images = funcGetImages(_td.GetRandom).ToList();

         bool testTransparent = _td.Bl;
         images.Select(x => x as StaticImg<TImage>)
            .Where(x => x != null)
            .ToList()
            .ForEach(img => _td.ApplyRandom<TPaintable, TImage, TPaintContext, TImageInner>(img, testTransparent));

         Image[,] imgControls;

         {
            double sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 100;
            double sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 100;
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            ATestDrawing.CellTilingResult<TImageEx> ctr = _td.CellTiling(rc, images, testTransparent);
            Size imgSize = ctr.imageSize;
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

               if (imgObj is StaticImg<TImage>) {
                  StaticImg<TImage> simg = imgObj as StaticImg<TImage>;
                  simg.Size = imgSize;

                  imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                     Source = simg,
                     Path = new PropertyPath(nameof(StaticImg<TImage>.Image)),
                     Mode = BindingMode.OneWay
                  });
               } else
               if (imgObj is Flag) {
                  imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                     Source = imgObj,
                     Path = new PropertyPath(nameof(Flag.Image)),
                     Mode = BindingMode.OneWay
                  });
               } else
               if (imgObj is Mine) {
                  imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                     Source = imgObj,
                     Path = new PropertyPath(nameof(Mine.Image)),
                     Mode = BindingMode.OneWay
                  });
               } else
               if (imgObj is Smile) {
                  imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                     Source = imgObj,
                     Path = new PropertyPath(nameof(Smile.Image)),
                     Mode = BindingMode.OneWay
                  });
               } else {
                  throw new Exception("Unsupported image type");
               }

               _panel.Children.Add(imgCntrl);
               imgControls[cti.i, cti.j] = imgCntrl;
            }
         }

         _panel.SizeChanged += (s, ev) => {
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
               if (imgObj is Flag) {
                  // none
               } else
               if (imgObj is Mine) {
                  // none
               } else
               if (imgObj is Smile) {
                  // none
               } else {
                  throw new Exception("Unsupported image type");
               }

               imgControls[cti.i, cti.j].Margin = new Thickness {
                  Left = offset.X,
                  Top = offset.Y
               };
            }
         };

         _onCloseImages = () => {
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

   }

}
