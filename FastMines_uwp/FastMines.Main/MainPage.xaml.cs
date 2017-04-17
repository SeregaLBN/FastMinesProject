using System;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
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
   public sealed partial class MainPage : Page
   {
      internal const int MenuTextWidth = 110;

      readonly IDisposable _sizeChangedObservable;

      public ShellViewModel ViewModel { get; } = new ShellViewModel();

      public Frame RightFrame => this._frame;


      public MainPage() {
         this.InitializeComponent();
         Unloaded += OnClosing;
         //Loaded += (sender, ev) => { // unit test
         //   var r = ThreadLocalRandom.Current;
         //   for (int i = 0; i < 100; ++i) {
         //      TileHelper.CreateRandomMosaicImage(10 + r.Next(100), 10+r.Next(100));
         //   }
         //};

         ViewModel.MosaicGroupDs.PropertyChanged += OnMosaicGroupDsPropertyChanged;
         ViewModel.MosaicSkillDs.PropertyChanged += OnMosaicSkillDsPropertyChanged;
         ViewModel.MosaicGroupDs.CurrentElement = ViewModel.MosaicGroupDs.DataSource.First(x => x.MosaicGroup == EMosaicGroup.eQuadrangles);
         ViewModel.MosaicSkillDs.CurrentElement = ViewModel.MosaicSkillDs.DataSource.First(x => x.SkillLevel == ESkillLevel.eBeginner);
         Loaded += (sender, ev) => {
            var smp = RightFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var ds = smp.ViewModel.MosaicsDs;
               ds.CurrentElement = ds.DataSource.First();
            }

            ApplyButtonColorSmoothTransition(_bttnGroupPanel, ViewModel.MosaicGroupDs.TopElement.Image);
            ApplyButtonColorSmoothTransition(_bttnSkillPanel, ViewModel.MosaicSkillDs.TopElement.Image);
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
         var smp = RightFrame.Content as SelectMosaicPage;
         if (smp == null) {
            RightFrame.SourcePageType = typeof(SelectMosaicPage);
            smp = RightFrame.Content as SelectMosaicPage;
         }
         smp.CurrentElement = null;
         smp.CurrentMosaicGroup = currentGroupItem.MosaicGroup.Value;
         smp.CurrentSkillLevel = currentSkillItem.SkillLevel.Value;
      }

      private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicGroupsDataSource::" + ev.PropertyName);
         switch (ev.PropertyName) {
         case nameof(ViewModel.MosaicGroupDs.CurrentElement):
            OnPropertyCurrentElementChanged(((MosaicGroupsDataSource)sender).CurrentElement, ViewModel.MosaicSkillDs.CurrentElement);
            break;
         }
      }

      private void OnMosaicSkillDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("MosaicSkillsDataSource::" + ev.PropertyName);
         switch(ev.PropertyName) {
         case nameof(ViewModel.MosaicSkillDs.CurrentElement):
            OnPropertyCurrentElementChanged(ViewModel.MosaicGroupDs.CurrentElement, ((MosaicSkillsDataSource)sender).CurrentElement);
            break;
         }
      }

      private void OnClosing(object sender, RoutedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnClosing");
         ViewModel.MosaicGroupDs.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
         ViewModel.MosaicSkillDs.PropertyChanged -= OnMosaicSkillDsPropertyChanged;

         ViewModel.Dispose();
         _sizeChangedObservable.Dispose();
      }

      void OnSizeChanged(object sender, SizeChangedEventArgs ev) {
         //System.Diagnostics.Debug.WriteLine("OnSizeChanged");
         var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
         {
            const int minSize = 50;
            const int topElemHeight = 48;
            const int pad = 3;
            System.Diagnostics.Debug.Assert(topElemHeight <= minSize);

            var size1 = size/7;
            var wh = (int)Math.Min(Math.Max(minSize, size1), 100); // TODO: DPI dependency
            ViewModel.MosaicGroupDs.ImageSize =
            ViewModel.MosaicSkillDs.ImageSize = new Size(wh, wh);

            ViewModel.MosaicGroupDs.TopElement.ImageSize =
            ViewModel.MosaicSkillDs.TopElement.ImageSize = new Size(wh, topElemHeight);
            ViewModel.MosaicGroupDs.TopElement.ImagePadding =
            ViewModel.MosaicSkillDs.TopElement.ImagePadding = new Bound(pad, pad, wh - topElemHeight + pad, pad); // left margin

            int whBurger = topElemHeight / 2 + Math.Min(topElemHeight / 2 - pad, Math.Max(0, (int)(wh - 1.5 * topElemHeight)));
            Bound padBurger = new Bound(wh - whBurger, topElemHeight - whBurger, pad, pad);
            ViewModel.MosaicGroupDs.TopElement.ImagePaddingBurgerMenu =
            ViewModel.MosaicSkillDs.TopElement.ImagePaddingBurgerMenu = padBurger; // right-bottom margin
         }
         {
            var smp = RightFrame?.Content as SelectMosaicPage;
            if (smp != null) {
               var size2 = size/4;
               var wh = (int)Math.Min(Math.Max(100, size2), 200); // TODO: DPI dependency
               smp.ViewModel.ImageSize = new Size(wh, wh);
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

      private void OnCreateResourcesCanvasControl_MosaicsSkillImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsSkillCanvasBmp);
         OnCreateResourcesCanvasControl(canvasControl, ev);
      }

      private void OnCreateResourcesCanvasControl_MosaicsGroupImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
         System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsGroupCanvasBmp);
         OnCreateResourcesCanvasControl(canvasControl, ev);
      }
      private void OnCreateResourcesCanvasControl(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
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

      private void ApplyButtonColorSmoothTransition(Button bttn, StaticImg<CanvasBitmap> image) {
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
            flag = 2; // start exited
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

      private void OnClickBttnGroupPanel(object sender, RoutedEventArgs e) {
         if (_listViewMosaicGroupMenu.Visibility == Visibility.Collapsed) {
            ApplySmoothVisibilityOverScale(typeof(EMosaicGroup), _listViewMosaicGroupMenu, Visibility.Visible);
            ViewModel.MosaicGroupDs.TopElement.Image.HorizontalBurgerMenu = false;
         } else {
            _splitView.IsPaneOpen = !_splitView.IsPaneOpen;
            ViewModel.MosaicGroupDs.TopElement.Image.RotateAngleDelta = -ViewModel.MosaicGroupDs.TopElement.Image.RotateAngleDelta;
         }
      }

      private void OnClickBttnSkillPanel(object sender, RoutedEventArgs e) {
         if (_listViewSkillLevelMenu.Visibility == Visibility.Collapsed) {
            ApplySmoothVisibilityOverScale(typeof(ESkillLevel), _listViewSkillLevelMenu, Visibility.Visible);
            ViewModel.MosaicSkillDs.TopElement.Image.RotateAngleDelta = -ViewModel.MosaicSkillDs.TopElement.Image.RotateAngleDelta;
         } else {
            if (!_scroller.ScrollableHeight.HasMinDiff(_scroller.VerticalOffset) && (_listViewMosaicGroupMenu.Visibility == Visibility.Visible)) {
               ApplySmoothVisibilityOverScale(typeof(EMosaicGroup), _listViewMosaicGroupMenu, Visibility.Collapsed);
               ViewModel.MosaicGroupDs.TopElement.Image.HorizontalBurgerMenu = true;
            } else {
               ApplySmoothVisibilityOverScale(typeof(ESkillLevel), _listViewSkillLevelMenu, Visibility.Collapsed);
               ViewModel.MosaicSkillDs.TopElement.Image.RotateAngleDelta = -ViewModel.MosaicSkillDs.TopElement.Image.RotateAngleDelta;
            }
         }
      }

      /// <summary> set pseudo-async ListView.Visibility = target </summary>
      private void ApplySmoothVisibilityOverScale(Type enumType, ListView lv, Visibility target) {
         if (lv.Visibility == target)
            return;

         // save
         var original = lv.RenderTransform;
         if (original is CompositeTransform)
            return; // already called for this list
         var h0 = lv.Height;

         var h1 = h0;
         if (double.IsNaN(h1)) // WTF!
            h1 = Enum.GetValues(enumType).Length * (ViewModel.MosaicGroupDs.ImageSize.Height + 2 /* padding */);

         var transformer = new CompositeTransform();
         lv.RenderTransform = transformer;

         var toVisible = (target == Visibility.Visible);
         if (toVisible) {
            transformer.ScaleX = transformer.ScaleY = 0.01;
            lv.Height = 0.1;        // first  - set min height
            lv.Visibility = target; // second - set Visibility.Visible before smooting
         } else {
            transformer.ScaleX = transformer.ScaleY = 1;
         }

         var angle = 0.0;
         Action r = () => {
            angle += 12.345;

            if (angle < 90) { // repeat?
               var scale = toVisible
                  ? Math.Sin(angle.ToRadian())
                  : Math.Cos(angle.ToRadian());
               transformer.ScaleX = transformer.ScaleY = scale;
               lv.Height = h1 * scale;
            } else {
               // stop it

               if (!toVisible)
                  lv.Visibility = target;          // first - set Visibility.Collapsed after smooting

               // restore
               lv.RenderTransform = original; // mark to stop repeat
               lv.Height = h0;                     // second - restore original height
            }
         };
         r.RepeatNoWait(TimeSpan.FromMilliseconds(50), () => ReferenceEquals(lv.RenderTransform, original));
      }

   }

}
