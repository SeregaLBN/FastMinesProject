////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SelectMosaic.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SELECTMOSAIC__
#define __FILE__SELECTMOSAIC__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../Mosaic.h"

namespace nsSelectMosaic {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   int GetGroup(nsMosaic::EMosaic);
   void SetFirstKey(int);
}

#endif // __FILE__SELECTMOSAIC__
