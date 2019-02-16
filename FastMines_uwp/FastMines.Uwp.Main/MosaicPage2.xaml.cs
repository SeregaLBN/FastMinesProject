using System;
using System.ComponentModel;
using System.Reactive.Linq;
using Windows.System;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Windows.UI.ViewManagement;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.core.types.click;
using fmg.core.mosaic;
using fmg.uwp.utils;
using Logger = fmg.common.LoggerSimple;
using MosaicControllerSwap = fmg.uwp.mosaic.win2d.MosaicCanvasSwapChainPanelController;

namespace fmg {

    public sealed partial class MosaicPage2 : Page {

        private MosaicControllerSwap mosaicController;

        /// <summary> Mosaic controller </summary>
        public MosaicControllerSwap MosaicController {
            get {
                if (mosaicController == null)
                    MosaicController = new MosaicControllerSwap(CanvasDevice.GetSharedDevice(), _canvasSwapChainPanel); // call setter
                return mosaicController;
            }
            private set {
                if (mosaicController != null) {
                    mosaicController.PropertyChanged -= OnMosaicControllerPropertyChanged;
                    mosaicController.Dispose();
                }
                mosaicController = value;
                if (mosaicController != null) {
                    mosaicController.PropertyChanged += OnMosaicControllerPropertyChanged;
                }
            }
        }

        public MosaicPage2() {
            this.InitializeComponent();

            this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.ManipulationMode =
                ManipulationModes.TranslateX |
                ManipulationModes.TranslateY |
                    ManipulationModes.Rotate |
                ManipulationModes.Scale |
                ManipulationModes.TranslateInertia;

            if (Windows.ApplicationModel.DesignMode.DesignModeEnabled) {
                AsyncRunner.InvokeFromUiLater(() => {
                    MosaicController.SizeField = new Matrisize(10, 10);
                    MosaicController.MosaicType = EMosaic.eMosaicRhombus1;
                    MosaicController.MinesCount = 3;
                }, CoreDispatcherPriority.High);
            }

            this.SizeChanged += OnPageSizeChanged;
        }

        protected override void OnNavigatedTo(NavigationEventArgs ev) {
            base.OnNavigatedTo(ev);

            System.Diagnostics.Debug.Assert(ev.Parameter is MosaicInitData);
            var initParam = ev.Parameter as MosaicInitData;
            MosaicController.SizeField  = initParam.SizeField;
            MosaicController.MosaicType = initParam.MosaicType;
            MosaicController.MinesCount = initParam.MinesCount;
        }

        private void OnMosaicControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
        }


        private void GoBack() {
            if (this.Frame != null && this.Frame.CanGoBack)
                this.Frame.GoBack();
        }

        private void OnPageSizeChanged(object sender, SizeChangedEventArgs ev) {
            MosaicController.SizeOptimal();
        }

        private void OnPageLoaded(object sender, RoutedEventArgs e) {
            Window.Current.CoreWindow.KeyUp += OnKeyUp_CoreWindow;
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs e) {
            Window.Current.CoreWindow.KeyUp -= OnKeyUp_CoreWindow;

            MosaicController = null; // call explicit setter

            // Explicitly remove references to allow the Win2D controls to get garbage collected
            _canvasSwapChainPanel.RemoveFromVisualTree();
            _canvasSwapChainPanel = null;

            //Bindings.StopTracking();
        }

        protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
            var currPoint = ev.GetCurrentPoint(this);
            using (new Tracer(GetCallerName(), "pointerId=" + currPoint.PointerId, () => "ev.Handled = " + ev.Handled)) {

                //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
                var props = currPoint.Properties;
                // Ignore button chords with the left, right, and middle buttons
                if (!props.IsLeftButtonPressed && !props.IsRightButtonPressed && !props.IsMiddleButtonPressed) {
                    // If back or foward are pressed (but not both) navigate appropriately
                    var backPressed = props.IsXButton1Pressed;
                    if (backPressed) {
                        ev.Handled = true;
                        GoBack();
                    }
                }

                if (!ev.Handled)
                    base.OnPointerPressed(ev);
            }
        }

        protected override void OnKeyUp(KeyRoutedEventArgs ev) {
            using (new Tracer(GetCallerName(), "virtKey=" + ev.Key)) {
                base.OnKeyUp(ev);
            }
        }

        private void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs ev) {
            //using (new Tracer(GetCallerName(), "virtKey=" + ev.VirtualKey)) {
            //ev.Handled = true;
            //switch (ev.VirtualKey) {
            //case VirtualKey.F2:
            //    break;
            //default:
            //    ev.Handled = false;
            //    break;
            //}
            //}
        }

        private void OnClickBttnBack___________not_binded(object sender, RoutedEventArgs ev) {
            GoBack();
        }
        private void OnClickBttnNewGame___________not_binded(object sender, RoutedEventArgs ev) {
            MosaicController.GameNew();
        }

        private void OnClickBttnSkillBeginner___________not_binded(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eBeginner);
        }
        private void OnClickBttnSkillAmateur___________not_binded(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eAmateur);
        }
        private void OnClickBttnSkillProfi___________not_binded(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eProfi);
        }
        private void OnClickBttnSkillCrazy___________not_binded(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eCrazy);
        }

        static string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) { return callerName; }

    }

}
