using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
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

      static readonly int SIZE = 300;
      static readonly int margin = 10;

      class TestDrawing : ATestDrawing { }

      TestDrawing _td;

      public Action TestLogos() {
         return TestApp<Logo, IPaintable, WriteableBitmap, PaintContext<object>, object>(p => new Logo[] {
            new Logo(),
            new Logo(),
            new Logo()
         });
      }
      public Action TestMosaicsSkillImg() {
         return TestApp<MosaicsSkillImg, IPaintable, WriteableBitmap, PaintContext<object>, object>(p =>
            (new MosaicsSkillImg[] { new MosaicsSkillImg(null) })
               .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new MosaicsSkillImg(e)))
         );
      }
      public Action TestMosaicsGroupImg() {
         return TestApp<MosaicsGroupImg, IPaintable, WriteableBitmap, PaintContext<object>, object>(p =>
            (new MosaicsGroupImg[] { new MosaicsGroupImg(null) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new MosaicsGroupImg(e)))
         );
      }
      public Action TestMosaicsImg() {
         return TestApp<MosaicsImg, PaintableWBmp, WriteableBitmap, PaintUwpContext<WriteableBitmap>, WriteableBitmap>(p =>
            EMosaicEx.GetValues().Select(e => new MosaicsImg(e, new Matrisize(3 + R(4), 4 + R(3))))
         );
      }

      private int R(int max) => _td.R(max);
      private bool Bl => _td.Bl; // random bool
      private int Np => _td.NP; // negative or positive

      public DemoPage() {
         InitializeComponent();

         _td = new TestDrawing();
         TestLogos();
      }

      Action TestApp<TImageEx, TPaintable, TImage, TPaintContext, TImageInner>(Func<Tuple<Size, Random>, IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
         where TPaintable : IPaintable
         where TImage : class
         where TImageInner : class
         where TPaintContext : PaintContext<TImageInner>
      {
         _grid.Children.Clear();

         Rect rc = new Rect(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
         List<TImageEx> images = funcGetImages(new Tuple<Size, Random>(rc.Size(), _td.GetRandom)).ToList();

         bool testTransparent = _td.Bl;
         images.Select(x => x as StaticImg<TImage>)
            .Where(x => x != null)
            .ToList()
            .ForEach(img => _td.ApplyRandom<TPaintable, TImage, TPaintContext, TImageInner>(img, testTransparent));

         ATestDrawing.CellTilingResult<TImageEx> ctr = _td.CellTiling(rc, images, testTransparent);
         Size imgSize = ctr.imageSize;
         for (var colNum = 0; colNum < ctr.tableSize.Width; ++colNum) {
            _grid.ColumnDefinitions.Add(new ColumnDefinition {
               Width = new GridLength(1, GridUnitType.Star)
            });
         }
         for (var rowNum = 0; rowNum < ctr.tableSize.Height; ++rowNum) {
            _grid.RowDefinitions.Add(new RowDefinition {
               Height = new GridLength(1, GridUnitType.Star)
            });
         }


         Func<TImageEx, ATestDrawing.CellTilingInfo> callback = ctr.itemCallback;
         foreach (var imgObj in images) {
            ATestDrawing.CellTilingInfo cti = callback(imgObj);
            PointDouble offset = cti.imageOffset;

            Image imgCntrl = new Image {
               //Margin = new Thickness {
               //   Left = offset.X,
               //   Top = offset.Y
               //}
            };

            if (imgObj is StaticImg<TImage>) {
               StaticImg<TImage> simg = imgObj as StaticImg<TImage>;
               simg.Size = imgSize;

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
            }

            _grid.Children.Add(imgCntrl);
            Grid.SetRow(imgCntrl, cti.i);
            Grid.SetColumn(imgCntrl, cti.j);
         }

         Action onClose = () => {
            images.Select(x => x as StaticImg<TImage>)
               .Where(x => x != null)
               .ToList()
               .ForEach(img => img.Dispose());
            images.Clear();
         };
         return onClose;
      }

   }

}
