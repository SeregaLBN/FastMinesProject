using System;
using System.ComponentModel;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Microsoft.Graphics.Canvas;
using Fmg.Common;
using Fmg.Common.UI;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Types;
using Fmg.Core.Mosaic;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Mosaic.Win2d;
using Fmg.Uwp.Mosaic.Xaml;
using IMosaicController = Fmg.Core.Mosaic.IMosaicController<
        Windows.UI.Xaml.FrameworkElement,
        object,
        Fmg.Core.Mosaic.IMosaicView<
                Windows.UI.Xaml.FrameworkElement,
                object,
                Fmg.Core.Mosaic.IMosaicDrawModel<object>>,
        Fmg.Core.Mosaic.IMosaicDrawModel<object>>;
using MosaicControllerType = Fmg.Core.Mosaic.MosaicController<
        Windows.UI.Xaml.FrameworkElement,
        object,
        Fmg.Core.Mosaic.IMosaicView<
                Windows.UI.Xaml.FrameworkElement,
                object,
                Fmg.Core.Mosaic.IMosaicDrawModel<object>>,
        Fmg.Core.Mosaic.IMosaicDrawModel<object>>;

namespace Fmg {

    public sealed partial class MosaicPage : Page, INotifyPropertyChanged {

        private IMosaicController _mosaicController;
        private ITimer GameTimer { get; }

        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary> Mosaic controller </summary>
        public IMosaicController MosaicController {
            get {
                if (_mosaicController == null) {
                    IMosaicController ctrl;
                    if (_canvasVirtualControl.Visibility == Visibility.Visible)
                        ctrl = new MosaicCanvasVirtualControlController(CanvasDevice.GetSharedDevice(), _canvasVirtualControl) { BindSizeDirection = false, ExtendedManipulation = true };
                    else
                    if (_canvasSwapChainPanel.Visibility == Visibility.Visible)
                        ctrl = new MosaicCanvasSwapChainPanelController(CanvasDevice.GetSharedDevice(), _canvasSwapChainPanel) { BindSizeDirection = false, ExtendedManipulation = true };
                    else
                    if (_canvasControl.Visibility == Visibility.Visible)
                        ctrl = new MosaicXamlController(_canvasControl)                                                        { BindSizeDirection = false, ExtendedManipulation = true };
                    else
                        throw new Exception("Illegal usage...");

                    ctrl.Model.AutoFit = false;
                    MosaicController = ctrl; // call this setter
                }
                return _mosaicController;
            }
            private set {
                if (_mosaicController != null) {
                    _mosaicController.PropertyChanged -= OnMosaicControllerPropertyChanged;
                    _mosaicController.Dispose();
                }
                _mosaicController = value;
                if (_mosaicController != null) {
                    _mosaicController.PropertyChanged += OnMosaicControllerPropertyChanged;
                }
            }
        }

        public MosaicPage() {
            this.InitializeComponent();

            GameTimer = new Timer() {
                Interval = 1000,
                Callback = OnTimerCallback
            };

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
                    MosaicController.CountMines = 3;
                }, CoreDispatcherPriority.High);
            }
        }

        protected override void OnNavigatedTo(NavigationEventArgs ev) {
            base.OnNavigatedTo(ev);

            System.Diagnostics.Debug.Assert(ev.Parameter is MosaicInitData);
            var initParam = ev.Parameter as MosaicInitData;
            MosaicController.SizeField  = initParam.SizeField;
            MosaicController.MosaicType = initParam.MosaicType;
            MosaicController.CountMines = initParam.CountMines;
        }

        private void OnMosaicControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(MosaicControllerType.GameStatus):
                var timer = GameTimer;
                switch (((PropertyChangedExEventArgs<EGameStatus>)ev).NewValue) {
                case EGameStatus.eGSCreateGame:
                case EGameStatus.eGSReady:
                    timer.Reset();
                    break;
                case EGameStatus.eGSPlay:
                    timer.Start();
                    break;
                case EGameStatus.eGSEnd:
                    timer.Pause();
                    break;
                }
                OnTimerCallback(timer); // reload UI
                break;
            case nameof(MosaicControllerType.CountMinesLeft):
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(MinesLeft)));
                break;
            }
        }

        private void GoBack() {
            if (this.Frame != null && this.Frame.CanGoBack)
                this.Frame.GoBack();
        }

        private void OnPageSizeChanged(object sender, RoutedEventArgs ev) {
        }

        private void OnPageLoaded(object sender, RoutedEventArgs ev) {
            Window.Current.CoreWindow.KeyUp += OnKeyUp_CoreWindow;
            //this.DataContext = MosaicController;
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            Window.Current.CoreWindow.KeyUp -= OnKeyUp_CoreWindow;

            MosaicController = null; // call explicit setter

            // Explicitly remove references to allow the Win2D controls to get garbage collected
            _canvasVirtualControl?.RemoveFromVisualTree();
            _canvasVirtualControl = null;
            _canvasSwapChainPanel?.RemoveFromVisualTree();
            _canvasSwapChainPanel = null;

            //Bindings.StopTracking();
            GameTimer.Dispose();
        }

        protected override void OnPointerPressed(PointerRoutedEventArgs ev) {
            var currPoint = ev.GetCurrentPoint(this);
            //using (CreateTracer(GetCallerName(), "pointerId=" + currPoint.PointerId, () => "ev.Handled = " + ev.Handled))
            {

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
            using (CreateTracer(GetCallerName(), "virtKey=" + ev.Key)) {
                base.OnKeyUp(ev);
            }
        }

        private void OnKeyUp_CoreWindow(CoreWindow win, KeyEventArgs ev) {
            //using (CreateTracer(GetCallerName(), "virtKey=" + ev.VirtualKey)) {
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

        private void OnClickBttnBack(object sender, RoutedEventArgs ev) {
            GoBack();
        }
        private void OnClickBttnNewGame(object sender, RoutedEventArgs ev) {
            topAppBar.IsOpen = false;
            bottomAppBar.IsOpen = false;
            MosaicController.GameNew();
        }

        /// <summary> Поменять игру на новый уровень сложности </summary>
        private void ChangeGame(ESkillLevel skill) {
            //if (isPaused())
            //    ChangePause(e);

            if (skill == ESkillLevel.eCustom) {
                // TODO ... dialog box 'Select custom skill level...'
                return;
            }

            var model = MosaicController.Model;
            model.SizeField = skill.GetDefaultSize();
            MosaicController.CountMines = skill.GetNumberMines(model.MosaicType);
        }

        private void OnClickBttnSkillBeginner(object sender, RoutedEventArgs ev) {
            ChangeGame(ESkillLevel.eBeginner);
        }
        private void OnClickBttnSkillAmateur(object sender, RoutedEventArgs ev) {
            ChangeGame(ESkillLevel.eAmateur);
        }
        private void OnClickBttnSkillProfi(object sender, RoutedEventArgs ev) {
            ChangeGame(ESkillLevel.eProfi);
        }
        private void OnClickBttnSkillCrazy(object sender, RoutedEventArgs ev) {
            ChangeGame(ESkillLevel.eCrazy);
        }
        private void OnTimerCallback(ITimer timer) {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(TimeLeft)));
        }

        private string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) {
            return callerName;
        }
        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicPage);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return new Tracer(typeName + "." + callerName, ctorMessage, disposeMessage);
        }

        private int MinesLeft => _mosaicController.CountMinesLeft;

        private string TimeLeft {
            get {
                if (_mosaicController.GameStatus == EGameStatus.eGSEnd)
                    return (GameTimer.Time / 1000.0).ToString(); // show time as float (with milliseconds)

                return (GameTimer.Time / 1000).ToString();       // show time as int (only seconds)
            }
        }

    }

}
