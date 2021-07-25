using System;
using System.Linq;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Types.Model;
using Fmg.Core.Img;
using Fmg.Uwp.App;
using Fmg.Uwp.Utils;
using Fmg.Uwp.App.Model.Items;
using Fmg.Uwp.App.Model;
using MosaicsCanvasCtrllr = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpController;
using    LogoCanvasCtrllr = Fmg.Uwp.Img.Win2d.Logo     .CanvasBmpController;
using Fmg.Uwp.App.Presentation;

namespace Fmg.Uwp.App {

    /// <summary> Page for selecting the type of mosaic in the current group </summary>
    public sealed partial class SelectMosaicPage : Page {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData InitData => FastMinesApp.Get.InitData;
        /// <summary> View-Model </summary>
        public MosaicsViewModel ViewModel { get; private set; }
        private SolidColorBrush BorderColorStartBttn;
        private bool _rotateBkColorOfGameBttn;
        private IDisposable _sizeChangedObservable;
        IDictionary<CanvasControl, MosaicsCanvasCtrllr> mapBindingControlToController = new Dictionary<CanvasControl, MosaicsCanvasCtrllr>();
        private bool _unloaded = false;

        public SelectMosaicPage() {
            this.InitializeComponent();
            ViewModel = new MosaicsViewModel();
            ViewModel.MosaicDS.DataSource.CollectionChanged += OnMosaicDsCollectionChanged;

            {
                var hsv = new HSV(AnimatedImageModelConst.DefaultForegroundColor) {
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
                run.Repeat(TimeSpan.FromMilliseconds(100), () => _rotateBkColorOfGameBttn);
            }

            this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.SizeChanged += OnPageSizeChanged;
        }

        protected override void OnNavigatedTo(NavigationEventArgs ev) {
            base.OnNavigatedTo(ev);

            { // setup header
                var logoController = ViewModel.MosaicDS.Header.Entity;
                logoController.UsePolarLightFgTransforming(true);
                var logoModel = logoController.Model;
                logoModel.RotateMode = LogoModel.ERotateMode.Classic;
                logoModel.AnimatePeriod = 30000;
                logoModel.TotalFrames = 700;
                logoModel.UseGradient = true;
                logoModel.Animated = true;
                logoModel.BorderWidth = 2;
                logoModel.BorderColor = Color.BlueViolet;

                panelMosaicHeader.Visibility = ProjSettings.IsMobile ? Visibility.Visible : Visibility.Collapsed;
                panelMosaicHeader.Background = new SolidColorBrush(MainPage.BackgroundHeaderColor.ToWinColor());

                System.Diagnostics.Debug.Assert(ev.Parameter is IDictionary<string, object>);
                if ((ev.Parameter is IDictionary<string, object> args) &&
                    args.TryGetValue(MainPage.ARGUMENTS_KEY__HEADER_SIZE_HEIGHT, out object headerSizeHeight) &&
                    (headerSizeHeight is double d))
                {
                    UpdateHeader(d);
                }
            }
        }

        private void OnPageLoaded(object sender, RoutedEventArgs ev) {
            this.Loaded -= OnPageLoaded;

            UpdateViewModel();
            //StartNewGame(); // <<<<<<<<<  delete this line
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            _unloaded = true;
            this.Unloaded -= OnPageUnloaded;
            _rotateBkColorOfGameBttn = true;
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

            var minTileWidth = Cast.DpToPx(48);
            var maxTileWidth = Cast.DpToPx(ProjSettings.IsMobile ? 90 : 140);
            var gridViewItemBorderWidth = 3.0 + // magic number ;(    not in visual
                     4 + 4      // <DataTemplate <StackPanel Margin.LeftAndRight
                    +8 + 8;     // <DataTemplate <StackPanel <canvas:CanvasControl Margin.LeftAndRight
            var widthBetweenItems = 4;

            var pageBorderWidth =
                    4 + 4;      // <ScrollViewer Margin.LeftAndRight
                  //+ 10 + 10;  // <ScrollViewer <GridView Margin.LeftAndRight

            var size = ev.NewSize.Width; // Math.Min(ev.NewSize.Width, ev.NewSize.Height);
            var spaceToItems = size - pageBorderWidth;

            var rows = 1;
            for (; rows <= EMosaicEx.GetValues().Length; ++rows) {
                var size2 = maxTileWidth * rows + (rows - 1) * widthBetweenItems;
                if (size2 > spaceToItems)
                    break;
            }

            var spaceToItemsClear = spaceToItems - (rows - 1) * widthBetweenItems;
            var tileWidth = spaceToItemsClear / rows;
            var tileWidth2 = Math.Min(Math.Max(tileWidth, minTileWidth), maxTileWidth);
            var imageSize = tileWidth2 - gridViewItemBorderWidth;
            //LoggerSimple.Put("tileWidth={0}, tileWidth2={1}, imageSize={2}", tileWidth, tileWidth2, imageSize);
            ViewModel.ImageSize = new SizeDouble(imageSize, imageSize);
        }

        private void OnMosaicItemSelectionChanged(object sender, SelectionChangedEventArgs ev) {
            //throw new NotImplementedException();
        }


        private void OnMosaicHeaderClick(object sender, RoutedEventArgs ev) {
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

        private void OnClickBttnBeginGame(object sender, RoutedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(SelectMosaicPage) + "::" + nameof(OnClickBttnBeginGame));
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

        public void UpdateHeader(double headerSizeHeight) {
            ViewModel.MosaicDS.Header.Size = new SizeDouble(headerSizeHeight, headerSizeHeight);
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

        private void OnCreateResourcesCanvasControl_MosaicImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
            System.Diagnostics.Debug.Assert(canvasControl.DataContext is LogoCanvasCtrllr);

            if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
                var img = canvasControl.DataContext as LogoCanvasCtrllr;

                canvasControl.Draw += (sender2, ev2) => {
                    ev2.DrawingSession.DrawImage(img.Image, new Windows.Foundation.Rect(0, 0, sender2.Width, sender2.Height)); // zoomed size
                    //ev2.DrawingSession.DrawImage(img.Image, new Windows.Foundation.Rect(0, 0, img.Width, img.Height)); // real size
                };
                img.PropertyChanged += (sender3, ev3) => {
                    if (ev3.PropertyName == nameof(img.Image))
                        canvasControl.Invalidate();
                };
            } else {
                System.Diagnostics.Debug.Assert(false, "Support me"); // TODO
            }
        }

    }

}
