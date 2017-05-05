using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;
using fmg.uwp.draw.mosaic;
using FlagCanvasBmp = fmg.uwp.draw.img.win2d.Flag.CanvasBmp;
using MineCanvasBmp = fmg.uwp.draw.img.win2d.Mine.CanvasBmp;
using Windows.UI;

namespace fmg.uwp.mosaic.win2d {

   /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasSwapChainPanel"/> */
   public class MosaicViewInCanvasSwapChainPanel : AMosaicViewWin2D {

      private CanvasDevice _device;
      private CanvasRenderTarget[] _drawDblBuffer = new CanvasRenderTarget[2];
      private int _bufferIndex = 0;
      private CanvasSwapChain _swapChain;
      private CanvasSwapChainPanel _control;
      private MineCanvasBmp _mineImage;
      private FlagCanvasBmp _flagImage;
      private long _tokenPropWidth, _tokenPropHeight;
      private bool _needResizeSwapChain, _needResizeFirstBuffer, _needResizeSecondBuffer;

      public CanvasSwapChainPanel Control {
         get { return _control; }
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

      private CanvasRenderTarget FrontBuffer { get { return _drawDblBuffer[_bufferIndex]; } }
      private CanvasRenderTarget  BackBuffer { get { return _drawDblBuffer[1 - _bufferIndex]; } }
      private void SwapDrawBuffer() {
         _bufferIndex = 1 - _bufferIndex;
      }

      private void DisplayBackBufferInFrontBuffer(CanvasDrawingSession dsFrontBuffer) {
         if (BackBuffer == null)
            return;

         dsFrontBuffer.DrawImage(BackBuffer);
      }

      private CanvasRenderTarget RenderImage {
         get {
            System.Diagnostics.Debug.Assert(_control != null);
            System.Diagnostics.Debug.Assert(!Disposed);

            if (_bufferIndex == 0) {
               if (_needResizeFirstBuffer) {
                  if (_drawDblBuffer[0] != null) {
                     _drawDblBuffer[0].Dispose();
                     _drawDblBuffer[0] = null;
                  }
                  _needResizeFirstBuffer = false;
               }
            } else { // _bufferIndex == 1
               if (_needResizeSecondBuffer) {
                  if (_drawDblBuffer[1] != null) {
                     _drawDblBuffer[1].Dispose();
                     _drawDblBuffer[1] = null;
                  }
                  _needResizeSecondBuffer = false;
               }
            }

            if (_drawDblBuffer[_bufferIndex] == null) {
               var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
               RenderImage = new CanvasRenderTarget(Device, (float)Control.Width, (float)Control.Height, dpi);
            }
            return FrontBuffer;
         }
         set {
            if (FrontBuffer != null)
               FrontBuffer.Dispose();
            _drawDblBuffer[_bufferIndex] = value;
         }
      }

      private CanvasDevice Device {
         get {
            //return CanvasDevice.GetSharedDevice();
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

      private MineCanvasBmp MineImg {
         get {
            if (_mineImage == null)
               _mineImage = new MineCanvasBmp(Device);
            return _mineImage;
         }
      }

      private FlagCanvasBmp FlagImg {
         get {
            if (_flagImage == null)
               _flagImage = new FlagCanvasBmp(Device);
            return _flagImage;
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

            SwapDrawBuffer();
            using (var ds = RenderImage.CreateDrawingSession()) {
               ds.Clear(Colors.Transparent);

               DisplayBackBufferInFrontBuffer(ds);

               Paintable = ds;
               Repaint(modifiedCells, null);
               Paintable = null;
            }

            var sc = SwapChain;
            using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
               ds.DrawImage(RenderImage);
            }
            sc.Present();
         }
      }

      /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
      protected override void ChangeSizeImagesMineFlag() {
         // PS: картинки не зависят от размера ячейки...
         PaintUwpContext<CanvasBitmap> pc = PaintContext;
         int sq = (int)Mosaic.CellAttr.GetSq(pc.PenBorder.Width);
         if (sq <= 0) {
            System.Diagnostics.Debug.Assert(false, "Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
            sq = 3; // ат балды...
         }
         //MineImg = null;
         //FlagImg = null;

         if (sq >= 50) { // ignore small sizes
            MineImg.Size = new Size(sq, sq);
         }
         pc.ImgMine = MineImg.Image;
         pc.ImgFlag = FlagImg.Image;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            MineImg.Dispose();
            FlagImg.Dispose();
            _mineImage = null;
            _flagImage = null;

            RenderImage = null;
            SwapDrawBuffer();
            RenderImage = null;
            SwapChain = null;
            Device = null;
            Control = null;
         }
      }

   }

}
