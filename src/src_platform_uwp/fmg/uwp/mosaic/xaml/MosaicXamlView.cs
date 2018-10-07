using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Shapes;
using fmg.common;
using fmg.common.geom;
using fmg.common.Converters;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.xaml {

    /// <summary> MVC: view. UWP Xaml shapes implementation </summary>
    public class MosaicXamlView : MosaicView<Panel, ImageSource, MosaicDrawModel<ImageSource>> {

        private Panel _control;
        class CellShapes {
            public Polygon   Poly { get; set; }
            public TextBlock Txt  { get; set; }
            public Image     Img  { get; set; }
        }
        private IDictionary<BaseCell, CellShapes> _xamlBinder;
        private IDictionary<BaseCell, CellShapes> XamlBinder => _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, CellShapes>());
        private readonly IDictionary<Color, Brush> _brushCacheMap = new Dictionary<Color, Brush>();

        public MosaicXamlView()
            : base(new MosaicDrawModel<ImageSource>())
        {
            _notifier.DeferredNotifications = true;
            ChangeSizeImagesMineFlag();
        }

        static MosaicXamlView() {
            StaticInitializer.Init();
        }

        protected override Panel CreateImage() {
            // will return once created window
            return GetControl();
        }

        public Panel GetControl() {
            if (_control == null) {
                _control = new Canvas();
                //LoggerSimple.Put("MosaicXamlView.GetControl: new Control");
                _control.SetBinding(FrameworkElement.WidthProperty, new Binding {
                    Source = this,
                    Path = new PropertyPath(nameof(this.Size)),
                    Mode = BindingMode.OneWay,
                    Converter = new SizeToWidthConverter()
                });
                _control.SetBinding(FrameworkElement.HeightProperty, new Binding {
                    Source = this,
                    Path = new PropertyPath(nameof(this.Size)),
                    Mode = BindingMode.OneWay,
                    Converter = new SizeToHeightConverter()
                });
                BindXamlToMosaic();
            }
            return _control;
        }

        private void UnbindXaml() {
            GetControl().Children.Clear();
            XamlBinder.Clear();
        }

        private void BindXamlToMosaic() {
            var container = GetControl();

            //UnbindXaml();
            var xamlBinder = XamlBinder;
            foreach (var cell in Model.Matrix) {
                var shape = new Polygon();
                var txt = new TextBlock();
                var img = new Image();
                xamlBinder.Add(cell, new CellShapes { Poly = shape, Txt = txt, Img = img });
                container.Children.Add(shape);
                container.Children.Add(txt);
                container.Children.Add(img);
            }
        }

        /// <summary> find cached solid brush. if not exist - create it </summary>
        protected Brush FindBrush(Color clr) {
            if (!_brushCacheMap.ContainsKey(clr))
                _brushCacheMap.Add(clr, new SolidColorBrush(clr.ToWinColor()));
            return _brushCacheMap[clr];
        }

        protected override void DrawModified(ICollection<BaseCell> requiredCells) {
            var container = GetControl();

            //System.Diagnostics.Debug.Assert(container != null);
            if (container == null)
                return;

            DrawOverXaml(requiredCells, true);
        }

        private void DrawOverXaml(IEnumerable<BaseCell> modifiedCells, bool drawBk) {
            var m = Model;
            var container = GetControl();

            // 1. background color
            if (drawBk) { // paint background
                //LoggerSimple.Put("MosaicXamlView.DrawOverXaml: drawBk=true");
                var bkb = container.Background as SolidColorBrush;
                var bkc = m.BackgroundColor.ToWinColor();
                if ((bkb == null) || (bkb.Color != bkc))
                    container.Background = new SolidColorBrush(bkc);
            }

            if ((modifiedCells == null) || !modifiedCells.Any())
                modifiedCells = m.Matrix;

            var pen = m.PenBorder;
            var padding = m.Padding;
            var margin = m.Margin;
            var offset = new SizeDouble(margin.Left + padding.Left,
                                        margin.Top + padding.Top);
            var isIconicMode = pen.ColorLight == pen.ColorShadow;

            // 2. paint all cells
            var xamlBinder = XamlBinder;
            foreach (var cell in modifiedCells) {
               var binder = xamlBinder[cell];
               var txt = binder.Txt;
               var image = binder.Img;
               var poly = binder.Poly;
               var rcInner = cell.GetRcInner(pen.Width).MoveXY(offset.Width, offset.Height);

               { // 2.1. paint component
                    { // 2.1.1. paint cell background
                        Color clr;
                        if (isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
                            clr = m.BackgroundColor;
                        else
                            clr = cell.GetBackgroundFillColor(
                                    m.BkFill.Mode,
                                    m.BackgroundColor,
                                    m.BkFill.GetColor);
                        binder.Poly.Fill = FindBrush(clr);
                    }

                    // output Pictures
                    void paintImage(ImageSource img) {
                        image.Source = img;
                        image.Stretch = Stretch.UniformToFill;
                        image.Width = rcInner.Width;
                        image.Height = rcInner.Height;
                        Canvas.SetLeft(image, rcInner.Left());
                        Canvas.SetTop(image, rcInner.Top());
                        Canvas.SetZIndex(image, 5);
                        image.Visibility = Visibility.Visible;
                        txt.Visibility = Visibility.Collapsed;
                    }

                    if ((m.ImgFlag != null) &&
                        (cell.State.Status == EState._Close) &&
                        (cell.State.Close == EClose._Flag)) {
                        paintImage(m.ImgFlag);
                    } else if ((m.ImgMine != null) &&
                               (cell.State.Status == EState._Open) &&
                               (cell.State.Open   == EOpen._Mine)) {
                        paintImage(m.ImgMine);
                    } else
                    // output text
                    {
                        image.Visibility = Visibility.Collapsed;
                        string szCaption;
                        Color txtColor;
                        if (cell.State.Status == EState._Close) {
                            txtColor = m.ColorText.GetColorClose((int)cell.State.Close.Ordinal());
                            szCaption = cell.State.Close.ToCaption();
                            //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                            //szCaption = ""+cell.getDirection(); // debug
                        } else {
                            txtColor = m.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
                            szCaption = cell.State.Open.ToCaption();
                        }
                        if (string.IsNullOrWhiteSpace(szCaption)) {
                            txt.Visibility = Visibility.Collapsed;
                        } else {
                            txt.Visibility = Visibility.Visible;

                            if (cell.State.Down)
                                rcInner.MoveXY(pen.Width, pen.Width);
                            txt.Text = szCaption;
                            txt.TextAlignment = TextAlignment.Center;
                            txt.FontFamily = new FontFamily(m.FontInfo.Name);
                            txt.FontStyle = FontStyle.Normal;
                            txt.FontWeight = m.FontInfo.Bold ? FontWeights.SemiBold : FontWeights.Normal;
                            txt.FontSize = m.FontInfo.Size;
                            txt.Foreground = FindBrush(txtColor);
                            Canvas.SetLeft(txt, rcInner.Left());
                            Canvas.SetTop(txt, rcInner.Top());
                            txt.Width = rcInner.Width;
                            txt.Height = rcInner.Height;
                            Canvas.SetZIndex(txt, 4);
                        }
                    }
                }

                // 2.2. paint border
                {
                    // draw border lines
                    // TODO set pen width
                    //... = paintContext.PenBorder.Width;

                    // draw border lines
#if true
                    if (poly.Points == null)
                        poly.Points = new PointCollection();
                    { //  check vertex
                        var cnt = cell.GetRegion().CountPoints;
                        var d = (poly.Points.Count != cnt);
                        if (d)
                            poly.Points.Clear();
                        for (var p = 0; p < cnt; p++) {
                            var point = cell.GetRegion().GetPoint(p);
                            point.Move(offset.Width, offset.Height);
                            if (d)
                                poly.Points.Add(point.ToWinPoint());
                            else
                                poly.Points[p] = point.ToWinPoint();
                        }
                    }
#else
                    var points = new PointCollection();
                    { //  check vertex
                        var region = cell.getRegion();
                        for (var p = 0; p < region.CountPoints; p++) {
                            var point = region.getPoint(p);
                            point.Move(paintContext.Bound);
                            points.Add(point.ToWinPoint());
                        }
                    }
                    poly.Points = points;
#endif
                    var open = (cell.State.Status == EState._Open);
                    var down = cell.State.Down || open;
                    poly.StrokeThickness = open ? (pen.Width * 2) : pen.Width;
                    poly.Stroke = FindBrush(down ? pen.ColorLight : pen.ColorShadow);
                    Canvas.SetZIndex(poly, open ? 1 : down ? 3 : 2);
                    // TODO граница региона должна быть двухцветной...

                    // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
                    //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
                    //bmp.DrawRectangle(rcInner.x, rcInner.y, rcInner.right(), rcInner.bottom(), (Windows.UI.Color)Color.MAGENTA);
                }
            }
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            //LoggerSimple.Put("MosaicXamlView.OnPropertyChanged: ev.PropertyName=" + ev.PropertyName);
            base.OnPropertyChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(this.Image):
                var img = Image; // implicit call Draw() -> DrawBegin() -> DrawModified() -> DrawOverXaml()
                break;
            //case nameof(this.Size):
            //    var s = Model.Size;
            //    var control = GetControl();
            //    control.Width  = s.Width;
            //    control.Height = s.Height;
            //    break;
            }
        }

        protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, Model));
            switch (ev.PropertyName) {
            case nameof(Model.MosaicType):
            case nameof(Model.Matrix):
                UnbindXaml();
                BindXamlToMosaic();
                break;
            }
            base.OnPropertyModelChanged(sender, ev);
        }

        /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
        protected void ChangeSizeImagesMineFlag() {
            // none
        }

        protected override void Disposing() {
            UnbindXaml();
            base.Dispose();
        }

    }

}
