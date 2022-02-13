using System;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel;
using Windows.UI.ViewManagement;
using Windows.System;
using Windows.ApplicationModel;
using Windows.ApplicationModel.Activation;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Controls;
using Fmg.Common;
using Fmg.Core.Img;
using Fmg.Core.Types;
using Fmg.Core.App.Model;
using Fmg.Uwp.App.Model;
using Fmg.Uwp.App.Presentation;
using Fmg.Uwp.App.Serializers;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.App {

    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    sealed partial class FastMinesApp : Application {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData MosaicInitData { get; private set; }
        private MosaicPageBackupData MosaicPageBackupData { get; set; }
        public MenuSettings MenuSettings { get; private set; }
        public Players Players { get; private set; }
        public Champions Champions { get; private set; }
        private bool playersChanged;
        private bool championsChanged;
        private Page lastPage;

        public bool HasMosaicPageBackupData => (MosaicPageBackupData != null);
        public MosaicPageBackupData GetAndResetMosaicPageBackupData() {
            MosaicPageBackupData res = MosaicPageBackupData;
            MosaicPageBackupData = null;
            return res;
        }

        private static FastMinesApp self;

        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public FastMinesApp() {
            self = this;
            ProjSettings.Init();
            this.InitializeComponent();
            this.Suspending += OnSuspending;
            this.LeavingBackground += OnForegrounded;
            this.EnteredBackground += OnBackgrounded;
        }

        protected override void OnActivated(IActivatedEventArgs args) {
            Logger.Info("FastMinesApp::OnActivated");
            base.OnActivated(args);
        }

        /// <summary> get single instance of application (singleton) </summary>
        public static FastMinesApp Get => self;

        /// <summary>
        /// Invoked when the application is launched normally by the end user.  Other entry points
        /// will be used such as when the application is launched to open a specific file.
        /// </summary>
        /// <param name="e">Details about the launch request and process.</param>
        protected override async void OnLaunched(LaunchActivatedEventArgs ev) {
            Logger.Info("FastMinesApp::OnLaunched");
#if  DEBUG
            if (System.Diagnostics.Debugger.IsAttached) {
                // disabled, obscures the hamburger button, enable if you need it
                //this.DebugSettings.EnableFrameRateCounter = true;
            }
#endif

            // you need to add a reference to the correspondent Extension:
            //  * Windows Mobile Extensions for the UWP
            //  * Windows Desktop Extensions for the UWP

            //PC customization
            if (ApiInformation.IsTypePresent("Windows.UI.ViewManagement.ApplicationView")) {
                ApplicationView.GetForCurrentView().SetPreferredMinSize(new Windows.Foundation.Size(350, 560));

                var titleBar = ApplicationView.GetForCurrentView().TitleBar;
                if (titleBar != null) {
                    titleBar.BackgroundColor =
                    titleBar.ButtonBackgroundColor = AnimatedImageModelConst.DefaultBkColor.ToWinColor();
                }
            }
            //Mobile customization
            if (ApiInformation.IsTypePresent("Windows.UI.ViewManagement.StatusBar")) {
                var statusBar = StatusBar.GetForCurrentView();
                if (statusBar != null)
                    statusBar.BackgroundColor = AnimatedImageModelConst.DefaultBkColor.ToWinColor();
            }


            Frame rootFrame = Window.Current.Content as Frame;
            // Do not repeat app initialization when the Window already has content,
            // just ensure that the window is active
            if (rootFrame == null) {
                // Create a Frame which navigates to the first page
                rootFrame = new Frame();

                // hook-up root frame navigation events
                rootFrame.NavigationFailed += OnNavigationFailed;
                rootFrame.Navigated += OnNavigated;

                if (ev.PreviousExecutionState == ApplicationExecutionState.Terminated) {
                    // TODO: Load state from previously suspended application
                }
                // set the Frame as content
                Window.Current.Content = rootFrame;


                // listen for back button clicks (both soft- and hardware)
                SystemNavigationManager.GetForCurrentView().BackRequested += OnBackRequested;
                if (ApiInformation.IsTypePresent("Windows.Phone.UI.Input.HardwareButtons")) {
                    HardwareButtons.BackPressed += OnBackPressed;
                }
                Window.Current.CoreWindow.KeyDown += OnKeyDown;

                UpdateBackButtonVisibility();
            }

            if (rootFrame.Content == null) {
                // create a common model between all the pages in the application
                await LoadAppData();

                if (!rootFrame.Navigate(typeof(MainPage), this.MosaicInitData)) {
                    throw new Exception("Failed to create initial page ;(");
                }
            }

            // Ensure the current window is active
            Window.Current.Activate();

        }

        private void OnForegrounded(object sender, LeavingBackgroundEventArgs ev) {
            Logger.Info("FastMinesApp::OnForegrounded");

            MenuSettings  .PropertyChanged += OnMenuSettingsPropertyChanged;
            MosaicInitData.PropertyChanged += OnMosaicInitDataPropertyChanged;
        }

        private void OnBackgrounded(object sender, EnteredBackgroundEventArgs ev) {
            Logger.Info("FastMinesApp::OnBackgrounded");

            var deferral = ev.GetDeferral();
            deferral.Complete();

            MenuSettings  .PropertyChanged -= OnMenuSettingsPropertyChanged;
            MosaicInitData.PropertyChanged -= OnMosaicInitDataPropertyChanged;
        }

        /// <summary>
        /// Invoked when application execution is being suspended.  Application state is saved
        /// without knowing whether the application will be terminated or resumed with the contents
        /// of memory still intact.
        /// </summary>
        /// <param name="sender">The source of the suspend request.</param>
        /// <param name="e">Details about the suspend request.</param>
        private async void OnSuspending(object sender, SuspendingEventArgs e) {
            Logger.Info("FastMinesApp::OnSuspending");
            var deferral = e.SuspendingOperation.GetDeferral();

            // Save application state and stop any background activity
            await SaveAppData();

            //Frame rootFrame = Window.Current.Content as Frame;
            //var mp = rootFrame.Content as MainPage;
            //if (mp?.RightFrame?.SourcePageType == typeof(SelectMosaicPage)) {
            //   var smp = mp.RightFrame.Content as SelectMosaicPage;
            //}

            deferral.Complete();

            Players.PropertyChanged   -= OnPlayersPropertyChanged;
            Champions.PropertyChanged -= OnChampionsPropertyChanged;

            Champions.Dispose();
            Players.Dispose();
            MosaicInitData.Dispose();
            MenuSettings.Dispose();
        }

        protected override void OnBackgroundActivated(BackgroundActivatedEventArgs ev) {
            Logger.Info("FastMinesApp::OnBackgroundActivated");
            base.OnBackgroundActivated(ev);
        }

        protected override void OnWindowCreated(WindowCreatedEventArgs ev) {
            Logger.Info($"FastMinesApp::OnWindowCreated: ev={ev}");
            base.OnWindowCreated(ev);
        }

        // handle hardware back button press
        void OnBackPressed(object sender, BackPressedEventArgs ev) {
            Logger.Info("FastMinesApp::OnBackPressed");
            var frame = (Frame)Window.Current.Content;
            if (frame.CanGoBack) {
                ev.Handled = true;
                frame.GoBack();
            }
        }

        // handle software back button press
        void OnBackRequested(object sender, BackRequestedEventArgs ev) {
            Logger.Info("FastMinesApp::OnBackRequested");
            var frame = (Frame)Window.Current.Content;
            //if (frame.CanGoBack) {
            //    ev.Handled = true;
            //    frame.GoBack();
            //}
            if (!frame.CanGoBack)
                return;

            ev.Handled = true;

            if (lastPage is MosaicPage mosaicPage)
                mosaicPage.ConfirmBackRequested();
            else
                frame.GoBack();
        }

        void OnKeyDown(CoreWindow sender, KeyEventArgs ev) {
            //LoggerSimple.Put("FastMinesApp::OnKeyDown: VirtualKey=" + ev.VirtualKey);
            var frame = (Frame)Window.Current.Content;
            switch (ev.VirtualKey) {
            //case VirtualKey.GoBack: // System.Diagnostics.Debug.Assert(false, "must be handled in " + nameof(App) + "." + nameof(OnBackRequested));
            case VirtualKey.Back:
                if (frame.CanGoBack) {
                    ev.Handled = true;
                    frame.GoBack();
                }
                break;
            }
        }

        void OnNavigated(object sender, NavigationEventArgs ev) {
            Logger.Info($"FastMinesApp::OnNavigated: ev={ev}");
            lastPage = (sender as Frame)?.Content as Page;
            UpdateBackButtonVisibility();
        }

        /// <summary>
        /// Invoked when Navigation to a certain page fails
        /// </summary>
        /// <param name="sender">The Frame which failed navigation</param>
        /// <param name="ev">Details about the navigation failure</param>
        void OnNavigationFailed(object sender, NavigationFailedEventArgs ev) {
            Logger.Info("FastMinesApp::OnNavigationFailed");
            throw new Exception("Failed to load Page " + ev.SourcePageType.FullName);
        }

        private async Task SaveAppData() {
            await new ChampionsUwpSerializer().Save(Champions);
            await new PlayersUwpSerializer().Save(Players);

            var appData = new AppData {
                SplitPaneOpen = this.MenuSettings.SplitPaneOpen,
                MosaicInitData = this.MosaicInitData
            };
            if (lastPage is MosaicPage mosaicPage)
                appData.MosaicPageBackupData = mosaicPage.BackupData;

            new AppDataSerializer().Save(appData, Windows.Storage.ApplicationData.Current.LocalSettings.Values);
        }

        private async Task LoadAppData() {
            var appData = new AppDataSerializer().Load(Windows.Storage.ApplicationData.Current.LocalSettings.Values);
            this.MenuSettings = new MenuSettings() {
                SplitPaneOpen = appData.SplitPaneOpen
            };
            this.MosaicInitData = appData.MosaicInitData;
            this.MosaicPageBackupData = appData.MosaicPageBackupData;

            Players = await new PlayersUwpSerializer().Load();
            if (!Players.Records.Any())
                // create default user for UWP
                Players.AddNewPlayer("You", null);
            Players.PropertyChanged += OnPlayersPropertyChanged;

            Champions = await new ChampionsUwpSerializer().Load();
            //Champions.SubscribeTo(Players);
            Champions.PropertyChanged += OnChampionsPropertyChanged;
        }

        private void UpdateBackButtonVisibility() {
            var frame = (Frame)Window.Current.Content;

            var visibility = frame.CanGoBack
                ? AppViewBackButtonVisibility.Visible
                : AppViewBackButtonVisibility.Collapsed;

            SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = visibility;
        }

        private void OnMenuSettingsPropertyChanged(object sender, PropertyChangedEventArgs ev)
        {
            Logger.Info("FastMinesApp::OnMenuSettingsPropertyChanged: ev={0}", ev);
        }

        private void OnMosaicInitDataPropertyChanged(object sender, PropertyChangedEventArgs ev)
        {
            Logger.Info("FastMinesApp::OnInitDataPropertyChanged: ev={0}", ev);
        }

        /// <summary> Сохранить чемпиона && Установить статистику </summary>
        public int UpdateStatistic(EMosaic mosaic, ESkillLevel skill, bool victory, long countOpenField, long playTime, int clickCount) {
            // логика сохранения...
            Guid userId = Players.Records
                                 [0] // first user - default user
                                 .user
                                 .Id;
            // ...статистики
            Players.UpdateStatistic(userId, mosaic, skill, victory, countOpenField, playTime, clickCount);

            // ...чемпиона
            if (victory) {
                var user = Players.GetUser(userId);
                return Champions.Add(user, playTime, mosaic, skill, clickCount);
            }

            return -1;
        }


        private void OnPlayersPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            playersChanged = true;
        }

        private void OnChampionsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            championsChanged = true;
        }

    }

}
