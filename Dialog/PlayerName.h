////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PlayerName.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_PLAYERNAME
#define FILE_PLAYERNAME

#include "..\Preproc.h"
#include <windows.h>
#include <tchar.h>

namespace nsPlayerName {
   extern bool firstLoad;

   const int maxPlayerNameLength = 20;
   const int maxPasswordLength = 20;
   const TCHAR szRobotNameDefault [maxPlayerNameLength] = TEXT("Assistant");

   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
}

#endif // FILE_PLAYERNAME
