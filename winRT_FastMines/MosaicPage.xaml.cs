using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Shapes;
// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238
using ua.ksn.fmg.controller.win_rt;
using ua.ksn.fmg.model.mosaics;

namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicPage : Page
   {
      private MosaicExt _mosaic;

      public MosaicPage() {
         this.InitializeComponent();

         _mosaic = new MosaicExt(new ua.ksn.geom.Size(10, 10), EMosaic.eMosaicPentagonT24, 5, 3000);
         ContentRoot.Children.Add(_mosaic.Container);
         _mosaic.Repaint();
      }

      /// <summary>
      /// Invoked when this page is about to be displayed in a Frame.
      /// </summary>
      /// <param name="e">Event data that describes how this page was reached.  The Parameter
      /// property is typically used to configure the page.</param>
      protected override void OnNavigatedTo(NavigationEventArgs e) {
      }
   }
}
