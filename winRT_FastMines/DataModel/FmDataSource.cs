using System;
using System.ComponentModel;
using System.Linq;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.controller.types;
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
               foreach (var itemGroup in EMosaicGroupEx.GetValues()) {
                  var dataGroup = new FmDataGroup(itemGroup);
                  foreach (var item in itemGroup.getBind())
                     dataGroup.Items.Add(new FmDataItem(item, dataGroup));
                  _allGroups.Add(dataGroup);
               }
            }
            return _allGroups;
         }
      }

      public static FmDataGroup GetGroup(EMosaicGroup uniqueId) {
         // Simple linear search is acceptable for small data sets
         return AllGroups.First(group => (group.UniqueId == uniqueId));
      }

      public static FmDataItem GetItem(EMosaic uniqueId) {
         // Simple linear search is acceptable for small data sets
         return AllGroups.SelectMany(group => group.Items).First(item => (item.UniqueId == uniqueId));
      }

      public static ESkillLevel SkillLevel { get; set; }
      public static ua.ksn.geom.Size SizeField { get; set; }
      public static int MinesCount { get; set; }
   }
}