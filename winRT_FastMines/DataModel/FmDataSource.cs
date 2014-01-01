using System;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using Windows.ApplicationModel.Resources.Core;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
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
	/// Base class for <see cref="FmDataItem"/> and <see cref="FmDataGroup"/> that defines properties common to both.
	/// </summary>
	[Windows.Foundation.Metadata.WebHostHidden]
	public abstract class FmDataCommon : FastMines.Common.BindableBase {
		private static Uri _baseUri = new Uri("ms-appx:///");

		public FmDataCommon(int uniqueId, String title, String imagePath) {
			this._uniqueId = uniqueId;
			this._title = title;
			this._imagePath = imagePath;

			Subtitle = "Subtitle...";
			Description = "Description...";
		}

		private int _uniqueId = 0;
		public int UniqueId {
			get { return this._uniqueId; }
			set { this.SetProperty(ref this._uniqueId, value); }
		}

		private string _title = string.Empty;
		public string Title {
			get { return this._title; }
			set { this.SetProperty(ref this._title, value); }
		}

		public string Subtitle { get; set; }
		public string Description { get; set; }

      private WriteableBitmap _image = null;
		private String _imagePath = null;
		public WriteableBitmap Image {
			get {
				if (this._image == null && this._imagePath != null) {
					//this._image = new BitmapImage(new Uri(FmDataCommon._baseUri, this._imagePath));
               this._image = BitmapFactory.New(1024, 1024);

               using (var ctx = _image.GetBitmapContext()) {
                  int[] points = new int[] { 10, 10, 10, 200, 200, 200, 200, 10 };
                  var clr = 0xFF << 24;//unchecked((int)0xFF000000);
                  _image.FillPolygon(points, clr);
                  //_image.DrawRectangle(10, 10, 200, 200, clr);
                  clr |= 0xFFFFFF;
                  _image.DrawLine(10, 10, 200, 200, clr);
                  int wbmp = _image.PixelWidth, hbmp = _image.PixelHeight;
                  WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, 10, 10, 200, clr);
                  WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 10, 200, 200, 200, clr);
                  WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 200, 200, 200, 10, clr);
                  WriteableBitmapExtensions.DrawLine(ctx, wbmp, hbmp, 200, 10, 10, 10, clr);
               }
				}
				return this._image;
			}

			set {
				this._imagePath = null;
				this.SetProperty(ref this._image, value);
			}
		}

		public void SetImage(String path) {
			this._image = null;
			this._imagePath = path;
			this.OnPropertyChanged("Image");
		}

		public override string ToString() {
			return this.Title;
		}
	}

	/// <summary>
	/// Generic item data model.
	/// </summary>
	public class FmDataItem : FmDataCommon {
		public FmDataItem(EMosaic eMosaic, FmDataGroup group)
			: base(eMosaic.getIndex(), eMosaic.getDescription(false), "res/Mosaic/32x32/"+eMosaic.getDescription(true)+".png")
		{
			this._group = group;
			this.Subtitle = "Subtitle item...";
			this.Description = "Description item...";
		}

		private FmDataGroup _group;
		public FmDataGroup Group {
			get { return this._group; }
			set { this.SetProperty(ref this._group, value); }
		}
	}

	/// <summary>
	/// Generic group data model.
	/// </summary>
	public class FmDataGroup : FmDataCommon {
		public FmDataGroup(EMosaicGroup eMosaicGroup)
			: base(eMosaicGroup.getIndex(), eMosaicGroup.getDescription(), "res/MosaicGroup/" + eMosaicGroup.getDescription() + ".png")
		{
			Items.CollectionChanged += ItemsCollectionChanged;
			this.Subtitle = "Subtitle group...";
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

		public static FmDataGroup GetGroup(int uniqueId) {
			// Simple linear search is acceptable for small data sets
			var matches = AllGroups.Where((group) => (group.UniqueId == uniqueId));
			return matches.FirstOrDefault();
		}

		public static FmDataItem GetItem(int uniqueId) {
			// Simple linear search is acceptable for small data sets
			var matches = AllGroups.SelectMany(group => group.Items).Where((item) => (item.UniqueId == uniqueId));
			return matches.FirstOrDefault();
		}
	}
}