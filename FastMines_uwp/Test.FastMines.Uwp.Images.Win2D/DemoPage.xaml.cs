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
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.core.mosaic.draw;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.img.win2d;
using fmg.uwp.draw.mosaic.win2d;
using StaticCanvasBmp = fmg.core.img.StaticImg<Microsoft.Graphics.Canvas.CanvasBitmap>;
using StaticCanvasImg = fmg.core.img.StaticImg<Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource>;
using LogoCanvasBmp = fmg.uwp.draw.img.win2d.Logo.CanvasBmp;
using LogoCanvasImg = fmg.uwp.draw.img.win2d.Logo.CanvasImgSrc;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;
using MosaicsSkillCanvasImg = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasImgSrc;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;
using MosaicsGroupCanvasImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasImgSrc;
using MosaicsCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;
using MosaicsCanvasImg = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasImgSrc;
using SmileCanvasBmp = fmg.uwp.draw.img.win2d.Smile.CanvasBmp;
using SmileCanvasImg = fmg.uwp.draw.img.win2d.Smile.CanvasImgSrc;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using FlagCanvasImg = fmg.uwp.draw.img.win2d.Flag.CanvasImgSrc;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;
using MineCanvasImg = fmg.uwp.draw.img.win2d.Mine.CanvasImgSrc;

namespace Test.FastMines.Uwp.Images.Win2D {

   public sealed partial class DemoPage : Page {

      class TestDrawing : ATestDrawing {
         public TestDrawing() : base("Win2D") { }
      }

      TestDrawing _td;
      Panel _panel;
      static readonly int margin = 10; // panel margin - padding to inner images
      Action _onCloseImages;
      Action[] _onCreateImages; // images factory
      int _nextCreateImagesIndex;
      Action<bool> _onActivated;

      #region images Fabrica
      public void TestLogos1(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasBmp(() => new LogoCanvasBmp[] {
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator),
            new LogoCanvasBmp(resourceCreator)
         });
      }
      public void TestLogos2(ICanvasResourceCreator resourceCreator) {
         TestAppCanvasImg(() => new LogoCanvasImg[] {
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator),
            new LogoCanvasImg(resourceCreator)
         });
      }
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
         SystemNavigationManager.GetForCurrentView().BackRequested += (s, ev) => { OnNewImages(); ev.Handled = true; };
         if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
            HardwareButtons.BackPressed += (s, ev) => { OnNewImages(); ev.Handled = true; };
         }
         _page.Loaded   += (s, ev) => OnNewImages();
         _page.Unloaded += (s, ev) => _onCloseImages();
      }

      #region main part
      void TestAppCanvasBmp<TImageEx>(Func<IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
      {
         TestApp<TImageEx, PaintableWin2D, CanvasBitmap, PaintUwpContext<CanvasBitmap>, CanvasBitmap>(funcGetImages);
      }
      void TestAppCanvasImg<TImageEx>(Func<IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
      {
         TestApp<TImageEx, PaintableWin2D, CanvasImageSource, PaintUwpContext<CanvasBitmap>, CanvasBitmap>(funcGetImages);
      }

      void TestApp<TImageEx, TPaintable, TImage, TPaintContext, TImageInner>(Func<IEnumerable<TImageEx>> funcGetImages)
         where TImageEx : class
         where TPaintable : IPaintable
         where TImage : DependencyObject, ICanvasResourceCreator
         where TImageInner : class
         where TPaintContext : PaintContext<TImageInner>
      {
         _panel.Children.Clear();
         var images = funcGetImages().ToList();
         ApplicationView.GetForCurrentView().Title = _td.GetTitle(images);

         var testTransparent = _td.Bl;
         images.Select(x => x as StaticImg<TImage>)
            .Where(x => x != null)
            .ToList()
            .ForEach(img => _td.ApplyRandom<TPaintable, TImage, TPaintContext, TImageInner>(img, testTransparent));

         FrameworkElement[,] imgControls;

         {
            var sizeW = _panel.ActualWidth;  if (sizeW <= 0) sizeW = 100;
            var sizeH = _panel.ActualHeight; if (sizeH <= 0) sizeH = 100;
            var rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            var ctr = _td.CellTiling<TImageEx, TImage>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;
            imgControls = new FrameworkElement[ctr.tableSize.Width, ctr.tableSize.Height];

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               var cti = callback(imgObj);
               var offset = cti.imageOffset;

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
                     var simg = imgObj as StaticCanvasImg;
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
                     var simg = imgObj as StaticCanvasBmp;
                     simg.Size = imgSize;
                     simg.PropertyChanged += (s, ev) => {
                        if (ev.PropertyName == nameof(simg.Image))
                           cnvsCtrl.Invalidate();
                     };
                     cnvsCtrl.SetBinding(CanvasControl.WidthProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(StaticCanvasBmp.Size)),
                        Converter = new SizeConverter(true),
                        Mode = BindingMode.OneWay
                     });
                     cnvsCtrl.SetBinding(CanvasControl.HeightProperty, new Binding {
                        Source = simg,
                        Path = new PropertyPath(nameof(StaticCanvasBmp.Size)),
                        Converter = new SizeConverter(false),
                        Mode = BindingMode.OneWay
                     });
                  } else
                  if (imgObj is FlagCanvasBmp) {
                     var fimg = imgObj as FlagCanvasBmp;
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
                     var simg = imgObj as SmileCanvasBmp;
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
            var sizeW = ev.NewSize.Width;
            var sizeH = ev.NewSize.Height;
            var rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

            var ctr = _td.CellTiling<TImageEx, TImage>(rc, images, testTransparent);
            var imgSize = ctr.imageSize;

            var callback = ctr.itemCallback;
            foreach (var imgObj in images) {
               var cti = callback(imgObj);
               var offset = cti.imageOffset;

               if (imgObj is StaticImg<TImage>) {
                  var simg = imgObj as StaticImg<TImage>;
                  simg.Size = imgSize;
               } else
               if (imgObj is Flag.AFlagImageWin2D<TImage>) {
                  var fimg = imgObj as Flag.AFlagImageWin2D<TImage>;
                  fimg.Width = imgSize.Width;
                  fimg.Height = imgSize.Height;
               } else
               if (imgObj is Smile.ASmileImageWin2D<TImage>) {
                  var simg = imgObj as Smile.ASmileImageWin2D<TImage>;
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
            images.Select(x => x as IDisposable)
               .Where(x => x != null)
               .ToList()
               .ForEach(img => img.Dispose());
            images.Clear();
            images = null;
            imgControls = null;
         };

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
      }
      #endregion


      void OnNewImages() {
         _onCloseImages?.Invoke();

         var onCreate = _onCreateImages[_nextCreateImagesIndex];
         if (++_nextCreateImagesIndex >= _onCreateImages.Length)
            _nextCreateImagesIndex = 0;
         onCreate();
      }

      public void Animation(bool enable) {
         _onActivated?.Invoke(enable);
      }

   }

   public sealed class SizeConverter : IValueConverter {
      private readonly bool _width;
      public SizeConverter(bool width) {
         _width = width;
      }
      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble(_width
            ? ((Size)value).Width
            : ((Size)value).Height);
      }
      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         throw new NotImplementedException("Not supported...");
      }
   }

}
