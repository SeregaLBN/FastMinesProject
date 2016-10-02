using System;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom.util;
using fmg.core.types;
using fmg.core.img;
using fmg.data.controller.types;
using fmg.uwp.utils;
using fmg.DataModel.Items;
using fmg.DataModel.DataSources;
using MosaicsSkillCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsSkillImg.CanvasBmp;
using MosaicsGroupCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsGroupImg.CanvasBmp;

namespace fmg
{
   public sealed partial class Main : UserControl
   {
      internal const int MenuTextWidth = 110;

      readonly IDisposable _sizeChangedObservable;

      public ShellViewModel ViewModel { get; } = new ShellViewModel();

      public Frame RootFrame => this._frame;


      public Main() {
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
         ViewModel.MosaicGroupDs.CurrentElement = ViewModel.MosaicGroupDs.DataSource.First(x => x.MosaicGroup == EMosaicGroup.eQuadrangles);
         ViewModel.MosaicSkillDs.CurrentElement = ViewModel.MosaicSkillDs.DataSource.First(x => x.SkillLevel == ESkillLevel.eBeginner);
         Loaded += (sender, ev) => {
            var smp = RootFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var ds = smp.ViewModel.MosaicsDs;
               ds.CurrentElement = ds.DataSource.First();
            }

            ApplyButtonColorSmoothTransition(_togglePaneButton, ViewModel.MosaicGroupDs.TopElement.Image);
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
         smp.CurrentMosaicGroup = currentGroupItem.MosaicGroup.Value;
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
            ViewModel.TopImageSize = new common.geom.Size(wh, 48);
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
         System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsSkillCanvasBmp);
         CanvasControl_CreateResources(canvasControl, ev);
      }

      private void CanvasControl_CreateResources_MosaicsGroupImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsGroupCanvasBmp);
         CanvasControl_CreateResources(canvasControl, ev);
      }
      private void CanvasControl_CreateResources(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
            var img = canvasControl.DataContext as StaticImg<CanvasBitmap>;
            if (img == null)
               return;
            canvasControl.Draw += (sender2, ev2) => {
               ev2.DrawingSession.DrawImage(img.Image, new Windows.Foundation.Rect(0, 0, sender2.Width, sender2.Height)); // zoomed size
               //ev2.DrawingSession.DrawImage(img.Image, new Windows.Foundation.Rect(0, 0, img.Width, img.Height)); // real size
            };
            img.PropertyChanged += (sender3, ev3) => {
               if (ev3.PropertyName == nameof(img.Image))
                  canvasControl.Invalidate();
            };
         } else {
            System.Diagnostics.Debug.Assert(false, "Support me"); // TODO
         }
      }

      private void ApplyButtonColorSmoothTransition(ToggleButton bttn, StaticImg<CanvasBitmap> image) {
         int flag = 0;
         var clrFrom = image.BackgroundColor; //Color.Coral;
         var clrTo = Color.BlueViolet;
         double fullTimeMsec = 1500, repeatTimeMsec = 100;
         double currStepAngle = 0;
         double deltaStepAngle = 360.0 / (fullTimeMsec / repeatTimeMsec);
         bttn.PointerEntered += (s, ev3) => {
            flag = 1; // start entered
            Action r = () => {
               Color clrCurr;
               if (currStepAngle >= 360) {
                  flag = 0; // stop
                  clrCurr = clrTo;
               } else {
                  currStepAngle += deltaStepAngle;
                  var sin = Math.Sin((currStepAngle / 4).ToRadian());
                  clrCurr.A = (byte)(clrFrom.A + sin * (clrTo.A - clrFrom.A));
                  clrCurr.R = (byte)(clrFrom.R + sin * (clrTo.R - clrFrom.R));
                  clrCurr.G = (byte)(clrFrom.G + sin * (clrTo.G - clrFrom.G));
                  clrCurr.B = (byte)(clrFrom.B + sin * (clrTo.B - clrFrom.B));
               }
               image.BackgroundColor = clrCurr;
            };
            r.RepeatNoWait(TimeSpan.FromMilliseconds(repeatTimeMsec), () => flag != 1);
         };
         bttn.PointerExited += (s, ev3) => {
            flag = 2;
            Action r = () => {
               Color clrCurr;
               if (currStepAngle <= 0) {
                  flag = 0; // stop
                  clrCurr = clrFrom;
               } else {
                  currStepAngle -= deltaStepAngle;
                  var cos = Math.Cos((currStepAngle / 4).ToRadian());
                  clrCurr.A = (byte)(clrTo.A - (1 - cos * (clrFrom.A - clrTo.A)));
                  clrCurr.R = (byte)(clrTo.R - (1 - cos * (clrFrom.R - clrTo.R)));
                  clrCurr.G = (byte)(clrTo.G - (1 - cos * (clrFrom.G - clrTo.G)));
                  clrCurr.B = (byte)(clrTo.B - (1 - cos * (clrFrom.B - clrTo.B)));
               }
               image.BackgroundColor = clrCurr;
            };
            r.RepeatNoWait(TimeSpan.FromMilliseconds(repeatTimeMsec), () => flag != 2);
         };
      }

   }

}
