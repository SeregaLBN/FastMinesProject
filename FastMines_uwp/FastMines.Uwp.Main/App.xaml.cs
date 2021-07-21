using System;
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
using Fmg.Core.Types.Model;
using Fmg.Uwp.App;
using Fmg.Uwp.App.Model;
using Fmg.Uwp.App.Presentation;
using Fmg.Uwp.Utils;

namespace Fmg {

    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    sealed partial class App : Application {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData   InitData => SharedData.MosaicInitData;
        public MenuSettings MenuSettings => SharedData.MenuSettings;

        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public App() {
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

        /// <summary>
        /// Invoked when the application is launched normally by the end user.  Other entry points
        /// will be used such as when the application is launched to open a specific file.
        /// </summary>
        /// <param name="e">Details about the launch request and process.</param>
        protected override void OnLaunched(LaunchActivatedEventArgs ev) {
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
                LoadAppData();

                if (!rootFrame.Navigate(typeof(MainPage), this.InitData)) {
                    throw new Exception("Failed to create initial page ;(");
                }
            }

            // Ensure the current window is active
            Window.Current.Activate();

           AsyncRunner.InvokeLater(x => TileHelper.RegisterBackgroundTask(), Windows.System.Threading.WorkItemPriority.Low);
        }

        private void OnForegrounded(object sender, LeavingBackgroundEventArgs ev) {
            Logger.Info("FastMinesApp::OnForegrounded");

            MenuSettings.PropertyChanged += OnMenuSettingsPropertyChanged;
            InitData    .PropertyChanged += OnInitDataPropertyChanged;
        }

        private void OnBackgrounded(object sender, EnteredBackgroundEventArgs ev) {
            Logger.Info("FastMinesApp::OnBackgrounded");

            var deferral = ev.GetDeferral();
            SaveAppData();
            deferral.Complete();

            MenuSettings.PropertyChanged -= OnMenuSettingsPropertyChanged;
            InitData    .PropertyChanged -= OnInitDataPropertyChanged;
        }

        /// <summary>
        /// Invoked when application execution is being suspended.  Application state is saved
        /// without knowing whether the application will be terminated or resumed with the contents
        /// of memory still intact.
        /// </summary>
        /// <param name="sender">The source of the suspend request.</param>
        /// <param name="e">Details about the suspend request.</param>
        private void OnSuspending(object sender, SuspendingEventArgs e) {
            Logger.Info("FastMinesApp::OnSuspending");
            var deferral = e.SuspendingOperation.GetDeferral();

            // Save application state and stop any background activity
            //SaveAppData();

            //Frame rootFrame = Window.Current.Content as Frame;
            //var mp = rootFrame.Content as MainPage;
            //if (mp?.RightFrame?.SourcePageType == typeof(SelectMosaicPage)) {
            //   var smp = mp.RightFrame.Content as SelectMosaicPage;
            //}

            deferral.Complete();

            InitData.Dispose();
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
            if (frame.CanGoBack) {
                ev.Handled = true;
                frame.GoBack();
            }
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

        private void SaveAppData() {
            SharedData.Save(Windows.Storage.ApplicationData.Current.LocalSettings.Values);
        }

        private void LoadAppData() {
            SharedData.Load(Windows.Storage.ApplicationData.Current.LocalSettings.Values);
        }

        private void UpdateBackButtonVisibility() {
            var frame = (Frame)Window.Current.Content;

            var visibility = frame.CanGoBack
                ? AppViewBackButtonVisibility.Visible
                : AppViewBackButtonVisibility.Collapsed;

            SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = visibility;
        }

        private void OnMenuSettingsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            Logger.Info("FastMinesApp::OnMenuSettingsPropertyChanged: ev={0}", ev);
        }

        private void OnInitDataPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            Logger.Info("FastMinesApp::OnInitDataPropertyChanged: ev={0}", ev);
        }

    }

}
