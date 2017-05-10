using System.Linq;
using System.Collections.Generic;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

   /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasSwapChainPanel"/> */
   public class MosaicViewInCanvasSwapChainPanel : AMosaicViewInControl<CanvasSwapChainPanel> {

      private CanvasRenderTarget[] _doubleBuffer = new CanvasRenderTarget[2];
      private int _bufferIndex = 0;
      private CanvasSwapChain _swapChain;
      private long _tokenPropWidth, _tokenPropHeight;
      private bool _needResizeSwapChain, _needResizeFirstBuffer, _needResizeSecondBuffer;

      public override CanvasSwapChainPanel Control {
         //get { return _control; }
         set {
            if (_control != null) {
               if (_tokenPropWidth != 0)
                  _control.UnregisterPropertyChangedCallback(CanvasSwapChainPanel.WidthProperty, _tokenPropWidth);
               if (_tokenPropHeight != 0)
                  _control.UnregisterPropertyChangedCallback(CanvasSwapChainPanel.HeightProperty, _tokenPropHeight);
               _tokenPropWidth = _tokenPropHeight = 0;
            }
            _control = value;
            if (_control != null) {
               _tokenPropWidth = _control.RegisterPropertyChangedCallback(CanvasSwapChainPanel.WidthProperty, OnControlPropertyChanged);
               _tokenPropHeight = _control.RegisterPropertyChangedCallback(CanvasSwapChainPanel.HeightProperty, OnControlPropertyChanged);
            }
         }
      }

      private void OnControlPropertyChanged(DependencyObject sender, DependencyProperty dp) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _control));
         if (ReferenceEquals(dp, CanvasSwapChainPanel.WidthProperty) ||
             ReferenceEquals(dp, CanvasSwapChainPanel.HeightProperty))
         {
            _needResizeSwapChain = _needResizeFirstBuffer = _needResizeSecondBuffer = true;
         }
      }

      private CanvasSwapChain SwapChain {
         get {
            System.Diagnostics.Debug.Assert(_control != null);
            System.Diagnostics.Debug.Assert(!Disposed);
            if (_swapChain == null) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               SwapChain = new CanvasSwapChain(Device, (float)Control.Width, (float)Control.Height, dpi);
               _needResizeSwapChain = false;
            }
            if (_needResizeSwapChain) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               _swapChain.ResizeBuffers((float)Control.Width, (float)Control.Height, dpi);
               _needResizeSwapChain = false;
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
      private CanvasRenderTarget ActualBuffer {
         get {
            System.Diagnostics.Debug.Assert(_control != null);
            System.Diagnostics.Debug.Assert(!Disposed);

            if (_bufferIndex == 0) {
               if (_needResizeFirstBuffer) {
                  if (_doubleBuffer[0] != null) {
                     _doubleBuffer[0].Dispose();
                     _doubleBuffer[0] = null;
                  }
                  _needResizeFirstBuffer = false;
               }
            } else { // _bufferIndex == 1
               if (_needResizeSecondBuffer) {
                  if (_doubleBuffer[1] != null) {
                     _doubleBuffer[1].Dispose();
                     _doubleBuffer[1] = null;
                  }
                  _needResizeSecondBuffer = false;
               }
            }

            if (_doubleBuffer[_bufferIndex] == null) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               ActualBuffer = new CanvasRenderTarget(Device, (float)Control.Width, (float)Control.Height, dpi);
            }
            return FrontBuffer;
         }
         set {
            if (FrontBuffer != null)
               FrontBuffer.Dispose();
            _doubleBuffer[_bufferIndex] = value;
         }
      }

      public override void Invalidate(IEnumerable<BaseCell> modifiedCells = null) {
         System.Diagnostics.Debug.Assert((modifiedCells == null) || modifiedCells.Any());
         using (new Tracer()) {
            var canvasSwapChainPanel = Control;
            if (canvasSwapChainPanel == null)
               return;
            if (double.IsNaN(canvasSwapChainPanel.Width) || double.IsNaN(canvasSwapChainPanel.Height))
               return;
            //if ((canvasVirtualControl.Size.Width == 0) || (canvasVirtualControl.Size.Height == 0))
            //   return;

            AsyncRunner.InvokeFromUiLater(() => {
               Repaint(modifiedCells);
            });
         }
      }

      //int cnt = 0;
      public void Repaint(IEnumerable<BaseCell> modifiedCells) {
         SwapDrawBuffer();
         using (var ds = ActualBuffer.CreateDrawingSession()) {

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

         var sc = SwapChain;
         using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
            ds.DrawImage(ActualBuffer);
         }
         //{
         //   var canvasSwapChainPanel = Control;
         //   var filename = string.Format("tmp-{0}x{1}-{2}.png",
         //                          (int)canvasSwapChainPanel.Width,
         //                          (int)canvasSwapChainPanel.Height,
         //                          DateTime.Now.ToString("dd-MM-yyyy HH_mm_ss_fff"));
         //   ActualBuffer.SaveAsync(Path.Combine(ApplicationData.Current.LocalFolder.Path, filename), CanvasBitmapFileFormat.Png);
         //}
         sc.Present();
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            ActualBuffer = null;
            SwapDrawBuffer();
            ActualBuffer = null;
            SwapChain = null;
         }
      }

   }

}
