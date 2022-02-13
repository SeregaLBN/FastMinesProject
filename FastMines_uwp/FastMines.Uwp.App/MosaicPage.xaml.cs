using System;
using System.ComponentModel;
using Windows.ApplicationModel;
using Windows.UI.Core;
using Windows.UI.Notifications;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Popups;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Fmg.Common;
using Fmg.Common.UI;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Img;
using Fmg.Core.Types;
using Fmg.Core.App.Model;
using Fmg.Uwp.App.Model;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Img.Win2d;
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

namespace Fmg.Uwp.App {

    public sealed partial class MosaicPage : Page, INotifyPropertyChanged {

        private IMosaicController _mosaicController;
        private readonly Smile.CanvasImgSrcController btnNewGameImage;
        private ITimer GameTimer { get; }
        private Func<bool> IsVictory;

        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary> Mosaic controller </summary>
        public IMosaicController MosaicController {
            get {
                if (_mosaicController == null) {
                    IMosaicController ctrl;
                    if (_canvasVirtualControl.Visibility == Visibility.Visible) {
                        var c = new MosaicCanvasVirtualControlController(CanvasDevice.GetSharedDevice(), _canvasVirtualControl) { BindSizeDirection = false, ExtendedManipulation = true };
                        IsVictory = () => c.IsVictory;
                        c.SetOnClickEvent(OnMosaicClickHandler);
                        ctrl = c;
                    } else
                    if (_canvasSwapChainPanel.Visibility == Visibility.Visible) {
                        var c = new MosaicCanvasSwapChainPanelController(CanvasDevice.GetSharedDevice(), _canvasSwapChainPanel) { BindSizeDirection = false, ExtendedManipulation = true };
                        IsVictory = () => c.IsVictory;
                        c.SetOnClickEvent(OnMosaicClickHandler);
                        ctrl = c;
                    } else
                    if (_canvasControl.Visibility == Visibility.Visible) {
                        var c = new MosaicXamlController(_canvasControl)                                                        { BindSizeDirection = false, ExtendedManipulation = true };
                        IsVictory = () => c.IsVictory;
                        c.SetOnClickEvent(OnMosaicClickHandler);
                        ctrl = c;
                    } else
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
                AsyncRunner.InvokeFromUi(() => {
                    MosaicController.SizeField = new Matrisize(10, 10);
                    MosaicController.MosaicType = EMosaic.eMosaicRhombus1;
                    MosaicController.CountMines = 3;
                }, CoreDispatcherPriority.High);
            }

            btnNewGameImage = new Smile.CanvasImgSrcController(SmileModel.EFaceType.Face_WhiteSmiling, CanvasDevice.GetSharedDevice());
        }

        protected override void OnNavigatedTo(NavigationEventArgs ev) {
            base.OnNavigatedTo(ev);
            Windows.ApplicationModel.Core.CoreApplication.LeavingBackground += OnLeavingBackground;

            System.Diagnostics.Debug.Assert((ev.Parameter is MosaicInitData) ||
                                            (ev.Parameter is MosaicPageBackupData));

            if (ev.Parameter is MosaicPageBackupData mosaicPageBackupData) {
                    AsyncRunner.InvokeFromUiLaterDelayed(() => {
                        MosaicController.GameRestore(mosaicPageBackupData.MosaicBackupData);
                        MosaicController.Model.MosaicOffset = mosaicPageBackupData.MosaicOffset;
                        GameTimer.Time = mosaicPageBackupData.PlayTime;
                    },
                    TimeSpan.FromMilliseconds(300) // !large MosaicFrameworkElementController: .Throttle(TimeSpan.FromSeconds(0.2)) // debounce events
                );
            } else {
                var initParam = ev.Parameter as MosaicInitData;
                MosaicController.SizeField  = initParam.SizeField;
                MosaicController.MosaicType = initParam.MosaicType;
                MosaicController.CountMines = initParam.CountMines;
            }
        }

        private void OnMosaicControllerPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(MosaicControllerType.GameStatus):
                var timer = GameTimer;
                switch (((PropertyChangedExEventArgs<EGameStatus>)ev).NewValue) {
                case EGameStatus.eGSCreateGame:
                case EGameStatus.eGSReady:
                    timer.Reset();
                    SetBtnNewGameFaceType(SmileModel.EFaceType.Face_WhiteSmiling);
                    break;
                case EGameStatus.eGSPlay:
                    timer.Start();
                    break;
                case EGameStatus.eGSEnd:
                    timer.Pause();
                    SetBtnNewGameFaceType(IsVictory()
                                ? SmileModel.EFaceType.Face_SmilingWithSunglasses
                                : SmileModel.EFaceType.Face_Disappointed);

                if (GetSkillLevel() != ESkillLevel.eCustom)
                    // сохраняю статистику и чемпиона
                    SaveStatisticAndChampion();

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

        private void OnLeavingBackground(object sender, LeavingBackgroundEventArgs ev) {
            // Your code here.
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            Window.Current.CoreWindow.KeyUp -= OnKeyUp_CoreWindow;

            //MosaicController.SetOnClickEvent(null); // TODO
            (MosaicController as MosaicCanvasVirtualControlController)?.SetOnClickEvent(null);
            (MosaicController as MosaicCanvasSwapChainPanelController)?.SetOnClickEvent(null);
            (MosaicController as MosaicXamlController                )?.SetOnClickEvent(null);
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

        private int MinesLeft => MosaicController.CountMinesLeft;

        private string TimeLeft {
            get {
                if (MosaicController.GameStatus == EGameStatus.eGSEnd)
                    return (GameTimer.Time / 1000.0).ToString(); // show time as float (with milliseconds)

                return (GameTimer.Time / 1000).ToString();       // show time as int (only seconds)
            }
        }

        private CanvasImageSource BtnNewGameImg => btnNewGameImage.Image;

        private async void OnBtnNewTapped(object sender, TappedRoutedEventArgs ev) {
            if (MosaicController.GameStatus != EGameStatus.eGSPlay) {
                MosaicController.GameNew();
                return;
            }

            var dialog = new MessageDialog("New game?");
            dialog.Commands.Add(new UICommand("Yes", new UICommandInvokedHandler(cmd => MosaicController.GameNew())));
            dialog.Commands.Add(new UICommand("No", new UICommandInvokedHandler(cmd => { /* none */ })));
            dialog.CancelCommandIndex = 1;
            dialog.DefaultCommandIndex = 1;
            await dialog.ShowAsync();
        }

        private void OnBtnNewPointerPressed(object sender, PointerRoutedEventArgs ev) {
            SetBtnNewGameFaceType(SmileModel.EFaceType.Face_SavouringDeliciousFood);
        }

        private void OnBtnNewPointerReleased(object sender, PointerRoutedEventArgs ev) {
            SetBtnNewGameFaceType(SmileModel.EFaceType.Face_WhiteSmiling);
        }

        public void OnMosaicClickHandler(ClickResult clickResult) {
            var gs = MosaicController.GameStatus;
            //Logger.Info("OnMosaicClick: down=" + clickResult.IsDown + "; leftClick=" + clickResult.IsLeft + "; gameStatus=" + gs);
            if (clickResult.IsLeft && ((gs == EGameStatus.eGSPlay) || (gs == EGameStatus.eGSReady))) {
                SetBtnNewGameFaceType(clickResult.IsDown
                        ? SmileModel.EFaceType.Face_Grinning
                        : SmileModel.EFaceType.Face_WhiteSmiling);
            }
        }


        private void SetBtnNewGameFaceType(SmileModel.EFaceType btnNewFaceType) {
            btnNewGameImage.Model.FaceType = btnNewFaceType;
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(BtnNewGameImg)));
        }

        public ESkillLevel GetSkillLevel() {
            var mc = MosaicController;
            var mosaicType = mc.MosaicType;
            var sizeFld = mc.SizeField;
            var numberMines = mc.CountMines;
            return ESkillLevelEx.CalcSkillLevel(mosaicType, sizeFld, numberMines);
        }

        /** Сохранить чемпиона && Установить статистику */
        private void SaveStatisticAndChampion() {
            var mc = MosaicController;
            if (mc.GameStatus != EGameStatus.eGSEnd)
                throw new ArgumentException("Invalid method state call");

            // сохраняю все нужные данные
            bool victory = mc.IsVictory;
            ESkillLevel eSkill = GetSkillLevel();
            if (eSkill == ESkillLevel.eCustom)
                return;

            var eMosaic = mc.MosaicType;
            var realCountOpen = victory ? mc.CountMines : mc.CountOpen;
            var playTime = GameTimer.Time;
            var clickCount = mc.CountClick;

            int pos = FastMinesApp.Get.UpdateStatistic(eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
            if (pos >= 0)
                ShowToastNotification("Victory", "Your best result is position #" + (pos + 1));
        }

        public async void ConfirmBackRequested() {
            if (MosaicController.GameStatus != EGameStatus.eGSPlay) {
                Frame.GoBack();
                return;
            }

            var dialog = new MessageDialog("Confirm exit");
            dialog.Commands.Add(new UICommand("Yes", new UICommandInvokedHandler(cmd => Frame.GoBack() )));
            dialog.Commands.Add(new UICommand("No" , new UICommandInvokedHandler(cmd => { /* none */ } )));
            dialog.CancelCommandIndex = 1;
            dialog.DefaultCommandIndex = 1;
            await dialog.ShowAsync();
        }

        public static void ShowToastNotification(string title, string stringContent) {
            try {
                var toastNotifier = ToastNotificationManager.CreateToastNotifier();
                var toastXml = ToastNotificationManager.GetTemplateContent(ToastTemplateType.ToastText02);
                var toastNodeList = toastXml.GetElementsByTagName("text");
                toastNodeList.Item(0).AppendChild(toastXml.CreateTextNode(title));
                toastNodeList.Item(1).AppendChild(toastXml.CreateTextNode(stringContent));
                var audio = toastXml.CreateElement("audio");
                audio.SetAttribute("src", "ms-winsoundevent:Notification.SMS");

                var toast = new ToastNotification(toastXml);
                toast.ExpirationTime = DateTime.Now.AddSeconds(4);
                toastNotifier.Show(toast);
            } catch (Exception ex) {
                Logger.Error("ShowToastNotification", ex);
            }
        }

        public MosaicPageBackupData BackupData { 
            get {
                var controller = MosaicController;
                if (controller.GameStatus == EGameStatus.eGSPlay) 
                    return new MosaicPageBackupData {
                        MosaicBackupData = controller.GameBackup(),
                        MosaicOffset = controller.Model.MosaicOffset,
                        PlayTime = GameTimer.Time
                    };

                return null;
            }
        }

    }

}
