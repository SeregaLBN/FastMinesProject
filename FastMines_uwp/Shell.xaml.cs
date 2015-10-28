using System;
using System.ComponentModel;
using System.Linq;
using Windows.UI.Core;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.core.types;
using fmg.uwp.res.img;
using FastMines.Common;
using FastMines.Data;
using FastMines.Pages;
using FastMines.Presentation;

namespace FastMines
{
   public sealed partial class Shell : UserControl
   {
      private readonly ShellViewModel _vm = new ShellViewModel();

      public Shell()
      {
         this.InitializeComponent();

         foreach (var fmDataGroup in FmDataSource.AllGroups)
         {
            fmDataGroup.MosaicGroupImage.PolarLights = false;
            fmDataGroup.MosaicGroupImage.Rotate = false;
            _vm.MenuItems.Add(new MenuItem
            {
               MosaicGroupImage = fmDataGroup.MosaicGroupImage,
               Icon = fmDataGroup.UniqueId.UnicodeChar(false).ToString(),
               Title = fmDataGroup.Title,
               PageType = typeof(WelcomePage)
            });
         }
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE170", Title = "Welcome", PageType = typeof(WelcomePage) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE115", Title = "Page 1", PageType = typeof(Page1) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE1C3", Title = "Page 2", PageType = typeof(Page2) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE212", Title = "Page 3", PageType = typeof(Page3) });

         // select the first menu item
         _vm.SelectedMenuItem = _vm.MenuItems.First();
         OnViewModelPropertyChanged(_vm, new PropertyChangedEventArgs("SelectedPageType"));

         this.ViewModel = _vm;

         _vm.PropertyChanged += OnViewModelPropertyChanged;
      }

      private void OnViewModelPropertyChanged(object sender, PropertyChangedEventArgs ev)
      {
         if (ev.PropertyName == "SelectedPageType")
         {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender as ShellViewModel, _vm));

            // for all - stop animate
            _vm.MenuItems.ToList().ForEach(mi =>
            {
               if (ReferenceEquals(mi, sender))
                  return;
               mi.MosaicGroupImage.PolarLights = false;
               mi.MosaicGroupImage.Rotate = false;
               mi.MosaicGroupImage.BkColor = Color.Gray.Brighter(0.4);
               mi.Icon = mi.MosaicGroupImage.MosaicGroup.UnicodeChar(false).ToString();
            });

            // for one selected- start animate
            _vm.SelectedMenuItem.MosaicGroupImage.Rotate = true;
            _vm.SelectedMenuItem.MosaicGroupImage.PolarLights = true;
            _vm.SelectedMenuItem.MosaicGroupImage.BkColor = MosaicsGroupImg.BkColorDefault;
            _vm.SelectedMenuItem.Icon = _vm.SelectedMenuItem.MosaicGroupImage.MosaicGroup.UnicodeChar(true).ToString();
         }
      }

      public ShellViewModel ViewModel { get; private set; }

      public Frame RootFrame
      {
         get
         {
            return this.Frame;
         }
      }
   }
}
