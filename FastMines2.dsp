# Microsoft Developer Studio Project File - Name="FastMines2" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Application" 0x0101

CFG=FastMines2 - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "FastMines2.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "FastMines2.mak" CFG="FastMines2 - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "FastMines2 - Win32 Release" (based on "Win32 (x86) Application")
!MESSAGE "FastMines2 - Win32 Debug" (based on "Win32 (x86) Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "FastMines2 - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /FR /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /o "NUL" /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /o "NUL" /win32
# ADD BASE RSC /l 0x422 /d "NDEBUG"
# ADD RSC /l 0x422 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /machine:I386
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /subsystem:windows /machine:I386

!ELSEIF  "$(CFG)" == "FastMines2 - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /YX /FD /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /FR /YX /FD /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /o "NUL" /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /o "NUL" /win32
# ADD BASE RSC /l 0x422 /d "_DEBUG"
# ADD RSC /l 0x422 /d "_DEBUG"
# SUBTRACT RSC /x
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# SUBTRACT BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# SUBTRACT LINK32 /pdb:none

!ENDIF 

# Begin Target

# Name "FastMines2 - Win32 Release"
# Name "FastMines2 - Win32 Debug"
# Begin Group "Figure"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\Figure\TcBase.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcBase.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcHexagon.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcHexagon.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcParquet1.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcParquet1.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcParquet2.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcParquet2.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagon.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagon.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagonT10.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagonT10.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagonT5.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcPentagonT5.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcQuadrangle1.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcQuadrangle1.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcRhombus.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcRhombus.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSqTrHex.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSqTrHex.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSquare1.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSquare1.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSquare2.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcSquare2.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid1.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid1.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid2.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid2.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid3.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrapezoid3.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle1.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle1.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle2.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle2.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle3.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle3.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle4.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTriangle4.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrSq.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrSq.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrSq2.cpp
# End Source File
# Begin Source File

SOURCE=.\Figure\TcTrSq2.h
# End Source File
# Begin Source File

SOURCE=.\Figure\TcVirtual.h
# End Source File
# End Group
# Begin Group "Dialog"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\Dialog\About.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\About.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Assistant.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Assistant.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Champions.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Champions.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\CustomSkill.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\CustomSkill.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Info.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Info.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\PlayerName.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\PlayerName.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Registration.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Registration.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\SelectFigure.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\SelectFigure.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Skin.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Skin.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\Statistics.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\Statistics.h
# End Source File
# End Group
# Begin Group "Others"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\EraseBk.cpp
# End Source File
# Begin Source File

SOURCE=.\EraseBk.h
# End Source File
# Begin Source File

SOURCE=.\FigureName.h
# End Source File
# Begin Source File

SOURCE=.\Lib.cpp
# End Source File
# Begin Source File

SOURCE=.\Lib.h
# End Source File
# Begin Source File

SOURCE=.\TcImage.cpp
# End Source File
# Begin Source File

SOURCE=.\TcImage.h
# End Source File
# End Group
# Begin Group "Control"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\Control\TcButtonImage.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\TcButtonImage.h
# End Source File
# Begin Source File

SOURCE=.\Control\TcButtonImageCheck.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\TcButtonImageCheck.h
# End Source File
# Begin Source File

SOURCE=.\Control\TcTable.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\TcTable.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\FastMines.rc
# End Source File
# Begin Source File

SOURCE=.\FastMines2.cpp
# End Source File
# Begin Source File

SOURCE=.\ID_resource.h
# End Source File
# Begin Source File

SOURCE=.\Preproc.h
# End Source File
# Begin Source File

SOURCE=.\TcMosaic.cpp
# End Source File
# Begin Source File

SOURCE=.\TcMosaic.h
# End Source File
# Begin Source File

SOURCE=.\TcRobot.cpp
# End Source File
# Begin Source File

SOURCE=.\TcRobot.h
# End Source File
# End Target
# End Project
