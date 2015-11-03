using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows.Input;
using fmg.core.types;
using fmg.uwp.res.img;
using FastMines.Presentation.Menu;
using FastMines.Presentation.Notyfier;

namespace FastMines.Presentation
{
   public class ShellViewModel : NotifyPropertyChanged
   {
      private ObservableCollection<MenuItem> _menuItems = new ObservableCollection<MenuItem>();
      private MenuItem _selectedMenuItem;
      private bool _isSplitViewPaneOpen;
      private int _imageSize = MosaicsGroupImg.DefaultImageSize / 2;

      public ShellViewModel()
      {
         this.ToggleSplitViewPaneCommand = new Command(() => this.IsSplitViewPaneOpen = !this.IsSplitViewPaneOpen);
      }

      public ICommand ToggleSplitViewPaneCommand { get; private set; }

      public bool IsSplitViewPaneOpen
      {
         get { return this._isSplitViewPaneOpen; }
         set { SetProperty(ref this._isSplitViewPaneOpen, value); }
      }


      public MenuItem SelectedMenuItem
      {
         get { return this._selectedMenuItem; }
         set
         {
            if (SetProperty(ref this._selectedMenuItem, value))
            {
               OnPropertyChanged("SelectedPageType");
               OnPropertyChanged("UnicodeChars");

               // auto-close split view pane
               //this.IsSplitViewPaneOpen = false;
            }
         }
      }

      public string UnicodeChars
      {
         get
         {
            var smi = SelectedMenuItem;
            return string.Join(" ", MenuItems.Select(mi =>
            {
               var selected = (smi != null) && (mi.MosaicGroupImage.MosaicGroup == smi.MosaicGroupImage.MosaicGroup);
               return mi.MosaicGroupImage.MosaicGroup.UnicodeChar(selected);
            }));
         }
      }

      public Type SelectedPageType
      {
         get
         {
            return this._selectedMenuItem?.PageType;
         }
         set
         {
            // select associated menu item
            this.SelectedMenuItem = (value == null)
               ? null
               : this.MenuItems.FirstOrDefault(m => m.PageType == value);
         }
      }

      public ObservableCollection<MenuItem> MenuItems
      {
         get
         {
            if (!_menuItems.Any()) {
               // add elements
               var groups = EMosaicGroupEx.GetValues();
               foreach (var g in groups) {
                  _menuItems.Add(new MenuItem(g) {ImageSize = ImageSize});
               }
            }
            return this._menuItems;
         }
      }

      public int ImageSize {
         get { return _imageSize; }
         set {
            if (SetProperty(ref _imageSize, value)) {
               foreach (var mi in MenuItems) {
                  mi.ImageSize = value;
               }
            }
         }
      }
   }
}
