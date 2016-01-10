using System;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using FastMines.Pages;
using FastMines.Presentation;
using FastMines.Common;

namespace FastMines
{
   public sealed partial class Shell : UserControl
   {
      internal const int MenuTextWidth = 110;

      public Shell() {
         this.InitializeComponent();
         Unloaded += OnClosing;
         //Loaded += (sender, ev) => { // unit test
         //   var r = new Random(Guid.NewGuid().GetHashCode());
         //   for (int i = 0; i < 100; ++i) {
         //      TileHelper.CreateRandomMosaicImage(10 + r.Next(100), 10+r.Next(100));
         //   }
         //};

         var vm = new ShellViewModel();
         foreach (var mi in vm.MosaicGroupDs.DataSource) {
            mi.PageType = typeof(WelcomePage);
         }

         this.ViewModel = vm;

         //this.SizeChanged += OnSizeChanged;
         _sizeChangedObservable = Observable
            .FromEventPattern<SizeChangedEventHandler, SizeChangedEventArgs>(h => SizeChanged += h, h => SizeChanged -= h) // equals .FromEventPattern<SizeChangedEventArgs>(this, "SizeChanged")
            .Throttle(TimeSpan.FromSeconds(0.2)) // debounce events
            .Subscribe(x => AsyncRunner.InvokeFromUiLater(() => OnSizeChanged(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.Low));
      }

      readonly IDisposable _sizeChangedObservable;

      public ShellViewModel ViewModel { get; private set; }

      public Frame RootFrame => this._frame;


      private void OnClosing(object sender, RoutedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnClosing");
         ViewModel?.Dispose();
         _sizeChangedObservable.Dispose();
      }

      void OnSizeChanged(object sender, SizeChangedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnSizeChanged");
         var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
         size = size / 7;
         ViewModel.ImageSize = (int)Math.Min(Math.Max(50, size), 100); // TODO: DPI dependency
      }

      //public static IEnumerable<T> FindChilds<T>(FrameworkElement parent, int depth = 1, Func<T, bool> filter = null)
      //   where T : FrameworkElement {
      //   var cnt = VisualTreeHelper.GetChildrenCount(parent);
      //   for (var i = 0; i < cnt; i++) {
      //      var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
      //      var correctlyTyped = child as T;
      //      if (correctlyTyped != null && (filter == null || filter(correctlyTyped)))
      //         yield return correctlyTyped;
      //   }
      //   for (var i = 0; (depth > 1) && (i < cnt); i++) {
      //      var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
      //      foreach (var c in FindChilds(child, depth - 1, filter))
      //         yield return c;
      //   }
      //}

   }
}
