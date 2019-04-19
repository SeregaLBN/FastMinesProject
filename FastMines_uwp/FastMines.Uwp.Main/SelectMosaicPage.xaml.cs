using System;
using System.Linq;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.DataModel.Items;
using FastMines.Uwp.App.Model;
using MosaicsCanvasCtrllr = fmg.uwp.img.win2d.MosaicImg.CanvasBmpController;

namespace fmg {

    /// <summary> Page for selecting the type of mosaic in the current group </summary>
    public sealed partial class SelectMosaicPage : Page {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData InitData => MosaicInitDataExt.SharedData;
        /// <summary> View-Model </summary>
        public MosaicsViewModel ViewModel { get; private set; }
        private SolidColorBrush BorderColorStartBttn;
        private bool _closed;
        private IDisposable _sizeChangedObservable;
        IDictionary<CanvasControl, MosaicsCanvasCtrllr> mapBindingControlToController = new Dictionary<CanvasControl, MosaicsCanvasCtrllr>();
        private static readonly double TileMinSize = Cast.DpToPx(30);
        private static readonly double TileMaxSize = Cast.DpToPx(90);

        public SelectMosaicPage() {
            this.InitializeComponent();
            ViewModel = new MosaicsViewModel();
            ViewModel.MosaicDS.DataSource.CollectionChanged += OnMosaicDsCollectionChanged;

            {
                HSV hsv = new HSV(AnimatedImageModelConst.DefaultForegroundColor) {
                    s = 80,
                    v = 70,
                    a = 170
                };
                BorderColorStartBttn = new SolidColorBrush(hsv.ToColor().ToWinColor());

                Action run = () => {
                    if (gridMosaics.SelectedItem == null)
                        return;
                    hsv.h += 10;
                    BorderColorStartBttn.Color = hsv.ToColor().ToWinColor();
                };
                run.Repeat(TimeSpan.FromMilliseconds(100), () => _closed);
            }

            this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.SizeChanged += OnPageSizeChanged;
        }

        private void OnPageLoaded(object sender, RoutedEventArgs e) {
            this.Loaded -= OnPageLoaded;

            UpdateViewModel();
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            this.Unloaded -= OnPageUnloaded;
            _closed = true;
            ViewModel.MosaicDS.DataSource.CollectionChanged -= OnMosaicDsCollectionChanged;
            ViewModel.Dispose();
            _sizeChangedObservable?.Dispose();

            Bindings.StopTracking();
        }

        private void OnPageSizeChanged(object sender, SizeChangedEventArgs ev) {
            if (_sizeChangedObservable == null) {
                this.SizeChanged -= OnPageSizeChanged;
                _sizeChangedObservable = Observable
                    .FromEventPattern<SizeChangedEventHandler, SizeChangedEventArgs>(h => SizeChanged += h, h => SizeChanged -= h) // equals .FromEventPattern<SizeChangedEventArgs>(this, "SizeChanged")
                    .Throttle(TimeSpan.FromSeconds(0.2)) // debounce events
                    .Subscribe(x => {
                        System.Threading.Tasks.Task.Run(() => AsyncRunner.InvokeFromUiLater(() => OnPageSizeChanged(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.Low));
                        //AsyncRunner.InvokeFromUiLater(() => OnPageSizeChanged(x.Sender, x.EventArgs), Windows.UI.Core.CoreDispatcherPriority.Low);
                    });
            }

            var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
            var size2 = size / 3.9;
            var wh = Math.Min(Math.Max(TileMinSize, size2), TileMaxSize);
            //LoggerSimple.Put("Math.Min(Math.Max(TileMinSize={0}, size2={1}), TileMaxSize={2}) = {3}", TileMinSize, size2, TileMaxSize, wh);
            ViewModel.ImageSize = new SizeDouble(wh, wh);
        }

        private void OnMosaicItemSelectionChanged(object sender, SelectionChangedEventArgs ev) {
            //throw new NotImplementedException();
        }

        private void OnMosaicItemClick(object sender, ItemClickEventArgs ev) {
            //int oldPosition = gridMosaics.SelectedIndex; // TIP: SelectedIndex is _OLD_ index

            System.Diagnostics.Debug.Assert(ev.ClickedItem is MosaicDataItem);
            var clickItem = ev.ClickedItem as MosaicDataItem;
            EMosaic selectedMosaic = clickItem.MosaicType;
            InitData.MosaicType = selectedMosaic;
        }

        private void OnMosaicItemDoubleClick(object sender, Windows.UI.Xaml.Input.DoubleTappedRoutedEventArgs ev) {
            StartNewGame();
        }

        private void OnClickBttnStartGame(object sender, RoutedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(SelectMosaicPage) + "::" + nameof(OnClickBttnStartGame));
            StartNewGame();
        }

        private void StartNewGame() {
            //Frame frame = this.Frame;
            Frame frame = Window.Current.Content as Frame;
            System.Diagnostics.Debug.Assert(frame != null);

            frame.Navigate(typeof(MosaicPage), InitData);

            //Window.Current.Content = new MosaicPage();
            //// Ensure the current window is active
            //Window.Current.Activate();
        }

        public void UpdateViewModel() {
            ESkillLevel skill = InitData.SkillLevel;
            EMosaic mosaicType = InitData.MosaicType;
            ViewModel.MosaicDS.SkillLevel = skill;
            ViewModel.MosaicDS.MosaicGroup = mosaicType.GetGroup();
            var newItem = ViewModel.MosaicDS.DataSource.First(x => x.MosaicType == mosaicType);
            ViewModel.MosaicDS.CurrentItem = newItem;
        }

        private void OnDataContextChangedCanvasControl(FrameworkElement sender, DataContextChangedEventArgs ev) {
            if (ev.NewValue == null)
                return;

            var canvasControl = sender as CanvasControl;
            System.Diagnostics.Debug.Assert(ev.NewValue is MosaicsCanvasCtrllr);
            if (mapBindingControlToController.ContainsKey(canvasControl))
                mapBindingControlToController[canvasControl] = ev.NewValue as MosaicsCanvasCtrllr;
            else
                mapBindingControlToController.Add(canvasControl, ev.NewValue as MosaicsCanvasCtrllr);
            canvasControl.Invalidate();
            ev.Handled = true;
        }

        private void OnMosaicDsCollectionChanged(object sender, NotifyCollectionChangedEventArgs ev) {
            // verify mapBindingControlToController and remove obsolete bindings
            if (ev.OldItems == null)
                return;
            foreach (var item in ev.OldItems) {
                System.Diagnostics.Debug.Assert(item is MosaicDataItem);
                var mi = (MosaicDataItem)item;
                var pair = mapBindingControlToController.FirstOrDefault(kv => ReferenceEquals(kv.Value, mi));
                if (pair.Key != null)
                    mapBindingControlToController.Remove(pair.Key);
            }
        }

        public void OnDrawCanvasControl(CanvasControl canvasControl, CanvasDrawEventArgs ev) {
            if (!mapBindingControlToController.ContainsKey(canvasControl))
                return;
            var ctrllr = mapBindingControlToController[canvasControl];
            if (ctrllr.Disposed)
                return;
            var img = ctrllr.Image;
            System.Diagnostics.Debug.Assert(img != null); // null where is disposed
            ev.DrawingSession.DrawImage(img, new Windows.Foundation.Rect(0, 0, canvasControl.Width, canvasControl.Height));
        }

    }

}
