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
            protected readonly NotifyPropertyChanged _notifier;

            public Modelka(IControl imgCtrl) {
                _notifier = new NotifyPropertyChanged(this);
                _notifier.PropertyChanged += OnNotifierPropertyChanged;
                var mosaicImg = new MosaicGroupImg.RenderTargetBmpController(null /*EMosaicGroup.eOthers*/, imgCtrl);
                mosaicImg.UseRotateTransforming(true);
                mosaicImg.UsePolarLightFgTransforming(true);
                var mosaicModel = mosaicImg.Model;
                mosaicModel.Animated = true;
                MosaicImg = mosaicImg;

                mosaicImg.PropertyChanged += (sender, ev) => {
                    if (ev.PropertyName == nameof(MosaicImg.Image)) {
                        _notifier.FirePropertyChanged(nameof(Modelka.Bitmap));
                        imgCtrl.InvalidateVisual(); // Dispatcher.UIThread.InvokeAsync(() => img.InvalidateVisual());//.Wait()
                    }
                };
            }

            public MosaicGroupImg.RenderTargetBmpController MosaicImg { get; }
            public IBitmap Bitmap => MosaicImg.Image;

            private void OnNotifierPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _notifier));
                PropertyChanged?.Invoke(this, ev);
            }

            public void Dispose() {
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
            _viewModel.Dispose();
        }

    }

}
