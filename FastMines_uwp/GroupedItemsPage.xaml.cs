using System;
using System.Collections.Generic;
using Windows.Foundation;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using FastMines.Data;
using fmg.data.controller.types;
using fmg.uwp.mosaic;

// The Grouped Items Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234231

namespace FastMines {
   /// <summary>
   /// A page that displays a grouped collection of items.
   /// </summary>
   public sealed partial class GroupedItemsPage : FastMines.Common.LayoutAwarePage {
      public GroupedItemsPage() {
         // modify to http://stackoverflow.com/questions/15435023/add-dynamically-an-image-in-xaml-in-a-canvas
         this.InitializeComponent();
         //this.Loaded += PageOnLoaded;


         //ApplicationView.GetForCurrentView().SetPreferredMinSize(new Size { Width = 500, Height = 400 });
         //ApplicationView.PreferredLaunchViewSize = new Size { Height = 800, Width = 600 };
         //ApplicationView.PreferredLaunchWindowingMode = ApplicationViewWindowingMode.PreferredLaunchViewSize;
      }

      /// <summary>
      /// Populates the page with content passed during navigation.  Any saved state is also
      /// provided when recreating a page from a prior session.
      /// </summary>
      /// <param name="navigationParameter">The parameter value passed to
      /// <see cref="Frame.Navigate(Type, Object)"/> when this page was initially requested.
      /// </param>
      /// <param name="pageState">A dictionary of state preserved by this page during an earlier
      /// session.  This will be null the first time a page is visited.</param>
      protected override void LoadState(Object navigationParameter, Dictionary<String, Object> pageState) {
         this.DefaultViewModel["Groups_bindDataSource"] = FmDataSource.AllGroups;
      }

      /// <summary>
      /// Invoked when a group header is clicked.
      /// </summary>
      /// <param name="sender">The Button used as a group header for the selected group.</param>
      /// <param name="e">Event data that describes how the click was initiated.</param>
      private void Header_Click(object sender, RoutedEventArgs e) {
         // Determine what group the Button instance represents
         var group = (sender as FrameworkElement).DataContext;

         // Navigate to the appropriate destination page, configuring the new page
         // by passing required information as a navigation parameter
         this.Frame.Navigate(typeof (GroupDetailPage), ((FmDataGroup) group).UniqueId);
      }

      /// <summary>
      /// Invoked when an item within a group is clicked.
      /// </summary>
      /// <param name="sender">The GridView (or ListView when the application is snapped)
      /// displaying the item clicked.</param>
      /// <param name="e">Event data that describes the item clicked.</param>
      private void ItemView_ItemClick(object sender, ItemClickEventArgs e) {
         // Navigate to the appropriate destination page, configuring the new page
         // by passing required information as a navigation parameter
         var eMosaic = ((FmDataItem)e.ClickedItem).UniqueId;
         //this.Frame.Navigate(typeof(ItemDetailPage), eMosaic);
         this.Frame.Navigate(typeof(MosaicPage), new MosaicPageInitParam {
            MosaicTypes = eMosaic,
            MinesCount = FmDataSource.MinesCount = FmDataSource.SkillLevel.GetNumberMines(eMosaic),
            SizeField = FmDataSource.SizeField = FmDataSource.SkillLevel.DefaultSize()
         });
      }

      private void ButtonBase_OnClick(object sender, RoutedEventArgs e) {
         //await FmDataSource.ReloadImages();
         throw new NotImplementedException();
      }

      //private void PageOnLoaded(object sender, RoutedEventArgs e) {
      //}
   }
}