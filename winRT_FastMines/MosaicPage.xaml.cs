using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Shapes;
using ua.ksn.fmg.controller.win_rt;
using ua.ksn.fmg.model.mosaics;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicPage : Page {
      private MosaicExt _mosaic;

      public MosaicPage() {
         this.InitializeComponent();

         this.Loaded += MosaicPage_OnLoaded;
         this.Unloaded += MosaicPage_OnUnloaded;
      }

      protected override void OnNavigatedTo(NavigationEventArgs e) {
         base.OnNavigatedTo(e);

         _mosaic = new MosaicExt(new ua.ksn.geom.Size(10, 10), (EMosaic)e.Parameter, 5, 3000);
         ContentRoot.Children.Add(_mosaic.Container);
         _mosaic.Repaint();
      }

      private void OnPointerPressed(CoreWindow sender, PointerEventArgs args) {
         var properties = args.CurrentPoint.Properties;

         // Ignore button chords with the left, right, and middle buttons
         if (properties.IsLeftButtonPressed || properties.IsRightButtonPressed ||
             properties.IsMiddleButtonPressed)
            return;

         // If back or foward are pressed (but not both) navigate appropriately
         var backPressed = properties.IsXButton1Pressed;
         if (backPressed)
            GoBack(args);

      }

      private void GoBack(ICoreWindowEventArgs args) {
         if (this.Frame != null && this.Frame.CanGoBack) {
            args.Handled = true;
            this.Frame.GoBack();
         }
      }

      private void MosaicPage_OnLoaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.PointerPressed += OnPointerPressed;
      }

      private void MosaicPage_OnUnloaded(object sender, RoutedEventArgs e) {
         Window.Current.CoreWindow.PointerPressed -= OnPointerPressed;
      }
   }
}