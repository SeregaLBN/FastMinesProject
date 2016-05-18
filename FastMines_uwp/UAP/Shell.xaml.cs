using System;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.common;
using fmg.Common;
using fmg.DataModel.DataSources;
using fmg.common.Controls;

namespace fmg
{
   public sealed partial class Shell : UserControl
   {
      internal const int MenuTextWidth = 110;

      readonly IDisposable _sizeChangedObservable;

      public ShellViewModel ViewModel { get; } = new ShellViewModel();

      public Frame RootFrame => this._frame;


      public Shell() {
         this.InitializeComponent();
         Unloaded += OnClosing;
         //Loaded += (sender, ev) => { // unit test
         //   var r = new Random(Guid.NewGuid().GetHashCode());
         //   for (int i = 0; i < 100; ++i) {
         //      TileHelper.CreateRandomMosaicImage(10 + r.Next(100), 10+r.Next(100));
         //   }
         //};

         ViewModel.MosaicGroupDs.PropertyChanged += MosaicGroupDsOnPropertyChanged;
         ViewModel.MosaicSkillDs.PropertyChanged += MosaicSkillDsOnPropertyChanged;
         ViewModel.MosaicGroupDs.CurrentElement = ViewModel.MosaicGroupDs.DataSource.First(x => x.MosaicGroup == EMosaicGroup.ePentagons);
         ViewModel.MosaicSkillDs.CurrentElement = ViewModel.MosaicSkillDs.DataSource.First(x => x.SkillLevel == ESkillLevel.eCrazy);
         Loaded += (sender, ev) => {
            var smp = RootFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var ds = smp.ViewModel.MosaicsDs;
               ds.CurrentElement = ds.DataSource.First();
            }
         };

         foreach (var mi in ViewModel.MosaicGroupDs.DataSource) {
            mi.PageType = typeof(SelectMosaicPage);
         }

         //this.SizeChanged += OnSizeChanged;
         _sizeChangedObservable = Observable
            .FromEventPattern<SizeChangedEventHandler, SizeChangedEventArgs>(h => SizeChanged += h, h => SizeChanged -= h) // equals .FromEventPattern<SizeChangedEventArgs>(this, "SizeChanged")
            .Throttle(TimeSpan.FromSeconds(0.2)) // debounce events
            .Subscribe(x => AsyncRunner.InvokeFromUiLater(() => OnSizeChanged(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.Low));
      }

      private void MosaicGroupDsOnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicGroupsDataSource::" + ev.PropertyName);
         switch (ev.PropertyName) {
         case "CurrentElement":
            var smp = RootFrame?.Content as SelectMosaicPage;
            var ds = (MosaicGroupsDataSource)sender;
            if (smp == null) {
               SelectMosaicPage.DefaultMosaicGroup = ds.CurrentElement.MosaicGroup;
            } else {
               smp.CurrentMosaicGroup = ds.CurrentElement.MosaicGroup;
            }
            break;
         }
      }

      private void MosaicSkillDsOnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicSkillsDataSource::" + ev.PropertyName);
         switch(ev.PropertyName) {
         case "CurrentElement":
            var smp = RootFrame?.Content as SelectMosaicPage;
            var ds = (MosaicSkillsDataSource)sender;
            if (smp == null) {
               SelectMosaicPage.DefaultSkillLevel = ds.CurrentElement.SkillLevel;
            } else {
               smp.CurrentSkillLevel = ds.CurrentElement.SkillLevel;
            }
            break;
         }
      }

      private void OnClosing(object sender, RoutedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnClosing");
         ViewModel.MosaicGroupDs.PropertyChanged -= MosaicGroupDsOnPropertyChanged;
         ViewModel.MosaicSkillDs.PropertyChanged -= MosaicSkillDsOnPropertyChanged;

         ViewModel.Dispose();
         _sizeChangedObservable.Dispose();
      }

      void OnSizeChanged(object sender, SizeChangedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnSizeChanged");
         var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
         {
            var size1 = size/7;
            ViewModel.ImageSize = (int)Math.Min(Math.Max(50, size1), 100); // TODO: DPI dependency
         }
         {
            var smp = RootFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var size2 = size/4;
               smp.ViewModel.ImageSize = (int)Math.Min(Math.Max(100, size2), 200); // TODO: DPI dependency
            }
         }
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
