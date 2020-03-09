using Avalonia;
using Avalonia.Controls;
using Avalonia.Diagnostics;
using Avalonia.Markup.Xaml;
using Fmg.Ava.Utils;

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
//                .UseReactiveUI()
//                .LogToDebug()
                .Start<DemoWindow>();
        }

    }

}
