using System;
using System.Linq;
using System.Collections.Generic;
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
using fmg.uwp.img;
using fmg.uwp.utils;
using fmg.uwp.mosaic.xaml;
using Win2dMosaicCanvasSwapController = fmg.uwp.mosaic.win2d.MosaicCanvasSwapChainPanelController;
using Win2dMosaicCanvasVirtController = fmg.uwp.mosaic.win2d.MosaicCanvasVirtualControlController;
using Win2dMosaicImg                  = fmg.uwp.img.win2d.MosaicImg;
using Win2dMosaicSkillImg             = fmg.uwp.img.win2d.MosaicSkillImg;
using Win2dMosaicGroupImg             = fmg.uwp.img.win2d.MosaicGroupImg;
using Win2dLogo                       = fmg.uwp.img.win2d.Logo;
using Win2dMine                       = fmg.uwp.img.win2d.Mine;
using Win2dSmile                      = fmg.uwp.img.win2d.Smile;
using Win2dFlag                       = fmg.uwp.img.win2d.Flag;
using WBmpMosaicImageController = fmg.uwp.mosaic.wbmp.MosaicImageController;
using WBmpMosaicImg             = fmg.uwp.img.wbmp.MosaicImg;
using WBmpMosaicSkillImg        = fmg.uwp.img.wbmp.MosaicSkillImg;
using WBmpMosaicGroupImg        = fmg.uwp.img.wbmp.MosaicGroupImg;
using WBmpLogo                  = fmg.uwp.img.wbmp.Logo;
using WBmpMine                  = fmg.uwp.img.wbmp.Mine;
using WBmpFlag                  = fmg.uwp.img.wbmp.Flag;
using WBmpSmile                 = fmg.uwp.img.wbmp.Smile;
using IMosaicController = fmg.core.mosaic.IMosaicController<
        Windows.UI.Xaml.FrameworkElement,
        object,
        fmg.core.mosaic.IMosaicView<
                Windows.UI.Xaml.FrameworkElement,
                object,
                fmg.core.mosaic.IMosaicDrawModel<object>>,
        fmg.core.mosaic.IMosaicDrawModel<object>>;
using IImageController = fmg.core.img.IImageController<
        object,
        fmg.core.img.IImageView<
                object,
                fmg.core.img.IImageModel>,
        fmg.core.img.IImageModel>;

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
        private void TestWin2dLogo1(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new Win2dLogo.ControllerBitmap[] { new Win2dLogo.ControllerBitmap(resourceCreator)
                                                 , new Win2dLogo.ControllerBitmap(resourceCreator)
                                                 , new Win2dLogo.ControllerBitmap(resourceCreator)
                                                 , new Win2dLogo.ControllerBitmap(resourceCreator)}
            );
        }
        private void TestWin2dLogo2(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new Win2dLogo.ControllerImgSrc[] { new Win2dLogo.ControllerImgSrc(resourceCreator)
                                                 , new Win2dLogo.ControllerImgSrc(resourceCreator)
                                                 , new Win2dLogo.ControllerImgSrc(resourceCreator)
                                                 , new Win2dLogo.ControllerImgSrc(resourceCreator)}
            );
        }
        private void TestWin2dMine1(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new Win2dMine.ControllerBitmap[] { new Win2dMine.ControllerBitmap(resourceCreator)
                                                 , new Win2dMine.ControllerBitmap(resourceCreator)
                                               //, new Win2dMine.ControllerBitmap(resourceCreator)
                                                 , new Win2dMine.ControllerBitmap(resourceCreator)}
            );
        }
        private void TestWin2dMine2(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                new Win2dMine.ControllerImgSrc[] { new Win2dMine.ControllerImgSrc(resourceCreator)
                                                 , new Win2dMine.ControllerImgSrc(resourceCreator)
                                               //, new Win2dMine.ControllerImgSrc(resourceCreator)
                                                 , new Win2dMine.ControllerImgSrc(resourceCreator)}
            );
        }
        public void TestWin2dMosaicSkillImg1(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
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
            TestApp(() =>
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
            TestApp(() =>
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
            TestApp(() =>
                (new Win2dMosaicGroupImg.ControllerImgSrc[] {
                        new Win2dMosaicGroupImg.ControllerImgSrc(null, resourceCreator),
                        new Win2dMosaicGroupImg.ControllerImgSrc(null, resourceCreator) })
                .Concat(EMosaicGroupEx.GetValues()
                                    .Select(e => new Win2dMosaicGroupImg.ControllerImgSrc[] {
                                        new Win2dMosaicGroupImg.ControllerImgSrc(e, resourceCreator),
                                        new Win2dMosaicGroupImg.ControllerImgSrc(e, resourceCreator) })
                                    .SelectMany(m => m)));
        }
        private void TestWin2dMosaicsImg1(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                    //new List<Win2dMosaicImg.ControllerBitmap>() { new Win2dMosaicImg.ControllerBitmap(resourceCreator) { MosaicType = EMosaic.eMosaicSquare1 } }
                    EMosaicEx.GetValues().Select(e => new Win2dMosaicImg.ControllerBitmap(resourceCreator) { MosaicType = e })
            );
        }
        private void TestWin2dMosaicsImg2(ICanvasResourceCreator resourceCreator) {
            TestApp(() =>
                    //new List<Win2dMosaicImg.ControllerImgSrc>() { new Win2dMosaicImg.ControllerImgSrc(resourceCreator) { MosaicType = EMosaic.eMosaicSquare1 } }
                    EMosaicEx.GetValues().Select(e => new Win2dMosaicImg.ControllerImgSrc(resourceCreator) { MosaicType = e })
            );
        }
        public void TestWin2dFlag1(ICanvasResourceCreator resourceCreator) { TestApp(() => new Win2dFlag.ControllerBitmap[] { new Win2dFlag.ControllerBitmap(resourceCreator) }); }
        public void TestWin2dFlag2(ICanvasResourceCreator resourceCreator) { TestApp(() => new Win2dFlag.ControllerImgSrc[] { new Win2dFlag.ControllerImgSrc(resourceCreator) }); }
        public void TestWin2dSmile1(ICanvasResourceCreator resourceCreator) {
            var vals = (SmileModel.EFaceType[])Enum.GetValues(typeof(SmileModel.EFaceType));
            TestApp(() =>
                vals.Select(e => new Win2dSmile.ControllerBitmap(e, resourceCreator))
            );
        }
        public void TestWin2dSmile2(ICanvasResourceCreator resourceCreator) {
            var vals = (SmileModel.EFaceType[])Enum.GetValues(typeof(SmileModel.EFaceType));
            TestApp(() =>
                vals.Select(e => new Win2dSmile.ControllerImgSrc(e, resourceCreator))
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
                mosaicController.MinesCount = skill.GetNumberMines(mosaicType);
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
                    //new List<MosaicWBmpImg.Controller>() { new MosaicWBmpImg.Controller() { MosaicType = EMosaic.eMosaicSquare1 } }
                    EMosaicEx.GetValues().Select(e => new WBmpMosaicImg.Controller() { MosaicType = e })
            );
        }
        private void TestWBmpLogo() {
            TestApp(() =>
                new WBmpLogo.Controller[] {
                    new WBmpLogo.Controller(),
                    new WBmpLogo.Controller(),
                    new WBmpLogo.Controller(),
                    new WBmpLogo.Controller()}
            );
        }
        private void TestWBmpMosaicSkillImg() {
            TestApp(() =>
                (new WBmpMosaicSkillImg.Controller[] { new WBmpMosaicSkillImg.Controller(null) })
                    .Concat(ESkillLevelEx.GetValues()
                                         .Select(e => new WBmpMosaicSkillImg.Controller[] { new WBmpMosaicSkillImg.Controller(e) })
                                         .SelectMany(m => m)));
        }
        private void TestWBmpMosaicGroupImg() {
            TestApp(() =>
                (new WBmpMosaicGroupImg.Controller[] { new WBmpMosaicGroupImg.Controller(null) })
                    .Concat(EMosaicGroupEx.GetValues()
                                          .Select(e => new WBmpMosaicGroupImg.Controller[] { new WBmpMosaicGroupImg.Controller(e) })
                                          .SelectMany(m => m))
            );
        }
        private void TestWBmpMine() {
            TestApp(() =>
                new WBmpMine.Controller[] { new WBmpMine.Controller() }
            );
        }
        private void TestWBmpFlag() {
            TestApp(() =>
                new WBmpFlag.Controller[] { new WBmpFlag.Controller() }
            );
        }
        private void TestWBmpSmile() {
            TestApp(() =>
                new WBmpSmile.Controller[] { new WBmpSmile.Controller(SmileModel.EFaceType.Face_WhiteSmiling) }
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
                //() => TestWin2dMosaicsCanvasVirtualControl(device),
                //() => TestWin2dMosaicsCanvasSwapControl(device),
                //TestXamlMosaicControl,
                //TestWBmpMosaicControl,
                () => TestWin2dMosaicsImg1    (device),
                //() => TestWin2dMosaicsImg2    (device),
                //() => TestWin2dMosaicSkillImg1(device),
                //() => TestWin2dMosaicSkillImg2(device),
                //() => TestWin2dMosaicGroupImg1(device),
                //() => TestWin2dMosaicGroupImg2(device),
                //() => TestWin2dLogo1          (device),
                //() => TestWin2dLogo2          (device),
                //() => TestWin2dMine1          (device),
                //() => TestWin2dMine2          (device),
                //() => TestWin2dSmile1         (device),
                //() => TestWin2dSmile2         (device),
                //() => TestWin2dFlag1          (device),
                //() => TestWin2dFlag2          (device),
                //TestWBmpLogo,
                //TestWBmpMine,
                //TestWBmpMosaicSkillImg,
                //TestWBmpMosaicGroupImg,
                //TestWBmpMosaicsImg,
                //TestWBmpFlag,
                //TestWBmpSmile
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
                            if (img is CanvasBitmap) {
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
