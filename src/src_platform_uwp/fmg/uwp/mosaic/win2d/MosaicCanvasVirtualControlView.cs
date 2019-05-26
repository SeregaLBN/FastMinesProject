using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

    /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasVirtualControl"/> */
    public class MosaicCanvasVirtualControlView : MosaicFrameworkElementView<CanvasVirtualControl> {

        private readonly bool _useClearColor = !true; // TODO при true проявляются артефакты в рисовании ;-\
        private readonly bool _accumulateInvalidate = true;

        public MosaicCanvasVirtualControlView(ICanvasResourceCreator resourceCreator, CanvasVirtualControl control = null)
            : base(resourceCreator, control)
        { }

        public override CanvasVirtualControl Control {
            get {
                if (base.Control == null)
                    this.Control = new CanvasVirtualControl(); // call this setter
                return base.Control;
            }
            protected set {
                var old = base.Control;
                if (old != null)
                    old.RegionsInvalidated -= OnRegionsInvalidated;

                base.Control = value;
                if (value != null) {
                    value.RegionsInvalidated += OnRegionsInvalidated;
                    if (_useClearColor)
                        value.ClearColor = Model.BackgroundColor.ToWinColor();
                }
            }
        }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            // TIP: explicit call canvasVirtualControl.Invalidate()  =>  Implicit call this.OnRegionsInvalidated

            System.Diagnostics.Debug.Assert((modifiedCells == null) || modifiedCells.Any());
            using (var tracer = CreateTracer(nameof(DrawModified), "modifiedCells=" + (modifiedCells==null ? "null" : "size_"+ modifiedCells.Count))) {
                var canvasVirtualControl = Control;
                if (canvasVirtualControl == null) {
                    tracer.Put("Can`t draw: canvasVirtualControl is null");
                    return;
                }

                SizeDouble size;
                if (double.IsNaN(canvasVirtualControl.Width) || double.IsNaN(canvasVirtualControl.Height)) {
                    tracer.Put($"canvasVirtualControl.Width/Height is NaN; size={canvasVirtualControl.Size}, DesiredSize={canvasVirtualControl.DesiredSize}, RenderSize={canvasVirtualControl.RenderSize}");
                    size = canvasVirtualControl.Size.ToFmSizeDouble();
                } else {
                    size = new SizeDouble(canvasVirtualControl.Width, canvasVirtualControl.Height); // double values
                }
                //if ((canvasVirtualControl.Size.Width == 0) || (canvasVirtualControl.Size.Height == 0))
                //   return;

                System.Diagnostics.Debug.Assert(!_alreadyPainted);

                if (modifiedCells == null) {
                    tracer.Put("Invalidate all");
                    canvasVirtualControl.Invalidate(); // redraw all of mosaic
                    return;
                }

#if DEBUG
                var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif

                var model = Model;
                var offset = model.MosaicOffset;
                tracer.Put($"offset={offset}");
                if (!_accumulateInvalidate) {
                    foreach (var cell in modifiedCells) {
                        var rc = cell.GetRcOuter();
                        rc.X += offset.Width;
                        rc.Y += offset.Height;
#if DEBUG
                        var containsLT = tmp.Contains(rc.PointLT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Top()));
                        var containsLB = tmp.Contains(rc.PointLB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Left()) && tmp.Top.HasMinDiff(rc.Bottom()));
                        var containsRT = tmp.Contains(rc.PointRT().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Top()));
                        var containsRB = tmp.Contains(rc.PointRB().ToWinPoint()) || (tmp.Left.HasMinDiff(rc.Right()) && tmp.Top.HasMinDiff(rc.Bottom()));
                        bool intersect = (tmp != Windows.Foundation.Rect.Empty);
                        //LoggerSimple.Put($"intersect={intersect}; containsLT={containsLT}; containsLB={containsLB}; containsRT={containsRT}; containsRB={containsRB}");
                        System.Diagnostics.Debug.Assert(intersect && containsLT && containsRT && containsLB && containsRB);
                        if (!(intersect && containsLT && containsRT && containsLB && containsRB))
                            return;
#endif
                        tracer.Put($"canvasVirtualControl.Invalidate(rc={rc})");
                        canvasVirtualControl.Invalidate(rc.ToWinRect());
                    }
                } else {
                    double minX = 0, minY = 0, maxX = 0, maxY = 0;
                    bool first = true;
                    foreach (BaseCell cell in modifiedCells) {
                        RectDouble rc = cell.GetRcOuter();
                        if (first) {
                            first = false;
                            minX = rc.X;
                            minY = rc.Y;
                            maxX = rc.Right();
                            maxY = rc.Bottom();
                        } else {
                            minX = Math.Min(minX, rc.X);
                            minY = Math.Min(minY, rc.Y);
                            maxX = Math.Max(maxX, rc.Right());
                            maxY = Math.Max(maxY, rc.Bottom());
                        }
                    }
                    var rcClip = new Windows.Foundation.Rect(minX + offset.Width, minY + offset.Height, maxX - minX, maxY - minY);
                    tracer.Put($"canvasVirtualControl.Invalidate(rcClip={rcClip})");
                    canvasVirtualControl.Invalidate(rcClip);
                }
            }
        }

        bool _alreadyPainted2 = false;
        internal void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
            using (CreateTracer()) {
                System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Control));
                System.Diagnostics.Debug.Assert(!_alreadyPainted2);

                _alreadyPainted2 = true;
                foreach (var region in ev.InvalidatedRegions) {
                    using (var ds = sender.CreateDrawingSession(region)) {
                        bool drawBk = !_useClearColor;
                        DrawWin2D(ds, ToDrawCells(region.ToFmRectDouble()), drawBk);
                    }
                }
                _alreadyPainted2 = false;
            }
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            LoggerSimple.Put(GetFullCallerName() + ": ev.PropertyName=" + ev.PropertyName);
            base.OnPropertyChanged(sender, ev);
            if (ev.PropertyName == nameof(Image)) {
                var _ = this.Image; // implicit call this.DrawModified
            }
        }

        protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyModelChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(MosaicDrawModel<CanvasBitmap>.BackgroundColor):
                if (_useClearColor)
                    Control.ClearColor = Model.BackgroundColor.ToWinColor();
                break;
            }
        }

        private string GetFullCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicCanvasVirtualControlView);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return typeName + "." + callerName;
        }
        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null) {
            return new Tracer(GetFullCallerName(callerName), ctorMessage);
        }

    }

}
