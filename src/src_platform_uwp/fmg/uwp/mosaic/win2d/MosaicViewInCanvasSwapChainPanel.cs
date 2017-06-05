using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using MosaicControllerWin2D = fmg.uwp.mosaic.win2d.MosaicControllerWin2D<fmg.uwp.mosaic.win2d.MosaicViewInCanvasSwapChainPanel>;

namespace fmg.uwp.mosaic.win2d {

   /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasSwapChainPanel"/> */
   public class MosaicViewInCanvasSwapChainPanel : AMosaicViewInControl<CanvasSwapChainPanel> {

      private CanvasDevice _device;
      private CanvasRenderTarget[] _doubleBuffer = new CanvasRenderTarget[2];
      private int _bufferIndex = 0;
      private CanvasSwapChain _swapChain;
      public SizeDouble Offset { get; set; }
      public MosaicControllerWin2D Controller { get; set; } // TODO exclude this... Controller contains View, but not vice versa.

      protected override CanvasDevice GetCanvasDevice() {
         return Device;
      }

      private CanvasDevice Device {
         get {
            if (_device == null)
               Device = new CanvasDevice();
            return _device;
         }
         set {
            if (_device != null)
               _device.Dispose();
            _device = value;
         }
      }

      private CanvasSwapChain SwapChain {
         get {
            System.Diagnostics.Debug.Assert(_control != null);
            System.Diagnostics.Debug.Assert(!Disposed);
            var cw = Control.Width;
            var ch = Control.Height;
            if (_swapChain == null) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               SwapChain = new CanvasSwapChain(Device, (float)cw, (float)ch, dpi);
            } else {
               var size = _swapChain.Size;
               if (!size.Width.HasMinDiff(cw) || !size.Height.HasMinDiff(ch)) {
                  var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                  _swapChain.ResizeBuffers((float)cw, (float)ch, dpi);
               }
            }
            return _swapChain;
         }
         set {
            if (_swapChain != null)
               _swapChain.Dispose();
            Control.SwapChain = _swapChain = value;
         }
      }

      private void SwapDrawBuffer() {
         _bufferIndex = 1 - _bufferIndex;
      }
      private CanvasRenderTarget  FrontBuffer { get { return _doubleBuffer[_bufferIndex]; } }
      private CanvasRenderTarget   BackBuffer { get { return _doubleBuffer[1 - _bufferIndex]; } }
      public CanvasRenderTarget ActualBuffer {
         get {
            System.Diagnostics.Debug.Assert(_control != null);
            System.Diagnostics.Debug.Assert(!Disposed);

            var mosaicWinSize = Controller.WindowSize;
            //Func<Windows.Foundation.Size, bool> hasMinDiff = size => mosaicWinSize.Width.HasMinDiff(size.Width, 4) && mosaicWinSize.Height.HasMinDiff(size.Height, 4);
            Func<Windows.Foundation.Size, bool> hasMinDiff = size => (Math.Abs(mosaicWinSize.Width  - size.Width ) < 0.5) &&
                                                                     (Math.Abs(mosaicWinSize.Height - size.Height) < 0.5);
            if ((_doubleBuffer[_bufferIndex] != null) && !hasMinDiff(_doubleBuffer[_bufferIndex].Size)) {
               _doubleBuffer[_bufferIndex].Dispose();
               _doubleBuffer[_bufferIndex] = null;
            }

            if (_doubleBuffer[_bufferIndex] == null) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               ActualBuffer = new CanvasRenderTarget(Device, (float)mosaicWinSize.Width, (float)mosaicWinSize.Height, dpi);
            }
            return FrontBuffer;
         }
         private set {
            if (FrontBuffer != null)
               FrontBuffer.Dispose();
            _doubleBuffer[_bufferIndex] = value;
         }
      }

      public override void Invalidate(IEnumerable<BaseCell> modifiedCells = null) {
         System.Diagnostics.Debug.Assert((modifiedCells == null) || modifiedCells.Any());
         using (var tr = new Tracer()) {
            var canvasSwapChainPanel = Control;
            //if (canvasSwapChainPanel == null)
            //   return;
            if (double.IsNaN(canvasSwapChainPanel.Width) || double.IsNaN(canvasSwapChainPanel.Height))
               return;
            //if ((canvasVirtualControl.Size.Width == 0) || (canvasVirtualControl.Size.Height == 0))
            //   return;

            AsyncRunner.InvokeFromUiLater(() => {
               Repaint(modifiedCells);
            }, Windows.UI.Core.CoreDispatcherPriority.High);
         }
      }

      public void Repaint(IEnumerable<BaseCell> modifiedCells) {
         SwapDrawBuffer();
         var ab = ActualBuffer;
         using (var ds = ab.CreateDrawingSession()) {

            bool needRedrawAll = (modifiedCells == null);
            if (!needRedrawAll) {
               if (BackBuffer == null) {
                  ds.Clear(Colors.Transparent);
                  LoggerSimple.Put("{0} BackBuffer is null: ds.Clear(Colors.Transparent): modifiedCells={1}", _bufferIndex, modifiedCells == null ? "null" : modifiedCells.Count().ToString());
               } else {
                  ds.DrawImage(BackBuffer);
                  LoggerSimple.Put("{0} BackBuffer != null: ds.DrawImage(BackBuffer): modifiedCells={1}", _bufferIndex, modifiedCells == null ? "null" : modifiedCells.Count().ToString());
               }
            }

            Paintable = ds;
            PaintContext.IsUseBackgroundColor = needRedrawAll;
            Repaint(modifiedCells, null);
            Paintable = null;
         }
         RepaintOffsetInternal(ab);
      }

      public void RepaintOffset() {
         var canvasSwapChainPanel = Control;
         //if (canvasSwapChainPanel == null)
         //   return;
         if (double.IsNaN(canvasSwapChainPanel.Width) || double.IsNaN(canvasSwapChainPanel.Height))
            return;

         RepaintOffsetInternal(ActualBuffer);
      }

      private void RepaintOffsetInternal(CanvasRenderTarget actualBuffer) {
         var sc = SwapChain;
         using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
            ds.DrawImage(actualBuffer, (float)Offset.Width, (float)Offset.Height);
         }
         //AsyncRunner.InvokeFromUiLater(() => { // TODO: Removes blink artifacts when zooming.  Need remove....
         sc.Present();
         //}, Windows.UI.Core.CoreDispatcherPriority.High);
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         if (disposing) {
            SwapChain = null;
         }

         base.Dispose(disposing);

         if (disposing) {
            ActualBuffer = null;
            SwapDrawBuffer();
            ActualBuffer = null;
            Device = null;
         }
      }

   }

}
