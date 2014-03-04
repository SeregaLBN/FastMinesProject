using System.Collections.ObjectModel;
using System.Collections.Specialized;
using ua.ksn.fmg.model.mosaics;

// The data model defined by this file serves as a representative example of a strongly-typed
// model that supports notification when members are added, removed, or modified.  The property
// names chosen coincide with data bindings in the standard item templates.
//
// Applications may use this model as a starting point and build on it, or discard it entirely and
// replace it with something appropriate to their needs.

namespace FastMines.Data {

   /// <summary>
   /// Generic group data model.
   /// </summary>
   public class FmDataGroup : FmDataCommon<EMosaicGroup> {
      public FmDataGroup(EMosaicGroup eMosaicGroup)
         : base(eMosaicGroup, eMosaicGroup.GetDescription(), "res/MosaicGroup/" + eMosaicGroup.GetDescription() + ".png") {
         Items.CollectionChanged += ItemsCollectionChanged;
         this.Subtitle = "Subtitle group " + eMosaicGroup.GetDescription();
         this.Description = "Description group...";
      }

      private void ItemsCollectionChanged(object sender, System.Collections.Specialized.NotifyCollectionChangedEventArgs e) {
         // Provides a subset of the full items collection to bind to from a GroupedItemsPage
         // for two reasons: GridView will not virtualize large items collections, and it
         // improves the user experience when browsing through groups with large numbers of
         // items.
         //
         // A maximum of 12 items are displayed because it results in filled grid columns
         // whether there are 1, 2, 3, 4, or 6 rows displayed

         switch (e.Action) {
         case NotifyCollectionChangedAction.Add:
            if (e.NewStartingIndex < 12) {
               TopItems.Insert(e.NewStartingIndex, Items[e.NewStartingIndex]);
               if (TopItems.Count > 12) {
                  TopItems.RemoveAt(12);
               }
            }
            break;
         case NotifyCollectionChangedAction.Move:
            if (e.OldStartingIndex < 12 && e.NewStartingIndex < 12) {
               TopItems.Move(e.OldStartingIndex, e.NewStartingIndex);
            } else if (e.OldStartingIndex < 12) {
               TopItems.RemoveAt(e.OldStartingIndex);
               TopItems.Add(Items[11]);
            } else if (e.NewStartingIndex < 12) {
               TopItems.Insert(e.NewStartingIndex, Items[e.NewStartingIndex]);
               TopItems.RemoveAt(12);
            }
            break;
         case NotifyCollectionChangedAction.Remove:
            if (e.OldStartingIndex < 12) {
               TopItems.RemoveAt(e.OldStartingIndex);
               if (Items.Count >= 12) {
                  TopItems.Add(Items[11]);
               }
            }
            break;
         case NotifyCollectionChangedAction.Replace:
            if (e.OldStartingIndex < 12) {
               TopItems[e.OldStartingIndex] = Items[e.OldStartingIndex];
            }
            break;
         case NotifyCollectionChangedAction.Reset:
            TopItems.Clear();
            while (TopItems.Count < Items.Count && TopItems.Count < 12) {
               TopItems.Add(Items[TopItems.Count]);
            }
            break;
         }
      }

      private ObservableCollection<FmDataItem> _items = new ObservableCollection<FmDataItem>();
      public ObservableCollection<FmDataItem> Items {
         get { return this._items; }
      }

      private ObservableCollection<FmDataItem> _topItem = new ObservableCollection<FmDataItem>();
      public ObservableCollection<FmDataItem> TopItems {
         get { return this._topItem; }
      }
   }
}