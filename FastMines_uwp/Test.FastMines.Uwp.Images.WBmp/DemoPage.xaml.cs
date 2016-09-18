using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
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

      #region images Fabrica
      public Action TestLogos() {
         return TestApp<Logo, IPaintable, WriteableBitmap, PaintContext<object>, object>(rnd => new Logo[] {
            new Logo(),
            new Logo(),
            new Logo()
         });
      }
      public Action TestMosaicsSkillImg() {
         return TestAppW(rnd =>
            (new MosaicsSkillImg[] { new MosaicsSkillImg(null) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillImg(e)))
         );
      }
      public Action TestMosaicsGroupImg() {
         return TestAppW(rnd =>
            (new MosaicsGroupImg[] { new MosaicsGroupImg(null) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupImg(e)))
         );
      }
      public Action TestMosaicsImg() {
         return TestAppW(rnd =>
            EMosaicEx.GetValues().Select(e => new MosaicsImg(e, new Matrisize(3 + _td.R(4), 4 + _td.R(3))))
         );
      }
      #endregion


      public DemoPage() {
         InitializeComponent();
         _page.Content = _panel = new Canvas();

         _td = new TestDrawing();
         TestLogos();
      }

      #region main part
      Action TestAppW<TImageEx>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
      {
         return TestApp<TImageEx, PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>, WriteableBitmap>(funcGetImages);
      }

      Action TestApp<TImageEx, TPaintable, TImage, TPaintContext, TImageInner>(Func<Random, IEnumerable<TImageEx>> funcGetImages)
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
            double sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 300;
            double sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 300;
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

                  #region binding
                  imgCntrl.SetBinding(Image.SourceProperty, new Binding {
                     Source = simg,
                     Path = new PropertyPath(nameof(simg.Image)),
                     Mode = BindingMode.OneWay
                  });
                  imgCntrl.SetBinding(Image.WidthProperty, new Binding {
                     Source = simg,
                     Path = new PropertyPath(nameof(simg.Width)),
                     Mode = BindingMode.OneWay
                  });
                  imgCntrl.SetBinding(Image.HeightProperty, new Binding {
                     Source = simg,
                     Path = new PropertyPath(nameof(simg.Height)),
                     Mode = BindingMode.OneWay
                  });
                  #endregion
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
               }

               imgControls[cti.i, cti.j].Margin = new Thickness {
                  Left = offset.X,
                  Top = offset.Y
               };
            }
         };

         Action onClose = () => {
            images.Select(x => x as StaticImg<TImage>)
               .Where(x => x != null)
               .ToList()
               .ForEach(img => img.Dispose());
            images.Clear();
            images = null;
            imgControls = null;
         };
         _page.Unloaded += (s, ev) => onClose();
         return onClose;
      }
      #endregion

   }

}
