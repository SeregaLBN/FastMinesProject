using Avalonia;
using Avalonia.Controls;
using Avalonia.Diagnostics;
using Avalonia.Markup.Xaml;
using Fmg.Ava.App;

namespace Test.FastMines.Ava.Images {

    /// <summary>
    /// live UI test application
    /// <para>run from command line</para>
    /// <c>dotnet run --project .\FastMines_Ava\Test.FastMines.Ava.Images\Test.FastMines.Ava.Images.csproj</c>
    /// </summary>
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
