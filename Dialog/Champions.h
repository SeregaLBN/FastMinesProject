////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Champions.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CHAMPIONS__
#define __FILE__CHAMPIONS__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../Mosaic.h"

namespace nsChampions {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void SaveResult(const nsMosaic::EMosaic mosaic, const nsMosaic::ESkillLevel skill, const int time, LPCTSTR szName);
}

#endif // __FILE__CHAMPIONS__
