using System;
using System.Linq;
using System.Collections.Generic;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

    /// summary> MVC: view. UWP Win2D implementation. View located into control <see cref="CanvasVirtualControl"/> */
    public class MosaicCanvasVirtualControlView : MosaicFrameworkElementView<CanvasVirtualControl> {

        private bool _isInnerControl;

        public MosaicCanvasVirtualControlView(ICanvasResourceCreator resourceCreator, CanvasVirtualControl control = null)
            : base(resourceCreator, control)
        { }

        public override CanvasVirtualControl Control {
            get {
                var ctrl = base.Control;
                if (ctrl == null) {
                    ctrl = new CanvasVirtualControl();
                    ctrl.RegionsInvalidated += OnRegionsInvalidated;
                    base.Control = ctrl;
                    _isInnerControl = true;
                }
                return ctrl;
            }
            protected set {
                if (_isInnerControl)
                    base.Control.RegionsInvalidated -= OnRegionsInvalidated;
                base.Control = value;
                _isInnerControl = false;
            }
        }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            Invalidate(modifiedCells);
        }

        public override void Invalidate(ICollection<BaseCell> modifiedCells) {
            System.Diagnostics.Debug.Assert((modifiedCells == null) || modifiedCells.Any());
            using (new Tracer()) {
                var canvasVirtualControl = Control;
                if (canvasVirtualControl == null)
                    return;
                if (double.IsNaN(canvasVirtualControl.Width) || double.IsNaN(canvasVirtualControl.Height))
                    return;
                //if ((canvasVirtualControl.Size.Width == 0) || (canvasVirtualControl.Size.Height == 0))
                //   return;

                System.Diagnostics.Debug.Assert(!_alreadyPainted);

                if (modifiedCells == null) {
                    canvasVirtualControl.Invalidate(); // redraw all of mosaic. Implicit call this.OnRegionsInvalidated
                    return;
                }

#if DEBUG
                var size = new SizeDouble(canvasVirtualControl.Width, canvasVirtualControl.Height); // double values
              //var size = canvasVirtualControl.Size;                                               // int values
                var tmp = new Windows.Foundation.Rect(0, 0, size.Width, size.Height);
#endif

                var model = Model;
                var padding = model.Padding;
                var margin = model.Margin;
                var offset = new SizeDouble(margin.Left + padding.Left,
                                            margin.Top  + padding.Top);
                if (false) {
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
                        canvasVirtualControl.Invalidate(rc.ToWinRect()); // Implicit call this.OnRegionsInvalidated
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
                    canvasVirtualControl.Invalidate(rcClip); // Implicit call this.OnRegionsInvalidated
                }
            }
        }

        bool _alreadyPainted2 = false;
        internal void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
            using (new Tracer()) {
                System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Control));
                System.Diagnostics.Debug.Assert(!_alreadyPainted2);

                _alreadyPainted2 = true;
                foreach (var region in ev.InvalidatedRegions) {
                    using (var ds = sender.CreateDrawingSession(region)) {
                        bool drawBk = true;
                        DrawWin2D(ds, ToDrawCells(region.ToFmRectDouble()), drawBk);
                    }
                }
                _alreadyPainted2 = false;
            }
        }

    }

}
