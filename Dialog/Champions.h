////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Champions.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_CHAMPIONS
#define FILE_CHAMPIONS

#include "..\Preproc.h"
#include <windows.h>
#include "..\TcMosaic.h"

namespace nsChampions {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void SaveResult(const TeFigure, const TeSkillLevel, const int, LPCTSTR szName);
}

#endif // FILE_CHAMPIONS
