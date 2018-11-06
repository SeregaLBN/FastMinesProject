using Microsoft.Graphics.Canvas;
using fmg.core.mosaic.draw;

namespace fmg.uwp.draw.mosaic.win2d {

    /// <summary> Container for <see cref="CanvasDrawingSession"/> </summary>
    public class PaintableWin2D : IPaintable {

        public PaintableWin2D(CanvasDrawingSession ds) { DrawingSession = ds; }

        public CanvasDrawingSession DrawingSession { get; private set; }

    }

}
