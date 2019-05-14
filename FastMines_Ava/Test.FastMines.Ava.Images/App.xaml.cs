using Avalonia;
using Avalonia.Controls;
using Avalonia.Diagnostics;
using Avalonia.Markup.Xaml;
using fmg.ava.utils;

namespace Test.FastMines.Ava.Images {

    class App : Application {

        public override void Initialize() {
            AvaloniaXamlLoader.Load(this);
            base.Initialize();
            ProjSettings.Init();
        }

        static void Main(string[] args) {
            AppBuilder.Configure<App>()
                .UsePlatformDetect()
                .Start<DemoWindow>();
        }

        public static void AttachDevTools(Window window) {
#if DEBUG
            DevTools.Attach(window);
#endif
        }

    }

}
