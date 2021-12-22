IDE Workspaces / Solutions:
   ./build.gradle  - open (import) in IDE Eclipse / IDEA
         subprojects (Java):
            ./FastMines_core    - Core of FastMine project      open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_swing   - SWING support                 open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_jfx     - JavaFX support                open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_android - Android version               open in Android Studio(IDEA)
   ./FastMines.sln  - solutions for Visual Studio
         subprojects in subdirectories:
            ./FastMines_core                    - C#    - Core of FastMines project
                ./UnitTest.FastMines.Core                   - shared library for Unit Tests
                ./UnitTest.FastMines.NUnit                  - unit test for FastMines.Core over NUnit
            ./FastMines_uwp                     - C#    - Universal Windows Platform
                ./FastMines.Uwp.Main                        - Main program
                ./FastMines.Uwp.Shared                      - shared library for all UWP projects
                ./Test.FastMines.Uwp.Images                 - visual check drawing demo-program
                ./FastMines.Uwp.Draw.Win2D                  - shared library (used https://github.com/Microsoft/Win2D)
                ./FastMines.Uwp.Draw.Xaml                   - shared library (uses a composite mosaic of XAML elements; left as an example)  (obsolete; left as an example)
                ./FastMines.Uwp.Draw.WBmp                   - shared library (used WritableBitmap extension https://github.com/reneschulte/WriteableBitmapEx) (obsolete because brakes; left as an example)
                ./UnitTest.FastMines.Uwp                    - unit test for UWP over MSTest
            ./FastMines_Logo                    - C++   - Main logo (canonical)
            ./FastMines_Ava                     - C#    - Avalonia framework (can be opened and run in VS Code)
                ./FastMines.Ava.Draw                        - shared library for all Avalonia projects
                ./Test.FastMines.Ava.Images                 - Template for future visual check drawing demo-program
                ./UnitTest.FastMines.Ava                    - unit test for Avalonia over xUnit

----------------------------------
FastMines_xxx - projects of a specific language implementation, for a specific UI platform:
   FastMines_core     - shared project libraries (common utils;  core business logic;  unit tests)
   FastMines_android  - (  demo  ) - Java Android tablet/mobile application                          (open from Android Studio)
                                     Flavors: Main program;  visual check drawing demo-program;  Unit tests
   FastMines_swing    - ( stable ) - Java desktop SWING project (multiplatform)                      (open from the Eclipse workspace / IDEA / VSCode)
                                     Main program;  visual check drawing demo-program ;  unit tests
   FastMines_jfx      - (  demo  ) - Java FX desktop project                                         (open from the Eclipse workspace / IDEA / VSCode)
                                     Template for future main program;  visual check drawing demo-program;  unit tests
   FastMines_uwp      - ( stable ) - C# desktop/tablet/mobile Universal Windows Platform application (open as a Visual Studio solution)
   FastMines_Logo     - (release ) - C++ Win32 proj (generate logo project - raw bitmap)             (open as a Visual Studio solution)
   FastMines_svg      - (  demo  ) - SVG-animation FastMines images over JS/SVG/HTML                 (open any IDE/text editor and view in browser Firefox/Chrome)
   FastMines_Ava      - (try demo) - C# desktop Avalonia project (multiplatform - .NET Core)         (open in VS code / VS)
   ...

----------------------------------
Agreements:
 * basic package/namespace name - fmg.* / Fmg.*   -  FastMinesGame
 * using 4 space   for  tab size
 * subdirectories corresponds to the package name/namespace

*/src - source files
./res - shared resources files

----------------------------------
Requirements:
 * Java 11
    https://jdk.java.net

 * Gradle 7
    https://gradle.org/releases/

 * Microsoft Visual Studio Community 2019  ( to build UWP application )
    https://visualstudio.microsoft.com/downloads/

 * .NET Core
    https://docs.microsoft.com/en-us/dotnet/core/install/

----------------------------------
Run from command line

 * FastMines_swing
    Main app:
        gradle :FastMines_swing:run
    Test demo app:
        gradle :FastMines_swing:runDemoApp

 * FastMines_jfx
    Simple app:
        gradle :FastMines_jfx:run
    Test demo app:
        gradle :FastMines_jfx:runDemoApp

 * FastMines_Ava
    Test demo app
        dotnet run --project ./FastMines_Ava/Test.FastMines.Ava.Images/Test.FastMines.Ava.Images.csproj

 * FastMines_svg
        ./FastMines_svg/DemoApp.html
    (try reload F5; see output console F12)

 * FastMines_android
    run manual emulator
    Build:
        gradle FastMines_android:build
    Main app:
        adb install -t -r ./FastMines_android/build/outputs/apk/app/debug/FastMines_android-app-debug.apk
        adb shell am start -a android.intent.action.MAIN -n SeregaLBN.FastMinesGame.Android.App.debug/fmg.android.app.MainActivity
    Test demo app:
        adb install -t -r ./FastMines_android/build/outputs/apk/demo/debug/FastMines_android-demo-debug.apk
        adb shell am start -a android.intent.action.MAIN -n SeregaLBN.FastMinesGame.Android.App.demo.debug/fmg.android.app.DemoActivity

----------------------------------
Android Studio
How to:
* Open FastMinesProject:
 1. Import project (Gradle ...)
 2. and select root FastMinesProject directory

----------------------------------
Eclipse (Java)
How to:
* Open FastMinesProject
1. Create new or open existing workspace in different external folder.
2. Menu 'File' -> Import... -> expand 'Gradle' -> select 'Existing Gradle Project' -> button 'Next >' -> 'Next >' -> in 'Project root directory' click 'Browse...' -> select path ./FastMinesProject -> Ok -> Finish

* Configure workspace:
1. Verify default encoding (need UFT-8):
   Menu Window -> Preferences -> expand 'General' -> Workspace -> in group 'Text file encoding' select 'Other: UTF-8' -> click 'Apply and Close'
2. Set 'tab' as 4 spaces:
   Menu Window -> Preferences -> expand 'General' -> expand 'Editors' -> Text Editors ->
      -> Displayed tab width: 4
      -> Insert spaced for tabs: checked!
   2.1. To resolve the issue of tab with space in eclipse editor:
      2.1.1. menu Window -> Preferences -> Java -> Code Style -> Formatter
      2.1.2. click 'Configure Project Specific Settings...' choose the project
      2.1.3. Create custom 'Active profile' -> click 'New...' -> enter 'FastMines profile' -> click Ok
      2.1.4. Edit 'FastMines profile'. Click 'Edit...' -> page will be poped up
            -> tab 'Indentation' -> group 'General settings'
                  -> in 'Tab policy:' select 'Spaces only'
                  -> in 'Indentation' enter 4
                  -> in 'Tab size'    enter 4
                  -> click Ok
            -> click 'Apply and Close'

----------------------------------
Identic
UWP
./src/src_platform_uwp/fmg/uwp/mosaic/MosaicFrameworkElementController.cs
Android
./src/src_platform_android/fmg/android/mosaic/MosaicViewController.java

{A} - GUI platform
{B} - rendering subsytem (optional)
{C} - {A} or {B} if exist
{D} - UI control
{E} - UI image
Mosaic Views as UI controls:                             fmg.{A}    .mosaic{.B}  .Mosaic{C}View      ->  fmg.{A}    .mosaic{.B}  .Mosaic{D}View                            {D}
   Java SWING                                      /-->  fmg.swing  .mosaic      .MosaicSwingView    ->  fmg.swing  .mosaic      .MosaicJPanelView                 over javax.swing.JPanel
   Java JFX     ... -> fmg.core.mosaic.MosaicView  --->  fmg.jfx    .mosaic      .MosaicJfxView      ->  fmg.jfx    .mosaic      .MosaicCanvasView                 over javafx.scene.canvas.Canvas
   Java Android                                    \-->  fmg.android.mosaic      .MosaicAndroidView  ->  fmg.android.mosaic      .MosaicViewView                   over android.view.View
   C#   UWP     ... -> Fmg.Core.Mosaic.MosaicView  --->  Fmg.Uwp    .Mosaic.Win2d.MosaicWin2DView    ->  Fmg.Uwp    .Mosaic.Win2d.MosaicFrameworkElementView --    over Windows.UI.Xaml.FrameworkElement
                                                   \                                              /-----------------------------------------------------------/
                                                   |                                              \--->  Fmg.Uwp    .Mosaic.Win2d.MosaicCanvasSwapChainPanelView   over Microsoft.Graphics.Canvas.UI.Xaml.CanvasSwapChainPanel
                                                   |                                               \-->  Fmg.Uwp    .Mosaic.Win2d.MosaicCanvasVirtualControlView   over Microsoft.Graphics.Canvas.UI.Xaml.CanvasVirtualControl
                                                   |-------------------------------------------------->  Fmg.Uwp    .Mosaic.Wbmp .MosaicImageView                  over Windows.UI.Xaml.Controls.Image
                                                    \--> Fmg.Uwp    .Mosaic.Wbmp .MosaicWBmpView ----->  Fmg.Uwp    .Mosaic.Wbmp .MosaicImageView.InnerView        over Windows.UI.Xaml.Media.Imaging.WriteableBitmap
                                                     \------------------------------------------------>  Fmg.Uwp    .Mosaic.Xaml .MosaicXamlView                   over Windows.UI.Xaml.Controls.Panel

Mosaic Views as image:                                   fmg.{A}    .mosaic{.B}  .Mosaic{C}View      ->  fmg.{A}    .img{.B}  .MosaicImg.{A or E}View                      {E}
   Java SWING                                       /->  fmg.swing  .mosaic      .MosaicSwingView    ->  fmg.swing  .img      .MosaicImg.SwingView --
                                                   |                                              /-------------------------------------------------/
                                                   |                                              \--->  fmg.swing  .img      .MosaicImg.IconView                  over javax.swing.Icon
                                                   /                                               \-->  fmg.swing  .img      .MosaicImg.ImageAwtView              over java.awt.Image
   Java JFX     ... -> fmg.core.mosaic.MosaicView  --->  fmg.jfx    .mosaic      .MosaicJfxView      ->  fmg.jfx    .img      .MosaicImg.JfxView ---
                                                   \                                              /------------------------------------------------/
                                                   |                                              \--->  fmg.jfx    .img      .MosaicImg.CanvasView                over javafx.scene.canvas.Canvas
                                                   |                                               \-->  fmg.jfx    .img      .MosaicImg.ImageJfxView              over javafx.scene.image.Image
   Java Android                                     \->  fmg.android.mosaic      .MosaicAndroidView  ->  fmg.android.img      .MosaicImg.AndroidView --
                                                                                                  /---------------------------------------------------/
                                                                                                  \--->  fmg.android.img      .MosaicImg.BitmapView                over android.graphics.Bitmap
   C#   UWP     ... -> Fmg.Core.Mosaic.MosaicView  --->  Fmg.Uwp    .Mosaic.Win2d.MosaicWin2DView    ->  Fmg.Uwp    .Img.Win2d.MosaicImg.Win2DView --              over Microsoft.Graphics.Canvas.ICanvasResourceCreator
                                                   \                                              /-------------------------------------------------/
                                                   |                                              \--->  Fmg.Uwp    .Img.Win2d.MosaicImg.CanvasBmpView             over Microsoft.Graphics.Canvas.CanvasBitmap
                                                   \                                               \-->  Fmg.Uwp    .Img.Win2d.MosaicImg.CanvasImgSrcView          over Microsoft.Graphics.Canvas.UI.Xaml.CanvasImageSource
                                                    \--> Fmg.Uwp    .Mosaic.Wbmp .MosaicWBmpView ----->  Fmg.Uwp    .Img.Wbmp .MosaicImg.WBmpView                  over Windows.UI.Xaml.Media.Imaging.WriteableBitmap
