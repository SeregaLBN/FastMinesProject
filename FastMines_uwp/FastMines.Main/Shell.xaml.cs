using System;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using Windows.Foundation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.utils;
using fmg.DataModel.DataSources;
using MosaicsSkillImg = fmg.uwp.draw.img.win2d.MosaicsSkillImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using MosaicsGroupImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using fmg.DataModel.Items;

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

         //this.SizeChanged += OnSizeChanged;
         _sizeChangedObservable = Observable
            .FromEventPattern<SizeChangedEventHandler, SizeChangedEventArgs>(h => SizeChanged += h, h => SizeChanged -= h) // equals .FromEventPattern<SizeChangedEventArgs>(this, "SizeChanged")
            .Throttle(TimeSpan.FromSeconds(0.2)) // debounce events
            .Subscribe(x => AsyncRunner.InvokeFromUiLater(() => OnSizeChanged(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.Low));
      }

      private void OnPropertyCurrentElementChanged(MosaicGroupDataItem currentGroupItem, MosaicSkillDataItem currentSkillItem) {
         if ((currentGroupItem  == null) || (currentSkillItem == null)) {
            LoggerSimple.Put("TODO:  redirect to ShowHypnosisLogoPage...");
            return;
         }
         if (currentSkillItem.SkillLevel == ESkillLevel.eCustom) {
            LoggerSimple.Put("TODO:  redirect to CustomSizePage...");
            return;
         }
         var smp = RootFrame.Content as SelectMosaicPage;
         if (smp == null) {
            RootFrame.SourcePageType = typeof(SelectMosaicPage);
            smp = RootFrame.Content as SelectMosaicPage;
         }
         smp.CurrentElement = null;
         smp.CurrentMosaicGroup = currentGroupItem.MosaicGroup;
         smp.CurrentSkillLevel = currentSkillItem.SkillLevel;
      }

      private void MosaicGroupDsOnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicGroupsDataSource::" + ev.PropertyName);
         switch (ev.PropertyName) {
         case nameof(ViewModel.MosaicGroupDs.CurrentElement):
            OnPropertyCurrentElementChanged(((MosaicGroupsDataSource)sender).CurrentElement, ViewModel.MosaicSkillDs.CurrentElement);
            break;
         }
      }

      private void MosaicSkillDsOnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicSkillsDataSource::" + ev.PropertyName);
         switch(ev.PropertyName) {
         case nameof(ViewModel.MosaicSkillDs.CurrentElement):
            OnPropertyCurrentElementChanged(ViewModel.MosaicGroupDs.CurrentElement, ((MosaicSkillsDataSource)sender).CurrentElement);
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
            var wh = (int)Math.Min(Math.Max(50, size1), 100); // TODO: DPI dependency
            ViewModel.ImageSize = new common.geom.Size(wh, wh);
         }
         {
            var smp = RootFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var size2 = size/4;
               var wh = (int)Math.Min(Math.Max(100, size2), 200); // TODO: DPI dependency
               smp.ViewModel.ImageSize = new common.geom.Size(wh, wh);
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

      private void CanvasControl_CreateResources_MosaicsSkillImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
            System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsSkillImg);
            var img = canvasControl.DataContext as MosaicsSkillImg;
            if (img == null)
               return;
            canvasControl.Draw += (sender2, ev2) => {
               ev2.DrawingSession.DrawImage(img.Image, new Rect(0, 0, sender2.Width, sender2.Height));
            };
            img.PropertyChanged += (sender3, ev3) => {
               if (ev3.PropertyName == nameof(img.Image))
                  canvasControl.Invalidate();
            };
         } else {
            System.Diagnostics.Debug.Assert(false, "Support me"); // TODO
         }
      }

      private void CanvasControl_CreateResources_MosaicsGroupImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
            System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsGroupImg);
            var img = canvasControl.DataContext as MosaicsGroupImg;
            if (img == null)
               return;
            canvasControl.Draw += (sender2, ev2) => {
               ev2.DrawingSession.DrawImage(img.Image, new Rect(0, 0, sender2.Width, sender2.Height));
            };
            img.PropertyChanged += (sender3, ev3) => {
               if (ev3.PropertyName == nameof(img.Image))
                  canvasControl.Invalidate();
            };
         } else {
            System.Diagnostics.Debug.Assert(false, "Support me"); // TODO
         }
      }

   }
}
