using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic;
using Fmg.Core.Mosaic.Cells;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.Mosaic.Win2d {

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

                System.Diagnostics.Debug.Assert(!_alreadyPainted);

                if (modifiedCells == null) {
                    tracer.Put("Invalidate all");
                    canvasVirtualControl.Invalidate(); // redraw all of mosaic
                    return;
                }

                var model = Model;
                var offset = model.MosaicOffset;
                tracer.Put($"offset={offset}");
                var rcAll = new RectDouble(model.Size);
                if (!_accumulateInvalidate) {
                    foreach (var cell in modifiedCells) {
                        var rcClip = cell.GetRcOuter().MoveXY(offset).GetIntersection(rcAll);
                        tracer.Put($"canvasVirtualControl.Invalidate(rcClip={rcClip})");
                        if (rcClip.HasValue)
                            canvasVirtualControl.Invalidate(rcClip.Value.ToWinRect());
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
                    var rcClip = new RectDouble(minX, minY, maxX - minX, maxY - minY).MoveXY(offset).GetIntersection(rcAll);
                    tracer.Put($"canvasVirtualControl.Invalidate(rcClip={rcClip})");
                    if (rcClip.HasValue)
                        canvasVirtualControl.Invalidate(rcClip.Value.ToWinRect());
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
            Logger.Info(GetCallerName() + ": ev.PropertyName=" + ev.PropertyName);
            base.OnPropertyChanged(sender, ev);
            if (ev.PropertyName == nameof(Image)) {
                var _ = this.Image; // implicit call this.DrawModified
            }
        }

        protected override void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnModelPropertyChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(MosaicDrawModel<CanvasBitmap>.BackgroundColor):
                if (_useClearColor)
                    Control.ClearColor = Model.BackgroundColor.ToWinColor();
                break;
            }
        }

        private string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) {
            return callerName;
        }
        private Tracer CreateTracer([System.Runtime.CompilerServices.CallerMemberName] string callerName = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            var typeName = GetType().Name;
            var thisName = nameof(MosaicCanvasVirtualControlView);
            if (typeName != thisName)
                typeName += "(" + thisName + ")";
            return new Tracer(typeName + "." + callerName, ctorMessage, disposeMessage);
        }

    }

}
