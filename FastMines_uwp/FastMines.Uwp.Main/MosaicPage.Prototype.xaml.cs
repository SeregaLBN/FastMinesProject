using System.ComponentModel;
using System.Diagnostics;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Mosaic;
using Fmg.Core.Types;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Mosaic.Xaml;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
namespace Fmg {

    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MosaicPagePrototype : Page {
        /// <summary> мин отступ от краев экрана для мозаики </summary>

        private MosaicXamlController mosaicController;
        private Panel mosaicContainer;

        public Panel MosaicContainer {
            get {
                if (mosaicContainer == null) {
                    mosaicContainer = new Canvas();
                    ContentRoot.Children.Add(mosaicContainer);
                }
                return mosaicContainer;
            }
        }

        /// <summary> Mosaic controller </summary>
        public MosaicXamlController MosaicController {
            get {
                if (mosaicController == null) {
                    var mc = new MosaicXamlController(MosaicContainer) { // call setter
                        BindSizeDirection = false,
                        ExtendedManipulation = true,
                    };
                    mc.Model.AutoFit = false;
                    MosaicController = mc;
                }
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
                    //MosaicContainer.InvalidateCells(null); // TODO: try remove it
                }
            }
        }

        public MosaicPagePrototype() {
            this.InitializeComponent();

            this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.SizeChanged += OnPageSizeChanged;
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
        }

        protected override void OnNavigatedTo(NavigationEventArgs e) {
            base.OnNavigatedTo(e);

            var initParam = e.Parameter as MosaicInitData;
            Debug.Assert(initParam != null);
            MosaicController.SizeField = initParam.SizeField;
            MosaicController.MosaicType = initParam.MosaicType;
            MosaicController.MinesCount = initParam.MinesCount;

            MosaicController.Model.BackgroundColor = MosaicDrawModelConst.DefaultBkColor;

            // if () // TODO: check if no tablet
            {
                ToolTipService.SetToolTip(bttnNewGame, new ToolTip {Content = "F2"});
                ToolTipService.SetToolTip(bttnSkillBeginner, new ToolTip {Content = "1"});
                ToolTipService.SetToolTip(bttnSkillAmateur, new ToolTip { Content = "2" });
                ToolTipService.SetToolTip(bttnSkillProfi, new ToolTip {Content = "3"});
                ToolTipService.SetToolTip(bttnSkillCrazy, new ToolTip { Content = "4" });
            }
        }

        private void OnMosaicControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(MosaicXamlController.MosaicType):
            case nameof(MosaicXamlController.Matrix):
            case nameof(MosaicXamlController.GameStatus):
            case nameof(MosaicXamlController.SizeField):
            case nameof(MosaicXamlController.MinesCount):
            case nameof(MosaicXamlController.CountFlag):
            case nameof(MosaicXamlController.CountOpen):
            case nameof(MosaicXamlController.CountMinesLeft):
            case nameof(MosaicXamlController.CountClick):
                break;
            }
        }






        private void GoBack() {
            if (this.Frame != null && this.Frame.CanGoBack)
                this.Frame.GoBack();
        }

        private void OnPageSizeChanged(object sender, RoutedEventArgs e) {
            //MosaicController.SizeOptimal();
        }

        private void OnPageLoaded(object sender, RoutedEventArgs e) {
            Window.Current.CoreWindow.KeyUp += OnKeyUp_CoreWindow;
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs e) {
            Window.Current.CoreWindow.KeyUp -= OnKeyUp_CoreWindow;
        }

        private void Mosaic_OnChangedGameStatus(MosaicXamlController sender, PropertyChangedExEventArgs<EGameStatus> ev) {
            Debug.Assert(ReferenceEquals(sender, MosaicController));
            if (sender.GameStatus == EGameStatus.eGSEnd) {
                //this.bottomAppBar.Focus(FocusState.Programmatic);
                bottomAppBar.IsOpen = true;
            }
        }

        //protected override void OnPointerWheelChanged(PointerRoutedEventArgs ev) {
        //    base.OnPointerWheelChanged(ev);
        //}

        protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
            using (new Tracer("OnPointerPressed", null, () => "ev.Handled = " + ev.Handled)) {

                var pointerPoint = ev.GetCurrentPoint(this);
                //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
                var props = pointerPoint.Properties;
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
            using (new Tracer("OnKeyUp", "virtKey=" + ev.Key)) {
                base.OnKeyUp(ev);
            }
        }

        private void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs ev) {
            //using (new Tracer("OnKeyUp_CoreWindow", "virtKey=" + ev.Key)) {
            //ev.Handled = true;
            //switch (ev.VirtualKey) {
            //    case VirtualKey.F2:
            //        break;
            //    default:
            //        ev.Handled = false;
            //        break;
            //}
            //}
        }

        private void OnClickBttnBack(object sender, RoutedEventArgs ev) {
            GoBack();
        }
        private void OnClickBttnNewGame(object sender, RoutedEventArgs ev) {
            topAppBar.IsOpen = false;
            bottomAppBar.IsOpen = false;
            MosaicController.GameNew();
        }

        private void OnClickBttnSkillBeginner(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eBeginner);
        }
        private void OnClickBttnSkillAmateur(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eAmateur);
        }
        private void OnClickBttnSkillProfi(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eProfi);
        }
        private void OnClickBttnSkillCrazy(object sender, RoutedEventArgs ev) {
            MosaicController.SetGame(ESkillLevel.eCrazy);
        }

    }

}
