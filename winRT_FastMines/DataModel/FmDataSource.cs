using System;
using System.ComponentModel;
using System.Linq;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.model.mosaics;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {

   /// <summary>
   /// Creates a collection of groups and items with hard-coded content.
   /// 
   /// FmDataSource initializes with placeholder data rather than live production
   /// data so that sample data is provided at both design-time and run-time.
   /// </summary>
   public sealed class FmDataSource {
      private static ObservableCollection<FmDataGroup> _allGroups = null;
      public static ObservableCollection<FmDataGroup> AllGroups {
         get {
            if (_allGroups == null) {
               _allGroups = new ObservableCollection<FmDataGroup>();
               foreach (EMosaicGroup itemGroup in Enum.GetValues(typeof(EMosaicGroup))) {
                  var dataGroup = new FmDataGroup(itemGroup);
                  foreach (EMosaic item in itemGroup.getBind()) {
                     dataGroup.Items.Add(new FmDataItem(item, dataGroup));
                  }
                  _allGroups.Add(dataGroup);
               }
            }
            return _allGroups;
         }
      }

      public static FmDataGroup GetGroup(EMosaicGroup uniqueId) {
         // Simple linear search is acceptable for small data sets
         var matches = AllGroups.Where((group) => (group.UniqueId == uniqueId));
         return matches.FirstOrDefault();
      }

      public static FmDataItem GetItem(EMosaic uniqueId) {
         // Simple linear search is acceptable for small data sets
         var matches = AllGroups.SelectMany(group => group.Items).Where((item) => (item.UniqueId == uniqueId));
         return matches.FirstOrDefault();
      }

      public static async Task ReloadImages(bool forceReload) {
         foreach (var fmItem in AllGroups.SelectMany(fmDataGroup => fmDataGroup.Items))
            if (forceReload || !(fmItem.Image is WriteableBitmap))
               fmItem.Image = await FmDataItem.CreateImage(fmItem.UniqueId);
      }
   }
}