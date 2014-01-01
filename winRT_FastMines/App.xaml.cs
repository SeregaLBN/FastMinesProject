using FastMines.Common;

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Windows.ApplicationModel;
using Windows.ApplicationModel.Activation;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Grid App template is documented at http://go.microsoft.com/fwlink/?LinkId=234226

namespace FastMines {
   /// <summary>
   /// Provides application-specific behavior to supplement the default Application class.
   /// </summary>
   sealed partial class App : Application {
      /// <summary>
      /// Initializes the singleton Application object.  This is the first line of authored code
      /// executed, and as such is the logical equivalent of main() or WinMain().
      /// </summary>
      public App() {
         this.InitializeComponent();
         this.Suspending += OnSuspending;

#if DEBUG
         var testString = "Hello World! Чпунтику привет!";
         var bs = System.Text.Encoding.UTF8.GetBytes(testString);
         var text2 = Windows.Security.Cryptography.CryptographicBuffer.ConvertBinaryToString(Windows.Security.Cryptography.BinaryStringEncoding.Utf8, System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBufferExtensions.AsBuffer(bs));
         System.Diagnostics.Debug.Assert(testString == text2);

         var omg = Windows.Security.Cryptography.CryptographicBuffer.EncodeToHexString(System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeBufferExtensions.AsBuffer(new byte[] { 0x01, 0x02, 0x1d, 0x55, 0xFF }));

         { // 3DES
            {
               var secKey = "super-puper mega Password";
               var tdes1 = new ua.ksn.crypt.TripleDESOperations() { SecurityKeyStr = secKey, DataStr = testString };
               var encrypted = tdes1.EncryptB64();
               var decrypted = new ua.ksn.crypt.TripleDESOperations() { SecurityKeyStr = secKey, DataB64 = encrypted }.DecryptStr();
               System.Diagnostics.Debug.Assert(decrypted == testString, "Triple DES failed!");
            }
            //{
            //   var secKey = ua.ksn.crypt.TripleDESOperations.GenerateKey();
            //   var encrypted = new ua.ksn.crypt.TripleDESOperations() { SecurityKey = secKey, DataStr = testString }.EncryptB64();
            //   var decrypted = new ua.ksn.crypt.TripleDESOperations() { SecurityKey = secKey, DataB64 = encrypted }.DecryptStr();
            //   System.Diagnostics.Debug.Assert(decrypted == testString, "Triple DES failed!");
            //}
            //{
            //   var secKey = ua.ksn.crypt.TripleDESOperations.GenerateKey();
            //   var iv = ua.ksn.crypt.TripleDESOperations.GenerateInitVector(CipherMode.CBC);
            //   var encrypted = new ua.ksn.crypt.TripleDESOperations() { InitVector = iv, Mode = CipherMode.CBC, SecurityKey = secKey, DataStr = testString }.EncryptB64();
            //   var decrypted = new ua.ksn.crypt.TripleDESOperations() { InitVector = iv, Mode = CipherMode.CBC, SecurityKey = secKey, DataB64 = encrypted }.DecryptStr();
            //   System.Diagnostics.Debug.Assert(decrypted == testString, "Triple DES failed!");
            //}
         }
#endif
      }

      /// <summary>
      /// Invoked when the application is launched normally by the end user.  Other entry points
      /// will be used when the application is launched to open a specific file, to display
      /// search results, and so forth.
      /// </summary>
      /// <param name="args">Details about the launch request and process.</param>
      protected override async void OnLaunched(LaunchActivatedEventArgs args) {
         Frame rootFrame = Window.Current.Content as Frame;

         // Do not repeat app initialization when the Window already has content, just ensure that the window is active
         if (rootFrame == null) {
            // Create a Frame to act as the navigation context and navigate to the first page
            rootFrame = new Frame();
            //Associate the frame with a SuspensionManager key                                
            SuspensionManager.RegisterFrame(rootFrame, "AppFrame");

            if (args.PreviousExecutionState == ApplicationExecutionState.Terminated) {
               // Restore the saved session state only when appropriate
               try {
                  await SuspensionManager.RestoreAsync();
               } catch (SuspensionManagerException ex) {
                  //Something went wrong restoring state.
                  //Assume there is no state and continue
                  System.Diagnostics.Debug.Assert(false, ex.ToString());
               }
            }

            // Place the frame in the current Window
            Window.Current.Content = rootFrame;
         }
         if (rootFrame.Content == null) {
            // When the navigation stack isn't restored navigate to the first page,
            // configuring the new page by passing required information as a navigation parameter
            if (!rootFrame.Navigate(typeof(GroupedItemsPage), "AllGroups")) {
               throw new Exception("Failed to create initial page");
            }
         }
         // Ensure the current window is active
         Window.Current.Activate();
      }

      /// <summary>
      /// Invoked when application execution is being suspended.  Application state is saved
      /// without knowing whether the application will be terminated or resumed with the contents
      /// of memory still intact.
      /// </summary>
      /// <param name="sender">The source of the suspend request.</param>
      /// <param name="e">Details about the suspend request.</param>
      private async void OnSuspending(object sender, SuspendingEventArgs e) {
         var deferral = e.SuspendingOperation.GetDeferral();
         await SuspensionManager.SaveAsync();
         deferral.Complete();
      }
   }
}