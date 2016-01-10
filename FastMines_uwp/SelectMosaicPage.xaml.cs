using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using FastMines.Presentation;

namespace FastMines
{
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class MosaicGroupItemsPage : Page
   {

      public MosaicGroupItemsPage() {
         this.InitializeComponent();
         ViewModel = new MosaicsViewModel();
      }

      public MosaicsViewModel ViewModel { get; private set; }

      private void Selector_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
      {
         //throw new NotImplementedException();
      }

      private void ListViewBase_OnItemClick(object sender, ItemClickEventArgs e)
      {
         //throw new NotImplementedException();
      }
   }
}
