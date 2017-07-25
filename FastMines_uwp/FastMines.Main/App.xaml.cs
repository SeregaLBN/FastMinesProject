using System;
using Windows.System;
using Windows.ApplicationModel;
using Windows.ApplicationModel.Activation;
using Windows.Foundation.Metadata;
using Windows.Phone.UI.Input;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Controls;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;

namespace fmg
{
   /// <summary>
   /// Provides application-specific behavior to supplement the default Application class.
   /// </summary>
   sealed partial class App : Application
   {

      /// <summary> Model (a common model between all the pages in the application) </summary>
      public MosaicInitData MosaicData { get; private set; }

      /// <summary>
      /// Initializes the singleton application object.  This is the first line of authored code
      /// executed, and as such is the logical equivalent of main() or WinMain().
      /// </summary>
      public App()
      {
         this.InitializeComponent();
         this.Suspending += OnSuspending;
      }

      /// <summary>
      /// Invoked when the application is launched normally by the end user.  Other entry points
      /// will be used such as when the application is launched to open a specific file.
      /// </summary>
      /// <param name="e">Details about the launch request and process.</param>
      protected override void OnLaunched(LaunchActivatedEventArgs ev)
      {
#if DEBUG
         if (System.Diagnostics.Debugger.IsAttached)
         {
            // disabled, obscures the hamburger button, enable if you need it
            //this.DebugSettings.EnableFrameRateCounter = true;
         }
#endif

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
            this.MosaicData = LoadAppData();
            if (!rootFrame.Navigate(typeof(MainPage), this.MosaicData)) {
               throw new Exception("Failed to create initial page ;(");
            }
         }

         // Ensure the current window is active
         Window.Current.Activate();

         //AsyncRunner.InvokeLater(x => TileHelper.RegisterBackgroundTask(), Windows.System.Threading.WorkItemPriority.Low);
      }

      // handle hardware back button press
      void OnBackPressed(object sender, BackPressedEventArgs ev) {
         LoggerSimple.Put("App.OnBackPressed:");
         var frame = (Frame)Window.Current.Content;
         if (frame.CanGoBack) {
            ev.Handled = true;
            frame.GoBack();
         }
      }

      // handle software back button press
      void OnBackRequested(object sender, BackRequestedEventArgs ev) {
         LoggerSimple.Put("App.OnBackRequested:");
         var frame = (Frame)Window.Current.Content;
         if (frame.CanGoBack) {
            ev.Handled = true;
            frame.GoBack();
         }
      }

      void OnKeyDown(CoreWindow sender, KeyEventArgs ev) {
         //LoggerSimple.Put("App.OnKeyDown: VirtualKey=" + ev.VirtualKey);
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

      void OnNavigated(object sender, NavigationEventArgs e)
      {
         UpdateBackButtonVisibility();
      }

      /// <summary>
      /// Invoked when Navigation to a certain page fails
      /// </summary>
      /// <param name="sender">The Frame which failed navigation</param>
      /// <param name="e">Details about the navigation failure</param>
      void OnNavigationFailed(object sender, NavigationFailedEventArgs e)
      {
         throw new Exception("Failed to load Page " + e.SourcePageType.FullName);
      }

      /// <summary>
      /// Invoked when application execution is being suspended.  Application state is saved
      /// without knowing whether the application will be terminated or resumed with the contents
      /// of memory still intact.
      /// </summary>
      /// <param name="sender">The source of the suspend request.</param>
      /// <param name="e">Details about the suspend request.</param>
      private void OnSuspending(object sender, SuspendingEventArgs e)
      {
         var deferral = e.SuspendingOperation.GetDeferral();

         // Save application state and stop any background activity
         SaveAppData();

         //Frame rootFrame = Window.Current.Content as Frame;
         //var mp = rootFrame.Content as MainPage;
         //if (mp?.RightFrame?.SourcePageType == typeof(SelectMosaicPage)) {
         //   var smp = mp.RightFrame.Content as SelectMosaicPage;
         //}

         deferral.Complete();
      }

      private void SaveAppData() {
         MosaicInitData saveData = this.MosaicData;

         Windows.Storage.ApplicationDataCompositeValue compositeMosaic = new Windows.Storage.ApplicationDataCompositeValue();
         compositeMosaic[nameof(MosaicInitData.MosaicType)                           ] = saveData.MosaicType.Ordinal();
         compositeMosaic[nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.m)] = saveData.SizeField.m;
         compositeMosaic[nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.n)] = saveData.SizeField.n;
         compositeMosaic[nameof(MosaicInitData.MinesCount)                           ] = saveData.MinesCount;
         compositeMosaic[nameof(MosaicInitData.Area)                                 ] = saveData.Area;

         var lsv = Windows.Storage.ApplicationData.Current.LocalSettings.Values;
         lsv[nameof(MosaicInitData)] = compositeMosaic;
      }

      private MosaicInitData LoadAppData() {
         var lsv = Windows.Storage.ApplicationData.Current.LocalSettings.Values;
         if (lsv.ContainsKey(nameof(MosaicInitData))) 
            try {
               Windows.Storage.ApplicationDataCompositeValue compositeMosaic = (Windows.Storage.ApplicationDataCompositeValue)lsv[nameof(MosaicInitData)];
               int mosaicTypeOrdinal = (int)   compositeMosaic[nameof(MosaicInitData.MosaicType)];
               int sizeFieldM        = (int)   compositeMosaic[nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.m)];
               int sizeFieldN        = (int)   compositeMosaic[nameof(MosaicInitData.SizeField) + '.' + nameof(Matrisize.n)];
               int minesCount        = (int)   compositeMosaic[nameof(MosaicInitData.MinesCount)];
               double area           = (double)compositeMosaic[nameof(MosaicInitData.Area)];

               MosaicInitData loadData = new MosaicInitData();
               loadData.MosaicType = EMosaicEx.FromOrdinal(mosaicTypeOrdinal);
               loadData.SizeField  = new Matrisize(sizeFieldM, sizeFieldN);
               loadData.MinesCount = minesCount;
               loadData.Area       = area;
               return loadData;
            } catch (Exception ex) {
               LoggerSimple.Put($"Fail load data: {ex.Message}");
               System.Diagnostics.Debug.Assert(false, ex.Message);
            }
         return new MosaicInitData();
      }


      private void UpdateBackButtonVisibility()
      {
         var frame = (Frame)Window.Current.Content;

         var visibility = frame.CanGoBack
            ? AppViewBackButtonVisibility.Visible
            : AppViewBackButtonVisibility.Collapsed;

         SystemNavigationManager.GetForCurrentView().AppViewBackButtonVisibility = visibility;
      }

   }
}
