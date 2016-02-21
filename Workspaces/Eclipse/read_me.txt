Initialize Eclipse workspace:
1. Open Eclipse workspace: path ./FastMinesProject/Workspaces/Eclipse
2. Add project FastMines_swing into workspace:
   menu File -> Import... -> Gradle: Gradle Project -> Project root directory: ./FastMinesProject/FastMines_swing -> Finish
      or
   menu File -> Import... -> General: Existing Project into Workspace -> select root Directory: ./FastMinesProject/FastMines_swing
3. Set UFT-8 as default encoding:
   menu Window -> Preferences -> General -> Workspace -> Text file encoding: Other: UTF-8 -> click Ok  
4. Set 'tab' as 3 spaces:
   menu Window -> Preferences -> General -> Editors -> Text Editors -> 
      -> Displayed tab width: 3
      -> Insert spaced for tabs: checked!

   4.1. To resolve the issue of tab with space in eclipse editor:
      Step-1. window-->preferences-->java-->code style-->formatter
      Step-2. click on configure project specific settings.. choose the project
      Step-3. check Enable project specific settings -Click on new -mention your profile name-->click ok.
      Step-4. Profile page will be poped up - Chose space only under tab policy label of indentation tab. -click apply and the ok.
      Step-5. Use ctrl+shift+f to format a java class which will replace tab with space.
