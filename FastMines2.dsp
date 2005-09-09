# Microsoft Developer Studio Project File - Name="FastMines2" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Application" 0x0101

CFG=FastMines2 - Win32 Debug Unicode
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "FastMines2.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "FastMines2.mak" CFG="FastMines2 - Win32 Debug Unicode"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "FastMines2 - Win32 Release Unicode" (based on "Win32 (x86) Application")
!MESSAGE "FastMines2 - Win32 Release" (based on "Win32 (x86) Application")
!MESSAGE "FastMines2 - Win32 Debug Unicode" (based on "Win32 (x86) Application")
!MESSAGE "FastMines2 - Win32 Debug" (based on "Win32 (x86) Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "FastMines2 - Win32 Release Unicode"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release_U"
# PROP BASE Intermediate_Dir "Release_U"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release_U"
# PROP Intermediate_Dir "Release_U"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /Yu"stdafx.h" /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "UNICODE" /D "_UNICODE" /FR /Yu"stdafx.h" /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x422 /d "NDEBUG"
# ADD RSC /l 0x422 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /subsystem:windows /machine:I386
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /version:2.20 /subsystem:windows /machine:I386

!ELSEIF  "$(CFG)" == "FastMines2 - Win32 Release"

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
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /Yu"stdafx.h" /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /Yu"stdafx.h" /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x422 /d "NDEBUG"
# ADD RSC /l 0x422 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /machine:I386
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /version:2.20 /subsystem:windows /machine:I386

!ELSEIF  "$(CFG)" == "FastMines2 - Win32 Debug Unicode"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug_U"
# PROP BASE Intermediate_Dir "Debug_U"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug_U"
# PROP Intermediate_Dir "Debug_U"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /Yu"stdafx.h" /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "UNICODE" /D "_UNICODE" /Yu"stdafx.h" /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x422 /d "_DEBUG"
# ADD RSC /l 0x422 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /version:2.20 /subsystem:windows /debug /machine:I386 /pdbtype:sept

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
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /Yu"stdafx.h" /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /FR /Yu"stdafx.h" /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x422 /d "_DEBUG"
# ADD RSC /l 0x422 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# ADD LINK32 user32.lib gdi32.lib comdlg32.lib advapi32.lib shell32.lib comctl32.lib /nologo /version:2.20 /subsystem:windows /debug /machine:I386 /pdbtype:sept

!ENDIF 

# Begin Target

# Name "FastMines2 - Win32 Release Unicode"
# Name "FastMines2 - Win32 Release"
# Name "FastMines2 - Win32 Debug Unicode"
# Name "FastMines2 - Win32 Debug"
# Begin Group "Mosaic"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\Mosaic\Base.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Base.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Hexagon1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Hexagon1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Parquet1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Parquet1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Parquet2.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Parquet2.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT10.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT10.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT24.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT24.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT5.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\PentagonT5.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Quadrangle1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Quadrangle1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Rhombus1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Rhombus1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\SqTrHex.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\SqTrHex.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Square1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Square1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Square2.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Square2.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid2.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid2.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid3.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Trapezoid3.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle2.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle2.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle3.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle3.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle4.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\Triangle4.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\TrSq1.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\TrSq1.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic\TrSq2.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic\TrSq2.h
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

SOURCE=.\Dialog\AssistantDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\AssistantDlg.h
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

SOURCE=.\Dialog\PlayerName.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\PlayerName.h
# End Source File
# Begin Source File

SOURCE=.\Dialog\SelectMosaic.cpp
# End Source File
# Begin Source File

SOURCE=.\Dialog\SelectMosaic.h
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
# Begin Group "Control"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\Control\ButtonImage.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\ButtonImage.h
# End Source File
# Begin Source File

SOURCE=.\Control\ButtonImageCheck.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\ButtonImageCheck.h
# End Source File
# Begin Source File

SOURCE=.\Control\CaptionButton.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\CaptionButton.h
# End Source File
# Begin Source File

SOURCE=.\Control\MenuItem.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\MenuItem.h
# End Source File
# Begin Source File

SOURCE=.\Control\Table.cpp
# End Source File
# Begin Source File

SOURCE=.\Control\Table.h
# End Source File
# End Group
# Begin Group "Others"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\__MyLib__\CommonLib.cpp
# End Source File
# Begin Source File

SOURCE=..\..\__MyLib__\CommonLib.h
# End Source File
# Begin Source File

SOURCE=..\..\__MyLib__\CStringKS.h
# End Source File
# Begin Source File

SOURCE=.\EraseBk.cpp
# End Source File
# Begin Source File

SOURCE=.\EraseBk.h
# End Source File
# Begin Source File

SOURCE=.\Image.cpp
# End Source File
# Begin Source File

SOURCE=.\Image.h
# End Source File
# Begin Source File

SOURCE=.\Lang.cpp
# End Source File
# Begin Source File

SOURCE=.\Lang.h
# End Source File
# Begin Source File

SOURCE=..\..\__MyLib__\Logger.cpp
# End Source File
# Begin Source File

SOURCE=..\..\__MyLib__\Logger.h
# End Source File
# Begin Source File

SOURCE=.\OldVersion.h
# End Source File
# Begin Source File

SOURCE=.\StorageMines.cpp
# End Source File
# Begin Source File

SOURCE=.\StorageMines.h
# End Source File
# Begin Source File

SOURCE=.\WinDefAdd.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\Assistant.cpp
# End Source File
# Begin Source File

SOURCE=.\Assistant.h
# End Source File
# Begin Source File

SOURCE=.\FastMines.rc
# End Source File
# Begin Source File

SOURCE=.\FastMines2.cpp
# End Source File
# Begin Source File

SOURCE=.\FastMines2.h
# End Source File
# Begin Source File

SOURCE=.\ID_resource.h
# End Source File
# Begin Source File

SOURCE=.\Mosaic.cpp
# End Source File
# Begin Source File

SOURCE=.\Mosaic.h
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicHexagon1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicNull.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicParquet1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicParquet2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicPentagonT10.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicPentagonT24.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicPentagonT5.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicQuadrangle1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicRhombus1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicSqTrHex.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicSquare1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicSquare2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTrapezoid1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTrapezoid2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTrapezoid3.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTriangle1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTriangle2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTriangle3.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTriangle4.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTrSq1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\MosaicTrSq2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\new0.ico
# End Source File
# Begin Source File

SOURCE=.\Image\new1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\new2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\new3.ico
# End Source File
# Begin Source File

SOURCE=.\Image\pause0.ico
# End Source File
# Begin Source File

SOURCE=.\Image\pause1.ico
# End Source File
# Begin Source File

SOURCE=.\Image\pause2.ico
# End Source File
# Begin Source File

SOURCE=.\Image\pause3.ico
# End Source File
# Begin Source File

SOURCE=.\Image\project.ico
# End Source File
# Begin Source File

SOURCE=.\StdAfx.cpp
# ADD CPP /Yc"stdafx.h"
# End Source File
# Begin Source File

SOURCE=.\StdAfx.h
# End Source File
# End Target
# End Project
