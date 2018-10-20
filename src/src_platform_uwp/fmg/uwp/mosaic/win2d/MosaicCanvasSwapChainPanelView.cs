using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

    /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasSwapChainPanel"/> */
    public class MosaicCanvasSwapChainPanelView : MosaicFrameworkElementView<CanvasSwapChainPanel> {

        private CanvasSwapChain _swapChain;
        private readonly CanvasRenderTarget[] _doubleBuffer = new CanvasRenderTarget[2];
        private int _bufferIndex = 0;
        private bool _needResizeFirstBuffer = true, _needResizeSecondBuffer = true;

        public MosaicCanvasSwapChainPanelView(ICanvasResourceCreator resourceCreator, CanvasSwapChainPanel control = null)
            : base(resourceCreator, control)
        { }

        public override CanvasSwapChainPanel Control {
            get {
                var ctrl = base.Control;
                if (ctrl == null) {
                    ctrl = new CanvasSwapChainPanel();
                    base.Control = ctrl;
                    ctrl.SwapChain = SwapChain;
                }
                return ctrl;
            }
        }

        protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyModelChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(MosaicGameModel.MosaicType):
            case nameof(MosaicGameModel.Area):
            case nameof(MosaicGameModel.SizeField):
                _needResizeFirstBuffer = _needResizeSecondBuffer = true;
                break;
            }
        }

        public SizeDouble Offset { get; set; }
        //public Func<SizeDouble> GetterMosaicWindowSize { get; set; }
        //public void OnMosaicWindowSizeChanged() {
        //    _needResizeFirstBuffer = _needResizeSecondBuffer = true;
        //}

        private CanvasSwapChain SwapChain {
            get {
                System.Diagnostics.Debug.Assert(_control != null);
                System.Diagnostics.Debug.Assert(!Disposed);
                var cw = Control.Width;
                var ch = Control.Height;
                if (_swapChain == null) {
                    var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                    SwapChain = new CanvasSwapChain(_resourceCreator, (float)cw, (float)ch, dpi);
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
        private CanvasRenderTarget ActualBuffer {
            get {
                System.Diagnostics.Debug.Assert(_control != null);
                System.Diagnostics.Debug.Assert(!Disposed);

                var i = _bufferIndex;
                if ((i == 0) ? _needResizeFirstBuffer : _needResizeSecondBuffer) {
                    _doubleBuffer[i]?.Dispose();
                    _doubleBuffer[i] = null;
                    if (i == 0)
                        _needResizeFirstBuffer = false;
                    else
                        _needResizeSecondBuffer = false;
                }

                if (_doubleBuffer[i] == null) {
                    var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                    var size = Model.Size; // GetterMosaicWindowSize();
                    try {
                        _doubleBuffer[i] = new CanvasRenderTarget(_resourceCreator, (float)size.Width, (float)size.Height, dpi);
                    } catch(ArgumentException) {
                        // :(((
                        try {
                            _doubleBuffer[i] = new CanvasRenderTarget(_resourceCreator, (float)size.Width, (float)size.Height, dpi/2);
                        } catch (ArgumentException) {
                            try {
                                _doubleBuffer[i] = new CanvasRenderTarget(_resourceCreator, (float)size.Width, (float)size.Height, dpi / 4);
                            } catch (ArgumentException) {
                                _doubleBuffer[i] = new CanvasRenderTarget(_resourceCreator, (float)size.Width, (float)size.Height, dpi / 8);
                            }
                        }
                    }
                }

                return FrontBuffer;
            }
        }

        public override void Invalidate(ICollection<BaseCell> modifiedCells) {
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
                    DrawModified(modifiedCells);
                }, Windows.UI.Core.CoreDispatcherPriority.High);
            }
        }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            using (var tr = new Tracer()) {
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

                    bool drawBk = false;
                    DrawWin2D(ds, modifiedCells, drawBk);
                }
                RepaintOffsetInternal(ab);
            }
        }

        public void RepaintOffset() {
            var canvasSwapChainPanel = Control;
            //if (canvasSwapChainPanel == null)
            //    return;
            if (double.IsNaN(canvasSwapChainPanel.Width) || double.IsNaN(canvasSwapChainPanel.Height))
                return;

            RepaintOffsetInternal(ActualBuffer);
        }

        public void RepaintScaled(RectDouble rcDestination) {
            var sc = SwapChain;
            using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
                ds.DrawImage(ActualBuffer, rcDestination.ToWinRect());
            }
            sc.Present();
        }

        private void RepaintOffsetInternal(CanvasRenderTarget actualBuffer) {
            var sc = SwapChain;
            var o = Offset;
            using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
                ds.DrawImage(actualBuffer, (float)o.Width, (float)o.Height);
            }
            //AsyncRunner.InvokeFromUiLater(() => { // TODO: Removes blink artifacts when zooming.  Need remove....
            sc.Present();
            //}, Windows.UI.Core.CoreDispatcherPriority.High);
        }

        protected override void Disposing() {
            base.Disposing();
            SwapChain = null;
            _doubleBuffer[0]?.Dispose();
            _doubleBuffer[1]?.Dispose();
        }

    }

}
