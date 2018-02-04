IDE Workspaces / Solutions:
   ./FastMines_swing/build.gradle  - open (import) in IDE Eclipse or IDEA
   ./FastMines_uwp/FastMines.sln   - solutions for Visual Studio 

./src - common source projects
./res - shared resources projects

FastMines_xxx - проекты конкретной языковой реализации, под конкретную UI платформу:
 * FastMines_swing    - ( stable ) - Java desktop SWING project (multiplatform)                      (open from Eclipse workspace)
 * FastMines_jfx      - (  demo  ) - Java FX desktop project                                         (open from Eclipse workspace)
 * FastMines_uwp      - ( stable ) - C# desktop/tablet/mobile Universal Windows Platform application (open from Visual Studio workspace)
 * FastMines_Logo     - (release ) - C++ Win32 proj (generate logo project - raw bitmap)             (open from Visual Studio workspace)
 * FastMines_SVG      - (  demo  ) - SVG-animation FastMines images over JS
 * FastMines_Ava      - (  demo  ) - C# desktop Avalon project (multiplatform - .NET Core)           (open in VS code / VS)
 * ...
 
 
 
 
 
 
 
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
      -> Displayed tab width: 3
      -> Insert spaced for tabs: checked!
   2.1. To resolve the issue of tab with space in eclipse editor:
      2.1.1. menu Window -> Preferences -> Java -> Code Style -> Formatter
      2.1.2. click 'Configure Project Specific Settings...' choose the project
      2.1.3. Create custom 'Active profile' -> click 'New...' -> enter 'FastMines profile' -> click Ok
      2.1.4. Edit 'FastMines profile'. Click 'Edit...' -> page will be poped up
            -> tab 'Indentation' -> group 'General settings'
                  -> in 'Tab policy:' select 'Spaces only'
                  -> in 'Indentation' enter 3
                  -> in 'Tab size'    enter 3
                  -> click Ok
            -> click 'Apply and Close'
      (PS: Use ctrl+shift+f to format a java class which will replace tab with space.)
 