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
using fmg.common.notyfier;
using fmg.ava.img;

namespace Test.FastMines.Ava.Images {

    public class DemoWindow : Window {

        private class Modelka : INotifyPropertyChanged {

            public event PropertyChangedEventHandler PropertyChanged;
            protected readonly NotifyPropertyChanged _notifier;

            public Modelka(IControl imgCtrl) {
                _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
                var mosaicImg = new MosaicGroupImg.ControllerRenderTargetBmp(null /*EMosaicGroup.eOthers*/, imgCtrl);
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

            public MosaicGroupImg.ControllerRenderTargetBmp MosaicImg { get; }
            public IBitmap Bitmap { get => MosaicImg.Image; }
        }

        private Modelka _viewModel;
        // private IControl _img;

        public DemoWindow() {
            InitializeComponent();
            this.AttachDevTools();

            DataContext = _viewModel;
        }

        private void InitializeComponent() {
            AvaloniaXamlLoaderPortableXaml.Load(this);

            IControl imgCtrl = ((StackPanel)Content).Children.First();

            _viewModel = new Modelka(imgCtrl);
        }

    }

}
