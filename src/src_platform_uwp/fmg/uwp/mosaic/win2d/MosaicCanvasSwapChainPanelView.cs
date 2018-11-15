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
                ResetBuffers();
                break;
            }
        }

        //public SizeDouble Offset { get; set; }
        //public Func<SizeDouble> GetterMosaicWindowSize { get; set; }
        //public void OnMosaicWindowSizeChanged() {
        //    ResetBuffers();
        //}

        private CanvasSwapChain SwapChain {
            get {
                System.Diagnostics.Debug.Assert(_control != null);
                System.Diagnostics.Debug.Assert(!Disposed);
                var cw = Control.Width;
                var ch = Control.Height;
                if (double.IsNaN(cw) || double.IsNaN(ch)) {
                    var s = Model.Size;
                    cw = s.Width;
                    ch = s.Height;
                }
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
                var bb = BackBuffer;
                if (bb == null) {
                    modifiedCells = null; // force redraw all
                    tr.Put("BackBuffer is null: force set modifiedCells as null");
                }
                bool needRedrawAll = (modifiedCells == null);
                using (var ds = ab.CreateDrawingSession()) {
                    if (bb != null) {
                        ds.DrawImage(bb);
                        tr.Put("{0} BackBuffer != null: ds.DrawImage(BackBuffer): modifiedCells={1}", _bufferIndex, modifiedCells == null ? "null" : modifiedCells.Count().ToString());
                    }

                    bool drawBk = needRedrawAll;
                    DrawWin2D(ds, modifiedCells, drawBk);
                }
                RepaintOffsetInternal(ab);
            }
        }

        /** /
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
        /**/

        private void RepaintOffsetInternal(CanvasRenderTarget actualBuffer) {
            var sc = SwapChain;
            //var o = Offset;
            using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
                //ds.DrawImage(actualBuffer, (float)o.Width, (float)o.Height);
                ds.DrawImage(actualBuffer);
            }
            //AsyncRunner.InvokeFromUiLater(() => { // TODO: Removes blink artifacts when zooming.  Need remove....
            sc.Present();
            //}, Windows.UI.Core.CoreDispatcherPriority.High);
        }

        private void ResetBuffers() {
            _doubleBuffer[0]?.Dispose();
            _doubleBuffer[1]?.Dispose();
            _doubleBuffer[0] = _doubleBuffer[1] = null;
        }

        protected override void Disposing() {
            SwapChain = null; // call setter
            ResetBuffers();
            base.Disposing();
        }

    }

}
