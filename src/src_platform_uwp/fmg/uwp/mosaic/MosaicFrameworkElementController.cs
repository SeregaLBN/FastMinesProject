using System;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.Devices.Input;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.types;
using fmg.core.types.click;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic {

   /// <summary> MVC: controller. UWP implementation over control <see cref="FrameworkElement"/> </summary>
   /// <typeparam name="TImageAsFrameworkElement">image-control based of <see cref="FrameworkElement"/></typeparam>
   /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
   /// <typeparam name="TMosaicView">mosaic view</typeparam>
   public abstract class MosaicFrameworkElementController<TImageAsFrameworkElement, TImageInner, TMosaicView> : MosaicController<TImageAsFrameworkElement, TImageInner, TMosaicView, MosaicDrawModel<TImageInner>>
      where TImageAsFrameworkElement : FrameworkElement
      where TImageInner : class
      where TMosaicView : IMosaicView<TImageAsFrameworkElement, TImageInner, MosaicDrawModel<TImageInner>>
   {

      private readonly ClickInfo _clickInfo = new ClickInfo();

      public MosaicFrameworkElementController(TMosaicView view)
         : base(view)
      {
         SubscribeToViewControl();
      }

      public TImageAsFrameworkElement GetViewControl() {
         return View.Image;
      }

      bool ClickHandler(ClickResult clickResult) {
         if (clickResult == null)
            return false;
         _clickInfo.CellDown = clickResult.CellDown;
         _clickInfo.IsLeft = clickResult.IsLeft;
         var handled = clickResult.IsAnyChanges;
         if (clickResult.IsDown)
            _clickInfo.DownHandled = handled;
         else
            _clickInfo.UpHandled = handled;
         _clickInfo.Released = !clickResult.IsDown;
         return handled;
      }

      bool OnClickLost() {
         return ClickHandler(this.MouseFocusLost());
      }

      bool OnClick(Windows.Foundation.Point pos, bool leftClick, bool down) {
         var point = ToImagePoint(pos);
         return ClickHandler(down
               ? MousePressed(point, leftClick)
               : MouseReleased(point, leftClick));
      }

      void OnTapped(object sender, TappedRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnTapped", () => "handled=" + ev.Handled))
         {
            if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
               ev.Handled = OnClick(ev.GetPosition(GetViewControl()), true, false);
            }
         }
      }

      protected void OnDoubleTapped(object sender, DoubleTappedRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnDoubleTapped", () => "handled=" + ev.Handled))
         {
            var imgControl = GetViewControl();
            var rcImage = new Windows.Foundation.Rect(0, 0, imgControl.Width, imgControl.Height);
            if (rcImage.Contains(ev.GetPosition(imgControl))) {
               if (this.GameStatus == EGameStatus.eGSEnd) {
                  this.GameNew();
                  ev.Handled = true;
               }
            } else {
               //RecheckLocation();
               ev.Handled = true;
            }
            }
      }

      protected void OnRightTapped(object sender, RightTappedRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnRightTapped", () => "handled=" + ev.Handled))
         {
            if (ev.PointerDeviceType == PointerDeviceType.Mouse) {
          //   ev.Handled = _clickInfo.DownHandled || _clickInfo.UpHandled; // TODO: для избежания появления appBar'ов при установке '?'
            } else //if (!_manipulationStarted)
            {

               // 1. release left click in invalid coord
               OnClick(new Windows.Foundation.Point(-1, -1), true, false);

               // 2. make right click - up & down
               var imgControl = GetViewControl();
               var pos = ev.GetPosition(imgControl);
               var handled1 = OnClick(pos, false, true);
               var handled2 = OnClick(pos, false, false);
               ev.Handled = handled1 || handled2;
            }
            }
      }

      protected void OnPointerPressed(object sender, PointerRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnPointerPressed", () => "handled=" + ev.Handled))
         {
            var imgControl = GetViewControl();
            var currPoint = ev.GetCurrentPoint(imgControl);

            //_clickInfo.PointerDevice = pointerPoint.PointerDevice.PointerDeviceType;
            var props = currPoint.Properties;
            // Ignore button chords with the left, right, and middle buttons
          //if (!props.IsLeftButtonPressed && !props.IsRightButtonPressed && !props.IsMiddleButtonPressed) {
          //   // If back or foward are pressed (but not both) navigate appropriately
          //   var backPressed = props.IsXButton1Pressed;
          //   if (backPressed) {
          //      ev.Handled = true;
          //      GoBack();
          //   }
          //}

            ev.Handled = OnClickLost(); // Protection from the two-finger click.
            //if (_manipulationStarted) {
            //   // touch two-finger
            //   OnClickLost(); // Protection from the two-finger click.
            //}

            if (!ev.Handled)
               ev.Handled = OnClick(currPoint.Position, props.IsLeftButtonPressed, true);

            _clickInfo.DownHandled = ev.Handled;
            }
      }

      protected void OnPointerReleased(object sender, PointerRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnPointerReleased", () => "handled="+ ev.Handled))
         {
            var imgControl = GetViewControl();
            var currPoint = ev.GetCurrentPoint(imgControl);
            //if (_manipulationStarted)
            if (ev.Pointer.PointerDeviceType == PointerDeviceType.Mouse) {
               var isLeftClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.LeftButtonReleased);
               var isRightClick = (currPoint.Properties.PointerUpdateKind == PointerUpdateKind.RightButtonReleased);
               System.Diagnostics.Debug.Assert(isLeftClick != isRightClick);
               ev.Handled = OnClick(currPoint.Position, isLeftClick, false);
            } else {
               AsyncRunner.InvokeFromUiLater(() => {
                  if (!_clickInfo.Released) {
                     LoggerSimple.Put("ã OnPointerReleased: forced left release click...");
                     OnClick(currPoint.Position, true, false);
                  }
               }, CoreDispatcherPriority.High);
            }

            _clickInfo.UpHandled = ev.Handled;
         }
      }

      protected void OnPointerCaptureLost(object sender, PointerRoutedEventArgs ev) {
         //using (new fmg.common.Tracer("MosaicFEC.OnPointerCaptureLost", () => "handled=" + ev.Handled))
         {
            var imgControl = GetViewControl();
            var currPoint = ev.GetCurrentPoint(imgControl);
            if (!_clickInfo.Released) {
               LoggerSimple.Put("ã OnPointerCaptureLost: forced left release click...");
               OnClick(currPoint.Position, true, false);
            }
         }
      }

      void OnFocusLost(object sender, RoutedEventArgs ev) {
         //LoggerSimple.Put("<> MosaicFEC.OnFocusLost");
         this.MouseFocusLost();
      }

      protected override bool CheckNeedRestoreLastGame() {
         return false;
      }

      private void SubscribeToViewControl() {
         var imgControl = this.GetViewControl();
         imgControl.Tapped += OnTapped;
         imgControl.DoubleTapped += OnDoubleTapped;
         imgControl.RightTapped += OnRightTapped;
         imgControl.PointerPressed += OnPointerPressed;
         imgControl.PointerReleased += OnPointerReleased;
         imgControl.PointerCaptureLost += OnPointerCaptureLost;
         imgControl.LostFocus += OnFocusLost;
      }

      private void UnsubscribeToViewControl() {
         var imgControl = this.GetViewControl();
         imgControl.Tapped += OnTapped;
         imgControl.DoubleTapped -= OnDoubleTapped;
         imgControl.RightTapped -= OnRightTapped;
         imgControl.PointerPressed -= OnPointerPressed;
         imgControl.PointerReleased -= OnPointerReleased;
         imgControl.PointerCaptureLost -= OnPointerCaptureLost;
         imgControl.LostFocus -= OnFocusLost;
      }

      protected override void Disposing() {
         UnsubscribeToViewControl();
         base.Disposing();
      }

      private PointDouble ToImagePoint(Windows.Foundation.Point pagePoint) {
         var imgControl = this.GetViewControl();
         var point = pagePoint.ToFmPointDouble(); // imgControl.TransformToVisual(imgControl).TransformPoint(pagePoint).ToFmPointDouble();
         //var o = GetOffset();
         //var point2 = new PointDouble(pagePoint.X - o.Left, pagePoint.Y - o.Top);
         //System.Diagnostics.Debug.Assert(point == point2);
         return point;
      }
      class ClickInfo {
         public BaseCell CellDown { get; set; }
         public bool IsLeft { get; set; }
         /// <summary> pressed or released </summary>
         public bool Released { get; set; }
         public bool DownHandled { get; set; }
         public bool UpHandled { get; set; }
         //public PointerDeviceType PointerDevice { get; set; }
      }

   }

}
