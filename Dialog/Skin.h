////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Skin.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SKIN__
#define __FILE__SKIN__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../FastMines2.h"

namespace nsChangeSkin {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
}

namespace nsFileSkin {
   BOOL  CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void  LoadMenuList  (HMENU);
   void  ReloadMenuList(HMENU);
   CSkin LoadSkin      (HMENU, UINT);
}

#endif // __FILE__SKIN__
