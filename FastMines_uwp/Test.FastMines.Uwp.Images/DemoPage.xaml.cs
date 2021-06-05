using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.ViewManagement;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Img;
using Fmg.Core.Types;
using Fmg.Core.Mosaic;
using Fmg.Uwp.Img;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Mosaic.Xaml;
using Win2dMosaicCanvasSwapController = Fmg.Uwp.Mosaic.Win2d.MosaicCanvasSwapChainPanelController;
using Win2dMosaicCanvasVirtController = Fmg.Uwp.Mosaic.Win2d.MosaicCanvasVirtualControlController;
using Win2dMosaicImg                  = Fmg.Uwp.Img.Win2d.MosaicImg;
using Win2dMosaicSkillImg             = Fmg.Uwp.Img.Win2d.MosaicSkillImg;
using Win2dMosaicGroupImg             = Fmg.Uwp.Img.Win2d.MosaicGroupImg;
using Win2dLogo                       = Fmg.Uwp.Img.Win2d.Logo;
using Win2dMine                       = Fmg.Uwp.Img.Win2d.Mine;
using Win2dSmile                      = Fmg.Uwp.Img.Win2d.Smile;
using Win2dFlag                       = Fmg.Uwp.Img.Win2d.Flag;
using WBmpMosaicImageController = Fmg.Uwp.Mosaic.Wbmp.MosaicImageController;
using WBmpMosaicImg             = Fmg.Uwp.Img.Wbmp.MosaicImg     .WBmpController;
using WBmpMosaicSkillImg        = Fmg.Uwp.Img.Wbmp.MosaicSkillImg.WBmpController;
using WBmpMosaicGroupImg        = Fmg.Uwp.Img.Wbmp.MosaicGroupImg.WBmpController;
using WBmpLogo                  = Fmg.Uwp.Img.Wbmp.Logo          .WBmpController;
using WBmpMine                  = Fmg.Uwp.Img.Wbmp.Mine          .WBmpController;
using WBmpFlag                  = Fmg.Uwp.Img.Wbmp.Flag          .WBmpController;
using WBmpSmile                 = Fmg.Uwp.Img.Wbmp.Smile         .WBmpController;
using IMosaicController = Fmg.Core.Mosaic.IMosaicController<
        Windows.UI.Xaml.FrameworkElement,
        object,
        Fmg.Core.Mosaic.IMosaicView<
                Windows.UI.Xaml.FrameworkElement,
                object,
                Fmg.Core.Mosaic.IMosaicDrawModel<object>>,
        Fmg.Core.Mosaic.IMosaicDrawModel<object>>;
using IImageController = Fmg.Core.Img.IImageController<
        object,
        Fmg.Core.Img.IImageView<
                object,
                Fmg.Core.Img.IImageModel>,
        Fmg.Core.Img.IImageModel>;

namespace Test.FastMines.Uwp.Images {

    public sealed partial class DemoPage : Page {

        private TestDrawing _td;
        private Panel _panel;
        private static readonly int margin = 10; // panel margin - padding to inner images
        private Action _onCloseImages;
        private Action[] _onCreateImages; // images factory
        private int _nextCreateImagesIndex;
        private Action<bool> _onActivated;

        #region images Fabrica
        private void TestWin2dLogo(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new IImageController[] { new Win2dLogo.CanvasBmpController(resourceCreator)
                                       , new Win2dLogo.CanvasImgSrcController(resourceCreator)
                                       , new Win2dLogo.CanvasBmpController(resourceCreator)
                                       , new Win2dLogo.CanvasImgSrcController(resourceCreator)}
            );
        }
        private void TestWin2dMine(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new IImageController[] { new Win2dMine.CanvasBmpController(resourceCreator)
                                       , new Win2dMine.CanvasImgSrcController(resourceCreator)
                                       , new Win2dMine.CanvasBmpController(resourceCreator)
                                       , new Win2dMine.CanvasImgSrcController(resourceCreator)}
            );
        }
        public void TestWin2dMosaicSkillImg(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                (new IImageController[] {
                        new Win2dMosaicSkillImg.CanvasBmpController(null, resourceCreator),
                        new Win2dMosaicSkillImg.CanvasImgSrcController(null, resourceCreator) })
                .Concat(ESkillLevelEx.GetValues()
                                    .Select(e => new IImageController[] {
                                        new Win2dMosaicSkillImg.CanvasBmpController(e, resourceCreator),
                                        new Win2dMosaicSkillImg.CanvasImgSrcController(e, resourceCreator) })
                                    .SelectMany(m => m)));
        }
        public void TestWin2dMosaicGroupImg(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                (new IImageController[] {
                        new Win2dMosaicGroupImg.CanvasBmpController(null, resourceCreator),
                        new Win2dMosaicGroupImg.CanvasImgSrcController(null, resourceCreator) })
                .Concat(EMosaicGroupEx.GetValues()
                                    .Select(e => new IImageController[] {
                                        new Win2dMosaicGroupImg.CanvasBmpController(e, resourceCreator),
                                        new Win2dMosaicGroupImg.CanvasImgSrcController(e, resourceCreator) })
                                    .SelectMany(m => m)));
        }
        private void TestWin2dMosaicsImg(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                    //new List<Win2dMosaicImg.ControllerBitmap>() { new Win2dMosaicImg.ControllerBitmap(resourceCreator) { MosaicType = EMosaic.eMosaicSquare1 } }
                    EMosaicEx.GetValues().Select(e =>
                        ((e.Ordinal() % 2) == 0)
                            ? (IImageController)new Win2dMosaicImg.CanvasBmpController(resourceCreator) { MosaicType = e }
                            :                   new Win2dMosaicImg.CanvasImgSrcController(resourceCreator) { MosaicType = e })
            );
        }
        public void TestWin2dFlag(ICanvasResourceCreator resourceCreator) { TestApp(() => new IImageController[] { new Win2dFlag.CanvasBmpController(resourceCreator),
                                                                                                                   new Win2dFlag.CanvasImgSrcController(resourceCreator) }); }
        public void TestWin2dSmile(ICanvasResourceCreator resourceCreator) {
            var vals = (SmileModel.EFaceType[])Enum.GetValues(typeof(SmileModel.EFaceType));
            int i = 0;
            TestApp(() =>
                vals.Select(e =>
                    ((++i % 2) == 0)
                        ? (IImageController)new Win2dSmile.CanvasBmpController(e, resourceCreator)
                        :                   new Win2dSmile.CanvasImgSrcController(e, resourceCreator))
            );
        }

        private static IMosaicController TuneMosaicGameController(IMosaicController mosaicController) {
            if (ThreadLocalRandom.Current.Next(2) == 1) {
                // unmodified controller test
            } else {
                EMosaic mosaicType = EMosaicEx.FromOrdinal(ThreadLocalRandom.Current.Next(EMosaicEx.GetValues().Length));
                ESkillLevel skill = ESkillLevel.eBeginner;

                mosaicController.MosaicType = mosaicType;
                mosaicController.SizeField = skill.GetDefaultSize();
                mosaicController.CountMines = skill.GetNumberMines(mosaicType);
                mosaicController.GameNew();
            }
            return mosaicController;
        }

        private void TestWin2dMosaicsCanvasSwapControl(ICanvasResourceCreator resourceCreator) {
            TestApp(
                () => new IMosaicController[] {
                  //TuneMosaicGameController(new Win2dMosaicCanvasSwapController(resourceCreator)),
                    TuneMosaicGameController(new Win2dMosaicCanvasSwapController(resourceCreator))
            });
        }

        private void TestWin2dMosaicsCanvasVirtualControl(ICanvasResourceCreator resourceCreator) {
            TestApp(
                () => new IMosaicController[] {
                  //TuneMosaicGameController(new Win2dMosaicCanvasVirtController(resourceCreator)),
                    TuneMosaicGameController(new Win2dMosaicCanvasVirtController(resourceCreator))
            });
        }

        private void TestXamlMosaicControl()  {
            TestApp(
                () => new IMosaicController[] {
                    TuneMosaicGameController(new MosaicXamlController())
            });
        }
        private void TestWBmpMosaicControl() {
            TestApp(
                () => new IMosaicController[] {
                    TuneMosaicGameController(new WBmpMosaicImageController())
            });
        }
        private void TestWBmpMosaicsImg() {
            TestApp(
                () =>
                    //new List<WBmpMosaicImg>() { new WBmpMosaicImg() { MosaicType = EMosaic.eMosaicSquare1 } }
                    EMosaicEx.GetValues().Select(e => new WBmpMosaicImg() { MosaicType = e })
            );
        }
        private void TestWBmpLogo() {
            TestApp(() =>
                new WBmpLogo[] {
                    new WBmpLogo(),
                    new WBmpLogo(),
                    new WBmpLogo(),
                    new WBmpLogo()}
            );
        }
        private void TestWBmpMosaicSkillImg() {
            TestApp(() =>
                (new WBmpMosaicSkillImg[] { new WBmpMosaicSkillImg(null) })
                    .Concat(ESkillLevelEx.GetValues()
                                         .Select(e => new WBmpMosaicSkillImg[] { new WBmpMosaicSkillImg(e) })
                                         .SelectMany(m => m)));
        }
        private void TestWBmpMosaicGroupImg() {
            TestApp(() =>
                (new WBmpMosaicGroupImg[] { new WBmpMosaicGroupImg(null) })
                    .Concat(EMosaicGroupEx.GetValues()
                                          .Select(e => new WBmpMosaicGroupImg[] { new WBmpMosaicGroupImg(e) })
                                          .SelectMany(m => m))
            );
        }
        private void TestWBmpMine() {
            TestApp(() =>
                new WBmpMine[] { new WBmpMine() }
            );
        }
        private void TestWBmpFlag() {
            TestApp(() =>
                new WBmpFlag[] { new WBmpFlag() }
            );
        }
        private void TestWBmpSmile() {
            TestApp(() =>
                new WBmpSmile[] { new WBmpSmile(SmileModel.EFaceType.Face_WhiteSmiling) }
            );
        }
        #endregion


        public DemoPage() {
            _td = new TestDrawing("UWP");

#if DEBUG
            MosaicViewCfg.DEBUG_DRAW_FLOW = false; // true - strongly slows rendering
#endif
            var device = CanvasDevice.GetSharedDevice();
            _onCreateImages = new Action[] {
                () => TestWin2dMosaicsCanvasVirtualControl(device),
                () => TestWin2dMosaicsCanvasSwapControl(device),
                TestXamlMosaicControl,
                TestWBmpMosaicControl,                                  // <<<<<<<===----  very slow!
                () => TestWin2dMosaicsImg    (device),
                () => TestWin2dMosaicSkillImg(device),
                () => TestWin2dMosaicGroupImg(device),
                () => TestWin2dLogo          (device),
                () => TestWin2dMine          (device),
                () => TestWin2dSmile         (device),
                () => TestWin2dFlag          (device),
            #region very slow!
                TestWBmpLogo,
                TestWBmpMine,
                TestWBmpMosaicSkillImg,
                TestWBmpMosaicGroupImg,
                TestWBmpMosaicsImg,
                TestWBmpFlag,
                TestWBmpSmile
            #endregion very slow!
            };

            InitializeComponent();

            mosaicContainer.Content = _panel = new Canvas();
            _panel.Background = new SolidColorBrush(Colors.Transparent); // lifehack: otherwise the click does not handled on the empty place

            SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = AppViewBackButtonVisibility.Visible;
            SystemNavigationManager.GetForCurrentView().BackRequested += (s, ev) => { OnNextImages(false); ev.Handled = true; };
            if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
                HardwareButtons.BackPressed += (s, ev) => { OnNextImages(false); ev.Handled = true; };
            }
            prevImagesBtn.Click += (s, ev) => OnNextImages(false);
            refreshButton.Click += (s, ev) => OnNextImages(null);
            nextImagesBtn.Click += (s, ev) => OnNextImages(true);
            this.Loaded         += (s, ev) => {
                nextImagesBtn.Focus(FocusState.Programmatic);
                OnNextImages(null);
            };
            this.Unloaded       += (s, ev) => OnDestroy();
        }

        protected void OnDestroy() {
            _onCloseImages();
            Animator.Singleton.Dispose();
        }

        #region main part

        void TestApp(Func<IEnumerable<IImageController>> funcGetImages) {
            var images = funcGetImages().ToList();
            var binding1 = new Dictionary<IImageController, CanvasControl>();
            var binding2 = new Dictionary<IImageController, Image>();
            void onCanvasBitmapImageControllerPropertyChaged(object sender, PropertyChangedEventArgs ev) {
                var imgObj = (IImageController)sender;
                switch (ev.PropertyName) {
                case nameof(IImageController.Image):
                    binding1[imgObj].Invalidate();
                    break;
                case nameof(IImageController.Size): {
                        var control = binding1[imgObj];
                        var evEx = (PropertyChangedExEventArgs<SizeDouble>)ev;
                        control.Width  = evEx.NewValue.Width;
                        control.Height = evEx.NewValue.Height;
                    }
                    break;
                }
            }
            void onCanvasImgSrcOrWBmpImageControllerPropertyChaged(object sender, PropertyChangedEventArgs ev) {
                var imgObj = (IImageController)sender;
                switch (ev.PropertyName) {
                case nameof(IImageController.Image):
                    binding2[imgObj].Source = (ImageSource)imgObj.Image;
                    break;
                }
            }

            ApplicationView.GetForCurrentView().Title = _td.GetTitle(images);
            _panel.Children.Clear();

            FrameworkElement[] imgControls = null;
            bool testTransparent = false;
            bool isMosaicGameController = (images[0] is Win2dMosaicCanvasSwapController)
                                       || (images[0] is WBmpMosaicImageController)
                                       || (images[0] is Win2dMosaicCanvasVirtController)
                                       || (images[0] is MosaicXamlController);
            bool closed = false;

            void onCellTilingHandler(bool applySettings, bool createImgControls, bool resized) {
                if (isMosaicGameController) // when is this game field...
                    applySettings = false;  // ... then test as is
                resized = resized || createImgControls || applySettings;

                if (applySettings) {
                    testTransparent = _td.Bl;
                    images.ForEach(img => _td.ApplySettings(img, testTransparent));
                }

                double sizeW = _panel.ActualWidth;
                double sizeH = _panel.ActualHeight;
                var rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

                var ctr = _td.CellTiling(rc, images, testTransparent);
                var imgSize = ctr.imageSize;
                if (imgSize.Width <= 0 || imgSize.Height <= 0)
                    return;
                if (createImgControls)
                    imgControls = new FrameworkElement[images.Count];

                var callback = ctr.itemCallback;
                foreach (var imgObj in images) {
                    TestDrawing.CellTilingInfo cti = callback(imgObj);
                    PointDouble offset = cti.imageOffset;

                    if (createImgControls) {
                        var img = imgObj.Image;
                        FrameworkElement imgControl;
                        if (img is FrameworkElement imgFE) {
                            imgControl = imgFE;
                    #region lifehack for XAML
                            if (img.GetType() == typeof(MosaicXamlController)) {
                                imgControl.Visibility = Visibility.Collapsed;
                                AsyncRunner.InvokeFromUiLater(() => { imgControl.Visibility = Visibility.Visible; });
                            }
                    #endregion
                        } else {
                    #region CanvasImageSource or WriteableBitmap
                            if ((img is CanvasImageSource) ||
                                (img is WriteableBitmap))
                            {
                                var image = new Image {
                                    Stretch = Stretch.None
                                };
                                imgControl = image;
                                binding2.Add(imgObj, image);
                                imgObj.PropertyChanged += onCanvasImgSrcOrWBmpImageControllerPropertyChaged;
                            } else
                    #endregion
                    #region CanvasBitmap
                            if (img is CanvasBitmap) {
                                var cnvsCtrl = new CanvasControl {
                                    //ClearColor = ColorExt.RandomColor(_td.GetRandom).Brighter().ToWinColor(),
                                };
                                imgControl = cnvsCtrl;

                                binding1.Add(imgObj, cnvsCtrl);
                                imgObj.PropertyChanged += onCanvasBitmapImageControllerPropertyChaged;

                                void onDraw(CanvasControl s, CanvasDrawEventArgs ev) {
                                    if (closed)
                                        return;
                                    var img2 = imgObj.Image; // reload image !!
                                    ev.DrawingSession.DrawImage(img2 as CanvasBitmap, new Windows.Foundation.Rect(0, 0, cnvsCtrl.Width, cnvsCtrl.Height));
                                }
                                cnvsCtrl.Draw += onDraw;
                                cnvsCtrl.Unloaded += (s, ev) => {
                                    cnvsCtrl.Draw -= onDraw;
                                };
                            } else
                    #endregion
                            {
                                throw new Exception("Unsupported image type: " + img.GetType().FullName);
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
            }
            void onPointerPressed(object sender, PointerRoutedEventArgs ev) {
                //onCellTilingHandler(true, false, false);
            }
            void onTapped(object sender, TappedRoutedEventArgs ev) {
                onCellTilingHandler(true, false, false);
            }
            _panel.SizeChanged += onSizeChanged;
            if (isMosaicGameController)
                _panel.PointerPressed += onPointerPressed;
            else
                _panel.Tapped         += onTapped;

            _onCloseImages = () => {
                closed = true;
                _panel.SizeChanged -= onSizeChanged;
                if (isMosaicGameController)
                    _panel.PointerPressed -= onPointerPressed;
                else
                    _panel.Tapped         -= onTapped;

                foreach (var kv in binding1)
                    kv.Key.PropertyChanged -= onCanvasBitmapImageControllerPropertyChaged;
                binding1.Clear();
                foreach (var kv in binding2)
                    kv.Key.PropertyChanged -= onCanvasImgSrcOrWBmpImageControllerPropertyChaged;
                binding2.Clear();
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

        void OnNextImages(bool? isNext) {
            _onCloseImages?.Invoke();

            if (isNext != null)
                if (isNext.Value) {
                    if (++_nextCreateImagesIndex >= _onCreateImages.Length)
                        _nextCreateImagesIndex = 0;
                } else {
                    if (--_nextCreateImagesIndex < 0)
                        _nextCreateImagesIndex = _onCreateImages.Length - 1;
                }
            _onCreateImages[_nextCreateImagesIndex]();
        }

        public void Animation(bool enable) {
            _onActivated?.Invoke(enable);
        }

    }

}
