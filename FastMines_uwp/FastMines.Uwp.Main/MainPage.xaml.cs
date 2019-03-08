using System;
using System.ComponentModel;
using System.Linq;
using System.Reactive.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Navigation;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.uwp.utils;
using fmg.DataModel.Items;
using fmg.DataModel.DataSources;
using FastMines.Uwp.Main.Presentation;
using MosaicsSkillImg = fmg.uwp.img.win2d.MosaicSkillImg.ControllerBitmap;
using MosaicsGroupImg = fmg.uwp.img.win2d.MosaicGroupImg.ControllerBitmap;

namespace fmg {

    public sealed partial class MainPage : Page {

        internal const int MenuTextWidth = 110;

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData InitData { get; private set; }
        /// <summary> View-Model </summary>
        public MainMenuViewModel ViewModel { get; } = new MainMenuViewModel();
        public Frame RightFrame => this._frame;
        private IDisposable _sizeChangedObservable;


        public MainPage() {
            this.InitializeComponent();

            this.Loaded += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;
            this.SizeChanged += OnPageSizeChanged;
        }

        protected override void OnNavigatedTo(NavigationEventArgs ev) {
            base.OnNavigatedTo(ev);

            System.Diagnostics.Debug.Assert(ev.Parameter is MosaicInitData);
            InitData = (ev.Parameter as MosaicInitData) ?? new MosaicInitData();
        }

        private void OnPageLoaded(object sender, RoutedEventArgs e) {
            this.Loaded -= OnPageLoaded;

            //var dpi = Cast.ToDpi(100);

            ViewModel.MosaicGroupDS.PropertyChanged += OnMosaicGroupDsPropertyChanged;
            ViewModel.MosaicSkillDS.PropertyChanged += OnMosaicSkillDsPropertyChanged;

            ViewModel.MosaicGroupDS.CurrentItem = ViewModel.MosaicGroupDS.DataSource.First(x => x.MosaicGroup == InitData.MosaicType.GetGroup());
            ViewModel.MosaicSkillDS.CurrentItem = ViewModel.MosaicSkillDS.DataSource.First(x => x.SkillLevel == InitData.SkillLevel);

            var smp = RightFrame?.Content as SelectMosaicPage;
            if (smp != null) {
                var ds = smp.ViewModel.MosaicDS;
                ds.CurrentItem = ds.DataSource.First(x => x.MosaicType == InitData.MosaicType);
            }

            SmoothHelper.ApplyButtonColorSmoothTransition(_bttnGroupPanel, ViewModel.MosaicGroupDS.Header.Entity.Model);
            SmoothHelper.ApplyButtonColorSmoothTransition(_bttnSkillPanel, ViewModel.MosaicSkillDS.Header.Entity.Model);
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            this.Unloaded -= OnPageUnloaded;

            //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnClosing));
            ViewModel.MosaicGroupDS.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
            ViewModel.MosaicSkillDS.PropertyChanged -= OnMosaicSkillDsPropertyChanged;

            ViewModel.Dispose();
            _sizeChangedObservable?.Dispose();

            Bindings.StopTracking();
        }

        private void OnListViewMosaicGroupMenuTapped(object sender, Windows.UI.Xaml.Input.TappedRoutedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnListViewMosaicGroupMenuTapped));
            var listView = (ListView)sender;
            if (!(RightFrame.Content is SelectMosaicPage))
                ShowSelectMosaicPage(EMosaicGroupEx.FromIndex(listView.SelectedIndex));
        }

        private void OnListViewSkillLevelMenuTapped(object sender, Windows.UI.Xaml.Input.TappedRoutedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnListViewSkillLevelMenuTapped));
            var listView = (ListView)sender;
            if ((listView.SelectedIndex == ESkillLevel.eCustom.Ordinal()) && !(RightFrame.Content is CustomSkillPage))
                ShowCustomSkillPage();
        }

        private void ShowSelectMosaicPage(EMosaicGroup mosaicGroup) {
            var smp = RightFrame.Content as SelectMosaicPage;
            if (smp == null) {
                RightFrame.SourcePageType = typeof(SelectMosaicPage);
                smp = RightFrame.Content as SelectMosaicPage;
            }
            smp.CurrentMosaicGroup = mosaicGroup;
            smp.MosaicData = this.InitData;
            smp.CurrentSkillLevel = this.InitData.SkillLevel;
            if (this.InitData.MosaicType.GetGroup() == mosaicGroup)
                smp.CurrentItem = smp.ViewModel.MosaicDS.DataSource.First(x => x.MosaicType == this.InitData.MosaicType);
        }
        private void ShowCustomSkillPage() {
            RightFrame.SourcePageType = typeof(CustomSkillPage);
            var csp = RightFrame.Content as CustomSkillPage;
            csp.MosaicData = this.InitData;
        }
        private void ShowHypnosisLogoPage() {
            LoggerSimple.Put("TODO:  redirect to ShowHypnosisLogoPage...");
        }

        private void OnPropertyCurrenItemChanged(bool senderIsMosaicGroup, MosaicGroupDataItem currentGroupItem, MosaicSkillDataItem currentSkillItem) {
            if ((currentGroupItem == null) || (currentSkillItem == null)) {
                ShowHypnosisLogoPage();
                return;
            }

            if (currentSkillItem.SkillLevel.Value != ESkillLevel.eCustom)
                InitData.SkillLevel = currentSkillItem.SkillLevel.Value;

            if (!senderIsMosaicGroup && (currentSkillItem.SkillLevel == ESkillLevel.eCustom)) {
                ShowCustomSkillPage();
            } else {
                //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnPropertyCurrenItemChanged) + ": " + currentGroupItem.MosaicGroup);
                ShowSelectMosaicPage(currentGroupItem.MosaicGroup.Value);
            }
        }

        private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnMosaicGroupDsPropertyChanged) + ": ev.Name=" + ev.PropertyName);
            switch (ev.PropertyName) {
            case nameof(ViewModel.MosaicGroupDS.CurrentItem):
                var currentGroupItem = ViewModel.MosaicSkillDS.CurrentItem;
                OnPropertyCurrenItemChanged(true, ((MosaicGroupDataSource)sender).CurrentItem, currentGroupItem);
                break;
            }
        }

        private void OnMosaicSkillDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            //LoggerSimple.Put("> " + nameof(MainPage) + "::" + nameof(OnMosaicSkillDsPropertyChanged) + ": ev.Name=" + ev.PropertyName);
            switch (ev.PropertyName) {
            case nameof(ViewModel.MosaicSkillDS.CurrentItem):
                var currentSkillItem = ((MosaicSkillDataSource)sender).CurrentItem;
                OnPropertyCurrenItemChanged(false, ViewModel.MosaicGroupDS.CurrentItem, currentSkillItem);
                break;
            }
        }

        void OnPageSizeChanged(object sender, SizeChangedEventArgs ev) {
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

            //System.Diagnostics.Debug.WriteLine("OnSizeChanged");
            const int minSize = 50;
            const int topElemHeight = 48;
            const int pad = 3;
            System.Diagnostics.Debug.Assert(topElemHeight <= minSize);

            var size = Math.Min(ev.NewSize.Height, ev.NewSize.Width);
            var size1 = size/7;
            var wh = Math.Min(Math.Max(minSize, size1), 100); // TODO: DPI dependency
            ViewModel.MosaicGroupDS.ImageSize =
            ViewModel.MosaicSkillDS.ImageSize = new SizeDouble(wh, wh);

            ViewModel.MosaicGroupDS.Header.Size =
            ViewModel.MosaicSkillDS.Header.Size = new SizeDouble(wh, topElemHeight);
            ViewModel.MosaicGroupDS.Header.Padding =
            ViewModel.MosaicSkillDS.Header.Padding = new BoundDouble(pad, pad, wh - topElemHeight + pad, pad); // left margin

            double whBurger = topElemHeight / 2 + Math.Min(topElemHeight / 2 - pad, Math.Max(0, (wh - 1.5 * topElemHeight)));
            var padBurger = new BoundDouble(wh - whBurger, topElemHeight - whBurger, pad, pad);
            ViewModel.MosaicGroupDS.Header.PaddingBurgerMenu =
            ViewModel.MosaicSkillDS.Header.PaddingBurgerMenu = padBurger; // right-bottom margin
        }

        //public static IEnumerable<T> FindChilds<T>(FrameworkElement parent, int depth = 1, Func<T, bool> filter = null)
        //    where T : FrameworkElement {
        //    var cnt = VisualTreeHelper.GetChildrenCount(parent);
        //    for (var i = 0; i < cnt; i++) {
        //        var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
        //        var correctlyTyped = child as T;
        //        if (correctlyTyped != null && (filter == null || filter(correctlyTyped)))
        //            yield return correctlyTyped;
        //    }
        //    for (var i = 0; (depth > 1) && (i < cnt); i++) {
        //        var child = VisualTreeHelper.GetChild(parent, i) as FrameworkElement;
        //        foreach (var c in FindChilds(child, depth - 1, filter))
        //            yield return c;
        //    }
        //}

        private void OnCreateResourcesCanvasControl_MosaicsSkillImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
            System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsSkillImg);

            if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
                var img = canvasControl.DataContext as MosaicsSkillImg;

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

        private void OnCreateResourcesCanvasControl_MosaicsGroupImg(CanvasControl canvasControl, CanvasCreateResourcesEventArgs ev) {
            System.Diagnostics.Debug.Assert(canvasControl.DataContext is MosaicsGroupImg);

            if (ev.Reason == CanvasCreateResourcesReason.FirstTime) {
                var img = canvasControl.DataContext as MosaicsGroupImg;

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

        private void OnClickBttnGroupPanel(object sender, RoutedEventArgs e) {
            if (_listViewMosaicGroupMenu.Visibility == Visibility.Collapsed) {
                SmoothHelper.ApplySmoothVisibilityOverScale(_listViewMosaicGroupMenu, true, LvGroupHeight);
                ViewModel.MosaicGroupDS.Header.Entity.BurgerMenuModel.Horizontal = false;
            } else {
                _splitView.IsPaneOpen = !_splitView.IsPaneOpen;
                ViewModel.MosaicGroupDS.Header.Entity.Model.AnimeDirection = !ViewModel.MosaicGroupDS.Header.Entity.Model.AnimeDirection;
            }
        }

        double LvGroupHeight() => Enum.GetValues(typeof(EMosaicGroup)).Length * (ViewModel.MosaicGroupDS.ImageSize.Height + 2 /* padding */);
        double LvSkillHeight() => Enum.GetValues(typeof(ESkillLevel )).Length * (ViewModel.MosaicSkillDS.ImageSize.Height + 2 /* padding */);

        private void OnClickBttnSkillPanel(object sender, RoutedEventArgs e) {
            bool isVisibleScrollerFunc() => !_scroller.ScrollableHeight.HasMinDiff(_scroller.VerticalOffset);
            bool isVisibleScroller = isVisibleScrollerFunc();
            if (_listViewSkillLevelMenu.Visibility == Visibility.Collapsed) {
                if (isVisibleScroller) {
                    SmoothHelper.ApplySmoothVisibilityOverScale(_listViewSkillLevelMenu , true , LvSkillHeight);
                    SmoothHelper.ApplySmoothVisibilityOverScale(_listViewMosaicGroupMenu, false, LvGroupHeight);
                } else {
                    SmoothHelper.ApplySmoothVisibilityOverScale(_listViewSkillLevelMenu, true, LvSkillHeight,
                        () => {
                            if (isVisibleScrollerFunc())
                                SmoothHelper.ApplySmoothVisibilityOverScale(_listViewMosaicGroupMenu, false, LvGroupHeight);
                        });
                }
                ViewModel.MosaicSkillDS.Header.Entity.Model.AnimeDirection = !ViewModel.MosaicSkillDS.Header.Entity.Model.AnimeDirection;
            } else {
                if (isVisibleScroller && (_listViewMosaicGroupMenu.Visibility == Visibility.Visible)) {
                    SmoothHelper.ApplySmoothVisibilityOverScale(_listViewMosaicGroupMenu, false, LvGroupHeight);
                    ViewModel.MosaicGroupDS.Header.Entity.BurgerMenuModel.Horizontal = true;
                } else {
                    SmoothHelper.ApplySmoothVisibilityOverScale(_listViewSkillLevelMenu, false, LvSkillHeight);
                    ViewModel.MosaicSkillDS.Header.Entity.Model.AnimeDirection = !ViewModel.MosaicSkillDS.Header.Entity.Model.AnimeDirection;
                }
            }
        }

    }

}
