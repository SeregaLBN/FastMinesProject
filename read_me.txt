IDE Workspaces / Solutions:
   ./build.gradle  - open (import) in IDE Eclipse / IDEA
         subprojects (Java):
            ./FastMines_core    - open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_swing   - open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_jfx     - open in Eclipse or IDEA(Android Studio) or VS Code
            ./FastMines_android - open in Android Studio(IDEA)
   ./FastMines.sln  - solutions for Visual Studio 2017
         subprojects in subdirectories:
            ./FastMines_core                    - C#
                ./FastMines.Core                        - shared library - common utils  &  core business logic
                ./UnitTest.FastMines.Core               - shared library for Unit Tests
                ./UnitTest.FastMines.NUnit              - unit test for FastMines.Core over NUnit
            ./FastMines_uwp                     - C#
                ./FastMines.Uwp.Main                    - Main program
                ./FastMines.Uwp.Shared                  - shared library for all UWP projects
                ./Test.FastMines.Uwp.Images             - visual check drawing demo-program
                ./FastMines.Uwp.Draw.Win2D              - shared library (used https://github.com/Microsoft/Win2D)
                ./FastMines.Uwp.Draw.XamlElem           - shared library (uses a composite mosaic of XAML elements; left as an example)  (obsolete; left as an example)
                ./FastMines.Uwp.Draw.WBmp               - shared library (used WritableBitmap extension https://github.com/reneschulte/WriteableBitmapEx) (obsolete because brakes; left as an example)
                ./FastMines.Uwp.BackgroundTasks         - service for main program
                ./UnitTest.FastMines.Uwp                - unit test for UWP over Microsoft TestFramework
            ./FastMines_Logo                    - C++
            ./FastMines_Ava                     - C#     (can be opened and run in VS Code)
                ./FastMines.Ava.Draw                    - shared library for all Avalonia projects
                ./Test.FastMines.Ava.Images             - Template for future visual check drawing demo-program

./src - common code base
./res - shared resources files


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
GRADLE lifehacks
 If you want to import FastMines_android into Eclipse IDE (open without build)
   * temporarily change in ./build.gradle
     to   classpath 'com.android.tools.build:gradle:3.2.1'
     and restore after successful import
 If you do not want to import FastMines_android into Eclipse IDE
   * temporarily comment on the following line in ./settings.gradle
     include ':FastMines_android'
     and restore after successful import

----------------------------------
Android Studio
How to:
* Open FastMinesProject:
 1. Import project (Gradle ...)
 2. and select root FastMinesProject directory

Lifehacks:
If you want successfully build the FastMines_jfx
 * change internal JDK to external
   Menu 'File' -> Project Structure... -> SDK Location -> unselect 'Use embedded JDK (recommended)' -> select path to you JDK8 -> Ok
 * or manualy copy file  jfxrt.jar
   from external JDK
      %JAVA_HOME_8%\jre\lib\ext\
   to
      you_path\Android Studio\jre\jre\lib\ext\

----------------------------------
Eclipse (Java)
How to:
* Open FastMinesProject
  (see up GRADLE lifehacks)
1. Create new or open existing workspace in different external folder.
2. Menu 'File' -> Import... -> expand 'Gradle' -> select 'Existing Gradle Project' -> button 'Next >' -> 'Next >' -> in 'Project root directory' click 'Browse...' -> select path ./FastMinesProject -> Ok -> Finish

* Configure workspace:
1. Verify default encoding (need UFT-8):
   Menu Window -> Preferences -> expand 'General' -> Workspace -> in group 'Text file encoding' select 'Other: UTF-8' -> click 'Apply and Close'
2. Set 'tab' as 3 spaces:
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
      (PS: Use ctrl+shift+f to format a java class which will replace tab with space.)
