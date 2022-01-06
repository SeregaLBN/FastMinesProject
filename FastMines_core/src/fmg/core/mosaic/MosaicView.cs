using System.Linq;
using System.Collections.Generic;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Core.Mosaic.Cells;

namespace Fmg.Core.Mosaic {

#if DEBUG
    public sealed class MosaicViewCfg {
        private MosaicViewCfg() { }

        public static bool DEBUG_DRAW_FLOW = false;
    }
#endif

    /// <summary> MVC: view. Base mosaic view implementation </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
    public abstract class MosaicView<TImage, TImageInner, TMosaicModel>
                         : ImageView<TImage, TMosaicModel>,
                         IMosaicView<TImage, TImageInner, TMosaicModel>
          where TImage : class
          where TImageInner : class
          where TMosaicModel : MosaicDrawModel<TImageInner>
    {
        protected MosaicView(TMosaicModel mosaicModel)
            : base(mosaicModel)
        { }

        private readonly HashSet<BaseCell> _modifiedCells = new HashSet<BaseCell>();

        protected ICollection<BaseCell> ToDrawCells(RectDouble? invalidatedRect) {
#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info("> MosaicView.ToDrawCells: invalidatedRect=" + (invalidatedRect==null ? "null" : invalidatedRect.ToString()));
#endif

            if (invalidatedRect == null)
                return null; // equals Model.Matrix

            // check to redraw all mosaic cells
            TMosaicModel model = Model;
            var rc = invalidatedRect.Value;
            if (rc.X.HasMinDiff(0) && rc.Y.HasMinDiff(0)) {
                var size = model.Size;
                if (rc.Width.HasMinDiff(size.Width) && rc.Height.HasMinDiff(size.Height))
                    return null; // equals Model.Matrix
            }

            var offset = model.MosaicOffset;

            // redraw only when needed...
            var toDrawCells = model.Matrix
                .Where(cell => cell.GetRcOuter()
                                   .MoveXY(offset.Width, offset.Height)
                                   .Intersection(rc)) // ...when the cells and update region intersect
                .ToList();

#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info("< MosaicView.ToDrawCells: cnt=" + toDrawCells.Count);
#endif
            return toDrawCells;
        }

        public virtual void Invalidate(ICollection<BaseCell> modifiedCells) {
            if (modifiedCells == null) // mark NULL if all mosaic is changed
                _modifiedCells.Clear();
            else
                _modifiedCells.UnionWith(modifiedCells);
#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info("MosaicView.Invalidate: " + ((modifiedCells == null) ? "all" : ("cnt=" + modifiedCells.Count) + ": " + modifiedCells.Take(5).ToList()));
#endif
            Invalidate();
        }

        /// <summary>Draw modified mosaic cells</summary>
        /// <param name="modifiedCells">Cells to be redrawn. NULL - redraw the full mosaic.</param>
        protected abstract void DrawModified(ICollection<BaseCell> modifiedCells);

        /// <summary> repaint all </summary>
        protected override void DrawBody() {
#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info("MosaicView.DrawBody: " + (!_modifiedCells.Any() ? "all" : ("cnt=" + _modifiedCells.Count) + ": " + _modifiedCells.Take(5).ToList()));
#endif
            DrawModified(!_modifiedCells.Any() ? null : _modifiedCells);
            _modifiedCells.Clear();
        }

    }

}
