using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI;
using Windows.Graphics.Display;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic.Cells;

namespace Fmg.Uwp.Mosaic.Win2d {

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
                if (ctrl == null)
                    ctrl = Control = new CanvasSwapChainPanel(); // call this setter
                return ctrl;
            }
            protected set {
                var old = base.Control;
                if (old != null) {
                    old.SwapChain = null;
                    old.SizeChanged -= OnControlSizeChanged;
                }
                base.Control = value;
                if (value != null) {
                    value.SwapChain = SwapChain;
                    value.SizeChanged += OnControlSizeChanged;
                }
            }
        }

        private CanvasSwapChain SwapChain {
            get {
                System.Diagnostics.Debug.Assert(Control != null);
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
                        //LoggerSimple.Put("MosaicCanvasSwapChainPanelView.getSwapChain: resized");
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
                System.Diagnostics.Debug.Assert(Control != null);
                System.Diagnostics.Debug.Assert(!Disposed);

                var i = _bufferIndex;
                if (_doubleBuffer[i] == null) {
                    var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                    var size = Model.Size;
                    _doubleBuffer[i] = new CanvasRenderTarget(_resourceCreator, (float)size.Width, (float)size.Height, dpi);
                }

                return FrontBuffer;
            }
        }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            using (var tracer = CreateTracer(nameof(DrawModified), "modifiedCells=" + (modifiedCells == null ? "null" : "size_" + modifiedCells.Count))) {
                SwapDrawBuffer();
                var ab = ActualBuffer;
                var bb = BackBuffer;
                if (bb == null) {
                    modifiedCells = null; // force redraw all
                    tracer.Put("BackBuffer is null: force set modifiedCells as null");
                }
                bool needRedrawAll = (modifiedCells == null);
                var model = Model;
                var rcMosaic = new RectDouble(model.MosaicSize).MoveXY(model.MosaicOffset);

                var rcAll = new RectDouble(model.Size);
                if (rcMosaic.GetIntersection(rcAll) != rcMosaic) {
                    if (modifiedCells == null)
                        modifiedCells = model.Matrix;
                    modifiedCells = modifiedCells.Where(x => x.GetRcOuter().Intersection(rcAll)).ToList();
                }
                using (var ds = ab.CreateDrawingSession()) {
                    if (bb != null) {
                        ds.DrawImage(bb);
                        tracer.Put("{0} BackBuffer != null: ds.DrawImage(BackBuffer): modifiedCells={1}", _bufferIndex, modifiedCells == null ? "null" : modifiedCells.Count().ToString());
                    }

                    bool drawBk = needRedrawAll;
                    DrawWin2D(ds, modifiedCells, drawBk);
                    //LoggerSimple.Put("MosaicCanvasSwapChainPanelView.DrawModified: drawed!");
                }
                PresentImage(ab);
            }
        }

        private void PresentImage(CanvasRenderTarget actualBuffer) {
            var sc = SwapChain;
            //var o = Offset;
            using (var ds = sc.CreateDrawingSession(Colors.Transparent)) {
                //ds.DrawImage(actualBuffer, (float)o.Width, (float)o.Height);
                ds.DrawImage(actualBuffer);
            }
            //AsyncRunner.InvokeFromUiLater(() => { // TODO: Removes blink artifacts when zooming.  Need remove....
            sc.Present();
            //}, Windows.UI.Core.CoreDispatcherPriority.High);
            //LoggerSimple.Put("MosaicCanvasSwapChainPanelView.PresentImage: presented!");
        }

        private void ResetBuffers() {
            _doubleBuffer[0]?.Dispose();
            _doubleBuffer[1]?.Dispose();
            _doubleBuffer[0] = _doubleBuffer[1] = null;
            //LoggerSimple.Put("MosaicCanvasSwapChainPanelView.ResetBuffers: reset!");
        }

        private void OnControlSizeChanged(object sender, Windows.UI.Xaml.SizeChangedEventArgs ev) {
            ResetBuffers();
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            //LoggerSimple.Put(GetCallerName() + ": ev.PropertyName=" + ev.PropertyName);
            base.OnPropertyChanged(sender, ev);
            if (ev.PropertyName == nameof(Image)) {
                var _ = this.Image; // implicit call this.DrawModified
            }
        }

        protected override void Disposing() {
            SwapChain = null; // call setter
            ResetBuffers();
            base.Disposing();
        }

        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicCanvasSwapChainPanelView);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return new Tracer(typeName + "." + callerName, ctorMessage, disposeMessage);
        }

    }

}
