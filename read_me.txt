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
                ./FastMines.Core
                ./Test.FastMines.Common
            ./FastMines_uwp                     - C#
                ./FastMines.Uwp.Main
                ./FastMines.Uwp.Shared
                ./Test.FastMines.Uwp.Images
                ./FastMines.Uwp.Draw.WBmp
                ./FastMines.Uwp.Draw.Win2D
                ./FastMines.Uwp.Draw.XamlElem
                ./FastMines.Uwp.BackgroundTasks
            ./FastMines_Logo                    - C++
            ./FastMines_Ava                     - C#     (can be opened and run in VS Code)
                ./FastMines.Ava.Draw
                ./Test.FastMines.Ava.Images

./src - common code base
./res - shared resources files


FastMines_xxx - projects of a specific language implementation, for a specific UI platform:
   FastMines_core     - shared project libraries 
   FastMines_android  - (  demo  ) - Java Android tablet/mobile application                          (open from Android Studio)
   FastMines_swing    - ( stable ) - Java desktop SWING project (multiplatform)                      (open from the Eclipse workspace / IDEA / VSCode)
   FastMines_jfx      - (  demo  ) - Java FX desktop project                                         (open from the Eclipse workspace / IDEA / VSCode)
   FastMines_uwp      - ( stable ) - C# desktop/tablet/mobile Universal Windows Platform application (open as a Visual Studio solution)
   FastMines_Logo     - (release ) - C++ Win32 proj (generate logo project - raw bitmap)             (open as a Visual Studio solution)
   FastMines_SVG      - (  demo  ) - SVG-animation FastMines images over JS/SVG/HTML                 (open any IDE/text editor)
   FastMines_Ava      - (try demo) - C# desktop Avalonia project (multiplatform - .NET Core)         (open in VS code / VS)
   ...





----------------------------------
Eclipse (Java)
How to:
* Open FastMinesProject
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
