////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "CustomSkill.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CUSTOMSKILL__
#define __FILE__CUSTOMSKILL__

#ifndef __AFX_H__
   #include <Windows.h>
#endif

namespace nsCustomSkill {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   extern int   curMines;
   extern COORD curSizeMosaic;
}

#endif // __FILE__CUSTOMSKILL__
