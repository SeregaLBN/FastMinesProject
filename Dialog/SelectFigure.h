////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SelectFigure.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_SELECTFIGURE
#define FILE_SELECTFIGURE

#include "..\Preproc.h"
#include <windows.h>
#include <tchar.h>
#include "..\TcMosaic.h"

namespace nsSelectFigure {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   int GetGroup(TeFigure);
   void SetFirstKey(int);

   extern const TCHAR MosaicName[][64];
}

#endif // FILE_SELECTFIGURE
