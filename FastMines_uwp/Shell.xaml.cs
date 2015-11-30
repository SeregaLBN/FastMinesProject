using System;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using FastMines.Pages;
using FastMines.Presentation;

namespace FastMines
{
   public sealed partial class Shell : UserControl
   {
      internal const int MenuTextWidth = 110;

      private readonly ShellViewModel _vm;

      public Shell() {
         this.InitializeComponent();
         Unloaded += OnClosing;

         _vm = new ShellViewModel();
         foreach (var mi in _vm.MosaicGroupDs.DataSource) {
            mi.PageType = typeof(WelcomePage);
         }

         this.ViewModel = _vm;
         this.SizeChanged += OnSizeChanged;
      }

      public ShellViewModel ViewModel { get; private set; }

      public Frame RootFrame => this._frame;


      private void OnClosing(object sender, RoutedEventArgs ev) {
         System.Diagnostics.Debug.WriteLine("OnClosing");
         _vm?.Dispose();
      }

      void OnSizeChanged(object sender, SizeChangedEventArgs ev) {
         System.Diagnostics.Debug.WriteLine("OnSizeChanged");
         //foreach (var stackPanel in FindChilds<StackPanel>(_listView, 10)) {
         //   stackPanel.Height++;
         //   var cnt = VisualTreeHelper.GetChildrenCount(stackPanel);
         //   cnt++;
         //}
         var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
         size = size / 7;
         //Windows.Graphics.Display.DisplayInformation.GetForCurrentView().LogicalDpi
         _vm.ImageSize = (int)Math.Min(Math.Max(50, size), 100); // TODO: DPI dependency
      }

      public static IEnumerable<T> FindChilds<T>(FrameworkElement parent, int depth = 1, Func<T, bool> filter = null)
         where T : FrameworkElement {
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

      private void ButtonSplitSkillLevel_OnClick(object sender, RoutedEventArgs e) {
         _listViewSkillLevelMenu.Visibility = (_listViewSkillLevelMenu.Visibility == Visibility.Visible)
            ? Visibility.Collapsed
            : Visibility.Visible;
      }
   }
}
