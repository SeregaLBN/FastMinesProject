using System.Collections.Generic;
using System.Linq;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Threading;
using Avalonia.VisualTree;

namespace Test.FastMines.Ava.Images.WBmp {

    public class DemoWindow : Window
    {
        // private SnowViewModel _viewModel;
        // private IControl _img;

        public DemoWindow()
        {
            InitializeComponent();
            this.AttachDevTools();

            // DataContext = _viewModel;
        }

        private void InitializeComponent()
        {
            AvaloniaXamlLoaderPortableXaml.Load(this);
        }

    }
}
