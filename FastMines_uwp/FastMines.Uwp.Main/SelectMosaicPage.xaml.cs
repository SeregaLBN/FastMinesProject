using System;
using System.Collections.Generic;
using System.Reactive.Linq;
using Windows.Foundation;
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
using MosaicsCanvasBmp = fmg.uwp.img.win2d.MosaicImg.CanvasBmp;

namespace fmg {

    /// <summary> Page for selecting the type of mosaic in the current group </summary>
    public sealed partial class SelectMosaicPage : Page {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData MosaicData { get; set; }
        /// <summary> View-Model </summary>
        public MosaicGroupViewModel ViewModel { get; private set; }
        public SolidColorBrush BorderColorStartBttn;
        private bool _closed;
        private IDisposable _sizeChangedObservable;

        public SelectMosaicPage() {
            this.InitializeComponent();
            MosaicData = new MosaicInitData();
            ViewModel = new MosaicGroupViewModel();

          //this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.SizeChanged += OnPageSizeChanged;

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
                run.RepeatNoWait(TimeSpan.FromMilliseconds(100), () => _closed);
            }
        }

      //private void OnPageLoaded(object sender, RoutedEventArgs e) {
      //    this.Loaded -= OnPageLoaded;
      //}

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            this.Unloaded -= OnPageUnloaded;
            _closed = true;
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
            var wh = (int)Math.Min(Math.Max(100, size2), 200); // TODO: DPI dependency
            ViewModel.ImageSize = new SizeDouble(wh, wh);
        }

        private void OnSelectionChangedGridViewMosaics(object sender, SelectionChangedEventArgs ev) {
            //throw new NotImplementedException();
        }

        private void OnItemClickGridViewMosaics(object sender, ItemClickEventArgs ev) {
            // invoke after set/change ViewModel.MosaicDS.CurrentItem
            AsyncRunner.InvokeFromUiLater(() => {
                var cu = ViewModel.MosaicDS.CurrentItem;
                if (cu != null)
                    MosaicData.MosaicType = cu.MosaicType;
            });
        }

        public EMosaicGroup CurrentMosaicGroup {
            //get { return ViewModel.MosaicDS.CurrentGroup; }
            set { ViewModel.MosaicDS.CurrentGroup = value; }
        }

        public ESkillLevel? CurrentSkillLevel {
            private get { return ViewModel.MosaicDS.CurrentSkill; }
            set { ViewModel.MosaicDS.CurrentSkill = value; }
        }

        public MosaicDataItem CurrentItem {
            private get { return ViewModel.MosaicDS.CurrentItem; }
            set { ViewModel.MosaicDS.CurrentItem = value; }
        }

        private void OnDataContextChangedCanvasControl(FrameworkElement sender, DataContextChangedEventArgs ev) {
            if (ev.NewValue == null)
                return;
            var canvasControl = sender as CanvasControl;
            System.Diagnostics.Debug.Assert(ev.NewValue is MosaicsCanvasBmp);
            if (map.ContainsKey(canvasControl))
                map[canvasControl] = ev.NewValue as MosaicsCanvasBmp;
            else
                map.Add(canvasControl, ev.NewValue as MosaicsCanvasBmp);
            canvasControl.Invalidate();
            ev.Handled = true;
        }

        IDictionary<CanvasControl, MosaicsCanvasBmp> map = new Dictionary<CanvasControl, MosaicsCanvasBmp>();

        private void OnDrawCanvasControl(CanvasControl canvasControl, CanvasDrawEventArgs ev) {
            var img = map[canvasControl];
            ev.DrawingSession.DrawImage(img.Image, new Windows.Foundation.Rect(0, 0, canvasControl.Width, canvasControl.Height));
        }

        private void StartNewGame() {
            //Frame frame = this.Frame;
            Frame frame = Window.Current.Content as Frame;
            System.Diagnostics.Debug.Assert(frame != null);

            var eMosaic = CurrentItem.MosaicType;
            frame.Navigate(typeof(MosaicPage2), MosaicData);

            //Window.Current.Content = new MosaicPage();
            //// Ensure the current window is active
            //Window.Current.Activate();
        }

        private void OnDoubleTappedGridViewMosaics(object sender, Windows.UI.Xaml.Input.DoubleTappedRoutedEventArgs e) {
            StartNewGame();
        }

        private void OnClickBttnStartGame(object sender, RoutedEventArgs ev) {
            LoggerSimple.Put("OnClickBttnStartGame");
            StartNewGame();
        }

    }

}
