using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw;
using fmg.uwp.res.img;
using FastMines.Common;
using FastMines.Data;
using FastMines.Pages;
using FastMines.Presentation;
using FastMines.Presentation.Menu;

namespace FastMines
{
   public sealed partial class Shell : UserControl
   {
      private readonly ShellViewModel _vm;

      public Shell()
      {
         this.InitializeComponent();
         Unloaded += OnClosing;

         _vm = new ShellViewModel();
         foreach (var mi in _vm.MosaicGroupDs.MenuItems)
         {
            mi.PageType = typeof (WelcomePage);
         }
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE170", Title = "Welcome", PageType = typeof(WelcomePage) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE115", Title = "Page 1", PageType = typeof(Page1) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE1C3", Title = "Page 2", PageType = typeof(Page2) });
         //_vm.MenuItems.Add(new MenuItem { Icon = "\uE212", Title = "Page 3", PageType = typeof(Page3) });

         this.ViewModel = _vm;

         this.SizeChanged += OnSizeChanged;
      }

      public ShellViewModel ViewModel { get; private set; }

      public Frame RootFrame => this._frame;

      private void OnClosing(object sender, RoutedEventArgs ev) {
         System.Diagnostics.Debug.WriteLine("OnClosing");
      }

      void OnSizeChanged(object sender, SizeChangedEventArgs ev) {
         System.Diagnostics.Debug.WriteLine("OnSizeChanged");
         //foreach (var stackPanel in FindChilds<StackPanel>(_listView, 10)) {
         //   stackPanel.Height++;
         //   var cnt = VisualTreeHelper.GetChildrenCount(stackPanel);
         //   cnt++;
         //}
      }

      public static IEnumerable<T> FindChilds<T>(FrameworkElement parent, int depth = 1, Func<T, bool> filter = null)
         where T : FrameworkElement
      {
         var cnt = VisualTreeHelper.GetChildrenCount(parent);
         for (var i = 0; i < cnt; i++) {
            var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
            var correctlyTyped = child as T;
            if (correctlyTyped != null && (filter == null || filter(correctlyTyped)))
               yield return correctlyTyped;
         }
         for (var i = 0; (depth > 1) && (i < cnt); i++) {
            var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
            foreach (var c in FindChilds(child, depth - 1, filter))
               yield return c;
         }
      }
   }
}
