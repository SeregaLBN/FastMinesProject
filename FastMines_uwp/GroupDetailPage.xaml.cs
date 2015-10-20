using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using FastMines.Data;
using fmg.data.controller.types;
using fmg.core.types;
using fmg.uwp.mosaic;
using FastMines.Common;

// The Group Detail Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234229

namespace FastMines {
   /// <summary>
   /// A page that displays an overview of a single group, including a preview of the items
   /// within the group.
   /// </summary>
   public sealed partial class GroupDetailPage : FastMines.Common.LayoutAwarePage {
      private EMosaicGroup _mosaicGroup;

      public GroupDetailPage() {
         this.InitializeComponent();

         this.Loaded += delegate {
            var img = FmDataSource.GetGroup(_mosaicGroup).MosaicGroupImage;
            img.Animate = true;
            img.Dance = false;
         };
         this.Unloaded += delegate {
            var img = FmDataSource.GetGroup(_mosaicGroup).MosaicGroupImage;
            img.Animate = img.Dance = false;
         };
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
         // TODO: Create an appropriate data model for your problem domain to replace the sample data
         _mosaicGroup = (EMosaicGroup) navigationParameter;
         var group = FmDataSource.GetGroup(_mosaicGroup);
         this.DefaultViewModel["Group_bindDataSource"] = group;
         this.DefaultViewModel["Items_bindDataSource"] = group.Items;
      }

      /// <summary>
      /// Invoked when an item is clicked.
      /// </summary>
      /// <param name="sender">The GridView (or ListView when the application is snapped)
      /// displaying the item clicked.</param>
      /// <param name="e">Event data that describes the item clicked.</param>
      private void ItemView_ItemClick(object sender, ItemClickEventArgs e) {
         // Navigate to the appropriate destination page, configuring the new page
         // by passing required information as a navigation parameter
         var eMosaic = ((FmDataItem) e.ClickedItem).UniqueId;
         //this.Frame.Navigate(typeof(ItemDetailPage), eMosaic);
         this.Frame.Navigate(typeof(MosaicPage), new MosaicPageInitParam {
            MosaicTypes = eMosaic,
            MinesCount = FmDataSource.MinesCount = FmDataSource.SkillLevel.GetNumberMines(eMosaic),
            SizeField = FmDataSource.SizeField = FmDataSource.SkillLevel.DefaultSize()
         });
      }

      private void GroupImage_OnTapped(object sender, TappedRoutedEventArgs e) {
         var img = FmDataSource.GetGroup(_mosaicGroup).MosaicGroupImage;
         img.Dance = !img.Dance;
      }

      private void GroupImage_OnDoubleTapped(object sender, DoubleTappedRoutedEventArgs e) {
         var img = FmDataSource.GetGroup(_mosaicGroup).MosaicGroupImage;
         img.Animate = !img.Animate;
      }
   }
}