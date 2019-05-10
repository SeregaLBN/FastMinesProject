using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Media.Imaging;
using Avalonia.Threading;
using Avalonia.VisualTree;
using fmg.common.notifier;
using fmg.core.img;
using fmg.ava.img;

namespace Test.FastMines.Ava.Images {

    public class DemoWindow : Window {

        private class Modelka : INotifyPropertyChanged, IDisposable {

            public event PropertyChangedEventHandler PropertyChanged;
            public MosaicGroupImg.RenderTargetBmpController MosaicImg { get; }
            public IBitmap Bitmap => MosaicImg.Image;
            private readonly NotifyPropertyChanged _notifier;
            private readonly IVisual _visual;

            public Modelka(IVisual visual) {
                _visual = visual;
                _notifier = new NotifyPropertyChanged(this);
                _notifier.PropertyChanged += OnNotifierPropertyChanged;

                MosaicImg = new MosaicGroupImg.RenderTargetBmpController(null /*EMosaicGroup.eOthers*/, visual);
                MosaicImg.UseRotateTransforming(true);
                MosaicImg.UsePolarLightFgTransforming(true);
                MosaicImg.PropertyChanged += OnMosaicImgPropertyChanged;

                var mosaicModel = MosaicImg.Model;
                mosaicModel.Animated = true;
            }

            private void OnMosaicImgPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                if (ev.PropertyName == nameof(MosaicImg.Image)) {
                    _notifier.FirePropertyChanged(nameof(Modelka.Bitmap));
                    _visual.InvalidateVisual(); // Dispatcher.UIThread.InvokeAsync(() => img.InvalidateVisual());//.Wait()
                }
            }

            private void OnNotifierPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _notifier));
                PropertyChanged?.Invoke(this, ev);
            }

            public void Dispose() {
                MosaicImg.PropertyChanged -= OnMosaicImgPropertyChanged;
                MosaicImg.Dispose();
                _notifier.PropertyChanged -= OnNotifierPropertyChanged;
                _notifier.Dispose();
                NotifyPropertyChanged.AssertCheckSubscribers(this);
            }

        }

        private Modelka _viewModel;
        // private IControl _img;

        public DemoWindow() {
            InitializeComponent();
            this.AttachDevTools();

            DataContext = _viewModel;

            Closing += OnClosing;
        }

        private void InitializeComponent() {
            AvaloniaXamlLoader.Load(this);

            IControl imgCtrl = ((StackPanel)Content).Children.First();

            _viewModel = new Modelka(imgCtrl);
        }

        private void OnClosing(object sender, CancelEventArgs e) {
            Closing -= OnClosing;
            DataContext = null;
            _viewModel.Dispose();
        }

    }

}
