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
using fmg.uwp.utils.wbmp;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.wbmp {

   /** MVC: controller. UWP WriteableBitmap implementation */
   public class MosaicCanvasController : MosaicController<Canvas, WriteableBitmap, MosaicCanvasView, MosaicDrawModel<WriteableBitmap>> {

      private readonly ClickInfo _clickInfo = new ClickInfo();

      public MosaicCanvasController()
         : base(new MosaicCanvasView())
      {
         SubscribeToViewControl();
      }

      public Canvas GetViewPanel() {
         return View.GetControl();
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
         var point = ToCanvasPoint(pos);
         return ClickHandler(down
               ? MousePressed(point, leftClick)
               : MouseReleased(point, leftClick));
      }

      void OnTapped(object sender, TappedRoutedEventArgs ev) {
         if (ev.PointerDeviceType != PointerDeviceType.Mouse) {
            ev.Handled = OnClick(ev.GetPosition(View.GetControl()), true, false);
         }
      }

      protected void OnDoubleTapped(object sender, DoubleTappedRoutedEventArgs ev) {
         var canvas = View.GetControl();
         var rcCanvas = new Windows.Foundation.Rect(0, 0, canvas.Width, canvas.Height);
         if (rcCanvas.Contains(ev.GetPosition(canvas))) {
            if (this.GameStatus == EGameStatus.eGSEnd) {
               this.GameNew();
               ev.Handled = true;
            }
         } else {
            //RecheckLocation();
            ev.Handled = true;
         }
      }

      protected void OnRightTapped(object sender, RightTappedRoutedEventArgs ev) {
       //if (ev.PointerDeviceType == PointerDeviceType.Mouse)
       //   ev.Handled = _clickInfo.DownHandled || _clickInfo.UpHandled; // TODO: для избежания появления appBar'ов при установке '?'
       //else if (!_manipulationStarted)
         {

            // 1. release left click in invalid coord
            OnClick(new Windows.Foundation.Point(-1, -1), true, false);

            // 2. make right click - up & down
            var canvas = View.GetControl();
            var pos = ev.GetPosition(canvas);
            var handled1 = OnClick(pos, false, true);
            var handled2 = OnClick(pos, false, false);
            ev.Handled = handled1 || handled2;
         }
      }

      protected void OnPointerPressed(object sender, PointerRoutedEventArgs ev) {
         var canvas = View.GetControl();
         var currPoint = ev.GetCurrentPoint(canvas);

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

      protected void OnPointerReleased(object sender, PointerRoutedEventArgs ev) {
         var canvas = View.GetControl();
         var currPoint = ev.GetCurrentPoint(canvas);
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

      protected void OnPointerCaptureLost(object sender, PointerRoutedEventArgs ev) {
         var canvas = View.GetControl();
         var currPoint = ev.GetCurrentPoint(canvas);
         if (!_clickInfo.Released) {
            LoggerSimple.Put("ã OnPointerCaptureLost: forced left release click...");
            OnClick(currPoint.Position, true, false);
         }
      }

      void OnFocusLost(object sender, RoutedEventArgs ev) {
         //System.out.println("Mosaic::MosaicMouseListeners::focusLost: " + e);
         this.MouseFocusLost();
      }

      protected override bool CheckNeedRestoreLastGame() {
         return false;
      }

      private void SubscribeToViewControl() {
         Canvas control = this.View.GetControl();
         control.Tapped += OnTapped;
         control.DoubleTapped += OnDoubleTapped;
         control.RightTapped += OnRightTapped;
         control.PointerPressed += OnPointerPressed;
         control.PointerReleased += OnPointerReleased;
         control.PointerCaptureLost += OnPointerCaptureLost;
         control.LostFocus += OnFocusLost;
      }

      private void UnsubscribeToViewControl() {
         Canvas control = this.View.GetControl();
         control.Tapped += OnTapped;
         control.DoubleTapped -= OnDoubleTapped;
         control.RightTapped -= OnRightTapped;
         control.PointerPressed -= OnPointerPressed;
         control.PointerReleased -= OnPointerReleased;
         control.PointerCaptureLost -= OnPointerCaptureLost;
         control.LostFocus -= OnFocusLost;
      }

      protected override void Disposing() {
         UnsubscribeToViewControl();
         View.Dispose();
         base.Disposing();
      }

      private PointDouble ToCanvasPoint(Windows.Foundation.Point pagePoint) {
         Canvas control = this.View.GetControl();
         var point = pagePoint.ToFmPointDouble(); // control.TransformToVisual(control).TransformPoint(pagePoint).ToFmPointDouble();
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

      ////////////// TEST //////////////
      public static MosaicCanvasController GetTestData() {
         MosaicView<Canvas, WriteableBitmap, MosaicDrawModel<WriteableBitmap>>._DEBUG_DRAW_FLOW = true;
         MosaicCanvasController ctrllr = new MosaicCanvasController();

         if (ThreadLocalRandom.Current.Next(2) == 1) {
            // unmodified controller test
         } else {
             EMosaic mosaicType = EMosaic.eMosaicTrSq1;
             ESkillLevel skill  = ESkillLevel.eBeginner;

             ctrllr.Area = 500;
             ctrllr.MosaicType = mosaicType;
             ctrllr.SizeField = skill.GetDefaultSize();
             ctrllr.MinesCount = skill.GetNumberMines(mosaicType);
             ctrllr.GameNew();
         }
         return ctrllr;
      }
      //////////////////////////////////

   }

}
