using System.ComponentModel;
using Windows.UI.Text;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Text;
using Microsoft.Graphics.Canvas.Geometry;

namespace fmg.uwp.draw.mosaic.win2d {

    /// <summary>Information required for drawing the entire mosaic and cells.
    /// UWP Win2D impl
    /// </summary>
    public class PaintWin2DContext : PaintUwpContext<CanvasBitmap> {

        private CanvasTextFormat _font;
        private CanvasStrokeStyle _cssBorderLine;

        public CanvasTextFormat Font {
            get {
                if (ReferenceEquals(_font, null)) {
                    var ctf = new CanvasTextFormat() {
                        FontSize = FontInfo.Size,
                        FontFamily = FontInfo.Name,
                        FontStyle = FontStyle.Normal,
                        FontWeight = FontInfo.Bold ? FontWeights.Bold : FontWeights.Normal,
                        HorizontalAlignment = CanvasHorizontalAlignment.Center,
                        VerticalAlignment = CanvasVerticalAlignment.Center,
                    };
                    Font = ctf; // call setter
                }
                return _font;
            }
            set {
                var old = _font;
                if (SetProperty(ref _font, value))
                    old?.Dispose();
            }
        }

        public CanvasStrokeStyle CssBorderLine {
            get {
                if (ReferenceEquals(_cssBorderLine, null)) {
                    var css = new CanvasStrokeStyle() {
                        StartCap = CanvasCapStyle.Triangle,
                        EndCap = CanvasCapStyle.Triangle,
                    };
                    CssBorderLine = css; // call setter
                }
                return _cssBorderLine;
            }
            set {
                var old = _cssBorderLine;
                if (SetProperty(ref _cssBorderLine, value))
                    old?.Dispose();
            }
        }

        protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(ev);
            switch (ev.PropertyName) {
            case nameof(FontInfo):
                Font = null;
                break;
            }
        }

        protected override void Dispose(bool disposing) {
            if (Disposed)
                return;

            base.Dispose(disposing);

            if (disposing) {
                Font = null;
                CssBorderLine = null;
            }
        }

    }

}
