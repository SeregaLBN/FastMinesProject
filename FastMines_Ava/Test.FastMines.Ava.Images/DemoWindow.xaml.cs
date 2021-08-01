using System;
using System.Linq;
using System.ComponentModel;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Markup.Xaml;
using Avalonia.Media.Imaging;
using Avalonia.VisualTree;
using Fmg.Common.Notifier;
using Fmg.Ava.Img;

namespace Test.FastMines.Ava.Images {

    public class DemoWindow : Window {

        private class Modelka : INotifyPropertyChanged, IDisposable {

            public MosaicGroupImg.RenderTargetBmpController MosaicImg { get; }
            public IBitmap Bitmap => MosaicImg.Image;
            private readonly IVisual _visual;
            public event PropertyChangedEventHandler PropertyChanged {
                add    { _notifier.PropertyChanged += value;  }
                remove { _notifier.PropertyChanged -= value;  }
            }
            private readonly NotifyPropertyChanged _notifier;

            public Modelka(IVisual visual) {
                _visual = visual;
                _notifier = new NotifyPropertyChanged(this);

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

            public void Dispose() {
                MosaicImg.PropertyChanged -= OnMosaicImgPropertyChanged;
                MosaicImg.Dispose();
                _notifier.Dispose();
            }

        }

        private Modelka _viewModel;
        // private IControl _img;

        public DemoWindow() {
            InitializeComponent();
            //this.AttachDevTools();

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
